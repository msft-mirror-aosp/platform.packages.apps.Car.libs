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

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;

import androidx.annotation.NonNull;

/** Wrapper class to round the corners of drawables */
public class RoundedDrawable extends DrawableWrapper {

    private final float mCornerRatio;
    private Path mClippedPath;

    public RoundedDrawable(Drawable drawable, float cornerRatio) {
        super(drawable);
        mCornerRatio = cornerRatio;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }

        // Clip the canvas to the path before drawing
        canvas.clipPath(mClippedPath);
        getDrawable().draw(canvas);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (getDrawable() == null) {
            return;
        }

        mClippedPath = new Path();
        RectF rect = new RectF(getBounds());
        float adjustedCornerRadius = mCornerRatio * Math.min(getDrawable().getIntrinsicHeight(),
                getDrawable().getIntrinsicWidth());
        mClippedPath.addRoundRect(rect, adjustedCornerRadius, adjustedCornerRadius,
                Path.Direction.CW);
    }
}
