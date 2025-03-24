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

package com.example.appcard.sample.weather

import android.util.Log

class IconUriUtility {
    companion object {
        private const val TAG = "IconUriUtility"

        private fun logIfDebuggable(msg: String) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, msg)
            }
        }

        fun getRes(code: String, dayTime: Boolean): Int {
            return when (code) {
                "skc" -> {
                    logIfDebuggable("$code: Fair/clear")
                    if (dayTime) {
                        R.drawable.ic_clear_day
                    } else {
                        R.drawable.ic_clear_night
                    }
                }

                "few" -> {
                    logIfDebuggable("$code: A few clouds")
                    if (dayTime) {
                        R.drawable.ic_cloudy_day
                    } else {
                        R.drawable.ic_cloudy_night
                    }
                }

                "sct" -> {
                    logIfDebuggable("$code: Partly cloudy")
                    if (dayTime) {
                        R.drawable.ic_cloudy_day
                    } else {
                        R.drawable.ic_cloudy_night
                    }
                }

                "bkn" -> {
                    logIfDebuggable("$code: Mostly cloudy")
                    if (dayTime) {
                        R.drawable.ic_cloudy_day
                    } else {
                        R.drawable.ic_cloudy_night
                    }
                }

                "ovc" -> {
                    logIfDebuggable("$code: Overcast")
                    if (dayTime) {
                        R.drawable.ic_cloudy_day
                    } else {
                        R.drawable.ic_cloudy_night
                    }
                }

                "wind_skc" -> {
                    logIfDebuggable("$code: Fair/clear and windy")
                    if (dayTime) {
                        R.drawable.ic_clear_day
                    } else {
                        R.drawable.ic_clear_night
                    }
                }

                "wind_few" -> {
                    logIfDebuggable("$code: A few clouds and windy")
                    if (dayTime) {
                        R.drawable.ic_cloudy_day
                    } else {
                        R.drawable.ic_cloudy_night
                    }
                }

                "wind_sct" -> {
                    logIfDebuggable("$code: Partly cloudy and windy")
                    if (dayTime) {
                        R.drawable.ic_cloudy_day
                    } else {
                        R.drawable.ic_cloudy_night
                    }
                }

                "wind_bkn" -> {
                    logIfDebuggable("$code: Mostly cloudy and windy")
                    if (dayTime) {
                        R.drawable.ic_cloudy_day
                    } else {
                        R.drawable.ic_cloudy_night
                    }
                }

                "wind_ovc" -> {
                    logIfDebuggable("$code: Overcast and windy")
                    if (dayTime) {
                        R.drawable.ic_cloudy_day
                    } else {
                        R.drawable.ic_cloudy_night
                    }
                }

                "snow" -> {
                    logIfDebuggable("$code: Snow")
                    if (dayTime) {
                        R.drawable.ic_snow_day
                    } else {
                        R.drawable.ic_snow_night
                    }
                }

                "rain_snow" -> {
                    logIfDebuggable("$code: Rain/Snow")
                    R.drawable.ic_rainy_snow
                }

                "rain_sleet" -> {
                    logIfDebuggable("$code: Rain/sleet")
                    R.drawable.ic_sleet
                }

                "snow_sleet" -> {
                    logIfDebuggable("$code: Snow/sleet")
                    R.drawable.ic_sleet
                }

                "fzra" -> {
                    logIfDebuggable("$code: Freezing rain")
                    R.drawable.ic_rainy_snow
                }

                "rain_fzra" -> {
                    logIfDebuggable("$code: Rain/freezing rain")
                    R.drawable.ic_rainy_snow
                }

                "snow_fzra" -> {
                    logIfDebuggable("$code: Freezing rain/snow")
                    R.drawable.ic_rainy_snow
                }

                "sleet" -> {
                    logIfDebuggable("$code: Sleet")
                    R.drawable.ic_sleet
                }

                "rain" -> {
                    logIfDebuggable("$code: Rain")
                    R.drawable.ic_rainy
                }

                "rain_showers" -> {
                    logIfDebuggable("$code: Rain showers (high cloud cover)")
                    R.drawable.ic_rainy
                }

                "rain_showers_hi" -> {
                    logIfDebuggable("$code: Rain showers (low cloud cover)")
                    R.drawable.ic_rainy
                }

                "tsra" -> {
                    logIfDebuggable("$code: Thunderstorm (high cloud cover)")
                    R.drawable.ic_thunderstorm
                }

                "tsra_sct" -> {
                    logIfDebuggable("$code: Thunderstorm (medium cloud cover)")
                    R.drawable.ic_thunderstorm
                }

                "tsra_hi" -> {
                    logIfDebuggable("$code: Thunderstorm (low cloud cover)")
                    R.drawable.ic_thunderstorm
                }

                "tornado" -> {
                    logIfDebuggable("$code: Tornado")
                    R.drawable.ic_tornado
                }

                "hurricane" -> {
                    logIfDebuggable("$code: Hurricane conditions")
                    R.drawable.ic_cyclone
                }

                "tropical_storm" -> {
                    logIfDebuggable("$code: Tropical storm conditions")
                    R.drawable.ic_storm
                }

                "dust" -> {
                    logIfDebuggable("$code: Dust")
                    R.drawable.ic_air
                }

                "smoke" -> {
                    logIfDebuggable("$code: Smoke")
                    R.drawable.ic_storm
                }

                "haze" -> {
                    logIfDebuggable("$code: Haze")
                    R.drawable.ic_haze
                }

                "hot" -> {
                    logIfDebuggable("$code: Hot")
                    R.drawable.ic_heat
                }

                "cold" -> {
                    logIfDebuggable("$code: Cold")
                    R.drawable.ic_cold
                }

                "blizzard" -> {
                    logIfDebuggable("$code: blizzard")
                    R.drawable.ic_blizzard
                }

                "fog" -> {
                    logIfDebuggable("$code: fog/mist")
                    R.drawable.ic_foggy
                }

                else -> {
                    logIfDebuggable("Unrecognized code: $code")
                    R.drawable.ic_error
                }
            }
        }
    }
}
