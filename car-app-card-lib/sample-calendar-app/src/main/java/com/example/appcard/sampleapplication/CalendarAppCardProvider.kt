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

package com.example.appcard.sampleapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Size
import com.android.car.appcard.AppCardContext
import com.android.car.appcard.ImageAppCard
import com.android.car.appcard.component.Button
import com.android.car.appcard.component.Header
import com.android.car.appcard.component.Image
import com.android.car.appcard.component.ProgressBar
import com.android.car.appcard.component.interaction.OnClickListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

/** Provides [AppCard]s according to provided configurables */
class CalendarAppCardProvider(
    val context: Context,
    val id: String,
    val update: SimpleAppCardContentProvider.AppCardUpdater,
    var is24hr: Boolean = false,
    var clockMode: Boolean = false,
    val isInteractable: Boolean = true,
    val switchable: Boolean = true,
    val imageButtonNoBackground: Boolean = true,
    private val cal: Calendar = Calendar.getInstance(),
) {
    private lateinit var latestAppCardContext: AppCardContext

    private var timer = Timer()
    private var progressTimer: Timer? = null

    private var timerSetup = AtomicBoolean(false)
    private var progressTimerSetup = AtomicBoolean(false)

    private var calculatedIsInteractable = isInteractable

    /** @return [ImageAppCard] depending on configurables and given [AppCardContext] */
    fun getAppCard(appCardContext: AppCardContext): ImageAppCard {
        latestAppCardContext = appCardContext
        calculatedIsInteractable =
            isInteractable &&
                latestAppCardContext.isInteractable &&
                latestAppCardContext.imageAppCardContext.minimumGuaranteedButtons > 0

        // reset calendar time
        cal.timeInMillis = cal.time.time + (System.currentTimeMillis() - cal.time.time)

        // create app card builder
        val builder =
            ImageAppCard.newBuilder(id)
                .setHeader(getHeader())
                .setPrimaryText(getPrimaryText())
                .setSecondaryText(getSecondaryText())

        if (!calculatedIsInteractable) {
            if (clockMode && !switchable) {
                builder.setProgressBar(getProgressBar())
            } else {
                builder.setImage(getImage(latestAppCardContext))
            }
        } else {
            // setup app card buttons depending on the number of minimum guaranteed buttons
            // the host supports
            val minButtons = latestAppCardContext.imageAppCardContext.minimumGuaranteedButtons
            var numButtons = 0
            if (switchable && minButtons > numButtons) {
                builder.addButton(getPrimaryButton(latestAppCardContext))
                numButtons = 1
            }
            if (clockMode && minButtons > numButtons) {
                builder.addButton(getSecondaryButton(latestAppCardContext))
            }

            // add progress bar if in clock mode
            if (clockMode) {
                builder.setProgressBar(getProgressBar())
            }
        }

        // setup update timer
        val msLeftInCurrMinute = (MINUTE_IN_MS - (cal.get(Calendar.SECOND) * SECONDS_TO_MS))
        if (!timerSetup.getAndSet(true)) {
            timer.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        latestAppCardContext.let { update.sendUpdate(getAppCard(it)) }
                    }
                },
                msLeftInCurrMinute,
                MINUTE_IN_MS,
            )
        }

        return builder.build()
    }

    /** Cleanup when provided [ImageAppCard] is no longer required */
    fun destroy() {
        timer.cancel()
        timer = Timer()
        progressTimer?.cancel()
        timerSetup.set(false)
        progressTimerSetup.set(false)
    }

    private fun getImage(ctx: AppCardContext): Image {
        val size = ctx.imageAppCardContext.getMaxImageSize(ImageAppCard::class.java)

        return Image.newBuilder(IMAGE_COMPONENT_ID).setImageData(getTextAsImage(size)).build()
    }

    private fun getTextAsImage(size: Size): Bitmap {
        val text = getImageText()

        val paint = Paint()
        paint.setColor(Color.WHITE)
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER

        val right: Int = size.width
        val bottom: Int = size.height
        val side = right.coerceAtLeast(bottom).toFloat()

        // approximate text size
        val testTextSize = SAMPLE_TEXT_SIZE
        paint.textSize = testTextSize
        val testRect = Rect()
        val start = 0
        paint.getTextBounds(text, start, text.length, testRect)
        val testTextWidth = testRect.width().toFloat()
        val textSizeModifier =
            if (clockMode) {
                CLOCK_MODE_TEXT_SIZE_MODIFIER
            } else {
                CALENDAR_MODE_TEXT_SIZE_MODIFIER
            }
        val desiredTextSize = (testTextSize * side / testTextWidth) * textSizeModifier
        paint.textSize = desiredTextSize

        // create image
        val fm: Paint.FontMetrics = paint.getFontMetrics()
        val y: Float = (side) / 2 - (fm.descent + fm.ascent) / 2
        val image =
            Bitmap.createBitmap(side.roundToInt(), side.roundToInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val x: Float = side * X_CANVAS_MODIFIER
        canvas.drawText(text, x, y, paint)
        return image
    }

    private fun getImageText(): String {
        return if (clockMode) {
            if (is24hr) {
                val format = SimpleDateFormat(TIME_FORMAT_24)
                format.format(cal.time)
            } else {
                val format = SimpleDateFormat(TIME_FORMAT_12)
                format.format(cal.time)
            }
        } else {
            cal.get(Calendar.DAY_OF_MONTH).toString()
        }
    }

    private fun getPrimaryText(): String {
        val day =
            cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG_FORMAT, Locale.getDefault())
        val month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.getDefault())

        return if (!clockMode) {
            if (!calculatedIsInteractable) {
                "$day • $month"
            } else {
                day
            }
        } else {
            if (is24hr) {
                val format = SimpleDateFormat(TIME_FORMAT_24)
                format.format(cal.time)
            } else {
                val format = SimpleDateFormat(TIME_FORMAT_12)
                format.format(cal.time)
            }
        }
    }

    private fun getSecondaryText(): String {
        return if (!calculatedIsInteractable) {
            if (clockMode) {
                SimpleDateFormat(DATE_FORMAT).format(cal.time)
            } else {
                cal[Calendar.YEAR].toString()
            }
        } else if (!clockMode) {
            SimpleDateFormat(DATE_FORMAT).format(cal.time)
        } else {
            SimpleDateFormat(DATE_FORMAT).format(cal.time)
        }
    }

    private fun getPrimaryButton(ctx: AppCardContext): Button {
        val size = ctx.imageAppCardContext.getMaxImageSize(Button::class.java)

        val icon =
            if (clockMode) {
                resToBitmap(R.drawable.ic_calendar, size.width, size.height)
            } else {
                resToBitmap(R.drawable.ic_clock, size.width, size.height)
            }

        val builder =
            Button.newBuilder(
                PRIMARY_BUTTON_COMPONENT_ID,
                if (imageButtonNoBackground) Button.ButtonType.NO_BACKGROUND
                else Button.ButtonType.PRIMARY,
                object : OnClickListener {
                    override fun onClick() {
                        // Switch mode when button is clicked
                        clockMode = !clockMode
                        if (!clockMode && progressTimerSetup.getAndSet(false)) {
                            progressTimer?.cancel()
                        }
                        update.sendUpdate(getAppCard(ctx))
                    }
                },
            )

        if (!imageButtonNoBackground)
            builder.setText(if (clockMode) CALENDAR_TITLE else CLOCK_TITLE)

        return builder
            .setImage(
                Image.newBuilder(PRIMARY_BUTTON_IMAGE_COMPONENT_ID)
                    .setContentScale(Image.ContentScale.FILL_BOUNDS)
                    .setImageData(icon)
                    .build()
            )
            .build()
    }

    private fun getSecondaryButton(ctx: AppCardContext): Button {
        return Button.newBuilder(
                SECONDARY_BUTTON_COMPONENT_ID,
                if (imageButtonNoBackground) Button.ButtonType.PRIMARY
                else Button.ButtonType.SECONDARY,
                object : OnClickListener {
                    override fun onClick() {
                        // Switch mode when button is clicked
                        is24hr = !is24hr
                        update.sendUpdate(getAppCard(ctx))
                    }
                },
            )
            .setText(if (is24hr) TITLE_TIME_12 else TITLE_TIME_24)
            .build()
    }

    private fun getHeader(): Header {
        val clockPreText = if (is24hr) TITLE_TIME_24 + SPACE else TITLE_TIME_12 + SPACE

        return Header.newBuilder(HEADER_COMPONENT_ID)
            .setImage(getHeaderImage())
            .setTitle((if (clockMode) clockPreText + CLOCK_TITLE else CALENDAR_TITLE))
            .build()
    }

    private fun getHeaderImage(): Image {
        val resId = if (clockMode) R.drawable.ic_clock else R.drawable.ic_calendar
        val size = latestAppCardContext.imageAppCardContext.getMaxImageSize(Header::class.java)

        return Image.newBuilder(HEADER_IMAGE_COMPONENT_ID)
            .setColorFilter(Image.ColorFilter.TINT)
            .setContentScale(Image.ContentScale.FILL_BOUNDS)
            .setImageData(resToBitmap(resId, size.width, size.height))
            .build()
    }

    private fun getProgressBar(): ProgressBar {
        if (!progressTimerSetup.getAndSet(true)) {
            progressTimer = Timer()
            progressTimer?.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        latestAppCardContext.let {
                            update.sendComponentUpdate(id, getProgressBar())
                        }
                    }
                },
                SECONDS_TO_MS,
                SECONDS_TO_MS,
            )
        }

        cal.timeInMillis = cal.time.time + (System.currentTimeMillis() - cal.time.time)

        return ProgressBar.newBuilder(PROGRESS_BAR_COMPONENT_ID, PROGRESS_BAR_MIN, PROGRESS_BAR_MAX)
            .setProgress(cal.get(Calendar.SECOND))
            .build()
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

    private fun resToBitmap(res: Int, width: Int, height: Int): Bitmap {
        val drawable = context.getDrawable(res)

        return drawableToBitmap(drawable!!, width, height)
    }

    companion object {
        private const val SECONDS_TO_MS = 1000L
        private const val MINUTE_IN_MS = 60000L
        private const val SAMPLE_TEXT_SIZE = 70f
        private const val X_CANVAS_MODIFIER = 0.5f
        private const val CALENDAR_MODE_TEXT_SIZE_MODIFIER = 0.5f
        private const val CLOCK_MODE_TEXT_SIZE_MODIFIER = 1f
        private const val PROGRESS_BAR_MIN = 0
        private const val PROGRESS_BAR_MAX = 60
        private const val IMAGE_COMPONENT_ID = "image"
        private const val PRIMARY_BUTTON_COMPONENT_ID = "primaryButton"
        private const val SECONDARY_BUTTON_COMPONENT_ID = "secondaryButton"
        private const val PRIMARY_BUTTON_IMAGE_COMPONENT_ID = "primaryButtonImage"
        private const val HEADER_COMPONENT_ID = "header"
        private const val HEADER_IMAGE_COMPONENT_ID = "headerImage"
        private const val PROGRESS_BAR_COMPONENT_ID = "progressBar"
        private const val TIME_FORMAT_24 = "HH:mm"
        private const val TIME_FORMAT_12 = "hh:mm a"
        private const val DATE_FORMAT = "MM/dd/yy"
        private const val CALENDAR_TITLE = "Calendar"
        private const val CLOCK_TITLE = "Clock"
        private const val TITLE_TIME_24 = "24-hr"
        private const val TITLE_TIME_12 = "12-hr"
        private const val SPACE = " "
    }
}
