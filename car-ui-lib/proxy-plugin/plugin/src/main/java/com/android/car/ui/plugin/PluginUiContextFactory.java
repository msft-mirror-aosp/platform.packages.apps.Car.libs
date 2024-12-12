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
package com.android.car.ui.plugin;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.chassis.car.ui.plugin.CarUiProxyLayoutInflaterFactory;
import com.chassis.car.ui.plugin.R;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class maintains plugin ui contexts per app, which are used to inflate views with the
 * plugin.
 */
public final class PluginUiContextFactory {
    /**
     * The singular context generated from the plugin package in {@code PluginFactorySingleton}.
     */
    private final Context mPluginContext;
    /**
     * The most recently created/referenced plugin ui context.
     */
    private WeakReference<Context> mRecentUiContext = null;
    /**
     * The most recently referenced app ui context.
     */
    private WeakReference<Context> mRecentAppUiContext = null;
    /**
     * A map from app contexts to their corresponding plugin ui contexts.
     */
    private final Map<Context, Context> mAppToPluginContextMap = new WeakHashMap<>();
    private final CarUiProxyLayoutInflaterFactory mCarUiProxyLayoutInflaterFactory;

    public PluginUiContextFactory(@NonNull Context pluginContext,
            @NonNull CarUiProxyLayoutInflaterFactory carUiProxyLayoutInflaterFactory) {
        mPluginContext = pluginContext;
        mCarUiProxyLayoutInflaterFactory = carUiProxyLayoutInflaterFactory;
    }

    /**
     * Returns the most recently referenced plugin ui context from {@code getPluginUiContext}. This
     * is used by PluginFactoryImplV# to obtain a relevant ui context without a source context
     * (i.e., for createListItemAdapter which does not receive a context as a parameter).
     * <p>
     * Note: list items are always used with a RecyclerView, so mRecentUiContext will be set in
     * createRecyclerView method, which should happen before createListItemAdapter.
     *
     * @throws IllegalStateException if mRecentUiContext is not initialized
     */
    @NonNull
    public Context getRecentPluginUiContext() throws IllegalStateException {
        if (mRecentUiContext == null) {
            throw new IllegalStateException(
                    "Method getRecentPluginUiContext cannot be called before getPluginUiContext");
        }
        return mRecentUiContext.get();
    }

    /** Returns true if the current system default attribute is lightTheme. */
    private static boolean isLightTheme(@NonNull Context context) {
        TypedValue value = new TypedValue();
        return context.getTheme().resolveAttribute(android.R.attr.isLightTheme,
                value, true)
                && value.data != 0;
    }

    /**
     * Returns the most recently referenced app ui context from {@code getPluginContext}. This is
     * used by CarUiProxyLayoutInflaterFactory to instantiate Rotary specific Views.
     *
     * @throws IllegalStateException if mRecentAppUiContext is not initialized
     */
    @NonNull
    public Context getRecentAppUiContext() throws IllegalStateException {
        if (mRecentAppUiContext == null) {
            throw new IllegalStateException(
                    "Method getRecentAppUiContext cannot be called before getPluginUiContext");
        }
        return mRecentAppUiContext.get();
    }

    /**
     * This method tries to return a ui context for usage in the plugin that has the same
     * configuration as the given source ui context.
     *
     * @param sourceContext A ui context, normally an Activity context.
     */
    @NonNull
    public Context getPluginUiContext(@NonNull Context sourceContext) {
        Context uiContext = mAppToPluginContextMap.get(sourceContext);
        if (uiContext == null) {
            uiContext = mPluginContext;
            if (!uiContext.isUiContext()) {
                uiContext = uiContext
                        .createWindowContext(sourceContext.getDisplay(), TYPE_APPLICATION, null);
            }
        }
        Configuration currentConfiguration = uiContext.getResources().getConfiguration();
        Configuration newConfiguration = sourceContext.getResources().getConfiguration();
        if (currentConfiguration.diff(newConfiguration) != 0) {
            uiContext = uiContext.createConfigurationContext(newConfiguration);
        }

        // Add a custom layout inflater that can handle things like CarUiTextView that is in the
        // layout files of the car-ui-lib static implementation
        LayoutInflater inflater = LayoutInflater.from(uiContext);
        if (inflater.getFactory2() == null) {
            inflater.setFactory2(mCarUiProxyLayoutInflaterFactory);
        }
        mAppToPluginContextMap.put(sourceContext, uiContext);
        mRecentUiContext = new WeakReference<>(uiContext);
        mRecentAppUiContext = new WeakReference<>(sourceContext);

        // Add required theme attributes to support OEM Design Tokens
        int oemStyleOverride = uiContext.getResources().getIdentifier("OemStyle",
                "style", "com.android.oem.tokens");
        if (oemStyleOverride == 0) {
            if (isLightTheme(uiContext)) {
                uiContext.getTheme().applyStyle(
                        R.style.OemTokensBaseLight, true);
            } else {
                uiContext.getTheme().applyStyle(
                        R.style.OemTokensBaseDark, true);
            }
        } else {
            uiContext.getTheme().applyStyle(
                    R.style.OemTokens, true);
            if (isLightTheme(uiContext)) {
                uiContext.getTheme().applyStyle(
                        R.style.OemTokensLight, true);
            } else {
                uiContext.getTheme().applyStyle(
                        R.style.OemTokensDark, true);
            }
            uiContext.getTheme().applyStyle(oemStyleOverride, true);
        }

        return new ContextWrapper(sourceContext) {
            @Override
            public Resources getResources() {
                return mAppToPluginContextMap.get(sourceContext).getResources();
            }

            @Override
            public Object getSystemService(@NonNull String name) {
                if (LAYOUT_INFLATER_SERVICE.equals(name)) {
                    return mAppToPluginContextMap.get(sourceContext).getSystemService(name);
                }

                return super.getSystemService(name);
            }
        };
    }
}
