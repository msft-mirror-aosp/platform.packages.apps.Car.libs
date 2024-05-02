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

import com.android.car.appcard.annotations.EnforceFastUpdateRate
import com.android.car.appcard.internal.proto.ProgressBar.ProgressBarMessage

/** A progress bar is a component that indicates the progress of an operation */
@EnforceFastUpdateRate
class ProgressBar private constructor(builder: Builder) : Component(builder) {
  /**
   * @return minimum value
   */
  var min: Int
    private set

  /**
   * @return maximum value
   */
  var max: Int
    private set

  /**
   * @return current progress value which will always be between maximum and minimum values
   */
  var progress: Int
    private set

  init {
    min = builder.min
    max = builder.max
    progress = builder.progress
  }

  /**
   * @return protobuf message
   */
  fun toMessage(): ProgressBarMessage = ProgressBarMessage.newBuilder()
    .setComponentId(componentId)
    .setMin(min)
    .setMax(max)
    .setProgress(progress)
    .build()

  /**
   * @return protobuf byte array
   */
  override fun toByteArray(): ByteArray = toMessage().toByteArray()

  override fun updateComponent(component: Component): Boolean {
    if (component !is ProgressBar || componentId != component.componentId) return false

    progress = component.progress
    min = component.min
    max = component.max

    return true
  }

  override fun equals(other: Any?): Boolean {
    if (other === this) return true

    if (other !is ProgressBar) return false

    return super.equals(other) &&
      progress == other.progress &&
      max == other.max &&
      min == other.min
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + min
    result = 31 * result + max
    result = 31 * result + progress
    return result
  }

  /** A builder of [ProgressBar] */
  class Builder : Component.Builder {
    internal var min: Int
    internal var max: Int
    internal var progress: Int

    internal constructor(
      progressBarMessage: ProgressBarMessage
    ) : super(progressBarMessage.componentId) {
      min = progressBarMessage.min
      max = progressBarMessage.max
      progress = progressBarMessage.progress
    }

    internal constructor(
      componentId: String,
      min: Int,
      max: Int
    ) : super(componentId) {
      this.min = min
      this.max = max
      progress = min
    }

    /** Set current progress value */
    fun setProgress(progress: Int): Builder {
      this.progress = progress
      return this
    }

    /**
     * @return [ProgressBar] built using this builder
     */
    fun build(): ProgressBar {
      check(min <= max) { "Minimum must not exceed maximum" }

      check(progress >= min) { "Progress must not be less than minimum" }

      check(max >= progress) { "Progress must not be greater than maximum" }

      return ProgressBar(builder = this)
    }
  }

  companion object {
    /**
     * @return an instance of [Builder]
     */
    @JvmStatic
    fun newBuilder(componentId: String, min: Int, max: Int) = Builder(componentId, min, max)

    /**
     * @return an instance of [ProgressBar] from [ProgressBarMessage]
     */
    @JvmStatic
    fun fromMessage(progressBarMessage: ProgressBarMessage) = Builder(progressBarMessage).build()
  }
}
