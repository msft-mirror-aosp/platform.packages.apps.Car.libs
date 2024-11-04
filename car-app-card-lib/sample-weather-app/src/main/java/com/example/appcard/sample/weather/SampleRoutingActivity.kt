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

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import androidx.appcompat.app.AppCompatActivity

/** An sample routing activity that re-routes the intent sent from the app card host. */
class SampleRoutingActivity : AppCompatActivity() {
  override fun onStart() {
    super.onStart()
    val intent = Intent(ACTION_LOCATION_SOURCE_SETTINGS).apply {
      setFlags(FLAG_ACTIVITY_CLEAR_TOP)
    }
    startActivity(intent)
    finish()
  }
}
