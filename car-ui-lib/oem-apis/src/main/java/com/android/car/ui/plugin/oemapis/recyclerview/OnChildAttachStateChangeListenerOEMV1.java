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
package com.android.car.ui.plugin.oemapis.recyclerview;

import android.view.View;

import com.android.car.ui.plugin.oemapis.NonNull;

/** See {@code androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListenerOEMV1} */
public interface OnChildAttachStateChangeListenerOEMV1 {

    /**
     * see {@code RecyclerView.OnChildAttachStateChangeListenerOEMV1#onChildViewAttachedToWindow}
     */
    void onChildViewAttachedToWindow(@NonNull View view);

    /**
     * see {@code RecyclerView.OnChildAttachStateChangeListenerOEMV1#onChildViewDetachedFromWindow}
     */
    void onChildViewDetachedFromWindow(@NonNull View view);
}
