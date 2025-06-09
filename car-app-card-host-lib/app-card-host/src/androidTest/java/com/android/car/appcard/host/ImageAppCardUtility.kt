/*
 * Copyright 2024 The Android Open Source Project
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

package com.android.car.appcard.host

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import com.android.car.appcard.ImageAppCard
import com.android.car.appcard.component.Button
import com.android.car.appcard.component.Header
import com.android.car.appcard.component.Image
import com.android.car.appcard.component.ProgressBar
import com.android.car.appcard.component.interaction.OnClickListener

class ImageAppCardUtility {
    companion object {
        const val TEST_ID = "ID"
        const val TEST_PRIMARY_TEXT = "PRIMARY_TEXT"
        const val TEST_SECONDARY_TEXT = "SECONDARY_TEXT"
        const val TEST_IMAGE_COMPONENT_ID = "TEST_IMAGE_COMPONENT_ID"
        const val TEST_HEADER_COMPONENT_ID = "TEST_HEADER_COMPONENT_ID"
        const val TEST_PROGRESS_BAR_COMPONENT_ID = "TEST_PROGRESS_BAR_COMPONENT_ID"
        const val TEST_BUTTON_COMPONENT_ID = "TEST_BUTTON_COMPONENT_ID"
        const val TEST_TITLE = "TITLE"

        private fun getSampleDrawable(): Drawable {
            val shape = GradientDrawable()
            shape.setShape(GradientDrawable.RECTANGLE)
            shape.setCornerRadii(floatArrayOf(8f, 8f, 8f, 8f, 0f, 0f, 0f, 0f))
            shape.setColor(Color.BLACK)
            val width = 3
            shape.setStroke(width, Color.BLUE)
            return shape
        }

        private fun getSampleBitmap(): Bitmap {
            val drawable = getSampleDrawable()

            val width = if (drawable.intrinsicWidth <= 0) 1 else drawable.intrinsicWidth
            val height = if (drawable.intrinsicHeight <= 0) 1 else drawable.intrinsicHeight

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            val left = 0
            val top = 0
            drawable.setBounds(left, top, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }

        val button =
            Button.newBuilder(
                    TEST_BUTTON_COMPONENT_ID,
                    Button.ButtonType.PRIMARY,
                    object : OnClickListener {
                        override fun onClick() {}
                    },
                )
                .setText(TEST_TITLE)
                .build()
        val header = Header.newBuilder(TEST_HEADER_COMPONENT_ID).setTitle(TEST_TITLE).build()
        val progressBar =
            ProgressBar.newBuilder(TEST_PROGRESS_BAR_COMPONENT_ID, min = 0, max = 2)
                .setProgress(1)
                .build()
        val image =
            Image.newBuilder(TEST_IMAGE_COMPONENT_ID).setImageData(getSampleBitmap()).build()
        val imageAppCard =
            ImageAppCard.newBuilder(TEST_ID)
                .setImage(image)
                .setHeader(header)
                .setPrimaryText(TEST_PRIMARY_TEXT)
                .setSecondaryText(TEST_SECONDARY_TEXT)
                .build()
        val progressBarButtonCard =
            ImageAppCard.newBuilder(TEST_ID)
                .setHeader(header)
                .setPrimaryText(TEST_PRIMARY_TEXT)
                .setSecondaryText(TEST_SECONDARY_TEXT)
                .setProgressBar(progressBar)
                .addButton(button)
                .build()
    }
}
