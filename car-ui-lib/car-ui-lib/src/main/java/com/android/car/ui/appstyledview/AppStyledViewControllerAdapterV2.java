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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.android.car.ui.appstyledview.AppStyledDialogController.NavIcon;
import com.android.car.ui.plugin.oemapis.appstyledview.AppStyledViewControllerOEMV2;

/**
 * Adapts a {@link AppStyledViewControllerOEMV2} into a {@link AppStyledViewController}
 */
public class AppStyledViewControllerAdapterV2 implements AppStyledViewController {
    @NonNull
    private final AppStyledViewControllerOEMV2 mOemController;

    private final AppStyledDialog mDialog;
    private View mContent;

    public AppStyledViewControllerAdapterV2(@NonNull Context context,
            @NonNull AppStyledViewControllerOEMV2 controllerOEMV2) {
        mOemController = controllerOEMV2;
        mOemController.setNavIcon(AppStyledViewControllerOEMV2.NAV_ICON_CLOSE);
        mDialog = new AppStyledDialog((Activity) context) {
            @Override
            public WindowManager.LayoutParams getDialogWindowLayoutParam(
                    WindowManager.LayoutParams params) {
                return mOemController.getDialogWindowLayoutParam(params);
            }
        };
    }

    @Override
    public void setContent(@NonNull View content) {
        mContent = content;
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

    @Override
    public void show() {
        if (mContent == null) {
            return;
        }

        if (mContent.getParent() != null) {
            ((ViewGroup) mContent.getParent()).removeView(mContent);
        }

        mOemController.setContent(mContent);

        View wrappedContent = mOemController.getView();
        if (wrappedContent == null) {
            return;
        }

        if (wrappedContent.getParent() != null) {
            ((ViewGroup) wrappedContent.getParent()).removeView(wrappedContent);
        }

        mDialog.setContentView(wrappedContent);

        mDialog.show();
    }

    @Override
    public void dismiss() {
        mDialog.dismiss();
    }

    @Override
    public void setOnDismissListener(Runnable runnable) {
        if (runnable == null) {
            mDialog.setOnDismissListener(null);
            return;
        }

        mDialog.setOnDismissListener(dialog -> runnable.run());
    }

    @Override
    public WindowManager.LayoutParams getAttributes() {
        return mDialog.getWindowLayoutParams();
    }
}
