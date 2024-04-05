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

package com.android.car.media.common.source;

import static com.android.car.apps.common.util.CarAppsDebugUtils.idHash;
import static com.android.car.media.common.MediaConstants.BROWSE_CUSTOM_ACTIONS_ACTION_LIMIT;
import static com.android.car.media.common.MediaConstants.KEY_ROOT_HINT_MEDIA_SESSION_API;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.car.app.mediaextensions.analytics.host.IAnalyticsManager;
import androidx.core.util.Preconditions;
import androidx.media.utils.MediaConstants;

import com.android.car.media.common.R;
import com.android.car.media.common.analytics.AnalyticsHelper;

import java.util.Objects;

/**
 * A helper class to connect to a single {@link MediaSource} to its {@link MediaBrowserCompat}.
 * Connecting to a new one automatically disconnects the previous browser. Changes in the
 * connection status are sent via {@link MediaBrowserConnector.Callback}.
 */

public class MediaBrowserConnector {

    private static final String TAG = "MediaBrowserConnector";

    private static final Bundle sExtraRootHints = new Bundle();

    /**
     * Represents the state of the connection to the media browser service given to
     * {@link #connectTo}.
     */
    public enum ConnectionStatus {
        /**
         * The connection request to the browser is being initiated.
         * Sent from {@link #connectTo} just before calling {@link MediaBrowserCompat#connect}.
         */
        CONNECTING,
        /**
         * The connection to the browser has been established and it can be used.
         * Sent from {@link MediaBrowserCompat.ConnectionCallback#onConnected} if
         * {@link MediaBrowserCompat#isConnected} also returns true.
         */
        CONNECTED,
        /**
         * The connection to the browser was refused.
         * Sent from {@link MediaBrowserCompat.ConnectionCallback#onConnectionFailed} or from
         * {@link MediaBrowserCompat.ConnectionCallback#onConnected} if
         * {@link MediaBrowserCompat#isConnected} returns false.
         */
        REJECTED,
        /**
         * The browser crashed and that calls should NOT be made to it anymore.
         * Called from {@link MediaBrowserCompat.ConnectionCallback#onConnectionSuspended} and from
         * {@link #connectTo} when {@link MediaBrowserCompat#connect} throws
         * {@link IllegalStateException}.
         */
        SUSPENDED,
        /**
         * The connection to the browser is being closed.
         * When connecting to a new browser and the old browser is connected, this is sent
         * from {@link #connectTo} just before calling {@link MediaBrowserCompat#disconnect} on the
         * old browser.
         */
        DISCONNECTING,
        /**
         * The browser does not exist an no connection can be made
         */
        NONEXISTENT
    }

    /**
     * Encapsulates a {@link ComponentName} with its {@link MediaBrowserCompat} and the
     * {@link ConnectionStatus}.
     */
    public static class BrowsingState {
        @NonNull final Context mContext;
        @NonNull public final MediaSource mMediaSource;
        @Nullable public final MediaBrowserCompat mBrowser;
        @NonNull public final ConnectionStatus mConnectionStatus;
        @NonNull final Bundle mRootExtras = new Bundle();
        @NonNull IAnalyticsManager mAnalyticsManager;

        @VisibleForTesting
        public BrowsingState(Context context, @NonNull MediaSource mediaSource,
                @Nullable MediaBrowserCompat browser, @NonNull ConnectionStatus status) {
            mContext = context;
            mMediaSource = Preconditions.checkNotNull(mediaSource, "source can't be null");
            mBrowser = browser;
            mConnectionStatus = Preconditions.checkNotNull(status, "status can't be null");
            if (browser != null && browser.isConnected() && browser.getExtras() != null) {
                mRootExtras.putAll(browser.getExtras());
            }
            mAnalyticsManager = AnalyticsHelper.makeAnalyticsManager(mContext, browser,
                    mRootExtras);
        }

        /** Updates rootextras */
        public void updateRootExtras(@NonNull Bundle rootExtras) {
            mRootExtras.clear();
            mRootExtras.putAll(rootExtras);
            mAnalyticsManager.sendQueue();
            mAnalyticsManager = AnalyticsHelper.makeAnalyticsManager(mContext, mBrowser,
                    mRootExtras);
        }

