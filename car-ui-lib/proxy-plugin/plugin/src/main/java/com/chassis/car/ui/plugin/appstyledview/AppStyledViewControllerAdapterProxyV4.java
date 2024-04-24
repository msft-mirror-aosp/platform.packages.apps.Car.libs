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

package com.chassis.car.ui.plugin.appstyledview;

import android.view.View;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.NonNull;

import com.android.car.ui.appstyledview.AppStyledDialogController.NavIcon;
import com.android.car.ui.appstyledview.AppStyledDialogController.SceneType;
import com.android.car.ui.appstyledview.AppStyledViewController;
import com.android.car.ui.appstyledview.AppStyledViewControllerImpl;
import com.android.car.ui.plugin.oemapis.appstyledview.AppStyledViewControllerOEMV4;

/**
 * Adapts a {@link AppStyledViewController} into a {@link AppStyledViewControllerOEMV4}.
 */
public class AppStyledViewControllerAdapterProxyV4 implements AppStyledViewControllerOEMV4 {

    @NonNull
    private final AppStyledViewControllerImpl mController;
    private View mContentView;

    public AppStyledViewControllerAdapterProxyV4(@NonNull AppStyledViewControllerImpl controller) {
        mController = controller;
    }

    @Override
    public View getView() {
        if (mContentView == null) {
            return null;
        }

        return mController.createAppStyledView(mContentView);
    }

    @Override
    public void setContent(View view) {
        mContentView = view;
        mController.setContent(mContentView);
    }

    @Override
    public void setOnBackClickListener(Runnable runnable) {
        mController.setOnNavIconClickListener(runnable);
    }

    @Override
    public void setNavIcon(@NavIcon int navIcon) {
        switch (navIcon) {
            case AppStyledViewControllerOEMV4.NAV_ICON_BACK:
                mController.setNavIcon(NavIcon.BACK);
                break;
            case AppStyledViewControllerOEMV4.NAV_ICON_CLOSE:
                mController.setNavIcon(NavIcon.CLOSE);
                break;
            default:
                throw new IllegalArgumentException("Unknown nav icon style: " + navIcon);
        }
    }

    @Override
    public void setSceneType(@SceneType int sceneType) {
        switch (sceneType) {
            case AppStyledViewControllerOEMV4.SCENE_TYPE_SINGLE:
                mController.setSceneType(SceneType.SINGLE);
                break;
            case AppStyledViewControllerOEMV4.SCENE_TYPE_ENTER:
                mController.setSceneType(SceneType.ENTER);
                break;
            case AppStyledViewControllerOEMV4.SCENE_TYPE_INTERMEDIATE:
                mController.setSceneType(SceneType.INTERMEDIATE);
                break;
            case AppStyledViewControllerOEMV4.SCENE_TYPE_EXIT:
                mController.setSceneType(SceneType.EXIT);
                break;
            default:
                throw new IllegalArgumentException("Unknown nav icon style: " + sceneType);
        }
    }

    @Override
    public void show() {
        mController.show();
    }

    @Override
    public void dismiss() {
        mController.dismiss();
    }

    @Override
    public void setOnDismissListener(Runnable runnable) {
        mController.setOnDismissListener(runnable);
    }

    @Override
    public LayoutParams getAttributes() {
        return null;
    }

    @Override
    public int getContentAreaWidth() {
        return mController.getContentAreaWidth();
    }

    @Override
    public int getContentAreaHeight() {
        return mController.getContentAreaHeight();
    }
}
