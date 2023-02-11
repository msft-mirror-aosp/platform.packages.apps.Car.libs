/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.car.media.common;

/**
 * Holds constants used when dealing with MediaBrowserServices that support the
 * content style API for media.
 */
public final class MediaConstants {

    // TODO(b/222362032): Replace with androidx reference.
    public static final String KEY_SUBTITLE_LINK_MEDIA_ID =
            "androidx.car.app.mediaextensions.KEY_SUBTITLE_LINK_MEDIA_ID";

    // TODO(b/222362032): Replace with androidx reference.
    public static final String KEY_DESCRIPTION_LINK_MEDIA_ID =
            "androidx.car.app.mediaextensions.KEY_DESCRIPTION_LINK_MEDIA_ID";

    // TODO(b/222362032): Replace with androidx reference.
    public static final String
            BROWSER_SERVICE_EXTRAS_KEY_APPLICATION_PREFERENCES_USING_CAR_APP_LIBRARY_INTENT =
            "androidx.media.BrowserRoot.Extras"
                    + ".APPLICATION_PREFERENCES_USING_CAR_APP_LIBRARY_INTENT";

    // TODO(b/222362032): Replace with androidx reference.
    public static final String BROWSE_CUSTOM_ACTIONS_ROOT_LIST =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_ROOT_LIST";
    public static final String BROWSE_CUSTOM_ACTIONS_ITEM_LIST =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_ITEM_LIST";
    public static final String BROWSE_CUSTOM_ACTIONS_ACTION_ID =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_ACTION_ID";
    public static final String BROWSE_CUSTOM_ACTIONS_MEDIA_ITEM_ID =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_MEDIA_ITEM_ID";
    public static final String BROWSE_CUSTOM_ACTIONS_ACTION_LABEL =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_ACTION_LABEL";
    public static final String BROWSE_CUSTOM_ACTIONS_ACTION_ICON =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_ACTION_ICON";
    public static final String BROWSE_CUSTOM_ACTIONS_ACTION_LIMIT =
            "androidx.media.MediaBrowserCompat.BROWSE_CUSTOM_ACTIONS_ACTION_LIMIT";
    public static final String BROWSE_CUSTOM_ACTIONS_ACTION_EXTRAS =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_ACTION_EXTRAS";
    public static final String BROWSE_CUSTOM_ACTIONS_EXTRA_RESULT_BROWSE_NODE =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_EXTRA_NEW_BROWSE_NODE";
    public static final String BROWSE_CUSTOM_ACTIONS_EXTRA_RESULT_PROGRESS_UPDATE =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_EXTRA_PROGRESS_UPDATE";
    public static final String BROWSE_CUSTOM_ACTIONS_EXTRA_RESULT_OPEN_PLAYBACK =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_EXTRA_OPEN_PLAYBACK";
    public static final String BROWSE_CUSTOM_ACTIONS_EXTRA_RESULT_REFRESH_ITEM =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_EXTRA_RESULT_REFRESH_ITEM";
    public static final String BROWSE_CUSTOM_ACTIONS_EXTRA_RESULT_MESSAGE =
            "android.car.media.common.BROWSE_CUSTOM_ACTIONS_EXTRA_RESULT_MESSAGE";
}
