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

package com.android.car.ui.toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.ui.plugin.oemapis.toolbar.MenuItemOEMV1;
import com.android.car.ui.utils.CarUiUtils;

/**
 * Adapts a {@link com.android.car.ui.toolbar.MenuItem} into a
 * {@link com.android.car.ui.plugin.oemapis.toolbar.MenuItemOEMV1}
 */
public class MenuItemAdapterV1 {

    @Nullable
    private ToolbarControllerAdapterV1 mToolbarV1;
    @Nullable
    private ToolbarControllerAdapterV2 mToolbarV2;
    @Nullable
    private ToolbarControllerAdapterV3 mToolbarV3;
    @NonNull
    private final MenuItem mClientMenuItem;
    @NonNull
    private MenuItemOEMV1 mPluginMenuItem = MenuItemOEMV1.builder().build();

    @SuppressWarnings("FieldCanBeLocal") // Used with weak references
    private final MenuItem.Listener mClientListener = menuItem -> {
        updateMenuItem();
        updateMenuItems();
    };

    public MenuItemAdapterV1(@NonNull ToolbarControllerAdapterV1 toolbar, @NonNull MenuItem item) {
        mToolbarV1 = toolbar;
        mClientMenuItem = item;
        item.setListener(mClientListener);
        updateMenuItem();
    }

    public MenuItemAdapterV1(@NonNull ToolbarControllerAdapterV2 toolbar, @NonNull MenuItem item) {
        mToolbarV2 = toolbar;
        mClientMenuItem = item;
        item.setListener(mClientListener);
        updateMenuItem();
    }

    public MenuItemAdapterV1(@NonNull ToolbarControllerAdapterV3 toolbar, @NonNull MenuItem item) {
        mToolbarV3 = toolbar;
        mClientMenuItem = item;
        item.setListener(mClientListener);
        updateMenuItem();
    }

    private void updateMenuItems() {
        if (mToolbarV3 != null) {
            mToolbarV3.updateMenuItems();
        } else if (mToolbarV2 != null) {
            mToolbarV2.updateMenuItems();
        } else if (mToolbarV1 != null) {
            mToolbarV1.updateMenuItems();
        }
    }

    // Recreates mPluginMenuItem from mClientMenuItem
    private void updateMenuItem() {
        MenuItemOEMV1.Builder builder = mPluginMenuItem.copy()
                .setKey(mClientMenuItem.hashCode())
                .setTitle(CarUiUtils.charSequenceToString(mClientMenuItem.getTitle()))
                .setIcon(mClientMenuItem.getIcon())
                .setEnabled(mClientMenuItem.isEnabled())
                .setPrimary(mClientMenuItem.isPrimary())
                .setRestricted(mClientMenuItem.isRestricted())
                .setShowIconAndTitle(mClientMenuItem.isShowingIconAndTitle())
                .setDisplayBehavior(convertDisplayBehavior(mClientMenuItem.getDisplayBehavior()));

        if (mClientMenuItem.isCheckable()) {
            builder.setCheckable(true)
                    .setChecked(mClientMenuItem.isChecked());
        }

        if (mClientMenuItem.isActivatable()) {
            builder.setActivatable(true)
                    .setActivated(mClientMenuItem.isActivated());
        }

        MenuItem.OnClickListener onClickListener = mClientMenuItem.getOnClickListener();
        if (onClickListener != null || mClientMenuItem.isActivatable()
                || mClientMenuItem.isCheckable() || mClientMenuItem.isRestricted()) {
            builder.setOnClickListener(mClientMenuItem::performClick);
        } else {
            builder.setOnClickListener(null);
        }

        mPluginMenuItem = builder.build();
    }

    @NonNull
    public MenuItemOEMV1 getPluginMenuItem() {
        return mPluginMenuItem;
    }

    @NonNull
    public MenuItem getClientMenuItem() {
        return mClientMenuItem;
    }

    public boolean isVisible() {
        return mClientMenuItem.isVisible();
    }

    private static int convertDisplayBehavior(MenuItem.DisplayBehavior displayBehavior) {
        switch (displayBehavior) {
            case ALWAYS:
                return MenuItemOEMV1.DISPLAY_BEHAVIOR_ALWAYS;
            case NEVER:
                return MenuItemOEMV1.DISPLAY_BEHAVIOR_NEVER;
            default:
                throw new IllegalArgumentException("Unknown display behavior!");
        }
    }
}
