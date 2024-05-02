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
package com.android.car.appcard

import android.os.Bundle
import android.util.Size
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.appcard.AppCardContext.Companion.fromBundle
import com.android.car.appcard.component.Button
import com.android.car.appcard.component.Header
import com.android.car.appcard.component.ProgressBar
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppCardContextTest {
  private lateinit var appCardContentBundle: Bundle
  private lateinit var imageAppCardContextBundle: Bundle

  @Before
  fun setup() {
    appCardContentBundle = Bundle()
    appCardContentBundle.putInt(AppCardContext.BUNDLE_KEY_API_LEVEL, TEST_API_LEVEL)
    appCardContentBundle.putInt(AppCardContext.BUNDLE_KEY_REFRESH_PERIOD, TEST_REFRESH_PERIOD)
    appCardContentBundle.putInt(
      AppCardContext.BUNDLE_KEY_FAST_REFRESH_PERIOD,
      TEST_FAST_REFRESH_PERIOD
    )
    appCardContentBundle.putBoolean(AppCardContext.BUNDLE_KEY_INTERACTABLE, TEST_INTERACTABLE)

    imageAppCardContextBundle = Bundle()
    imageAppCardContextBundle.putString(
      AppCardContext.ImageAppCardContext.BUNDLE_KEY_IMAGE_SIZE_IMAGE_APP_CARD,
      TEST_IMAGE_SIZE_IMAGE_APP_CARD.toString()
    )
    imageAppCardContextBundle.putString(
      AppCardContext.ImageAppCardContext.BUNDLE_KEY_IMAGE_SIZE_BUTTON,
      TEST_IMAGE_SIZE_BUTTON.toString()
    )
    imageAppCardContextBundle.putString(
      AppCardContext.ImageAppCardContext.BUNDLE_KEY_IMAGE_SIZE_HEADER,
      TEST_IMAGE_SIZE_HEADER.toString()
    )
    imageAppCardContextBundle.putInt(
      AppCardContext.ImageAppCardContext.BUNDLE_KEY_MIN_BUTTONS_IN_IMAGE_APP_CARD,
      TEST_MIN_BUTTONS_IN_IMAGE_APP_CARD
    )

    appCardContentBundle.putBundle(
      AppCardContext.BUNDLE_KEY_IMAGE_CONTEXT,
      imageAppCardContextBundle
    )
  }

  @Test
  fun testGetApiLevel_isSupported() {
    val appCardContext = fromBundle(appCardContentBundle)

    assertThat(appCardContext?.apiLevel).isEqualTo(TEST_API_LEVEL)
  }

  @Test(expected = IllegalStateException::class)
  fun testGetApiLevel_isNotSupported_throwError() {
    appCardContentBundle.putInt(AppCardContext.BUNDLE_KEY_API_LEVEL, 2)

    fromBundle(appCardContentBundle)
  }

  @Test
  fun testGetRefreshPeriod_positive() {
    val appCardContext = fromBundle(appCardContentBundle)

    assertThat(appCardContext?.refreshPeriod).isEqualTo(TEST_REFRESH_PERIOD)
  }

  @Test(expected = IllegalStateException::class)
  fun testGetRefreshPeriod_negative_throwError() {
    appCardContentBundle.remove(AppCardContext.BUNDLE_KEY_REFRESH_PERIOD)

    fromBundle(appCardContentBundle)
  }

  @Test
  fun testGetFastRefreshPeriod_positive() {
    val appCardContext = fromBundle(appCardContentBundle)

    assertThat(appCardContext?.fastRefreshPeriod).isEqualTo(TEST_FAST_REFRESH_PERIOD)
  }

  @Test(expected = IllegalStateException::class)
  fun testGetFastRefreshPeriod_negative_throwError() {
    appCardContentBundle.remove(AppCardContext.BUNDLE_KEY_FAST_REFRESH_PERIOD)

    fromBundle(appCardContentBundle)
  }

  @Test
  fun testIsInteractable_isTrue() {
    val appCardContext = fromBundle(appCardContentBundle)

    assertThat(appCardContext?.isInteractable).isTrue()
  }

  @Test
  fun testGetImageAppCardContext_isTrue() {
    val appCardContext = fromBundle(appCardContentBundle)

    assertThat(appCardContext?.imageAppCardContext).isEqualTo(TEST_IMAGE_APP_CARD_CONTEXT)
  }

  @Test
  fun testToBundle() {
    val bundle = fromBundle(appCardContentBundle)!!.toBundle()

    assertThat(checkBundleKeyAndValueEquality(bundle, appCardContentBundle)).isTrue()
  }

  @Test
  fun testGetMinimumGuaranteedButtons() {
    val appCardContext = fromBundle(appCardContentBundle)

    val result = appCardContext?.imageAppCardContext?.minimumGuaranteedButtons

    assertThat(result).isEqualTo(TEST_MIN_BUTTONS_IN_IMAGE_APP_CARD)
  }

  @Test
  fun testGetMaxImageSize_button() {
    val appCardContext = fromBundle(appCardContentBundle)

    val maxSize = appCardContext?.imageAppCardContext?.getMaxImageSize(Button::class.java)

    assertThat(maxSize).isEqualTo(TEST_IMAGE_SIZE_BUTTON)
  }

  @Test
  fun testGetMaxImageSize_header() {
    val appCardContext = fromBundle(appCardContentBundle)

    val maxSize = appCardContext?.imageAppCardContext?.getMaxImageSize(Header::class.java)

    assertThat(maxSize).isEqualTo(TEST_IMAGE_SIZE_HEADER)
  }

  @Test
  fun testGetMaxImageSize_imageAppCard() {
    val appCardContext = fromBundle(appCardContentBundle)

    val maxSize = appCardContext?.imageAppCardContext?.getMaxImageSize(ImageAppCard::class.java)

    assertThat(maxSize).isEqualTo(TEST_IMAGE_SIZE_IMAGE_APP_CARD)
  }

  @Test(expected = IllegalStateException::class)
  fun testGetMaxImageSize_unrecognizedClass_throwError() {
    val appCardContext = fromBundle(appCardContentBundle)

    appCardContext?.imageAppCardContext?.getMaxImageSize(ProgressBar::class.java)
  }

  @Test
  fun testFromBundle_null() {
    assertThat(fromBundle(null)).isNull()
  }

  companion object {
    private const val TEST_API_LEVEL = 1
    private const val TEST_REFRESH_PERIOD = 1000
    private const val TEST_FAST_REFRESH_PERIOD = 500
    private const val TEST_INTERACTABLE = true
    private val TEST_IMAGE_SIZE_IMAGE_APP_CARD = Size.parseSize("2x2")
    private val TEST_IMAGE_SIZE_BUTTON = Size.parseSize("3x2")
    private val TEST_IMAGE_SIZE_HEADER = Size.parseSize("2x2")
    private const val TEST_MIN_BUTTONS_IN_IMAGE_APP_CARD = 3
    private val TEST_IMAGE_APP_CARD_CONTEXT = AppCardContext.ImageAppCardContext(
      TEST_IMAGE_SIZE_IMAGE_APP_CARD,
      TEST_IMAGE_SIZE_BUTTON,
      TEST_IMAGE_SIZE_HEADER,
      TEST_MIN_BUTTONS_IN_IMAGE_APP_CARD
    )

    private fun checkBundleKeyAndValueEquality(first: Bundle, second: Bundle?): Boolean {
      val firstApiLevel = first.getInt(AppCardContext.BUNDLE_KEY_API_LEVEL)
      val secondApiLevel = second?.getInt(AppCardContext.BUNDLE_KEY_API_LEVEL)
      var result = firstApiLevel == secondApiLevel
      if (!result) {
        return false
      }

      val firstRefreshPeriod = first.getInt(AppCardContext.BUNDLE_KEY_REFRESH_PERIOD)
      val secondRefreshPeriod = second?.getInt(AppCardContext.BUNDLE_KEY_REFRESH_PERIOD)
      result = firstRefreshPeriod == secondRefreshPeriod
      if (!result) {
        return false
      }

      val firstFastRefreshPeriod = first.getInt(AppCardContext.BUNDLE_KEY_FAST_REFRESH_PERIOD)
      val secondFastRefreshPeriod = second?.getInt(AppCardContext.BUNDLE_KEY_FAST_REFRESH_PERIOD)
      result = firstFastRefreshPeriod == secondFastRefreshPeriod
      if (!result) {
        return false
      }

      val firstInteractable = first.getBoolean(AppCardContext.BUNDLE_KEY_INTERACTABLE)
      val secondInteractable = second?.getBoolean(AppCardContext.BUNDLE_KEY_INTERACTABLE)
      result = firstInteractable == secondInteractable
      if (!result) {
        return false
      }

      val firstImageContext = first.getBundle(AppCardContext.BUNDLE_KEY_IMAGE_CONTEXT)
      val secondImageContext = second?.getBundle(AppCardContext.BUNDLE_KEY_IMAGE_CONTEXT)

      val firstImageAppCardMaxSize = firstImageContext?.getString(
        AppCardContext.ImageAppCardContext.BUNDLE_KEY_IMAGE_SIZE_IMAGE_APP_CARD
      )
      val secondImageAppCardMaxSize = secondImageContext?.getString(
        AppCardContext.ImageAppCardContext.BUNDLE_KEY_IMAGE_SIZE_IMAGE_APP_CARD
      )
      result = firstImageAppCardMaxSize == secondImageAppCardMaxSize
      if (!result) {
        return false
      }

      val firstButtonMaxSize = firstImageContext?.getString(
        AppCardContext.ImageAppCardContext.BUNDLE_KEY_IMAGE_SIZE_BUTTON
      )
      val secondButtonMaxSize = secondImageContext?.getString(
        AppCardContext.ImageAppCardContext.BUNDLE_KEY_IMAGE_SIZE_BUTTON
      )
      result = firstButtonMaxSize == secondButtonMaxSize
      if (!result) {
        return false
      }

      val firstHeaderMaxSize = firstImageContext?.getString(
        AppCardContext.ImageAppCardContext.BUNDLE_KEY_IMAGE_SIZE_HEADER
      )
      val secondHeaderMaxSize = secondImageContext?.getString(
        AppCardContext.ImageAppCardContext.BUNDLE_KEY_IMAGE_SIZE_HEADER
      )
      result = firstHeaderMaxSize == secondHeaderMaxSize
      if (!result) {
        return false
      }

      val firstMinButtons = firstImageContext?.getInt(
        AppCardContext.ImageAppCardContext.BUNDLE_KEY_MIN_BUTTONS_IN_IMAGE_APP_CARD
      )
      val secondMinButtons = secondImageContext?.getInt(
        AppCardContext.ImageAppCardContext.BUNDLE_KEY_MIN_BUTTONS_IN_IMAGE_APP_CARD
      )
      result = firstMinButtons == secondMinButtons
      return result
    }
  }
}
