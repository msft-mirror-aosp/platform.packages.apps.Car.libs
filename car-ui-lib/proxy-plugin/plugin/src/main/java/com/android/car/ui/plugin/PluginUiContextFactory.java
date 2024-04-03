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

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.UiContext;

import com.chassis.car.ui.plugin.CarUiProxyLayoutInflaterFactory;

import java.lang.ref.WeakReference;

/**
 * This class maintains plugin ui contexts per app, which are used to inflate views with the plugin.
 */
public final class PluginUiContextFactory {
    /** The singular context generated from the plugin package in {@code PluginFactorySingleton}. */
    private final Context mPluginContext;
    /** The most recently created/referenced plugin ui context. */
    private WeakReference<Context> mRecentUiContext = null;

    public PluginUiContextFactory(@NonNull Context pluginContext) {
        mPluginContext = pluginContext;
    }

    /**
     * Returns the most recently referenced plugin ui context from {@code getPluginUiContext}. This
     * is used by PluginFactoryImplV# to obtain a relevant ui context without a source context
     * (i.e., for createListItemAdapter which does not receive a context as a parameter).
     *
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

    /**
     * This method tries to return a ui context for usage in the plugin that has the same
     * configuration as the given source ui context.
     *
     * @param sourceContext A ui context, normally an Activity context.
     */
    public Context getPluginUiContext(@UiContext Context sourceContext) {
        Context pluginContext = new ContextWrapper(sourceContext) {
            @Override
            public Resources getResources() {
                return mPluginContext.getResources();
            }

            @Override
            public Object getSystemService(@NonNull String name) {
                if (LAYOUT_INFLATER_SERVICE.equals(name)) {
                    return mPluginContext.getSystemService(name);
                }

                return super.getSystemService(name);
            }
        };

        // Add a custom layout inflater that can handle things like CarUiTextView that is in the
        // layout files of the car-ui-lib static implementation
        LayoutInflater inflater = LayoutInflater.from(pluginContext);
        if (inflater.getFactory2() == null) {
            inflater.setFactory2(new CarUiProxyLayoutInflaterFactory());
        }

        mRecentUiContext = new WeakReference<>(pluginContext);

        return pluginContext;
    }
}
