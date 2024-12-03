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
package com.android.car.telephony.calling;

import static com.android.car.telephony.calling.InCallServiceManager.PROPERTY_IN_CALL_SERVICE;

import android.telecom.Call;
import android.telecom.CallAudioState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.car.apps.common.log.L;

import com.google.common.base.Predicate;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

/**
 * This class holds the core call entities from the call list provided by
 * {@link android.telecom.InCallService}. The calls are grouped and ordered based on a given
 * {@link Comparator<Call>}.
 */
public class InCallModel implements SimpleInCallServiceImpl.ActiveCallListChangedCallback,
        SimpleInCallServiceImpl.CallStateCallback, SimpleInCallServiceImpl.CallAudioStateCallback,
        PropertyChangeListener {

    private static final String TAG = "CTC.InCallModel";

    private final MutableLiveData<List<Call>> mCallListLiveData;
    private final MutableLiveData<Call> mIncomingCallLiveData;
    private final MutableLiveData<List<Call>> mOngoingCallListLiveData;
    private final MutableLiveData<List<Call>> mSelfManagedCallListLiveData;
    private final MutableLiveData<CallAudioState> mCallAudioStateLiveData;

    private final MutableLiveData<Call> mPrimaryCallLiveData;
    private final MutableLiveData<Call> mSecondaryCallLiveData;
    private final MutableLiveData<List<Call>> mConferenceCallListLiveData;

    private final Comparator<Call> mCallComparator;

    private final InCallServiceManager mInCallServiceManager;
    protected SimpleInCallServiceImpl mInCallService;

    @Inject
    public InCallModel(InCallServiceManager inCallServiceManager,
                       Comparator<Call> callComparator) {
        mCallListLiveData = new MutableLiveData<>();
        mIncomingCallLiveData = new MutableLiveData<>();
        mOngoingCallListLiveData = new MutableLiveData<>();
        mSelfManagedCallListLiveData = new MutableLiveData<>();
        mConferenceCallListLiveData = new MutableLiveData<>();
        mCallAudioStateLiveData = new MutableLiveData<>();
        mPrimaryCallLiveData = new MutableLiveData<>();
        mSecondaryCallLiveData = new MutableLiveData<>();

        mCallComparator = callComparator;

        mInCallServiceManager = inCallServiceManager;
        mInCallServiceManager.addObserver(this);
        if (mInCallServiceManager.getInCallService() != null) {
            onInCallServiceConnected();
        }
    }

    private void onInCallServiceConnected() {
        L.d(TAG, "InCallService connected");
        mInCallService = (SimpleInCallServiceImpl) mInCallServiceManager.getInCallService();
        mInCallService.addActiveCallListChangedCallback(this);
        mInCallService.addCallAudioStateChangedCallback(this);
        mInCallService.addCallStateChangedCallback(this);
        modelCallList();
    }


    @Override
    public boolean onTelecomCallAdded(Call telecomCall) {
        modelCallList();
        return false;
    }

    @Override
    public boolean onTelecomCallRemoved(Call telecomCall) {
        modelCallList();
        return false;
    }

    @Override
    public void onCallAudioStateChanged(CallAudioState callAudioState) {
        mCallAudioStateLiveData.setValue(callAudioState);
    }

    @Override
    public void onStateChanged(Call call, int state) {
        modelCallList();
    }

    @Override
    public void onParentChanged(Call call, Call parent) {
        modelCallList();
    }

    @Override
    public void onChildrenChanged(Call call, List<Call> children) {
        modelCallList();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        L.d(TAG, "InCallService has updated.");
        if (PROPERTY_IN_CALL_SERVICE.equals(evt.getPropertyName())) {
            if (mInCallServiceManager.getInCallService() != null) {
                onInCallServiceConnected();
            } else {
                modelCallList();
            }
        }
    }

    /** Cleanup connected services on teardown. */
    public void tearDown() {
        mInCallServiceManager.removeObserver(this);
        if (mInCallService != null) {
            mInCallService.removeActiveCallListChangedCallback(this);
            mInCallService.removeCallAudioStateChangedCallback(this);
            mInCallService.removeCallStateChangedCallback(this);
        }
    }

    /**
     * This function sets the values for the call list LiveDatas. It is triggered whenever a
     * change in the call list or call state occurs.
     */
    protected void modelCallList() {
        L.d(TAG, "Call list changed");

        List<Call> callList = getCallList();
        mCallListLiveData.setValue(callList);

        List<Call> selfManagedCallList = filter(callList, call ->
                call != null && call.getDetails().hasProperty(Call.Details.PROPERTY_SELF_MANAGED));
        mSelfManagedCallListLiveData.setValue(selfManagedCallList);

        List<Call> activeCallList = filter(callList, call ->
                call != null && call.getDetails().getState() != Call.STATE_RINGING);
        recalculateOngoingCallList(activeCallList);

        Call ringingCall = firstMatch(callList, call ->
                call != null && call.getDetails().getState() == Call.STATE_RINGING);
        mIncomingCallLiveData.setValue(ringingCall);
    }

    private void recalculateOngoingCallList(List<Call> activeCallList) {
        L.d(TAG, "Recalculate ongoing call list");

        List<Call> conferenceList = new ArrayList<>();
        List<Call> ongoingCallList = new ArrayList<>();
        for (Call call : activeCallList) {
            if (call.getParent() != null) {
                conferenceList.add(call);
            } else {
                ongoingCallList.add(call);
            }
        }
        ongoingCallList.sort(mCallComparator);

        L.d(TAG, "activeList(%d): %s", activeCallList.size(), activeCallList);
        L.d(TAG, "conf(%d): %s", conferenceList.size(), conferenceList);
        L.d(TAG, "ongoing(%d): %s", ongoingCallList.size(), ongoingCallList);
        mConferenceCallListLiveData.setValue(conferenceList);
        mOngoingCallListLiveData.setValue(ongoingCallList);
        mPrimaryCallLiveData.setValue(ongoingCallList.isEmpty() ? null : ongoingCallList.get(0));
        mSecondaryCallLiveData.setValue(ongoingCallList.size() > 1 ? ongoingCallList.get(1) : null);
    }

    @Nullable
    private static Call firstMatch(List<Call> callList, Predicate<Call> predicate) {
        List<Call> filteredResults = filter(callList, predicate);
        return filteredResults.isEmpty() ? null : filteredResults.get(0);
    }

    @NonNull
    private static List<Call> filter(List<Call> callList, Predicate<Call> predicate) {
        if (callList == null || predicate == null) {
            return Collections.emptyList();
        }

        List<Call> filteredResults = new ArrayList<>();
        for (Call call : callList) {
            if (predicate.apply(call)) {
                filteredResults.add(call);
            }
        }
        return filteredResults;
    }

    /**
     * Returns the list of calls from the InCallService.
     */
    public List<Call> getCallList() {
        return mInCallService == null ? Collections.emptyList() : mInCallService.getCalls();
    }

    /**
     * Returns the call list livedata.
     */
    public LiveData<List<Call>> getCallListLiveData() {
        return mCallListLiveData;
    }

    /**
     * Returns the ongoing call list livedata. This includes all calls that are not ringing.
     */
    public LiveData<List<Call>> getOngoingCallListLiveData() {
        return mOngoingCallListLiveData;
    }

    /**
     * Returns the incoming call livedata.
     */
    public LiveData<Call> getIncomingCallLiveData() {
        return mIncomingCallLiveData;
    }

    /**
     * Returns the conference call list livedata. These are calls that are involved in a conference.
     */
    public LiveData<List<Call>> getConferenceCallListLiveData() {
        return mConferenceCallListLiveData;
    }

    /**
     * Returns the primary call livedata.
     */
    public LiveData<Call> getPrimaryCallLiveData() {
        return mPrimaryCallLiveData;
    }

    /**
     * Returns the secondary call livedata.
     */
    public LiveData<Call> getSecondaryCallLiveData() {
        return mSecondaryCallLiveData;
    }

    /**
     * Returns the self managed call list livedata.
     */
    public LiveData<List<Call>> getSelfManagedCallListLiveData() {
        return mSelfManagedCallListLiveData;
    }

    /**
     * Returns the CallAudioState livedata of the currently active phone.
     */
    public LiveData<CallAudioState> getCallAudioStateLiveData() {
        return mCallAudioStateLiveData;
    }
}
