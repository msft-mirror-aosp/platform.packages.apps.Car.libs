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
import static com.android.car.media.common.MediaTestUtils.newFakeMediaSource;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.media.common.source.MediaBrowserConnector.BrowsingState;
import com.android.car.media.common.source.MediaBrowserConnector.ConnectionStatus;
import com.android.car.testing.common.CaptureObserver;
import com.android.car.testing.common.InstantTaskExecutorRule;
import com.android.car.testing.common.TestLifecycleOwner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


@RunWith(AndroidJUnit4.class)
public class MediaSourceViewModelTest {

    private static final String TAG = "MediaSourceVMTest";

    private MutableLiveData<MediaSource> mMediaSourceLiveData = null;

    @Rule
    public final MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Rule
    public final InstantTaskExecutorRule mTaskExecutorRule = new InstantTaskExecutorRule();
    @Rule
    public final TestLifecycleOwner mLifecycleOwner = new TestLifecycleOwner();

    @Mock
    public MediaBrowserCompat mMediaBrowser;

    private MediaSourceViewModel mViewModel;

    private MediaSource mRequestedSource;
    private MediaSource mMediaSource;
    private Context mContext;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
        mMediaSourceLiveData = dataOf(null);
        mRequestedSource = null;
        mMediaSource = null;
    }

    private void initializeViewModel() {
        Application application = ApplicationProvider.getApplicationContext();
        if (Looper.myLooper() == null) {
            Looper.prepare();  // Needed when running with atest.
        }

        mViewModel = new MediaSourceViewModel(mMediaSourceLiveData,
                callback -> new MediaBrowserConnector(application, callback) {
                    @NonNull
                    @Override
                    protected MediaBrowserCompat createMediaBrowser(@NonNull MediaSource src,
                            @NonNull MediaBrowserCompat.ConnectionCallback callback) {
                        mRequestedSource = src;
                        return mMediaBrowser;
                    }
                });
    }

    @Test
    public void testGetSelectedMediaSource_none() {
        initializeViewModel();
        CaptureObserver<MediaSource> observer = new CaptureObserver<>();

        mViewModel.getPrimaryMediaSource().observe(mLifecycleOwner, observer);

        assertThat(observer.getObservedValue()).isNull();
    }

    @Test
    public void testGetMediaController_connectedBrowser() {
        CaptureObserver<BrowsingState> observer = new CaptureObserver<>();
        mMediaSource = newFakeMediaSource(mContext.getPackageManager(), "test", "test");
        when(mMediaBrowser.isConnected()).thenReturn(true);

        initializeViewModel();
        mMediaSourceLiveData.setValue(mMediaSource);

        mViewModel.getBrowserCallback().onBrowserConnectionChanged(
                new BrowsingState(mContext, mMediaSource, mMediaBrowser,
                        ConnectionStatus.CONNECTED));
        mViewModel.getBrowsingState().observe(mLifecycleOwner, observer);

        BrowsingState browsingState = observer.getObservedValue();
        assertThat(browsingState.mBrowser).isSameInstanceAs(mMediaBrowser);
        assertThat(browsingState.mConnectionStatus).isEqualTo(ConnectionStatus.CONNECTED);
        assertThat(mRequestedSource).isEqualTo(mMediaSource);
    }

    @Test
    public void testGetMediaController_noActiveSession_notConnected() {
        CaptureObserver<BrowsingState> observer = new CaptureObserver<>();
        mMediaSource = newFakeMediaSource(mContext.getPackageManager(), "test", "test");
        when(mMediaBrowser.isConnected()).thenReturn(false);
        initializeViewModel();
        mMediaSourceLiveData.setValue(mMediaSource);

        mViewModel.getBrowserCallback().onBrowserConnectionChanged(
                new BrowsingState(mContext, mMediaSource, mMediaBrowser,
                        ConnectionStatus.REJECTED));
        mViewModel.getBrowsingState().observe(mLifecycleOwner, observer);

        BrowsingState browsingState = observer.getObservedValue();
        assertThat(browsingState.mBrowser).isSameInstanceAs(mMediaBrowser);
        assertThat(browsingState.mConnectionStatus).isEqualTo(ConnectionStatus.REJECTED);
        assertThat(mRequestedSource).isEqualTo(mMediaSource);
    }
}
