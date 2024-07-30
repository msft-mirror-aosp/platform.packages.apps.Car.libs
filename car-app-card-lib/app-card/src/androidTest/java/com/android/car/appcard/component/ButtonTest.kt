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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.appcard.BitmapUtility.convertToBytes
import com.android.car.appcard.BitmapUtility.getSampleBitmap
import com.android.car.appcard.component.Button.Companion.newBuilder
import com.android.car.appcard.component.Image.Companion.fromMessage
import com.android.car.appcard.component.ProgressBar.Companion.newBuilder
import com.android.car.appcard.component.interaction.OnClickListener
import com.android.car.appcard.internal.proto.Button.ButtonMessage
import com.android.car.appcard.internal.proto.Image.ColorFilter
import com.android.car.appcard.internal.proto.Image.ContentScale
import com.android.car.appcard.internal.proto.Image.ImageMessage
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.ByteString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ButtonTest {
  private val onClickListener: OnClickListener = TestOnClickListener()
  private lateinit var image: Image
  private lateinit var bitmap: Bitmap

  @Before
  fun setup() {
    bitmap = getSampleBitmap(strokeWidth = 3)

    val imageMessage = ImageMessage.newBuilder()
      .setComponentId(TEST_COMPONENT_ID)
      .setImage(ByteString.copyFrom(convertToBytes(bitmap)))
      .setColorFilter(ColorFilter.TINT)
      .setContentScale(ContentScale.FILL_BOUNDS)
      .build()
    image = fromMessage(imageMessage)
  }

  @Test
  fun testGetOnClickListener_nonNull() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setText(TEST_TEXT)
      .build()

    assertThat(button.onClickListener).isEqualTo(onClickListener)
  }

  @Test
  fun testGetText_nonNull() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setText(TEST_TEXT)
      .build()

    assertThat(button.text).isEqualTo(TEST_TEXT)
  }

  @Test
  fun testGetText_null() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .build()

    assertThat(button.text).isNull()
  }

  @Test
  fun testGetImage_nonNull() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .build()

    assertThat(button.image).isEqualTo(image)
  }

  @Test
  fun testGetImage_null() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setText(TEST_TEXT)
      .build()

    assertThat(button.image).isNull()
  }

  @Test(expected = IllegalStateException::class)
  fun testBuilder_noImage_noText_throwError() {
    newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener).build()
  }

  @Test(expected = IllegalStateException::class)
  fun testBuilder_noBackgroundType_noImage_throwError() {
    newBuilder(TEST_COMPONENT_ID, Button.ButtonType.NO_BACKGROUND, onClickListener)
      .setText(TEST_TEXT)
      .build()
  }

  @Test(expected = IllegalStateException::class)
  fun testBuilder_noBackgroundType_imageNoTint_throwError() {
    newBuilder(TEST_COMPONENT_ID, Button.ButtonType.NO_BACKGROUND, onClickListener)
      .setImage(
        fromMessage(
          ImageMessage.newBuilder()
            .setComponentId(TEST_COMPONENT_ID)
            .setImage(ByteString.copyFrom(convertToBytes(bitmap)))
            .setColorFilter(ColorFilter.NO_TINT)
            .setContentScale(ContentScale.FILL_BOUNDS)
            .build()
        )
      )
      .build()
  }

  @Test(expected = IllegalStateException::class)
  fun testBuilder_noBackgroundType_withText_throwError() {
    newBuilder(TEST_COMPONENT_ID, Button.ButtonType.NO_BACKGROUND, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()
  }

  @Test
  fun testGetButtonType_primary() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setText(TEST_TEXT)
      .build()

    assertThat(button.buttonType).isEqualTo(Button.ButtonType.PRIMARY)
  }

  @Test
  fun testGetButtonType_secondary() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.SECONDARY, onClickListener)
      .setText(TEST_TEXT)
      .build()

    assertThat(button.buttonType).isEqualTo(Button.ButtonType.SECONDARY)
  }

  @Test
  fun testGetButtonType_noBackground() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.NO_BACKGROUND, onClickListener)
      .setImage(image)
      .build()

    assertThat(button.buttonType).isEqualTo(Button.ButtonType.NO_BACKGROUND)
  }

  @Test
  fun testGetComponentId_nonNull() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.SECONDARY, onClickListener)
      .setText(TEST_TEXT)
      .build()

    assertThat(button.componentId).isEqualTo(TEST_COMPONENT_ID)
  }

  @Test
  fun testToMessage() {
    val buttonMessage = ButtonMessage.newBuilder()
      .setComponentId(TEST_COMPONENT_ID)
      .setType(com.android.car.appcard.internal.proto.Button.ButtonType.PRIMARY)
      .setImage(image.toMessage())
      .build()
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .build()

    assertThat(button.toMessage()).isEqualTo(buttonMessage)
  }

  @Test
  fun testToByteArray() {
    val buttonMessage = ButtonMessage.newBuilder()
      .setComponentId(TEST_COMPONENT_ID)
      .setType(com.android.car.appcard.internal.proto.Button.ButtonType.PRIMARY)
      .setText(TEST_TEXT)
      .build()
    val button = newBuilder(
      TEST_COMPONENT_ID,
      Button.ButtonType.PRIMARY,
      onClickListener
    )
      .setText(TEST_TEXT)
      .build()

    assertThat(button.toByteArray()).isEqualTo(buttonMessage.toByteArray())
  }

  @Test
  fun testUpdateComponent_notRecognizedComponent_imageNull_returnFalse() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setText(TEST_TEXT)
      .build()
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()

    assertThat(button.updateComponent(progressBar)).isFalse()
  }

  @Test
  fun testUpdateComponent_noBackgroundType_notColorFilterTint_returnFalse() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.NO_BACKGROUND, onClickListener)
      .setImage(image)
      .build()
    val imageMessage = ImageMessage.newBuilder()
      .setComponentId(TEST_COMPONENT_ID)
      .setImage(ByteString.copyFrom(convertToBytes(bitmap)))
      .setColorFilter(ColorFilter.NO_TINT)
      .setContentScale(ContentScale.FILL_BOUNDS)
      .build()
    val image = fromMessage(imageMessage)

    assertThat(button.updateComponent(image)).isFalse()
  }

  @Test
  fun testUpdateComponent_imageComponent_imageUpdateComponentCalled() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .build()
    val imageMessage = ImageMessage.newBuilder()
      .setComponentId(TEST_COMPONENT_ID)
      .setImage(ByteString.copyFrom(convertToBytes(bitmap)))
      .setColorFilter(ColorFilter.TINT)
      .setContentScale(ContentScale.FILL_BOUNDS)
      .build()
    val image = fromMessage(imageMessage)

    assertThat(button.updateComponent(image)).isTrue()
  }

  @Test
  fun testUpdateComponent_buttonComponent_notEqualComponentId_returnFalse() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .build()
    val input = newBuilder(TEST_TEXT, Button.ButtonType.PRIMARY, onClickListener)
      .setText(TEST_TEXT)
      .build()

    assertThat(button.updateComponent(input)).isFalse()
  }

  @Test
  fun testUpdateComponent_buttonComponent_equalComponentId_returnTrue() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .build()
    val input = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.SECONDARY, onClickListener)
      .setText(TEST_TEXT)
      .build()

    assertThat(button.updateComponent(input)).isTrue()
  }

  @Test
  fun testUpdateComponent_buttonComponent_equalComponentId_correctUpdate() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .build()
    val input = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.SECONDARY, onClickListener)
      .setText(TEST_TEXT)
      .build()

    button.updateComponent(input)

    assertThat(button.toMessage()).isEqualTo(input.toMessage())
  }

  @Test
  fun testUpdateComponent_imageComponent_incorrectContentScale_returnFalse() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .build()
    val otherBitMap = getSampleBitmap(strokeWidth = 4)
    val otherImage = Image.newBuilder(TEST_COMPONENT_ID)
      .setImageData(otherBitMap)
      .setContentScale(Image.ContentScale.FIT)
      .build()

    assertThat(button.updateComponent(otherImage)).isFalse()
  }

  @Test
  fun testUpdateComponent_imageComponent_imageTooLarge_returnFalse() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .build()
    val tooLargeBitmap = getSampleBitmap(
      strokeWidth = 3,
      width = 1000,
      height = 1001
    )
    val otherImage = Image.newBuilder(TEST_COMPONENT_ID)
      .setImageData(tooLargeBitmap)
      .setContentScale(Image.ContentScale.FILL_BOUNDS)
      .build()

    assertThat(button.updateComponent(otherImage)).isFalse()
  }

  @Test
  fun testEquals_allFieldsEqual_returnTrue() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()
    val input = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()

    assertThat(button == input).isTrue()
  }

  @Test
  fun testEquals_sameObject_returnTrue() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()

    assertThat(button.equals(button)).isTrue()
  }

  @Test
  fun testEquals_differentClass_returnFalse() {
    val button = newBuilder(
      TEST_COMPONENT_ID,
      Button.ButtonType.PRIMARY,
      onClickListener
    )
      .setImage(image)
      .setText(TEST_TEXT)
      .build()
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()

    assertThat(button.equals(progressBar)).isFalse()
  }

  @Test
  fun testEquals_differentComponentId_returnFalse() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()
    val input = newBuilder(TEST_TEXT, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()

    assertThat(button == input).isFalse()
  }

  @Test
  fun testEquals_differentButtonType_returnFalse() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()
    val input = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.SECONDARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()

    assertThat(button == input).isFalse()
  }

  @Test
  fun testEquals_differentOnClickListener_returnFalse() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()
    val input = newBuilder(
      TEST_COMPONENT_ID,
      Button.ButtonType.PRIMARY,
      object : OnClickListener {
        override fun onClick() {
        }
      }
    )
      .setImage(image)
      .setText(TEST_TEXT)
      .build()

    assertThat(button == input).isFalse()
  }

  @Test
  fun testEquals_differentImage_returnFalse() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()
    val imageMessage = ImageMessage.newBuilder()
      .setComponentId(TEST_TEXT)
      .setImage(ByteString.copyFrom(convertToBytes(bitmap)))
      .setColorFilter(ColorFilter.NO_TINT)
      .setContentScale(ContentScale.FILL_BOUNDS)
      .build()
    val image = fromMessage(imageMessage)
    val input = newBuilder(TEST_TEXT, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()

    assertThat(button == input).isFalse()
  }

  @Test
  fun testEquals_differentText_returnFalse() {
    val button = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_TEXT)
      .build()
    val input = newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(image)
      .setText(TEST_COMPONENT_ID)
      .build()

    assertThat(button == input).isFalse()
  }

  @Test(expected = IllegalStateException::class)
  fun testBuilder_setImage_incorrectContentScale_exceptionOccurred() {
    newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(Image.newBuilder(TEST_COMPONENT_ID).setContentScale(Image.ContentScale.FIT).build())
      .build()
  }

  @Test(expected = IllegalStateException::class)
  fun testBuilder_setImage_imageTooLarge_exceptionOccurred() {
    val tooLargeBitmap = getSampleBitmap(
      strokeWidth = 3,
      width = 1001,
      height = 1000
    )

    newBuilder(TEST_COMPONENT_ID, Button.ButtonType.PRIMARY, onClickListener)
      .setImage(
        Image.newBuilder(TEST_COMPONENT_ID)
          .setImageData(tooLargeBitmap)
          .setContentScale(Image.ContentScale.FILL_BOUNDS)
          .build()
      )
      .build()
  }

  private class TestOnClickListener : OnClickListener {
    override fun onClick() {
    }
  }

  companion object {
    private const val TEST_TEXT = "TEXT"
    private const val TEST_COMPONENT_ID = "TEST_COMPONENT_ID"
  }
}
