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
package com.android.car.appcard.internal

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.appcard.BitmapUtility
import com.android.car.appcard.ImageAppCard
import com.android.car.appcard.ImageAppCard.Companion.fromMessage
import com.android.car.appcard.component.Button
import com.android.car.appcard.component.Button.Companion.fromMessage
import com.android.car.appcard.component.Header
import com.android.car.appcard.component.Header.Companion.fromMessage
import com.android.car.appcard.component.Image
import com.android.car.appcard.component.Image.Companion.fromMessage
import com.android.car.appcard.component.ProgressBar
import com.android.car.appcard.component.ProgressBar.Companion.fromMessage
import com.android.car.appcard.internal.proto.AppCard
import com.android.car.appcard.internal.proto.Button.ButtonMessage
import com.android.car.appcard.internal.proto.Button.ButtonType
import com.android.car.appcard.internal.proto.Header.HeaderMessage
import com.android.car.appcard.internal.proto.Image.ColorFilter
import com.android.car.appcard.internal.proto.Image.ContentScale
import com.android.car.appcard.internal.proto.Image.ImageMessage
import com.android.car.appcard.internal.proto.ImageAppCard.ButtonList
import com.android.car.appcard.internal.proto.ImageAppCard.ImageAppCardMessage
import com.android.car.appcard.internal.proto.ProgressBar.ProgressBarMessage
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppCardTransportTest {
    private lateinit var buttonMessage: ButtonMessage
    private lateinit var headerMessage: HeaderMessage
    private lateinit var imageMessage: ImageMessage
    private lateinit var progressBarMessage: ProgressBarMessage
    private lateinit var imageAppCardMessage: ImageAppCardMessage

    @Before
    fun setup() {
        imageMessage =
            ImageMessage.newBuilder()
                .setComponentId(TEST_COMPONENT_ID)
                .setImage(
                    ByteString.copyFrom(
                        BitmapUtility.convertToBytes(BitmapUtility.getSampleBitmap(strokeWidth = 3))
                    )
                )
                .setColorFilter(ColorFilter.NO_TINT)
                .setContentScale(ContentScale.FILL_BOUNDS)
                .build()
        buttonMessage =
            ButtonMessage.newBuilder()
                .setComponentId(TEST_COMPONENT_ID)
                .setImage(imageMessage)
                .setText(TEST_TITLE)
                .setType(ButtonType.PRIMARY)
                .build()
        headerMessage =
            HeaderMessage.newBuilder()
                .setComponentId(TEST_COMPONENT_ID)
                .setTitle(TEST_TITLE)
                .build()
        progressBarMessage =
            ProgressBarMessage.newBuilder()
                .setComponentId(TEST_COMPONENT_ID)
                .setMax(2)
                .setMin(0)
                .setProgress(1)
                .build()
        imageAppCardMessage =
            ImageAppCardMessage.newBuilder()
                .setAppCard(AppCard.AppCardMessage.newBuilder().setId(TEST_ID).build())
                .setHeader(headerMessage)
                .setPrimaryText(TEST_PRIMARY_TEXT)
                .setSecondaryText(TEST_SECONDARY_TEXT)
                .setProgressBar(progressBarMessage)
                .setButtonList(ButtonList.newBuilder().addAllButtons(listOf(buttonMessage)).build())
                .build()
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetAppCard_parcelConstructor() {
        val bytes = imageAppCardMessage.toByteString()
        val parcel = Parcel.obtain()
        parcel.writeInt(AppCardTransport.SUPPORTED_SERIALIZATION_VERSION)
        parcel.writeString(ImageAppCard::class.java.getSimpleName())
        parcel.writeInt(bytes.size())
        parcel.writeByteArray(bytes.toByteArray())
        parcel.setDataPosition(0)

        val appCardTransport = AppCardTransport(parcel)

        val result = appCardTransport.appCard as ImageAppCard?
        assertThat(result?.toByteArray()).isEqualTo(bytes.toByteArray())
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetAppCard_appCardConstructor() {
        val expected = fromMessage(imageAppCardMessage)

        val appCardTransport = AppCardTransport(expected)

        val result = appCardTransport.appCard as ImageAppCard?
        assertThat(result?.toByteArray()).isEqualTo(expected.toByteArray())
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetComponent_imageAppCard_isNull() {
        val expected = fromMessage(imageAppCardMessage)

        val appCardTransport = AppCardTransport(expected)

        assertThat(appCardTransport.component).isNull()
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetComponent_button_parcelConstructor() {
        val bytes = buttonMessage.toByteString()
        val parcel = Parcel.obtain()
        parcel.writeInt(AppCardTransport.SUPPORTED_SERIALIZATION_VERSION)
        parcel.writeString(Button::class.java.getSimpleName())
        parcel.writeInt(bytes.size())
        parcel.writeByteArray(bytes.toByteArray())
        parcel.setDataPosition(0)

        val appCardTransport = AppCardTransport(parcel)

        val result = appCardTransport.component as Button?
        assertThat(result?.toByteArray()).isEqualTo(bytes.toByteArray())
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetComponent_button_appCardConstructor() {
        val expected = fromMessage(buttonMessage)

        val appCardTransport = AppCardTransport(expected)

        val result = appCardTransport.component as Button?
        assertThat(result?.toByteArray()).isEqualTo(expected.toByteArray())
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetAppCard_button_isNull() {
        val expected = fromMessage(buttonMessage)

        val appCardTransport = AppCardTransport(expected)

        assertThat(appCardTransport.appCard).isNull()
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetComponent_header_parcelConstructor() {
        val bytes = headerMessage.toByteString()
        val parcel = Parcel.obtain()
        parcel.writeInt(AppCardTransport.SUPPORTED_SERIALIZATION_VERSION)
        parcel.writeString(Header::class.java.getSimpleName())
        parcel.writeInt(bytes.size())
        parcel.writeByteArray(bytes.toByteArray())
        parcel.setDataPosition(0)

        val appCardTransport = AppCardTransport(parcel)

        val result = appCardTransport.component as Header?
        assertThat(result?.toByteArray()).isEqualTo(bytes.toByteArray())
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetComponent_header_appCardConstructor() {
        val expected = fromMessage(headerMessage)

        val appCardTransport = AppCardTransport(expected)

        val result = appCardTransport.component as Header?
        assertThat(result?.toByteArray()).isEqualTo(expected.toByteArray())
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetAppCard_header_isNull() {
        val expected = fromMessage(headerMessage)

        val appCardTransport = AppCardTransport(expected)

        assertThat(appCardTransport.appCard).isNull()
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetComponent_image_parcelConstructor() {
        val bytes = imageMessage.toByteString()
        val parcel = Parcel.obtain()
        parcel.writeInt(AppCardTransport.SUPPORTED_SERIALIZATION_VERSION)
        parcel.writeString(Image::class.java.getSimpleName())
        parcel.writeInt(bytes.size())
        parcel.writeByteArray(bytes.toByteArray())
        parcel.setDataPosition(0)

        val appCardTransport = AppCardTransport(parcel)

        val result = appCardTransport.component as Image?
        assertThat(result?.toByteArray()).isEqualTo(bytes.toByteArray())
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetComponent_image_appCardConstructor() {
        val expected = fromMessage(imageMessage)

        val appCardTransport = AppCardTransport(expected)

        val result = appCardTransport.component as Image?
        assertThat(result?.toByteArray()).isEqualTo(expected.toByteArray())
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetAppCard_image_isNull() {
        val expected = fromMessage(imageMessage)

        val appCardTransport = AppCardTransport(expected)

        assertThat(appCardTransport.appCard).isNull()
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetComponent_progressBar_parcelConstructor() {
        val bytes = progressBarMessage.toByteString()
        val parcel = Parcel.obtain()
        parcel.writeInt(AppCardTransport.SUPPORTED_SERIALIZATION_VERSION)
        parcel.writeString(ProgressBar::class.java.getSimpleName())
        parcel.writeInt(bytes.size())
        parcel.writeByteArray(bytes.toByteArray())
        parcel.setDataPosition(0)

        val appCardTransport = AppCardTransport(parcel)

        val result = appCardTransport.component as ProgressBar?
        assertThat(result?.toByteArray()).isEqualTo(bytes.toByteArray())
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetComponent_progressBar_appCardConstructor() {
        val expected = fromMessage(progressBarMessage)

        val appCardTransport = AppCardTransport(expected)

        val result = appCardTransport.component as ProgressBar?
        assertThat(result?.toByteArray()).isEqualTo(expected.toByteArray())
    }

    @Test
    @Throws(InvalidProtocolBufferException::class)
    fun testGetAppCard_progressBar_isEmpty() {
        val expected = fromMessage(progressBarMessage)

        val appCardTransport = AppCardTransport(expected)

        assertThat(appCardTransport.appCard).isNull()
    }

    @Test
    fun testWriteToParcel() {
        val bytes = imageAppCardMessage.toByteString()
        val expected = Parcel.obtain()
        expected.writeInt(TEST_UNSUPPORTED_SERIALIZATION_VERSION)
        expected.writeString(ImageAppCard::class.java.getSimpleName())
        expected.writeInt(bytes.size())
        expected.writeByteArray(bytes.toByteArray())
        expected.setDataPosition(0)
        val appCardTransport = AppCardTransport(expected)
        val result = Parcel.obtain()
        val flags = 0

        appCardTransport.writeToParcel(result, flags)

        result.setDataPosition(0)
        assertThat(result.marshall()).isEqualTo(expected.marshall())
    }

    companion object {
        private const val TEST_UNSUPPORTED_SERIALIZATION_VERSION =
            AppCardTransport.SUPPORTED_SERIALIZATION_VERSION + 1
        private const val TEST_ID = "ID"
        private const val TEST_PRIMARY_TEXT = "PRIMARY_TEXT"
        private const val TEST_SECONDARY_TEXT = "SECONDARY_TEXT"
        private const val TEST_COMPONENT_ID = "COMPONENT_ID"
        private const val TEST_TITLE = "TITLE"
    }
}
