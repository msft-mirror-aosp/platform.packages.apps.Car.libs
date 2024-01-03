/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.car.telephony.common;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.telecom.Call;
import android.telecom.DisconnectCause;
import android.telecom.GatewayInfo;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

/**
 * Represents details of {@link Call.Details}.
 */
public class CallDetail {
    /**
     * Extra that should correspond to a {@link String} value in the {@link
     * Call.Details#getExtras()} if a VOIP call provides it. The value should represent the
     * current speaker in the call.
     */
    private static final String EXTRA_CURRENT_SPEAKER = "android.telecom.extra.CURRENT_SPEAKER";

    /**
     * Extra that should correspond to an {@link Integer} value in the {@link
     * Call.Details#getExtras()} if a VOIP call provides it. The value should represent the
     * number of participants in a call.
     */
    private static final String EXTRA_PARTICIPANT_COUNT = "android.telecom.extra.PARTICIPANT_COUNT";

    /**
     * Extra that should correspond to a {@link Uri} value in the {@link Call.Details#getExtras()}
     * if a VOIP call provides it. The value can be a resource/content provider URI or a web
     * image to be displayed in the in-call view.
     */
    private static final String EXTRA_CALL_IMAGE_URI = "android.telecom.extra.CALL_IMAGE_URI";

    /**
     * Extra that should correspond to a `boolean` value in the {@link Call.Details#getExtras()}
     * if a VOIP call provides it. If the value is true then we will use the local microphone
     * call silence for our mute button toggling rather than the default of global microphone
     * call silence.
     */
    private static final String EXTRA_TELECOM_USE_LOCAL_CALL_SILENCE_CAPABILITY =
            "android.telecom.extra.USE_LOCAL_CALL_SILENCE_CAPABILITY";

    /**
     * Extra with a boolean value that indicates the current call microphone silence state. This is
     * only relevant for apps that declare the
     * {@link #EXTRA_TELECOM_USE_LOCAL_CALL_SILENCE_CAPABILITY}.
     */
    private static final String EXTRA_LOCAL_CALL_SILENCE_STATE = "android.telecom.extra"
            + ".LOCAL_CALL_SILENCE_STATE";

    /**
     * Event sent to request a change to the call silence state. Will be packaged with the
     * {@link #EXTRA_LOCAL_CALL_SILENCE_STATE} and a boolean value.
     *
     * <p>This does not guarantee that the silence state will change, that is determined by the VOIP
     * app hosting the call.
     */
    private static final String EVENT_LOCAL_CALL_SILENCE_STATE_CHANGED =
            "android.telecom.event.LOCAL_CALL_SILENCE_STATE_CHANGED";

    private static final String EXTRA_SCO_STATE = "com.android.bluetooth.hfpclient.SCO_STATE";
    private static final String[] HFP_CLIENT_CONNECTION_SERVICE_CLASS_NAMES = new String[]{
            /* S= */"com.android.bluetooth.hfpclient.connserv.HfpClientConnectionService",
            /* T= */"com.android.bluetooth.hfpclient.HfpClientConnectionService"};
    private static final int EXTRA_INTEGER_INVALID = -1;
    public static final int STATE_AUDIO_ERROR = EXTRA_INTEGER_INVALID;
    public static final int STATE_AUDIO_DISCONNECTED = 0;
    public static final int STATE_AUDIO_CONNECTING = 1;
    public static final int STATE_AUDIO_CONNECTED = 2;

    private Call mCall = null;
    @Nullable
    private final Call.Details mCallDetails;
    private final String mNumber;
    private final CharSequence mDisconnectCause;
    private final Uri mGatewayInfoOriginalAddress;
    private final long mConnectTimeMillis;
    private final boolean mIsConference;
    private final PhoneAccountHandle mPhoneAccountHandle;
    private final String mCallingAppPackageName;
    private final int mScoState;
    private final String mCallerDisplayName;
    private final boolean mIsSelfManaged;
    private final int mParticipantCount;
    private final String mCurrentSpeaker;
    private final Uri mCallerImageUri;
    // The VoIP app can set the component name of the activity that hosts its own in call view.
    private final ComponentName mInCallViewComponentName;
    private final boolean mHasLocalSilenceCapability;
    private final boolean mLocalSilenceState;

    private CallDetail(@NonNull Call call) {
        this(call.getDetails());
        mCall = call;
    }

