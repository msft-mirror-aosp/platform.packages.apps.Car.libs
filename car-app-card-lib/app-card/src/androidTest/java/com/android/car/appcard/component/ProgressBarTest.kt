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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.appcard.component.Button.Companion.newBuilder
import com.android.car.appcard.component.Header.Companion.newBuilder
import com.android.car.appcard.component.ProgressBar.Companion.newBuilder
import com.android.car.appcard.component.interaction.OnClickListener
import com.android.car.appcard.internal.proto.ProgressBar
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressBarTest {

  @Test(expected = IllegalStateException::class)
  fun testBuilder_maxLessThanMin_throwError() {
    newBuilder(TEST_COMPONENT_ID, min = 1, max = 0).build()
  }

  @Test(expected = IllegalStateException::class)
  fun testBuilder_progressLessThanMin_throwError() {
    newBuilder(TEST_COMPONENT_ID, min = 1, max = 2)
      .setProgress(0)
      .build()
  }

  @Test(expected = IllegalStateException::class)
  fun testBuilder_maxLessThanProgress_throwError() {
    newBuilder(TEST_COMPONENT_ID, min = 0, max = 1)
      .setProgress(2)
      .build()
  }

  @Test
  fun testEquals_sameObject_returnTrue() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 1, max = 3)
      .setProgress(2)
      .build()

    assertThat(progressBar.equals(progressBar)).isTrue()
  }

  @Test
  fun testEquals_allFieldsEqual_returnTrue() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 1, max = 3)
      .setProgress(2)
      .build()
    val other = newBuilder(TEST_COMPONENT_ID, min = 1, max = 3)
      .setProgress(2)
      .build()

    assertThat(progressBar == other).isTrue()
  }

  @Test
  fun testEquals_differentClass_returnFalse() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 1, max = 3)
      .setProgress(2)
      .build()
    val other = newBuilder(TEST_COMPONENT_ID)
      .setTitle(TEST_OTHER_COMPONENT_ID)
      .build()

    assertThat(progressBar.equals(other)).isFalse()
  }

  @Test
  fun testEquals_differentComponentId_returnFalse() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 1, max = 3)
      .setProgress(2)
      .build()
    val other = newBuilder(TEST_OTHER_COMPONENT_ID, min = 1, max = 3)
      .setProgress(2)
      .build()

    assertThat(progressBar == other).isFalse()
  }

  @Test
  fun testEquals_differentMin_returnFalse() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 1, max = 3)
      .setProgress(2)
      .build()
    val other = newBuilder(TEST_COMPONENT_ID, min = 0, max = 3)
      .setProgress(2)
      .build()

    assertThat(progressBar == other).isFalse()
  }

  @Test
  fun testEquals_differentMax_returnFalse() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 1, max = 3)
      .setProgress(2)
      .build()
    val other = newBuilder(TEST_COMPONENT_ID, min = 1, max = 4)
      .setProgress(2)
      .build()

    assertThat(progressBar == other).isFalse()
  }

  @Test
  fun testEquals_differentProgress_returnFalse() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 1, max = 3)
      .setProgress(2)
      .build()
    val other = newBuilder(TEST_COMPONENT_ID, min = 1, max = 3)
      .setProgress(1)
      .build()

    assertThat(progressBar == other).isFalse()
  }

  @Test
  fun testGetMax() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()

    assertThat(progressBar.max).isEqualTo(1)
  }

  @Test
  fun testGetComponentId_nonNull() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()

    assertThat(progressBar.componentId).isEqualTo(TEST_COMPONENT_ID)
  }

  @Test
  fun testGetMin() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = -1, max = 1)
      .build()

    assertThat(progressBar.min).isEqualTo(-1)
  }

  @Test
  fun testGetProgress_defaultValue() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()

    assertThat(progressBar.progress).isEqualTo(0)
  }

  @Test
  fun testGetProgress_setValue() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1)
      .setProgress(1)
      .build()

    assertThat(progressBar.progress).isEqualTo(1)
  }

  @Test
  fun testToMessage() {
    val progressBarMessage = ProgressBar.ProgressBarMessage.newBuilder()
      .setComponentId(TEST_COMPONENT_ID)
      .setMax(2)
      .setProgress(1)
      .setMin(0)
      .build()
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 2)
      .setProgress(1)
      .build()

    assertThat(progressBar.toMessage()).isEqualTo(progressBarMessage)
  }

  @Test
  fun testToByteArray() {
    val progressBarMessage = ProgressBar.ProgressBarMessage.newBuilder()
      .setComponentId(TEST_COMPONENT_ID)
      .setMax(2)
      .setProgress(1)
      .setMin(0)
      .build()
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 2)
      .setProgress(1)
      .build()

    assertThat(progressBar.toByteArray()).isEqualTo(progressBarMessage.toByteArray())
  }

  @Test
  fun testUpdateComponent_notProgressBar_returnFalse() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()
    val button = newBuilder(
      TEST_COMPONENT_ID,
      Button.ButtonType.PRIMARY,
      object : OnClickListener {
        override fun onClick() {
        }
      }
    )
      .setText(TEST_OTHER_COMPONENT_ID)
      .build()

    assertThat(progressBar.updateComponent(button)).isFalse()
  }

  @Test
  fun testUpdateComponent_notEqualComponentId_returnFalse() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()
    val input = newBuilder(TEST_OTHER_COMPONENT_ID, min = 0, max = 1).build()

    assertThat(progressBar.updateComponent(input)).isFalse()
  }

  @Test
  fun testUpdateComponent_equalComponentId_returnTrue() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()
    val input = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()

    assertThat(progressBar.updateComponent(input)).isTrue()
  }

  @Test
  fun testUpdateComponent_equalComponentId_correctUpdate() {
    val progressBar = newBuilder(TEST_COMPONENT_ID, min = 0, max = 1).build()
    val input = newBuilder(TEST_COMPONENT_ID, min = 1, max = 100)
      .setProgress(10)
      .build()

    progressBar.updateComponent(input)

    assertThat(progressBar.toMessage()).isEqualTo(input.toMessage())
  }

  companion object {
    private const val TEST_COMPONENT_ID = "TEST_COMPONENT_ID"
    private const val TEST_OTHER_COMPONENT_ID = "TEST_OTHER_COMPONENT_ID"
  }
}
