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

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.car.ui.R;

/**
 * Controller to interact with the app styled view.
 */
public class AppStyledViewControllerImpl implements AppStyledViewController {
    private static final double VISIBLE_SCREEN_PERCENTAGE = 0.9;

    private final Context mContext;
    @AppStyledViewNavIcon
    private int mAppStyleViewNavIcon;
    @Nullable
    private Runnable mAppStyledVCloseClickListener;
    @Nullable
    private View mAppStyledView;
    private int mWidth;
    private int mHeight;

    public AppStyledViewControllerImpl(Context context) {
        mContext = context;
    }

    @Override
    public void setNavIcon(@AppStyledViewNavIcon int navIcon) {
        mAppStyleViewNavIcon = navIcon;
        updateNavIcon();

    }

    /**
     * Sets the AppStyledVCloseClickListener on the close icon.
     */
    @Override
    public void setOnNavIconClickListener(@Nullable Runnable listener) {
        mAppStyledVCloseClickListener = listener;
        updateNavIconClickListener();
    }

    @Override
    public LayoutParams getDialogWindowLayoutParam(LayoutParams params) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = mContext.getSystemService(WindowManager.class);
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        int maxWidth = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_width_max);
        int maxHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_height_max);

        int displayWidth = (int) (displayMetrics.widthPixels * VISIBLE_SCREEN_PERCENTAGE);
        int displayHeight = (int) (displayMetrics.heightPixels * VISIBLE_SCREEN_PERCENTAGE);

        if (mContext.getResources().getConfiguration()
                .orientation == Configuration.ORIENTATION_LANDSCAPE && maxWidth < displayWidth) {
            params.gravity = Gravity.START;
        }

        int configuredWidth = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_width);
        mWidth = configuredWidth != 0 ? configuredWidth : Math.min(displayWidth, maxWidth);
        int configuredHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_height);
        mHeight = configuredHeight != 0 ? configuredHeight : Math.min(displayHeight, maxHeight);

        params.width = mWidth;
        params.height = mHeight;

        int posX = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_position_x);
        int posY = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_position_y);

        if (posX != 0 || posY != 0) {
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = posX;
            params.y = posY;
        }

        params.windowAnimations = R.style.Widget_CarUi_AppStyledView_WindowAnimations;

        return params;
    }

    @Override
    public int getContentAreaWidth() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return mWidth - mContext.getResources().getDimensionPixelSize(
                    R.dimen.car_ui_toolbar_first_row_height);
        }

        return mWidth;
    }

    @Override
    public int getContentAreaHeight() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return mHeight;
        }

        return mHeight - mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_toolbar_first_row_height);
    }

    @Override
    public View getAppStyledView(@Nullable View contentView) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mAppStyledView = inflater.inflate(R.layout.car_ui_app_styled_view, null, false);
        mAppStyledView.setClipToOutline(true);
        RecyclerView rv = mAppStyledView.findViewById(R.id.car_ui_app_styled_content);

        AppStyledRecyclerViewAdapter adapter = new AppStyledRecyclerViewAdapter(contentView);
        rv.setLayoutManager(new LinearLayoutManager(mContext));
        rv.setAdapter(adapter);

        updateNavIcon();
        updateNavIconClickListener();

        return mAppStyledView;
    }

    private void updateNavIcon() {
        if (mAppStyledView == null) {
            return;
        }

        ImageView close = mAppStyledView.findViewById(R.id.car_ui_app_styled_view_icon_close);
        if (mAppStyleViewNavIcon == AppStyledViewNavIcon.BACK) {
            close.setImageResource(R.drawable.car_ui_icon_arrow_back);
        } else if (mAppStyleViewNavIcon == AppStyledViewNavIcon.CLOSE) {
            close.setImageResource(R.drawable.car_ui_icon_close);
        } else {
            close.setImageResource(R.drawable.car_ui_icon_close);
        }
    }

    private void updateNavIconClickListener() {
        if (mAppStyledView == null) {
            return;
        }

        FrameLayout navContainer =
                mAppStyledView.findViewById(R.id.car_ui_app_styled_view_nav_icon_container);
        if (navContainer != null) {
            navContainer.setOnClickListener((v) -> {
                if (mAppStyledVCloseClickListener == null) {
                    return;
                }
                mAppStyledVCloseClickListener.run();
            });
        }
    }
}
