/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.android.car.media.common.ui;

import static com.android.car.apps.common.util.LiveDataFunctions.dataOf;

import android.app.Application;
import android.car.media.CarMediaManager;
import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.car.media.common.browse.MediaItemsRepository;
import com.android.car.media.common.playback.PlaybackViewModel;
import com.android.car.media.common.source.CarMediaManagerHelper;
import com.android.car.media.common.source.MediaModels;
import com.android.car.media.common.source.MediaSessionHelper;
import com.android.car.media.common.source.MediaSource;
import com.android.car.media.common.source.MediaSourceViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** ViewModel used to track state of media widgets that use the {@link PlaybackCardController} */
public class PlaybackCardViewModel extends AndroidViewModel {

    private Context mContext;
    private MediaModels mModels;
    private CarMediaManagerHelper mCarMediaManagerHelper;
    private MediaSessionHelper mMediaSessionHelper;
    private MutableLiveData<List<MediaSource>> mHistoryListData = dataOf(null);
    private boolean mNeedsInitialization = true;
    private boolean mQueueVisible = false;
    private boolean mHistoryVisible = false;
    private boolean mOverflowExpanded = false;

    public PlaybackCardViewModel(@NonNull Application application) {
        super(application);
    }

    /** Initialize the PlaybackCardViewModel */
    public void init(MediaModels models) {
        mContext = getApplication().getApplicationContext();
        mModels = models;
        mCarMediaManagerHelper = CarMediaManagerHelper.getInstance(getApplication());
        mMediaSessionHelper = MediaSessionHelper.getInstance(getApplication());
        mMediaSessionHelper.getActiveOrPausedMediaSources().observeForever(this::updateHistoryList);
        mNeedsInitialization = false;
    }

    /**
     * Returns whether the ViewModel needs to be initialized. The ViewModel may need
     * re-initialization if a config change occurs or if the system kills the Fragment.
     */
    public boolean needsInitialization() {
        return mNeedsInitialization;
    }

    public MediaItemsRepository getMediaItemsRepository() {
        return mModels.getMediaItemsRepository();
    }

    public PlaybackViewModel getPlaybackViewModel() {
        return mModels.getPlaybackViewModel();
    }

    public MediaSourceViewModel getMediaSourceViewModel() {
        return mModels.getMediaSourceViewModel();
    }

    public void setQueueVisible(boolean visible) {
        mQueueVisible = visible;
    }

    public boolean getQueueVisible() {
        return mQueueVisible;
    }

    public void setHistoryVisible(boolean visible) {
        mHistoryVisible = visible;
    }

    public boolean getHistoryVisible() {
        return mHistoryVisible;
    }

    public void setOverflowExpanded(boolean expanded) {
        mOverflowExpanded = expanded;
    }

    public boolean getOverflowExpanded() {
        return mOverflowExpanded;
    }

    public LiveData<List<MediaSource>> getHistoryList() {
        return mHistoryListData;
    }

    private void updateHistoryList(List<MediaSource> mediaSources) {
        List<MediaSource> historyList = new ArrayList<>();

        Set<ComponentName> componentNameSet = new HashSet<>();
        for (MediaSource mediaSource : mediaSources) {
            ComponentName componentName = mediaSource.getBrowseServiceComponentName();
            if (componentName != null) {
                componentNameSet.add(componentName);
            }
            historyList.add(mediaSource);
        }

        List<ComponentName> componentNameList = mCarMediaManagerHelper
                .getLastMediaSources(CarMediaManager.MEDIA_SOURCE_MODE_PLAYBACK);
        for (ComponentName componentName : componentNameList) {
            if (componentNameSet.contains(componentName)) {
                continue;
            }
            MediaSource mediaSource = MediaSource.create(mContext, componentName);
            historyList.add(mediaSource);
        }

        mHistoryListData.setValue(historyList);
    }
}
