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

import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.activity.ViewTreeOnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.savedstate.SavedStateRegistry;
import androidx.savedstate.SavedStateRegistryController;
import androidx.savedstate.SavedStateRegistryOwner;
import androidx.savedstate.ViewTreeSavedStateRegistryOwner;

import com.android.car.ui.utils.CarUiUtils;

import java.util.List;

/**
 * App styled dialog used to display a view that cannot be customized via OEM. Dialog will inflate a
 * layout and add the view provided by the application into the layout. Everything other than the
 * view within the layout can be customized by OEM.
 * <p>
 * Apps should not use this directly. Apps should use {@link AppStyledDialogController}.
 */

class AppStyledDialog extends Dialog implements DialogInterface.OnDismissListener, LifecycleOwner,
        SavedStateRegistryOwner, OnBackPressedDispatcherOwner {

    private static final int IME_OVERLAP_DP = 32;
    private final AppStyledViewController mController;
    private Runnable mOnDismissListener;
    private View mContent;
    private View mAppStyledView;
    private final Context mContext;
    private final LifecycleRegistry mLifecycleRegistry;
    private final SavedStateRegistryController mSavedStateRegistryController;
    private final OnBackPressedDispatcher mOnBackPressedDispatcher;

    AppStyledDialog(@NonNull Activity context, @NonNull AppStyledViewController controller) {
        super(context);
        mLifecycleRegistry = new LifecycleRegistry(this);
        mSavedStateRegistryController = SavedStateRegistryController.create(this);
        mOnBackPressedDispatcher = new OnBackPressedDispatcher(super::onBackPressed);
        // super.getContext() returns a ContextThemeWrapper which is not an Activity which we
        // need in order to get call getWindow()
        mContext = context;
        mController = controller;
        setOnDismissListener(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        Bundle bundle = super.onSaveInstanceState();
        mSavedStateRegistryController.performSave(bundle);
        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedStateRegistryController.performRestore(savedInstanceState);
        Window window = getWindow();
        if (window == null) {
            return;
        }

        window.setAttributes(mController.getDialogWindowLayoutParam(getWindow().getAttributes()));
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        configureImeInsetFit();

        mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
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

                    private boolean isImeAnimation(WindowInsetsAnimationCompat animation) {
                        return (animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0;

                    }

                    @Override
                    @SuppressLint({"NewApi", "RtlHardcoded"})
                    public void onPrepare(@NonNull WindowInsetsAnimationCompat animation) {
                        if (!isImeAnimation(animation)) {
                            return;
                        }

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
                        if (!isImeAnimation(animation)) {
                            return bounds;
                        }
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
                        if (isImeAnimation(animation)) {
                            mStartLayoutParams.height = mEndHeight;
                            window.setAttributes(mStartLayoutParams);
                        } else {
                            window.setAttributes(
                                    mController.getDialogWindowLayoutParam(window.getAttributes()));
                            copyWindowInsets();
                        }

                        super.onEnd(animation);
                    }

                    @NonNull
                    @Override
                    public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets,
                            @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                        // Find an IME animation.
                        WindowInsetsAnimationCompat imeAnimation = null;
                        for (WindowInsetsAnimationCompat animation : runningAnimations) {
                            if (isImeAnimation(animation)) {
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
    protected void onStart() {
        mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        mLifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);

        if (getWindow() != null) {
            getWindow().setAttributes(
                    mController.getDialogWindowLayoutParam(getWindow().getAttributes()));
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        mLifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);

        if (getWindow() != null) {
            ViewCompat.setWindowInsetsAnimationCallback(getWindow().getDecorView().getRootView(),
                    null);
        }
        super.onStop();
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
        if (getWindow() == null) {
            return;
        }

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
        Window window = getWindow();
        if (window == null) {
            return;
        }

        // WindowInsetsController corresponding to the dialog
        WindowInsetsControllerCompat dialogWindowInsetsController =
                WindowCompat.getInsetsController(window, getWindow().getDecorView());

        Activity activity = ((Activity) mContext);

        // WindowInsetsController corresponding to activity that requested the dialog
        WindowInsetsControllerCompat activityWindowInsetsController =
                WindowCompat.getInsetsController(activity.getWindow(),
                        activity.getWindow().getDecorView());


        int activitySystemBarBehavior = activityWindowInsetsController.getSystemBarsBehavior();
        // Only set system bar behavior when non-default settings are required. Setting default may
        // overwrite flags set by deprecated methods with different defaults.
        if (activitySystemBarBehavior != 0) {
            // Configure the behavior of the hidden system bars to match requesting activity
            dialogWindowInsetsController.setSystemBarsBehavior(activitySystemBarBehavior);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Configure nav bar visibility to match requesting activity
            WindowInsets windowInsets = activity.getWindow().getDecorView().getRootWindowInsets();
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

    @Override
    public void setContentView(@NonNull View view) {
        initViewTreeOwners();
        super.setContentView(view);
    }

    @Override
    public void setContentView(int layoutResID) {
        initViewTreeOwners();
        super.setContentView(layoutResID);
    }

    @Override
    public void setContentView(@NonNull View view, @Nullable ViewGroup.LayoutParams params) {
        initViewTreeOwners();
        super.setContentView(view, params);
    }

    @Override
    public void addContentView(@NonNull View view, @Nullable ViewGroup.LayoutParams params) {
        initViewTreeOwners();
        super.addContentView(view, params);
    }

    View getContent() {
        return mContent;
    }

    void setOnDismissListener(Runnable listener) {
        mOnDismissListener = listener;
    }

    @Nullable
    WindowManager.LayoutParams getWindowLayoutParams() {
        if (getWindow() == null) {
            return null;
        }

        return getWindow().getAttributes();
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    private void initViewTreeOwners() {
        Window window = getWindow();
        if (window == null) {
            return;
        }

        ViewTreeLifecycleOwner.set(window.getDecorView(), this);
        ViewTreeSavedStateRegistryOwner.set(window.getDecorView(), this);
        ViewTreeOnBackPressedDispatcherOwner.set(window.getDecorView(), this);
    }

    @NonNull
    @Override
    public SavedStateRegistry getSavedStateRegistry() {
        return mSavedStateRegistryController.getSavedStateRegistry();
    }

    @NonNull
    @Override
    public OnBackPressedDispatcher getOnBackPressedDispatcher() {
        return mOnBackPressedDispatcher;
    }
}
