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

package com.chassis.car.ui.plugin.appstyleview;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.android.car.ui.plugin.oemapis.appstyledview.AppStyledViewControllerOEMV3;

import com.chassis.car.ui.plugin.R;

/**
 * The OEM implementation for {@link AppStyledViewControllerOEMV3} for a AppStyledView.
 */
public class AppStyleViewControllerImpl implements AppStyledViewControllerOEMV3 {
    private static final double VISIBLE_SCREEN_PERCENTAGE = 0.9;
    private final Context mPluginContext;
    private final View mAppStyleView;
    private int mWidth;
    private int mHeight;

    public AppStyleViewControllerImpl(Context pluginContext) {
        mPluginContext = pluginContext;
        LayoutInflater inflater = LayoutInflater.from(mPluginContext);
        mAppStyleView = inflater.inflate(R.layout.app_styled_view, null, false);
        mAppStyleView.setClipToOutline(true);
    }

    @Override
    public View getView() {
        return mAppStyleView;
    }

    @Override
    public void setContent(View content) {
        ScrollView scrollview = mAppStyleView.requireViewById(R.id.app_styled_content);
        scrollview.removeAllViews();

        // Remove content ViewGroup from any existing view hierarchies
        if (content.getParent() != null) {
            ((ViewGroup) content.getParent()).removeView(content);
        }

        scrollview.addView(content);
    }

    @Override
    public void setOnBackClickListener(Runnable listener) {
        ImageView navIconView = mAppStyleView.requireViewById(R.id.app_styled_view_icon_close);
        if (listener == null) {
            navIconView.setOnClickListener(null);
            navIconView.setClickable(false);
        } else {
            navIconView.setOnClickListener(v -> listener.run());
        }
    }

    @Override
    public void setNavIcon(int navIcon) {
        ImageView navIconView = mAppStyleView.requireViewById(R.id.app_styled_view_icon_close);
        navIconView.setVisibility(navIcon == NAV_ICON_DISABLED ? View.INVISIBLE : View.VISIBLE);
        if (navIcon == NAV_ICON_BACK) {
            navIconView.setImageResource(R.drawable.icon_back);
        } else if (navIcon == NAV_ICON_CLOSE) {
            navIconView.setImageResource(R.drawable.icon_close);
        }
    }

    @Override
    public LayoutParams getDialogWindowLayoutParam(LayoutParams params) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = mPluginContext.getSystemService(WindowManager.class);
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        int maxWidth = mPluginContext.getResources().getDimensionPixelSize(
                R.dimen.app_styled_dialog_width_max);
        int maxHeight = mPluginContext.getResources().getDimensionPixelSize(
                R.dimen.app_styled_dialog_height_max);

        int displayWidth = (int) (displayMetrics.widthPixels * VISIBLE_SCREEN_PERCENTAGE);
        int displayHeight = (int) (displayMetrics.heightPixels * VISIBLE_SCREEN_PERCENTAGE);

        if (mPluginContext.getResources().getConfiguration()
                .orientation == Configuration.ORIENTATION_LANDSCAPE && maxWidth < displayWidth) {
            params.gravity = Gravity.START;
        }

        int configuredWidth = mPluginContext.getResources().getDimensionPixelSize(
                R.dimen.app_styled_dialog_width);
        mWidth = configuredWidth != 0 ? configuredWidth : Math.min(displayWidth, maxWidth);
        int configuredHeight = mPluginContext.getResources().getDimensionPixelSize(
                R.dimen.app_styled_dialog_height);
        mHeight = configuredHeight != 0 ? configuredHeight : Math.min(displayHeight, maxHeight);

        params.width = mWidth;
        params.height = mHeight;

        int posX = mPluginContext.getResources().getDimensionPixelSize(
                R.dimen.app_styled_dialog_position_x);
        int posY = mPluginContext.getResources().getDimensionPixelSize(
                R.dimen.app_styled_dialog_position_y);

        if (posX != 0 || posY != 0) {
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = posX;
            params.y = posY;
        }
        // TODO (233660647) AppStyledView animations don't render when using plugin
        return params;
    }

    @Override
    public int getContentAreaWidth() {
        int orientation = mPluginContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return mWidth - mPluginContext.getResources().getDimensionPixelSize(
                    R.dimen.app_styled_dialog_nav_bar_height);
        }

        return mWidth;
    }

    @Override
    public int getContentAreaHeight() {
        int orientation = mPluginContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return mHeight;
        }

        return mHeight - mPluginContext.getResources().getDimensionPixelSize(
                R.dimen.app_styled_dialog_nav_bar_height);
    }

    @Override
    public void setSceneType(int sceneType) {
        // Plugin does not support different scene types
    }
}
