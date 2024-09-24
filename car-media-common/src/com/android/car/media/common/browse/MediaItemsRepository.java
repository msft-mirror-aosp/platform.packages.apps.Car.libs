/*
 * Copyright (C) 2020 The Android Open Source Project
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

import static com.android.car.apps.common.util.LiveDataFunctions.dataOf;
import static com.android.car.media.common.MediaConstants.BROWSE_CUSTOM_ACTIONS_ACTION_EXTRAS;
import static com.android.car.media.common.MediaConstants.BROWSE_CUSTOM_ACTIONS_ACTION_ICON;
import static com.android.car.media.common.MediaConstants.BROWSE_CUSTOM_ACTIONS_ACTION_ID;
import static com.android.car.media.common.MediaConstants.BROWSE_CUSTOM_ACTIONS_ACTION_LABEL;
import static com.android.car.media.common.MediaConstants.BROWSE_CUSTOM_ACTIONS_ROOT_LIST;
import static com.android.car.media.common.source.MediaBrowserConnector.ConnectionStatus.CONNECTED;

import static java.util.stream.Collectors.toList;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.ItemCallback;
import android.support.v4.media.MediaBrowserCompat.SearchCallback;
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.car.app.mediaextensions.analytics.host.IAnalyticsManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.car.apps.common.util.FutureData;
import com.android.car.media.common.CustomBrowseAction;
import com.android.car.media.common.MediaItemMetadata;
import com.android.car.media.common.source.CarMediaManagerHelper;
import com.android.car.media.common.source.MediaBrowserConnector.BrowsingState;
import com.android.car.media.common.source.MediaModels;
import com.android.car.media.common.source.MediaSource;
import com.android.car.media.common.source.MediaSourceViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Fulfills media items search and children queries. The latter also provides the last list of
 * results alongside the new list so that differences can be calculated and acted upon.
 */
public class MediaItemsRepository {
    private static final String TAG = "MediaItemsRepository";

    /** One instance per MEDIA_SOURCE_MODE. */
    private static MediaItemsRepository[] sInstances = new MediaItemsRepository[2];

    /**
     * @deprecated Apps should maintain their own instance(s) of MediaItemsRepository.
     * {@link MediaModels} can help simplify this.
     */
    @Deprecated
    public static MediaItemsRepository get(@NonNull Application application, int mode) {
        if (sInstances[mode] == null) {
            String debugId = "Deprecated-" + CarMediaManagerHelper.getMode(mode) + "-AudioSource";
            sInstances[mode] = new MediaItemsRepository(application,
                    MediaSourceViewModel.get(application, mode).getBrowsingState(), debugId);
        }
        return sInstances[mode];
    }

    /** The live data providing the updates for a query. */
    public static class MediaItemsLiveData
            extends LiveData<FutureData<List<MediaItemMetadata>>> {

        private MediaItemsLiveData() {
            this(true);
        }

        private MediaItemsLiveData(boolean initAsLoading) {
            if (initAsLoading) {
                setLoading();
            } else {
                clear();
            }
        }

        private void onDataLoaded(List<MediaItemMetadata> old, List<MediaItemMetadata> list) {
            setValue(FutureData.newLoadedData(old, list));
        }

        private void setLoading() {
            setValue(FutureData.newLoadingData());
        }

        private void clear() {
            setValue(null);
        }
    }

    private static class MediaChildren {
        final String mNodeId;
        final MediaItemsLiveData mLiveData = new MediaItemsLiveData();
        List<MediaItemMetadata> mPreviousValue = Collections.emptyList();

        MediaChildren(String nodeId) {
            mNodeId = nodeId;
        }
    }

    private static class PerMediaSourceCache {
        String mRootId;
        Map<String, MediaChildren> mChildrenByNodeId = new HashMap<>();
    }

    private BrowsingState mBrowsingState;
    private final WeakReference<Context> mContext;
    private final String mDebugId;
    private final Map<MediaSource, PerMediaSourceCache> mCaches = new HashMap<>();
    private final MutableLiveData<BrowsingState> mBrowsingStateLiveData = dataOf(null);
    private final MediaItemsLiveData mRootMediaItems = new MediaItemsLiveData();
    private final MediaItemsLiveData mSearchMediaItems = new MediaItemsLiveData(/*loading*/ false);
    private final MutableLiveData<Map<String, CustomBrowseAction>> mCustomBrowseActions =
            dataOf(Collections.emptyMap());
    private final LiveData<BrowsingState> mBrowsingStateLiveDataSource;
    private final Observer<BrowsingState> mObserver = this::onMediaBrowsingStateChanged;
    private String mSearchQuery;

    public MediaItemsRepository(Context context, LiveData<BrowsingState> browsingState,
            String debugId) {
        mContext = new WeakReference<>(context);
        mDebugId = debugId + " ";
        mBrowsingStateLiveDataSource = browsingState;
        mBrowsingStateLiveDataSource.observeForever(this::onMediaBrowsingStateChanged);
    }

