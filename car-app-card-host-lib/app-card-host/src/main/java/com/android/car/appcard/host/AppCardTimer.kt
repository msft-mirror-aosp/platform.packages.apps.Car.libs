/*
 * Copyright 2024 The Android Open Source Project
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

package com.android.car.appcard.host

import com.android.car.appcard.ImageAppCard
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer

internal class AppCardTimer(
  private val listener: UpdateReadyListener,
  private val updateRateMs: Int,
  private val fastUpdateRateMs: Int,
  private val timerFactory: TimerFactory = object : TimerFactory {
    override fun getTimer() = Timer()
  }
) {
  private val componentUpdateStatusMap: ConcurrentMap<String, Boolean>
  private var identifier: ApplicationIdentifier? = null
  private var appCardId: String? = null
  private var refreshTimer = timerFactory.getTimer()

  init {
    componentUpdateStatusMap = ConcurrentHashMap()
  }

  fun updateAppCard(appCardContainer: AppCardContainer) {
    if (appCardContainer.appCard !is ImageAppCard) return

    handleImageAppCardUpdate(
      appCardContainer.appCard as ImageAppCard,
      appCardContainer.appId
    )
  }

  private fun handleImageAppCardUpdate(
    imageAppCard: ImageAppCard,
    id: ApplicationIdentifier
  ) {
    synchronized(lock = this) {
      refreshTimer.cancel()
      refreshTimer = timerFactory.getTimer()

      componentUpdateStatusMap.clear()

      identifier = id
      appCardId = imageAppCard.id

      imageAppCard.progressBar?.let {
        componentUpdateStatusMap[it.componentId] = false
        refreshTimer.schedule(
          object : TimerTask() {
            override fun run() {
              componentUpdateStatusMap[it.componentId] = true
            }
          },
          fastUpdateRateMs.toLong()
        )
      }

      refreshTimer.schedule(
        object : TimerTask() {
          override fun run() {
            listener.appCardIsReadyForUpdate(identifier, appCardId)
          }
        },
        updateRateMs.toLong(),
        updateRateMs.toLong()
      )
    }
  }

  fun resetAppCardTimerAndRequestUpdate() {
    synchronized(lock = this) {
      appCardId ?: return
      identifier ?: return

      refreshTimer.cancel()
      refreshTimer = timerFactory.getTimer()

      listener.appCardIsReadyForUpdate(identifier, appCardId)

      componentUpdateStatusMap.replaceAll { _, _ -> false }

      componentUpdateStatusMap.keys.forEach(Consumer { componentId: String ->
        refreshTimer.schedule(
          object : TimerTask() {
            override fun run() {
              componentUpdateStatusMap[componentId] = true
            }
          },
          fastUpdateRateMs.toLong()
        )
      })

      refreshTimer.schedule(
        object : TimerTask() {
          override fun run() {
            listener.appCardIsReadyForUpdate(identifier, appCardId)
          }
        },
        updateRateMs.toLong(),
        updateRateMs.toLong()
      )
    }
  }

  fun isComponentReadyForUpdate(componentId: String): Boolean {
    synchronized(lock = this) {
      val defaultValue = false
      return componentUpdateStatusMap.getOrDefault(componentId, defaultValue)
    }
  }

  fun destroy() {
    synchronized(lock = this) {
      refreshTimer.cancel()
      refreshTimer = timerFactory.getTimer()

      componentUpdateStatusMap.clear()
    }
  }

  fun componentUpdate(componentId: String) {
    synchronized(lock = this) {
      if (!componentUpdateStatusMap.containsKey(componentId)) return

      componentUpdateStatusMap[componentId] = false

      refreshTimer.schedule(
        object : TimerTask() {
          override fun run() {
            componentUpdateStatusMap[componentId] = true
          }
        },
        fastUpdateRateMs.toLong()
      )
    }
  }

  internal interface UpdateReadyListener {
    fun appCardIsReadyForUpdate(identifier: ApplicationIdentifier?, appCardId: String?)
  }

  internal interface TimerFactory {
    fun getTimer(): Timer
  }
}
