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

import android.util.Size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.android.car.appcard.AppCardContext

/** Jetpack compose specific stateful representation of [AppCardContext] */
class AppCardContextState(
    val refreshPeriod: MutableState<Float>,
    val fastRefreshPeriod: MutableState<Float>,
    val isInteractable: MutableState<Boolean>,
    val minGuaranteedButtons: MutableState<Float>,
    val largeImageWidth: MutableState<Float>,
    val largeImageHeight: MutableState<Float>,
    val buttonImageWidth: MutableState<Float>,
    val buttonImageHeight: MutableState<Float>,
    val headerImageWidth: MutableState<Float>,
    val headerImageHeight: MutableState<Float>,
) {
    fun toAppCardContext() =
        AppCardContext(
            APP_CARD_API_LEVEL,
            refreshPeriod.value.toInt(),
            fastRefreshPeriod.value.toInt(),
            isInteractable.value,
            Size(largeImageWidth.value.toInt(), largeImageHeight.value.toInt()),
            Size(buttonImageWidth.value.toInt(), buttonImageHeight.value.toInt()),
            Size(headerImageWidth.value.toInt(), headerImageHeight.value.toInt()),
            minGuaranteedButtons.value.toInt(),
        )

    companion object {
        private const val APP_CARD_API_LEVEL = 1
        internal const val UPDATE_RATE_MS = 5000
        internal const val FAST_UPDATE_RATE_MS = 1000

        @Composable
        private fun rememberState(
            refreshPeriod: MutableState<Float>,
            fastRefreshPeriod: MutableState<Float>,
            isInteractable: MutableState<Boolean>,
            minGuaranteedButtons: MutableState<Float>,
            largeImageWidth: MutableState<Float>,
            largeImageHeight: MutableState<Float>,
            buttonImageWidth: MutableState<Float>,
            buttonImageHeight: MutableState<Float>,
            headerImageWidth: MutableState<Float>,
            headerImageHeight: MutableState<Float>,
        ) =
            remember(
                refreshPeriod,
                fastRefreshPeriod,
                isInteractable,
                minGuaranteedButtons,
                largeImageWidth,
                largeImageHeight,
                buttonImageWidth,
                buttonImageHeight,
            ) {
                AppCardContextState(
                    refreshPeriod,
                    fastRefreshPeriod,
                    isInteractable,
                    minGuaranteedButtons,
                    largeImageWidth,
                    largeImageHeight,
                    buttonImageWidth,
                    buttonImageHeight,
                    headerImageWidth,
                    headerImageHeight,
                )
            }

        @Composable
        fun getState() =
            rememberState(
                mutableStateOf(UPDATE_RATE_MS.toFloat()),
                mutableStateOf(FAST_UPDATE_RATE_MS.toFloat()),
                mutableStateOf(value = true),
                mutableStateOf(value = floatResource(R.dimen.min_num_guaranteed_buttons)),
                mutableStateOf(value = dimensionResource(R.dimen.card_width).dpToPx()),
                mutableStateOf(value = dimensionResource(R.dimen.card_height).dpToPx()),
                mutableStateOf(value = dimensionResource(R.dimen.card_button_icon_size).dpToPx()),
                mutableStateOf(value = dimensionResource(R.dimen.card_button_icon_size).dpToPx()),
                mutableStateOf(value = dimensionResource(R.dimen.card_header_icon_size).dpToPx()),
                mutableStateOf(value = dimensionResource(R.dimen.card_header_icon_size).dpToPx()),
            )

        @Composable private fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

        @Composable
        private fun floatResource(resId: Int): Float =
            LocalContext.current.resources.getFloat(resId)
    }
}
