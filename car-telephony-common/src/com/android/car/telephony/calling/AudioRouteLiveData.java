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

package com.android.car.telephony.calling;

import android.telecom.CallAudioState;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.android.car.apps.common.log.L;
import com.android.car.telephony.common.CallDetail;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;

/**
 * Provides the current connecting audio route.
 */
public class AudioRouteLiveData extends MediatorLiveData<Integer> {
    private static final String TAG = "CD.AudioRouteLiveData";

    private final InCallServiceManager mInCallServiceManager;
    private final LiveData<CallDetail> mPrimaryCallDetailLiveData;

    @AssistedInject
    public AudioRouteLiveData(
            @Assisted LiveData<CallDetail> primaryCallDetailLiveData,
            @Assisted LiveData<CallAudioState> callAudioStateLiveData,
            InCallServiceManager inCallServiceManager) {
        mInCallServiceManager = inCallServiceManager;
        mPrimaryCallDetailLiveData = primaryCallDetailLiveData;

        addSource(mPrimaryCallDetailLiveData, this::updateOngoingCallAudioRoute);
        addSource(callAudioStateLiveData, callAudioState -> updateAudioRoute());
    }

    @Override
    protected void onActive() {
        super.onActive();
        updateAudioRoute();
    }

    private void updateAudioRoute() {
        CallDetail primaryCallDetail = mPrimaryCallDetailLiveData.getValue();
        updateOngoingCallAudioRoute(primaryCallDetail);
    }

    private void updateOngoingCallAudioRoute(CallDetail callDetail) {
        if (callDetail == null) {
            // Phone call might have disconnected, no action.
            return;
        }
        int state = callDetail.getScoState();
        int audioRoute = mInCallServiceManager.getAudioRoute(state);
        if (getValue() == null || audioRoute != getValue()) {
            L.d(TAG, "updateAudioRoute to %s", audioRoute);
            setValue(audioRoute);
        }
    }

    /**
     * Factory to create {@link AudioRouteLiveData} instances via the {@link AssistedInject}
     * constructor.
     */
    @AssistedFactory
    public interface Factory {
        /** Creates an {@link AudioRouteLiveData} instance. */
        AudioRouteLiveData create(LiveData<CallDetail> primaryCallDetailLiveData,
                                  LiveData<CallAudioState> callAudioStateLiveData);
    }
}
