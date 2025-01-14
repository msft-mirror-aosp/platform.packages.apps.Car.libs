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

import static com.android.car.media.common.MediaTestUtils.newFakeMediaSource;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.media.common.source.MediaBrowserConnector.BrowsingState;
import com.android.car.media.common.source.MediaBrowserConnector.ConnectionStatus;
import com.android.car.testing.common.InstantTaskExecutorRule;
import com.android.car.testing.common.TestLifecycleOwner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class MediaBrowserConnectorTest {

    @Rule
    public final MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Rule
    public final InstantTaskExecutorRule mTaskExecutorRule = new InstantTaskExecutorRule();
    @Rule
    public final TestLifecycleOwner mLifecycleOwner = new TestLifecycleOwner();

    @Mock
    public MediaBrowserCompat mMediaBrowser1;
    @Mock
    public MediaBrowserCompat mMediaBrowser2;
    @Mock
    public MediaControllerCompat mMediaController;

    private MediaSource mMediaSource1;
    private MediaSource mMediaSource2;
    private MediaSource mMediaSource3;

    private final Map<MediaSource, MediaBrowserCompat> mBrowsers = new HashMap<>(2);

    private MediaBrowserConnector mBrowserConnector;
    private MediaBrowserCompat.ConnectionCallback mConnectionCallback;
    private Context mContext;

    @Mock
    public MediaBrowserConnector.Callback mConnectedBrowserCallback;
    @Captor
    private ArgumentCaptor<BrowsingState> mBrowsingStateCaptor;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
        mMediaSource1 = newFakeMediaSource(mContext.getPackageManager(), "mediaService1",
                "className1");
        mMediaSource2 = newFakeMediaSource(mContext.getPackageManager(), "mediaService2",
                "className2");
        mMediaSource3 = newFakeMediaSource(mContext.getPackageManager(), mMediaController);
        mBrowsers.put(mMediaSource1, mMediaBrowser1);
        mBrowsers.put(mMediaSource2, mMediaBrowser2);

        doNothing().when(mConnectedBrowserCallback).onBrowserConnectionChanged(
                mBrowsingStateCaptor.capture());

        mBrowserConnector = new MediaBrowserConnector(mContext, mConnectedBrowserCallback) {
            @Override
            protected MediaBrowserCompat createMediaBrowser(@NonNull MediaSource mediaSource,
                    @NonNull MediaBrowserCompat.ConnectionCallback callback) {
                mConnectionCallback = callback;
                return mBrowsers.get(mediaSource);
            }
        };
    }

    @Test
    public void testExceptionOnConnectDoesNotCrash() {
        setConnectionAction(() -> {
            throw new IllegalStateException("expected");
        });

        mBrowserConnector.connectTo(mMediaSource1);
        verify(mMediaBrowser1).connect();
    }

    @Test
    public void testConnectionCallback_onConnected() {
        doReturn(true).when(mMediaBrowser1).isConnected();
        setConnectionAction(() -> mConnectionCallback.onConnected());

        mBrowserConnector.connectTo(mMediaSource1);

        BrowsingState state = mBrowsingStateCaptor.getValue();
        assertThat(state.mBrowser).isEqualTo(mMediaBrowser1);
        assertThat(state.mConnectionStatus).isEqualTo(ConnectionStatus.CONNECTED);
    }

    @Test
    public void testConnectionCallback_onConnected_inconsistentState() {
        doReturn(false).when(mMediaBrowser1).isConnected();
        setConnectionAction(() -> mConnectionCallback.onConnected());

        mBrowserConnector.connectTo(mMediaSource1);

        BrowsingState state = mBrowsingStateCaptor.getValue();
        assertThat(state.mBrowser).isEqualTo(mMediaBrowser1);
        assertThat(state.mConnectionStatus).isEqualTo(ConnectionStatus.REJECTED);
    }

    @Test
    public void testConnectionCallback_onConnectionFailed() {
        setConnectionAction(() -> mConnectionCallback.onConnectionFailed());

        mBrowserConnector.connectTo(mMediaSource1);

        BrowsingState state = mBrowsingStateCaptor.getValue();
        assertThat(state.mBrowser).isEqualTo(mMediaBrowser1);
        assertThat(state.mConnectionStatus).isEqualTo(ConnectionStatus.REJECTED);
    }

    @Test
    public void testConnectionCallback_onConnectionSuspended() {
        doReturn(true).when(mMediaBrowser1).isConnected();
        setConnectionAction(() -> {
            mConnectionCallback.onConnected();
            mConnectionCallback.onConnectionSuspended();
        });

        mBrowserConnector.connectTo(mMediaSource1);


        List<BrowsingState> browsingStates = mBrowsingStateCaptor.getAllValues();
        assertThat(browsingStates.get(0).mBrowser).isEqualTo(mMediaBrowser1);
        assertThat(browsingStates.get(1).mBrowser).isEqualTo(mMediaBrowser1);
        assertThat(browsingStates.get(2).mBrowser).isEqualTo(mMediaBrowser1);

        assertThat(browsingStates.get(0).mConnectionStatus).isEqualTo(ConnectionStatus.CONNECTING);
        assertThat(browsingStates.get(1).mConnectionStatus).isEqualTo(ConnectionStatus.CONNECTED);
        assertThat(browsingStates.get(2).mConnectionStatus).isEqualTo(ConnectionStatus.SUSPENDED);
    }

    @Test
    public void testConnectionCallback_onConnectedIgnoredWhenLate() {
        mBrowserConnector.connectTo(mMediaSource1);
        MediaBrowserCompat.ConnectionCallback cb1 = mConnectionCallback;

        mBrowserConnector.connectTo(mMediaSource2);
        MediaBrowserCompat.ConnectionCallback cb2 = mConnectionCallback;

        cb2.onConnected();
        cb1.onConnected();
        assertThat(mBrowsingStateCaptor.getValue().mBrowser).isEqualTo(mMediaBrowser2);
    }

    @Test
    public void testConnectionCallback_noBrowseTree_returnsNull() {
        mBrowserConnector.connectTo(mMediaSource3);

        assertThat(mBrowsingStateCaptor.getValue().mConnectionStatus)
            .isEqualTo(ConnectionStatus.NONEXISTENT);
    }

    private void setConnectionAction(@NonNull Runnable action) {
        doAnswer(invocation -> {
            action.run();
            return null;
        }).when(mMediaBrowser1).connect();
    }

}
