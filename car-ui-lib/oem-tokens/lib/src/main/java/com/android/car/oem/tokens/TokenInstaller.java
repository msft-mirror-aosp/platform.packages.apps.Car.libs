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
package com.android.car.oem.tokens;

import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;

/**
 * TokenInstaller leverages {@link ContentProvider ContentProvider's} onCreate() methods, which are
 * "called for all registered content providers on the application main thread at application launch
 * time." This means we can use a content provider to register for Activity lifecycle callbacks
 * before any activities have started and load OEM values for design tokens.
 */
public class TokenInstaller extends ContentProvider {
    private static final String TAG = "TokenInstaller";

    // Apps against which we have already called register
    private static final HashSet<Application> sAppsRegistered = new HashSet<>();

    private static boolean hasAlreadyRegistered(@NonNull Application application) {
        synchronized (sAppsRegistered) {
            return !sAppsRegistered.add(application);
        }
    }

    private static boolean getThemeBoolean(@NonNull Activity activity, int attr) {
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{attr});

        try {
            return a.getBoolean(0, false);
        } finally {
            a.recycle();
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context == null || !(context.getApplicationContext() instanceof Application)) {
            Log.e(TAG, "TokenInstaller had a null context, unable to call register!"
                    + " Need app to call register by itself");
            return false;
        }

        Application application = (Application) context.getApplicationContext();
        register(application);

        return true;
    }

    /**
     * In some cases {@link TokenInstaller#onCreate} is called before the {@link Application}
     * instance is created. In those cases applications have to call this method separately
     * after the Application instance is fully initialized.
     */
    public void register(@NonNull Application application) {
        if (hasAlreadyRegistered(application)) {
            return;
        }

        application.registerActivityLifecycleCallbacks(
                new Application.ActivityLifecycleCallbacks() {
                    private boolean shouldRun(Activity activity) {
                        return getThemeBoolean(activity, R.attr.oemTokenOverrideEnabled);
                    }

                    @Override
                    public void onActivityPreCreated(@NonNull Activity activity,
                            @Nullable Bundle savedInstanceState) {
                        if (!shouldRun(activity)) {
                            return;
                        }

                        int useOemTokenId = activity.getResources().getIdentifier(
                                "enable_oem_tokens",
                                "bool", Token.getTokenSharedLibraryName());
                        boolean useOemToken =
                                useOemTokenId != 0 && activity.getResources().getBoolean(
                                        useOemTokenId);

                        if (useOemToken && Token.isTokenSharedLibInstalled(
                                activity.getPackageManager())) {
                            Log.i(TAG, "Setting OEM token values");
                            activity.getTheme().applyStyle(R.style.OemTokens, true);
                            int oemStyleOverride = activity.getResources().getIdentifier("OemStyle",
                                    "style", Token.getTokenSharedLibraryName());
                            if (oemStyleOverride == 0) {
                                Log.e(TAG,
                                        "Unable to apply OEM design token overrides. Style with "
                                                + "name OemStyle not found.");
                            }
                            activity.getTheme().applyStyle(oemStyleOverride, true);
                        }
                    }

                    @Override
                    public void onActivityCreated(@NonNull Activity activity,
                            @Nullable Bundle savedInstanceState) {

                    }

                    @Override
                    public void onActivityStarted(@NonNull Activity activity) {

                    }

                    @Override
                    public void onActivityResumed(@NonNull Activity activity) {

                    }

                    @Override
                    public void onActivityPaused(@NonNull Activity activity) {

                    }

                    @Override
                    public void onActivityStopped(@NonNull Activity activity) {

                    }

                    @Override
                    public void onActivitySaveInstanceState(@NonNull Activity activity,
                            @NonNull Bundle outState) {

                    }

                    @Override
                    public void onActivityDestroyed(@NonNull Activity activity) {

                    }
                });
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
            @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
            @Nullable String[] selectionArgs) {
        return 0;
    }
}
