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

import android.telecom.Call;

import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;

/**
 * Comparator used by the {@link InCallModel} to order a list of calls
 */
public class CallComparator implements Comparator<Call> {
    /**
     * The rank of call state. Used for sorting active calls. Rank is listed from lowest to
     * highest.
     */
    private static final List<Integer> CALL_STATE_RANK = Lists.newArrayList(
            Call.STATE_RINGING,
            Call.STATE_DISCONNECTED,
            Call.STATE_DISCONNECTING,
            Call.STATE_NEW,
            Call.STATE_CONNECTING,
            Call.STATE_SELECT_PHONE_ACCOUNT,
            Call.STATE_HOLDING,
            Call.STATE_ACTIVE,
            Call.STATE_DIALING);

    @Override
    public int compare(Call call, Call otherCall) {
        boolean callHasParent = call.getParent() != null;
        boolean otherCallHasParent = otherCall.getParent() != null;

        if (callHasParent && !otherCallHasParent) {
            return 1;
        } else if (!callHasParent && otherCallHasParent) {
            return -1;
        }
        int carCallRank = CALL_STATE_RANK.indexOf(call.getDetails().getState());
        int otherCarCallRank = CALL_STATE_RANK.indexOf(otherCall.getDetails().getState());

        return otherCarCallRank - carCallRank;
    }
}
