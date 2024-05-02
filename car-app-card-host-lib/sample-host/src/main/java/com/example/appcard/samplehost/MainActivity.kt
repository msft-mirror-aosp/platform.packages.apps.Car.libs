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

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.car.appcard.AppCardContentProvider
import com.android.car.appcard.annotations.EnforceFastUpdateRate
import com.android.car.appcard.component.Component
import com.android.car.appcard.host.AppCardHost
import com.android.car.appcard.host.AppCardListener
import com.android.car.oem.tokens.compose.compat.ui.theme.OemTokenTheme
import com.example.appcard.samplehost.ui.picker.Picker
import com.example.appcard.samplehost.ui.settings.Settings

/** Main application activity */
class MainActivity : AppCompatActivity() {
  private lateinit var viewModel: HostViewModel
  private lateinit var picker: Picker
  private lateinit var settings: Settings
  private lateinit var stateFactory: AppCardContainerStateFactory
  private lateinit var appCardHost: AppCardHost

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel = ViewModelProvider(owner = this)[HostViewModel::class.java]
    stateFactory = AppCardContainerStateFactory(applicationContext.packageManager, viewModel)
    viewModel.setStateFactory(stateFactory)

    /** Create [AppCardHost] */
    appCardHost = AppCardHost(
      /** Provide [Context] for [AppCardHost] */
      applicationContext,

      /**
       * Provide a rate at which [AppCardHost] will query [AppCardContentProvider]s
       * for updates
       */
      AppCardContextState.UPDATE_RATE_MS,

      /**
       * Provide a rate at which [AppCardContentProvider] can send [EnforceFastUpdateRate]
       * [Component] updates
       */
      AppCardContextState.FAST_UPDATE_RATE_MS,

      /** Provide a executor that [AppCardListener] results are received on */
      ContextCompat.getMainExecutor(applicationContext)
    )
    viewModel.setAppCardHost(appCardHost)

    picker = Picker(viewModel = viewModel)
    settings = Settings(viewModel)

    refreshContent()
  }

  override fun onDestroy() {
    super.onDestroy()

    viewModel.onDestroy()
  }

  override fun onStart() {
    super.onStart()

    refreshContent()
  }

  override fun onStop() {
    super.onStop()

    viewModel.onStop()
  }

  private fun refreshContent() {
    setContentView(
      ComposeView(this).apply {
        setContent {
          refreshAppCards()
          Main()
        }
      }
    )
  }

  @Composable
  private fun refreshAppCards() =
    AppCardContextState.getState().toAppCardContext().let { viewModel.refresh(it) }

  @Composable
  private fun Main() {
    OemTokenTheme {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
      ) {
        Row(
          modifier = Modifier.fillMaxSize(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier.weight(floatResource(R.dimen.picker_box_weight)),
          ) {
            picker.CreatePickerFromAllAppCards()
          }

          VerticalDivider()

          Box(
            modifier = Modifier.weight(floatResource(R.dimen.settings_box_weight)),
          ) {
            settings.CreateSettings()
          }
        }
      }
    }
  }

  companion object {
    @Composable
    private fun floatResource(resId: Int): Float = LocalContext.current.resources.getFloat(resId)
  }
}