        @NonNull
        public IAnalyticsManager getAnalyticsManager() {
            return mAnalyticsManager;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BrowsingState that = (BrowsingState) o;
            return mMediaSource.equals(that.mMediaSource)
                    && Objects.equals(mBrowser, that.mBrowser)
                    && mConnectionStatus == that.mConnectionStatus;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mMediaSource, mBrowser, mConnectionStatus);
        }
    }

    /** The callback to receive the current {@link MediaBrowserCompat} and its connection state. */
    public interface Callback {
        /** Notifies the listener of connection status changes. */
        void onBrowserConnectionChanged(@NonNull BrowsingState state);
    }

    private final Context mContext;
    private final Callback mCallback;
    private final int mMaxBitmapSizePx;

    @Nullable private MediaSource mMediaSource;
    @Nullable private MediaBrowserCompat mBrowser;

    /** Appends some root hints that will be sent to the MediaBrowserCompat. */
    public static void addRootHints(@NonNull Bundle rootHints) {
        sExtraRootHints.putAll(rootHints);
    }

    /**
     * Create a new MediaBrowserConnector.
     *
     * @param context The Context with which to build MediaBrowsers.
     */
    public MediaBrowserConnector(@NonNull Context context, @NonNull Callback callback) {
        mContext = context;
        mCallback = callback;
        mMaxBitmapSizePx = mContext.getResources().getInteger(
                com.android.car.media.common.R.integer.media_items_bitmap_max_size_px);
    }

    private String getSourcePackage() {
        if (mMediaSource == null || mMediaSource.getBrowseServiceComponentName() == null) {
            return null;
        }
        return mMediaSource.getBrowseServiceComponentName().getPackageName();
    }

    /** Counter so callbacks from obsolete connections can be ignored. */
    private int mBrowserConnectionCallbackCounter = 0;

    private class BrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        private final int mSequenceNumber = ++mBrowserConnectionCallbackCounter;
        private final String mCallbackPackage = getSourcePackage();

        private BrowserConnectionCallback() {
            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, "New Callback: " + idHash(this));
            }
        }

        private boolean isValidCall(String method) {
            if (mSequenceNumber != mBrowserConnectionCallbackCounter) {
                Log.e(TAG, "Callback: " + idHash(this) + " ignoring " + method + " for "
                        + mCallbackPackage + " seq: "
                        + mSequenceNumber + " current: " + mBrowserConnectionCallbackCounter
                        + " package: " + getSourcePackage());
                return false;
            } else if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, method + " " + getSourcePackage() + " mBrowser: " + idHash(mBrowser));
            }
            return true;
        }

        @Override
        public void onConnected() {
            if (isValidCall("onConnected")) {
                if (mBrowser != null && mBrowser.isConnected()) {
                    sendNewState(ConnectionStatus.CONNECTED);
                } else {
                    sendNewState(ConnectionStatus.REJECTED);
                }
            }
        }

        @Override
        public void onConnectionFailed() {
            if (isValidCall("onConnectionFailed")) {
                sendNewState(ConnectionStatus.REJECTED);
            }
        }

        @Override
        public void onConnectionSuspended() {
            if (isValidCall("onConnectionSuspended")) {
                sendNewState(ConnectionStatus.SUSPENDED);
            }
        }
    }

    private void sendNewState(ConnectionStatus cnx) {
        if (mMediaSource == null) {
            Log.e(TAG, "sendNewState mMediaSource is null!");
            return;
        }
        mCallback.onBrowserConnectionChanged(
                new BrowsingState(mContext, mMediaSource, mBrowser, cnx));
    }

    /** Disconnect from the {@link MediaBrowserCompat} if it was connected. */
    public void maybeDisconnect() {
        if (mBrowser != null && mBrowser.isConnected()) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Disconnecting: " + getSourcePackage()
                        + " mBrowser: " + idHash(mBrowser));
            }

            // Send queued analytic events before we disconnect.
            Bundle rootExtras =  mBrowser.getExtras() == null ? new Bundle() : mBrowser.getExtras();
            AnalyticsHelper.makeAnalyticsManager(mContext, mBrowser, rootExtras).sendQueue();

            sendNewState(ConnectionStatus.DISCONNECTING);
            mBrowser.disconnect();
        }
    }

    /**
     * Creates and connects a new {@link MediaBrowserCompat} if the given {@link MediaSource}
     * isn't null. If needed, the previous browser is disconnected.
     * @param mediaSource the media source to connect to.
     * @see MediaBrowserCompat#MediaBrowserCompat(Context, ComponentName,
     * MediaBrowserCompat.ConnectionCallback, android.os.Bundle)
     */
    public void connectTo(@Nullable MediaSource mediaSource) {
        maybeDisconnect();

        mMediaSource = mediaSource;
        if (mMediaSource == null) {
            mBrowser = null;
        } else if (mMediaSource.getBrowseServiceComponentName() != null) {
            mBrowser = createMediaBrowser(mMediaSource, new BrowserConnectionCallback());
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Connecting to: " + getSourcePackage()
                        + " mBrowser: " + idHash(mBrowser));
            }
            try {
                sendNewState(ConnectionStatus.CONNECTING);
                mBrowser.connect();
            } catch (IllegalStateException ex) {
                // Is this comment still valid ?
                // Ignore: MediaBrowse could be in an intermediate state (not connected, but not
                // disconnected either.). In this situation, trying to connect again can throw
                // this exception, but there is no way to know without trying.
                Log.e(TAG, "Connection exception: " + ex);
                sendNewState(ConnectionStatus.SUSPENDED);
            }
        } else {
            // No browse service
            mBrowser = null;
            sendNewState(ConnectionStatus.NONEXISTENT);
        }
    }

    // Override for testing.
    @NonNull
    protected MediaBrowserCompat createMediaBrowser(@NonNull MediaSource mediaSource,
            @NonNull MediaBrowserCompat.ConnectionCallback callback) {
        Bundle rootHints = new Bundle();
        rootHints.putInt(KEY_ROOT_HINT_MEDIA_SESSION_API, 1);
        rootHints.putInt(MediaConstants.BROWSER_ROOT_HINTS_KEY_MEDIA_ART_SIZE_PIXELS,
                mMaxBitmapSizePx);
        rootHints.putInt(BROWSE_CUSTOM_ACTIONS_ACTION_LIMIT,
                mContext.getResources().getInteger(R.integer.max_custom_actions));
        rootHints.putAll(sExtraRootHints);
        ComponentName browseService = mediaSource.getBrowseServiceComponentName();
        return new MediaBrowserCompat(mContext, browseService, callback, rootHints);
    }
}

