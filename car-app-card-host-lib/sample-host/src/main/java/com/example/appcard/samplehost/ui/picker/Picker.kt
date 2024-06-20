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

package com.example.appcard.samplehost.ui.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.android.car.appcard.AppCard
import com.example.appcard.samplehost.HostViewModel
import com.example.appcard.samplehost.R

/** A component to show all [AppCard] */
class Picker(val viewModel: HostViewModel) {

  @Composable
  fun CreatePickerFromAllAppCards() {
    val appCardStateMap = viewModel.allAppCards
    val size = appCardStateMap.size

    if (size > 0) {
      LazyHorizontalGrid(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
          .verticalScroll(rememberScrollState())
          .height(LocalConfiguration.current.screenHeightDp.pxToDp())
          .fillMaxWidth()
          .padding(vertical = dimensionResource(R.dimen.grid_content_padding)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.Center,
        rows = GridCells.FixedSize(dimensionResource(R.dimen.card_height)),
        contentPadding = PaddingValues(
          horizontal = dimensionResource(R.dimen.grid_content_padding)
        )
      ) {
        val keyList = appCardStateMap.keys.toMutableList()

        items(
          count = keyList.size,
          key = { index ->
            keyList[index]
          },
        ) {
          appCardStateMap[keyList[it]]?.AppCard()
        }
      }
    } else {
      Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
      ) {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = NO_APP_CARDS,
          style = MaterialTheme.typography.displayLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center
        )
      }
    }
  }

  @Composable
  fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

  companion object {
    private const val NO_APP_CARDS = "No available app cards!"
  }
}
