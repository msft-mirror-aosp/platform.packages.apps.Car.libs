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

import com.android.car.appcard.component.Component
import com.android.car.appcard.internal.ProtobufBytes

/** Base template for all App Card templates */
abstract class AppCard protected constructor(id: String) : ProtobufBytes {
  /**
   * @return application specific ID of template
   */
  val id: String

  init {
    this.id = id
  }

  override fun equals(other: Any?): Boolean {
    if (other === this) return true

    if (other !is AppCard) return false

    return id == other.id
  }

  abstract override fun toByteArray(): ByteArray

  /**
   * @return `true` if there are no conflicting component Ids inside a supported App Card
   */
  abstract fun verifyUniquenessOfComponentIds(): Boolean

  /** Update with given component only if component's ID matches a pre-existing component */
  abstract fun updateComponent(component: Component)

  override fun hashCode(): Int = id.hashCode()
}
