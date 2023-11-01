/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.android.car.ui.pluginsupport;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.car.ui.FocusArea;
import com.android.car.ui.FocusAreaAdapterV1;
import com.android.car.ui.FocusParkingView;
import com.android.car.ui.FocusParkingViewAdapterV1;
import com.android.car.ui.appstyledview.AppStyledViewController;
import com.android.car.ui.appstyledview.AppStyledViewControllerAdapterV1;
import com.android.car.ui.appstyledview.AppStyledViewControllerImpl;
import com.android.car.ui.baselayout.Insets;
import com.android.car.ui.baselayout.InsetsChangedListener;
import com.android.car.ui.plugin.oemapis.FocusAreaOEMV1;
import com.android.car.ui.plugin.oemapis.FocusParkingViewOEMV1;
import com.android.car.ui.plugin.oemapis.InsetsOEMV1;
import com.android.car.ui.plugin.oemapis.PluginFactoryOEMV1;
import com.android.car.ui.plugin.oemapis.appstyledview.AppStyledViewControllerOEMV1;
import com.android.car.ui.plugin.oemapis.toolbar.ToolbarControllerOEMV1;
import com.android.car.ui.recyclerview.CarUiListItem;
import com.android.car.ui.recyclerview.CarUiRecyclerView;
import com.android.car.ui.toolbar.ToolbarController;
import com.android.car.ui.toolbar.ToolbarControllerAdapterV1;
import com.android.car.ui.widget.CarUiTextView;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is a wrapper around {@link PluginFactoryOEMV1} that implements {@link
 * PluginFactory}, to provide a version-agnostic way of interfacing with the OEM's
 * PluginFactory.
 */
public final class PluginFactoryAdapterV1 implements PluginFactory {
    @NonNull
    private final PluginFactoryOEMV1 mOem;
    @NonNull
    private final PluginFactoryStub mFactoryStub = new PluginFactoryStub();

    public PluginFactoryAdapterV1(@NonNull PluginFactoryOEMV1 oem) {
        mOem = oem;

        mOem.setRotaryFactories(
                // TODO (b/304841988) Provide full Function definitions instead of a lambda as a
                //  workaround to avoid r8 stripping this code during optimization, which prevents a
                //  crash when an app relies on this callback
                new Function<Context, FocusParkingViewOEMV1>() {
                    @Override
                    public FocusParkingViewOEMV1 apply(Context context) {
                        return new FocusParkingViewAdapterV1(new FocusParkingView(context));
                    }
                },
                new Function<Context, FocusAreaOEMV1>() {
                    @Override
                    public FocusAreaOEMV1 apply(Context context) {
                        return new FocusAreaAdapterV1(new FocusArea(context));
                    }
                });
    }

    @Nullable
    @Override
    public ToolbarController installBaseLayoutAround(
            @NonNull Context context,
            @NonNull View contentView,
            @Nullable InsetsChangedListener insetsChangedListener,
            boolean toolbarEnabled,
            boolean fullscreen) {

        if (!mOem.customizesBaseLayout()) {
            return mFactoryStub.installBaseLayoutAround(context, contentView,
                    insetsChangedListener, toolbarEnabled, fullscreen);
        }

        ToolbarControllerOEMV1 toolbar = mOem.installBaseLayoutAround(
                context,
                contentView,
                // TODO (b/304841988) Provide full Consumer definition instead of a lambda as a
                //  workaround to avoid r8 stripping this code during optimization, which prevents a
                //  crash when an app relies on this callback
                new Consumer<InsetsOEMV1>() {
                    @Override
                    public void accept(@NonNull InsetsOEMV1 insets) {
                        if (insetsChangedListener != null) {
                            insetsChangedListener.onCarUiInsetsChanged(adaptInsets(insets));
                        }
                    }
                },
                toolbarEnabled, fullscreen);

        if (toolbar != null) {
            return new ToolbarControllerAdapterV1(context,  toolbar);
        }

        if (toolbarEnabled) {
            return mFactoryStub.installBaseLayoutAround(context, contentView, insetsChangedListener,
                    toolbarEnabled, fullscreen);
        }

        return null;
    }

    @NonNull
    @Override
    public CarUiTextView createTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        return mFactoryStub.createTextView(context, attrs);
    }

    @NonNull
    @Override
    public View createCarUiPreferenceView(@NonNull Context context, @NonNull AttributeSet attrs) {
        return mFactoryStub.createCarUiPreferenceView(context, attrs);
    }

    @NonNull
    @Override
    public AppStyledViewController createAppStyledView(@NonNull Context activityContext) {
        AppStyledViewControllerOEMV1 appStyledViewControllerOEMV1 = mOem.createAppStyledView(
                activityContext);
        return appStyledViewControllerOEMV1 == null ? new AppStyledViewControllerImpl(
                activityContext) : new AppStyledViewControllerAdapterV1(
                appStyledViewControllerOEMV1);
    }

    private Insets adaptInsets(InsetsOEMV1 insetsOEM) {
        return new Insets(insetsOEM.getLeft(), insetsOEM.getTop(),
                insetsOEM.getRight(), insetsOEM.getBottom());
    }

    @NonNull
    @Override
    public CarUiRecyclerView createRecyclerView(@NonNull Context context,
            @Nullable AttributeSet attrs) {
        return mFactoryStub.createRecyclerView(context, attrs);
    }

    @NonNull
    @Override
    public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> createListItemAdapter(
            @NonNull List<? extends CarUiListItem> items) {
        return mFactoryStub.createListItemAdapter(items);
    }
}
