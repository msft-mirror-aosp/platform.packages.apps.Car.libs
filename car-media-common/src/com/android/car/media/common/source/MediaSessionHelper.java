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

import static android.car.media.CarMediaManager.MEDIA_SOURCE_MODE_PLAYBACK;

import android.app.Notification;
import android.car.Car;
import android.car.media.CarMediaManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.service.notification.StatusBarNotification;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
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
 * This class keeps track of two different types of media sessions:
 * 1. The most important media session with an active playback state, tracked by getMediaSource().
 *    In scenarios with multiple media sessions, it prioritizes the most important returned from
 *    MediaSessionManager.
 * 2. All media sessions with an active or paused playback state, tracked by
 *    getActiveOrPausedMediaSources()
 * <p>
 *
 * Apps may not want their media sessions to be externally controlled, which may include legal
 * reasons, and instead rely on the creation of a media notification to give users the ability to
 * control the session when the app is backgrounded. For this reason callers must provide access to
 * notifications for MediaSessionHelper to filter the media sessions.
 *
 * For non-active MediaSessions, listeners are created to be notified if one of the others become
 * active, since playback changes don't always trigger a session change.
 */
public class MediaSessionHelper extends MediaController.Callback {

    private static final String TAG = "MediaSessionHelper";
    private static final String SHARED_PREF = MediaSessionHelper.class.getCanonicalName();
    private static final String LAST_ACTIVE_MEDIA_SOURCE = "last_active_media_source";
    private final Context mContext;
    private final MutableLiveData<MediaSource> mPrimaryMediaSource = new MutableLiveData<>(null);
    private final MutableLiveData<List<MediaSource>> mActiveOrPausedMediaSources =
            new MutableLiveData<>(Collections.emptyList());
    private final MediaSessionManager mMediaSessionManager;
    private final InputFactory mInputFactory;
    private final List<MediaController> mMediaControllersList = new ArrayList<>();
    private final SharedPreferences mSharedPrefs;
    @Nullable
    private final NotificationProvider mNotificationProvider;

    private final MediaSessionManager.OnActiveSessionsChangedListener mActiveSessionsListener =
            this::onMediaControllersChange;
    private static MediaSessionHelper sInstance;

    /**
     *  Returns the singleton.
     * @deprecated Use {@link #getInstance(Context, NotificationProvider)} instead
     */
    @Deprecated
    public static MediaSessionHelper getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new MediaSessionHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /** Returns the singleton. */
    public static MediaSessionHelper getInstance(@NonNull Context context,
            @NonNull NotificationProvider notificationProvider) {
        if (sInstance == null) {
            sInstance =
                    new MediaSessionHelper(context.getApplicationContext(), notificationProvider);
        }
        return sInstance;
    }

    /** Class that provides a list of active notifications. */
    public interface NotificationProvider {

        /** Returns a list of currently active notifications */
        StatusBarNotification[] getActiveNotifications();

        /** Returns whether the notification is a media notification or not */
        boolean isMediaNotification(Notification notification);
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

    @Deprecated
    private MediaSessionHelper(@NonNull Context appContext) {
        this(appContext, null, createInputFactory(appContext));
    }

    private MediaSessionHelper(@NonNull Context appContext,
            @NonNull NotificationProvider notificationProvider) {
        this(appContext, notificationProvider, createInputFactory(appContext));
    }

    private MediaSessionHelper(Context appContext, NotificationProvider notificationProvider,
            InputFactory inputFactory) {
        mContext = appContext;
        mNotificationProvider = notificationProvider;
        mInputFactory = inputFactory;
        mSharedPrefs = appContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        // Register our listener to be notified of changes in the active media sessions.
        mMediaSessionManager = mInputFactory.getMediaSessionManager(appContext);
        if (mMediaSessionManager == null) {
            Log.e(TAG, "MediaSessionManager is null");
            return;
        }
        mMediaSessionManager.addOnActiveSessionsChangedListener(mActiveSessionsListener, null);

        // Set initial value
        setInitialMediaSource();
    }

