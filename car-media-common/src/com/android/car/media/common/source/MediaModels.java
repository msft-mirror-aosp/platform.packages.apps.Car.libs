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

package com.android.car.media.common.source;

import android.car.media.CarMediaManager;
import android.content.Context;

import androidx.lifecycle.LiveData;

import com.android.car.media.common.browse.MediaItemsRepository;
import com.android.car.media.common.playback.PlaybackViewModel;
import com.android.car.media.common.source.MediaBrowserConnector.BrowsingState;

/** Helps manage related "models". */
public class MediaModels {

    private final MediaSourceViewModel mMediaSourceViewModel;
    private final MediaItemsRepository mMediaItemsRepository;
    private final PlaybackViewModel mPlaybackViewModel;

    /**
     * Creates models tied to {@link CarMediaManagerHelper#getAudioSource} for the given
     * {@link CarMediaManager} mode.
     */
    public MediaModels(Context context, int mode) {
        CarMediaManagerHelper helper = CarMediaManagerHelper.getInstance(context);
        LiveData<MediaSource> srcData = helper.getAudioSource(mode);
        mMediaSourceViewModel = new MediaSourceViewModel(context, srcData);
        LiveData<BrowsingState> browseState = mMediaSourceViewModel.getBrowsingState();
        mMediaItemsRepository = new MediaItemsRepository(browseState);
        mPlaybackViewModel = new PlaybackViewModel(context, browseState);
    }

    /**
     * Creates models tied to a constant {@link MediaSource}. Use in activity models that always
     * show the same source.
     */
    public MediaModels(Context context, MediaSource constantSource) {
        LiveData<MediaSource> srcLiveData = new LiveData<MediaSource>(constantSource) {};
        mMediaSourceViewModel = new MediaSourceViewModel(context, srcLiveData);
        LiveData<BrowsingState> browseState = mMediaSourceViewModel.getBrowsingState();
        mMediaItemsRepository = new MediaItemsRepository(browseState);
        mPlaybackViewModel = new PlaybackViewModel(context, browseState);
    }

    /** Returns the {@link MediaSourceViewModel}. */
    public MediaSourceViewModel getMediaSourceViewModel() {
        return mMediaSourceViewModel;
    }

    /** Returns the {@link MediaItemsRepository}. */
    public MediaItemsRepository getMediaItemsRepository() {
        return mMediaItemsRepository;
    }

    /** Returns the {@link PlaybackViewModel}. */
    public PlaybackViewModel getPlaybackViewModel() {
        return mPlaybackViewModel;
    }

}
