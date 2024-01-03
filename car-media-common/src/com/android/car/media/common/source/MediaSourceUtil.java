/*
 * Copyright 2018 The Android Open Source Project
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

package com.android.car.media.common.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.util.Log;

import com.android.car.media.common.R;

import java.util.Arrays;
import java.util.List;

/**
 *  Util class that houses logic related to media sources
 */
public class MediaSourceUtil {
    private static final String TAG = "MediaSourceUtil";

    private static final String ANDROIDX_CAR_APP_LAUNCHABLE = "androidx.car.app.launchable";

    private final List<String> mCustomMediaComponents;

    Context mContext;

    private PackageManager mPackageManager;

    public MediaSourceUtil(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mCustomMediaComponents = Arrays.asList(
                context.getResources().getStringArray(R.array.custom_media_packages));
    }

    /**
     * Checks if the media source is supported i.e. Audio only no video, browsers etc.
     */
    public boolean isAudioMediaSource(ComponentName mbsComponentName) {
        if (mCustomMediaComponents.contains(mbsComponentName.flattenToString())) {
            Log.d(TAG, "Custom media component " + mbsComponentName + " is supported");
            return true;
        }
        return isMediaTemplate(mbsComponentName);
    }

    /**
     * Determines if the given media component is supported through media templates
     */
    public boolean isMediaTemplate(ComponentName mbsComponentName) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Checking if Component " + mbsComponentName + " is a media template");
        }

        // check the metadata for opt in info
        Bundle metaData = getMbsMetadata(mbsComponentName);

        if (metaData != null && metaData.containsKey(ANDROIDX_CAR_APP_LAUNCHABLE)) {
            boolean launchable = metaData.getBoolean(ANDROIDX_CAR_APP_LAUNCHABLE);
            Log.d(TAG, "MBS for " + mbsComponentName
                    + " is opted " + (launchable ? "in" : "out"));
            return launchable;
        }

        Log.d(TAG, "No opt-in info found for Component " + mbsComponentName);

        // No explicit declaration. For backward compatibility, keep MBS only for audio apps
        String packageName = mbsComponentName.getPackageName();
        try {
            if (isLegacyMediaApp(packageName)) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Including " + mbsComponentName
                            + " for media template app " + packageName);
                }
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package " + packageName + " was not found");
        }
        Log.d(TAG, "Skipping MBS for " + mbsComponentName
                + " belonging to non media template app " + packageName);
        return false;
    }

    /**
     * Determines if it's a legacy media app that doesn't have a launcher activity
     */
    private boolean isLegacyMediaApp(String packageName)
            throws PackageManager.NameNotFoundException {
        // a media app doesn't have a launcher activity
        return mPackageManager.getLaunchIntentForPackage(packageName) == null;
    }

    /**
     * Finds the media browser service for the given mbs component and returns its metadata
     */
    private Bundle getMbsMetadata(ComponentName mbsComponentName) {
        Intent mediaIntent = new Intent();
        mediaIntent.setComponent(mbsComponentName);
        mediaIntent.setAction(MediaBrowserService.SERVICE_INTERFACE);
        List<ResolveInfo> mediaServices = mPackageManager.queryIntentServices(mediaIntent,
                PackageManager.GET_META_DATA);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "MBS info found for " + mbsComponentName + " : " + mediaServices);
        }
        // This has to be not null, if not NPE is appropriate
        return mediaServices.get(0).serviceInfo.metaData;
    }
}