    /**
     * Rebroadcasts browsing state changes before the repository takes any action on them.
     */
    public LiveData<BrowsingState> getBrowsingState() {
        return mBrowsingStateLiveData;
    }

    /** returns AnalyticsManager or if no browserState returns stub */
    @NonNull
    public IAnalyticsManager getAnalyticsManager() {
        BrowsingState state = mBrowsingStateLiveData.getValue();
        if (state == null) {
            Log.i(TAG, "BrowsingState null #getAnalyticsManager() returning stub");
            return new IAnalyticsManager() {};
        }

        return state.getAnalyticsManager();
    }

    /** Returns the root id. Must be called after the source is connected. */
    public String getRootId() {
        return getCache().mRootId;
    }

    /**
     * Convenience wrapper for root media items. The live data is the same instance for all
     * media sources.
     * Note: this call doesn't fetch the items. Something else must call
     * {@link #getMediaChildren(String, Bundle)} with the result of {@link #getRootId()} once the
     * browsing state is connected.
     */
    public MediaItemsLiveData getRootMediaItems() {
        return mRootMediaItems;
    }

    /**
     * Returns the results from the current search query. The live data is the same instance
     * for all media sources.
     */
    public MediaItemsLiveData getSearchMediaItems() {
        return mSearchMediaItems;
    }

    /**
     * Returns the custom browse actions for the current browse root.
     */
    public MutableLiveData<Map<String, CustomBrowseAction>> getCustomBrowseActions() {
        return mCustomBrowseActions;
    }

    /** Returns the children of the given node. Initiates a MediaBrowserCompat query. */
    public MediaItemsLiveData getMediaChildren(String nodeId, @NonNull Bundle options) {
        PerMediaSourceCache cache = getCache();
        MediaChildren items = cache.mChildrenByNodeId.get(nodeId);
        if (items == null) {
            items = new MediaChildren(nodeId);
            cache.mChildrenByNodeId.put(nodeId, items);
        }

        // Always refresh the subscription (to work around bugs in media apps).
        if (mBrowsingState.mBrowser != null) {
            mBrowsingState.mBrowser.unsubscribe(nodeId);
            mBrowsingState.mBrowser.subscribe(nodeId, options, mBrowseCallback);
        }

        return items.mLiveData;
    }

    /** Retrieves a specific {@link MediaBrowserCompat.MediaItem} from the connected service. */
    public void getItem(@NonNull final String mediaId, @NonNull final ItemCallback cb) {
        if (mBrowsingState.mConnectionStatus == CONNECTED && mBrowsingState.mBrowser != null) {
            mBrowsingState.mBrowser.getItem(mediaId, cb);
        } else {
            Log.e(TAG, "getItem called without a connection! "
                    + mediaId + " status: " + mBrowsingState.mConnectionStatus);
            cb.onError(mediaId);
        }
    }

    /** Sets the search query. Results will be given through {@link #getSearchMediaItems}. */
    public void setSearchQuery(String query, @NonNull Bundle options) {
        mSearchQuery = query;
        if (TextUtils.isEmpty(mSearchQuery)) {
            clearSearchResults();
        } else if (mBrowsingState.mBrowser != null) {
            mSearchMediaItems.setLoading();
            mBrowsingState.mBrowser.search(mSearchQuery, options, mSearchCallback);
        }
    }

    /** Call to clear the model */
    public void onCleared() {
        mBrowsingStateLiveDataSource.removeObserver(mObserver);
    }

    private void clearSearchResults() {
        mSearchMediaItems.onDataLoaded(null, new ArrayList<>());
    }

    private MediaSource getMediaSource() {
        return (mBrowsingState != null) ? mBrowsingState.mMediaSource : null;
    }

