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

package com.android.car.ui.appstyledview;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.car.ui.appstyledview.AppStyledViewController.AppStyledViewNavIcon;
import com.android.car.ui.pluginsupport.PluginFactorySingleton;

import java.util.Objects;

/**
 * Controller to interact with the app styled view UI.
 * <p>
 * Rendered views will comply with
 * <a href="https://source.android.com/devices/automotive/hmi/car_ui/appendix_b">customization guardrails</a>
 */
public final class AppStyledDialogController {
    @NonNull
    private AppStyledViewController mAppStyledViewController;
    @NonNull
    private AppStyledDialog mDialog;

    public AppStyledDialogController(@NonNull Activity context) {
        Objects.requireNonNull(context);
        mAppStyledViewController = PluginFactorySingleton.get(context)
                .createAppStyledView(context);
        mDialog = new AppStyledDialog(context, mAppStyledViewController);
    }

    /**
     * @deprecated Use {@link #AppStyledDialogController(Activity)} instead
     */
    @Deprecated
    public AppStyledDialogController(@NonNull Context context) {
        Objects.requireNonNull(context);

        if (context instanceof Activity) {
            throw new IllegalArgumentException();
        }

        mAppStyledViewController = PluginFactorySingleton.get(context)
                .createAppStyledView(context);
        mDialog = new AppStyledDialog((Activity) context, mAppStyledViewController);
    }

    @VisibleForTesting
    void setAppStyledViewController(AppStyledViewController controller, Activity context) {
        mAppStyledViewController = controller;
        mDialog = new AppStyledDialog(context, mAppStyledViewController);
    }

    /**
     * Sets the content view to be displayed in AppStyledView.
     */
    public void setContentView(@NonNull View contentView) {
        Objects.requireNonNull(contentView);
        mDialog.setContent(contentView);
    }

    /**
     * Returns the content view to be displayed in AppStyledView.
     */
    @Nullable
    public View getContentView() {
        return mDialog.getContent();
    }

    /**
     * Sets the nav icon to be used.
     */
    public void setNavIcon(@AppStyledViewNavIcon int navIcon) {
        mAppStyledViewController.setNavIcon(navIcon);
    }

    /**
     * Displays the dialog to the user with the custom view provided by the app.
     */
    public void show() {
        mDialog.show();
    }

    /**
     * Dismiss this dialog, removing it from the screen. This method can be invoked safely from any
     * thread.
     */
    public void dismiss() {
        mDialog.dismiss();
    }

    /**
     * Sets a runnable that will be invoked when a nav icon is clicked.
     */
    public void setOnNavIconClickListener(@NonNull Runnable listener) {
        mAppStyledViewController.setOnNavIconClickListener(listener);
    }

    /**
     * Sets a runnable that will be invoked when a dialog is dismissed.
     */
    public void setOnDismissListener(@NonNull Runnable listener) {
        mDialog.setOnDismissListener(listener);
    }

    /**
     * Returns the width of the AppStyledView
     */
    public int getAppStyledViewDialogWidth() {
        return mAppStyledViewController.getDialogWindowLayoutParam(
                mDialog.getWindowLayoutParams()).width;
    }

    /**
     * Returns the height of the AppStyledView
     */
    public int getAppStyledViewDialogHeight() {
        return mAppStyledViewController.getDialogWindowLayoutParam(
                mDialog.getWindowLayoutParams()).height;
    }

    /**
     * Returns the maximum width for content to be rendered in the AppStyledView. A value of -1 will
     * be returned if content area size cannot be determined.
     */
    public int getContentAreaWidth() {
        return mAppStyledViewController.getContentAreaWidth();
    }

    /**
     * Returns the maximum height for content to be rendered in the AppStyledView. A value of -1
     * will be returned if content area size cannot be determined.
     */
    public int getContentAreaHeight() {
        return mAppStyledViewController.getContentAreaHeight();
    }

    @VisibleForTesting
    AppStyledDialog getAppStyledDialog() {
        return mDialog;
    }
}
