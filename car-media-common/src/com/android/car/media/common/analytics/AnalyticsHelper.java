/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.car.media.common.analytics;

import static com.android.car.media.common.analytics.AnalyticsFlags.ANALYTICS_ENABLED;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.car.app.mediaextensions.analytics.Constants;
import androidx.car.app.mediaextensions.analytics.host.AnalyticsManager;
import androidx.car.app.mediaextensions.analytics.host.IAnalyticsManager;

/**
 * Analytics related helper methods.
 */
public class AnalyticsHelper {

    static IAnalyticsManager sIAnalyticsManagerStub = new IAnalyticsManager() {};


    /**
     * Builds and returns analytics manager.
     * Returns stub if invalid component name or feature not enabled.
     *
     * @param context
     * @param browser browser to send analytics
     * @param rootExtras root extras returned by browser
     * @return IAnalyticsManager implementation.
     */
    public static IAnalyticsManager makeAnalyticsManager(@NonNull Context context,
            @NonNull MediaBrowserCompat browser, @NonNull Bundle rootExtras) {
        int batchinterval  = context.getResources().getInteger(
                com.android.car.media.common.R.integer.analytics_send_batch_interval);
        int batchSize = context.getResources().getInteger(
                com.android.car.media.common.R.integer.analytics_send_batch_size);
        boolean optIn = AnalyticsHelper.getOptIn(rootExtras);

        // We return unimplemented stub when analytics not enabled. This way we do not need this
        //  check at every capture point.
        if (ANALYTICS_ENABLED && optIn) {
            return new AnalyticsManager(context, browser, batchinterval, batchSize);
        } else {
            return sIAnalyticsManagerStub;
        }
    }

    private static boolean getOptIn(Bundle rootExtras) {
        return rootExtras.getBoolean(Constants.ANALYTICS_ROOT_KEY_OPT_IN, false);
    }
}
