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
package com.android.car.appcard.component.interaction

import android.os.Bundle
import com.android.car.appcard.AppCardContentProvider
import com.android.car.appcard.internal.ProtobufBytes
import com.android.car.appcard.internal.proto.Intent.IntentMessage
import com.android.car.appcard.util.ParcelableUtils
import com.google.protobuf.ByteString

/**
 * An interaction that tells the system to launch an activity in [AppCardContentProvider]'s package
 */
class RoutingActivityIntent private constructor(builder: Builder) : ProtobufBytes {
  /**
   * @return this intent's class name
   */
  var cls: String
    private set

  /**
   * @return an optional [Bundle] which will be converted to the intent's extras
   */
  var bundle: Bundle?
    private set

  init {
    cls = builder.cls
    bundle = builder.bundle
  }

  /**
   * @return protobuf message
   */
  fun toMessage(): IntentMessage {
    val builder = IntentMessage.newBuilder()

    builder.setClass_(cls)

    bundle?.let {
      builder.setBundle(ByteString.copyFrom(ParcelableUtils.parcelableToBytes(it)))
    }

    return builder.build()
  }

  override fun hashCode(): Int {
    var result = cls.hashCode()
    result = 31 * result + (bundle?.hashCode() ?: 0)
    return result
  }

  /**
   * @return protobuf byte array
   */
  override fun toByteArray(): ByteArray = toMessage().toByteArray()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is RoutingActivityIntent) return false

    if (cls != other.cls) return false
    if (bundle != other.bundle) return false

    return true
  }

  /** A builder of [RoutingActivityIntent] */
  class Builder {
    internal var cls: String
    internal var bundle: Bundle? = null

    internal constructor(intentMessage: IntentMessage) {
      cls = intentMessage.class_
      if (intentMessage.hasBundle()) {
        bundle = ParcelableUtils.bytesToParcelable(
          intentMessage.bundle.toByteArray(),
          Bundle.CREATOR
        )
      }
    }

    internal constructor(cls: String) {
      this.cls = cls
      bundle = null
    }

    /**
     * Set [Bundle] for the [RoutingActivityIntent]
     *
     * This bundle's contents must be serializable
     */
    fun setBundle(bundle: Bundle): Builder {
      this.bundle = bundle
      return this
    }

    /**
     * @return [RoutingActivityIntent] built using this builder
     */
    fun build(): RoutingActivityIntent {
      return RoutingActivityIntent(builder = this)
    }
  }

  companion object {
    /**
     * @return instance of [Builder]
     */
    @JvmStatic
    fun newBuilder(cls: String) = Builder(cls)

    /**
     * @return an instance of [RoutingActivityIntent] from [IntentMessage]
     */
    @JvmStatic
    fun fromMessage(intentMessage: IntentMessage) = Builder(intentMessage).build()
  }
}
