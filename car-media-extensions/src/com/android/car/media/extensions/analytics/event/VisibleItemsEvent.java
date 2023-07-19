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

import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_ITEM_IDS;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_PARENT_NODE_ID;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION_MODE;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/** Describes a visible items event. */
public class VisibleItemsEvent extends AnalyticsEvent {

    @ViewComponent
    private final int mViewComponent;

    @ViewAction
    private final int mViewAction;

    @ViewActionMode
    private final int mViewActionMode;

    private final String mNodeId;
    private final List<String> mItemsId;

    public VisibleItemsEvent(@NonNull Bundle eventBundle) {
        super(eventBundle, EventType.VISIBLE_ITEMS);
        mViewComponent = eventBundle.getInt(ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT);
        mViewAction = eventBundle.getInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION);
        mViewActionMode = eventBundle.getInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION_MODE);
        mNodeId = eventBundle.getString(ANALYTICS_EVENT_DATA_KEY_PARENT_NODE_ID);
        mItemsId = eventBundle.getStringArrayList(ANALYTICS_EVENT_DATA_KEY_ITEM_IDS);
    }

    public int getViewComponent() {
        return mViewComponent;
    }

    public int getViewAction() {
        return mViewAction;
    }

    public int getViewActionMode() {
        return mViewActionMode;
    }

    @Nullable
    public String getNodeId() {
        return mNodeId;
    }

    @Nullable
    public List<String> getItemsId() {
        return mItemsId;
    }

    @Override
    @NonNull
    public String toString() {
        final StringBuilder sb = new StringBuilder("VisibleItemsEvent{");
        sb.append("mViewComponent=").append(viewComponentToString(mViewComponent));
        sb.append(", mViewAction=").append(viewActionToString(mViewAction));
        sb.append(", mViewActionMode=").append(viewActionModeToString(mViewActionMode));
        sb.append(", mNodeId='").append(mNodeId).append('\'');
        sb.append(", mItemsId=").append(mItemsId);
        sb.append('}');
        return sb.toString();
    }
}
