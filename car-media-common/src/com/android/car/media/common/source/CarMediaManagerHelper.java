/*
 * Copyright 2023 The Android Open Source Project
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

import static android.car.media.CarMediaManager.MEDIA_SOURCE_MODE_BROWSE;
import static android.car.media.CarMediaManager.MEDIA_SOURCE_MODE_PLAYBACK;

import static com.android.car.apps.common.util.LiveDataFunctions.dataOf;

import android.car.Car;
import android.car.media.CarMediaManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Helps interact with {@link CarMediaManager}.
 */
public class CarMediaManagerHelper {

    private static final String TAG = "CarMediaManagerHelper";
    private static final String[] MODES = {"playback" , "browse"};

    private static CarMediaManagerHelper sInstance;

    /** Returns the singleton. */
    public static CarMediaManagerHelper getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new CarMediaManagerHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Factory for creating dependencies. Can be swapped out for testing.
     */
    @VisibleForTesting
    interface InputFactory {
        Car getCarApi();

        CarMediaManager getCarMediaManager(Car carApi);

        MediaSource getMediaSource(ComponentName componentName);

        boolean isAudioMediaSource(ComponentName componentName);
    }

    private final InputFactory mInputFactory;
    private final Car mCar;
    private final CarMediaManager mCarMediaManager;
    private final Handler mHandler;

    private final CarMediaManager.MediaSourceChangedListener[] mMediaSourceListeners = {null, null};

    private final ArrayList<MutableLiveData<MediaSource>> mAudioSources = new ArrayList<>(2);

    private CarMediaManagerHelper(@NonNull Context appContext) {
        this(appContext, createInputFactory(appContext));
    }

    @VisibleForTesting
    private CarMediaManagerHelper(@NonNull Context appContext, InputFactory factory) {
        mInputFactory = factory;
        mAudioSources.add(dataOf(null));
        mAudioSources.add(dataOf(null));

        mCar = mInputFactory.getCarApi();
        mCarMediaManager = mInputFactory.getCarMediaManager(mCar);
        mHandler = new Handler(appContext.getMainLooper());

        for (int mode : new int[]{MEDIA_SOURCE_MODE_BROWSE, MEDIA_SOURCE_MODE_PLAYBACK}) {
            mMediaSourceListeners[mode] = componentName -> mHandler.post(
                    () -> updateMediaSource(componentName, mode));
            mCarMediaManager.addMediaSourceListener(mMediaSourceListeners[mode], mode);

            ComponentName src = getInitialMediaSource(mode);
            Log.i(TAG, MODES[mode] + " init with " + src);
            updateMediaSource(src, mode);
        }
    }

    private static InputFactory createInputFactory(@NonNull Context appContext) {
        return new InputFactory() {
            private final MediaSourceUtil mMediaSourceUtil =
                    new MediaSourceUtil(appContext);

            @Override
            public Car getCarApi() {
                return Car.createCar(appContext);
            }

            @Override
            public CarMediaManager getCarMediaManager(Car carApi) {
                return (CarMediaManager) carApi.getCarManager(Car.CAR_MEDIA_SERVICE);
            }

            @Override
            public MediaSource getMediaSource(ComponentName componentName) {
                return componentName == null ? null : MediaSource.create(appContext, componentName);
            }

            @Override
            public boolean isAudioMediaSource(ComponentName componentName) {
                return mMediaSourceUtil.isAudioMediaSource(componentName);
            }
        };
    }

    /** Returns a filtered live data of {@link MediaSource} with only audio apps. */
    public LiveData<MediaSource> getAudioSource(int mode) {
        return mAudioSources.get(mode);
    }

    /**
     * Updates the primary media source for the given mode.
     */
    public void setPrimaryMediaSource(@NonNull MediaSource mediaSource, int mode) {
        // Update the live data with the new value right away.
        updateMediaSource(mediaSource.getBrowseServiceComponentName(), mode);
        mCarMediaManager.setMediaSource(mediaSource.getBrowseServiceComponentName(), mode);
    }

    private void updateMediaSource(ComponentName src, int mode) {
        MutableLiveData<MediaSource> audioSource = mAudioSources.get(mode);
        if (src == null) {
            audioSource.setValue(null);
        } else if (mInputFactory.isAudioMediaSource(src)) {
            Log.i(TAG, MODES[mode] + " from: " + audioSource.getValue() + " to audio app:" + src);
            audioSource.setValue(mInputFactory.getMediaSource(src));
        } else {
            Log.i(TAG, MODES[mode] + " keep: " + audioSource.getValue() + " skip:" + src);
        }
    }

    /**
     * Iterate over past sources and find the first valid media source
     */
    private ComponentName getInitialMediaSource(int mode) {
        List<ComponentName> lastMediaSources = mCarMediaManager.getLastMediaSources(mode);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, MODES[mode] + " found media sources history:  " + lastMediaSources);
        }
        return lastMediaSources.stream()
                .filter(Objects::nonNull)
                .filter(mInputFactory::isAudioMediaSource)
                .findFirst()
                .orElse(null);
    }
}
