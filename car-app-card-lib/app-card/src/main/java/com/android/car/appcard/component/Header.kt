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

import android.util.Log
import com.android.car.appcard.internal.proto.Header.HeaderMessage

/** Header contains a title. */
class Header private constructor(builder: Builder) : Component(builder) {
    /** @return title text */
    var title: String?
        private set

    /** @return [Image] logo */
    var logo: Image?
        private set

    init {
        title = builder.title
        logo = builder.logo
    }

    /** @return protobuf message */
    fun toMessage(): HeaderMessage {
        val builder = HeaderMessage.newBuilder()

        title?.let { builder.setTitle(title) }

        logo?.let { builder.setImage(it.toMessage()) }

        return builder.setComponentId(componentId).build()
    }

    /** @return protobuf byte array */
    override fun toByteArray(): ByteArray = toMessage().toByteArray()

    override fun updateComponent(component: Component): Boolean {
        when (component) {
            is Header -> {
                if (componentId != component.componentId) return false

                title = component.title
                logo = component.logo

                return true
            }

            is Image -> {
                try {
                    imageCheck(component)
                } catch (e: IllegalStateException) {
                    Log.d(TAG, "Failed to update image: ${e.message}")
                    return false
                }
            }
        }

        return logo?.updateComponent(component) ?: false
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true

        if (other !is Header) return false

        return super.equals(other) && title == other.title && logo == other.logo
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (logo?.hashCode() ?: 0)
        return result
    }

    /** A builder of [Header] */
    class Builder internal constructor(componentId: String) : Component.Builder(componentId) {
        internal var title: String? = null
        internal var logo: Image? = null

        internal constructor(headerMessage: HeaderMessage) : this(headerMessage.componentId) {
            if (headerMessage.hasTitle()) title = headerMessage.title

            if (headerMessage.hasImage()) {
                logo = Image.fromMessage(headerMessage.image)
                logo?.let { imageCheck(it) }
            }
        }

        /** Set title text */
        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        /** Set [Image] logo */
        fun setImage(logo: Image): Builder {
            imageCheck(logo)

            this.logo = logo
            return this
        }

        /** @return [Header] built using this builder */
        fun build(): Header = Header(builder = this)
    }

    companion object {
        private const val TAG = "Header"
        private const val IMAGE_SIZE_LIMIT = 10000

        /** @return an instance of [Builder] */
        @JvmStatic fun newBuilder(componentId: String) = Builder(componentId)

        /** @return an instance of [Header] from [HeaderMessage] */
        @JvmStatic fun fromMessage(headerMessage: HeaderMessage) = Builder(headerMessage).build()

        private fun imageCheck(logo: Image) {
            check(logo.contentScale == Image.ContentScale.FILL_BOUNDS) {
                "Image must be ContentScale.FILL_BOUNDS"
            }

            logo.imageData?.let {
                val size = it.height * it.width
                check(size <= IMAGE_SIZE_LIMIT) { "Image size must be <= $IMAGE_SIZE_LIMIT" }
            }
        }
    }
}
