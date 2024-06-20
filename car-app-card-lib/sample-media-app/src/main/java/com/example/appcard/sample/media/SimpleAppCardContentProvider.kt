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

package com.example.appcard.sample.media

import android.content.Context
import com.android.car.appcard.AppCard
import com.android.car.appcard.AppCardContentProvider
import com.android.car.appcard.AppCardContext

/** An [AppCardContentProvider] that supplies a sample now playing media app card */
class SimpleAppCardContentProvider : AppCardContentProvider() {
  private var appContext: Context? = null
  private var mediaAppCard: MediaAppCardProvider? = null
  private val updater = object : AppCardUpdater {
    override fun sendUpdate(appCard: AppCard) {
      sendAppCardUpdate(appCard)
    }
  }

  override val authority: String
    get() = AUTHORITY

  /** Setup [AppCardContentProvider] and its constituents */
  override fun onCreate(): Boolean {
    super.onCreate()
    appContext = context?.applicationContext

    // We can use the [AppCardContentProvider] as a lifecycle owner
    mediaAppCard = MediaAppCardProvider(APP_CARD_ID, appContext!!, updater, lifecycleOwner = this)
    return true
  }

  /** Setup an [AppCard] that is being requested */
  override fun onAppCardAdded(id: String, ctx: AppCardContext): AppCard {
    return mediaAppCard!!.getAppCard(ctx)
  }

  /** List of supported [AppCard] IDs */
  override val appCardIds: List<String>
    get() = listOf(APP_CARD_ID)

  /** Clean up when an [AppCard] is removed */
  override fun onAppCardRemoved(id: String) {
    if (id == APP_CARD_ID) mediaAppCard?.appCardRemoved()
  }

  /** Handle an [AppCardContext] change for a particular [AppCard] ID */
  override fun onAppCardContextChanged(id: String, appCardContext: AppCardContext) {
    if (id == APP_CARD_ID) mediaAppCard?.updateAppCardContext(appCardContext)
  }

  companion object {
    private const val APP_CARD_ID = "mediaAppCard"
    private const val AUTHORITY = "com.example.appcard.sample.media"
  }

  internal interface AppCardUpdater {
    /** Queue up a full [AppCard] update */
    fun sendUpdate(appCard: AppCard)
  }
}
