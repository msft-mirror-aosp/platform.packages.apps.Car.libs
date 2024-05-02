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

/** Any activity that wants to show/manage app cards must implement this listener. */
interface AppCardListener {
  /** An [AppCardContainer] has been received from an application */
  fun onAppCardReceived(appCard: AppCardContainer)

  /** An [AppCardComponentContainer] has been received from an application */
  fun onComponentReceived(component: AppCardComponentContainer)

  /**
   * A provider that supports app cards has been removed
   *
   * If authority is {@code null} then an entire package was removed
   */
  fun onProviderRemoved(packageName: String, authority: String?)

  /**
   * A provider that supports app cards has been added
   *
   * If authority is {@code null} then an entire package was added
   */
  fun onProviderAdded(packageName: String, authority: String?)

  /** There was an error when communicating with an application that supports app cards */
  fun onPackageCommunicationError(identifier: ApplicationIdentifier, throwable: Throwable)
}
