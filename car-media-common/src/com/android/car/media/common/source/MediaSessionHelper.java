/*
 * Copyright 2024 The Android Open Source Project
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

import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

/**
 * Source of MediaSources that listens to {@link MediaSessionManager} media session changes.
 * <p>
 * When there are multiple media sessions, it prioritizes returning the following:
 * 1. The most recent active MediaSession
 * 2. The most recent MediaSession
 * <p>
 * For non-active MediaSessions, listeners are created to be notified if one of the others become
 * active, since playback changes don't always trigger a session change.
 */
public class MediaSessionHelper extends MediaController.Callback {

    private final MutableLiveData<MediaSource> mMediaSource = new MutableLiveData<>();
    private final MutableLiveData<List<MediaSource>> mActiveMediaSources =
            new MutableLiveData<>();
    private final MediaSessionManager mMediaSessionManager;
    private final InputFactory mInputFactory;
    private final List<MediaController> mMediaControllersList = new ArrayList<>();

    private final MediaSessionManager.OnActiveSessionsChangedListener mActiveSessionsListener =
            this::onMediaControllersChange;
    private static MediaSessionHelper sInstance;

    /** Returns the singleton. */
    public static MediaSessionHelper getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new MediaSessionHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Factory for creating dependencies. Can be swapped out for testing.
     */
    @VisibleForTesting
    interface InputFactory {

        MediaSessionManager getMediaSessionManager(Context appContext);

        MediaSource getMediaSource(MediaController mediaController);

        List<MediaSource> getMediaSources(List<MediaController> mediaControllers);
    }

    private static InputFactory createInputFactory(@NonNull Context appContext) {
        return new InputFactory() {

            @Override
            public MediaSessionManager getMediaSessionManager(Context appContext) {
                return appContext.getSystemService(MediaSessionManager.class);
            }

            @Override
            public MediaSource getMediaSource(MediaController mediaController) {
                if (mediaController == null) {
                    return null;
                }
                MediaSessionCompat.Token token =
                        MediaSessionCompat.Token.fromToken(mediaController.getSessionToken());
                MediaControllerCompat newMediaController =
                        new MediaControllerCompat(appContext, token);

                return MediaSource.create(appContext, newMediaController);
            }

            @Override
            public List<MediaSource> getMediaSources(List<MediaController> mediaControllers) {
                List<MediaSource> mediaSources = new ArrayList<>();
                for (MediaController mediaController : mediaControllers) {
                    mediaSources.add(getMediaSource(mediaController));
                }

                return mediaSources;
            }
        };
    }

    private MediaSessionHelper(@NonNull Context appContext) {
        this(appContext, createInputFactory(appContext));
    }

    private MediaSessionHelper(Context appContext, InputFactory inputFactory) {
        mInputFactory = inputFactory;
        // Register our listener to be notified of changes in the active media sessions.
        mMediaSessionManager = mInputFactory.getMediaSessionManager(appContext);
        mMediaSessionManager.addOnActiveSessionsChangedListener(mActiveSessionsListener, null);

        // Set initial value
        onMediaControllersChange(mMediaSessionManager.getActiveSessions(null));
    }

    /** Returns a filtered live data of {@link MediaSource}. */
    public LiveData<MediaSource> getMediaSource() {
        return mMediaSource;
    }

    /** Returns a filtered live data of {@link MediaController} with active playback states. */
    public LiveData<List<MediaSource>> getActiveMediaSources() {
        return mActiveMediaSources;
    }

    private void onMediaControllersChange(List<MediaController> controllers) {
        unregisterSessionCallbacks();

        List<MediaController> activeMediaControllers = getActiveMediaControllers(controllers);
        updateMediaSource(activeMediaControllers);
        updateActiveMediaControllers(activeMediaControllers);
    }

    @Nullable
    private List<MediaController> getActiveMediaControllers(List<MediaController> controllers) {
        if (controllers == null || controllers.isEmpty()) {
            return null;
        }

        List<MediaController> activeMediaControllers = new ArrayList<>();
        for (MediaController mediaController : controllers) {
            if (mediaController.getPlaybackState() == null) {
                continue;
            }

            if (isActive(mediaController.getPlaybackState().getState())) {
                activeMediaControllers.add(mediaController);
            } else {
                // Since playback state changes don't trigger an active media session change, we
                // need to listen to the other media sessions in case another one becomes active.
                registerForPlaybackChanges(mediaController);
            }
        }

        // If no active sessions, return the most recent media session.
        return activeMediaControllers;
    }

    private void registerForPlaybackChanges(MediaController controller) {
        if (mMediaControllersList.contains(controller)) {
            return;
        }

        controller.registerCallback(this);
        mMediaControllersList.add(controller);
    }

    private void unregisterSessionCallbacks() {
        for (MediaController mediaController : mMediaControllersList) {
            if (mediaController != null) {
                mediaController.unregisterCallback(this);
            }
        }
        mMediaControllersList.clear();
    }

    @Override
    public void onPlaybackStateChanged(@Nullable PlaybackState state) {
        if (state != null && isActive(state.getState())) {
            onMediaControllersChange(
                    mMediaSessionManager.getActiveSessions(/* notificationListener= */ null));
        }
    }

    private void updateMediaSource(List<MediaController> mediaControllers) {
        MediaController primaryMediaController = null;

        if (mediaControllers != null && !mediaControllers.isEmpty()) {
            primaryMediaController = mediaControllers.get(0);
        }
        mMediaSource.setValue(mInputFactory.getMediaSource(primaryMediaController));
    }

    private void updateActiveMediaControllers(List<MediaController> mediaControllers) {
        mActiveMediaSources.setValue(mInputFactory.getMediaSources(mediaControllers));
    }

    /* Copy of PlaybackState.isActive() which is only available for minsdk >=S  */
    private boolean isActive(int playbackState) {
        switch (playbackState) {
            case PlaybackState.STATE_FAST_FORWARDING:
            case PlaybackState.STATE_REWINDING:
            case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
            case PlaybackState.STATE_SKIPPING_TO_NEXT:
            case PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM:
            case PlaybackState.STATE_BUFFERING:
            case PlaybackState.STATE_CONNECTING:
            case PlaybackState.STATE_PLAYING:
                return true;
        }
        return false;
    }
}
