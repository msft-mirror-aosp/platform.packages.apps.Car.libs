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

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.android.car.media.extensions.analytics.ExperimentalCarApi;
import com.android.car.media.extensions.analytics.event.BrowseChangeEvent;
import com.android.car.media.extensions.analytics.event.MediaClickedEvent;
import com.android.car.media.extensions.analytics.event.ViewChangeEvent;
import com.android.car.media.extensions.analytics.event.VisibleItemsEvent;

/**
 * Callback from {@link AnalyticsParser#parseAnalyticsBundle (Bundle, IAnalyticsCallback)}.
 **/
@ExperimentalCarApi
public interface AnalyticsCallback {

    /**
     * Callback for {@link BrowseChangeEvent} events.
     *
     * @param event
     */
    void onBrowseNodeChangeEvent(@NonNull BrowseChangeEvent event);

    /**
     * Callback for {@link MediaClickedEvent} events.
     *
     * @param event
     */
    void onMediaClickedEvent(@NonNull MediaClickedEvent event);

    /**
     * Callback for {@link ViewChangeEvent} events.
     *
     * @param event
     */
    void onViewChangeEvent(@NonNull ViewChangeEvent event);

    /**
     * Callback for {@link VisibleItemsEvent} events.
     *
     * @param event
     */
    void onVisibleItemsEvent(@NonNull VisibleItemsEvent event);

    /**
     * Callback for unknown event.
     */
    default void onUnknown(@NonNull Bundle eventBundle){};
}
