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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import java.io.ByteArrayOutputStream

object BitmapUtility {
    private const val TEST_SIDE = 100

    private fun getSampleDrawable(strokeWidth: Int): Drawable =
        getSampleDrawable(GradientDrawable.RECTANGLE, strokeWidth)

    private fun getSampleDrawable(shape: Int, strokeWidth: Int): Drawable {
        val drawable = GradientDrawable()
        drawable.setShape(shape)
        drawable.setCornerRadii(floatArrayOf(8f, 8f, 8f, 8f, 0f, 0f, 0f, 0f))
        drawable.setColor(Color.BLACK)
        drawable.setStroke(strokeWidth, Color.BLUE)
        return drawable
    }

    /** @return sample rectangular bitmap with given stroke width */
    @JvmStatic
    fun getSampleBitmap(strokeWidth: Int): Bitmap {
        val drawable = getSampleDrawable(strokeWidth)
        return fromDrawable(drawable, TEST_SIDE, TEST_SIDE)
    }

    /** @return sample bitmap with given stroke width and shape */
    @JvmStatic
    fun getSampleBitmap(shape: Int, strokeWidth: Int): Bitmap {
        val drawable = getSampleDrawable(shape, strokeWidth)
        return fromDrawable(drawable, TEST_SIDE, TEST_SIDE)
    }

    /** @return sample bitmap with given stroke width and size */
    @JvmStatic
    fun getSampleBitmap(strokeWidth: Int, width: Int, height: Int): Bitmap {
        val drawable = getSampleDrawable(strokeWidth)
        return fromDrawable(drawable, width, height)
    }

    private fun fromDrawable(drawable: Drawable, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val left = 0
        val top = 0
        drawable.setBounds(left, top, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /** @return byte array (as PNG with maximum quality) of [Bitmap] */
    @JvmStatic
    fun convertToBytes(bmp: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        val quality = 100
        bmp.compress(Bitmap.CompressFormat.PNG, quality, stream)
        return stream.toByteArray()
    }
}
