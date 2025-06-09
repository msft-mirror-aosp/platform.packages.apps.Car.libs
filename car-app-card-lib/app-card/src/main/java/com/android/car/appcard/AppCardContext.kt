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

import android.os.Bundle
import android.util.Size
import com.android.car.appcard.annotations.EnforceFastUpdateRate
import com.android.car.appcard.component.Button
import com.android.car.appcard.component.Header

/**
 * Provides application developers with certain contextual information on how their app cards are
 * going to be displayed.
 */
class AppCardContext(
    apiLevel: Int,
    refreshPeriod: Int,
    fastRefreshPeriod: Int,
    isInteractable: Boolean,
    imageContext: ImageAppCardContext?,
) {
    /** Get system's app card version */
    val apiLevel: Int

    /** Get OEM defined refresh period in ms (minimum of 1000ms) */
    val refreshPeriod: Int

    /** Get OEM defined refresh period for [EnforceFastUpdateRate] in ms (minimum of 500ms) */
    val fastRefreshPeriod: Int

    /** @return `true` if App Card view will be interactable */
    val isInteractable: Boolean

    /** @return [ImageAppCardContext] */
    val imageAppCardContext: ImageAppCardContext

    constructor(
        apiLevel: Int,
        refreshPeriod: Int,
        fastRefreshPeriod: Int,
        isInteractable: Boolean,
        imageAppCardSize: Size,
        buttonSize: Size,
        headerSize: Size,
        minGuaranteedButtons: Int,
    ) : this(
        apiLevel,
        refreshPeriod,
        fastRefreshPeriod,
        isInteractable,
        ImageAppCardContext(imageAppCardSize, buttonSize, headerSize, minGuaranteedButtons),
    )

    init {
        check(value = apiLevel == 1) { "API Level must be valid" }
        check(value = refreshPeriod >= 0) { "Refresh period must not be negative" }
        check(value = fastRefreshPeriod >= 0) { "Fast update refresh period must not be negative" }
        check(imageContext != null) { "ImageAppCardContext cannot be null" }

        this.apiLevel = apiLevel
        this.refreshPeriod = refreshPeriod
        this.fastRefreshPeriod = fastRefreshPeriod
        this.isInteractable = isInteractable
        imageAppCardContext = imageContext
    }

    /** Create bundle from a [AppCardContext] */
    fun toBundle(): Bundle {
        val b = Bundle()

        b.apply {
            putInt(BUNDLE_KEY_API_LEVEL, apiLevel)
            putInt(BUNDLE_KEY_REFRESH_PERIOD, refreshPeriod)
            putInt(BUNDLE_KEY_FAST_REFRESH_PERIOD, fastRefreshPeriod)
            putBoolean(BUNDLE_KEY_INTERACTABLE, isInteractable)
            putBundle(BUNDLE_KEY_IMAGE_CONTEXT, imageAppCardContext.toBundle())
        }

        return b
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true

        if (other !is AppCardContext) return false

        var result = apiLevel == other.apiLevel
        result = result and (refreshPeriod == other.refreshPeriod)
        result = result and (fastRefreshPeriod == other.fastRefreshPeriod)
        result = result and (isInteractable == other.isInteractable)
        result = result and (imageAppCardContext == other.imageAppCardContext)

        return result
    }

    override fun hashCode(): Int {
        var result = apiLevel
        result = 31 * result + refreshPeriod
        result = 31 * result + fastRefreshPeriod
        result = 31 * result + isInteractable.hashCode()
        result = 31 * result + imageAppCardContext.hashCode()
        return result
    }

    /**
     * Provides application developers with certain contextual information on how their
     * [ImageAppCard] cards are going to be displayed.
     */
    class ImageAppCardContext(
        private val mImageAppCardSize: Size,
        private val mButtonSize: Size,
        private val mHeaderSize: Size,
        /** Minimum number of buttons guaranteed to show in a row inside [ImageAppCard] */
        val minimumGuaranteedButtons: Int,
    ) {
        internal fun toBundle(): Bundle {
            val b = Bundle()

            b.apply {
                putString(BUNDLE_KEY_IMAGE_SIZE_IMAGE_APP_CARD, mImageAppCardSize.toString())
                putString(BUNDLE_KEY_IMAGE_SIZE_BUTTON, mButtonSize.toString())
                putString(BUNDLE_KEY_IMAGE_SIZE_HEADER, mHeaderSize.toString())
                putInt(BUNDLE_KEY_MIN_BUTTONS_IN_IMAGE_APP_CARD, minimumGuaranteedButtons)
            }

            return b
        }

        /**
         * Get maximum image size depending on the component it is housed in inside a [ImageAppCard]
         * such as Image, Button, etc If an object doesn't contain an
         * [com.android.car.appcard.component.Image], return `null`
         */
        fun getMaxImageSize(c: Class<*>): Size {
            return when (c) {
                Button::class.java -> mButtonSize

                ImageAppCard::class.java -> mImageAppCardSize

                Header::class.java -> mHeaderSize

                else ->
                    throw IllegalStateException(
                        "Maximum image size requested for unrecognized class"
                    )
            }
        }

        override fun equals(other: Any?): Boolean {
            if (other === this) return true

            if (other !is ImageAppCardContext) return false

            var result = mImageAppCardSize == other.mImageAppCardSize
            result = result and (mButtonSize == other.mButtonSize)
            result = result and (mHeaderSize == other.mHeaderSize)
            result = result and (minimumGuaranteedButtons == other.minimumGuaranteedButtons)

            return result
        }

        override fun hashCode(): Int {
            var result = mImageAppCardSize.hashCode()
            result = 31 * result + mButtonSize.hashCode()
            result = 31 * result + mHeaderSize.hashCode()
            result = 31 * result + minimumGuaranteedButtons
            return result
        }

        companion object {
            const val BUNDLE_KEY_IMAGE_SIZE_IMAGE_APP_CARD = "imageAppCardImageSize"
            const val BUNDLE_KEY_IMAGE_SIZE_BUTTON = "imageAppCardButtonImageSize"
            const val BUNDLE_KEY_IMAGE_SIZE_HEADER = "imageAppCardHeaderImageSize"
            const val BUNDLE_KEY_MIN_BUTTONS_IN_IMAGE_APP_CARD =
                "imageAppCardMinGuaranteedButtonsInRow"

            internal fun fromBundle(b: Bundle?): ImageAppCardContext? {
                b
                    ?: run {
                        return null
                    }

                return ImageAppCardContext(
                    Size.parseSize(b.getString(BUNDLE_KEY_IMAGE_SIZE_IMAGE_APP_CARD)),
                    Size.parseSize(b.getString(BUNDLE_KEY_IMAGE_SIZE_BUTTON)),
                    Size.parseSize(b.getString(BUNDLE_KEY_IMAGE_SIZE_HEADER)),
                    b.getInt(BUNDLE_KEY_MIN_BUTTONS_IN_IMAGE_APP_CARD),
                )
            }
        }
    }

    companion object {
        const val BUNDLE_KEY_API_LEVEL = "appCardApiLevel"
        const val BUNDLE_KEY_REFRESH_PERIOD = "appCardRefreshPeriod"
        const val BUNDLE_KEY_FAST_REFRESH_PERIOD = "appCardFastRefreshPeriod"
        const val BUNDLE_KEY_INTERACTABLE = "isAppCardInteractable"
        const val BUNDLE_KEY_IMAGE_CONTEXT = "imageAppCardContext"

        /** Create [AppCardContext] from a bundle */
        @JvmStatic
        fun fromBundle(b: Bundle?): AppCardContext? {
            b
                ?: run {
                    return null
                }

            val intDefaultValue = 0
            val apiLevel = b.getInt(BUNDLE_KEY_API_LEVEL, intDefaultValue)
            val refreshPeriodDefaultValue = -1
            val refreshPeriod = b.getInt(BUNDLE_KEY_REFRESH_PERIOD, refreshPeriodDefaultValue)
            val fastRefreshPeriod =
                b.getInt(BUNDLE_KEY_FAST_REFRESH_PERIOD, refreshPeriodDefaultValue)

            val boolDefaultValue = false
            val isInteractable = b.getBoolean(BUNDLE_KEY_INTERACTABLE, boolDefaultValue)

            val imageContextBundle = b.getBundle(BUNDLE_KEY_IMAGE_CONTEXT)

            return AppCardContext(
                apiLevel,
                refreshPeriod,
                fastRefreshPeriod,
                isInteractable,
                ImageAppCardContext.fromBundle(imageContextBundle),
            )
        }
    }
}
