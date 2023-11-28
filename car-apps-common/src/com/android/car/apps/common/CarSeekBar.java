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

package com.android.car.apps.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.android.car.ui.uxr.DrawableStateSeekBar;

/**
 * This is a definition for a {@link SeekBar} that has a AAOS design and support rotary function.
 */
public class CarSeekBar extends DrawableStateSeekBar {
    private Context mContext;
    private CarSeekBarRotaryHelper mCarSeekBarRotaryHelper;
    private Drawable mIcon;
    private InsetDrawable mIconContainer;

    public CarSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        LayerDrawable sliderIconDrawable = (LayerDrawable) mContext.getDrawable(
                R.drawable.car_seekbar_slider_icon);
        mIconContainer = (InsetDrawable) sliderIconDrawable.findDrawableByLayerId(
                R.id.seekbar_progress_icon);

        setSplitTrack(false);
        super.setThumb(null);
        setBackground(mContext.getDrawable(R.drawable.car_seekbar_rotary_selector));
        mCarSeekBarRotaryHelper = new CarSeekBarRotaryHelper(this, isClickable());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        super.setProgressDrawable(mContext.getDrawable(R.drawable.car_seekbar_progress));
        setIcon(mIcon);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        adjustSize();
    }

    private void adjustSize() {
        // Add small padding to prevent cutoff of the rotary highlight ring.
        int defaultPadding =
                mContext.getResources().getDimensionPixelSize(R.dimen.car_seekbar_padding);
        int paddingLeft = getPaddingLeft() >= defaultPadding ? getPaddingLeft() : defaultPadding;
        int paddingRight = getPaddingRight() >= defaultPadding ? getPaddingRight() : defaultPadding;
        setPadding(paddingLeft, getPaddingTop(), paddingRight, getPaddingBottom());
    }

    /**
     * This thumb will be the icon on the slider.
     */
    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(null);
        setIcon(thumb);
    }

    @Override
    public Drawable getThumb() {
        return mIcon;
    }

    /**
     * Sets to the on status icon.
     */
    public void setIcon(Drawable icon) {
        mIcon = icon;
        if (mIconContainer != null) {
            mIconContainer.setDrawable(mIcon);
            invalidate();
        }
    }

    @Override
    public void setClickable(boolean adjustable) {
        super.setClickable(adjustable);
        if (mCarSeekBarRotaryHelper != null) {
            mCarSeekBarRotaryHelper.setAdjustable(adjustable);
        }
    }

    @Override
    public void setProgressDrawable(Drawable d) {
        // Default progress drawable has been set.
    }
}
