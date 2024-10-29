/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.car.apps.common.util;

import android.app.PendingIntent;
import android.util.Log;

import androidx.annotation.NonNull;

/** Utility class for methods and constants related to intents */
public class IntentUtils {
    private static final String TAG = "IntentUtils";

    /**
     *  Intent extra for specifying BlockingActivity's exit button visibility. Passed value should
     *  be either View.VISIBLE, View.INVISIBLE or View.GONE.
     */
    public static final String EXTRA_MEDIA_BLOCKING_ACTIVITY_EXIT_BUTTON_VISIBILITY =
            "MEDIA_BLOCKING_ACTIVITY_EXIT_BUTTON_VISIBILITY";

    private IntentUtils() {
    }

    /** Sends the intent and catches any {@link PendingIntent.CanceledException}. */
    public static void sendIntent(@NonNull PendingIntent intent) {
        try {
            intent.send();
        } catch (PendingIntent.CanceledException e) {
            if (Log.isLoggable(TAG, Log.ERROR)) {
                Log.e(TAG, "Pending intent canceled");
            }
        }
    }
}
