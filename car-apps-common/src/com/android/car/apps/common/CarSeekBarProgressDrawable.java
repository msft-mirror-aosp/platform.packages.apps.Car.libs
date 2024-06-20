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

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.InsetDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.apps.common.log.L;

/**
 * A Drawable to use in the progress of a slider.
 */
public class CarSeekBarProgressDrawable extends InsetDrawable {
    private static final String TAG = CarSeekBarProgressDrawable.class.getSimpleName();
    private static final int MAX_LEVEL = 10000; //The level range for a drawable is from 0 to 10000.

    /**
     * Creates a new inset drawable with the specified inset.
     */
    public CarSeekBarProgressDrawable() {
        super(null, 0);
    }

    /**
     * Creates a new inset drawable with the specified inset.
     */
    public CarSeekBarProgressDrawable(@Nullable Drawable drawable, int inset) {
        super(drawable, inset);
    }

    /**
     * Creates a new inset drawable with the specified inset.
     */
    public CarSeekBarProgressDrawable(@Nullable Drawable drawable, float inset) {
        super(drawable, inset);
    }

    /**
     * Creates a new inset drawable with the specified insets in pixels.
     */
    public CarSeekBarProgressDrawable(@Nullable Drawable drawable, int insetLeft, int insetTop,
            int insetRight, int insetBottom) {
        super(drawable, insetLeft, insetTop, insetRight, insetBottom);
    }

    /**
     * Creates a new inset drawable with the specified insets in fraction of the view bounds.
     */
    public CarSeekBarProgressDrawable(@Nullable Drawable drawable, float insetLeftFraction,
            float insetTopFraction, float insetRightFraction, float insetBottomFraction) {
        super(drawable, insetLeftFraction, insetTopFraction, insetRightFraction,
                insetBottomFraction);
    }


    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        onLevelChange(getLevel());
    }

    @Override
    protected boolean onLevelChange(int level) {
        update(level);
        return super.onLevelChange(level);
    }

    private void update(int level) {
        Rect rect = getBounds();
        if (getDrawable() != null) {
            // The calculation is needed to make sure a round icon is shown when the level is 0.
            getDrawable().setBounds(rect.left, rect.top,
                    (rect.width() - rect.height()) * level / MAX_LEVEL + rect.height(),
                    rect.bottom);
        } else {
            L.d(TAG, "The wrapped drawable is null.");
        }
    }

    @Nullable
    @Override
    public ConstantState getConstantState() {
        return new CarSeekBarProgressState(super.getConstantState());
    }

    static final class CarSeekBarProgressState extends ConstantState {

        private ConstantState mWrappedState;

        CarSeekBarProgressState(ConstantState constantState) {
            super();
            mWrappedState = constantState;
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            return newDrawable(null);
        }

        @NonNull
        @Override
        public Drawable newDrawable(Resources res) {
            return new CarSeekBarProgressDrawable(((DrawableWrapper) mWrappedState
                    .newDrawable(res)).getDrawable(), /* inset */ 0);
        }

        @NonNull
        @Override
        public Drawable newDrawable(@Nullable Resources res,
                @Nullable @SuppressWarnings("unused") Resources.Theme theme) {
            return new CarSeekBarProgressDrawable(((DrawableWrapper) mWrappedState
                    .newDrawable(res)).getDrawable(), /* inset */ 0);
        }

        /**
         * Return a bit mask of configuration changes that will impact
         * this drawable (and thus require completely reloading it).
         */
        @Override
        public int getChangingConfigurations() {
            return mWrappedState.getChangingConfigurations();
        }
    }
}
