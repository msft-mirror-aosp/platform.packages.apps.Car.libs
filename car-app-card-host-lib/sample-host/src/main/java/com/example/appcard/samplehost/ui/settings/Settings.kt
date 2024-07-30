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

package com.example.appcard.samplehost.ui.settings

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import com.android.car.appcard.AppCardContext
import com.example.appcard.samplehost.AppCardContextState
import com.example.appcard.samplehost.HostViewModel
import com.example.appcard.samplehost.R
import kotlin.math.roundToInt

/** Settings component to change parameters of [AppCardContext] */
class Settings(
  private val viewModel: HostViewModel,
) {

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun CreateSettings() {
    val appCardContextState = viewModel.appCardContextState ?: AppCardContextState.getState()

    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = "Settings",
              style = MaterialTheme.typography.headlineMedium
            )
          },
        )
      },
      modifier = Modifier.background(MaterialTheme.colorScheme.surface),
    ) {
      Column(
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(it)
          .padding(dimensionResource(R.dimen.settings_root_column_padding))
      ) {
        SettingsSwitch(
          name = "Interaction",
          state = appCardContextState.isInteractable,
          onValueChange = {
            appCardContextState.isInteractable.value = it
            viewModel.sendContextUpdate(appCardContextState)
          },
        )

        SettingsSlider(
          name = "Maximum Buttons",
          sliderPosition = appCardContextState.minGuaranteedButtons,
          isInt = true,
          onValueChange = {
            appCardContextState.minGuaranteedButtons.value = it
            viewModel.sendContextUpdate(appCardContextState)
          },
          steps = 9,
          valueRange = 0f..10f
        )

        SettingsSlider(
          name = "Large Image Width",
          sliderPosition = appCardContextState.largeImageWidth,
          onValueChange = {
            appCardContextState.largeImageWidth.value = it
            viewModel.sendContextUpdate(appCardContextState)
          },
          steps = 989,
          valueRange = 10f..1000f
        )

        SettingsSlider(
          name = "Large Image Height",
          sliderPosition = appCardContextState.largeImageHeight,
          onValueChange = {
            appCardContextState.largeImageHeight.value = it
            viewModel.sendContextUpdate(appCardContextState)
          },
          steps = 989,
          valueRange = 10f..1000f
        )

        SettingsSlider(
          name = "Header Image Width",
          sliderPosition = appCardContextState.headerImageWidth,
          onValueChange = {
            appCardContextState.headerImageWidth.value = it
            viewModel.sendContextUpdate(appCardContextState)
          },
          steps = 89,
          valueRange = 10f..100f
        )

        SettingsSlider(
          name = "Header Image Height",
          sliderPosition = appCardContextState.headerImageHeight,
          onValueChange = {
            appCardContextState.headerImageHeight.value = it
            viewModel.sendContextUpdate(appCardContextState)
          },
          steps = 89,
          valueRange = 10f..100f
        )

        SettingsSlider(
          name = "Button Image Width",
          sliderPosition = appCardContextState.buttonImageWidth,
          onValueChange = {
            appCardContextState.buttonImageWidth.value = it
            viewModel.sendContextUpdate(appCardContextState)
          },
          steps = 89,
          valueRange = 10f..100f
        )

        SettingsSlider(
          name = "Button Image Height",
          sliderPosition = appCardContextState.buttonImageHeight,
          onValueChange = {
            appCardContextState.buttonImageHeight.value = it
            viewModel.sendContextUpdate(appCardContextState)
          },
          steps = 89,
          valueRange = 10f..100f
        )

        SettingsSwitch(
          name = "Debug Log",
          state = viewModel.isDebuggable.observeAsState(initial = true),
          onValueChange = {
            viewModel.isDebuggable.value = it
          },
        )
      }
    }
  }

  @Composable
  fun SettingsSwitch(
    name: String,
    state: State<Boolean>,
    onValueChange: (Boolean) -> Unit,
  ) {
    Surface(
      color = Color.Transparent,
      modifier = Modifier
        .fillMaxWidth()
        .padding(dimensionResource(R.dimen.settings_surface_padding)),
    ) {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = name,
              modifier = Modifier.padding(dimensionResource(R.dimen.settings_text_padding)),
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Start,
            )
          }

          Spacer(modifier = Modifier.weight(1f))

          Switch(
            checked = state.value,
            onCheckedChange = {
              onValueChange(it)
            }
          )
        }

        HorizontalDivider()
      }
    }
  }

  @Composable
  fun SettingsSlider(
    isInt: Boolean = false,
    name: String,
    sliderPosition: MutableState<Float>,
    onValueChange: (Float) -> Unit,
    @IntRange steps: Int,
    valueRange: ClosedFloatingPointRange<Float>,
  ) {
    var pos by remember { mutableFloatStateOf(sliderPosition.value) }

    Surface(
      color = Color.Transparent,
      modifier = Modifier
        .fillMaxWidth()
        .padding(dimensionResource(R.dimen.settings_surface_padding)),
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = name,
            modifier = Modifier.padding(dimensionResource(R.dimen.settings_text_padding)),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
          )
        }

        Slider(
          value = pos,
          onValueChange = {
            pos = it
          },
          onValueChangeFinished = {
            if (isInt) {
              pos = pos.roundToInt().toFloat()
            }
            onValueChange(pos)
          },
          steps = steps,
          valueRange = valueRange
        )

        Text(
          text = if (isInt) pos.roundToInt().toString() else pos.toString(),
          style = MaterialTheme.typography.bodyMedium
        )

        HorizontalDivider()
      }
    }
  }
}
