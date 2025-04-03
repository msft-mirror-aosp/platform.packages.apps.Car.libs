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

package com.android.car.media.common.playback;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import android.os.Handler;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.testing.common.CaptureObserver;
import com.android.car.testing.common.TestLifecycleOwner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;


@RunWith(AndroidJUnit4.class)
public class ProgressLiveDataTest {
    private static final long START_TIME = 500L;
    private static final long START_PROGRESS = 1000L;
    private static final long MAX_PROGRESS = 100000; // 100 seconds

    @Rule
    public final MockitoRule mMockitoRule = MockitoJUnit.rule();

    @Rule
    public final InstantTaskExecutorRule mTaskExecutorRule = new InstantTaskExecutorRule();
    @Rule
    public final TestLifecycleOwner mLifecycleOwner = new TestLifecycleOwner();

    @Mock Handler mHandler;
    private Runnable mRunnable;

    @Mock
    private PlaybackStateCompat mPlaybackState;
    private long mLastPositionUpdateTime;

    private long mCurrentElapsedTime;
    private ProgressLiveData mProgressLiveData;

    @Before
    public void setUp() {
        mCurrentElapsedTime = START_TIME;
        mLastPositionUpdateTime = START_TIME;
        when(mPlaybackState.getLastPositionUpdateTime()).thenAnswer(
                invocation -> mLastPositionUpdateTime);
        when(mPlaybackState.getPosition()).thenReturn(START_PROGRESS);
        when(mPlaybackState.getPlaybackSpeed()).thenReturn(1F);
        when(mPlaybackState.getState()).thenReturn(PlaybackStateCompat.STATE_PLAYING);
        when(mHandler.postDelayed(any(), anyLong())).thenAnswer((Answer<Boolean>) invocation -> {
            mRunnable = invocation.getArgument(0);
            return true;
        });
        doAnswer((Answer<Void>) invocation -> {
            mRunnable = null;
            return null;
        }).when(mHandler).removeCallbacksAndMessages(any());

        mProgressLiveData = new ProgressLiveData(mPlaybackState, MAX_PROGRESS,
                this::getCurrentElapsedTime, mHandler);
    }

    private long getCurrentElapsedTime() {
        return mCurrentElapsedTime;
    }

    private void advanceElapsedTime(long time) {
        mCurrentElapsedTime += time;
        if (mRunnable != null) {
            mRunnable.run();
            mRunnable = null;
        }
    }

    @Test
    public void testSetsValueOnActive() {
        CaptureObserver<PlaybackProgress> progressObserver = new CaptureObserver<>();
        mProgressLiveData.observe(mLifecycleOwner, progressObserver);

        assertThat(progressObserver.hasBeenNotified()).isTrue();
        assertThat(progressObserver.getObservedValue().getProgress()).isEqualTo(START_PROGRESS);
    }

    @Test
    public void testUnknownProgress() {
        CaptureObserver<PlaybackProgress> progressObserver = new CaptureObserver<>();
        when(mPlaybackState.getPosition())
                .thenReturn(PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN);
        mProgressLiveData.observe(mLifecycleOwner, progressObserver);

        assertThat(progressObserver.hasBeenNotified()).isTrue();
        assertThat(progressObserver.getObservedValue().getProgress()).isEqualTo(
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN);
    }

    @Test
    public void testMovesForwardAtNormalSpeed() {
        CaptureObserver<PlaybackProgress> progressObserver = new CaptureObserver<>();
        mProgressLiveData.observe(mLifecycleOwner, progressObserver);
        progressObserver.reset();

        advanceElapsedTime(ProgressLiveData.UPDATE_INTERVAL_MS);

        assertThat(progressObserver.hasBeenNotified()).isTrue();
        assertThat(progressObserver.getObservedValue().getProgress()).isEqualTo(
                START_PROGRESS + ProgressLiveData.UPDATE_INTERVAL_MS);
    }

    @Test
    public void testMovesForwardAtCustomSpeed() {
        CaptureObserver<PlaybackProgress> progressObserver = new CaptureObserver<>();
        mProgressLiveData.observe(mLifecycleOwner, progressObserver);
        float speed = 2F;
        when(mPlaybackState.getPlaybackSpeed()).thenReturn(speed);
        progressObserver.reset();

        advanceElapsedTime(ProgressLiveData.UPDATE_INTERVAL_MS);

        assertThat(progressObserver.hasBeenNotified()).isTrue();
        assertThat(progressObserver.getObservedValue().getProgress()).isEqualTo(
                (long) (START_PROGRESS + ProgressLiveData.UPDATE_INTERVAL_MS * speed));
    }

    @Test
    public void testDoesntMoveForwardWhenPaused() {
        CaptureObserver<PlaybackProgress> progressObserver = new CaptureObserver<>();
        mProgressLiveData.observe(mLifecycleOwner, progressObserver);
        when(mPlaybackState.getState()).thenReturn(PlaybackStateCompat.STATE_PAUSED);
        progressObserver.reset();

        advanceElapsedTime(ProgressLiveData.UPDATE_INTERVAL_MS);

        assertThat(progressObserver.hasBeenNotified()).isTrue();
        assertThat(progressObserver.getObservedValue().getProgress()).isEqualTo(
                START_PROGRESS);
    }

    @Test
    public void testDoesntMoveForwardWhenStopped() {
        CaptureObserver<PlaybackProgress> progressObserver = new CaptureObserver<>();
        mProgressLiveData.observe(mLifecycleOwner, progressObserver);
        when(mPlaybackState.getState()).thenReturn(PlaybackStateCompat.STATE_STOPPED);
        progressObserver.reset();

        advanceElapsedTime(ProgressLiveData.UPDATE_INTERVAL_MS);

        assertThat(progressObserver.hasBeenNotified()).isTrue();
        assertThat(progressObserver.getObservedValue().getProgress()).isEqualTo(
                START_PROGRESS);
    }

    @Test
    public void testDoesntUpdateWhenInactive() {
        CaptureObserver<PlaybackProgress> progressObserver = new CaptureObserver<>();
        mProgressLiveData.observe(mLifecycleOwner, progressObserver);
        mLifecycleOwner.markState(Lifecycle.State.DESTROYED);
        progressObserver.reset();

        advanceElapsedTime(ProgressLiveData.UPDATE_INTERVAL_MS);

        assertThat(progressObserver.hasBeenNotified()).isFalse();
    }
}
