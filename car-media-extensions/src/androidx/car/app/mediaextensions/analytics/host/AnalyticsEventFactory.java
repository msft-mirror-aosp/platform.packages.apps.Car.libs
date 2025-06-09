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

import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_BROWSE_NODE_CHANGE;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_BROWSE_MODE;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_EVENT_NAME;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_HOST_COMPONENT_ID;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_ITEM_IDS;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_MEDIA_ID;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_PARENT_NODE_ID;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_TIMESTAMP;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VERSION;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION_MODE;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_MEDIA_CLICKED;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_VIEW_CHANGE;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_VISIBLE_ITEMS;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_VERSION;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.car.app.mediaextensions.analytics.event.AnalyticsEvent;
import androidx.car.app.mediaextensions.analytics.event.BrowseChangeEvent;

import java.util.ArrayList;

/**
 * Provides factory for analytics events.
 *
 **/
@RestrictTo(RestrictTo.Scope.LIBRARY)
@ExperimentalCarApi
class AnalyticsEventFactory {

    @NonNull private final Bundle mBaseBundle;
    @NonNull private Bundle mBundle;

    AnalyticsEventFactory(@NonNull Context context) {
        mBaseBundle = new Bundle();
        mBundle = new Bundle();
        mBaseBundle.putString(ANALYTICS_EVENT_DATA_KEY_HOST_COMPONENT_ID,
                context.getApplicationContext().getPackageName());
    }
    /**
     * Create BrowseChangeEvent
     *
     * @param viewAction view action affecting node
     * @param browseNode  browse node that is changing
     *
     * @Hide
     */
    @NonNull
    Bundle createBrowseChangeEvent(@BrowseChangeEvent.BrowseMode int browseMode,
            @AnalyticsEvent.ViewAction int viewAction,
            @Nullable String browseNode) {
        mBundle = new Bundle(mBaseBundle);
        mBundle.putString(
                ANALYTICS_EVENT_DATA_KEY_EVENT_NAME, ANALYTICS_EVENT_BROWSE_NODE_CHANGE);
        mBundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION, viewAction);
        mBundle.putInt(ANALYTICS_EVENT_DATA_KEY_BROWSE_MODE, browseMode);
        mBundle.putString(ANALYTICS_EVENT_DATA_KEY_MEDIA_ID, browseNode);
        return create();
    }

    /**
     * Create MediaClickedEvent
     *
     * @param mediaId interacted with by user.
     *
     */
    @NonNull
    Bundle createMediaClickEvent(@NonNull String mediaId,
            @AnalyticsEvent.ViewComponent int viewComponent) {
        mBundle = new Bundle(mBaseBundle);
        mBundle.putString(ANALYTICS_EVENT_DATA_KEY_EVENT_NAME, ANALYTICS_EVENT_MEDIA_CLICKED);
        mBundle.putString(ANALYTICS_EVENT_DATA_KEY_MEDIA_ID, mediaId);
        mBundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT, viewComponent);
        return create();
    }

    /**
     * Create ViewChangeEvent with type ViewChangeEvent#SHOW
     *
     * @param viewComponent type of component visible to user.
     */
    @NonNull
    Bundle createViewEvent(@AnalyticsEvent.ViewComponent int viewComponent,
            @AnalyticsEvent.ViewAction int action) {
        mBundle = new Bundle(mBaseBundle);
        mBundle.putString(ANALYTICS_EVENT_DATA_KEY_EVENT_NAME, ANALYTICS_EVENT_VIEW_CHANGE);
        mBundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT, viewComponent);
        mBundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION, action);
        return create();
    }

    /**
     * Create VisibleItemsEvent
     *
     * @param visibleItemIds list of items visible to user
     */
    @NonNull
    Bundle createVisibleItemEvent(@Nullable  String nodeId,
            @AnalyticsEvent.ViewComponent int viewComponent,
            @AnalyticsEvent.ViewAction int viewAction,
            @AnalyticsEvent.ViewActionMode int viewActionMode,
            @Nullable ArrayList<String> visibleItemIds) {
        mBundle = new Bundle(mBaseBundle);
        mBundle.putString(ANALYTICS_EVENT_DATA_KEY_EVENT_NAME, ANALYTICS_EVENT_VISIBLE_ITEMS);
        mBundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT, viewComponent);
        mBundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION, viewAction);
        mBundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION_MODE, viewActionMode);
        mBundle.putString(ANALYTICS_EVENT_DATA_KEY_PARENT_NODE_ID, nodeId);
        mBundle.putStringArrayList(ANALYTICS_EVENT_DATA_KEY_ITEM_IDS, visibleItemIds);
        return create();
    }

    /**
     * Build event bundle with event extending AnalyticsEvent packed in.
     *
     * @return bundle with analyticsEvent packed in.
     */
    @NonNull
    private Bundle create() {
        // Use UTC - let 3P app figure out locale
        mBundle.putLong(ANALYTICS_EVENT_DATA_KEY_TIMESTAMP, System.currentTimeMillis());
        mBundle.putInt(ANALYTICS_EVENT_DATA_KEY_VERSION, ANALYTICS_VERSION);
        return mBundle;
    }
}
