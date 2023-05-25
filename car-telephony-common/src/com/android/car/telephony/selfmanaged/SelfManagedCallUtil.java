/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.car.telephony.selfmanaged;

import android.content.Context;
import android.telecom.Call;

import androidx.annotation.NonNull;

import com.android.car.telephony.R;
import com.android.car.ui.utils.CarUxRestrictionsUtil;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Utils to handle self managed calls.
 */
public class SelfManagedCallUtil {
    private final Context mContext;
    private final CarUxRestrictionsUtil mCarUxRestrictionsUtil;

    /**
     * Make it public to initiate directly.
     */
    @Inject
    public SelfManagedCallUtil(
            @ApplicationContext Context context, CarUxRestrictionsUtil carUxRestrictionsUtil) {
        mContext = context;
        mCarUxRestrictionsUtil = carUxRestrictionsUtil;
    }

    /**
     * Returns if should show the incall view built with CAL templates for the given {@link Call}.
     */
    public boolean shouldShowCalInCallView(@NonNull Call call) {
        return call.getDetails().hasProperty(Call.Details.PROPERTY_SELF_MANAGED)
                && canShowCalInCallView();
    }

    /**
     * Returns if can show the incall view built with CAL templates. True if the incall view
     * template is supported and the car is parked.
     */
    public boolean canShowCalInCallView() {
        return mContext.getResources().getBoolean(R.bool.feature_cal_support_incall_view)
                && !mCarUxRestrictionsUtil.getCurrentRestrictions()
                .isRequiresDistractionOptimization();
    }
}
