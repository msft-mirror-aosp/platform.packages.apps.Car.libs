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
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import androidx.core.graphics.Insets;
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

import com.android.car.ui.R;
import com.android.car.ui.utils.CarUiUtils;

import java.util.List;

/**
 * App styled dialog used to display a view that cannot be customized via OEM. Dialog will inflate a
 * layout and add the view provided by the application into the layout. Everything other than the
 * view within the layout can be customized by OEM.
 * <p>
 * Apps should not use this directly. Apps should use {@link AppStyledDialogController}.
 */

public class AppStyledDialog extends Dialog implements LifecycleOwner, SavedStateRegistryOwner,
        OnBackPressedDispatcherOwner {

    private static final double VISIBLE_SCREEN_PERCENTAGE = 0.9;
    private static final int DIALOG_START_MARGIN_THRESHOLD = 64;
    private static final int DIALOG_MIN_PADDING = 32;
    private static final int IME_OVERLAP_DP = 32;
    private View mContent;
    private final Context mContext;
    private final LifecycleRegistry mLifecycleRegistry;
    private final SavedStateRegistryController mSavedStateRegistryController;
    private final OnBackPressedDispatcher mOnBackPressedDispatcher;
    private WindowManager.LayoutParams mBaseLayoutParams;
    @AppStyledDialogController.SceneType
    private int mSceneType;


    public AppStyledDialog(@NonNull Context context) {
        super(context);
        mLifecycleRegistry = new LifecycleRegistry(this);
        mSavedStateRegistryController = SavedStateRegistryController.create(this);
        mOnBackPressedDispatcher = new OnBackPressedDispatcher(super::onBackPressed);
        // super.getContext() returns a ContextThemeWrapper which is not an Activity which we
        // need in order to get call getWindow()
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = getWindow();
        if (window == null) {
            return;
        }

        mBaseLayoutParams = new WindowManager.LayoutParams();
        mBaseLayoutParams.copyFrom(window.getAttributes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int types = WindowInsetsCompat.Type.systemBars();
            if (mBaseLayoutParams.layoutInDisplayCutoutMode
                    != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS) {
                types = types | WindowInsetsCompat.Type.displayCutout();
            }
            mBaseLayoutParams.setFitInsetsTypes(types);
        }

        updateAttributes();
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

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        updateAttributes();
        configureImeInsetFit();

        mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
    }

    private void updateAttributes() {
        Window window = getWindow();
        if (window == null) {
            return;
        }

        window.setAttributes(getDialogWindowLayoutParam(mBaseLayoutParams));
    }

    private float getVerticalInset(DisplayMetrics displayMetrics) {
        // Inset API not supported before Android R. Fallback to 90 percent of display size
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Context unwrappedContext = CarUiUtils.unwrapContext(mContext);
            WindowInsets windowInsets =
                    unwrappedContext.getSystemService(
                            WindowManager.class).getCurrentWindowMetrics().getWindowInsets();
            android.graphics.Insets systemBarInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars());

            return systemBarInsets.top + systemBarInsets.bottom;
        }

        return (float) (displayMetrics.heightPixels * (1 - VISIBLE_SCREEN_PERCENTAGE));
    }

    private float getHorizontalInset(DisplayMetrics displayMetrics) {
        // Inset API not supported before Android R. Fallback to 90 percent of display size
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Context unwrappedContext = CarUiUtils.unwrapContext(mContext);
            android.graphics.Insets systemBarInsets = unwrappedContext.getSystemService(
                    WindowManager.class).getCurrentWindowMetrics().getWindowInsets().getInsets(
                    WindowInsetsCompat.Type.systemBars());

            return systemBarInsets.left + systemBarInsets.right;
        }

        return (float) (displayMetrics.widthPixels * (1 - VISIBLE_SCREEN_PERCENTAGE));
    }


    /**
     * Returns the layout params for the AppStyledView dialog
     */
    public WindowManager.LayoutParams getDialogWindowLayoutParam(
            WindowManager.LayoutParams params) {
        DisplayMetrics displayMetrics = CarUiUtils.getDeviceDisplayMetrics(mContext);

        int maxWidth = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_width_max);
        int maxHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_height_max);

        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        int horizontalInset = (int) getHorizontalInset(displayMetrics);
        int verticalInset = (int) getVerticalInset(displayMetrics);

        int configuredWidth = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_width);
        int configuredHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_height);

        params.width = configuredWidth != 0 ? configuredWidth : Math.min(displayWidth, maxWidth);
        params.height = configuredHeight != 0 ? configuredHeight
                : Math.min(displayHeight, maxHeight);

        params.dimAmount = CarUiUtils.getFloat(mContext.getResources(),
                R.dimen.car_ui_app_styled_dialog_dim_amount);
        params.flags = params.flags | WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        switch (mSceneType) {
            case AppStyledDialogController.SceneType.ENTER:
                params.windowAnimations = R.style.Widget_CarUi_AppStyledView_WindowAnimations_Enter;
                break;
            case AppStyledDialogController.SceneType.EXIT:
                params.windowAnimations = R.style.Widget_CarUi_AppStyledView_WindowAnimations_Exit;
                break;
            case AppStyledDialogController.SceneType.INTERMEDIATE:
                params.windowAnimations =
                        R.style.Widget_CarUi_AppStyledView_WindowAnimations_Intermediate;
                break;
            case AppStyledDialogController.SceneType.SINGLE:
            default:
                params.windowAnimations = R.style.Widget_CarUi_AppStyledView_WindowAnimations;
                break;
        }

        int posX = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_position_x);
        int posY = mContext.getResources().getDimensionPixelSize(
                R.dimen.car_ui_app_styled_dialog_position_y);

        if (posX != 0 || posY != 0) {
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = posX;
            params.y = posY;

            return params;
        } else {
            params.x = 0;
            params.y = 0;
        }

        int minPaddingPx = (int) CarUiUtils.dpToPixel(mContext.getResources(),
                DIALOG_MIN_PADDING);

        if (params.width + horizontalInset >= displayWidth - (minPaddingPx * 2)) {
            params.width = displayWidth - horizontalInset - (minPaddingPx * 2);
        }

        if (params.height + verticalInset >= displayHeight - (minPaddingPx * 2)) {
            params.height = displayHeight - verticalInset - (minPaddingPx * 2);
        }

        int startMarginThresholdPx = (int) CarUiUtils.dpToPixel(mContext.getResources(),
                DIALOG_START_MARGIN_THRESHOLD);
        boolean isLandscape = mContext.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        int startMargin = (displayWidth - horizontalInset - params.width) / 2;

        if (isLandscape && startMargin >= startMarginThresholdPx) {
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = startMarginThresholdPx;
            params.y = (displayHeight - verticalInset - params.height) / 2;
        } else {
            params.gravity = Gravity.CENTER;
        }

        return params;
    }


    private void configureImeInsetFit() {
        Window window = getWindow();
        if (window == null) {
            return;
        }

        // Required inset API is unsupported. Fallback to resize behavior.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            return;
        }

        ViewCompat.setWindowInsetsAnimationCallback(window.getDecorView().getRootView(),
                new WindowInsetsAnimationCompat.Callback(
                        WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {

                    int mEndHeight;
                    int mStartHeight;
                    WindowManager.LayoutParams mAnimationLayoutParams;
                    int mContentBottomPadding;
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

                        window.setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                        mAnimationLayoutParams = new WindowManager.LayoutParams();
                        mAnimationLayoutParams.copyFrom(window.getAttributes());
                        mStartHeight = mAnimationLayoutParams.height;

                        int[] location = new int[2];
                        window.getDecorView().getRootView().getLocationOnScreen(location);
                        int x = location[0];
                        int y = location[1];

                        mContentBottomPadding = mContent.getPaddingBottom();

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
                        mIsImeShown = insets.getInsets(WindowInsetsCompat.Type.ime())
                                != Insets.NONE;
                        WindowManager.LayoutParams layoutParams = getDialogWindowLayoutParam(
                                window.getAttributes());

                        int resize = 0;
                        if (mIsImeShown) {
                            // Makes assumption that ime is shown on bottom of screen
                            int imeHeight = bounds.getUpperBound().bottom;

                            int[] location = new int[2];
                            window.getDecorView().getRootView().getLocationOnScreen(location);
                            int bottom = location[1] + layoutParams.height;

                            DisplayMetrics displayMetrics =
                                    CarUiUtils.getDeviceDisplayMetrics(mContext);

                            int imeTop = displayMetrics.heightPixels - imeHeight;
                            if (imeTop < bottom) {
                                resize = bottom - imeTop - mImeOverlapPx;
                            }
                        }

                        mEndHeight = layoutParams.height - resize;
                        return bounds;
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
                            mAnimationLayoutParams.height =
                                    (int) (mStartHeight - ((mStartHeight - mEndHeight)
                                            * imeAnimation.getInterpolatedFraction()));
                            window.setAttributes(mAnimationLayoutParams);
                            float imeOffset = mIsImeShown ? mImeOverlapPx
                                    * imeAnimation.getInterpolatedFraction()
                                    : -mImeOverlapPx * imeAnimation.getInterpolatedFraction();
                            mContent.setPadding(mContent.getPaddingLeft(),
                                    mContent.getPaddingTop(),
                                    mContent.getPaddingRight(),
                                    (int) (mContentBottomPadding + imeOffset));
                        }

                        return insets;
                    }

                    @Override
                    public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                        if (!mIsImeShown) {
                            updateAttributes();
                            copyWindowInsets();
                        }

                        super.onEnd(animation);
                    }
                });
    }

    @Override
    protected void onStart() {
        mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        mLifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);

        super.onStart();
    }

    @Override
    protected void onStop() {
        mLifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
        super.onStop();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        copyWindowInsets();
        copySystemUiVisibility();
        updateAttributes();
    }

    /**
     * Copy the visibility of the Activity that has started the dialog {@code mContext}. If the
     * activity is in Immersive mode the dialog will be in Immersive mode too and vice versa.
     */
    private void copySystemUiVisibility() {
        if (getWindow() == null) {
            return;
        }

        Activity activity = CarUiUtils.getActivity(mContext);

        getWindow().getDecorView().setSystemUiVisibility(
                activity.getWindow().getDecorView().getSystemUiVisibility());
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                i -> getWindow().getDecorView().setSystemUiVisibility(
                        activity.getWindow().getDecorView().getSystemUiVisibility()));
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

        Activity activity = CarUiUtils.getActivity(mContext);

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

            boolean isStatusBarVisible = windowInsets.isVisible(WindowInsets.Type.statusBars());
            if (!isStatusBarVisible) {
                dialogWindowInsetsController.hide(WindowInsetsCompat.Type.statusBars());
            }

            boolean isNavBarVisible = windowInsets.isVisible(WindowInsets.Type.navigationBars());
            if (!isNavBarVisible) {
                dialogWindowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
            }
        }
    }

    @Override
    public void show() {
        if (isShowing()) {
            return;
        }

        super.show();
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            focusedView.clearFocus();
        }
    }

    @Override
    public void setContentView(@NonNull View view) {
        initViewTreeOwners();
        mContent = view;
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

    public void setSceneType(int sceneType) {
        mSceneType = sceneType;
    }

    @Nullable
    public WindowManager.LayoutParams getWindowLayoutParams() {
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
