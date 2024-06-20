/*
 * Copyright 2024 The Android Open Source Project
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

package com.android.car.appcard.host

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log

internal class AppCardObserver(handler: Handler?, private val callback: AppCardObserverCallback) :
  ContentObserver(handler) {
  private val lock = Any()
  override fun onChange(selfChange: Boolean, uri: Uri?) {
    super.onChange(selfChange, uri)

    uri ?: run {
      Log.e(TAG, "Null URI not supported")
      return
    }

    val authority = uri.getAuthority() ?: run {
      Log.e(TAG, "Null authority not supported")
      return
    }

    val paths = uri.getPathSegments()
    if (paths.size != MAX_PATH_SIZE) {
      Log.e(TAG, "Path must contain only 2 segments")
      return
    }

    val id = paths[0]
    val componentId = paths[1]

    synchronized(lock) { callback.handleAppCardAppComponentRequest(authority, id, componentId) }
  }

  interface AppCardObserverCallback {
    fun handleAppCardAppComponentRequest(
      authority: String,
      id: String,
      componentId: String
    )
  }

  companion object {
    private const val TAG = "AppCardObserver"
    private const val MAX_PATH_SIZE = 2
  }
}
