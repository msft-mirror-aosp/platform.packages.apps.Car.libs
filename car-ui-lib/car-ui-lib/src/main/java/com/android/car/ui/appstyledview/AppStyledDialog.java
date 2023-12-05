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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.android.car.ui.utils.CarUiUtils;

import java.util.List;

/**
 * App styled dialog used to display a view that cannot be customized via OEM. Dialog will inflate a
 * layout and add the view provided by the application into the layout. Everything other than the
 * view within the layout can be customized by OEM.
 * <p>
 * Apps should not use this directly. Apps should use {@link AppStyledDialogController}.
 */
class AppStyledDialog extends Dialog implements DialogInterface.OnDismissListener {
    private static final int IME_OVERLAP_DP = 32;

    private final AppStyledViewController mController;
    private Runnable mOnDismissListener;
    private View mContent;
    private View mAppStyledView;
    private final Context mContext;

    AppStyledDialog(@NonNull Activity context, @NonNull AppStyledViewController controller) {
        super(context);
        // super.getContext() returns a ContextThemeWrapper which is not an Activity which we need
        // in order to get call getWindow()
        mContext = context;
        mController = controller;
        setOnDismissListener(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window == null) {
            return;
        }

        window.setAttributes(mController.getDialogWindowLayoutParam(getWindow().getAttributes()));
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        configureImeInsetFit();
    }

    private void configureImeInsetFit() {
        // Required inset API is unsupported. Fallback to default IME behavior.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return;
        }

        Window window = getWindow();
        if (window == null) {
            return;
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setWindowInsetsAnimationCallback(window.getDecorView().getRootView(),
                new WindowInsetsAnimationCompat.Callback(
                        WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {

                    int mEndHeight;
                    WindowManager.LayoutParams mAnimationLayoutParams;
                    WindowManager.LayoutParams mStartLayoutParams;
                    int mAppStyledViewBottomPadding;
                    boolean mIsImeShown;
                    final int mImeOverlapPx =
                            (int) CarUiUtils.dpToPixel(mContext.getResources(), IME_OVERLAP_DP);

                    @Override
                    @SuppressLint({"NewApi", "RtlHardcoded"})
                    public void onPrepare(@NonNull WindowInsetsAnimationCompat animation) {
                        mAnimationLayoutParams = new WindowManager.LayoutParams();
                        mStartLayoutParams = mController.getDialogWindowLayoutParam(
                                window.getAttributes());
                        mAnimationLayoutParams.copyFrom(mStartLayoutParams);

                        int[] location = new int[2];
                        window.getDecorView().getRootView().getLocationOnScreen(location);
                        int x = location[0];
                        int y = location[1];

                        mAppStyledViewBottomPadding = mAppStyledView.getPaddingBottom();

                        mAnimationLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                        mAnimationLayoutParams.setFitInsetsTypes(0);
                        mAnimationLayoutParams.x = x;
                        mAnimationLayoutParams.y = y;
                        window.setAttributes(mAnimationLayoutParams);
                    }

                    @NonNull
                    @Override
                    public WindowInsetsAnimationCompat.BoundsCompat onStart(
                            @NonNull WindowInsetsAnimationCompat animation,
                            @NonNull WindowInsetsAnimationCompat.BoundsCompat bounds) {
                        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(
                                window.getDecorView().getRootView());
                        int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                        mIsImeShown = imeHeight > 0;
                        int imeOverlap = mIsImeShown ? imeHeight - mImeOverlapPx : 0;
                        mEndHeight = mController.getDialogWindowLayoutParam(
                                window.getAttributes()).height - imeOverlap;

                        return bounds;
                    }

                    @Override
                    public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                        mStartLayoutParams.height = mEndHeight;
                        window.setAttributes(mStartLayoutParams);
                        super.onEnd(animation);
                    }

                    @NonNull
                    @Override
                    public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets,
                            @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                        // Find an IME animation.
                        WindowInsetsAnimationCompat imeAnimation = null;
                        for (WindowInsetsAnimationCompat animation : runningAnimations) {
                            if ((animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
                                imeAnimation = animation;
                                break;
                            }
                        }
                        if (imeAnimation != null) {
                            // Offset the view based on the interpolated fraction of the IME
                            // animation.
                            int mStartHeight = mStartLayoutParams.height;
                            mAnimationLayoutParams.height =
                                    (int) (mStartHeight - ((mStartHeight - mEndHeight)
                                            * imeAnimation.getInterpolatedFraction()));
                            window.setAttributes(mAnimationLayoutParams);
                            float imeOffset = mIsImeShown ? mImeOverlapPx
                                    * imeAnimation.getInterpolatedFraction()
                                    : -mImeOverlapPx * imeAnimation.getInterpolatedFraction();
                            mAppStyledView.setPadding(mAppStyledView.getPaddingLeft(),
                                    mAppStyledView.getPaddingTop(),
                                    mAppStyledView.getPaddingRight(),
                                    (int) (mAppStyledViewBottomPadding + imeOffset));
                        }

                        return insets;
                    }
                });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mOnDismissListener != null) {
            mOnDismissListener.run();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        copyWindowInsets();
        copySystemUiVisibility();
    }

