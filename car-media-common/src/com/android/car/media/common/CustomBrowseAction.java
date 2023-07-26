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

import android.net.Uri;
import android.os.Bundle;

import com.android.car.apps.common.imaging.ImageBinder;
import com.android.car.apps.common.imaging.UriArtRef;

import java.util.Objects;

/** Domain Model for Browse View custom actions */
public class CustomBrowseAction {

    private final String mActionId;
    private final String mLabel;
    private final Uri mIconUrl;
    private final UriArtRef mArtRef;
    private final Bundle mExtras;

    public CustomBrowseAction(String actionId, String label, Uri iconUrl, Bundle extras) {
        mActionId = actionId;
        mLabel = label;
        mIconUrl = iconUrl;
        mExtras = extras;
        mArtRef = new UriArtRef(mIconUrl);
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

    public ImageBinder.ImageRef getArtRef() {
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
