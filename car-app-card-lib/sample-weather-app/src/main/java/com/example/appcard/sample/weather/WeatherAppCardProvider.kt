/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appcard.sample.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.icu.util.MeasureUnit
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import com.android.car.appcard.AppCardContext
import com.android.car.appcard.ImageAppCard
import com.android.car.appcard.component.Header
import com.android.car.appcard.component.Image
import com.example.appcard.sample.weather.model.ForecastResponse
import com.example.appcard.sample.weather.model.Period
import com.example.appcard.sample.weather.model.PointsResponse
import com.google.gson.GsonBuilder
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class WeatherAppCardProvider(
  val context: Context,
  val id: String,
  val update: SimpleAppCardContentProvider.AppCardUpdater,
) {
  private lateinit var latestAppCardContext: AppCardContext
  private var locationManager: LocationManager? = null
  private var timer = Timer()
  private var timerSetup = AtomicBoolean(false)
  private var currLocation: Location? = null
  private var pointsResponse: PointsResponse? = null
  private var pointsDisposable: Disposable? = null
  private var forecastResponse: ForecastResponse? = null
  private var forecastDisposable: Disposable? = null
  private var carTemperatureUnit: MeasureUnit? = null
  private var latestPeriod: Period? = null

  init {
    if (permissionGranted()) {
      initLocationManager()
    }
  }

  fun setTempUnit(newMeasureUnit: MeasureUnit) {
    carTemperatureUnit = newMeasureUnit
    latestPeriod?.let {
      update.sendUpdate(getAppCard(it))
    }
  }

  private fun permissionGranted(): Boolean {
    val coarseLoc = ActivityCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!coarseLoc) {
      Log.e(TAG, "ACCESS_COARSE_LOCATION not granted")
    }

    val bgLoc = ActivityCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_BACKGROUND_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!bgLoc) {
      Log.e(TAG, "ACCESS_BACKGROUND_LOCATION not granted")
    }

    val fineLoc = ActivityCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!fineLoc) {
      Log.e(TAG, "ACCESS_FINE_LOCATION not granted")
    }

    return coarseLoc && bgLoc && fineLoc
  }

  fun getAppCard(appCardContext: AppCardContext): ImageAppCard {
    latestAppCardContext = appCardContext

    if (!timerSetup.getAndSet(true)) {
      timer.scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
          latestAppCardContext.let {
            update.sendUpdate(getAppCard(it))
          }
        }
      }, MINUTE_IN_MS, MINUTE_IN_MS)
    }

    if (permissionGranted()) {
      locationManager ?: initLocationManager()
    } else {
      return getGrantPermissionAppCard()
    }

    val loc = getCurrentLocation()
    getWeatherPoints(location = loc)?.subscribeOn(Schedulers.newThread())?.subscribe(
      object : SingleObserver<PointsResponse> {
        override fun onSubscribe(d: Disposable) {
          pointsDisposable?.dispose()
          pointsDisposable = d
        }

        override fun onError(e: Throwable) {
          logIfDebuggable("getWeatherPoints error: $e")
        }

        override fun onSuccess(response: PointsResponse) {
          logIfDebuggable("getWeatherPoints: $response")
          pointsResponse = response

          val gridId = pointsResponse?.properties?.gridId ?: return
          val gridY = pointsResponse?.properties?.gridY ?: return
          val gridX = pointsResponse?.properties?.gridX ?: return

          getWeatherForecast(gridId, gridX, gridY).subscribeOn(Schedulers.newThread()).subscribe(
            object : SingleObserver<ForecastResponse> {
              override fun onSubscribe(d: Disposable) {
                forecastDisposable?.dispose()
                forecastDisposable = d
              }

              override fun onError(e: Throwable) {
                logIfDebuggable("getWeatherForecast error: $e")
              }

              override fun onSuccess(response: ForecastResponse) {
                logIfDebuggable("getWeatherForecast: $response")
                forecastResponse = response
                val periods = forecastResponse?.properties?.periods ?: run {
                  update.sendUpdate(getErrorAppCard(ERROR_PERIODS))
                  return
                }
                val period = periods[0] ?: run {
                  update.sendUpdate(getErrorAppCard(ERROR_FIRST_PERIOD))
                  return
                }
                update.sendUpdate(getAppCard(period))
              }
            }
          )
        }
      })
    return loading()
  }

  private fun Int.convertTemp(from: MeasureUnit, to: MeasureUnit): Int = if (from == to) {
    this
  } else if (to == MeasureUnit.CELSIUS) {
    TemperatureConverter.convertFahrenheitToCelsius(this.toFloat()).roundToInt()
  } else {
    TemperatureConverter.convertCelsiusToFahrenheit(this.toFloat()).roundToInt()
  }

  private fun getAppCard(period: Period): ImageAppCard {
    period.temperature ?: return getErrorAppCard(INVALID_TEMP)
    period.temperatureUnit ?: return getErrorAppCard(INVALID_TEMP)

    latestPeriod = period
    val temperatureUnit = if (period.temperatureUnit == TEMP_F_UNIT) {
      MeasureUnit.FAHRENHEIT
    } else {
      MeasureUnit.CELSIUS
    }
    val temperature = carTemperatureUnit?.let {
      period.temperature.convertTemp(temperatureUnit, it)
    } ?: period.temperature
    val temperatureUnitText = carTemperatureUnit?.let {
      if (it == MeasureUnit.FAHRENHEIT) {
        TEMP_F_UNIT
      } else {
        TEMP_C_UNIT
      }
    } ?: period.temperatureUnit
    val primaryText = period.temperatureTrend?.let {
      if (it == TEMP_RISING) {
        "$temperature $temperatureUnitText $TEMP_RISING_ICON"
      } else {
        "$temperature $temperatureUnitText $TEMP_FALLING_ICON"
      }
    } ?: "$temperature $temperatureUnit"
    return ImageAppCard.newBuilder(id)
      .setPrimaryText(primaryText)
      .setSecondaryText(period.shortForecast ?: period.detailedForecast ?: FORECAST_NOT_FOUND)
      .setHeader(getHeader())
      .setImage(
        Image.newBuilder(IMAGE_ID)
          .setContentScale(Image.ContentScale.FILL_BOUNDS)
          .setColorFilter(Image.ColorFilter.TINT)
          .setImageData(getImageAccordingToIconUri(period.icon, period.isDaytime))
          .build()
      )
      .build()
  }

  private fun getHeader(): Header {
    val imageSize = latestAppCardContext.imageAppCardContext.getMaxImageSize(Header::class.java)
    return Header.newBuilder(HEADER_ID)
      .setTitle(WEATHER_HEADER)
      .setImage(
        Image.newBuilder(HEADER_IMAGE_ID)
          .setImageData(resToBitmap(R.drawable.ic_icon, imageSize.width, imageSize.height))
          .setColorFilter(Image.ColorFilter.TINT)
          .setContentScale(Image.ContentScale.FILL_BOUNDS)
          .build()
      )
      .build()
  }

  private fun getImageAccordingToIconUri(icon: String?, isDaylight: Boolean?): Bitmap {
    val imageSize =
      latestAppCardContext.imageAppCardContext.getMaxImageSize(ImageAppCard::class.java)
    icon ?: return resToBitmap(R.drawable.ic_error, imageSize.width, imageSize.height)
    val uri = Uri.parse(icon)
    logIfDebuggable("URI Paths: ${uri.pathSegments}")
    val dayTime = isDaylight ?: (uri.pathSegments[2] == "day")
    logIfDebuggable("isDaylight: $dayTime")
    val code = uri.pathSegments[3]
    val resId = IconUriUtility.getRes(code, dayTime)
    return resToBitmap(resId, imageSize.width, imageSize.height)
  }

  private fun getWeatherPoints(location: Location?): Single<PointsResponse>? {
    val numDecimal = 4
    val latitude = location?.latitude?.round(numDecimal) ?: return null
    val longitude = location.longitude.round(numDecimal)
    return getApiService().getPoints(latitude, longitude)
  }

  private fun getWeatherForecast(gridId: String, gridX: Int, gridY: Int): Single<ForecastResponse> {
    return getApiService().getForecast(gridId, gridX, gridY)
  }

  private fun getErrorAppCard(errorMsg: String): ImageAppCard {
    val imageSize =
      latestAppCardContext.imageAppCardContext.getMaxImageSize(ImageAppCard::class.java)
    return ImageAppCard.newBuilder(id)
      .setHeader(getHeader())
      .setPrimaryText(ERROR_PRIMARY)
      .setSecondaryText(errorMsg)
      .setImage(
        Image.newBuilder(IMAGE_ID)
          .setContentScale(Image.ContentScale.FILL_BOUNDS)
          .setColorFilter(Image.ColorFilter.TINT)
          .setImageData(resToBitmap(R.drawable.ic_error, imageSize.width, imageSize.height))
          .build()
      )
      .build()
  }

  private fun loading(): ImageAppCard {
    val imageSize =
      latestAppCardContext.imageAppCardContext.getMaxImageSize(ImageAppCard::class.java)
    return ImageAppCard.newBuilder(id)
      .setHeader(getHeader())
      .setPrimaryText(LOADING_PRIMARY)
      .setSecondaryText(LOADING_SECONDARY)
      .setImage(
        Image.newBuilder(IMAGE_ID)
          .setContentScale(Image.ContentScale.FILL_BOUNDS)
          .setColorFilter(Image.ColorFilter.TINT)
          .setImageData(resToBitmap(R.drawable.ic_loading, imageSize.width, imageSize.height))
          .build()
      )
      .build()
  }

  private fun getGrantPermissionAppCard(): ImageAppCard {
    val imageSize =
      latestAppCardContext.imageAppCardContext.getMaxImageSize(ImageAppCard::class.java)
    return ImageAppCard.newBuilder(id)
      .setHeader(getHeader())
      .setPrimaryText(ERROR_PRIMARY)
      .setSecondaryText(LOCATION_PERMISSION_SECONDARY)
      .setImage(
        Image.newBuilder(IMAGE_ID)
          .setContentScale(Image.ContentScale.FILL_BOUNDS)
          .setColorFilter(Image.ColorFilter.TINT)
          .setImageData(resToBitmap(R.drawable.ic_location_off, imageSize.width, imageSize.height))
          .build()
      )
      .build()
  }

  private fun getOkHttpClient(): OkHttpClient {
    val logger = HttpLoggingInterceptor().also {
      it.level = if (Log.isLoggable(TAG, Log.DEBUG)) {
        HttpLoggingInterceptor.Level.BASIC
      } else {
        HttpLoggingInterceptor.Level.NONE
      }
    }

    return OkHttpClient.Builder()
      .addInterceptor(logger)
      .addNetworkInterceptor(UserAgentInterceptor())
      .build()
  }

  class UserAgentInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
      val originalRequest: Request = chain.request()
      val requestWithUserAgent: Request = originalRequest.newBuilder()
        .header(USER_AGENT_HEADER, USER_AGENT_VALUE)
        .build()
      return chain.proceed(requestWithUserAgent)
    }
  }

  private fun getApiService(): ApiService {
    return Retrofit.Builder()
      .client(getOkHttpClient())
      .validateEagerly(true)
      .baseUrl(ApiService.URL)
      .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
      .create(ApiService::class.java)
  }

  fun destroy() {
    timer.cancel()
    timer = Timer()
    timerSetup.set(false)
  }

  @SuppressLint("MissingPermission")
  private fun initLocationManager() {
    locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
  }

  @SuppressLint("MissingPermission")
  private fun getCurrentLocation(): Location? {
    val cancellationSignal = null
    currLocation =
      currLocation ?: locationManager?.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
    locationManager?.getCurrentLocation(
      LocationManager.FUSED_PROVIDER,
      cancellationSignal,
      context.mainExecutor
    ) {
      currLocation?.let { curr ->
        if (it.latitude != curr.latitude || it.longitude != curr.longitude) {
          logIfDebuggable("Location received: $it")
          currLocation = curr
          update.sendUpdate(getAppCard(latestAppCardContext))
        }
      } ?: run {
        currLocation = it
      }
    }
    return currLocation
  }

  private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
    val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(bitmap)
    val left = 0
    val top = 0
    drawable.setBounds(left, top, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  private fun resToBitmap(res: Int, width: Int, height: Int): Bitmap {
    val drawable = context.getDrawable(res)

    return drawableToBitmap(drawable!!, width, height)
  }

  private fun Double.round(numDecimal: Int): Double {
    val multiplier = 10.0.pow(numDecimal.toDouble())
    return floor(this * multiplier) / multiplier
  }

  private fun logIfDebuggable(msg: String) {
    if (Log.isLoggable(TAG, Log.DEBUG)) {
      Log.d(TAG, msg)
    }
  }

  companion object {
    private const val TAG = "WeatherAppCardProvider"
    private const val MINUTE_IN_MS = 60000L
    private const val HEADER_ID = "HEADER_ID"
    private const val IMAGE_ID = "IMAGE_ID"
    private const val HEADER_IMAGE_ID = "HEADER_IMAGE_ID"
    private const val ERROR_PERIODS = "Forecast periods not found"
    private const val ERROR_FIRST_PERIOD = "First period not found"
    private const val INVALID_TEMP = "Invalid temperature received"
    private const val USER_AGENT_HEADER = "User-Agent"
    private const val USER_AGENT_VALUE = "Sample Weather App Card"
    private const val ERROR_PRIMARY = "Error"
    private const val LOCATION_PERMISSION_SECONDARY = "Location permission required"
    private const val LOADING_PRIMARY = "Loading..."
    private const val LOADING_SECONDARY = ""
    private const val WEATHER_HEADER = "Weather"
    private const val TEMP_RISING = "rising"
    private const val TEMP_RISING_ICON = "↑"
    private const val TEMP_FALLING_ICON = "↓"
    private const val TEMP_F_UNIT = "F"
    private const val TEMP_C_UNIT = "C"
    private const val FORECAST_NOT_FOUND = "Forecast Unavailable"
  }
}
