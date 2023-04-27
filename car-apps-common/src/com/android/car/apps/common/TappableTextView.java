/*
 * Copyright (C) 2023 The Android Open Source Project
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


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


/** TODO(b/260913934) implement links rendering in car-ui-lib. */
@SuppressLint("AppCompatCustomView")
public class TappableTextView extends TextView {

    private final int mHideViewMode;
    private final int mEndIconResId;
    private final int mTappableStyle;
    private final int mNormalStyle;

    private boolean mDrawLinkIcon;
    private Drawable mEndIcon;
    private final Rect mEndIconRect = new Rect();

    public TappableTextView(@NonNull Context context) {
        this(context, null);
    }

    public TappableTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TappableTextView(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.TappableTextView, defStyleAttr, 0);
        try {
            mEndIconResId = a.getResourceId(R.styleable.TappableTextView_endDrawable,
                    R.drawable.ic_link_indicator);

            mTappableStyle = a.getResourceId(R.styleable.TappableTextView_tappableTextStyle, 0);
            mNormalStyle = a.getResourceId(R.styleable.TappableTextView_normalTextStyle, 0);

            mHideViewMode = a.getInteger(R.styleable.TappableTextView_hideViewMode, INVISIBLE);
        } finally {
            a.recycle();
        }
    }

    /**
     * Sets the visibility of the view to:
     *   {@link View#VISIBLE} if hidden is false
     *   {@link View#INVISIBLE} if the view has no hideViewMode attribute
     *   {@link View#INVISIBLE} or {@link View#GONE} as specified by the attribute.
     */
    public void hideView(boolean hidden) {
        setVisibility(hidden ? mHideViewMode : VISIBLE);
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener l) {
        super.setOnClickListener(l);
        onNewClickListener(l);
    }

    /** Protected so OEMs can extend the view and customize the behavior further. */
    protected void onNewClickListener(@Nullable View.OnClickListener l) {
        mDrawLinkIcon = (l != null);

        Context ctx = getContext();
        int styleId;
        int drawableId;
        if (l != null) {
            styleId = mTappableStyle;
            TypedValue value = new TypedValue();
            ctx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
            drawableId = value.resourceId;
        } else {
            styleId = mNormalStyle;
            drawableId = 0;
        }

        setBackgroundResource(drawableId);
        setTextAppearance(styleId);
        invalidate();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setCompoundDrawablesRelative(null, null, null, null);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mDrawLinkIcon) {
            if (mEndIcon == null && mEndIconResId != 0) {
                mEndIcon = ContextCompat.getDrawable(getContext(), mEndIconResId);
            }
            if (mEndIcon != null) {
                int height = getMeasuredHeight();
                //noinspection SuspiciousNameCombination
                mEndIconRect.set(0, 0, height, height);
                mEndIcon.setBounds(mEndIconRect);
                setCompoundDrawablesRelative(null, null, mEndIcon, null);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }
}
