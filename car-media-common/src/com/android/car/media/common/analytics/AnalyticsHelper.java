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

package com.android.car.media.common.analytics;

import static androidx.car.app.mediaextensions.analytics.event.AnalyticsEvent.VIEW_ACTION_HIDE;
import static androidx.car.app.mediaextensions.analytics.event.AnalyticsEvent.VIEW_ACTION_MODE_NONE;
import static androidx.car.app.mediaextensions.analytics.event.AnalyticsEvent.VIEW_ACTION_MODE_SCROLL;
import static androidx.car.app.mediaextensions.analytics.event.AnalyticsEvent.VIEW_ACTION_SHOW;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import static com.android.car.media.common.analytics.AnalyticsFlags.ANALYTICS_ENABLED;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.car.app.mediaextensions.analytics.Constants;
import androidx.car.app.mediaextensions.analytics.event.AnalyticsEvent;
import androidx.car.app.mediaextensions.analytics.host.AnalyticsManager;
import androidx.car.app.mediaextensions.analytics.host.IAnalyticsManager;

import com.android.car.media.common.MediaItemMetadata;
import com.android.car.media.common.browse.MediaItemsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analytics related helper methods.
 */
@OptIn(markerClass = androidx.car.app.annotations.ExperimentalCarApi.class)
public class AnalyticsHelper {

    static IAnalyticsManager sIAnalyticsManagerStub = new IAnalyticsManager() {};


    /**
     * Builds and returns analytics manager.
     * Returns stub if invalid component name or feature not enabled.
     *
     * @param context
     * @param browser browser to send analytics
     * @param rootExtras root extras returned by browser
     * @return IAnalyticsManager implementation.
     */
    public static IAnalyticsManager makeAnalyticsManager(@NonNull Context context,
            @Nullable MediaBrowserCompat browser, @NonNull Bundle rootExtras) {
        int batchinterval  = context.getResources().getInteger(
                com.android.car.media.common.R.integer.analytics_send_batch_interval);
        int batchSize = context.getResources().getInteger(
                com.android.car.media.common.R.integer.analytics_send_batch_size);
        boolean optIn = AnalyticsHelper.getOptIn(rootExtras);

        // We return unimplemented stub when analytics not enabled. This way we do not need this
        //  check at every capture point.
        if (ANALYTICS_ENABLED && optIn && browser != null) {
            return new AnalyticsManager(context, browser, batchinterval, batchSize);
        } else {
            return sIAnalyticsManagerStub;
        }
    }

    private static boolean getOptIn(Bundle rootExtras) {
        return rootExtras.getBoolean(Constants.ANALYTICS_ROOT_KEY_OPT_IN, false);
    }

    /**
     * Creates a sends a visible items event for the items that became visible and another event
     * for the items that became hidden.
     * @return the currently visible items.
     */
    public static List<String> sendVisibleItemsInc(
            @AnalyticsEvent.ViewComponent int viewComp,
            MediaItemsRepository repo, MediaItemMetadata parentItem, List<String> prevItems,
            List<MediaItemMetadata> items, int currFirst, int currLast, boolean fromScroll) {
        return sendVisibleItemsInc(viewComp, repo, parentItem != null ? parentItem.getId() : null,
                prevItems,
                items.stream().map(MediaItemMetadata::getId).collect(Collectors.toList()),
                currFirst, currLast, fromScroll);
    }

    /**
     * Creates a sends a visible items event for the items that became visible and another event
     * for the items that became hidden.
     * @return the currently visible items.
     */
    public static List<String> sendVisibleItemsInc(
            @AnalyticsEvent.ViewComponent int viewComp,
            MediaItemsRepository repo, String parentItem, List<String> prevItems,
            List<String> items, int currFirst, int currLast, boolean fromScroll) {

        // Handle empty list by hiding previous and returning empty.
        if (items.isEmpty() && !prevItems.isEmpty()) {
            repo.getAnalyticsManager().sendVisibleItemsEvents(
                    parentItem, viewComp, VIEW_ACTION_HIDE,
                    fromScroll ? VIEW_ACTION_MODE_SCROLL : VIEW_ACTION_MODE_NONE,
                    new ArrayList<>(prevItems));
            return List.of();
        }

        // If for any reason there are no visible items or error state
        // we have nothing to show, hide prev
        if (currFirst == NO_POSITION
                || currLast == NO_POSITION
                || currLast > items.size()
                || items == null) {

            if (!prevItems.isEmpty()) {
                repo.getAnalyticsManager().sendVisibleItemsEvents(
                        parentItem, viewComp, VIEW_ACTION_HIDE,
                        fromScroll ? VIEW_ACTION_MODE_SCROLL : VIEW_ACTION_MODE_NONE,
                        new ArrayList<>(prevItems));
            }

            return List.of();
        }

        //Needed because wide search RV is sometimes given first and last swapped.
        //TODO(b/309150765): remove when fixed.
        int limitedMin = Math.min(currFirst, currLast + 1);
        int limitedMax = Math.max(currFirst, currLast + 1);

        List<String> currItemsSublist = new ArrayList<>(items
                .subList(limitedMin, Math.min(limitedMax, items.size())));

        List<String> delta = new ArrayList<>(prevItems);
        List<String> deltaNew = new ArrayList<>(currItemsSublist);
        currItemsSublist.forEach(delta::remove);
        prevItems.forEach(deltaNew::remove);

        if (!delta.isEmpty()) {
            repo.getAnalyticsManager().sendVisibleItemsEvents(
                    parentItem, viewComp, VIEW_ACTION_HIDE,
                    fromScroll ? VIEW_ACTION_MODE_SCROLL : VIEW_ACTION_MODE_NONE,
                    new ArrayList<>(delta));
        }
        if (!deltaNew.isEmpty()) {
            repo.getAnalyticsManager().sendVisibleItemsEvents(
                    parentItem, viewComp, VIEW_ACTION_SHOW,
                    fromScroll ? VIEW_ACTION_MODE_SCROLL : VIEW_ACTION_MODE_NONE,
                    new ArrayList<>(deltaNew));
        }

        return currItemsSublist;
    }
}
