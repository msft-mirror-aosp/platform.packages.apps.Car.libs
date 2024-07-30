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

import android.os.Bundle
import com.android.car.appcard.AppCard
import com.android.car.appcard.internal.AppCardTransport

/** Communicates with an application that supports [AppCard]s */
internal interface AppCardBroker {
  /** Cleanup broker */
  fun close()

  /** Get multiple [AppCard]s using a message */
  fun getAppCardTransports(
    identifier: ApplicationIdentifier,
    bundle: Bundle,
    msg: String,
  ): List<AppCardTransport>

  /** Get a single [AppCard] using a message */
  fun getAppCardTransport(
    identifier: ApplicationIdentifier,
    errorId: String,
    bundle: Bundle,
    msg: String,
  ): AppCardTransport

  /** Send a message without expecting a response */
  fun sendMessage(msg: String, bundle: Bundle, errorId: String)
}
