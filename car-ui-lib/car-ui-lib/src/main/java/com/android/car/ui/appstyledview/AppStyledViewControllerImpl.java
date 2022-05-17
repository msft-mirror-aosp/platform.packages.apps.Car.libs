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
import android.view.ContextThemeWrapper;
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

import com.android.car.ui.CarUiLayoutInflaterFactory;
import com.android.car.ui.R;

/**
 * Controller to interact with the app styled view.
 */
public class AppStyledViewControllerImpl implements AppStyledViewController {
    private static final double VISIBLE_SCREEN_PERCENTAGE = 0.9;

    private final Context mContext;
    @AppStyledViewNavIcon
    private int mAppStyleViewNavIcon;
    private Runnable mAppStyledVCloseClickListener = null;
    private int mWidth;
    private int mHeight;

    public AppStyledViewControllerImpl(Context context) {
        mContext = context;
    }

    @Override
    public void setNavIcon(@AppStyledViewNavIcon int navIcon) {
        mAppStyleViewNavIcon = navIcon;
    }

    /**
     * Sets the AppStyledVCloseClickListener on the close icon.
     */
    @Override
    public void setOnNavIconClickListener(Runnable listener) {
        mAppStyledVCloseClickListener = listener;
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
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(mContext, R.style.Theme_CarUi);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (inflater.getFactory2() == null) {
            inflater.setFactory2(new CarUiLayoutInflaterFactory());
        }
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        View appStyledView = localInflater.inflate(R.layout.car_ui_app_styled_view, null, false);
        appStyledView.setClipToOutline(true);
        RecyclerView rv = appStyledView.findViewById(R.id.car_ui_app_styled_content);

        AppStyledRecyclerViewAdapter adapter = new AppStyledRecyclerViewAdapter(contentView);
        rv.setLayoutManager(new LinearLayoutManager(mContext));
        rv.setAdapter(adapter);

        ImageView close = appStyledView.findViewById(R.id.car_ui_app_styled_view_icon_close);
        if (mAppStyleViewNavIcon == AppStyledViewNavIcon.BACK) {
            close.setImageResource(R.drawable.car_ui_icon_arrow_back);
        } else if (mAppStyleViewNavIcon == AppStyledViewNavIcon.CLOSE) {
            close.setImageResource(R.drawable.car_ui_icon_close);
        } else {
            close.setImageResource(R.drawable.car_ui_icon_close);
        }

        FrameLayout navContainer =
                appStyledView.findViewById(R.id.car_ui_app_styled_view_nav_icon_container);
        if (mAppStyledVCloseClickListener != null && navContainer != null) {
            navContainer.setOnClickListener((v) -> {
                mAppStyledVCloseClickListener.run();
            });
        }

        return appStyledView;
    }
}
