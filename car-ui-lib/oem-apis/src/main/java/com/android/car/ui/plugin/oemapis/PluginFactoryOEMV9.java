/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.android.car.ui.plugin.oemapis;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.ui.plugin.oemapis.appstyledview.AppStyledViewControllerOEMV4;
import com.android.car.ui.plugin.oemapis.preference.PreferenceOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.AdapterOEMV2;
import com.android.car.ui.plugin.oemapis.recyclerview.ListItemOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.RecyclerViewAttributesOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.RecyclerViewOEMV3;
import com.android.car.ui.plugin.oemapis.recyclerview.ViewHolderOEMV1;
import com.android.car.ui.plugin.oemapis.toolbar.ToolbarControllerOEMV3;

import java.util.List;

/**
 * This interface contains methods to create customizable Car UI components.
 * <p>
 * It returns them as their OEM-versioned interfaces (i.e. ToolbarControllerOEMV3) and is versioned
 * itself so that no additional reflection or casting is necessary once the PluginFactory has
 * been created.
 * <p>
 * Multiple of these can be provided via {@link PluginVersionProviderOEMV1} to allow plugins
 * to provide an old implementation for old apps, and a newer implementation for newer apps.
 */
public interface PluginFactoryOEMV9 {
    /**
     * Gives the plugin access to two factories that will create FocusParkingViews and
     * FocusAreas. These views have their implementation in the static car-ui-lib.
     * <p>
     * When {@link #installBaseLayoutAround} creates a base layout, it should include a
     * FocusParkingView for rotary to work properly. If the base layout has a toolbar, it should
     * also be wrapped in a FocusArea.
     *
     * @param focusParkingViewFactory a function that will infinitely return new instances of
     *                                FocusParkingView
     * @param focusAreaFactory        a function that will infinitely return new instances of
     *                                FocusArea
     */
    void setRotaryFactories(
            Function<Context, FocusParkingViewOEMV1> focusParkingViewFactory,
            Function<Context, FocusAreaOEMV1> focusAreaFactory);

    /**
     * Creates the base layout, and optionally the toolbar.
     *
     * @param sourceContext The context that will end up using this component. This context must not
     *                      be used for inflating views, use the plugin context for that. This
     *                      is used for two purposes: to add the correct configuration to the plugin
     *                      context via {@code pluginContext.createConfigurationContext(
     *                      sourceContext.getResources().getConfiguration()} before inflating views,
     *                      and to pass to the rotary factories provided via
     *                      {@link #setRotaryFactories}.
     * @param contentView           The view to install the base layout around.
     * @param insetsChangedListener A method to call when the insets change.
     * @param toolbarEnabled        Whether or not to add a toolbar to the base layout.
     * @param fullscreen            Whether or not this base layout / toolbar is taking up the whole
     *                              screen. This can be used to decide whether or not to add
     *                              decorations around the edge of it.
     * @return A {@link ToolbarControllerOEMV3} or null if {@code toolbarEnabled} was false.
     */
    @Nullable
    ToolbarControllerOEMV3 installBaseLayoutAround(
            @NonNull Context sourceContext,
            @NonNull View contentView,
            @Nullable Consumer<InsetsOEMV1> insetsChangedListener,
            boolean toolbarEnabled,
            boolean fullscreen);

    /**
     * If implementation of the library would like to opt out of controlling the base layout and
     * subsequently the toolbar they can do so by returning false from this method.
     *
     * @return false if {@link #installBaseLayoutAround} should not be called for this library
     */
    boolean customizesBaseLayout();

    /**
     * Creates a preference.
     *
     * @param sourceContext The context that will end up using this component. This context must not
     *                      be used for inflating views, use the plugin context for that. This
     *                      is used for two purposes: to add the correct configuration to the plugin
     *                      context via {@code pluginContext.createConfigurationContext(
     *                      sourceContext.getResources().getConfiguration()} before inflating views,
     *                      and to pass to the rotary factories provided via
     *                      {@link #setRotaryFactories}.
     * @return A {@link PreferenceOEMV1} or null if not implemented.
     */
    @Nullable
    PreferenceOEMV1 createCarUiPreference(@NonNull Context sourceContext);

    /**
     * Creates a app styled view.
     *
     * @param sourceContext The context that will end up using this component. This context must not
     *                      be used for inflating views, use the plugin context for that. This
     *                      is used for two purposes: to add the correct configuration to the plugin
     *                      context via {@code pluginContext.createConfigurationContext(
     *                      sourceContext.getResources().getConfiguration()} before inflating views,
     *                      and to pass to the rotary factories provided via
     *                      {@link #setRotaryFactories}.
     * @return the view used for app styled view.
     */
    @Nullable
    AppStyledViewControllerOEMV4 createAppStyledView(@NonNull Context sourceContext);

    /**
     * Creates an instance of CarUiRecyclerView
     *
     * @param sourceContext The context that will end up using this component. This context must not
     *                      be used for inflating views, use the plugin context for that. This
     *                      is used for two purposes: to add the correct configuration to the plugin
     *                      context via {@code pluginContext.createConfigurationContext(
     *                      sourceContext.getResources().getConfiguration()} before inflating views,
     *                      and to pass to the rotary factories provided via
     *                      {@link #setRotaryFactories}.
     * @param attrs   An object containing initial attributes for the button.
     */
    @Nullable
    RecyclerViewOEMV3 createRecyclerView(
            @NonNull Context sourceContext,
            @Nullable RecyclerViewAttributesOEMV1 attrs);

    /**
     * Creates an instance of list item adapter
     */
    @Nullable
    AdapterOEMV2<? extends ViewHolderOEMV1> createListItemAdapter(
            @NonNull List<ListItemOEMV1> items);
}
