/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.chassis.car.ui.sharedlibrary.toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.car.ui.sharedlibrary.oemapis.toolbar.MenuItemOEMV1;

import com.chassis.car.ui.sharedlibrary.R;

import java.util.Collections;
import java.util.List;

class OverflowMenuItem {

    @NonNull
    private final Context mActivityContext;

    @NonNull
    private List<MenuItemOEMV1> mOverflowMenuItems = Collections.emptyList();

    @Nullable
    private Dialog mDialog;

    private MenuItemOEMV1 mMenuItem;

    OverflowMenuItem(
            @NonNull Context sharedLibraryContext,
            @NonNull Context activityContext) {
        mActivityContext = activityContext;

        mMenuItem = MenuItemOEMV1.builder()
                .setTitle(sharedLibraryContext.getString(R.string.toolbar_menu_item_overflow_title))
                .setIcon(ContextCompat.getDrawable(
                        sharedLibraryContext, R.drawable.toolbar_menu_item_overflow))
                .setVisible(false)
                .setOnClickListener(() -> {
                    String[] titles = mOverflowMenuItems.stream()
                            .map(MenuItemOEMV1::getTitle)
                            .toArray(String[]::new);

                    mDialog = new AlertDialog.Builder(mActivityContext)
                            .setItems(titles, (dialog, which) -> {
                                Runnable onClickListener = mOverflowMenuItems.get(which)
                                        .getOnClickListener();
                                if (onClickListener != null) {
                                    onClickListener.run();
                                }
                                dialog.dismiss();
                            }).create();
                    mDialog.show();
                })
                .build();
    }

    public MenuItemOEMV1 getMenuItem() {
        return mMenuItem;
    }

    public void setOverflowMenuItems(List<MenuItemOEMV1> menuItems) {
        mOverflowMenuItems = menuItems;
        mMenuItem = mMenuItem.copy().setVisible(!menuItems.isEmpty()).build();

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
