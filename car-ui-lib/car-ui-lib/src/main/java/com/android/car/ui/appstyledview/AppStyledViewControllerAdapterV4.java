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
package com.android.car.ui.appstyledview;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.android.car.ui.appstyledview.AppStyledDialogController.NavIcon;
import com.android.car.ui.appstyledview.AppStyledDialogController.SceneType;
import com.android.car.ui.plugin.oemapis.appstyledview.AppStyledViewControllerOEMV4;

/**
 * Adapts a {@link AppStyledViewControllerOEMV4} into a {@link AppStyledViewController}
 */
public class AppStyledViewControllerAdapterV4 implements AppStyledViewController {

    @NonNull
    private final AppStyledViewControllerOEMV4 mOemController;

    public AppStyledViewControllerAdapterV4(@NonNull Context context,
            @NonNull AppStyledViewControllerOEMV4 controllerOemV4) {
        mOemController = controllerOemV4;
        mOemController.setNavIcon(AppStyledViewControllerOEMV4.NAV_ICON_CLOSE);
    }

    @Override
    public void setContent(@NonNull View content) {
        mOemController.setContent(content);
    }

    @Override
    public void setNavIcon(@NavIcon int navIcon) {
        switch (navIcon) {
            case NavIcon.BACK:
                mOemController.setNavIcon(AppStyledViewControllerOEMV4.NAV_ICON_BACK);
                break;
            case NavIcon.CLOSE:
                mOemController.setNavIcon(AppStyledViewControllerOEMV4.NAV_ICON_CLOSE);
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
    public int getContentAreaWidth() {
        return mOemController.getContentAreaWidth();
    }

    @Override
    public int getContentAreaHeight() {
        return mOemController.getContentAreaHeight();
    }

    @Override
    public void setSceneType(@SceneType int sceneType) {
        mOemController.setSceneType(sceneType);
    }

    @Override
    public void show() {
        mOemController.show();
    }

    @Override
    public void dismiss() {
        mOemController.dismiss();
    }

    @Override
    public void setOnDismissListener(Runnable runnable) {
        mOemController.setOnDismissListener(runnable);
    }

    @Override
    public WindowManager.LayoutParams getAttributes() {
        return mOemController.getAttributes();
    }
}
