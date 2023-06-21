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

import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;

import androidx.annotation.Nullable;

import com.android.car.apps.common.log.L;
import com.android.car.telephony.common.CallDetail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public final class InCallServiceManager {
    private static final String TAG = "CD.InCallServiceProvider";
    private static final String EVENT_SCO_CONNECT = "com.android.bluetooth.hfpclient.SCO_CONNECT";
    private static final String EVENT_SCO_DISCONNECT =
            "com.android.bluetooth.hfpclient.SCO_DISCONNECT";
    private static final String PROPERTY_IN_CALL_SERVICE="PROPERTY_IN_CALL_SERVICE";

    private InCallService mInCallService;
    private PropertyChangeSupport mPropertyChangeSupport;

    public InCallServiceManager() {
        mPropertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void setInCallService(@Nullable InCallService inCallService) {
        InCallService oldInCallService = mInCallService;
        mInCallService = inCallService;
        mPropertyChangeSupport.firePropertyChange(
                new PropertyChangeEvent(
                        this, PROPERTY_IN_CALL_SERVICE, oldInCallService, inCallService));
    }

    /** Adds the observer {@link PropertyChangeListener} to the listener map. */
    public void addObserver(PropertyChangeListener propertyChangeListener) {
        mPropertyChangeSupport.addPropertyChangeListener(
                PROPERTY_IN_CALL_SERVICE, propertyChangeListener);
    }

    /** Removes the observer {@link PropertyChangeListener} from the listener map. */
    public void removeObserver (PropertyChangeListener propertyChangeListener) {
        mPropertyChangeSupport.removePropertyChangeListener(
                PROPERTY_IN_CALL_SERVICE, propertyChangeListener);
    }

    /**
     * Returns the {@link InCallService} instance.
     */
    @Nullable
    public InCallService getInCallService() {
        return mInCallService;
    }

    public boolean getMuted() {
        L.d(TAG, "getMuted");
        if (mInCallService == null) {
            return false;
        }
        CallAudioState audioState = mInCallService.getCallAudioState();
        return audioState != null && audioState.isMuted();
    }

    public void setMuted(boolean muted) {
        L.d(TAG, "setMuted: %b", muted);
        if (mInCallService == null) {
            return;
        }
        mInCallService.setMuted(muted);
    }

    public int getSupportedAudioRouteMask() {
        L.d(TAG, "getSupportedAudioRouteMask");

        CallAudioState audioState = getCallAudioStateOrNull();
        return audioState != null ? audioState.getSupportedRouteMask() : 0;
    }

    /**
     * Returns a list of supported CallAudioRoute for the given {@link CallDetail}.
     */
    public List<Integer> getSupportedAudioRoute(CallDetail callDetail) {
        List<Integer> audioRouteList = new ArrayList<>();
        if (callDetail == null) {
            return audioRouteList;
        }

        if (callDetail.isBluetoothCall()) {
            // If this is bluetooth phone call, we can only select audio route between vehicle
            // and phone.
            // Vehicle speaker route.
            audioRouteList.add(CallAudioState.ROUTE_BLUETOOTH);
            // Headset route.
            audioRouteList.add(CallAudioState.ROUTE_EARPIECE);
        } else {
            // Most likely we are making phone call with on board SIM card.
            int supportedAudioRouteMask = getSupportedAudioRouteMask();

            if ((supportedAudioRouteMask & CallAudioState.ROUTE_EARPIECE) != 0) {
                audioRouteList.add(CallAudioState.ROUTE_EARPIECE);
            } else if ((supportedAudioRouteMask & CallAudioState.ROUTE_WIRED_HEADSET) != 0) {
                audioRouteList.add(CallAudioState.ROUTE_WIRED_HEADSET);
            }
            if ((supportedAudioRouteMask & CallAudioState.ROUTE_BLUETOOTH) != 0) {
                audioRouteList.add(CallAudioState.ROUTE_BLUETOOTH);
            }
            if ((supportedAudioRouteMask & CallAudioState.ROUTE_SPEAKER) != 0) {
                audioRouteList.add(CallAudioState.ROUTE_SPEAKER);
            }
        }

        return audioRouteList;
    }

    /**
     * Returns the current audio route given the SCO state. See {@link CallDetail#getScoState()}.
     * The available routes are defined in {@link CallAudioState}.
     */
    public int getAudioRoute(int scoState) {
        if (scoState != CallDetail.STATE_AUDIO_ERROR) {
            if (scoState == CallDetail.STATE_AUDIO_CONNECTED) {
                return CallAudioState.ROUTE_BLUETOOTH;
            } else {
                return CallAudioState.ROUTE_EARPIECE;
            }
        } else {
            CallAudioState audioState = getCallAudioStateOrNull();
            int audioRoute = audioState != null ? audioState.getRoute() : 0;
            L.d(TAG, "getAudioRoute: %d", audioRoute);
            return audioRoute;
        }
    }

    /**
     * Re-route the audio out phone of the ongoing phone call.
     */
    public void setAudioRoute(int audioRoute, Call primaryCall) {
        if (primaryCall == null) {
            return;
        }

        boolean isConference = !primaryCall.getChildren().isEmpty()
                && primaryCall.getDetails().hasProperty(Call.Details.PROPERTY_CONFERENCE);
        Call call = isConference ? primaryCall.getChildren().get(0) : primaryCall;

        // For bluetooth call, we need to send the sco call events for hfp client to handle.
        if (audioRoute == CallAudioState.ROUTE_BLUETOOTH) {
            call.sendCallEvent(EVENT_SCO_CONNECT, null);
            setMuted(false);
        } else if ((audioRoute & CallAudioState.ROUTE_WIRED_OR_EARPIECE) != 0) {
            call.sendCallEvent(EVENT_SCO_DISCONNECT, null);
        }
        // The following doesn't really switch audio route for a bluetooth call. The api works only
        //  if the call is non bluetooth call(a self managed call for example).
        if (mInCallService != null) {
            mInCallService.setAudioRoute(audioRoute);
        }
    }

    private CallAudioState getCallAudioStateOrNull() {
        return mInCallService != null ? mInCallService.getCallAudioState() : null;
    }
}
