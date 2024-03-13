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

import static androidx.car.app.mediaextensions.analytics.Constants.ACTION_ANALYTICS;
import static androidx.car.app.mediaextensions.analytics.Constants.ANALYTICS_EVENT_BUNDLE_ARRAY_KEY;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.car.app.annotations2.ExperimentalCarApi;
import androidx.car.app.mediaextensions.analytics.event.AnalyticsEvent;
import androidx.car.app.mediaextensions.analytics.event.BrowseChangeEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;


/** Creates and sends analytic events. */
@ExperimentalCarApi
public class AnalyticsManager implements IAnalyticsManager {
    public static final String TAG = "AnalyticsManager";

    private static final Map<String, Queue<Bundle>> sPackageEventQueueMap = new HashMap<>();
    private final Handler mHandler;
    private final long mBatchInterval;
    /** We don't want to come to close to binder buffer limit, so we limit queue. */
    private final int mEventBufferSize;
    private final AnalyticsEventFactory mAnalyticsFactory;
    private final WeakReference<MediaBrowserCompat> mBrowser;
    private final String mBrowserComponentString;

    /**
     *
     * @param context Context used to send analytics broadcast intent
     * @param batchInterval How long to listen for events before sending a batch of events
     * @param batchSize How many events to batch before sending a batch, interval or size,
     *                  whichever happens first.
     */
    public AnalyticsManager(@NonNull Context context, @NonNull MediaBrowserCompat browser,
            int batchInterval, int batchSize) {
        mHandler = new Handler(Looper.myLooper());
        mBatchInterval = batchInterval;
        mEventBufferSize = batchSize;
        mBrowser = new WeakReference<>(browser);
        mBrowserComponentString = browser.getServiceComponent().flattenToString();
        mAnalyticsFactory =  new AnalyticsEventFactory(context);
    }

    /**
     * Sends analytics event to browser. Build eventBundle with (@link AnalyticsManager#Builder)
     */
    private void sendAnalyticsEvent(@NonNull Bundle eventBundle) {

        //We have an event but empty queue, so we set up a window to receive a swarm before
        // sending a batch.
        if (sPackageEventQueueMap.isEmpty()) {
            mHandler.postDelayed(this::sendAllBatches, mBatchInterval);
        }

        Queue<Bundle> queue = sPackageEventQueueMap.get(mBrowserComponentString);
        if (queue == null) {
            queue = new ArrayDeque<>(mEventBufferSize);
            sPackageEventQueueMap.put(mBrowserComponentString, queue);
        }
        queue.add(eventBundle);

        //We hit queue limit, send batch now, and cancel runnable
        if (queue.size() >= mEventBufferSize) {
            sendAllBatches();
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * This will clear out all queues by sending all events to all sources.
     *
     * <p>The queue will sit empty and the handler idle until there is an event, then it will wait
     * mBatchInterval before clearing the queue. If queue is full before mBatchInterval is up,
     * we clear the queue early.
     */
    private void sendAllBatches() {
        for (String packageKey : sPackageEventQueueMap.keySet()) {
            if (sPackageEventQueueMap.get(packageKey) != null) {
                sendBatch(sPackageEventQueueMap.get(packageKey));
            } else {
                Log.w(TAG, "Event queue null with package key: " + packageKey);
            }
        }
        sPackageEventQueueMap.clear();
    }

    private void sendBatch(@NonNull Queue<Bundle> eventQueue) {
        //Get the browser service with mMediaAppComponent
        MediaBrowserCompat browser = mBrowser.get();
        if (browser == null || !browser.isConnected()){
            Log.w(TAG, "sendBatch failed, browser null or not connected.");
            return;
        }
        Bundle eventBatchBundle = createBatch(new ArrayList<>(eventQueue));
        browser.sendCustomAction(ACTION_ANALYTICS, eventBatchBundle, null);
    }

    /**
     * Packs an arrayList of AnalyticsEvent into a bundle.
     *
     * @param eventBundleList list of events each packed into a separate bundle.
     * @return bundle with list of events packed
     */
    private Bundle createBatch(@NonNull ArrayList<Bundle> eventBundleList) {
        Bundle batchBundle = new Bundle();
        batchBundle.putParcelableArrayList(ANALYTICS_EVENT_BUNDLE_ARRAY_KEY, eventBundleList);
        return batchBundle;
    }

    /** @inheritDoc */
    @Override
    public void sendBrowseChangeEvent(@BrowseChangeEvent.BrowseMode int browseMode,
            @AnalyticsEvent.ViewAction int viewAction,
            @Nullable String browseNode) {
        Bundle eventBundle = mAnalyticsFactory.createBrowseChangeEvent(browseMode, viewAction,
                browseNode);
        sendAnalyticsEvent(eventBundle);
    }

    /** @inheritDoc */
    @Override
    public void sendMediaClickedEvent(@NonNull String itemId,
            @AnalyticsEvent.ViewComponent int viewComponent) {
        Bundle eventBundle = mAnalyticsFactory.createMediaClickEvent(itemId, viewComponent);
        sendAnalyticsEvent(eventBundle);
    }

    /** @inheritDoc */
    @Override
    public void sendViewChangedEvent(@AnalyticsEvent.ViewComponent int viewComponent,
            @AnalyticsEvent.ViewAction int action) {
        Bundle eventBundle = mAnalyticsFactory.createViewEvent(viewComponent, action);
        sendAnalyticsEvent(eventBundle);
    }

    /** @inheritDoc */
    @Override
    public void sendVisibleItemsEvents(@Nullable String parentId,
            @AnalyticsEvent.ViewComponent int viewComponent,
            @AnalyticsEvent.ViewAction int viewAction,
            @AnalyticsEvent.ViewActionMode int viewActionMode,
            @Nullable List<String> listItems) {

        Bundle eventBundle = mAnalyticsFactory.createVisibleItemEvent(parentId, viewComponent,
                viewAction, viewActionMode, listItems == null ? null : new ArrayList<>(listItems));

        sendAnalyticsEvent(eventBundle);
    }

    /** @inheritDoc */
    @Override
    public void clearQueue() {
        sPackageEventQueueMap.clear();
    }

    /** @inheritDoc */
    @Override
    public void sendQueue() {
        sendAllBatches();
    }
}