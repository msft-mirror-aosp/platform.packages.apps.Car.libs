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

internal class TemperatureConverter {
  companion object {
    /**
     * Converts temperature value from Celsius to Fahrenheit
     *
     * @param temperatureInCelsius temperature value in Celsius
     * @return temperature value in Fahrenheit
     */
    fun convertCelsiusToFahrenheit(temperatureInCelsius: Float): Float {
      return temperatureInCelsius * 9f / 5f + 32
    }

    /**
     * Converts temperature value from Fahrenheit to Celsius
     *
     * @param temperatureInFahrenheit temperature value in Fahrenheit
     * @return temperature value in Celsius
     */
    fun convertFahrenheitToCelsius(temperatureInFahrenheit: Float): Float {
      return (temperatureInFahrenheit - 32) * 5f / 9f
    }
  }
}
