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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.android.car.telephony.common.CallDetail;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;

import java.util.List;

/**
 * Returns a list of {@link android.telecom.CallAudioState.CallAudioRoute}s for the primary ongoing
 * call.
 */
public class SupportedAudioRoutesLiveData extends MediatorLiveData<List<Integer>> {
    private boolean mIsHfpConnection;
    private final InCallServiceManager mInCallServiceManager;

    @AssistedInject
    public SupportedAudioRoutesLiveData(
            @Assisted LiveData<CallDetail> primaryCallDetailLiveData,
            InCallServiceManager inCallServiceManager) {
        mInCallServiceManager = inCallServiceManager;

        mIsHfpConnection = false;
        addSource(primaryCallDetailLiveData, this::updateOngoingCallSupportedAudioRoutes);
    }

    private void updateOngoingCallSupportedAudioRoutes(CallDetail callDetail) {
        if (callDetail == null) {
            // Phone call might have disconnected, no action.
            return;
        }
        boolean isHfpConnection = callDetail.isBluetoothCall();
        // If it is the same type of phone account with a previous call, do nothing.
        if (getValue() != null && isHfpConnection == mIsHfpConnection) {
            return;
        }
        mIsHfpConnection = isHfpConnection;
        List<Integer> audioRoutes = mInCallServiceManager.getSupportedAudioRoute(callDetail);
        setValue(audioRoutes);
    }

    /**
     * Factory to create {@link SupportedAudioRoutesLiveData} instances via the {@link
     * AssistedInject} constructor.
     */
    @AssistedFactory
    public interface Factory {
        /** Creates a {@link SupportedAudioRoutesLiveData} instance. */
        SupportedAudioRoutesLiveData create(LiveData<CallDetail> primaryCallDetailLiveData);
    }
}
