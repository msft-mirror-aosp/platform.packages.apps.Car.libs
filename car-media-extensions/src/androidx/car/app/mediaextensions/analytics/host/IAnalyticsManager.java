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

package androidx.car.app.mediaextensions.analytics.host;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.car.app.annotations2.ExperimentalCarApi;
import androidx.car.app.mediaextensions.analytics.event.AnalyticsEvent;
import androidx.car.app.mediaextensions.analytics.event.BrowseChangeEvent;

import java.util.List;

/**
 * Manager for sending {@Link AnalyticsEvent}.
 */
@ExperimentalCarApi
public interface IAnalyticsManager {

    /**Send browse change analytics event. */
    default void sendBrowseChangeEvent(@BrowseChangeEvent.BrowseMode int browseMode,
            @AnalyticsEvent.ViewAction int viewAction,
            @Nullable String newNode) {}

    /**Send media item clicked analytics event. */
    default void sendMediaClickedEvent(@NonNull String itemId,
            @AnalyticsEvent.ViewComponent int viewComponent) {}

    /**
     * Send screen or view changed analytics event
     * @param viewComponent string representation of view or screen, e.g. browse, search, playback,
     *                      etc.
     * @param action 0 exit, 1 enter screen or view
     */
    default void sendViewChangedEvent(@AnalyticsEvent.ViewComponent int viewComponent,
            @AnalyticsEvent.ViewAction int action){}

    /**
     * Send visible items analytics event. Media items will be sent when visible in a list, or
     * playback. Browse actions will be visible when overflow menu is displayed.
     *
     * @param parentId ParentId of affected items.
     * @param viewComponent Component where items are changing visibility
     * @param viewAction Action, e.g. SHOW, HIDE
     * @param viewActionMode Mode for action, e.g. scroll
     * @param listItems list of visible media items ids or browse action ids.
     */
    default void sendVisibleItemsEvents(@NonNull String parentId,
            @AnalyticsEvent.ViewComponent int viewComponent,
            @AnalyticsEvent.ViewAction int viewAction,
            @AnalyticsEvent.ViewActionMode int viewActionMode,
            @Nullable List<String> listItems){}

    /**Clears queue of {@link AnalyticsEvent}s */
    default void clearQueue() {};

    /** Sends all events in queue */
    default void sendQueue() {};
}
