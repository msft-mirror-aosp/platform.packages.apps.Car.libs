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
package com.chassis.car.ui.plugin.toolbar;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.ui.plugin.oemapis.InsetsOEMV1;
import com.android.car.ui.plugin.oemapis.toolbar.ToolbarControllerOEMV1;
import com.android.car.ui.plugin.oemapis.toolbar.ToolbarControllerOEMV2;
import com.android.car.ui.plugin.oemapis.toolbar.ToolbarControllerOEMV3;
import com.android.car.ui.pluginsupport.PluginFactoryStub;
import com.android.car.ui.toolbar.ToolbarControllerImpl;

/**
 * Helper class that delegates installing base layout to car-ui-lib shared library.
 */
public final class BaseLayoutInstallerProxy {

    /**
     * Installs the base layout around the contentView. Optionally installs a toolbar and returns
     * an implementation of {@code ToolbarControllerOEMV1} if the toolbar is enabled.
     */
    @Nullable
    public static ToolbarControllerOEMV1 installBaseLayoutAroundV1(
            @NonNull Context pluginContext,
            @NonNull View contentView,
            @Nullable java.util.function.Consumer<InsetsOEMV1> insetsChangedListener,
            boolean toolbarEnabled,
            boolean fullscreen) {
        ToolbarControllerImpl toolbarControllerImpl = installBaseLayoutAround(
                pluginContext, contentView, insetsChangedListener != null
                        ? new InsetsChangedListenerProxy(insetsChangedListener) : null,
                toolbarEnabled, fullscreen);
        return !toolbarEnabled ? null : new ToolbarAdapterProxyV1(pluginContext,
                toolbarControllerImpl);
    }

    /**
     * Installs the base layout around the contentView. Optionally installs a toolbar and returns
     * an implementation of {@code ToolbarControllerOEMV2} if the toolbar is enabled.
     */
    @Nullable
    public static ToolbarControllerOEMV2 installBaseLayoutAroundV2(
            @NonNull Context pluginContext,
            @NonNull View contentView,
            @Nullable com.android.car.ui.plugin.oemapis.Consumer<InsetsOEMV1> insetsChangedListener,
            boolean toolbarEnabled,
            boolean fullscreen) {
        ToolbarControllerImpl toolbarControllerImpl = installBaseLayoutAround(
                pluginContext, contentView, insetsChangedListener != null
                        ? new InsetsChangedListenerProxy(insetsChangedListener) : null,
                toolbarEnabled, fullscreen);
        return !toolbarEnabled ? null : new ToolbarAdapterProxyV2(pluginContext,
                toolbarControllerImpl);
    }

    /**
     * Installs the base layout around the contentView. Optionally installs a toolbar and returns
     * an implementation of {@code ToolbarControllerOEMV3} if the toolbar is enabled.
     */
    @Nullable
    public static ToolbarControllerOEMV3 installBaseLayoutAroundV3(
            @NonNull Context pluginContext,
            @NonNull View contentView,
            @Nullable com.android.car.ui.plugin.oemapis.Consumer<InsetsOEMV1> insetsChangedListener,
            boolean toolbarEnabled,
            boolean fullscreen) {
        ToolbarControllerImpl toolbarControllerImpl = installBaseLayoutAround(
                pluginContext, contentView, insetsChangedListener != null
                        ? new InsetsChangedListenerProxy(insetsChangedListener) : null,
                toolbarEnabled, fullscreen);
        return !toolbarEnabled ? null : new ToolbarAdapterProxyV3(pluginContext,
                toolbarControllerImpl);
    }

    private static ToolbarControllerImpl installBaseLayoutAround(
            @NonNull Context pluginContext,
            @NonNull View contentView,
            @Nullable InsetsChangedListenerProxy insetsChangedListenerProxy,
            boolean toolbarEnabled,
            boolean fullscreen) {
        // Delegate installing base layout to PluginFactoryStub
        PluginFactoryStub pluginFactoryStub = new PluginFactoryStub();
        return (ToolbarControllerImpl) pluginFactoryStub.installBaseLayoutAround(pluginContext,
                contentView, insetsChangedListenerProxy, toolbarEnabled, fullscreen);
    }
}