    private void onMediaBrowsingStateChanged(BrowsingState newBrowsingState) {
        mBrowsingState = newBrowsingState;
        if (mBrowsingState == null) {
            Log.e(TAG, "Null browsing state (no media source!)");
            return;
        }
        mBrowsingStateLiveData.setValue(mBrowsingState);
        switch (mBrowsingState.mConnectionStatus) {
            case CONNECTING:
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, getLogPrefix() + "connecting");
                }
                mRootMediaItems.setLoading();
                break;
            case CONNECTED:
            case NONEXISTENT:
                MediaBrowserCompat browser = mBrowsingState.mBrowser;
                String rootId = browser == null ? null : browser.getRoot();
                getCache().mRootId = rootId;
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, getLogPrefix() + "connected. Root: " + rootId);
                }
                mCustomBrowseActions.postValue(parseBrowseActions(mBrowsingState));
                break;
            case DISCONNECTING:
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, getLogPrefix() +  "disconnecting");
                }
                unsubscribeNodes();
                clearSearchResults();
                clearNodes();
                break;
            case REJECTED:
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, getLogPrefix() + "rejected ");
                }
            case SUSPENDED:
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, getLogPrefix() + "suspended");
                }
                onBrowseData(getCache().mRootId, null);
                clearSearchResults();
                clearNodes();
        }
    }

    private PerMediaSourceCache getCache() {
        PerMediaSourceCache cache = mCaches.get(getMediaSource());
        if (cache == null) {
            cache = new PerMediaSourceCache();
            mCaches.put(getMediaSource(), cache);
        }
        return cache;
    }

    /** Does NOT clear the cache. */
    private void unsubscribeNodes() {
        if (mBrowsingState.mBrowser == null) {
            return;
        }

        PerMediaSourceCache cache = getCache();
        for (String nodeId : cache.mChildrenByNodeId.keySet()) {
            mBrowsingState.mBrowser.unsubscribe(nodeId);
        }
    }

    /** Does NOT unsubscribe nodes. */
    private void clearNodes() {
        PerMediaSourceCache cache = getCache();
        cache.mChildrenByNodeId.clear();
    }

    private void onBrowseData(@NonNull String parentId, @Nullable List<MediaItemMetadata> list) {
        PerMediaSourceCache cache = getCache();
        MediaChildren children = cache.mChildrenByNodeId.get(parentId);
        if (children == null) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Browse parent not in the cache: " + parentId);
            }
            return;
        }

        List<MediaItemMetadata> old = children.mPreviousValue;
        children.mPreviousValue = list;
        children.mLiveData.onDataLoaded(old, list);

        if (Objects.equals(parentId, cache.mRootId)) {
            mRootMediaItems.onDataLoaded(old, list);
        }
    }

    private void onSearchData(@Nullable List<MediaItemMetadata> list) {
        mSearchMediaItems.onDataLoaded(null, list);
    }

    private final SubscriptionCallback mBrowseCallback = new SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId,
                @NonNull List<MediaBrowserCompat.MediaItem> children) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, getLogPrefix() + "onChildrenLoaded " + children.size()
                        + " P: " + parentId);
            }
            onBrowseData(parentId, children.stream()
                    .filter(Objects::nonNull)
                    .map(MediaItemMetadata::new)
                    .collect(Collectors.toList()));
        }

        @Override
        public void onChildrenLoaded(@NonNull String parentId,
                @NonNull List<MediaBrowserCompat.MediaItem> children,
                @NonNull Bundle options) {
            onChildrenLoaded(parentId, children);
        }

        @Override
        public void onError(@NonNull String parentId) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, getLogPrefix() + "onError  P: " + parentId);
            }
            onBrowseData(parentId, null);
        }

        @Override
        public void onError(@NonNull String parentId, @NonNull Bundle options) {
            onError(parentId);
        }
    };

    private final SearchCallback mSearchCallback = new SearchCallback() {
        @Override
        public void onSearchResult(@NonNull String query, Bundle extras,
                @NonNull List<MediaBrowserCompat.MediaItem> items) {
            super.onSearchResult(query, extras, items);
            if (Objects.equals(mSearchQuery, query)) {
                onSearchData(items.stream()
                        .filter(Objects::nonNull)
                        .map(MediaItemMetadata::new)
                        .collect(toList()));
            }
        }

        @Override
        public void onError(@NonNull String query, Bundle extras) {
            super.onError(query, extras);
            if (Objects.equals(mSearchQuery, query)) {
                onSearchData(null);
            }
        }
    };

    private Map<String, CustomBrowseAction> parseBrowseActions(BrowsingState browsingState) {
        Map<String, CustomBrowseAction> customBrowseActions = new HashMap<>();

        if (browsingState.mBrowser == null) return customBrowseActions;
        Bundle rootExtras = browsingState.mBrowser.getExtras();
        if (rootExtras == null) return customBrowseActions;

        List<Bundle> actionBundles =
                rootExtras.getParcelableArrayList(BROWSE_CUSTOM_ACTIONS_ROOT_LIST);
        if (actionBundles == null) return customBrowseActions;

        for (Bundle actionBundle : actionBundles) {
            CustomBrowseAction customBrowseAction =
                    new CustomBrowseAction(
                            actionBundle.getString(BROWSE_CUSTOM_ACTIONS_ACTION_ID),
                            actionBundle.getString(BROWSE_CUSTOM_ACTIONS_ACTION_LABEL),
                            Uri.parse(actionBundle.getString(BROWSE_CUSTOM_ACTIONS_ACTION_ICON)),
                            actionBundle.getBundle(BROWSE_CUSTOM_ACTIONS_ACTION_EXTRAS));
            customBrowseActions.put(customBrowseAction.getId(), customBrowseAction);
        }
        return customBrowseActions;
    }

    private String getLogPrefix() {
        String result = mDebugId;
        Context context = mContext.get();
        if (context == null) {
            result += "no context ";
        } else {
            MediaSource source = getMediaSource();
            result += (source != null) ? source.getDisplayName(context).toString() : "no source";
            result += " ";
        }
        return result;
    }
}
