/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.android.car.ui.pluginsupport;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.android.car.ui.plugin.oemapis.PluginFactoryOEMV1;
import com.android.car.ui.plugin.oemapis.PluginVersionProviderOEMV1;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Helper class for accessing oem-apis without reflection.
 *
 * Because this class creates adapters, and adapters implement OEM APIs, this class cannot
 * be created with the app's classloader. You must use {@link AdapterClassLoader} to load it
 * so that both the app and plugin's classes can be loaded from this class.
 */
@SuppressWarnings("AndroidJdkLibsChecker")
final class OemApiUtil {

    private static final String TAG = "carui";

    /**
     * Given a plugin's context, return it's implementation of {@link PluginFactory}.
     *
     * This is done by asking the plugin's {@link PluginVersionProvider} for a
     * factory object, and checking if it's instanceof each version of
     * {@link PluginFactoryOEMV1}, casting to the correct one when found.
     *
     * @param pluginContext  The plugin's context. This context will return
     *                       the plugin's classloader from
     *                       {@link Context#getClassLoader()}.
     * @param appPackageName The package name of the application. This is passed to the plugin
     *                       so that it can provide unique customizations per-app.
     * @return A {@link PluginFactory}
     */
    @SuppressLint("PrivateApi")
    static PluginFactory getPluginFactory(
            Context pluginContext, String appPackageName) {

        Object oemVersionProvider = null;
        try {
            oemVersionProvider = Class
                    .forName("com.android.car.ui.plugin.PluginVersionProviderImpl")
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "PluginVersionProviderImpl not found.", e);
        } catch (ReflectiveOperationException e) {
            Log.e(TAG, "PluginVersionProviderImpl could not be instantiated!",
                    e.getCause() == null ? e : e.getCause());
        }

        // Add new version providers in an if-else chain here, in descending version order so
        // that higher versions are preferred.
        PluginVersionProvider versionProvider = null;
        if (oemVersionProvider instanceof PluginVersionProviderOEMV1) {
            versionProvider = new PluginVersionProviderAdapterV1(
                    (PluginVersionProviderOEMV1) oemVersionProvider);
        } else {
            Log.e(TAG, "PluginVersionProviderImpl was not instanceof any known "
                    + "versions of PluginVersionProviderOEMV#.");
        }

        PluginFactory oemPluginFactory = null;
        if (versionProvider != null) {
            SortedSet<OemApi> oemApis = getAvailableOemApis();
            int maxSupportedVersion = oemApis.stream().findFirst().get().version;
            Object factory = versionProvider.getPluginFactory(
                    maxSupportedVersion, pluginContext, appPackageName);
            for (OemApi oemApi : oemApis) {
                if (oemApi.oemFactoryClass != null && oemApi.oemFactoryClass.isInstance(factory)) {
                    oemPluginFactory = oemApi.getAdapter(factory);
                    break;
                }
            }
            if (oemPluginFactory == null) {
                Log.e(TAG, "PluginVersionProvider found, but did not provide a"
                        + " factory implementing any known interfaces!");
            }
        }

        return oemPluginFactory;
    }

    private OemApiUtil() {}

    private static SortedSet<OemApi> getAvailableOemApis() {
        SortedSet<OemApi> oemApis = new TreeSet<>();
        oemApis.add(new OemApi(
                "com.android.car.ui.plugin.oemapis.PluginFactoryOEMV1",
                PluginFactoryAdapterV1.class,
                1));
        oemApis.add(new OemApi(
                "com.android.car.ui.plugin.oemapis.PluginFactoryOEMV2",
                PluginFactoryAdapterV2.class,
                2));
        oemApis.add(new OemApi(
                "com.android.car.ui.plugin.oemapis.PluginFactoryOEMV3",
                PluginFactoryAdapterV3.class,
                3));
        oemApis.add(new OemApi(
                "com.android.car.ui.plugin.oemapis.PluginFactoryOEMV4",
                PluginFactoryAdapterV4.class,
                4));
        oemApis.add(new OemApi(
                "com.android.car.ui.plugin.oemapis.PluginFactoryOEMV5",
                PluginFactoryAdapterV5.class,
                5));
        oemApis.add(new OemApi(
                "com.android.car.ui.plugin.oemapis.PluginFactoryOEMV6",
                PluginFactoryAdapterV6.class,
                6));
        oemApis.add(new OemApi(
                "com.android.car.ui.plugin.oemapis.PluginFactoryOEMV7",
                PluginFactoryAdapterV7.class,
                7));
        return oemApis;
    }
}
