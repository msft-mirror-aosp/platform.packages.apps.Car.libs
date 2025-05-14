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

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

internal class ProviderLifecycleDispatcher(provider: LifecycleOwner) {

    private val registry: LifecycleRegistry
    private val handler: Handler
    private var desiredState: Lifecycle.Event? = null

    init {
        registry = LifecycleRegistry(provider)
        handler = Handler(Looper.getMainLooper())
    }

    private fun postDispatchRunnable(event: Lifecycle.Event) {
        desiredState = event
        handler.post { registry.handleLifecycleEvent(event) }
    }

    fun queueOnCreate() = postDispatchRunnable(Lifecycle.Event.ON_CREATE)

    fun queueOnStart() = postDispatchRunnable(Lifecycle.Event.ON_START)

    fun queueOnDestroy() = postDispatchRunnable(Lifecycle.Event.ON_DESTROY)

    fun queueOnStop() = postDispatchRunnable(Lifecycle.Event.ON_STOP)

    fun queueOnPause() = postDispatchRunnable(Lifecycle.Event.ON_PAUSE)

    fun queueOnResume() = postDispatchRunnable(Lifecycle.Event.ON_RESUME)

    fun getDesiredState() = desiredState

    fun getCurrentState() = registry.currentState

    val lifecycle: Lifecycle
        get() = registry
}
