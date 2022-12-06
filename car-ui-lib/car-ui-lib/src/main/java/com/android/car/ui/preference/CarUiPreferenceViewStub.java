/*
 * Copyright 2023 The Android Open Source Project
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

package com.android.car.ui.preference;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IntDef;

import com.android.car.ui.R;

import java.lang.annotation.Retention;

/**
 * A wrapper around preferences that would allow to define {@link PreferenceType}  based on which
 * layouts can be loaded dynamically using the plugins.
 */
@SuppressLint("Instantiatable")
public interface CarUiPreferenceViewStub {

    int PREFERENCE = 0;
    int DROPDOWN = 1;
    int SWITCH = 2;
    int TWO_ACTION = 3;
    int TWO_ACTION_TEXT = 4;
    int TWO_ACTION_TEXT_BORDERLESS = 5;
    int TWO_ACTION_ICON = 6;
    int TWO_ACTION_SWITCH = 7;
    int EDIT_TEXT = 8;
    int SEEKBAR_DIALOG = 9;
    int FOOTER = 10;
    int CATEGORY = 11;

    /**
     * Define preference type
     */
    @Retention(SOURCE)
    @IntDef({PREFERENCE, DROPDOWN, SWITCH, TWO_ACTION, TWO_ACTION_TEXT, TWO_ACTION_TEXT_BORDERLESS,
            TWO_ACTION_ICON, TWO_ACTION_SWITCH,
            EDIT_TEXT, SEEKBAR_DIALOG, FOOTER, CATEGORY})
    @interface PreferenceType {
    }

    /**
     * Inflates the default preference view for each preference type.
     */
    static View createCarUiPreferenceView(Context sourceContext, AttributeSet attrs) {
        TypedArray a = sourceContext.obtainStyledAttributes(attrs, R.styleable.CarUiPreference, 0,
                0);

        int preferenceType = getPreferenceType(sourceContext, attrs);
        int layoutResId = getPreferenceViewResLayoutId(preferenceType);

        a.recycle();

        LayoutInflater inflater = LayoutInflater.from(sourceContext);
        return inflater.inflate(layoutResId, null, false);
    }

    /**
     * Get the preference type.
     */
    static int getPreferenceType(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.Preference, 0, 0);

        int preferenceType = a.getInt(R.styleable.Preference_carUiPreferenceType, PREFERENCE);
        a.recycle();

        return preferenceType;
    }

    /**
     * Get default layout is.
     */
    static int getPreferenceViewResLayoutId(@PreferenceType int preferenceType) {
        switch (preferenceType) {
            case SWITCH:
                return R.layout.car_ui_preference_primary_switch_internal;
            case EDIT_TEXT:
                return R.layout.car_ui_preference_dialog_edittext_internal;
            case CATEGORY:
                return R.layout.car_ui_preference_category_internal;
            case DROPDOWN:
                return R.layout.car_ui_preference_dropdown_internal;
            case TWO_ACTION:
                return R.layout.car_ui_two_action_preference_internal;
            case TWO_ACTION_TEXT:
                return R.layout.car_ui_preference_two_action_text_internal;
            case TWO_ACTION_TEXT_BORDERLESS:
                return R.layout.car_ui_preference_two_action_text_borderless_internal;
            case TWO_ACTION_ICON:
                return R.layout.car_ui_preference_two_action_icon_internal;
            case TWO_ACTION_SWITCH:
                return R.layout.car_ui_preference_two_action_switch_internal;
            case SEEKBAR_DIALOG:
                return R.layout.car_ui_seekbar_dialog_internal;
            case FOOTER:
                return R.layout.car_ui_preference_footer_internal;
            default:
                return R.layout.car_ui_preference_internal;
        }
    }
}
