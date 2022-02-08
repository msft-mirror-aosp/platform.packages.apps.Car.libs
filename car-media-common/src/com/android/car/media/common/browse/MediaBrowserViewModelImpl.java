/*
 * Copyright 2018 The Android Open Source Project
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

package com.android.car.media.common.browse;

import static com.android.car.media.common.MediaConstants.BROWSER_SERVICE_EXTRAS_KEY_APPLICATION_PREFERENCES_USING_CAR_APP_LIBRARY_INTENT;

import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.utils.MediaConstants;

import com.android.car.media.common.MediaItemMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * TODO: rename to MediaBrowserUtils.
 * Provides utility methods for {@link MediaBrowserCompat}.
 */
public class MediaBrowserViewModelImpl {

    private static final String TAG = "MediaBrViewModelImpl";

    private MediaBrowserViewModelImpl() {
    }

    /**
     * Filters the items that are valid for the root (tabs) or the current node. Returns null when
     * the given list is null to preserve its error signal.
     */
    @Nullable
    public static List<MediaItemMetadata> filterItems(boolean forRoot,
            @Nullable List<MediaItemMetadata> items) {
        if (items == null) return null;
        Predicate<MediaItemMetadata> predicate = forRoot ? MediaItemMetadata::isBrowsable
                : item -> (item.isPlayable() || item.isBrowsable());
        return items.stream().filter(predicate).collect(Collectors.toList());
    }

    /** Returns only the browse-able items from the given list. */
    @Nullable
    public static List<MediaItemMetadata> selectBrowseableItems(
            @Nullable List<MediaItemMetadata> items) {
        if (items == null) return null;
        Predicate<MediaItemMetadata> predicate = MediaItemMetadata::isBrowsable;
        return items.stream().filter(predicate).collect(Collectors.toList());
    }


    @SuppressWarnings("deprecation")
    public static boolean getSupportsSearch(@Nullable MediaBrowserCompat mediaBrowserCompat) {
        if (mediaBrowserCompat == null) {
            return false;
        }
        Bundle extras = mediaBrowserCompat.getExtras();
        if (extras == null) {
            return false;
        }
        if (extras.containsKey(MediaConstants.BROWSER_SERVICE_EXTRAS_KEY_SEARCH_SUPPORTED)) {
            return extras.getBoolean(MediaConstants.BROWSER_SERVICE_EXTRAS_KEY_SEARCH_SUPPORTED);
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static int getRootBrowsableHint(@Nullable MediaBrowserCompat mediaBrowserCompat) {
        if (mediaBrowserCompat == null) {
            return 0;
        }
        Bundle extras = mediaBrowserCompat.getExtras();
        if (extras == null) {
            return 0;
        }
        if (extras.containsKey(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE)) {
            return extras.getInt(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE, 0);
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
    public static int getRootPlayableHint(@Nullable MediaBrowserCompat mediaBrowserCompat) {
        if (mediaBrowserCompat == null) {
            return 0;
        }
        Bundle extras = mediaBrowserCompat.getExtras();
        if (extras == null) {
            return 0;
        }
        if (extras.containsKey(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE)) {
            return extras.getInt(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE, 0);
        }
        if (extras.containsKey(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE)) {
            return extras.getInt(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE, 0);
        }
        return 0;
    }

    /** Returns the elements of oldList that do NOT appear in newList. */
    public static @NonNull Collection<MediaItemMetadata> computeRemovedItems(
            @Nullable List<MediaItemMetadata> oldList, @Nullable List<MediaItemMetadata> newList) {
        if (oldList == null || oldList.isEmpty()) {
            // Nothing was removed
            return Collections.emptyList();
        }

        if (newList == null || newList.isEmpty()) {
            // Everything was removed
            return new ArrayList<>(oldList);
        }

        HashSet<MediaItemMetadata> itemsById = new HashSet<>(oldList);
        itemsById.removeAll(newList);
        return itemsById;
    }

    /**
     * Returns the {@link PendingIntent} set in the MediaConstants.
     * BROWSER_SERVICE_EXTRAS_KEY_APPLICATION_PREFERENCES_USING_CAR_APP_LIBRARY_INTENT extra.
     */
    public static @Nullable PendingIntent getSettingsIntent(
            @Nullable MediaBrowserCompat mediaBrowserCompat) {
        if (mediaBrowserCompat == null) {
            return null;
        }
        Bundle extras = mediaBrowserCompat.getExtras();
        if (extras == null) {
            return null;
        }

        Parcelable parcelable = extras.getParcelable(
                BROWSER_SERVICE_EXTRAS_KEY_APPLICATION_PREFERENCES_USING_CAR_APP_LIBRARY_INTENT);
        if (parcelable == null) {
            return null;
        }

        if (parcelable instanceof PendingIntent) {
            return (PendingIntent) parcelable;
        } else {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Settings extra isn't a PendingIntent: " + parcelable);
            }
            return null;
        }
    }
}
