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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.appcard.ImageAppCard
import com.google.common.truth.Truth.assertThat
import java.util.Timer
import java.util.TimerTask
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class AppCardTimerTest {
  private var actualIdentifier: ApplicationIdentifier? = null
  private var actualAppCardId: String? = null
  private val updateReadyListener = object : AppCardTimer.UpdateReadyListener {
    override fun appCardIsReadyForUpdate(
      identifier: ApplicationIdentifier?,
      appCardId: String?
    ) {
      actualIdentifier = identifier
      actualAppCardId = appCardId
    }
  }
  private lateinit var appCardTimer: AppCardTimer
  private val timer: Timer = mock<Timer>()
  private val identifier = ApplicationIdentifier(TEST_AUTHORITY, TEST_PACKAGE)
  private val timerFactory = object : AppCardTimer.TimerFactory {
    override fun getTimer() = timer
  }

  @Before
  fun setup() {
    appCardTimer = AppCardTimer(
      updateReadyListener,
      TEST_UPDATE_RATE_MS,
      TEST_FAST_UPDATE_RATE_MS,
      timerFactory
    )
  }

  @Test
  fun testUpdateAppCard_imageAppCard_timerCancelled() {
    appCardTimer.updateAppCard(AppCardContainer(identifier, ImageAppCardUtility.imageAppCard))

    verify(timer).cancel()
  }

  @Test
  fun testUpdateAppCard_imageAppCard_withProgressBar_componentTimerScheduled() {
    appCardTimer.updateAppCard(
      AppCardContainer(identifier, ImageAppCardUtility.progressBarButtonCard)
    )

    val captor = argumentCaptor<TimerTask>()
    verify(timer).schedule(captor.capture(), eq(TEST_FAST_UPDATE_RATE_MS.toLong()))
    captor.firstValue.run()
    assertThat(
      appCardTimer.isComponentReadyForUpdate(ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID)
    ).isTrue()
  }

  @Test
  fun testUpdateAppCard_imageAppCard_withProgressBar_appCardTimerScheduled() {
    appCardTimer.updateAppCard(AppCardContainer(identifier, ImageAppCardUtility.imageAppCard))

    val captor = argumentCaptor<TimerTask>()
    verify(timer).schedule(
      captor.capture(),
      eq(TEST_UPDATE_RATE_MS.toLong()),
      eq(TEST_UPDATE_RATE_MS.toLong())
    )
    captor.firstValue.run()
    assertThat(actualIdentifier).isEqualTo(identifier)
    assertThat(actualAppCardId).isEqualTo(ImageAppCardUtility.TEST_ID)
  }

  @Test
  fun testUpdateAppCard_imageAppCard_withoutProgressBar_componentTimerNotScheduled() {
    val imageAppCard = ImageAppCard.newBuilder(ImageAppCardUtility.TEST_ID)
      .setImage(ImageAppCardUtility.image)
      .setHeader(ImageAppCardUtility.header)
      .setPrimaryText(ImageAppCardUtility.TEST_PRIMARY_TEXT)
      .setSecondaryText(ImageAppCardUtility.TEST_SECONDARY_TEXT)
      .build()

    appCardTimer.updateAppCard(AppCardContainer(identifier, imageAppCard))

    verify(timer, never()).schedule(any<TimerTask>(), eq(TEST_FAST_UPDATE_RATE_MS.toLong()))
  }

  @Test
  fun testIsComponentReadyForUpdate_nonExistingComponent_returnFalse() {
    assertThat(appCardTimer.isComponentReadyForUpdate(TEST_COMPONENT_ID)).isFalse()
  }

  @Test
  fun testIsComponentReadyForUpdate_existingComponent_returnTrue() {
    appCardTimer.updateAppCard(
      AppCardContainer(identifier, ImageAppCardUtility.progressBarButtonCard)
    )
    val captor = argumentCaptor<TimerTask>()
    verify(timer).schedule(captor.capture(), eq(TEST_FAST_UPDATE_RATE_MS.toLong()))
    captor.firstValue.run()

    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isTrue()
  }

  @Test
  fun testDestroy_timerCancelled() {
    appCardTimer.destroy()

    verify(timer).cancel()
  }

  @Test
  fun testResetAppCardTimerAndRequestUpdate_noAppCard_timerCancelNotCalled() {
    appCardTimer.resetAppCardTimerAndRequestUpdate()

    verify(timer, never()).cancel()
  }

  @Test
  fun testResetAppCardTimerAndRequestUpdate_noAppCard_listenerNotCalled() {
    appCardTimer.resetAppCardTimerAndRequestUpdate()

    assertThat(actualIdentifier).isNull()
  }

  @Test
  fun testResetAppCardTimerAndRequestUpdate_noAppCard_timerScheduleNotCalled() {
    appCardTimer.resetAppCardTimerAndRequestUpdate()

    verify(timer, never()).schedule(any<TimerTask>(), any<Long>())
  }

  @Test
  fun testResetAppCardTimerAndRequestUpdate_appCard_timerCancelled() {
    appCardTimer.updateAppCard(AppCardContainer(identifier, ImageAppCardUtility.imageAppCard))
    reset(timer)

    appCardTimer.resetAppCardTimerAndRequestUpdate()

    verify(timer).cancel()
  }

  @Test
  fun testResetAppCardTimerAndRequestUpdate_appCard_componentStatusFalse() {
    appCardTimer.updateAppCard(
      AppCardContainer(identifier, ImageAppCardUtility.progressBarButtonCard)
    )
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isFalse()
    val captor = argumentCaptor<TimerTask>()
    verify(timer).schedule(captor.capture(), eq(TEST_FAST_UPDATE_RATE_MS.toLong()))
    reset(timer)
    captor.firstValue.run()
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isTrue()

    appCardTimer.resetAppCardTimerAndRequestUpdate()

    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isFalse()
  }

  @Test
  fun testResetAppCardTimerAndRequestUpdate_appCard_componentTimerTaskSetStatusToTrue() {
    appCardTimer.updateAppCard(
      AppCardContainer(identifier, ImageAppCardUtility.progressBarButtonCard)
    )
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isFalse()
    val captor = argumentCaptor<TimerTask>()
    verify(timer).schedule(captor.capture(), eq(TEST_FAST_UPDATE_RATE_MS.toLong()))
    reset(timer)
    captor.firstValue.run()
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isTrue()

    appCardTimer.resetAppCardTimerAndRequestUpdate()

    verify(timer).schedule(captor.capture(), eq(TEST_FAST_UPDATE_RATE_MS.toLong()))
    captor.secondValue.run()
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isTrue()
  }

  @Test
  fun testResetAppCardTimerAndRequestUpdate_appCard_appCardTimerTaskCorrectlySet() {
    appCardTimer.updateAppCard(AppCardContainer(identifier, ImageAppCardUtility.imageAppCard))
    reset(timer)

    appCardTimer.resetAppCardTimerAndRequestUpdate()

    val captor = argumentCaptor<TimerTask>()
    verify(timer).schedule(
      captor.capture(),
      eq(TEST_UPDATE_RATE_MS.toLong()),
      eq(TEST_UPDATE_RATE_MS.toLong())
    )
    captor.firstValue.run()
    assertThat(actualIdentifier).isEqualTo(identifier)
    assertThat(actualAppCardId).isEqualTo(ImageAppCardUtility.TEST_ID)
  }

  @Test
  fun testComponentUpdate_nonExistingComponent_timerNotScheduled() {
    appCardTimer.componentUpdate(TEST_COMPONENT_ID)

    verify(timer, never()).schedule(any<TimerTask>(), any<Long>())
  }

  @Test
  fun testComponentUpdate_existingComponent_timerScheduled() {
    appCardTimer.updateAppCard(
      AppCardContainer(identifier, ImageAppCardUtility.progressBarButtonCard)
    )
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isFalse()
    val captor = argumentCaptor<TimerTask>()
    verify(timer).schedule(captor.capture(), eq(TEST_FAST_UPDATE_RATE_MS.toLong()))
    reset(timer)
    captor.firstValue.run()
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isTrue()

    appCardTimer.componentUpdate(ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID)

    verify(timer).schedule(captor.capture(), eq(TEST_FAST_UPDATE_RATE_MS.toLong()))
    captor.secondValue.run()
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isTrue()
  }

  @Test
  fun testComponentUpdate_existingComponent_componentUpdateStatusIsFalse() {
    appCardTimer.updateAppCard(
      AppCardContainer(identifier, ImageAppCardUtility.progressBarButtonCard)
    )
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isFalse()
    val captor = argumentCaptor<TimerTask>()
    verify(timer).schedule(captor.capture(), eq(TEST_FAST_UPDATE_RATE_MS.toLong()))
    reset(timer)
    captor.firstValue.run()
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isTrue()

    appCardTimer.componentUpdate(ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID)

    verify(timer).schedule(captor.capture(), eq(TEST_FAST_UPDATE_RATE_MS.toLong()))
    assertThat(
      appCardTimer.isComponentReadyForUpdate(
        ImageAppCardUtility.TEST_PROGRESS_BAR_COMPONENT_ID
      )
    ).isFalse()
  }

  companion object {
    private const val TEST_UPDATE_RATE_MS = 5000
    private const val TEST_FAST_UPDATE_RATE_MS = 500
    private const val TEST_COMPONENT_ID = "TEST_COMPONENT_ID"
    private const val TEST_AUTHORITY = "TEST_AUTHORITY"
    private const val TEST_PACKAGE = "TEST_PACKAGE"
  }
}
