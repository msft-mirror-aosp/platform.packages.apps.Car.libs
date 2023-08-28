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

package com.android.car.media.common.source;

import android.content.ComponentName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleObserver;


// TODO: delete class after removing usage from radio.
/** @deprecated */
@Deprecated
public class MediaTrampolineHelper implements LifecycleObserver {

    /** @deprecated */
    @Deprecated
    public MediaTrampolineHelper(@NonNull FragmentActivity activity) {
    }

    /** @deprecated */
    @Deprecated
    public void setLaunchedMediaSource(@Nullable ComponentName launchedSourceComp) {
    }
}
