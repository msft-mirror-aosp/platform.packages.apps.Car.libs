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

package com.android.car.media.extensions.analytics.event;

import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_BROWSE_MODE;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_MEDIA_ID;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.os.Bundle;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.lang.annotation.Retention;

/** Describes a browse node change event. */
public class BrowseChangeEvent extends AnalyticsEvent {

    public static final int TREE_ROOT = 0;
    public static final int TREE_BROWSE = 1;
    public static final int TREE_TAB = 2;
    public static final int LINK = 3;
    public static final int LINK_BROWSE = 4;
    public static final int SEARCH_BROWSE = 5;
    public static final int SEARCH_RESULTS = 6;


    @Retention(SOURCE)
    @IntDef(
            flag = true,
            value = {TREE_ROOT, TREE_BROWSE, TREE_TAB, LINK, LINK_BROWSE, SEARCH_BROWSE,
                    SEARCH_RESULTS}
    )
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public @interface BrowseMode {}

    @ViewAction
    private final int mViewAction;
    @BrowseMode
    private final int mBrowseMode;
    private final String mBrowseNodeId;

    public BrowseChangeEvent(@NonNull Bundle eventBundle) {
        super(eventBundle, EventType.BROWSE_NODE_CHANGED);
        mViewAction = eventBundle.getInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION);
        mBrowseMode = eventBundle.getInt(ANALYTICS_EVENT_DATA_KEY_BROWSE_MODE);
        mBrowseNodeId = eventBundle.getString(ANALYTICS_EVENT_DATA_KEY_MEDIA_ID);
    }

    @ViewAction
    public int getViewAction() {
        return mViewAction;
    }

    @NonNull
    public String getBrowseNodeId() {
        return mBrowseNodeId;
    }

    @BrowseMode
    public int getBrowseMode() {
        return mBrowseMode;
    }

    /**Converts browse mode to human readable text */
    @NonNull
    public static String browseModeToString(@BrowseMode int browseMode) {
        switch (browseMode) {
            case BrowseChangeEvent.LINK:
                return "LINK";
            case BrowseChangeEvent.LINK_BROWSE:
                return "LINK_BROWSE";
            case BrowseChangeEvent.TREE_TAB:
                return "TREE_TAB";
            case BrowseChangeEvent.TREE_ROOT:
                return "TREE_ROOT";
            case BrowseChangeEvent.TREE_BROWSE:
                return "TREE_BROWSE";
            case BrowseChangeEvent.SEARCH_BROWSE:
                return "SEARCH_BROWSE";
            case BrowseChangeEvent.SEARCH_RESULTS:
                return "SEARCH_RESULTS";
        }

        return "" + browseMode;
    }

    @Override
    @NonNull
    public String toString() {
        final StringBuilder sb = new StringBuilder("BrowseChangeEvent{");
        sb.append("mBrowseMode=").append(browseModeToString(mBrowseMode));
        sb.append(", mViewAction=").append(viewActionToString(mViewAction));
        sb.append(", mBrowseNodeId='").append(mBrowseNodeId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