    /**
     * Returns a filtered live data of the most important active media session {@link MediaSource}.
     */
    public LiveData<MediaSource> getMediaSource() {
        return mPrimaryMediaSource;
    }

    /**
     *  Returns a filtered live data of {@link MediaSource} with active or paused playback states.
     */
    public LiveData<List<MediaSource>> getActiveOrPausedMediaSources() {
        return mActiveOrPausedMediaSources;
    }

    private void onMediaControllersChange(List<MediaController> controllers) {
        unregisterSessionCallbacks();

        List<MediaController> filteredControllers =
                getMediaControllersWithMediaNotifications(controllers);

        List<MediaController> activeControllers = new ArrayList<>();
        List<MediaController> activeOrPausedControllers = new ArrayList<>();
        parseMediaControllers(filteredControllers, activeControllers, activeOrPausedControllers);
        updatePrimaryMediaSource(activeControllers);
        updateActiveOrPausedMediaSources(activeOrPausedControllers);
    }

    private List<MediaController> getMediaControllersWithMediaNotifications(
            List<MediaController> controllers) {
        List<MediaController> mediaNotificationMediaControllers = new ArrayList<>();

        // TODO: cleanup once non-notificationprovider constructor is removed
        if (mNotificationProvider == null) {
            return controllers;
        }

        List<String> mediaNotificationPackageNames =
                getMediaNotificationPackages(mNotificationProvider.getActiveNotifications());

        for (MediaController mediaController : controllers) {
            MediaSource mediaSource = mInputFactory.getMediaSource(mediaController);
            // We only want media sources with MBS or have a media notification indicating they want
            // to allow background playback.
            if (mediaSource != null && (mediaSource.getBrowseServiceComponentName() != null
                    || mediaNotificationPackageNames.contains(mediaController.getPackageName()))) {
                mediaNotificationMediaControllers.add(mediaController);
            }
        }

        return mediaNotificationMediaControllers;
    }