    /**
     * Copy the visibility of the Activity that has started the dialog {@code mContext}. If the
     * activity is in Immersive mode the dialog will be in Immersive mode too and vice versa.
     */
    private void copySystemUiVisibility() {
        getWindow().getDecorView().setSystemUiVisibility(
                ((Activity) mContext).getWindow().getDecorView().getSystemUiVisibility());
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                i -> getWindow().getDecorView().setSystemUiVisibility(
                        ((Activity) mContext).getWindow().getDecorView().getSystemUiVisibility()));
    }

    /**
     * Copy window inset settings from the activity that requested the dialog. Status bar insets
     * mirror activity state but nav bar requires the following workaround.
     */
    private void copyWindowInsets() {
        // WindowInsetsController corresponding to activity that requested the dialog
        WindowInsetsControllerCompat activityWindowInsetsController =
                ViewCompat.getWindowInsetsController(
                        ((Activity) mContext).getWindow().getDecorView());

        // WindowInsetsController corresponding to the dialog
        WindowInsetsControllerCompat dialogWindowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());

        if (dialogWindowInsetsController == null || activityWindowInsetsController == null) {
            return;
        }

        int activitySystemBarBehavior = activityWindowInsetsController.getSystemBarsBehavior();
        // Only set system bar behavior when non-default settings are required. Setting default may
        // overwrite flags set by deprecated methods with different defaults.
        if (activitySystemBarBehavior != 0) {
            // Configure the behavior of the hidden system bars to match requesting activity
            dialogWindowInsetsController.setSystemBarsBehavior(
                    activityWindowInsetsController.getSystemBarsBehavior());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Configure nav bar visibility to match requesting activity
            WindowInsets windowInsets =
                    ((Activity) mContext).getWindow().getDecorView().getRootWindowInsets();
            if (windowInsets == null) {
                return;
            }

            boolean isNavBarVisible = windowInsets.isVisible(WindowInsets.Type.navigationBars());
            if (!isNavBarVisible) {
                dialogWindowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
            }
        }
    }

    @Override
    public void show() {
        if (getWindow() != null) {
            getWindow().setAttributes(
                    mController.getDialogWindowLayoutParam(getWindow().getAttributes()));
        }
        super.show();
        mContent.clearFocus();
    }

    void setContent(View contentView) {
        if (contentView.getParent() != null) {
            ((ViewGroup) contentView.getParent()).removeView(contentView);
        }

        mContent = contentView;
        mAppStyledView = mController.getAppStyledView(mContent);
        configureImeInsetFit();
        setContentView(mAppStyledView);
    }

    View getContent() {
        return mContent;
    }

    void setOnDismissListener(Runnable listener) {
        mOnDismissListener = listener;
    }

    WindowManager.LayoutParams getWindowLayoutParams() {
        return getWindow().getAttributes();
    }
}
