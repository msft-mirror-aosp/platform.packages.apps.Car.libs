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

import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT;

import android.os.Bundle;

import androidx.annotation.NonNull;

/** Describes a view entry or exit event. */
public class ViewChangeEvent extends AnalyticsEvent {

    private final @ViewComponent int mViewComponent;
    private final @ViewAction int mViewAction;

    public ViewChangeEvent(@NonNull Bundle eventBundle) {
        super(eventBundle, EventType.VIEW_CHANGE);
        mViewComponent = eventBundle.getInt(ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT);
        mViewAction = eventBundle.getInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION);
    }

    public int getViewComponent() {
        return mViewComponent;
    }

    @ViewAction
    public int getViewAction() {
        return mViewAction;
    }


    @Override
    @NonNull
    public String toString() {
        final StringBuilder sb = new StringBuilder("ViewChangeEvent{");
        sb.append("mViewComponent=").append(viewComponentToString(mViewComponent));
        sb.append(", mViewAction=").append(viewActionToString(mViewAction)).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
