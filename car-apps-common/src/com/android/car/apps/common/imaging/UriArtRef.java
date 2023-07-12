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

package com.android.car.apps.common.imaging;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;


/** Art Ref for simple icons without placeholders. */
public class UriArtRef implements ImageBinder.ImageRef {

    private final Uri mIconUri;

    public UriArtRef(@Nullable Uri iconUri) {
        mIconUri = iconUri;
    }

    @Override
    public boolean equals(Context context, Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UriArtRef other = (UriArtRef) o;
        return Objects.equals(mIconUri, other.mIconUri);
    }

    @Nullable
    @Override
    public Uri getImageURI() {
        return mIconUri;
    }

    @Override
    public Drawable getPlaceholder(Context context, @NonNull ImageBinder.PlaceholderType type) {
        return null;
    }
}
