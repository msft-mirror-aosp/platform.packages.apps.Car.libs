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

import android.view.View;
import android.view.WindowManager;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.android.car.ui.appstyledview.AppStyledDialogController.NavIcon;

import java.lang.annotation.Retention;

/**
 * An internal interface for accessing a Chassis AppStyledView, regardless of how the underlying
 * views are represented.
 * <p>
 * Apps should not use this directly. Apps should use {@link AppStyledDialogController}.
 */
public interface AppStyledViewController {
    /**
     * The possible values for AppStyledViewNavIcon.
     *
     * @deprecated Use {@link NavIcon} instead.
     */
    @Deprecated
    @IntDef({
            AppStyledViewNavIcon.BACK,
            AppStyledViewNavIcon.CLOSE,
    })
    @Retention(SOURCE)
    @interface AppStyledViewNavIcon {

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
     * Creates a app styled view.
     *
     * @return the view used for app styled view.
     */
    View getAppStyledView(@Nullable View contentView);

    /**
     * Sets the nav icon to be used.
     */
    void setNavIcon(@NavIcon int navIcon);

    /**
     * Sets a runnable that will be invoked when a nav icon is clicked.
     */
    void setOnNavIconClickListener(Runnable listener);

    /**
     * Returns the layout params for the AppStyledView dialog
     */
    WindowManager.LayoutParams getDialogWindowLayoutParam(WindowManager.LayoutParams params);

    /**
     * Returns the maximum width for content to be rendered in the AppStyledView.
     */
    int getContentAreaWidth();

    /**
     * Returns the maximum height for content to be rendered in the AppStyledView.
     */
    int getContentAreaHeight();
}
