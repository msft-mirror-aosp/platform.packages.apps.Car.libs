/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.google.car.ui.sharedlibrary.uxr;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Switch;

import androidx.annotation.Nullable;

/**
 * A {@link Switch} that implements {@link DrawableStateView}, for allowing additional states
 * such as ux restriction.
 */
public class DrawableStateSwitch extends Switch implements DrawableStateView {
    private DrawableStateUtil mUtil;

    public DrawableStateSwitch(Context context) {
        super(context);
    }

    public DrawableStateSwitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableStateSwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DrawableStateSwitch(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setExtraDrawableState(@Nullable int[] stateToAdd, @Nullable int[] stateToRemove) {
        if (mUtil == null) {
            mUtil = new DrawableStateUtil(this);
        }
        mUtil.setExtraDrawableState(stateToAdd, stateToRemove);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        if (mUtil == null) {
            mUtil = new DrawableStateUtil(this);
        }
        return mUtil.onCreateDrawableState(extraSpace, space -> super.onCreateDrawableState(space));
    }
}
