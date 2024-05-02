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

package com.example.appcard.samplehost

import android.content.pm.PackageManager
import com.android.car.appcard.ImageAppCard
import com.android.car.appcard.host.AppCardContainer

/** A factory that creates [AppCardContainerState] */
class AppCardContainerStateFactory(
  private val packageManager: PackageManager,
  private val viewModel: HostViewModel,
) {
  fun getState(appCardContainer: AppCardContainer): AppCardContainerState? {
    if (appCardContainer.appCard is ImageAppCard) {
      return ImageAppCardContainerState(appCardContainer, packageManager, viewModel)
    }

    return null
  }
}
