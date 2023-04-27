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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.telecom.CallAudioState;
import android.telecom.InCallService;

import com.android.car.telephony.common.CallDetail;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class InCallServiceManagerTest {

    private static final int CALL_AUDIO_STATE_ROUTE_ALL =
            CallAudioState.ROUTE_EARPIECE
                    | CallAudioState.ROUTE_BLUETOOTH
                    | CallAudioState.ROUTE_WIRED_HEADSET
                    | CallAudioState.ROUTE_SPEAKER;

    @Mock InCallService mMockInCallService;
    private InCallServiceManager mInCallServiceManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mInCallServiceManager = new InCallServiceManager();
        mInCallServiceManager.setInCallService(mMockInCallService);
    }

    @Test
    public void testGetMuted_isMuted() {
        CallAudioState callAudioState = new CallAudioState(true,
                CallAudioState.ROUTE_BLUETOOTH, CALL_AUDIO_STATE_ROUTE_ALL);
        when(mMockInCallService.getCallAudioState()).thenReturn(callAudioState);

        assertThat(mInCallServiceManager.getMuted()).isTrue();
    }

    @Test
    public void testGetMuted_audioRouteIsNull() {
        when(mMockInCallService.getCallAudioState()).thenReturn(null);

        assertThat(mInCallServiceManager.getMuted()).isFalse();
    }

    @Test
    public void testGetMuted_InCallServiceIsNull() {
        mInCallServiceManager.setInCallService(null);

        assertThat(mInCallServiceManager.getMuted()).isFalse();
    }

    @Test
    public void testSetMuted() {
        mInCallServiceManager.setMuted(true);

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(mMockInCallService).setMuted(captor.capture());
        assertThat(captor.getValue()).isTrue();
    }

    @Test
    public void testGetSupportedAudioRouteMask() {
        CallAudioState callAudioState = new CallAudioState(
                true, CallAudioState.ROUTE_BLUETOOTH, CALL_AUDIO_STATE_ROUTE_ALL);
        when(mMockInCallService.getCallAudioState()).thenReturn(callAudioState);

        assertThat(mInCallServiceManager.getSupportedAudioRouteMask()).isEqualTo(
                CALL_AUDIO_STATE_ROUTE_ALL);
    }

    @Test
    public void testGetSupportedAudioRouteMask_InCallServiceIsNull() {
        mInCallServiceManager.setInCallService(null);

        assertThat(mInCallServiceManager.getSupportedAudioRouteMask()).isEqualTo(0);
    }

    @Test
    public void testGetSupportedAudioRoute_supportedAudioRouteMaskIs0() {
        // SupportedAudioRouteMask is 0.
        assertThat(mInCallServiceManager.getSupportedAudioRoute(null).size()).isEqualTo(0);
    }

    @Test
    public void testGetSupportedAudioRoute_supportedAudioRouteMaskIsRouteAll() {
        CallDetail mockCallDetail = mock(CallDetail.class);

        // SupportedAudioRouteMask is CallAudioState.ROUTE_ALL.
        CallAudioState callAudioState = new CallAudioState(
                true, CallAudioState.ROUTE_BLUETOOTH, CALL_AUDIO_STATE_ROUTE_ALL);
        when(mMockInCallService.getCallAudioState()).thenReturn(callAudioState);

        List<Integer> audioRoutes = mInCallServiceManager.getSupportedAudioRoute(mockCallDetail);
        assertThat(audioRoutes.size()).isEqualTo(3);
        assertThat(audioRoutes.contains(CallAudioState.ROUTE_EARPIECE)).isTrue();
        assertThat(audioRoutes.contains(CallAudioState.ROUTE_SPEAKER)).isTrue();
        assertThat(audioRoutes.contains(CallAudioState.ROUTE_BLUETOOTH)).isTrue();
    }

    @Test
    public void testGetSupportedAudioRoute_supportedAudioRouteMaskIsRouteSpeaker() {
        CallDetail mockCallDetail = mock(CallDetail.class);

        // SupportedAudioRouteMask is CallAudioState.ROUTE_SPEAKER.
        CallAudioState callAudioState = new CallAudioState(
                true, CallAudioState.ROUTE_SPEAKER, CallAudioState.ROUTE_SPEAKER);
        when(mMockInCallService.getCallAudioState()).thenReturn(callAudioState);

        List<Integer> audioRoutes = mInCallServiceManager.getSupportedAudioRoute(mockCallDetail);
        assertThat(audioRoutes.size()).isEqualTo(1);
        assertThat(audioRoutes.get(0)).isEqualTo(CallAudioState.ROUTE_SPEAKER);
    }

    @Test
    public void bluetoothCall_getSupportedAudioRoute() {
        CallDetail mockCallDetail = mock(CallDetail.class);
        when(mockCallDetail.isBluetoothCall()).thenReturn(true);

        List<Integer> supportedAudioRoute = mInCallServiceManager.getSupportedAudioRoute(
                mockCallDetail);
        assertThat(supportedAudioRoute.get(0)).isEqualTo(CallAudioState.ROUTE_BLUETOOTH);
        assertThat(supportedAudioRoute.get(1)).isEqualTo(CallAudioState.ROUTE_EARPIECE);
    }

    @Test
    public void scoStateToAudioRoute() {
        CallAudioState callAudioState = new CallAudioState(
                true, CallAudioState.ROUTE_SPEAKER, CALL_AUDIO_STATE_ROUTE_ALL);
        when(mMockInCallService.getCallAudioState()).thenReturn(callAudioState);

        assertThat(mInCallServiceManager.getAudioRoute(CallDetail.STATE_AUDIO_CONNECTED))
                .isEqualTo(CallAudioState.ROUTE_BLUETOOTH);
        assertThat(mInCallServiceManager.getAudioRoute(CallDetail.STATE_AUDIO_CONNECTING))
                .isEqualTo(CallAudioState.ROUTE_EARPIECE);
        assertThat(mInCallServiceManager.getAudioRoute(CallDetail.STATE_AUDIO_DISCONNECTED))
                .isEqualTo(CallAudioState.ROUTE_EARPIECE);
        assertThat(mInCallServiceManager.getAudioRoute(CallDetail.STATE_AUDIO_ERROR))
                .isEqualTo(CallAudioState.ROUTE_SPEAKER);
    }

}
