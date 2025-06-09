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
package com.android.car.appcard.internal

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import com.android.car.appcard.AppCard
import com.android.car.appcard.ImageAppCard
import com.android.car.appcard.component.Button
import com.android.car.appcard.component.Component
import com.android.car.appcard.component.Header
import com.android.car.appcard.component.Image
import com.android.car.appcard.component.ProgressBar
import com.android.car.appcard.internal.proto.Button.ButtonMessage
import com.android.car.appcard.internal.proto.Header.HeaderMessage
import com.android.car.appcard.internal.proto.Image.ImageMessage
import com.android.car.appcard.internal.proto.ImageAppCard.ImageAppCardMessage
import com.android.car.appcard.internal.proto.ProgressBar.ProgressBarMessage
import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException

/** Help serializing and de-serializing [AppCard] */
class AppCardTransport : Parcelable {
    private val bytes: ByteString
    private val className: String?
    private val serializationVersion: Int

    constructor(parcel: Parcel) {
        serializationVersion = parcel.readInt()
        className = parcel.readString()

        val bytes = ByteArray(parcel.readInt())
        parcel.readByteArray(bytes)
        this.bytes = ByteString.copyFrom(bytes)
    }

    constructor(protobufBytes: ProtobufBytes) {
        serializationVersion = SUPPORTED_SERIALIZATION_VERSION
        bytes = ByteString.copyFrom(protobufBytes.toByteArray())
        className = protobufBytes.javaClass.getSimpleName()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(serializationVersion)
        dest.writeString(className)
        dest.writeInt(bytes.size())
        dest.writeByteArray(bytes.toByteArray())
    }

    @get:Throws(InvalidProtocolBufferException::class)
    val appCard: AppCard?
        /** @return decoded [AppCard] if present */
        get() =
            if (ImageAppCard::class.simpleName == className) {
                ImageAppCard.fromMessage(ImageAppCardMessage.parseFrom(bytes))
            } else {
                null
            }

    @get:Throws(InvalidProtocolBufferException::class)
    val component: Component?
        /** @return decoded [Component] if present */
        get() {
            if (Button::class.simpleName == className) {
                return Button.fromMessage(ButtonMessage.parseFrom(bytes))
            } else if (Header::class.simpleName == className) {
                return Header.fromMessage(HeaderMessage.parseFrom(bytes))
            } else if (Image::class.simpleName == className) {
                return Image.fromMessage(ImageMessage.parseFrom(bytes))
            } else if (ProgressBar::class.simpleName == className) {
                return ProgressBar.fromMessage(ProgressBarMessage.parseFrom(bytes))
            }
            return null
        }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AppCardTransport> =
            object : Parcelable.Creator<AppCardTransport> {
                override fun createFromParcel(source: Parcel): AppCardTransport =
                    AppCardTransport(source)

                override fun newArray(size: Int): Array<AppCardTransport?> = arrayOfNulls(size)
            }

        @VisibleForTesting const val SUPPORTED_SERIALIZATION_VERSION = 1
    }
}
