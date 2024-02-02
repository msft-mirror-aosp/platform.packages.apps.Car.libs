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
package com.android.car.media.common.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/** ViewModel used to track state of media widgets that use the {@link MediaWidgetController} */
public class MediaWidgetViewModel extends AndroidViewModel {

    private boolean mQueueVisible = false;
    private boolean mHistoryVisible = false;
    private boolean mOverflowExpanded = false;

    public MediaWidgetViewModel(@NonNull Application application) {
        super(application);
    }

    public void setQueueVisible(boolean visible) {
        mQueueVisible = visible;
    }

    public boolean getQueueVisible() {
        return mQueueVisible;
    }

    public void setHistoryVisible(boolean visible) {
        mHistoryVisible = visible;
    }

    public boolean getHistoryVisible() {
        return mHistoryVisible;
    }

    public void setOverflowExpanded(boolean expanded) {
        mOverflowExpanded = expanded;
    }

    public boolean getOverflowExpanded() {
        return mOverflowExpanded;
    }
}
