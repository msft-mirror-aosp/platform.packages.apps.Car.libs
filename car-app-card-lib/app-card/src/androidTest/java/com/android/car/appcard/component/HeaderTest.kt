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

import android.graphics.drawable.GradientDrawable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.appcard.BitmapUtility
import com.android.car.appcard.component.Header.Companion.newBuilder
import com.android.car.appcard.component.ProgressBar.Companion.newBuilder
import com.android.car.appcard.internal.proto.Header
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeaderTest {
  private val bitMap = BitmapUtility.getSampleBitmap(strokeWidth = 3)
  private val image = Image.newBuilder(TEST_COMPONENT_ID)
    .setImageData(bitMap)
    .setContentScale(Image.ContentScale.FILL_BOUNDS)
    .build()

  @Test
  fun testGetTitle_nonNull() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .build()

    assertThat(header.title).isEqualTo(TEST_HEADER)
  }

  @Test
  fun testGetTitle_null() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(null)
      .build()

    assertThat(header.title).isNull()
  }

  @Test
  fun testGetLogo_null() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .build()

    assertThat(header.logo).isNull()
  }

  @Test
  fun testGetLogo_nonNull() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setImage(image)
      .build()

    assertThat(header.logo).isEqualTo(image)
  }

  @Test
  fun testEquals_sameObject_returnTrue() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()

    assertThat(header.equals(header)).isTrue()
  }

  @Test
  fun testEquals_allFieldsEqual_returnTrue() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val other = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()

    assertThat(header == other).isTrue()
  }

  @Test
  fun testEquals_differentClass_returnFalse() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val other = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()

    assertThat(header.equals(other)).isFalse()
  }

  @Test
  fun testEquals_differentComponentId_returnFalse() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val other = newBuilder(TEST_HEADER)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()

    assertThat(header == other).isFalse()
  }

  @Test
  fun testEquals_differentTitle_returnFalse() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val other = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_COMPONENT_ID)
      .setImage(image)
      .build()

    assertThat(header == other).isFalse()
  }

  @Test
  fun testEquals_differentLogo_returnFalse() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val otherBitMap = BitmapUtility.getSampleBitmap(GradientDrawable.OVAL, strokeWidth = 4)
    val otherImage = Image.newBuilder(TEST_COMPONENT_ID)
      .setImageData(otherBitMap)
      .setContentScale(Image.ContentScale.FILL_BOUNDS)
      .build()
    val other = newBuilder(TEST_COMPONENT_ID)
      .setImage(otherImage)
      .setTitle(TEST_COMPONENT_ID)
      .build()

    assertThat(header == other).isFalse()
  }

  @Test
  fun testGetComponentId_nonNull() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .build()

    assertThat(header.componentId).isEqualTo(TEST_COMPONENT_ID)
  }

  @Test
  fun testToMessage() {
    val headerMessage = Header.HeaderMessage.newBuilder()
      .setComponentId(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image.toMessage())
      .build()
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()

    assertThat(header.toMessage()).isEqualTo(headerMessage)
  }

  @Test
  fun testToByteArray() {
    val headerMessage = Header.HeaderMessage.newBuilder()
      .setComponentId(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image.toMessage())
      .build()
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()

    assertThat(header.toByteArray()).isEqualTo(headerMessage.toByteArray())
  }

  @Test
  fun testUpdateComponent_notHeader_returnFalse() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()

    assertThat(header.updateComponent(progressBar)).isFalse()
  }

  @Test
  fun testUpdateComponent_notEqualComponentId_returnFalse() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val input = newBuilder(TEST_HEADER)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()

    assertThat(header.updateComponent(input)).isFalse()
  }

  @Test
  fun testUpdateComponent_equalComponentId_returnTrue() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val input = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_COMPONENT_ID)
      .setImage(image)
      .build()

    assertThat(header.updateComponent(input)).isTrue()
  }

  @Test
  fun testUpdateComponent_equalComponentId_updatedCorrectly() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val input = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_COMPONENT_ID)
      .setImage(image)
      .build()

    header.updateComponent(input)

    assertThat(header.toMessage()).isEqualTo(input.toMessage())
  }

  @Test
  fun testUpdateComponent_imageComponent_updatedCorrectly() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val otherBitMap = BitmapUtility.getSampleBitmap(strokeWidth = 4)
    val otherImage = Image.newBuilder(TEST_COMPONENT_ID)
      .setImageData(otherBitMap)
      .setContentScale(Image.ContentScale.FILL_BOUNDS)
      .build()

    header.updateComponent(otherImage)

    assertThat(header.logo).isEqualTo(otherImage)
  }

  @Test
  fun testUpdateComponent_imageComponent_incorrectContentScale_returnFalse() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val otherBitMap = BitmapUtility.getSampleBitmap(strokeWidth = 4)
    val otherImage = Image.newBuilder(TEST_COMPONENT_ID)
      .setImageData(otherBitMap)
      .setContentScale(Image.ContentScale.FIT)
      .build()

    assertThat(header.updateComponent(otherImage)).isFalse()
  }

  @Test
  fun testUpdateComponent_imageComponent_imageTooLarge_returnFalse() {
    val header = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_HEADER)
      .setImage(image)
      .build()
    val tooLargeBitmap = BitmapUtility.getSampleBitmap(
      strokeWidth = 3,
      width = 1000,
      height = 1000
    )
    val otherImage = Image.newBuilder(TEST_COMPONENT_ID)
      .setImageData(tooLargeBitmap)
      .setContentScale(Image.ContentScale.FILL_BOUNDS)
      .build()

    assertThat(header.updateComponent(otherImage)).isFalse()
  }

  @Test(expected = IllegalStateException::class)
  fun testBuilder_setImage_incorrectContentScale_exceptionOccurred() {
    newBuilder(TEST_COMPONENT_ID)
      .setImage(Image.newBuilder(TEST_COMPONENT_ID).setContentScale(Image.ContentScale.FIT).build())
      .build()
  }

  @Test(expected = IllegalStateException::class)
  fun testBuilder_setImage_imageTooLarge_exceptionOccurred() {
    val tooLargeBitmap = BitmapUtility.getSampleBitmap(
      strokeWidth = 3,
      width = 1000,
      height = 1000
    )

    newBuilder(TEST_COMPONENT_ID)
      .setImage(
        Image.newBuilder(TEST_COMPONENT_ID)
        .setImageData(tooLargeBitmap)
        .setContentScale(Image.ContentScale.FILL_BOUNDS)
        .build()
      )
      .build()
  }

  companion object {
    private const val TEST_HEADER = "HEADER"
    private const val TEST_COMPONENT_ID = "TEST_COMPONENT_ID"
  }
}