    /**
     * @deprecated It is now deprecated and will be removed in a following change. When remove
     * it, make Call object final and NonNull.
     */
    @Deprecated
    private CallDetail(@Nullable Call.Details callDetails) {
        mCallDetails = callDetails;
        mNumber = getNumber(mCallDetails);
        mDisconnectCause = getDisconnectCause(mCallDetails);
        mGatewayInfoOriginalAddress = getGatewayInfoOriginalAddress(mCallDetails);
        mConnectTimeMillis = getConnectTimeMillis(mCallDetails);
        mIsConference = isConferenceCall(mCallDetails);
        mPhoneAccountHandle = getPhoneAccountHandle(mCallDetails);
        mCallingAppPackageName =
                (mPhoneAccountHandle == null || mPhoneAccountHandle.getComponentName() == null)
                        ? null : mPhoneAccountHandle.getComponentName().getPackageName();
        mScoState = getIntegerExtra(mCallDetails, EXTRA_SCO_STATE);
        mCallerDisplayName = getCallerDisplayName(mCallDetails);
        mIsSelfManaged = isSelfManagedCall(mCallDetails);
        mParticipantCount = getIntegerExtra(mCallDetails, EXTRA_PARTICIPANT_COUNT);
        mCallerImageUri = getParcelable(mCallDetails, EXTRA_CALL_IMAGE_URI);
        mCurrentSpeaker = getStringExtra(mCallDetails, EXTRA_CURRENT_SPEAKER);
        mInCallViewComponentName = getParcelable(mCallDetails, Intent.EXTRA_COMPONENT_NAME);
        mHasLocalSilenceCapability = getBooleanExtra(mCallDetails,
                EXTRA_TELECOM_USE_LOCAL_CALL_SILENCE_CAPABILITY);
        mLocalSilenceState = mHasLocalSilenceCapability ? getBooleanExtra(mCallDetails,
                EXTRA_LOCAL_CALL_SILENCE_STATE) : false;
    }

    /**
     * Returns if the call supports the given capability.
     */
    public boolean can(int capability) {
        return mCallDetails != null && mCallDetails.can(capability);
    }

    /**
     * Creates an instance of {@link CallDetail} from a {@link Call}.
     */
    public static CallDetail fromTelecomCall(@NonNull Call call) {
        return new CallDetail(call);
    }

    /**
     * Creates an instance of {@link CallDetail} from a {@link Call.Details}.
     *
     * @deprecated It is now deprecated and will be removed in a following change.
     */
    @Deprecated
    public static CallDetail fromTelecomCallDetail(@Nullable Call.Details callDetail) {
        return new CallDetail(callDetail);
    }

    /**
     * Returns the phone number. Returns empty string if phone number is not available.
     */
    @NonNull
    public String getNumber() {
        return mNumber;
    }

    /**
     * Returns a descriptive reason of disconnect cause.
     */
    @Nullable
    public CharSequence getDisconnectCause() {
        return mDisconnectCause;
    }

    /**
     * Returns the address that the user is trying to connect to via the gateway.
     */
    @Nullable
    public Uri getGatewayInfoOriginalAddress() {
        return mGatewayInfoOriginalAddress;
    }

    /**
     * Returns the timestamp when the call is connected. Returns 0 if detail is not available.
     */
    public long getConnectTimeMillis() {
        return mConnectTimeMillis;
    }

    /**
     * Returns whether the call is a conference.
     */
    public boolean isConference() {
        return mIsConference;
    }

    /**
     * Returns if the call is a self managed call.
     */
    public boolean isSelfManaged() {
        return mIsSelfManaged;
    }

    /**
     * Returns the SCO state of the call.
     */
    public int getScoState() {
        return mScoState;
    }

    /**
     * Returns the caller display name.
     */
    @Nullable
    public String getCallerDisplayName() {
        return mCallerDisplayName;
    }

    /**
     * Returns the caller image uri for a VoIP call if any.
     */
    @Nullable
    public Uri getCallerImageUri() {
        return mCallerImageUri;
    }

    /**
     * Returns the current speaker for a VoIP call if any.
     */
    @Nullable
    public String getCurrentSpeaker() {
        return mCurrentSpeaker;
    }

    /**
     * Returns the participant count for a VoIP call if any.
     */
    public int getParticipantCount() {
        return mParticipantCount;
    }

    /**
     * Returns the component name for activity that hosts the VoIP in call view.
     */
    public ComponentName getInCallViewComponentName() {
        return mInCallViewComponentName;
    }

    /**
     * Returns the {@link PhoneAccountHandle} for this call.
     */
    @Nullable
    public PhoneAccountHandle getPhoneAccountHandle() {
        return mPhoneAccountHandle;
    }

    /**
     * Returns the package name of the calling app for this call.
     */
    @Nullable
    public String getCallingAppPackageName() {
        return mCallingAppPackageName;
    }

