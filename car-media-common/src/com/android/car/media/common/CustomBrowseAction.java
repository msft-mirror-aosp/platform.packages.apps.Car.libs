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

package com.android.car.media.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.apps.common.imaging.ImageBinder;

import java.util.Objects;

/** Domain Model for Browse View custom actions */
public class CustomBrowseAction {

    /** Art Ref for Browse Custom Actions icons. */
    public class BrowseActionArtRef implements ImageBinder.ImageRef {

        @Override
        public boolean equals(Context context, Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomBrowseAction.BrowseActionArtRef other = (CustomBrowseAction.BrowseActionArtRef) o;

            Uri myUri = getImageURI();
            Uri otherUri = other.getImageURI();
            if ((myUri != null) || (otherUri != null)) {
                return Objects.equals(myUri, otherUri);
            }
            return Objects.equals(this, o);
        }

        @Nullable
        @Override
        public Uri getImageURI() {
            return mIconUrl;
        }

        @Nullable
        @Override
        public Drawable getImage(Context context) {
            return ImageBinder.ImageRef.super.getImage(context);
        }

        @Override
        public Drawable getPlaceholder(Context context, @NonNull ImageBinder.PlaceholderType type) {
            return null;
        }
    }

    private final String mActionId;
    private final String mLabel;
    private final Uri mIconUrl;
    private final BrowseActionArtRef mArtRef = new BrowseActionArtRef();
    private final Bundle mExtras;

    public CustomBrowseAction(
            String actionId,
            String mLabel,
            Uri mIconUrl,
            Bundle extras) {
        this.mActionId = actionId;
        this.mLabel = mLabel;
        this.mIconUrl = mIconUrl;
        this.mExtras = extras;
    }

    public String getId() {
        return mActionId;
    }

    public String getLabel() {
        return mLabel;
    }

    public Uri getIconUrl() {
        return mIconUrl;
    }

    public BrowseActionArtRef getArtRef() {
        return mArtRef;
    }

    public Bundle getExtras() {
        return mExtras;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomBrowseAction)) {
            return false;
        }
        CustomBrowseAction that = (CustomBrowseAction) o;
        return Objects.equals(mActionId, that.mActionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mActionId);
    }
}
