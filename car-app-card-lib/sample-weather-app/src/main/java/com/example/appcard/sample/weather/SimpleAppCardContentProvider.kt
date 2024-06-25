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

import com.android.car.appcard.AppCard
import com.android.car.appcard.AppCardContentProvider
import com.android.car.appcard.AppCardContext
import com.android.car.appcard.component.Component

/** An [AppCardContentProvider] that supplies sample calendar app card */
class SimpleAppCardContentProvider : AppCardContentProvider() {
  private lateinit var weatherAppCardProvider: WeatherAppCardProvider

  override val authority: String = AUTHORITY

  private val appCardUpdater = object : AppCardUpdater {
    override fun sendUpdate(appCard: AppCard) = sendAppCardUpdate(appCard)

    override fun sendComponentUpdate(id: String, component: Component) =
      sendAppCardComponentUpdate(id, component)
  }

  /** Setup [AppCardContentProvider] and its constituents */
  override fun onCreate(): Boolean {
    val result = super.onCreate()
    check(context != null) { "Context cannot be null" }

    context?.let {
      weatherAppCardProvider = WeatherAppCardProvider(
        it.applicationContext,
        WEATHER_ID,
        appCardUpdater
      )
    }

    return result
  }

  /** Setup an [AppCard] that is being requested */
  override fun onAppCardAdded(id: String, ctx: AppCardContext): AppCard {
    return when (id) {
      WEATHER_ID -> weatherAppCardProvider.getAppCard(ctx)

      else -> throw IllegalStateException("Unidentified app card ID: $id")
    }
  }

  /** List of supported [AppCard] IDs */
  override val appCardIds: List<String> =
    listOf(WEATHER_ID).toMutableList()

  /** Clean up when an [AppCard] is removed */
  override fun onAppCardRemoved(id: String) {
    when (id) {
      WEATHER_ID -> weatherAppCardProvider.destroy()
    }
  }

  /** Handle an [AppCardContext] change for a particular [AppCard] ID */
  override fun onAppCardContextChanged(id: String, appCardContext: AppCardContext) {
    when (id) {
      WEATHER_ID -> sendAppCardUpdate(weatherAppCardProvider.getAppCard(appCardContext))
    }
  }

  interface AppCardUpdater {
    /** Queue up a full [AppCard] update */
    fun sendUpdate(appCard: AppCard)

    /** Queue an [AppCard] [Component] update */
    fun sendComponentUpdate(id: String, component: Component)
  }

  companion object {
    private const val AUTHORITY = "com.example.appcard.sample.weather"
    private const val WEATHER_ID = "sampleWeatherAppCard"
  }
}
