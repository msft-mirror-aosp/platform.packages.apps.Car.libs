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
import android.graphics.BitmapFactory
import android.util.Log
import com.android.car.appcard.internal.proto.Image.ImageMessage
import com.google.protobuf.ByteString
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

/**
 * Image is a component that contains an image and information on how the image should be displayed.
 */
class Image private constructor(builder: Builder) : Component(builder) {
    /** @return [ContentScale] */
    var contentScale: ContentScale
        private set

    /** @return [ColorFilter] */
    var colorFilter: ColorFilter
        private set

    /** @return [android.graphics.Bitmap] */
    var imageData: Bitmap?
        private set

    private var imageHashString: String? = null

    init {
        imageData = builder.imageData
        contentScale = builder.contentScale
        colorFilter = builder.colorFilter
        updateImageHash()
    }

    /** @return protobuf message */
    fun toMessage(): ImageMessage =
        ImageMessage.newBuilder()
            .setComponentId(componentId)
            .setImage(imageData?.let { ByteString.copyFrom(toByteArray(it)) })
            .setContentScale(toContentScale(contentScale))
            .setColorFilter(toColorFilter(colorFilter))
            .build()

    /** @return protobuf byte array */
    override fun toByteArray(): ByteArray = toMessage().toByteArray()

    override fun updateComponent(component: Component): Boolean {
        if (component !is Image || componentId != component.componentId) return false

        component.imageData?.let {
            imageData = component.imageData
            updateImageHash()
        }

        contentScale = component.contentScale
        colorFilter = component.colorFilter

        return true
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true

        if (other !is Image) return false

        val imageEquals =
            imageHashString?.let { it == other.imageHashString } ?: (other.imageHashString == null)
        return imageEquals &&
            super.equals(other) &&
            contentScale == other.contentScale &&
            colorFilter == other.colorFilter
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + contentScale.hashCode()
        result = 31 * result + colorFilter.hashCode()
        result = 31 * result + (imageData?.hashCode() ?: 0)
        return result
    }

    private fun updateImageHash() {
        val md = MessageDigest.getInstance(HASH_ALGORITHM)
        val imageByteArray = imageData?.let { toByteArray(it) }
        val hashBytes = imageByteArray?.let { md.digest(it) }
        imageHashString = hashBytes?.joinToString { HEX_FORMAT.format(it) }
    }

    /**
     * This enum will advise the system on how the image should be scaled.
     * - FIT: Scale the image uniformly, keeping the aspect ratio
     * - FILL_BOUNDS: Scale the content vertically & horizontally non-uniformly to fill the bounds
     */
    enum class ContentScale {
        FIT,
        FILL_BOUNDS,
    }

    /**
     * This enum will advise the system on whether this image can be tinted or not.
     * - TINT: The image can be tinted by the system
     * - NO_TINT: The image must not be tinted by the system
     */
    enum class ColorFilter {
        TINT,
        NO_TINT,
    }

    /** A builder of [Image] */
    class Builder(componentId: String) : Component.Builder(componentId) {
        internal var imageData: Bitmap? = null
        internal var contentScale: ContentScale
        internal var colorFilter: ColorFilter

        internal constructor(imageMessage: ImageMessage) : this(imageMessage.componentId) {
            if (imageMessage.hasImage() && !imageMessage.image.isEmpty) {
                imageData =
                    BitmapFactory.decodeByteArray(
                        imageMessage.image.toByteArray(),
                        BITMAP_OFFSET,
                        imageMessage.image.size(),
                    )
                imageData ?: run { Log.e(TAG, "Image was unable to be created from bundle") }
            }

            contentScale =
                if (
                    imageMessage.getContentScale() ==
                        com.android.car.appcard.internal.proto.Image.ContentScale.FILL_BOUNDS
                ) {
                    ContentScale.FILL_BOUNDS
                } else if (
                    imageMessage.getContentScale() ==
                        com.android.car.appcard.internal.proto.Image.ContentScale.FIT
                ) {
                    ContentScale.FIT
                } else {
                    throw IllegalStateException("ContentScale must not be unrecognized")
                }

            colorFilter =
                if (
                    imageMessage.getColorFilter() ==
                        com.android.car.appcard.internal.proto.Image.ColorFilter.TINT
                ) {
                    ColorFilter.TINT
                } else if (
                    imageMessage.getColorFilter() ==
                        com.android.car.appcard.internal.proto.Image.ColorFilter.NO_TINT
                ) {
                    ColorFilter.NO_TINT
                } else {
                    throw IllegalStateException("ContentScale must not be unrecognized")
                }
        }

        init {
            contentScale = ContentScale.FIT
            colorFilter = ColorFilter.TINT
        }

        /** Set image data */
        fun setImageData(imageData: Bitmap?): Builder {
            this.imageData = imageData
            return this
        }

        /** Set [ContentScale] */
        fun setContentScale(contentScale: ContentScale): Builder {
            this.contentScale = contentScale
            return this
        }

        /** Set [ColorFilter] */
        fun setColorFilter(colorFilter: ColorFilter): Builder {
            this.colorFilter = colorFilter
            return this
        }

        /** @return [Image] built using this builder */
        fun build(): Image = Image(builder = this)
    }

    companion object {
        private const val TAG = "Image"
        private const val BITMAP_QUALITY = 100
        private const val BITMAP_OFFSET = 0
        private const val HASH_ALGORITHM = "SHA-512"
        private const val HEX_FORMAT = "%02x"

        /** @return an instance of [Builder] */
        @JvmStatic fun newBuilder(componentId: String) = Builder(componentId)

        private fun toContentScale(
            contentScale: ContentScale
        ): com.android.car.appcard.internal.proto.Image.ContentScale {
            return if (contentScale == ContentScale.FILL_BOUNDS) {
                com.android.car.appcard.internal.proto.Image.ContentScale.FILL_BOUNDS
            } else {
                com.android.car.appcard.internal.proto.Image.ContentScale.FIT
            }
        }

        private fun toColorFilter(
            colorFilter: ColorFilter
        ): com.android.car.appcard.internal.proto.Image.ColorFilter {
            return if (colorFilter == ColorFilter.NO_TINT) {
                com.android.car.appcard.internal.proto.Image.ColorFilter.NO_TINT
            } else {
                com.android.car.appcard.internal.proto.Image.ColorFilter.TINT
            }
        }

        /** @return an instance of [Image] from [ImageMessage] */
        @JvmStatic fun fromMessage(imageMessage: ImageMessage) = Builder(imageMessage).build()

        private fun toByteArray(bmp: Bitmap?): ByteArray? {
            bmp
                ?: run {
                    return null
                }

            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, BITMAP_QUALITY, stream)

            return stream.toByteArray()
        }
    }
}
