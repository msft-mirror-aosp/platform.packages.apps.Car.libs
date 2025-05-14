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

/** Utility class that provides helper methods for [Parcelable] conversion */
object ParcelableUtils {
    /** Convert bytes to [Parcelable] */
    @JvmStatic
    fun <T> bytesToParcelable(bytes: ByteArray, creator: Parcelable.Creator<T>): T {
        val parcel = bytesToParcel(bytes)
        val result = creator.createFromParcel(parcel)
        parcel.recycle()

        return result
    }

    /** Convert [Parcelable] to bytes */
    @JvmStatic
    fun parcelableToBytes(parcelable: Parcelable): ByteArray {
        val parcel = Parcel.obtain()
        parcelable.writeToParcel(parcel, PARCEL_FLAGS)
        val bytes = parcel.marshall()
        parcel.recycle()

        return bytes
    }

    private fun bytesToParcel(bytes: ByteArray): Parcel {
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, START_OFFSET, bytes.size)
        parcel.setDataPosition(START_OFFSET) // do not remove, needed for re-read

        return parcel
    }

    private const val START_OFFSET = 0
    private const val PARCEL_FLAGS = 0
}
