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

import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_BROWSE_NODE_CHANGE;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_BUNDLE_ARRAY_KEY;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_EVENT_NAME;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_MEDIA_CLICKED;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_VIEW_CHANGE;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_VISIBLE_ITEMS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.car.media.extensions.analytics.ExperimentalCarApi;
import com.android.car.media.extensions.analytics.event.AnalyticsEvent;
import com.android.car.media.extensions.analytics.event.BrowseChangeEvent;
import com.android.car.media.extensions.analytics.event.MediaClickedEvent;
import com.android.car.media.extensions.analytics.event.ViewChangeEvent;
import com.android.car.media.extensions.analytics.event.VisibleItemsEvent;

import java.util.ArrayList;

/** Provides tools to parse AnalyticEvents from Bundle. **/
@ExperimentalCarApi
public class AnalyticsParser {
    public static final String TAG = "AnalyticsParser";

    private AnalyticsParser() {}

    /**
     * Parses batch of {@link AnalyticsEvent}s from intent.
     * <p>
     * Deserializes each event in batch and sends to analyticsCallback
     * @param intent intent with batch of events in extras.
     * @param analyticsCallback callback for deserialized events.
     *
     * @Hide
     */
    @SuppressLint("ExecutorRegistration")
    @SuppressWarnings("deprecation")
    public static void parseAnalyticsIntent(@NonNull Intent intent,
            @NonNull AnalyticsCallback analyticsCallback) {
        Bundle intentExtras = intent.getExtras();

        if (intentExtras.isEmpty()) {
            Log.e(TAG, "Analytics event bundle is empty.");
            return;
        }

        ArrayList<Bundle> eventBundles =
                intentExtras.getParcelableArrayList(ANALYTICS_EVENT_BUNDLE_ARRAY_KEY);

        if (eventBundles == null || eventBundles.isEmpty()) {
            Log.e(TAG, "Analytics event bundle list is empty.");
            return;
        }

        for (Bundle bundle : eventBundles) {
            AnalyticsParser.parseAnalyticsBundle(bundle, analyticsCallback);
        }
    }

    /**
     * Helper method to deserialize analytics event bundles marshalled through an intent bundle.
     * <p>
     * @param analyticsBundle Bundle with serialized analytics event
     * @param analyticsCallback Callback for deserialized analytics object.
     *
     * @Hide
     */
    @SuppressLint("ExecutorRegistration")
    public static void parseAnalyticsBundle(@NonNull Bundle analyticsBundle,
            @NonNull AnalyticsCallback analyticsCallback) {
        String eventName = analyticsBundle.getString(ANALYTICS_EVENT_DATA_KEY_EVENT_NAME, "");

        createEvent(
                analyticsCallback,
                getEventType(eventName),
                analyticsBundle);
    }

    private static void createEvent(
            @NonNull AnalyticsCallback analyticsCallback,
            AnalyticsEvent.EventType eventType,
            Bundle analyticsBundle) {

        switch (eventType) {
            case VISIBLE_ITEMS:
                analyticsCallback.onVisibleItemsEvent(new VisibleItemsEvent(analyticsBundle));
                break;
            case MEDIA_CLICKED:
                analyticsCallback.onMediaClickedEvent(new MediaClickedEvent(analyticsBundle));
                break;
            case BROWSE_NODE_CHANGED:
                analyticsCallback.onBrowseNodeChangeEvent(new BrowseChangeEvent(analyticsBundle));
                break;
            case VIEW_CHANGE:
                analyticsCallback.onViewChangeEvent(new ViewChangeEvent(analyticsBundle));
                break;
            case UNKNOWN:
                analyticsCallback.onUnknown(analyticsBundle);
                break;
        }
    }

    private static AnalyticsEvent.EventType getEventType(String eventName) {
        switch (eventName) {
            case ANALYTICS_EVENT_MEDIA_CLICKED:
                return AnalyticsEvent.EventType.MEDIA_CLICKED;
            case ANALYTICS_EVENT_BROWSE_NODE_CHANGE:
                return AnalyticsEvent.EventType.BROWSE_NODE_CHANGED;
            case ANALYTICS_EVENT_VIEW_CHANGE:
                return AnalyticsEvent.EventType.VIEW_CHANGE;
            case ANALYTICS_EVENT_VISIBLE_ITEMS:
                return AnalyticsEvent.EventType.VISIBLE_ITEMS;
            default:
                return AnalyticsEvent.EventType.UNKNOWN;
        }
    }
}

