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

import android.content.ContentResolver
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Size
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.appcard.AppCardContext.Companion.fromBundle
import com.android.car.appcard.AppCardMessageConstants.InteractionMessageConstants.MSG_INTERACTION_ON_CLICK
import com.android.car.appcard.AppCardMessageConstants.MSG_APP_CARD_ADDED
import com.android.car.appcard.AppCardMessageConstants.MSG_APP_CARD_COMPONENT_UPDATE
import com.android.car.appcard.AppCardMessageConstants.MSG_APP_CARD_CONTEXT_UPDATE
import com.android.car.appcard.AppCardMessageConstants.MSG_APP_CARD_INTERACTION
import com.android.car.appcard.AppCardMessageConstants.MSG_APP_CARD_REMOVED
import com.android.car.appcard.AppCardMessageConstants.MSG_APP_CARD_UPDATE
import com.android.car.appcard.AppCardMessageConstants.MSG_SEND_ALL_APP_CARDS
import com.android.car.appcard.BitmapUtility.getSampleBitmap
import com.android.car.appcard.ImageAppCard.Companion.newBuilder
import com.android.car.appcard.component.Button
import com.android.car.appcard.component.Button.Companion.fromMessage
import com.android.car.appcard.component.Button.Companion.newBuilder
import com.android.car.appcard.component.Header
import com.android.car.appcard.component.Header.Companion.fromMessage
import com.android.car.appcard.component.Image
import com.android.car.appcard.component.Image.Companion.fromMessage
import com.android.car.appcard.component.ProgressBar
import com.android.car.appcard.component.ProgressBar.Companion.fromMessage
import com.android.car.appcard.component.ProgressBar.Companion.newBuilder
import com.android.car.appcard.component.interaction.OnClickListener
import com.android.car.appcard.internal.AppCardTransport
import com.android.car.appcard.internal.proto.Header.HeaderMessage
import com.android.car.appcard.internal.proto.Image.ContentScale
import com.android.car.appcard.internal.proto.Image.ImageMessage
import com.android.car.appcard.internal.proto.ProgressBar.ProgressBarMessage
import com.android.car.appcard.util.ParcelableUtils.bytesToParcelable
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.InvalidProtocolBufferException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class AppCardContentProviderTest {
  private val contentResolver = mock<ContentResolver>()
  private val context = mock<Context> {
    on { contentResolver } doReturn contentResolver
  }

  private lateinit var image: Image
  private lateinit var header: Header
  private lateinit var progressBar: ProgressBar
  private lateinit var button: Button
  private lateinit var appCardContextBundle: Bundle
  private lateinit var imageAppCardContextBundle: Bundle
  private lateinit var testAppCardContentProvider: TestAppCardContentProvider
  private lateinit var imageAppCardWithImageAndText: ImageAppCard
  private lateinit var imageAppCardWithProgressBarButtons: ImageAppCard
  private var buttonClicked = false

  @Before
  fun setup() {
    buttonClicked = false

    image = Image.newBuilder(TEST_IMAGE_COMPONENT_ID)
      .setImageData(getSampleBitmap(strokeWidth = 3))
      .build()

    header = Header.newBuilder(TEST_HEADER_COMPONENT_ID)
      .setTitle(TEST_TITLE)
      .build()

    progressBar = newBuilder(TEST_PROGRESS_BAR_COMPONENT_ID, min = 0, max = 2)
      .build()

    val onClickListener = object : OnClickListener {
      override fun onClick() {
        buttonClicked = true
      }
    }

    button = newBuilder(TEST_BUTTON_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setText(TEST_TITLE)
      .build()

    imageAppCardWithImageAndText = newBuilder(TEST_ID_WITH_IMAGE)
      .setImage(image)
      .setHeader(header)
      .setPrimaryText(TEST_PRIMARY_TEXT)
      .setSecondaryText(TEST_SECONDARY_TEXT)
      .build()

    imageAppCardWithProgressBarButtons = newBuilder(TEST_ID_WITH_PROGRESS_BUTTONS)
      .setHeader(header)
      .setPrimaryText(TEST_PRIMARY_TEXT)
      .setSecondaryText(TEST_SECONDARY_TEXT)
      .setProgressBar(progressBar)
      .addButton(button)
      .build()

    appCardContextBundle = Bundle()
    appCardContextBundle.putInt(AppCardContext.BUNDLE_KEY_API_LEVEL, TEST_API_LEVEL)
    appCardContextBundle.putInt(AppCardContext.BUNDLE_KEY_REFRESH_PERIOD, TEST_REFRESH_PERIOD)
    appCardContextBundle.putInt(
      AppCardContext.BUNDLE_KEY_FAST_REFRESH_PERIOD,
      TEST_FAST_REFRESH_PERIOD
    )
    appCardContextBundle.putBoolean(AppCardContext.BUNDLE_KEY_INTERACTABLE, TEST_INTERACTABLE)

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
    appCardContextBundle.putBundle(
      AppCardContext.BUNDLE_KEY_IMAGE_CONTEXT,
      imageAppCardContextBundle
    )

    testAppCardContentProvider = TestAppCardContentProvider(
      TEST_AUTHORITY,
      listOf(TEST_ID_WITH_IMAGE, TEST_ID_WITH_PROGRESS_BUTTONS),
      listOf(imageAppCardWithImageAndText, imageAppCardWithProgressBarButtons)
    )
    testAppCardContentProvider.attachInfo(context, ProviderInfo())
  }

  @Test
  fun testOnCreate_returnTrue() {
    assertThat(testAppCardContentProvider.onCreate()).isTrue()
  }

  @Test
  fun testGetType() {
    val result = testAppCardContentProvider.getType(Uri.Builder().build())

    assertThat(result).isEqualTo(EXPECTED_TYPE)
  }

  @Test
  fun testInsert_returnNull() {
    val contentValues = null

    val result = testAppCardContentProvider.insert(Uri.Builder().build(), contentValues)

    assertThat(result).isNull()
  }

  @Test
  fun testDelete_returnZero() {
    val s = null
    val strings = null
    val result = testAppCardContentProvider.delete(Uri.Builder().build(), s, strings)

    assertThat(result).isEqualTo(0)
  }

  @Test
  fun testUpdate_returnZero() {
    val contentValues = null
    val s = null
    val strings = null

    val result = testAppCardContentProvider.update(
      Uri.Builder().build(),
      contentValues,
      s,
      strings
    )

    assertThat(result).isEqualTo(0)
  }

  @Test
  fun testCall_unrecognizedMethod_returnNull() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)

    val result = testAppCardContentProvider.call(MSG_SEND_ALL_APP_CARDS, NO_ARGS, bundle)

    assertThat(result).isNull()
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testQuery_sendAllAppCards_noError_correctAppCards() {
    val b = Bundle()
    b.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    b.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_SEND_ALL_APP_CARDS).build()

    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)

    val appCardTransport = getAppTransports(cursor)
    assertThat(appCardTransport.size).isEqualTo(2)
    val firstAppCard = appCardTransport[0].appCard as ImageAppCard?
    val secondAppCard = appCardTransport[1].appCard as ImageAppCard?
    assertThat(firstAppCard?.toMessage()).isEqualTo(imageAppCardWithImageAndText.toMessage())
    assertThat(secondAppCard?.toMessage()).isEqualTo(imageAppCardWithProgressBarButtons.toMessage())
  }

  @Test
  fun testCall_sendAllAppCards_missingAppCardContext_returnNull() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)

    val result = testAppCardContentProvider.call(
      MSG_SEND_ALL_APP_CARDS,
      NO_ARGS,
      bundle
    )

    assertThat(result).isNull()
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testCall_appCardAdded_noError_correctAppCard() {
    val b = Bundle()
    b.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    b.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()

    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)

    val appCardTransport = getAppTransports(cursor)
    assertThat(appCardTransport.size).isEqualTo(1)
    val result = appCardTransport[0].appCard as ImageAppCard?
    assertThat(result?.toMessage()).isEqualTo(imageAppCardWithImageAndText.toMessage())
  }

  @Test
  fun testCall_appCardAdded_missingId_returnNull() {
    val bundle = Bundle()
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()

    val result = testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)

    assertThat(result).isNull()
  }

  @Test
  fun testCall_appCardAdded_missingAppCardContext_returnNull() {
    val bundle = Bundle()
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()

    val result = testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)

    assertThat(result).isNull()
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testCall_appCardUpdate_noError_correctAppCard() {
    val b = Bundle()
    b.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    b.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    var uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)
    imageAppCardWithImageAndText = newBuilder(imageAppCardWithImageAndText)
      .setPrimaryText(TEST_SECONDARY_TEXT)
      .setSecondaryText(TEST_PRIMARY_TEXT)
      .build()
    testAppCardContentProvider.sendAppCardUpdate(imageAppCardWithImageAndText)
    uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_UPDATE).build()

    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)

    val appCardTransport = getAppTransports(cursor)
    val result = appCardTransport[0].appCard as ImageAppCard?
    assertThat(result?.primaryText).isEqualTo(TEST_SECONDARY_TEXT)
    assertThat(result?.secondaryText).isEqualTo(TEST_PRIMARY_TEXT)
  }

  @Test
  fun testCall_appCardUpdate_missingId_returnNull() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    var uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    imageAppCardWithImageAndText = newBuilder(imageAppCardWithImageAndText)
      .setPrimaryText(TEST_SECONDARY_TEXT)
      .setSecondaryText(TEST_PRIMARY_TEXT)
      .build()
    testAppCardContentProvider.sendAppCardUpdate(imageAppCardWithImageAndText)
    bundle.remove(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID)
    uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_UPDATE).build()

    val result = testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)

    assertThat(result).isNull()
  }

  @Test
  fun testCall_appCardUpdate_missingActiveAppCard_returnNull() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_UPDATE).build()

    val result = testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)

    assertThat(result).isNull()
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testCall_appCardComponentUpdate_noError_correctAppCard() {
    val b = Bundle()
    b.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    b.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)
    val appCardTransports = getAppTransports(cursor)
    val initial = appCardTransports[0].appCard as ImageAppCard?
    assertThat(initial?.progressBar?.progress).isEqualTo(0)
    val progressBarMessage = ProgressBarMessage
      .newBuilder(progressBar.toMessage())
      .setProgress(1)
      .build()
    progressBar = fromMessage(progressBarMessage)
    testAppCardContentProvider.sendAppCardComponentUpdate(
      TEST_ID_WITH_PROGRESS_BUTTONS,
      progressBar
    )
    verify(contentResolver).notifyChange(any(), isNull())
    b.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID,
      TEST_PROGRESS_BAR_COMPONENT_ID
    )

    val bundle = testAppCardContentProvider.call(
      MSG_APP_CARD_COMPONENT_UPDATE,
      NO_ARGS,
      b
    )

    val appCardTransport = bundle?.getParcelable(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT,
      AppCardTransport::class.java
    )
    val result = appCardTransport?.component as ProgressBar?
    assertThat(result?.progress).isEqualTo(1)
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testCall_appCardComponentUpdate_missingId_returnNull() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    val appCardTransport = getAppTransports(cursor)
    val initial = appCardTransport[0].appCard as ImageAppCard?
    assertThat(initial?.progressBar?.progress).isEqualTo(0)
    val progressBarMessage = ProgressBarMessage
      .newBuilder(progressBar.toMessage())
      .setProgress(1)
      .build()
    progressBar = fromMessage(progressBarMessage)
    testAppCardContentProvider.sendAppCardComponentUpdate(
      TEST_ID_WITH_PROGRESS_BUTTONS,
      progressBar
    )
    verify(contentResolver).notifyChange(any(), isNull())
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID,
      TEST_PROGRESS_BAR_COMPONENT_ID
    )
    bundle.remove(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID)

    val result = testAppCardContentProvider.call(
      MSG_APP_CARD_COMPONENT_UPDATE,
      NO_ARGS,
      bundle
    )

    assertThat(result).isNull()
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testCall_appCardComponentUpdate_missingComponentId_returnNull() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    val appCardTransport = getAppTransports(cursor)
    val initial = appCardTransport[0].appCard as ImageAppCard?
    assertThat(initial?.progressBar?.progress).isEqualTo(0)
    val progressBarMessage = ProgressBarMessage
      .newBuilder(progressBar.toMessage())
      .setProgress(1)
      .build()
    progressBar = fromMessage(progressBarMessage)
    testAppCardContentProvider.sendAppCardComponentUpdate(
      TEST_ID_WITH_PROGRESS_BUTTONS,
      progressBar
    )
    verify(contentResolver).notifyChange(any(), isNull())

    val result = testAppCardContentProvider.call(
      MSG_APP_CARD_COMPONENT_UPDATE,
      NO_ARGS,
      bundle
    )

    assertThat(result).isNull()
  }

  @Test
  fun testCall_appCardComponentUpdate_missingActiveAppCard_returnNull() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val progressBarMessage = ProgressBarMessage
      .newBuilder(progressBar.toMessage())
      .setProgress(1)
      .build()
    progressBar = fromMessage(progressBarMessage)
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID,
      TEST_PROGRESS_BAR_COMPONENT_ID
    )
    bundle.remove(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT)

    val result = testAppCardContentProvider.call(
      MSG_APP_CARD_COMPONENT_UPDATE,
      NO_ARGS,
      bundle
    )

    assertThat(result).isNull()
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testCall_appCardComponentUpdate_incorrectComponentId_returnNull() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    val appCardTransport = getAppTransports(cursor)
    val initial = appCardTransport[0].appCard as ImageAppCard?
    assertThat(initial?.progressBar?.progress).isEqualTo(0)
    val progressBarMessage = ProgressBarMessage
      .newBuilder(progressBar.toMessage())
      .setProgress(1)
      .build()
    progressBar = fromMessage(progressBarMessage)
    testAppCardContentProvider.sendAppCardComponentUpdate(
      TEST_ID_WITH_PROGRESS_BUTTONS,
      progressBar
    )
    verify(contentResolver).notifyChange(any(), isNull())
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID, TEST_ID_WITH_IMAGE)

    val result = testAppCardContentProvider.call(
      MSG_APP_CARD_COMPONENT_UPDATE,
      NO_ARGS,
      bundle
    )

    assertThat(result).isNull()
  }

  @Test
  fun testCall_appCardInteraction_noError_buttonClickedTrue() {
    var bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    bundle.putBundle(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT,
      appCardContextBundle
    )
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID,
      TEST_BUTTON_COMPONENT_ID
    )
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_INTERACTION_ID,
      MSG_INTERACTION_ON_CLICK
    )

    testAppCardContentProvider.call(MSG_APP_CARD_INTERACTION, NO_ARGS, bundle)

    assertThat(buttonClicked).isTrue()
  }

  @Test
  fun testCall_appCardInteraction_missingId_buttonClickedFalse() {
    var bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    bundle = Bundle()
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID,
      TEST_BUTTON_COMPONENT_ID
    )
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_INTERACTION_ID,
      MSG_INTERACTION_ON_CLICK
    )

    testAppCardContentProvider.call(MSG_APP_CARD_INTERACTION, NO_ARGS, bundle)

    assertThat(buttonClicked).isFalse()
  }

  @Test
  fun testCall_appCardInteraction_missingComponentId_buttonClickedFalse() {
    var bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_INTERACTION_ID,
      MSG_INTERACTION_ON_CLICK
    )

    testAppCardContentProvider.call(MSG_APP_CARD_INTERACTION, NO_ARGS, bundle)

    assertThat(buttonClicked).isFalse()
  }

  @Test
  fun testCall_appCardInteraction_missingInteractionId_buttonClickedFalse() {
    var bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID,
      TEST_BUTTON_COMPONENT_ID
    )

    testAppCardContentProvider.call(MSG_APP_CARD_INTERACTION, NO_ARGS, bundle)

    assertThat(buttonClicked).isFalse()
  }

  @Test
  fun testCall_appCardInteraction_missingActiveAppCard_buttonClickedFalse() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID,
      TEST_BUTTON_COMPONENT_ID
    )
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_INTERACTION_ID,
      MSG_INTERACTION_ON_CLICK
    )

    testAppCardContentProvider.call(MSG_APP_CARD_INTERACTION, NO_ARGS, bundle)

    assertThat(buttonClicked).isFalse()
  }

  @Test
  fun testCall_appCardInteraction_incorrectComponentId_buttonClickedFalse() {
    var bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID, TEST_ID_WITH_IMAGE)
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_INTERACTION_ID,
      MSG_INTERACTION_ON_CLICK
    )

    testAppCardContentProvider.call(MSG_APP_CARD_INTERACTION, NO_ARGS, bundle)

    assertThat(buttonClicked).isFalse()
  }

  @Test
  fun testCall_appCardInteraction_componentNotClickable_buttonClickedFalse() {
    var bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID,
      TEST_HEADER_COMPONENT_ID
    )
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_INTERACTION_ID,
      MSG_INTERACTION_ON_CLICK
    )

    testAppCardContentProvider.call(MSG_APP_CARD_INTERACTION, NO_ARGS, bundle)

    assertThat(buttonClicked).isFalse()
  }

  @Test
  fun testCall_appCardInteraction_incorrectInteractionId_buttonClickedFalse() {
    var bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putString(
      AppCardContentProvider.BUNDLE_KEY_APP_CARD_COMPONENT_ID,
      TEST_BUTTON_COMPONENT_ID
    )
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_INTERACTION_ID, TEST_ID_WITH_IMAGE)

    testAppCardContentProvider.call(MSG_APP_CARD_INTERACTION, NO_ARGS, bundle)

    assertThat(buttonClicked).isFalse()
  }

  @Test
  fun testCall_appCardContextUpdate_noError_correctId() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)

    testAppCardContentProvider.call(MSG_APP_CARD_CONTEXT_UPDATE, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.id).isEqualTo(TEST_ID_WITH_IMAGE)
  }

  @Test
  fun testCall_appCardContextUpdate_noError_correctAppCardContext() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)

    testAppCardContentProvider.call(MSG_APP_CARD_CONTEXT_UPDATE, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.appCardContext)
      .isEqualTo(fromBundle(appCardContextBundle))
  }

  @Test
  fun testCall_appCardContextUpdate_missingId_nullAppCardContext() {
    val bundle = Bundle()
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)

    testAppCardContentProvider.call(MSG_APP_CARD_CONTEXT_UPDATE, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.appCardContext).isNull()
  }

  @Test
  fun testCall_appCardContextUpdate_missingId_idEmpty() {
    val bundle = Bundle()
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)

    testAppCardContentProvider.call(MSG_APP_CARD_CONTEXT_UPDATE, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.id).isEqualTo("")
  }

  @Test
  fun testCall_appCardContextUpdate_missingAppCardContext_idEmpty() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)

    testAppCardContentProvider.call(MSG_APP_CARD_CONTEXT_UPDATE, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.id).isEqualTo("")
  }

  @Test
  fun testCall_appCardContextUpdate_missingAppCardContext_nullAppCardContext() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)

    testAppCardContentProvider.call(MSG_APP_CARD_CONTEXT_UPDATE, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.appCardContext).isNull()
  }

  @Test
  fun testCall_appCardRemoved_noActiveCard_idEmpty() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)

    testAppCardContentProvider.call(MSG_APP_CARD_REMOVED, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.id).isEqualTo("")
  }

  @Test
  fun testCall_appCardRemoved_activeCard_correctId() {
    var bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    testAppCardContentProvider.id = ""
    bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)

    testAppCardContentProvider.call(MSG_APP_CARD_REMOVED, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.id).isEqualTo(TEST_ID_WITH_IMAGE)
  }

  @Test
  fun testCall_appCardRemoved_missingId_idEmpty() {
    val bundle = Bundle()

    testAppCardContentProvider.call(MSG_APP_CARD_REMOVED, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.id).isEqualTo("")
  }

  @Test
  fun testCall_appCardRemoved_oneAppCard_correctId() {
    var bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)

    testAppCardContentProvider.call(MSG_APP_CARD_REMOVED, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.id).isEqualTo(TEST_ID_WITH_IMAGE)
  }

  @Test
  fun testCall_appCardRemoved_moreThanOneAppCard_idEmpty() {
    var bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    testAppCardContentProvider.id = ""

    testAppCardContentProvider.call(MSG_APP_CARD_REMOVED, NO_ARGS, bundle)

    assertThat(testAppCardContentProvider.id).isEqualTo("")
  }

  @Test
  fun testSendAppCardComponentUpdate_noActiveAppCard_notifyChangeNotCalled() {
    testAppCardContentProvider.sendAppCardComponentUpdate(TEST_ID_WITH_IMAGE, progressBar)

    verify(contentResolver, never()).notifyChange(any(), isNull())
  }

  @Test
  fun testSendAppCardComponentUpdate_noExistingComponentId_notifyChangeNotCalled() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)
    val progressBarMessage = ProgressBarMessage
      .newBuilder(progressBar.toMessage())
      .setComponentId(TEST_ID_WITH_IMAGE)
      .setProgress(1)
      .build()
    progressBar = fromMessage(progressBarMessage)

    testAppCardContentProvider.sendAppCardComponentUpdate(TEST_ID_WITH_IMAGE, progressBar)

    verify(contentResolver, never()).notifyChange(any(), isNull())
  }

  @Test
  fun testSendAppCardComponentUpdate_notEnforceFastUpdateRate_notifyChangeNotCalled() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)

    testAppCardContentProvider.sendAppCardComponentUpdate(TEST_ID_WITH_IMAGE, button)

    verify(contentResolver, never()).notifyChange(any(), isNull())
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testSendAppCardComponentUpdate_notEnforceFastUpdateRate_buttonUpdated() {
    val b = Bundle()
    b.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    b.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    var uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)
    val title = imageAppCardWithProgressBarButtons.buttons[0].text
    assertThat(title).isEqualTo(TEST_TITLE)
    assertThat(imageAppCardWithProgressBarButtons.buttons.size).isEqualTo(1)
    val buttonMessage = com.android.car.appcard.internal.proto.Button.ButtonMessage
      .newBuilder(button.toMessage())
      .setText(TEST_SECONDARY_TEXT)
      .build()
    button = fromMessage(buttonMessage)
    testAppCardContentProvider.sendAppCardComponentUpdate(TEST_ID_WITH_PROGRESS_BUTTONS, button)
    uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_UPDATE).build()

    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)

    val appCardTransport = getAppTransports(cursor)
    val result = appCardTransport[0].appCard as ImageAppCard?
    assertThat(result?.buttons?.get(0)?.text).isEqualTo(TEST_SECONDARY_TEXT)
    assertThat(result?.buttons?.size).isEqualTo(1)
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testSendAppCardComponentUpdate_notEnforceFastUpdateRate_headerUpdated() {
    val b = Bundle()
    b.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    b.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    var uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)
    assertThat(imageAppCardWithImageAndText.header?.title).isEqualTo(TEST_TITLE)
    val message = HeaderMessage
      .newBuilder(header.toMessage())
      .setTitle(TEST_SECONDARY_TEXT)
      .build()
    header = fromMessage(message)
    testAppCardContentProvider.sendAppCardComponentUpdate(TEST_ID_WITH_IMAGE, header)
    uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_UPDATE).build()

    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)

    val appCardTransport = getAppTransports(cursor)
    val result = appCardTransport[0].appCard as ImageAppCard?
    assertThat(result?.header?.title).isEqualTo(TEST_SECONDARY_TEXT)
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testSendAppCardComponentUpdate_notEnforceFastUpdateRate_imageUpdated() {
    val b = Bundle()
    b.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_IMAGE)
    b.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    var uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)
    assertThat(imageAppCardWithImageAndText.image?.contentScale).isEqualTo(Image.ContentScale.FIT)
    val message = ImageMessage
      .newBuilder(image.toMessage())
      .setContentScale(ContentScale.FILL_BOUNDS)
      .build()
    image = fromMessage(message)
    testAppCardContentProvider.sendAppCardComponentUpdate(TEST_ID_WITH_IMAGE, image)
    uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_UPDATE).build()

    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)

    val appCardTransport = getAppTransports(cursor)
    val result = appCardTransport[0].appCard as ImageAppCard?
    assertThat(result?.image?.contentScale).isEqualTo(Image.ContentScale.FILL_BOUNDS)
  }

  @Test
  fun testSendAppCardComponentUpdate_enforceFastUpdateRate_notifyChangeCalled() {
    val bundle = Bundle()
    bundle.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    bundle.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    val uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, bundle, NO_ARGS)

    testAppCardContentProvider.sendAppCardComponentUpdate(
      TEST_ID_WITH_PROGRESS_BUTTONS,
      progressBar
    )

    verify(contentResolver).notifyChange(any(), isNull())
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testSendAppCardComponentUpdate_enforceFastUpdateRate_progressBarUpdated() {
    val b = Bundle()
    b.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    b.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    var uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)
    assertThat(imageAppCardWithProgressBarButtons.progressBar?.progress).isEqualTo(0)
    val progressBarMessage = ProgressBarMessage
      .newBuilder(progressBar.toMessage())
      .setProgress(1)
      .build()
    progressBar = fromMessage(progressBarMessage)
    testAppCardContentProvider.sendAppCardComponentUpdate(
      TEST_ID_WITH_PROGRESS_BUTTONS,
      progressBar
    )
    uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_UPDATE).build()

    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)

    val appCardTransport = getAppTransports(cursor)
    val result = appCardTransport[0].appCard as ImageAppCard?
    assertThat(result?.progressBar?.progress).isEqualTo(1)
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testSendAppCardUpdate_noActiveAppCard_notUpdated() {
    val b = Bundle()
    b.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    b.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    var uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)
    assertThat(imageAppCardWithProgressBarButtons.progressBar?.progress).isEqualTo(0)
    val progressBarMessage = ProgressBarMessage
      .newBuilder(progressBar.toMessage())
      .setProgress(1)
      .build()
    progressBar = fromMessage(progressBarMessage)
    val update = newBuilder(TEST_TITLE)
      .setHeader(header)
      .setPrimaryText(TEST_PRIMARY_TEXT)
      .setSecondaryText(TEST_SECONDARY_TEXT)
      .setProgressBar(progressBar)
      .setButtons(listOf(button))
      .build()
    testAppCardContentProvider.sendAppCardUpdate(update)
    uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_UPDATE).build()

    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)

    val appCardTransport = getAppTransports(cursor)
    val result = appCardTransport[0].appCard as ImageAppCard?
    assertThat(result?.toMessage()).isEqualTo(imageAppCardWithProgressBarButtons.toMessage())
  }

  @Test
  @Throws(InvalidProtocolBufferException::class)
  fun testSendAppCardUpdate_activeAppCard_updated() {
    val b = Bundle()
    b.putString(AppCardContentProvider.BUNDLE_KEY_APP_CARD_ID, TEST_ID_WITH_PROGRESS_BUTTONS)
    b.putBundle(AppCardContentProvider.BUNDLE_KEY_APP_CARD_CONTEXT, appCardContextBundle)
    var uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_ADDED).build()
    testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)
    assertThat(imageAppCardWithProgressBarButtons.progressBar?.progress).isEqualTo(0)
    val progressBarMessage = ProgressBarMessage
      .newBuilder(progressBar.toMessage())
      .setProgress(1)
      .build()
    progressBar = fromMessage(progressBarMessage)
    val update = newBuilder(TEST_ID_WITH_PROGRESS_BUTTONS)
      .setHeader(header)
      .setPrimaryText(TEST_PRIMARY_TEXT)
      .setSecondaryText(TEST_SECONDARY_TEXT)
      .setProgressBar(progressBar)
      .setButtons(listOf(button))
      .build()
    testAppCardContentProvider.sendAppCardUpdate(update)
    uri = Uri.Builder().authority(TEST_AUTHORITY).appendPath(MSG_APP_CARD_UPDATE).build()

    val cursor = testAppCardContentProvider.query(uri, NO_ARGS, b, NO_ARGS)

    val appCardTransport = getAppTransports(cursor)
    val result = appCardTransport[0].appCard as ImageAppCard?
    assertThat(result?.toMessage()).isNotEqualTo(imageAppCardWithProgressBarButtons.toMessage())
    assertThat(result?.progressBar?.progress).isEqualTo(1)
  }

  private fun getAppTransports(cursor: Cursor?): MutableList<AppCardTransport> {
    val appCardTransports = mutableListOf<AppCardTransport>()
    cursor?.let {
      if (it.moveToFirst()) {
        do {
          val blob = it.getBlob(
            it.getColumnIndexOrThrow(AppCardContentProvider.CURSOR_COLUMN_APP_CARD_TRANSPORT)
          )
          val appCardTransport = bytesToParcelable(blob, AppCardTransport.CREATOR)
          appCardTransports.add(appCardTransport)
        } while (it.moveToNext())
      }
    }

    return appCardTransports
  }

  private class TestAppCardContentProvider(
    override val authority: String,
    override val appCardIds: List<String>,
    private val appCards: List<AppCard>
  ) : AppCardContentProvider() {

    var id = ""
    var appCardContext: AppCardContext? = null

    override fun onAppCardAdded(id: String, ctx: AppCardContext): AppCard {
      this.id = id
      appCardContext = ctx
      var appCard: AppCard? = null
      appCards.forEach {
        if (it.id == this.id) appCard = it
      }
      return appCard!!
    }

    override fun onAppCardRemoved(id: String) {
      this.id = id
    }

    override fun onAppCardContextChanged(id: String, appCardContext: AppCardContext) {
      this.id = id
      this.appCardContext = appCardContext
    }
  }

  companion object {
    private val NO_ARGS = null
    private const val EXPECTED_TYPE = "android.car.appcard"
    private const val TEST_AUTHORITY = "AUTHORITY"
    private const val TEST_ID_WITH_IMAGE = "ID_WITH_IMAGE"
    private const val TEST_ID_WITH_PROGRESS_BUTTONS = "ID_WITH_PROGRESS_BUTTONS"
    private const val TEST_PRIMARY_TEXT = "PRIMARY_TEXT"
    private const val TEST_SECONDARY_TEXT = "SECONDARY_TEXT"
    private const val TEST_IMAGE_COMPONENT_ID = "TEST_IMAGE_COMPONENT_ID"
    private const val TEST_HEADER_COMPONENT_ID = "TEST_HEADER_COMPONENT_ID"
    private const val TEST_PROGRESS_BAR_COMPONENT_ID = "TEST_PROGRESS_BAR_COMPONENT_ID"
    private const val TEST_BUTTON_COMPONENT_ID = "TEST_BUTTON_COMPONENT_ID"
    private const val TEST_TITLE = "TITLE"
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
  }
}
