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

import static com.android.car.apps.common.util.LiveDataFunctions.dataOf;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.car.media.common.source.MediaBrowserConnector.BrowsingState;
import com.android.car.media.common.source.MediaBrowserConnector.ConnectionStatus;

import java.util.Objects;

/**
 * Contains observable data needed for displaying playback and browse UI.
 * Each application decides which instances should be created.
 */
public class MediaSourceViewModel {
    private static final String TAG = "MediaSourceViewModel";

    /** @deprecated */
    @Deprecated
    private static CarMediaManagerHelper sCarMediaManagerHelper;
    private static MediaSourceViewModel[] sInstances = new MediaSourceViewModel[2];
    // Primary media source.
    private final MutableLiveData<MediaSource> mPrimaryMediaSource = dataOf(null);

    // Browser for the primary media source and its connection state.
    private final MutableLiveData<BrowsingState> mBrowsingState = dataOf(null);

    /**
     * Factory for creating dependencies. Can be swapped out for testing.
     */
    @VisibleForTesting
    interface InputFactory {
        MediaBrowserConnector createMediaBrowserConnector(
                @NonNull MediaBrowserConnector.Callback connectedBrowserCallback);
    }

    /**
     * @deprecated Apps should maintain their own instance(s) of MediaSourceViewModel.
     * {@link MediaModels} can help simplify this.
     */
    @Deprecated
    public static MediaSourceViewModel get(@NonNull Context context, int mode) {
        if (sInstances[mode] == null) {
            if (sCarMediaManagerHelper == null) {
                sCarMediaManagerHelper = CarMediaManagerHelper.getInstance(context);
            }
            LiveData<MediaSource> srcData = sCarMediaManagerHelper.getAudioSource(mode);
            sInstances[mode] = new MediaSourceViewModel(context, srcData);
        }
        return sInstances[mode];
    }
    private final LiveData<MediaSource> mInput;
    private final MediaBrowserConnector mBrowserConnector;
    private final MediaBrowserConnector.Callback mBrowserCallback = mBrowsingState::setValue;

    /** Creates a new model. */
    public MediaSourceViewModel(Context context, LiveData<MediaSource> source) {
        this(source, callback -> new MediaBrowserConnector(context, callback));
    }

    /** Creates a new model. */
    @VisibleForTesting
    public MediaSourceViewModel(LiveData<MediaSource> source, InputFactory factory) {
        mInput = source;
        mBrowserConnector = factory.createMediaBrowserConnector(mBrowserCallback);
        mInput.observeForever(this::updateModelState);
    }

    /** Call to clear the model. */
    public void onCleared() {
        mInput.removeObserver(this::updateModelState);
        mBrowserConnector.maybeDisconnect();
    }

    @VisibleForTesting
    MediaBrowserConnector.Callback getBrowserCallback() {
        return mBrowserCallback;
    }

    /**
     * Returns a LiveData that emits the MediaSource that is to be browsed or displayed.
     */
    public LiveData<MediaSource> getPrimaryMediaSource() {
        return mPrimaryMediaSource;
    }

    /**
     * Returns a LiveData that emits a {@link BrowsingState}, or {@code null} if there is no media
     * source.
     */
    public LiveData<BrowsingState> getBrowsingState() {
        return mBrowsingState;
    }

    private void updateModelState(MediaSource newMediaSource) {
        MediaSource oldMediaSource = mPrimaryMediaSource.getValue();
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "updateModelState from " + oldMediaSource + " to " + newMediaSource);
        }
        if (Objects.equals(oldMediaSource, newMediaSource)) {
            return;
        }

        // Broadcast the new source
        mPrimaryMediaSource.setValue(newMediaSource);

        // Recompute dependent values
        if (newMediaSource != null) {
            mBrowserConnector.connectTo(newMediaSource);
        }
    }

    /** Reconnects the current media source when it is {@link ConnectionStatus#SUSPENDED}. */
    public void maybeReconnect() {
        BrowsingState state = mBrowsingState.getValue();
        if ((state != null) && (state.mConnectionStatus == ConnectionStatus.SUSPENDED)) {
            mBrowserConnector.connectTo(state.mMediaSource);
        }
    }
}
