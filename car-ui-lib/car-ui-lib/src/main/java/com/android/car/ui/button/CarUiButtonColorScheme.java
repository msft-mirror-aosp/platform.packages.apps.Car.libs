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
package com.android.car.ui.button;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;

import java.lang.annotation.Retention;

/**
 * A class for specifying the color scheme of a {@link CarUiButton}.
 *
 * Apps should use one of {@link #BASIC} or the 4 primary colors in their buttons.
 */
public class CarUiButtonColorScheme {

    @Retention(SOURCE)
    @IntDef({TYPE_BASIC, TYPE_CUSTOM, TYPE_RED, TYPE_BLUE, TYPE_GREEN, TYPE_YELLOW})
    public @interface Type {}
    static final int TYPE_BASIC = 0;
    static final int TYPE_RED = 1;
    static final int TYPE_BLUE = 2;
    static final int TYPE_GREEN = 3;
    static final int TYPE_YELLOW = 4;
    static final int TYPE_CUSTOM = 5;

    public static final CarUiButtonColorScheme BASIC =
            new CarUiButtonColorScheme(TYPE_BASIC, 0);
    public static final CarUiButtonColorScheme RED =
            new CarUiButtonColorScheme(TYPE_RED, 0);
    public static final CarUiButtonColorScheme BLUE =
            new CarUiButtonColorScheme(TYPE_BLUE, 0);
    public static final CarUiButtonColorScheme GREEN =
            new CarUiButtonColorScheme(TYPE_GREEN, 0);
    public static final CarUiButtonColorScheme YELLOW =
            new CarUiButtonColorScheme(TYPE_YELLOW, 0);

    @Type
    private final int mType;
    @ColorInt
    private final int mCustomColor;

    private CarUiButtonColorScheme(@Type int type, int color) {
        mType = type;
        mCustomColor = color;
    }

    /**
     * Creates a new ButtonColorScheme from the given color integer. This should be
     * avoided where possible, and instead one of {@link #BASIC}, or the 4 primary colors
     * should be used, for maximum compatibility with OEM customizations.
     *
     * @param color The color to make the button. Alpha value from this color will be ignored.
     */
    public static CarUiButtonColorScheme fromColor(@ColorInt int color) {
        return new CarUiButtonColorScheme(TYPE_CUSTOM, color | 0xFF000000);
    }

    @Type
    int getType() {
        return mType;
    }

    @ColorInt
    int getCustomColor() {
        return mCustomColor;
    }
}
