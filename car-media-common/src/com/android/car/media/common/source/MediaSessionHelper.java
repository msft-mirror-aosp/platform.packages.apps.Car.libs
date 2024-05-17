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
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Source of MediaSources that listens to {@link MediaSessionManager} media session changes.
 * <p>
 * This class keeps track of three different types of media sessions:
 * 1. The most important media session with an active playback state, tracked by getMediaSource().
 *    In scenarios with multiple media sessions, it prioritizes the most important returned from
 *    MediaSessionManager. If there are no active playback states, it returns the most important
 *    media session that is able to be played.
 * 2. All media sessions with an active playback state, tracked by getActiveMediaSources()
 * 3. All media session active and able to be played, tracked by getPlayableMediaSources()
 * <p>
 * For non-active MediaSessions, listeners are created to be notified if one of the others become
 * active, since playback changes don't always trigger a session change.
 */
public class MediaSessionHelper extends MediaController.Callback {

    private static final String TAG = "MediaSessionHelper";
    private final MutableLiveData<MediaSource> mPrimaryMediaSource = new MutableLiveData<>();
    private final MutableLiveData<List<MediaSource>> mActiveMediaSources =
            new MutableLiveData<>();
    private final MutableLiveData<List<MediaSource>> mPlayableMediaSources =
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

        @Nullable
        MediaSessionManager getMediaSessionManager(Context appContext);

        @Nullable
        MediaSource getMediaSource(MediaController mediaController);

        @NonNull
        List<MediaSource> getMediaSources(List<MediaController> mediaControllers);
    }

    private static InputFactory createInputFactory(@NonNull Context appContext) {
        return new InputFactory() {

            @Override
            @Nullable
            public MediaSessionManager getMediaSessionManager(Context appContext) {
                return appContext.getSystemService(MediaSessionManager.class);
            }

            @Override
            @Nullable
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
            @NonNull
            public List<MediaSource> getMediaSources(List<MediaController> mediaControllers) {
                if (mediaControllers == null) {
                    return Collections.emptyList();
                }
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
        if (mMediaSessionManager == null) {
            Log.e(TAG, "MediaSessionManager is null");
            return;
        }
        mMediaSessionManager.addOnActiveSessionsChangedListener(mActiveSessionsListener, null);

        // Set initial value
        onMediaControllersChange(mMediaSessionManager.getActiveSessions(null));
    }

    /**
     * Returns a filtered live data of {@link MediaSource}. If there is no active media session, it
     * will return the highest priority playable one.
     */
    public LiveData<MediaSource> getMediaSource() {
        return mPrimaryMediaSource;
    }

    /** Returns a filtered live data of {@link MediaSource} with active playback states. */
    public LiveData<List<MediaSource>> getActiveMediaSources() {
        return mActiveMediaSources;
    }

    /**
     *  Returns a filtered live data of {@link MediaSource} with active playback states or that can
     *  become active.
     */
    public LiveData<List<MediaSource>> getPlayableMediaSources() {
        return mPlayableMediaSources;
    }

    private void onMediaControllersChange(List<MediaController> controllers) {
        unregisterSessionCallbacks();

        List<MediaController> activeControllers = new ArrayList<>();
        List<MediaController> playableControllers = new ArrayList<>();
        parseMediaControllers(controllers, activeControllers, playableControllers);
        updatePrimaryMediaSource(activeControllers, playableControllers);
        updateActiveMediaSources(activeControllers);
        updatePlayableMediaSources(playableControllers);
    }

    /**
     * Parses MediaControllers into active and playable and appends them to the provided lists.
     *
     * @param controllers the list of MediaControllers to parse through
     * @param activeControllers the list to which active MediaControllers will be added to
     * @param playableControllers the list to which playable MediaControllers will be added to
     */
    private void parseMediaControllers(List<MediaController> controllers,
            List<MediaController> activeControllers, List<MediaController> playableControllers) {
        if (controllers == null || controllers.isEmpty()) {
            return;
        }

        for (MediaController mediaController : controllers) {
            if (mediaController.getPlaybackState() == null) {
                continue;
            }

            if (isPlayable(mediaController)) {
                playableControllers.add(mediaController);
            }

            if (isActive(mediaController.getPlaybackState().getState())) {
                activeControllers.add(mediaController);
            } else {
                // Since playback state changes don't trigger an active media session change, we
                // need to listen to the other media sessions in case another one becomes active.
                registerForPlaybackChanges(mediaController);
            }
        }
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

    /** Returns whether the MediaController is active or playable */
    private boolean isPlayable(MediaController mediaController) {
        PlaybackState playbackState = mediaController.getPlaybackState();
        if (playbackState == null) {
            return false;
        }

        return isActive(playbackState.getState())
            || (playbackState.getActions() & PlaybackStateCompat.ACTION_PLAY) != 0;
    }

    private void updatePrimaryMediaSource(List<MediaController> activeMediaControllers,
            List<MediaController> playableMediaControllers) {
        MediaController primaryMediaController = null;

        if (activeMediaControllers != null && !activeMediaControllers.isEmpty()) {
            primaryMediaController = activeMediaControllers.get(0);
        } else if (playableMediaControllers != null && !playableMediaControllers.isEmpty()) {
            primaryMediaController = playableMediaControllers.get(0);
        }

        mPrimaryMediaSource.setValue(mInputFactory.getMediaSource(primaryMediaController));
    }

    private void updateActiveMediaSources(List<MediaController> mediaControllers) {
        mActiveMediaSources.setValue(mInputFactory.getMediaSources(mediaControllers));
    }

    private void updatePlayableMediaSources(List<MediaController> mediaControllers) {
        mPlayableMediaSources.setValue(mInputFactory.getMediaSources(mediaControllers));
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
