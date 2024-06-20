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
package com.android.car.appcard.util

import android.os.Parcel
import android.os.Parcelable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.appcard.util.ParcelableUtils.bytesToParcelable
import com.android.car.appcard.util.ParcelableUtils.parcelableToBytes
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParcelableUtilsTest {
  @Test
  fun testBytesToParcelable() {
    val expected = TestParcelable(TEST_DATA)
    val source = byteArrayOf(4, 0, 0, 0, 68, 0, 65, 0, 84, 0, 65, 0, 0, 0, 0, 0)

    val result = bytesToParcelable(source, TestParcelable.CREATOR)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun testParcelableToBytes() {
    val expected = byteArrayOf(4, 0, 0, 0, 68, 0, 65, 0, 84, 0, 65, 0, 0, 0, 0, 0)
    val source = TestParcelable(TEST_DATA)

    val result = parcelableToBytes(source)

    assertThat(result).isEqualTo(expected)
  }

  private class TestParcelable(private val mData: String?) : Parcelable {
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
      dest.writeString(mData)
    }

    override fun equals(other: Any?): Boolean {
      if (other is TestParcelable) {
        return mData == other.mData
      }
      return false
    }

    override fun hashCode(): Int = mData?.hashCode() ?: 0

    companion object {
      @JvmField
      val CREATOR: Parcelable.Creator<TestParcelable> =
        object : Parcelable.Creator<TestParcelable> {
          override fun createFromParcel(source: Parcel): TestParcelable =
            TestParcelable(source.readString())

          override fun newArray(size: Int): Array<TestParcelable?> = arrayOfNulls(size)
        }
    }
  }

  companion object {
    private const val TEST_DATA = "DATA"
  }
}
