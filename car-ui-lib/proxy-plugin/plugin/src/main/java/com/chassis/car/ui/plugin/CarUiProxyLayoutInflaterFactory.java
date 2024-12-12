/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.chassis.car.ui.plugin;

import static com.android.car.ui.preference.CarUiPreferenceViewStub.CATEGORY;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.DROPDOWN;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.EDIT_TEXT;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.FOOTER;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.PREFERENCE;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.SEEKBAR_DIALOG;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.SWITCH;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.TWO_ACTION;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.TWO_ACTION_ICON;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.TWO_ACTION_SWITCH;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.TWO_ACTION_TEXT;
import static com.android.car.ui.preference.CarUiPreferenceViewStub.TWO_ACTION_TEXT_BORDERLESS;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatViewInflater;

import com.android.car.ui.plugin.PluginUiContextFactory;
import com.android.car.ui.plugin.oemapis.FocusAreaOEMV1;
import com.android.car.ui.plugin.oemapis.FocusParkingViewOEMV1;
import com.android.car.ui.plugin.oemapis.Function;
import com.android.car.ui.pluginsupport.PluginFactoryStub;
import com.android.car.ui.preference.CarUiPreferenceViewStub;
import com.android.car.ui.recyclerview.CarUiRecyclerView;
import com.android.car.ui.widget.CarUiTextView;

/**
 * A custom {@link LayoutInflater.Factory2} that will create CarUi components such as {@link
 * CarUiTextView}. It extends AppCompatViewInflater so that it can still let AppCompat
 * components be created correctly.
 */
public class CarUiProxyLayoutInflaterFactory extends AppCompatViewInflater implements
        LayoutInflater.Factory2 {

    @NonNull
    private final PluginFactoryStub mFactoryStub = new PluginFactoryStub();

    @Nullable
    private PluginUiContextFactory mPluginUiContextFactory;
    @Nullable
    private Function<Context, FocusParkingViewOEMV1> mFocusParkingViewFactory;
    @Nullable
    private Function<Context, FocusAreaOEMV1> mFocusAreaFactory;

    /**
     * Sets factory classes for Rotary classes. They needs to be called using the app context;
     */
    public void setRotaryFactories(PluginUiContextFactory pluginUiContextFactory,
            Function<Context, FocusParkingViewOEMV1> focusParkingViewFactory,
            Function<Context, FocusAreaOEMV1> focusAreaFactory) {
        mPluginUiContextFactory = pluginUiContextFactory;
        mFocusParkingViewFactory = focusParkingViewFactory;
        mFocusAreaFactory = focusAreaFactory;
    }

    @Override
    @Nullable
    protected View createView(Context context, String name, AttributeSet attrs) {
        if ("CarUiTextView".equals(name)) {
            return mFactoryStub.createTextView(context, attrs);
        }
        if ("TextView".equals(name)) {
            return mFactoryStub.createTextView(context, attrs);
        }
        if (CarUiRecyclerView.class.getName().equals(name)) {
            return mFactoryStub.createRecyclerView(context, attrs).getView();
        }
        if (CarUiPreferenceViewStub.class.getName().equals(name)) {
            return getCarUiPreferenceView(context, attrs);
        }
        if ("com.android.car.ui.FocusParkingView".equals(name)
                && mFocusParkingViewFactory != null && mPluginUiContextFactory != null) {
            return ((FocusParkingViewOEMV1) mFocusParkingViewFactory
                    .apply(mPluginUiContextFactory.getRecentAppUiContext())).getView();
        }
        if ("com.android.car.ui.FocusArea".equals(name)
                && mFocusAreaFactory != null && mPluginUiContextFactory != null) {
            return ((FocusAreaOEMV1) mFocusAreaFactory
                    .apply(mPluginUiContextFactory.getRecentAppUiContext())).getView();
        }

        return null;
    }

    private View getCarUiPreferenceView(Context context, AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(context);
        int preferenceType = getPreferenceType(context, attrs);
        return inflater.inflate(getPreferenceViewResLayoutId(preferenceType), null, false);
    }

    int getPreferenceType(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(
                attrs, com.chassis.car.ui.plugin.R.styleable.Preference, 0, 0);

        int preferenceType = a.getInt(
                com.chassis.car.ui.plugin.R.styleable.Preference_carUiPreferenceType,
                PREFERENCE);
        a.recycle();
        return preferenceType;
    }

    private int getPreferenceViewResLayoutId(
            @CarUiPreferenceViewStub.PreferenceType int preferenceType) {
        switch (preferenceType) {
            case SWITCH:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_preference_primary_switch_internal;
            case EDIT_TEXT:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_preference_dialog_edittext_internal;
            case CATEGORY:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_preference_category_internal;
            case DROPDOWN:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_preference_dropdown_internal;
            case TWO_ACTION:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_two_action_preference_internal;
            case TWO_ACTION_TEXT:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_preference_two_action_text_internal;
            case TWO_ACTION_TEXT_BORDERLESS:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_preference_two_action_text_borderless_internal;
            case TWO_ACTION_ICON:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_preference_two_action_icon_internal;
            case TWO_ACTION_SWITCH:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_preference_two_action_switch_internal;
            case SEEKBAR_DIALOG:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_seekbar_dialog_internal;
            case FOOTER:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_preference_footer_internal;
            default:
                return com.chassis.car.ui.plugin
                        .R.layout.car_ui_preference_internal;
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // Deprecated, do nothing.
        return null;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return createView(context, name, attrs);
    }
}
