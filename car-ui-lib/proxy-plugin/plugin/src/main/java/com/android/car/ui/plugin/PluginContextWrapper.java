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

package com.android.car.ui.plugin;

import android.content.Context;
import android.content.ContextWrapper;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A wrapper class around the plugin context
 */
public class PluginContextWrapper extends ContextWrapper {

    @NonNull
    private final String mApplicationPackageName;
    @Nullable
    private WindowManager mWindowManager;

    public PluginContextWrapper(@NonNull Context pluginContext,
                                @NonNull String applicationPackageName) {
        super(pluginContext);
        mApplicationPackageName = applicationPackageName;
    }

    /**
     * Return this plugin context as the application context so that it doesn't return null when
     * called in the static implementation, for example, {@code MenuItem}
     */
    @Override
    public Context getApplicationContext() {
        return this;
    }

    /**
     * Return the application package name instead of the plugin package name because
     * {@code SearchResultsProvider} in static implementation needs application id for authority
     */
    @Override
    public String getPackageName() {
        return mApplicationPackageName;
    }

    /**
     * Sets the {@link WindowManager} instance for the plugin {@link Context}.
     * This was specifically needed for launching {@code Dialog}s.
     * @param windowManager needs to be attached to the main Activity
     */
    public void setWindowManager(@Nullable WindowManager windowManager) {
        mWindowManager = windowManager;
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (WINDOW_SERVICE.equals(name) && mWindowManager != null) {
            return mWindowManager;
        }
        return super.getSystemService(name);
    }
}