    /**
     * Parses MediaControllers and returns the active ones. Registers playback state listeners for
     * the non active ones to be notified if they become active.
     *
     * @param controllers the list of MediaControllers to parse through
     * @param activeControllers the list to which active MediaControllers will be added to
     * @param activeOrPausedControllers the list to which active or paused MediaControllers will be
     * added to
     */
    private void parseMediaControllers(List<MediaController> controllers,
            List<MediaController> activeControllers,
            List<MediaController> activeOrPausedControllers) {
        if (controllers == null || controllers.isEmpty()) {
            return;
        }

        for (MediaController mediaController : controllers) {
            PlaybackState playbackState = mediaController.getPlaybackState();
            if (playbackState == null) {
                continue;
            }

            if (isActive(playbackState.getState())) {
                activeControllers.add(mediaController);
                activeOrPausedControllers.add(mediaController);
            } else if (isPaused(playbackState.getState())) {
                activeOrPausedControllers.add(mediaController);
            }

            // Since playback state changes don't trigger an active media session change, we
            // need to listen to the other media sessions in case another one becomes active.
            registerForPlaybackChanges(mediaController);
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
        if (state != null && isPausedOrActive(state.getState())) {
            onMediaControllersChange(
                    mMediaSessionManager.getActiveSessions(/* notificationListener= */ null));
        }
    }

    private void updatePrimaryMediaSource(List<MediaController> activeMediaControllers) {
        // Only update when there are active media sources
        if (activeMediaControllers != null && !activeMediaControllers.isEmpty()) {
            MediaController primaryMediaController = activeMediaControllers.get(0);
            MediaSource mediaSource = mInputFactory.getMediaSource(primaryMediaController);
            saveLastActiveMediaSource(mediaSource);
            mPrimaryMediaSource.setValue(mediaSource);
        }
    }

    private void updateActiveOrPausedMediaSources(List<MediaController> activeMediaControllers) {
        // Only update when there are active or paused media sources
        if (activeMediaControllers != null && !activeMediaControllers.isEmpty()) {
            mActiveOrPausedMediaSources
                .setValue(mInputFactory.getMediaSources(activeMediaControllers));
        }
    }

    private void setInitialMediaSource() {
        List<MediaController> activeMediaControllers = new ArrayList<>();
        List<MediaController> activeOrPausedMediaControllers = new ArrayList<>();
        parseMediaControllers(mMediaSessionManager.getActiveSessions(null),
                activeMediaControllers, activeOrPausedMediaControllers);
        MediaSource savedMediaSource = null;

        // Setup initial activeMediaController
        if (activeMediaControllers.isEmpty()) {
            // Get the last saved media source
            String savedMediaSourceName = getLastActiveMediaSource();
            if (TextUtils.isEmpty(savedMediaSourceName)) {
                // Don't change default values
                return;
            }
            savedMediaSource = createSavedMediaSource(savedMediaSourceName);
            mPrimaryMediaSource.setValue(savedMediaSource);
        } else {
            updatePrimaryMediaSource(activeMediaControllers);
        }

        // Setup initial activeOrPausedMediaSources
        if (activeOrPausedMediaControllers.isEmpty()) {
            if (savedMediaSource != null) {
                mActiveOrPausedMediaSources.setValue(Collections.singletonList(savedMediaSource));
            }
        } else {
            updateActiveOrPausedMediaSources(activeOrPausedMediaControllers);
        }
    }

    private MediaSource createSavedMediaSource(String savedMediaSourceName) {
        ComponentName componentName = ComponentName.unflattenFromString(savedMediaSourceName);
        if (componentName != null) {
            // Initialize using MBS
            return MediaSource.create(mContext, componentName);
        } else {
            // Initialize using package name
            return MediaSource.create(mContext, savedMediaSourceName);
        }
    }

    private void saveLastActiveMediaSource(MediaSource mediaSource) {
        if (mediaSource == null) {
            Log.w(TAG, "Null media source detected, not saving as last active");
            return;
        }
        ComponentName componentName = mediaSource.getBrowseServiceComponentName();
        if (componentName != null) {
            Log.i(TAG, "Saving last active media source " + componentName);
            mSharedPrefs.edit()
                    .putString(LAST_ACTIVE_MEDIA_SOURCE, componentName.flattenToString())
                    .apply();
        } else {
            String packageName = mediaSource.getPackageName();
            Log.i(TAG, "Saving last active media source " + packageName);
            mSharedPrefs.edit().putString(LAST_ACTIVE_MEDIA_SOURCE, packageName).apply();
        }
    }

    private String getLastActiveMediaSource() {
        String mediaSource = mSharedPrefs.getString(LAST_ACTIVE_MEDIA_SOURCE, "");

        return TextUtils.isEmpty(mediaSource)
                ? getCarMediaServiceSession() : mediaSource;
    }

    private String getCarMediaServiceSession() {
        Car car = Car.createCar(mContext);
        CarMediaManager carMediaManager =
                    (CarMediaManager) car.getCarManager(Car.CAR_MEDIA_SERVICE);
        ComponentName componentName = carMediaManager.getMediaSource(MEDIA_SOURCE_MODE_PLAYBACK);
        car.disconnect();

        return componentName.flattenToString();
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

    /** Returns if a playback state is paused. */
    private boolean isPaused(int playbackState) {
        return playbackState == PlaybackState.STATE_PAUSED;
    }

    /** Returns whether a playback state is active or paused. */
    private boolean isPausedOrActive(int playbackState) {
        return isActive(playbackState) || isPaused(playbackState);
    }

    /**
     *  Parses through a list of notifications for those with media style and a media session.
     *  Returns a list of associated package names.
     */
    private List<String> getMediaNotificationPackages(StatusBarNotification[] activeNotifications) {
        List<String> mediaStyleNotificationPackages = new ArrayList<>();

        for (StatusBarNotification sbn : activeNotifications) {
            Notification notification = sbn.getNotification();

            if (notification.extras == null) {
                continue;
            }

            if (mNotificationProvider.isMediaNotification(notification)) {
                mediaStyleNotificationPackages.add(sbn.getPackageName());
            }
        }

        return mediaStyleNotificationPackages;
    }
}
