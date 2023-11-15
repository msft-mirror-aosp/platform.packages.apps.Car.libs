/*
 * Copyright (C) 2023 The Android Open Source Project
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

import static android.content.pm.PackageManager.GET_RESOLVED_FILTER;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.android.car.telephony.common.CallDetail;
import com.android.car.telephony.common.CallerInfo;
import com.android.car.telephony.common.Contact;
import com.android.car.telephony.common.TelecomUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Provide a list of methods that can be used to present call in the in-call UIs such as in-call
 * view of Dialer and calling widget in Launcher.
 *
 * <p>By using the given methods to present calls, it can support self managed calls and it also
 * provide consistency among various in-call UIs.
 */
public class InCallUtil {
    private static final String TAG = "CD.InCallUtil";
    private static final String CAR_APP_SERVICE_INTERFACE = "androidx.car.app.CarAppService";
    private static final String CAR_APP_ACTIVITY_INTERFACE =
            "androidx.car.app.activity.CarAppActivity";
    /**
     * androidx.car.app.CarAppService.CATEGORY_CALLING_APP from androidx car app library.
     */
    private static final String CAR_APP_CATEGORY_CALLING = "androidx.car.app.category.CALLING";
    private final Context mContext;
    private final PackageManager mPackageManager;
    private final TelecomManager mTelecomManager;

    /**
     * Make it public to be able to initiate directly.
     */
    @Inject
    public InCallUtil(@ApplicationContext Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mTelecomManager = context.getSystemService(TelecomManager.class);
    }

    /**
     * Returns basic {@link CallerInfo} that presents the call.
     *
     * <p>When a contact lookup returns a {@link TelecomUtils.PhoneNumberInfo}, prefer metadata from
     * {@link CallDetail} over the {@link TelecomUtils.PhoneNumberInfo}.
     */
    public CallerInfo getCallerInfo(
            @NonNull CallDetail callDetail,
            @Nullable TelecomUtils.PhoneNumberInfo phoneNumberInfo) {
        String callerDisplayName = callDetail.getCallerDisplayName();
        String initials = TelecomUtils.getInitials(callerDisplayName);
        if (TextUtils.isEmpty(callerDisplayName)) {
            if (phoneNumberInfo != null) {
                callerDisplayName = phoneNumberInfo.getDisplayName();
                initials = phoneNumberInfo.getInitials();
            } else {
                String formattedNumber = TelecomUtils.getFormattedNumber(mContext,
                        callDetail.getNumber());
                callerDisplayName = formattedNumber;
                initials = TelecomUtils.getInitials(formattedNumber);
            }
        }

        Uri avatarUri = callDetail.getCallerImageUri();
        if (avatarUri == null && phoneNumberInfo != null) {
            avatarUri = phoneNumberInfo.getAvatarUri();
        }
        return new CallerInfo(callerDisplayName, initials, avatarUri);
    }

    /**
     * Returns basic {@link CallerInfo} that presents the call.
     *
     * <p>When a contact presents, prefer metadata from {@link CallDetail} over the {@link Contact}.
     */
    public CallerInfo getCallerInfo(@NonNull CallDetail callDetail,
                                    @Nullable Contact contact) {
        String formattedNumber = TelecomUtils.getFormattedNumber(mContext, callDetail.getNumber());
        String callerDisplayName = callDetail.getCallerDisplayName();
        String initials = TelecomUtils.getInitials(callerDisplayName);
        if (TextUtils.isEmpty(callerDisplayName)) {
            if (contact != null) {
                callerDisplayName = contact.getDisplayName();
                initials = contact.getInitials();
            } else {
                callerDisplayName = formattedNumber;
                initials = TelecomUtils.getInitials(formattedNumber);
            }
        }

        Uri avatarUri = callDetail.getCallerImageUri();
        if (avatarUri == null && contact != null) {
            avatarUri = contact.getAvatarUri();
        }
        return new CallerInfo(callerDisplayName, initials, avatarUri);
    }

    /**
     * Loads the avatar for the call using {@link Glide}. It requires "INTERNET" permission to load
     * the avatar uri set by the app.
     */
    public void loadAvatar(Context context, CallerInfo callerInfo,
                           CustomTarget<Drawable> target) {
        Glide.with(context)
                .load(callerInfo.getAvatarUri())
                .error(TelecomUtils.createLetterTile(mContext, callerInfo.getInitials(),
                        callerInfo.getCallerDisplayName()))
                .into(target);
    }

    /**
     * Returns the launch {@link Intent} that starts the in call view presenting the given
     * {@link Call}.
     */
    public Intent getInCallViewLaunchIntent(Call call) {
        Intent intent = null;
        CallDetail callDetail = CallDetail.fromTelecomCall(call);
        if (callDetail.isSelfManaged()) {
            ComponentName componentName = callDetail.getInCallViewComponentName();
            if (componentName != null) {
                intent = new Intent();
                intent.setComponent(componentName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                String callingAppPackageName = callDetail.getCallingAppPackageName();
                if (!TextUtils.isEmpty(callingAppPackageName)) {
                    if (isCarAppCallingService(callingAppPackageName)) {
                        intent = new Intent();
                        intent.setComponent(
                                new ComponentName(
                                        callingAppPackageName, CAR_APP_ACTIVITY_INTERFACE));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    } else {
                        intent = mPackageManager.getLaunchIntentForPackage(callingAppPackageName);
                    }
                }
            }
        } else {
            intent = mPackageManager.getLaunchIntentForPackage(
                    mTelecomManager.getDefaultDialerPackage());
        }
        return intent;
    }

    /**
     * Returns if the calling app is a CAL app with category {@code androidx.car.app.category
     * .CALLING}.
     */
    public boolean isCarAppCallingService(String packageName) {
        Intent intent =
                new Intent(CAR_APP_SERVICE_INTERFACE)
                        .setPackage(packageName)
                        .addCategory(CAR_APP_CATEGORY_CALLING);
        return !mPackageManager.queryIntentServices(intent, GET_RESOLVED_FILTER).isEmpty();
    }

    /**
     * Loads the app name and icon for the given package name.
     */
    public Pair<CharSequence, Drawable> getAppInfo(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            try {
                ApplicationInfo applicationInfo;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    applicationInfo = mPackageManager.getApplicationInfo(
                            packageName, PackageManager.ApplicationInfoFlags.of(0));
                } else {
                    applicationInfo = mPackageManager.getApplicationInfo(
                            packageName, /* flags= */ 0);
                }
                Drawable appIcon = mPackageManager.getApplicationIcon(applicationInfo);
                CharSequence appName = mPackageManager.getApplicationLabel(applicationInfo);
                return new Pair<>(appName, appIcon);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "No such package found " + packageName, e);
            }
        }
        return null;
    }
}
