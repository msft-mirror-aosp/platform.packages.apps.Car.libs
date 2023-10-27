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

import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_HOST_COMPONENT_ID;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_SESSION_ID;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_TIMESTAMP;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VERSION;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.os.Bundle;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.lang.annotation.Retention;


/** Base class for Analytics events. */
public  abstract class AnalyticsEvent {


    public static final int BROWSE_LIST = 0;
    public static final int BROWSE_TABS = 1;
    public static final int QUEUE_LIST = 2;
    public static final int PLAYBACK = 3;
    public static final int MINI_PLAYBACK = 4;
    public static final int LAUNCHER = 5;
    public static final int SETTINGS_VIEW = 6;
    public static final int BROWSE_ACTION_OVERFLOW = 7;
    public static final int MEDIA_HOST = 8;
    public static final int ERROR_MESSAGE = 9;
    public static final int UNKNOWN = -1;


    @Retention(SOURCE)
    @IntDef(
            flag = true,
            value = {BROWSE_LIST, BROWSE_TABS, QUEUE_LIST, PLAYBACK, MINI_PLAYBACK, LAUNCHER,
                    SETTINGS_VIEW, BROWSE_ACTION_OVERFLOW, MEDIA_HOST, ERROR_MESSAGE,
                    UNKNOWN}
    )
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public @interface ViewComponent {}

    public static final int HIDE = 0;
    public static final int SHOW = 1;



    @Retention(SOURCE)
    @IntDef(
            flag = true,
            value = {SHOW, HIDE})
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public @interface ViewAction {}

    public static final int NONE = 0;
    public static final int SCROLL = 1;


    @Retention(SOURCE)
    @IntDef(
            flag = true,
            value = {NONE, SCROLL}
    )
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public @interface ViewActionMode {}

    public enum EventType {
        VISIBLE_ITEMS,
        MEDIA_CLICKED,
        BROWSE_NODE_CHANGED,
        VIEW_CHANGE,
        UNKNOWN,
    }

    private final float mAnalyticsVersion;
    private final int mSessionId;
    private final EventType mEventType;
    private final long mTime;
    private final String mComponent;


    public AnalyticsEvent(@NonNull Bundle eventBundle, @NonNull EventType eventType) {
        mAnalyticsVersion = eventBundle.getFloat(ANALYTICS_EVENT_DATA_KEY_VERSION, -1.0f);
        mSessionId = eventBundle.getInt(ANALYTICS_EVENT_DATA_KEY_SESSION_ID);
        mTime = eventBundle.getLong(ANALYTICS_EVENT_DATA_KEY_TIMESTAMP, -1);
        mComponent = eventBundle.getString(ANALYTICS_EVENT_DATA_KEY_HOST_COMPONENT_ID, "");
        mEventType = eventType;
    }

    public float getAnalyticsVersion() {
        return mAnalyticsVersion;
    }

    @NonNull
    public EventType getEventType() {
        return mEventType;
    }

    public long getTime() {
        return mTime;
    }

    @NonNull
    public String getComponent() {
        return mComponent;
    }

    public int getSessionId() {
        return mSessionId;
    }

    /**
     * Convert {@link ViewComponent} to human readable text.
     */
    @NonNull
    public static String viewComponentToString(@ViewComponent int viewComponent) {
        switch (viewComponent) {
            case BROWSE_ACTION_OVERFLOW:
                return "BROWSE_ACTION_OVERFLOW";
            case BROWSE_LIST:
                return "BROWSE_LIST";
            case BROWSE_TABS:
                return "BROWSE_TABS";
            case ERROR_MESSAGE:
                return "ERROR_MESSAGE";
            case LAUNCHER:
                return "LAUNCHER";
            case MEDIA_HOST:
                return "MEDIA_HOST";
            case MINI_PLAYBACK:
                return "MINI_PLAYBACK";
            case PLAYBACK:
                return "PLAYBACK";
            case QUEUE_LIST:
                return "QUEUE_LIST";
            case SETTINGS_VIEW:
                return "SETTINGS_VIEW";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Converts {@link ViewAction} to human readable text.
     */
    @NonNull
    public static String viewActionToString(@ViewAction int viewAction) {
        switch (viewAction) {
            case HIDE:
                return "HIDE";
            case SHOW:
                return "SHOW";
            default:
                return "UNKNOWN";
        }
    }

    /** converts view action flag to human readable text. */
    @NonNull
    public static String viewActionModeToString(@ViewActionMode int viewActionMode) {
        switch (viewActionMode) {
            case NONE:
                return "NONE";
            case SCROLL:
                return "SCROLL";
            default:
                return "UNKNOWN";
        }
    }

    @NonNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AnalyticsEvent{");
        sb.append("mAnalyticsVersion=").append(mAnalyticsVersion);
        sb.append(", mSessionId='").append(mSessionId).append('\'');
        sb.append(", mEventType=").append(mEventType);
        sb.append(", mTime=").append(mTime);
        sb.append(", mComponent='").append(mComponent).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
