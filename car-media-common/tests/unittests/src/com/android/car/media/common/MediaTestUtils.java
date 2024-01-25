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

package com.android.car.media.common;

import android.content.ComponentName;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.media.session.MediaControllerCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.apps.common.IconCropper;
import com.android.car.media.common.source.MediaSource;

public class MediaTestUtils {

    private MediaTestUtils() {
    }

    /** Creates a fake {@link MediaSource} with browse service. */
    public static MediaSource newFakeMediaSource(@NonNull String pkg, @NonNull String cls) {
        return newFakeMediaSource(new ComponentName(pkg, cls), null);
    }

    /** Creates a fake {@link MediaSource} with media controller. */
    public static MediaSource newFakeMediaSource(MediaControllerCompat mediaController) {
        return newFakeMediaSource(null, mediaController);
    }

    /** Creates a fake {@link MediaSource}. */
    public static MediaSource newFakeMediaSource(@Nullable ComponentName browseService,
            @Nullable MediaControllerCompat mediaController) {
        String displayName = browseService == null ? "" : browseService.getClassName();
        Drawable icon = new ColorDrawable();
        IconCropper iconCropper = new IconCropper(new Path());
        return new MediaSource(browseService, mediaController, displayName, icon, iconCropper);
    }
}
