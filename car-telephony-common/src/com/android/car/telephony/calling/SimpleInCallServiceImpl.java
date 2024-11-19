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
package com.android.car.telephony.calling;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;

import com.android.car.apps.common.log.L;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple InCallService implementation providing callback listeners for call audio state, call list,
 * and call state changes
 */
public class SimpleInCallServiceImpl extends InCallService {
    private static final String TAG = "CTC.InCallServiceImpl";

    /** An action which indicates a bind is from local component. */
    public static final String ACTION_LOCAL_BIND = "local_bind";

    protected final CopyOnWriteArrayList<CallAudioStateCallback> mCallAudioStateCallbacks =
            new CopyOnWriteArrayList<>();

    protected final ArrayList<ActiveCallListChangedCallback>
            mActiveCallListChangedCallbacks = new ArrayList<>();

    protected final ArrayList<CallStateCallback> mCallStateCallbacks = new ArrayList<>();

    protected final Call.Callback mCallStateChangedCallback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int state) {
            L.d(TAG, "onStateChanged: %s, %s", call, state);
            for (CallStateCallback callback : mCallStateCallbacks) {
                callback.onStateChanged(call, state);
            }
        }

        @Override
        public void onParentChanged(Call call, Call parent) {
            L.d(TAG, "onParentChanged: %s, %s", call, parent);
            for (CallStateCallback callback : mCallStateCallbacks) {
                callback.onParentChanged(call, parent);
            }
        }

        @Override
        public void onChildrenChanged(Call call, List<Call> children) {
            L.d(TAG, "onChildrenChanged: %s, %s", call, children);
            for (CallStateCallback callback : mCallStateCallbacks) {
                callback.onChildrenChanged(call, children);
            }
        }
    };

    @Override
    public void onCallAdded(Call call) {
        L.d(TAG, "onCallAdded: %s", call);
        call.registerCallback(mCallStateChangedCallback);
        notifyCallAdded(call);
    }

    @Override
    public void onCallRemoved(Call call) {
        L.d(TAG, "onCallRemoved: %s", call);
        call.unregisterCallback(mCallStateChangedCallback);
        notifyCallRemoved(call);
    }

    @Override
    public void onCallAudioStateChanged(CallAudioState audioState) {
        L.d(TAG, "onCallAudioStateChanged: %s", audioState);
        for (CallAudioStateCallback callback : mCallAudioStateCallbacks) {
            callback.onCallAudioStateChanged(audioState);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        L.d(TAG, "onBind: %s", intent);
        return ACTION_LOCAL_BIND.equals(intent.getAction())
                ? new LocalBinder()
                : super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.d(TAG, "onUnbind, intent: %s", intent);
        if (ACTION_LOCAL_BIND.equals(intent.getAction())) {
            return false;
        }
        return super.onUnbind(intent);
    }

    /**
     * Dispatches the call to {@link ActiveCallListChangedCallback}.
     */
    protected boolean notifyCallAdded(Call call) {
        boolean isHandled = false;
        for (ActiveCallListChangedCallback callback : mActiveCallListChangedCallbacks) {
            if (callback.onTelecomCallAdded(call)) {
                isHandled = true;
            }
        }
        return isHandled;
    }

    /**
     * Dispatches the call to {@link ActiveCallListChangedCallback}.
     */
    protected boolean notifyCallRemoved(Call call) {
        boolean isHandled = false;
        for (ActiveCallListChangedCallback callback : mActiveCallListChangedCallbacks) {
            if (callback.onTelecomCallRemoved(call)) {
                isHandled = true;
            }
        }
        return isHandled;
    }

    /** Adds a listener to receive CallAudioStateCallbacks */
    public void addCallAudioStateChangedCallback(CallAudioStateCallback callback) {
        mCallAudioStateCallbacks.add(callback);
    }

    /** Removes a listener that receives CallAudioStateCallbacks */
    public void removeCallAudioStateChangedCallback(CallAudioStateCallback callback) {
        mCallAudioStateCallbacks.remove(callback);
    }

    /** Adds a listener to receive CallListChangedCallbacks */
    public void addActiveCallListChangedCallback(ActiveCallListChangedCallback callback) {
        mActiveCallListChangedCallbacks.add(callback);
    }

    /** Removes a listener that receives CallListChangedCallbacks */
    public void removeActiveCallListChangedCallback(ActiveCallListChangedCallback callback) {
        mActiveCallListChangedCallbacks.remove(callback);
    }

    /** Adds a listener to receive CallStateCallbacks */
    public void addCallStateChangedCallback(CallStateCallback callback) {
        mCallStateCallbacks.add(callback);
    }

    /** Removes a listener that receives CallStateCallbacks */
    public void removeCallStateChangedCallback(CallStateCallback callback) {
        mCallStateCallbacks.remove(callback);
    }

    /** Listens to active call list changes. Callbacks will be called on main thread. */
    public interface ActiveCallListChangedCallback {
        /**
         * Called when a new call is added.
         *
         * @return if the given call has been handled by this callback.
         */
        boolean onTelecomCallAdded(Call telecomCall);

        /**
         * Called when an existing call is removed.
         *
         * @return if the given call has been handled by this callback.
         */
        boolean onTelecomCallRemoved(Call telecomCall);
    }

    /** Listens to call audio state changes. Callbacks will be called on the main thread. */
    public interface CallAudioStateCallback {
        /**
         * Called when the call audio state has changed.
         */
        void onCallAudioStateChanged(CallAudioState callAudioState);
    }

    /** Listens to call state changes. Callbacks will be called on the main thread. */
    public interface CallStateCallback {
        /**
         * Called when the state of a {@link Call} has changed.
         */
        void onStateChanged(Call call, int state);

        /**
         * Called when a {@link Call} has been added to a conference.
         */
        void onParentChanged(Call call, Call parent);

        /**
         * Called when a conference {@link Call} has children calls added or removed.
         */
        void onChildrenChanged(Call call, List<Call> children);
    }

    /**
     * Local binder available to the package owning this instance.
     */
    public class LocalBinder extends Binder {
        /**
         * Returns a reference to {@link SimpleInCallServiceImpl}.
         */
        public SimpleInCallServiceImpl getService() {
            if (getCallingPid() == Process.myPid()) {
                return SimpleInCallServiceImpl.this;
            }
            return null;
        }
    }
}
