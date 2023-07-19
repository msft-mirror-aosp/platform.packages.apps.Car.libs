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

package com.android.car.media.extensions.analytics.client;

import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_ROOT_KEY_BROADCAST_COMPONENT_NAME;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_ROOT_KEY_PASSKEY;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_ROOT_KEY_SESSION_ID;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_SHARE_OEM_DIAGNOSTICS;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_SHARE_PLATFORM_DIAGNOSTICS;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.media.extensions.analytics.ExperimentalCarApi;


/** Utilities for building root hints */
@ExperimentalCarApi
public class RootHintsUtil {

    private RootHintsUtil() {}

    /**
     * <p>
     * Adds analytics related extras to supplied Bundle for
     * {@link MediaBrowserServiceCompat.BrowserRoot} returned in
     * {@link MediaBrowserServiceCompat#onGetRoot(String, int, Bundle)}.
     * </p>
     * <p>
     * RootExtras can be updated after
     * {@link MediaBrowserServiceCompat#onGetRoot(String, int, Bundle)} with a call to
     * {@link MediaSessionCompat#setExtras(Bundle)}.
     * </p>
     *
     * @param rootExtras Bundle to be populated with rootExtras.
     * @param analyticsOptIn boolean value indicating opt-in to receive analytics.
     * @param shareOEM boolean value indicating opt-in to share diagnostic analytics to OEM.
     * @param sharePlatform boolean value indicating opt-in to share diagnostic analytics to
     *                      platform.
     * @param receiverComponentName ComponentName of {@link BroadcastReceiver } that extends
     * {@link AnalyticsBroadcastReceiver}. This will receive analytics event.
     * @param sessionId SessionId used to identify which session generated event.
     * @return Bundle with rootExtras.
     * @see AnalyticsBroadcastReceiver
     * @see MediaSessionCompat#setExtras(Bundle)
     */
    @NonNull
    public static Bundle addAnalyticsRootExtras(@NonNull Bundle rootExtras, boolean analyticsOptIn,
            boolean shareOEM, boolean sharePlatform, @Nullable ComponentName receiverComponentName,
            int sessionId) {

        if (analyticsOptIn && receiverComponentName != null) {
            rootExtras.putString(ANALYTICS_ROOT_KEY_BROADCAST_COMPONENT_NAME,
                    receiverComponentName.flattenToString());
        } else {
            rootExtras.putString(ANALYTICS_ROOT_KEY_BROADCAST_COMPONENT_NAME, "");
        }

        rootExtras.putBoolean(ANALYTICS_SHARE_PLATFORM_DIAGNOSTICS, sharePlatform);
        rootExtras.putBoolean(ANALYTICS_SHARE_OEM_DIAGNOSTICS, shareOEM);
        rootExtras.putString(ANALYTICS_ROOT_KEY_PASSKEY,
                AnalyticsBroadcastReceiver.sAuthKey.toString());
        rootExtras.putInt(ANALYTICS_ROOT_KEY_SESSION_ID, sessionId);

        return rootExtras;
    }
}
