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
package com.android.car.appcard.component

import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.appcard.BitmapUtility.convertToBytes
import com.android.car.appcard.BitmapUtility.getSampleBitmap
import com.android.car.appcard.component.Image.Companion.newBuilder
import com.android.car.appcard.component.ProgressBar.Companion.newBuilder
import com.android.car.appcard.internal.proto.Image.ImageMessage
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.ByteString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageTest {
    private lateinit var bitmap: Bitmap

    @Before
    fun setup() {
        bitmap = getSampleBitmap(strokeWidth = 3)
    }

    @Test
    fun testGetImageData_nonNull() {
        val image = newBuilder(TEST_COMPONENT_ID).setImageData(bitmap).build()

        assertThat(image.imageData).isEqualTo(bitmap)
    }

    @Test
    fun testGetImageData_null() {
        val image = newBuilder(TEST_COMPONENT_ID).build()

        assertThat(image.imageData).isNull()
    }

    @Test
    fun testGetComponentId_nonNull() {
        val image = newBuilder(TEST_COMPONENT_ID).setImageData(bitmap).build()

        assertThat(image.componentId).isEqualTo(TEST_COMPONENT_ID)
    }

    @Test
    fun testGetColorFilter_tint() {
        val image = newBuilder(TEST_COMPONENT_ID).setColorFilter(Image.ColorFilter.TINT).build()

        assertThat(image.colorFilter).isEqualTo(Image.ColorFilter.TINT)
    }

    @Test
    fun testGetColorFilter_noTint() {
        val image = newBuilder(TEST_COMPONENT_ID).setColorFilter(Image.ColorFilter.NO_TINT).build()

        assertThat(image.colorFilter).isEqualTo(Image.ColorFilter.NO_TINT)
    }

    @Test
    fun testGetColorFilter_defaultValue() {
        val image = newBuilder(TEST_COMPONENT_ID).build()

        assertThat(image.colorFilter).isEqualTo(Image.ColorFilter.TINT)
    }

    @Test
    fun testGetContentScale_fillBounds() {
        val image =
            newBuilder(TEST_COMPONENT_ID).setContentScale(Image.ContentScale.FILL_BOUNDS).build()

        assertThat(image.contentScale).isEqualTo(Image.ContentScale.FILL_BOUNDS)
    }

    @Test
    fun testGetContentScale_fit() {
        val image = newBuilder(TEST_COMPONENT_ID).setContentScale(Image.ContentScale.FIT).build()

        assertThat(image.contentScale).isEqualTo(Image.ContentScale.FIT)
    }

    @Test
    fun testGetContentScale_defaultValue() {
        val image = newBuilder(TEST_COMPONENT_ID).build()

        assertThat(image.contentScale).isEqualTo(Image.ContentScale.FIT)
    }

    @Test
    fun testToMessage() {
        val imageMessage =
            ImageMessage.newBuilder()
                .setComponentId(TEST_COMPONENT_ID)
                .setImage(ByteString.copyFrom(convertToBytes(bitmap)))
                .setColorFilter(com.android.car.appcard.internal.proto.Image.ColorFilter.NO_TINT)
                .setContentScale(com.android.car.appcard.internal.proto.Image.ContentScale.FIT)
                .build()
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()

        assertThat(image.toMessage()).isEqualTo(imageMessage)
    }

    @Test
    fun testToByteArray() {
        val imageMessage =
            ImageMessage.newBuilder()
                .setComponentId(TEST_COMPONENT_ID)
                .setImage(ByteString.copyFrom(convertToBytes(bitmap)))
                .setColorFilter(com.android.car.appcard.internal.proto.Image.ColorFilter.NO_TINT)
                .setContentScale(com.android.car.appcard.internal.proto.Image.ContentScale.FIT)
                .build()
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()

        assertThat(image.toByteArray()).isEqualTo(imageMessage.toByteArray())
    }

    @Test
    fun testEquals_sameObject_returnTrue() {
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()

        assertThat(image.equals(image)).isTrue()
    }

    @Test
    fun testEquals_allFieldsEqual_returnTrue() {
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()
        val other =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()

        assertThat(image == other).isTrue()
    }

    @Test
    fun testEquals_differentClass_returnFalse() {
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()
        val other = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()

        assertThat(image.equals(other)).isFalse()
    }

    @Test
    fun testEquals_differentComponentId_returnFalse() {
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()
        val other =
            newBuilder(componentId = TEST_COMPONENT_ID + TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()

        assertThat(image == other).isFalse()
    }

    @Test
    fun testEquals_differentImage_returnFalse() {
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()
        val other =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(getSampleBitmap(GradientDrawable.OVAL, strokeWidth = 10))
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()

        assertThat(image == other).isFalse()
    }

    @Test
    fun testEquals_differentColorFilter_returnFalse() {
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()
        val other =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()

        assertThat(image == other).isFalse()
    }

    @Test
    fun testEquals_differentContentScale_returnFalse() {
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FIT)
                .build()
        val other =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(bitmap)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .setContentScale(Image.ContentScale.FILL_BOUNDS)
                .build()

        assertThat(image == other).isFalse()
    }

    @Test
    fun testUpdateComponent_notImage_returnFalse() {
        val image =
            newBuilder(TEST_COMPONENT_ID).setContentScale(Image.ContentScale.FILL_BOUNDS).build()
        val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()

        assertThat(image.updateComponent(progressBar)).isFalse()
    }

    @Test
    fun testUpdateComponent_notEqualComponentId_returnFalse() {
        val image =
            newBuilder(TEST_COMPONENT_ID).setContentScale(Image.ContentScale.FILL_BOUNDS).build()
        val input =
            newBuilder(TEST_OTHER_COMPONENT_ID)
                .setContentScale(Image.ContentScale.FILL_BOUNDS)
                .build()

        assertThat(image.updateComponent(input)).isFalse()
    }

    @Test
    fun testUpdateComponent_equalComponentId_returnTrue() {
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(getSampleBitmap(strokeWidth = 3))
                .setContentScale(Image.ContentScale.FILL_BOUNDS)
                .setColorFilter(Image.ColorFilter.TINT)
                .build()
        val input =
            newBuilder(TEST_COMPONENT_ID)
                .setContentScale(Image.ContentScale.FIT)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .build()

        assertThat(image.updateComponent(input)).isTrue()
    }

    @Test
    fun testUpdateComponent_equalComponentId_nullImage_imageNotUpdated() {
        val expectedImage = getSampleBitmap(strokeWidth = 3)
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(expectedImage)
                .setContentScale(Image.ContentScale.FILL_BOUNDS)
                .setColorFilter(Image.ColorFilter.TINT)
                .build()
        val input =
            newBuilder(TEST_COMPONENT_ID)
                .setContentScale(Image.ContentScale.FIT)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .build()

        image.updateComponent(input)

        assertThat(image.imageData).isEqualTo(expectedImage)
    }

    @Test
    fun testUpdateComponent_equalComponentId_nonNullImage_imageUpdated() {
        val expectedImage = getSampleBitmap(strokeWidth = 3)
        val image =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(getSampleBitmap(strokeWidth = 10))
                .setContentScale(Image.ContentScale.FILL_BOUNDS)
                .setColorFilter(Image.ColorFilter.TINT)
                .build()
        val input =
            newBuilder(TEST_COMPONENT_ID)
                .setImageData(expectedImage)
                .setContentScale(Image.ContentScale.FIT)
                .setColorFilter(Image.ColorFilter.NO_TINT)
                .build()

        image.updateComponent(input)

        assertThat(image.imageData).isEqualTo(expectedImage)
    }

    companion object {
        private const val TEST_COMPONENT_ID = "TEST_COMPONENT_ID"
        private const val TEST_OTHER_COMPONENT_ID = "TEST_OTHER_COMPONENT_ID"
    }
}