    /**
     * Returns the id of the {@link PhoneAccountHandle} for this call.
     */
    @Nullable
    public String getPhoneAccountName() {
        return mPhoneAccountHandle == null ? null : mPhoneAccountHandle.getId();
    }

    /**
     * Returns if the call is a bluetooth call.
     */
    public boolean isBluetoothCall() {
        return Arrays.stream(HFP_CLIENT_CONNECTION_SERVICE_CLASS_NAMES).anyMatch(
                s -> s.equals(mPhoneAccountHandle.getComponentName().getClassName()));
    }

    /**
     * Returns the local silence state if the app declares the call has the capability of local
     * silence.
     */
    public boolean getLocalSilenceState() {
        return mLocalSilenceState;
    }

    /**
     * Set the local silence state if the app declares the capability. See
     * {@link #EVENT_LOCAL_CALL_SILENCE_STATE_CHANGED}.
     *
     * @return True if the app declares the local silence capability.
     */
    public boolean setLocalSilenceState(boolean localSilenceState) {
        if (mHasLocalSilenceCapability) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(EXTRA_LOCAL_CALL_SILENCE_STATE, localSilenceState);
            sendCallEvent(EVENT_LOCAL_CALL_SILENCE_STATE_CHANGED, bundle);
            return true;
        }
        return false;
    }

    private void sendCallEvent(String event, Bundle extras) {
        if (mCall != null) {
            mCall.sendCallEvent(event, extras);
        }
    }

    private static String getNumber(Call.Details callDetail) {
        String number = "";
        if (callDetail == null) {
            return number;
        }

        GatewayInfo gatewayInfo = callDetail.getGatewayInfo();
        if (gatewayInfo != null) {
            number = gatewayInfo.getOriginalAddress().getSchemeSpecificPart();
        } else if (callDetail.getHandle() != null) {
            number = callDetail.getHandle().getSchemeSpecificPart();
        }
        return number;
    }

    @Nullable
    private static CharSequence getDisconnectCause(Call.Details callDetail) {
        DisconnectCause cause = callDetail == null ? null : callDetail.getDisconnectCause();
        return cause == null ? null : cause.getLabel();
    }

    @Nullable
    private static Uri getGatewayInfoOriginalAddress(Call.Details callDetail) {
        return callDetail != null && callDetail.getGatewayInfo() != null
                ? callDetail.getGatewayInfo().getOriginalAddress()
                : null;
    }

    private static long getConnectTimeMillis(Call.Details callDetail) {
        return callDetail == null ? 0 : callDetail.getConnectTimeMillis();
    }

    private static boolean isConferenceCall(Call.Details callDetail) {
        return callDetail != null && callDetail.hasProperty(Call.Details.PROPERTY_CONFERENCE);
    }

    private static boolean isSelfManagedCall(Call.Details callDetail) {
        return callDetail != null && callDetail.hasProperty(Call.Details.PROPERTY_SELF_MANAGED);
    }

    @Nullable
    private static PhoneAccountHandle getPhoneAccountHandle(Call.Details callDetail) {
        return callDetail == null ? null : callDetail.getAccountHandle();
    }

    @Nullable
    private static String getCallerDisplayName(Call.Details callDetail) {
        if (callDetail != null) {
            String callerDisplayName = callDetail.getCallerDisplayName();
            if (!TextUtils.isEmpty(callerDisplayName)) {
                return callerDisplayName;
            }
            return callDetail.getContactDisplayName();
        }

        return null;
    }

    private static String getStringExtra(Call.Details callDetail, String key) {
        if (callDetail != null) {
            Bundle extras = callDetail.getExtras();
            if (extras != null && extras.containsKey(key)) {
                return extras.getString(key);
            }
        }
        return null;
    }

    private static boolean getBooleanExtra(Call.Details callDetail, String key) {
        if (callDetail != null) {
            Bundle extras = callDetail.getExtras();
            if (extras != null) {
                return extras.getBoolean(key, false);
            }
        }
        return false;
    }

    private static int getIntegerExtra(Call.Details callDetail, String key) {
        if (callDetail != null) {
            Bundle extras = callDetail.getExtras();
            if (extras != null && extras.containsKey(key)) {
                return extras.getInt(key);
            }
        }
        return EXTRA_INTEGER_INVALID;
    }

    private static <T extends Parcelable> T getParcelable(Call.Details callDetail, String key) {
        if (callDetail != null) {
            Bundle extras = callDetail.getExtras();
            if (extras != null && extras.containsKey(key)) {
                return extras.getParcelable(key);
            }
        }
        return null;
    }
}
