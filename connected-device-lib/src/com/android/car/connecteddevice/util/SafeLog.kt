/*
 * Copyright (C) 2019 The Android Open Source Project
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

@file:JvmName("SafeLog")
package com.android.car.connecteddevice.util

import android.util.Log

/** Log [message] if [tag] is whitelisted for `Log.VERBOSE` */
fun logv(tag: String, message: String) {
    if (Log.isLoggable(tag, Log.VERBOSE)) {
        Log.v(tag, message)
    }
}

/** Log [message] if [tag] is whitelisted for `Log.INFO` */
fun logi(tag: String, message: String) {
    if (Log.isLoggable(tag, Log.INFO)) {
        Log.i(tag, message)
    }
}

/** Log [message] if [tag] is whitelisted for `Log.DEBUG` */
fun logd(tag: String, message: String) {
    if (Log.isLoggable(tag, Log.DEBUG)) {
        Log.d(tag, message)
    }
}

/** Log [message] if [tag] is whitelisted for `Log.WARN` */
fun logw(tag: String, message: String) {
    if (Log.isLoggable(tag, Log.WARN)) {
        Log.w(tag, message)
    }
}

/** Log [message] if [tag] is whitelisted for `Log.ERROR` */
@JvmOverloads
fun loge(tag: String, message: String, throwable: Throwable? = null) {
    if (Log.isLoggable(tag, Log.ERROR)) {
        Log.e(tag, message, throwable)
    }
}
