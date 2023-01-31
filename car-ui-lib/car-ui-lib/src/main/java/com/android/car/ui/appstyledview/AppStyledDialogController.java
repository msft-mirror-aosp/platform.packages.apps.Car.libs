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

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.car.ui.pluginsupport.PluginFactorySingleton;

import java.lang.annotation.Retention;
import java.util.Objects;

/**
 * Controller to interact with the app styled view UI.
 * <p>
 * Rendered views will comply with
 * <a href="https://source.android.com/devices/automotive/hmi/car_ui/appendix_b">customization guardrails</a>
 */
public final class AppStyledDialogController {
    /**
     * The possible values for NavIcon.
     */
    @IntDef({
            NavIcon.BACK,
            NavIcon.CLOSE,
    })
    @Retention(SOURCE)
    public @interface NavIcon {

        /**
         * Show a back icon
         */
        int BACK = 0;

        /**
         * Show a close icon
         */
        int CLOSE = 1;
    }

    /**
     * The possible values for SceneType.
     */
    @IntDef({
            SceneType.SINGLE,
            SceneType.ENTER,
            SceneType.INTERMEDIATE,
            SceneType.EXIT,
    })
    @Retention(SOURCE)
    public @interface SceneType {
        /**
         * An AppStyledView that renders all content in a single modal.
         */
        int SINGLE = 0;

        /**
         * An AppStyledView that renders the initial content for content to be rendered across
         * multiple modals.
         */
        int ENTER = 1;

        /**
         * An AppStyledView that renders the intermediate content for content to be rendered across
         * multiple modals.
         */
        int INTERMEDIATE = 2;

        /**
         * An AppStyledView that renders the final content for content to be rendered across
         * multiple modals.
         */
        int EXIT = 3;
    }

    @NonNull
    private AppStyledViewController mAppStyledViewController;
    @NonNull
    private AppStyledDialog mDialog;

    /**
     * Constructs a controller that can display an app styled view.
     *
     * @param activity The {@code Activity} that will display the app styled view.
     */
    public AppStyledDialogController(@NonNull Activity activity) {
        this(activity, SceneType.SINGLE);
    }

    /**
     * Constructs a controller that can display an app styled view.
     *
     * @param activity The {@code Activity} that will display the app styled view.
     * @param sceneType The {@link SceneType} for the app styled view.
     */
    public AppStyledDialogController(@NonNull Activity activity, @SceneType int sceneType) {
        Objects.requireNonNull(activity);
        mAppStyledViewController = PluginFactorySingleton.get(activity)
                .createAppStyledView(activity);
        mAppStyledViewController.setSceneType(sceneType);
        mDialog = new AppStyledDialog(activity, mAppStyledViewController);
    }

    /**
     * Constructs a controller that can display an app styled view.
     *
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
     *
     * @deprecated Use {@link #setNavIconType(int)} instead.
     */
    @Deprecated
    public void setNavIcon(@AppStyledViewController.AppStyledViewNavIcon int navIcon) {
        mAppStyledViewController.setNavIcon(navIcon);
    }

    /**
     * Sets the nav icon to be used.
     */
    public void setNavIconType(@NavIcon int navIcon) {
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

    /**
     * Returns a {@link Context} with a {@link Configuration} set to values that align with the
     * sizing of the content area of the AppStyledView to better facilitate resource loading
     * appropriate for the size onf the content area.
     */
    public Context createContentViewConfigurationContext(Context context) {
        int width = getContentAreaWidth();
        if (width == -1) {
            int widthPx = getAppStyledViewDialogWidth();
            width = (int) (widthPx / context.getResources().getDisplayMetrics().density);
        }

        int height = getContentAreaHeight();
        if (height == -1) {
            int heightPx = getAppStyledViewDialogHeight();
            height = (int) (heightPx / context.getResources().getDisplayMetrics().density);
        }

        Configuration config = context.getResources().getConfiguration();
        config.smallestScreenWidthDp = Math.min(width, height);
        config.screenWidthDp = width;
        config.screenHeightDp = height;
        Context configContext = context.createConfigurationContext(config);
        return new ContextThemeWrapper(configContext, context.getTheme());
    }

    @VisibleForTesting
    AppStyledDialog getAppStyledDialog() {
        return mDialog;
    }
}
