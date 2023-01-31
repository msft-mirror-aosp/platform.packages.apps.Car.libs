/*
 * Copyright (C) 2022 The Android Open Source Project
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

import android.view.View;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.ui.appstyledview.AppStyledDialogController.NavIcon;
import com.android.car.ui.plugin.oemapis.appstyledview.AppStyledViewControllerOEMV2;

/**
 * Adapts a {@link AppStyledViewControllerOEMV2} into a {@link AppStyledViewController}
 */
public class AppStyledViewControllerAdapterV2 implements AppStyledViewController {
    @NonNull
    private final AppStyledViewControllerOEMV2 mOemController;

    public AppStyledViewControllerAdapterV2(@NonNull AppStyledViewControllerOEMV2 controllerOEMV2) {
        mOemController = controllerOEMV2;
        mOemController.setNavIcon(AppStyledViewControllerOEMV2.NAV_ICON_CLOSE);
    }

    /**
     * Returns the view that will be displayed on the screen.
     */
    @Override
    public View getAppStyledView(@Nullable View contentView) {
        mOemController.setContent(contentView);
        return mOemController.getView();
    }

    @Override
    public void setNavIcon(@NavIcon int navIcon) {
        switch (navIcon) {
            case NavIcon.BACK:
                mOemController.setNavIcon(AppStyledViewControllerOEMV2.NAV_ICON_BACK);
                break;
            case NavIcon.CLOSE:
                mOemController.setNavIcon(AppStyledViewControllerOEMV2.NAV_ICON_CLOSE);
                break;
            default:
                throw new IllegalArgumentException("Unknown nav icon style: " + navIcon);
        }
    }

    @Override
    public void setOnNavIconClickListener(Runnable listener) {
        mOemController.setOnBackClickListener(listener);
    }

    @Override
    public LayoutParams getDialogWindowLayoutParam(LayoutParams params) {
        return mOemController.getDialogWindowLayoutParam(params);
    }

    @Override
    public int getContentAreaWidth() {
        return mOemController.getContentAreaWidth();
    }

    @Override
    public int getContentAreaHeight() {
        return mOemController.getContentAreaHeight();
    }

    @Override
    public void setSceneType(int sceneType) {
        // Do nothing. Scene type not supported in AppStyledViewControllerOEMV2.
    }
}
