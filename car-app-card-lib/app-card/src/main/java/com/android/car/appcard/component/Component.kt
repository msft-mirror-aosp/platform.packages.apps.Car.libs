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

import com.android.car.appcard.internal.ProtobufBytes
import java.util.Objects

/** Base class for App Card Components */
abstract class Component(builder: Builder) : ProtobufBytes {
    /** @return unique component ID within an AppCard */
    var componentId: String
        private set

    init {
        componentId = builder.componentId
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true

        if (other !is Component) return false

        return Objects.equals(componentId, other.componentId)
    }

    override fun hashCode() = componentId.hashCode()

    abstract override fun toByteArray(): ByteArray

    /**
     * @return {@code true} If the component ID of the given component matches the component ID of
     *   the current component or its child component, and the component was successfully updated
     */
    abstract fun updateComponent(component: Component): Boolean

    /** Base class for app card component builders */
    open class Builder internal constructor(internal val componentId: String)
}
