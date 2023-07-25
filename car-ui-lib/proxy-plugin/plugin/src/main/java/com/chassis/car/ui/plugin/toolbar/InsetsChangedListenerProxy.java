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

package com.chassis.car.ui.plugin.toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.ui.baselayout.Insets;
import com.android.car.ui.baselayout.InsetsChangedListener;
import com.android.car.ui.plugin.oemapis.InsetsOEMV1;

/**
 * Wrapper class which converts Consumer<InsetsOEMV1> to InsetsChangedListener.
 */
public final class InsetsChangedListenerProxy implements InsetsChangedListener {
    @Nullable
    private com.android.car.ui.plugin.oemapis.Consumer<InsetsOEMV1>
            mPluginInsetsChangedListener = null;
    @Nullable
    private java.util.function.Consumer<InsetsOEMV1> mJavaInsetsChangedListener = null;

    /** Compatible with {@code ToolbarControllerOEMV2} and {@code ToolbarControllerOEMV3}. */
    public InsetsChangedListenerProxy(
            @NonNull com.android.car.ui.plugin.oemapis.Consumer<InsetsOEMV1>
                    insetsChangedListener) {
        mPluginInsetsChangedListener = insetsChangedListener;
    }

    /** Compatible with {@code ToolbarControllerOEMV1}. */
    public InsetsChangedListenerProxy(
            @NonNull java.util.function.Consumer<InsetsOEMV1> insetsChangedListener) {
        mJavaInsetsChangedListener = insetsChangedListener;
    }

    @Override
    public void onCarUiInsetsChanged(@NonNull Insets insets) {
        if (mPluginInsetsChangedListener != null) {
            mPluginInsetsChangedListener.accept(new InsetsOEMV1(
                    insets.getLeft(), insets.getTop(), insets.getRight(), insets.getBottom()));
        } else {
            mJavaInsetsChangedListener.accept(new InsetsOEMV1(
                    insets.getLeft(), insets.getTop(), insets.getRight(), insets.getBottom()));
        }
    }
}
