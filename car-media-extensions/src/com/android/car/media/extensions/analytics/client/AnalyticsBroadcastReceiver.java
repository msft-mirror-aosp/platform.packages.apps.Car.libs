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

import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_BUNDLE_KEY_PASSKEY;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import androidx.annotation.NonNull;

import com.android.car.media.extensions.analytics.Constants;
import com.android.car.media.extensions.analytics.ExperimentalCarApi;
import com.android.car.media.extensions.analytics.event.AnalyticsEvent;

import java.util.Objects;
import java.util.UUID;

/**
 * BroadcastReceiver that parses {@link AnalyticsEvent} from intent and hands off to
 * {@link AnalyticsCallback} .
 */
@ExperimentalCarApi
public abstract class AnalyticsBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "AnalyticsBroadcastRcvr";
    static final UUID sAuthKey = UUID.randomUUID();

    private final AnalyticsCallback mAnalyticsCallback;

    /**
     * Abstract BroadcastReceiver used to receive analytic events.
     * <p>
     *     Extend and add to manifest with {@link Constants#ANALYTICS_INTENT_ACTION}.
     * </p>
     * <p>
     *     Add analytics opt-in and sessionId to rootHints with
     *     {@link RootHintsUtil#addAnalyticsRootExtras(
     *     Bundle, boolean, boolean, boolean, ComponentName, int)}
     * </p>
     * @param analyticsCallback Callback for {@link AnalyticsEvent AnalyticEvents}.
     */
    public AnalyticsBroadcastReceiver(@NonNull AnalyticsCallback analyticsCallback) {
        super();
        this.mAnalyticsCallback = analyticsCallback;
    }

    /**
     * Receives intent with analytic events packed in arraylist of bundles.
     * <p>
     * Parses and sends to {@link AnalyticsCallback} with the result.
     * <p>
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null && isValid(sAuthKey.toString(), intent.getExtras())) {
            AnalyticsParser.parseAnalyticsIntent(intent, mAnalyticsCallback);
        } else {
            Log.w(TAG, "Invalid analytics auth key, ignoring analytics event!");
        }
    }

    /**
     * Checks if passkey in {@link AnalyticsEvent analyticsEvent} bundle is same passkey as
     * {@link AnalyticsBroadcastReceiver#sAuthKey#toString()}.
     */
    private boolean isValid(@NonNull String receiverPassKey,
            @NonNull Bundle batchBundle) {
        String bundlePassKey = batchBundle.getString(ANALYTICS_BUNDLE_KEY_PASSKEY);
        return bundlePassKey != null && Objects.equals(receiverPassKey, bundlePassKey);
    }
}
