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

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.car.app.mediaextensions.analytics.Constants;
import androidx.car.app.mediaextensions.analytics.host.AnalyticsManager;
import androidx.car.app.mediaextensions.analytics.host.IAnalyticsManager;

import com.android.car.media.common.source.MediaSource;

/**
 * Analytics related helper methods.
 */
public class AnalyticsHelper {

    static IAnalyticsManager sIAnalyticsManagerStub = new IAnalyticsManager() {};

    /**
     * Builds and returns analytics manager.
     * Returns stub if invalid component name or feature not enabled.
     * @param context
     * @param rootExtras
     * @return
     */
    public static IAnalyticsManager makeAnalyticsManager(@NonNull Context context,
            @NonNull MediaSource mediaSource, @NonNull Bundle rootExtras) {
        int batchinterval  = context.getResources().getInteger(
                com.android.car.media.common.R.integer.analytics_send_batch_interval);
        int batchSize = context.getResources().getInteger(
                com.android.car.media.common.R.integer.analytics_send_batch_size);
        String passkey = AnalyticsHelper.getPasskey(rootExtras);
        int sessionId = AnalyticsHelper.getSessionId(rootExtras);
        ComponentName receiverComponentName = AnalyticsHelper.getAnalyticsComponentName(rootExtras);
        String sourcePackage = mediaSource.getPackageName();

        // We return unimplemented stub when analytics not enabled. This way we do not need this
        //  check at every capture point.
        if (!ANALYTICS_ENABLED || receiverComponentName == null
                || !receiverComponentName.getPackageName().equals(sourcePackage)) {
            return sIAnalyticsManagerStub;
        } else {
            return new AnalyticsManager(context, receiverComponentName.flattenToString(), passkey,
                    sessionId, batchinterval, batchSize);
        }
    }

    /**
     * Empty string returned indicates no receiver package or invalid package.
     */
    @Nullable
    private static ComponentName getAnalyticsComponentName(@NonNull Bundle rootExtras) {
        String receiverComponentName =
                rootExtras.getString(Constants.ANALYTICS_ROOT_KEY_BROADCAST_COMPONENT_NAME);

        // Check null/empty
        if (TextUtils.isEmpty(receiverComponentName)) {
            return null;
        }

        return ComponentName.unflattenFromString(receiverComponentName);
    }

    @Nullable
    private static String getPasskey(@NonNull Bundle rootExtras) {
        return rootExtras.getString(Constants.ANALYTICS_ROOT_KEY_PASSKEY);
    }

    @Nullable
    private static int getSessionId(@NonNull Bundle rootExtras) {
        return rootExtras.getInt(Constants.ANALYTICS_ROOT_KEY_SESSION_ID);
    }
}
