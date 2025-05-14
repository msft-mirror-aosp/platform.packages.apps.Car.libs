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

package com.example.appcard.sample.weather.model

data class PointsProperties(
    val id: String? = null,
    val type: String? = null,
    val forecastOffice: String? = null,
    val cwa: String? = null,
    val gridId: String? = null,
    val gridX: Int? = null,
    val gridY: Int? = null,
    val forecast: String? = null,
    val forecastHourly: String? = null,
    val forecastGridData: String? = null,
    val observationStations: String? = null,
    val relativeLocation: RelativeLocation? = null,
    val forecastZone: String? = null,
    val county: String? = null,
    val fireWeatherZone: String? = null,
    val timeZone: String? = null,
    val radarStation: String? = null,
)
