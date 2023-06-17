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

import com.android.car.ui.plugin.oemapis.Consumer;
import com.android.car.ui.plugin.oemapis.InsetsOEMV1;
import com.android.car.ui.plugin.oemapis.toolbar.ToolbarControllerOEMV3;
import com.android.car.ui.pluginsupport.PluginFactoryStub;
import com.android.car.ui.toolbar.ToolbarControllerImpl;

/**
 * Helper class that delegates installing base layout to car-ui-lib shared library.
 */
public class BaseLayoutInstallerProxy {

    /**
     * Installs the base layout around the contentView.
     */
    @Nullable
    public static ToolbarControllerOEMV3 installBaseLayoutAround(
            @NonNull Context pluginContext,
            @NonNull View contentView,
            @Nullable Consumer<InsetsOEMV1> insetsChangedListener,
            boolean toolbarEnabled,
            boolean fullscreen) {

        InsetsChangedListenerProxy insetsChangedListenerProxy = insetsChangedListener != null
                ? new InsetsChangedListenerProxy(insetsChangedListener) : null;

        // Delegate installing base layout to PluginFactoryStub
        PluginFactoryStub pluginFactoryStub = new PluginFactoryStub();
        ToolbarControllerImpl toolbarControllerImpl =
                (ToolbarControllerImpl) pluginFactoryStub.installBaseLayoutAround(pluginContext,
                        contentView, insetsChangedListenerProxy, toolbarEnabled, fullscreen);

        return !toolbarEnabled ? null : new ToolbarAdapterProxy(pluginContext,
                toolbarControllerImpl);
    }
}
