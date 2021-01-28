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
package com.android.car.ui.sharedlibrarysupport;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.ui.baselayout.Insets;
import com.android.car.ui.baselayout.InsetsChangedListener;
import com.android.car.ui.button.CarUiButton;
import com.android.car.ui.button.CarUiButtonAttributes;
import com.android.car.ui.recyclerview.CarUiRecyclerView;
import com.android.car.ui.sharedlibrary.oemapis.InsetsOEMV1;
import com.android.car.ui.sharedlibrary.oemapis.SharedLibraryFactoryOEMV1;
import com.android.car.ui.sharedlibrary.oemapis.toolbar.ToolbarControllerOEMV1;
import com.android.car.ui.toolbar.ToolbarController;
import com.android.car.ui.toolbar.ToolbarControllerAdapterV1;

/**
 * This class is an wrapper around {@link SharedLibraryFactoryOEMV1} that implements
 * {@link SharedLibraryFactory}, to provide a version-agnostic way of interfacing with
 * the OEM's SharedLibraryFactory.
 */
public final class SharedLibraryFactoryAdapterV1 implements SharedLibraryFactory {

    SharedLibraryFactoryOEMV1 mOem;
    SharedLibraryFactoryStub mFactoryStub = new SharedLibraryFactoryStub();

    public SharedLibraryFactoryAdapterV1(SharedLibraryFactoryOEMV1 oem) {
        mOem = oem;
    }

    @Override
    @Nullable
    public ToolbarController installBaseLayoutAround(
            View contentView,
            InsetsChangedListener insetsChangedListener,
            boolean toolbarEnabled) {
        ToolbarControllerOEMV1 toolbar = mOem.installBaseLayoutAround(contentView,
                insets -> insetsChangedListener.onCarUiInsetsChanged(adaptInsets(insets)),
                toolbarEnabled, true);

        return toolbar != null
                ? new ToolbarControllerAdapterV1(contentView.getContext(), toolbar)
                : null;
    }

    @NonNull
    @Override
    public CarUiButton createButton(Context context, @Nullable CarUiButtonAttributes attrs) {
        // TODO(b/172345817) Create OEM APIs for this and call them from here
        return mFactoryStub.createButton(context, attrs);
    }

    private Insets adaptInsets(InsetsOEMV1 insetsOEM) {
        return new Insets(insetsOEM.getLeft(), insetsOEM.getTop(),
                insetsOEM.getRight(), insetsOEM.getBottom());
    }

    @Override
    public CarUiRecyclerView createRecyclerView(Context context, AttributeSet attrs) {
        // TODO(b/177687696): implement the adapter
        return mFactoryStub.createRecyclerView(context, attrs);
    }
}