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

package com.android.car.media.common;

import static com.android.car.media.common.MediaConstants.KEY_CONTENT_FORMAT_DARK_MODE_LARGE_ICON_URI;
import static com.android.car.media.common.MediaConstants.KEY_CONTENT_FORMAT_DARK_MODE_SMALL_ICON_URI;
import static com.android.car.media.common.MediaConstants.KEY_CONTENT_FORMAT_LIGHT_MODE_LARGE_ICON_URI;
import static com.android.car.media.common.MediaConstants.KEY_CONTENT_FORMAT_LIGHT_MODE_SMALL_ICON_URI;
import static com.android.car.media.common.MediaConstants.KEY_CONTENT_FORMAT_TINTABLE_LARGE_ICON_URI;
import static com.android.car.media.common.MediaConstants.KEY_CONTENT_FORMAT_TINTABLE_SMALL_ICON_URI;
import static com.android.car.media.common.R.styleable.ContentFormatView;
import static com.android.car.media.common.R.styleable.ContentFormatView_backgroundTone;
import static com.android.car.media.common.R.styleable.ContentFormatView_logoSize;
import static com.android.car.media.common.R.styleable.ContentFormatView_logoTint;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Preconditions;


/**
 * Renders one of the content format logos defined in the {@link MediaItemMetadata}'s extras.
 * View attributes:
 * <li> ContentFormatView_logoSize determines whether the view will show a small or a large
 * logo (if present in the extras).</li>
 * <li> ContentFormatView_logoTint specifies the color the logo will be tinted with, IIF a tintable
 * logo has been provided.</li>
 * <li> ContentFormatView_backgroundTone is considered when no tintable logo was provided, and will
 * select the logo matching the tone. </li>
 */
public class ContentFormatView extends ImageView {

    private static final int LOGO_LARGE = 1;

    private static final String [] KEYS_FOR_SMALL_LOGO = {
            KEY_CONTENT_FORMAT_TINTABLE_SMALL_ICON_URI, KEY_CONTENT_FORMAT_DARK_MODE_SMALL_ICON_URI,
            KEY_CONTENT_FORMAT_LIGHT_MODE_SMALL_ICON_URI };

    private static final String [] KEYS_FOR_LARGE_LOGO = {
            KEY_CONTENT_FORMAT_TINTABLE_LARGE_ICON_URI, KEY_CONTENT_FORMAT_DARK_MODE_LARGE_ICON_URI,
            KEY_CONTENT_FORMAT_LIGHT_MODE_LARGE_ICON_URI };

    private final int mLogoTint;
    private final int mLogoSize;
    private final int mBackgroundTone;

    public ContentFormatView(@NonNull Context context) {
        this(context, null);
    }

    public ContentFormatView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentFormatView(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, ContentFormatView,
                defStyleAttr, 0);
        try {
            mLogoTint = a.getColor(ContentFormatView_logoTint, Color.RED);
            mLogoSize = a.getInteger(ContentFormatView_logoSize, -1);
            mBackgroundTone = a.getInteger(ContentFormatView_backgroundTone, -1);

            Preconditions.checkArgument(mLogoSize != -1, "Invalid logo size");
            Preconditions.checkArgument(mBackgroundTone != -1, "Invalid background tone");

        } finally {
            a.recycle();
        }
    }

    /** Selects the right logo to display and adjusts the tint of the view. **/
    public Uri prepareToDisplay(MediaItemMetadata metadata) {
        String[] keys = (mLogoSize == LOGO_LARGE) ? KEYS_FOR_LARGE_LOGO : KEYS_FOR_SMALL_LOGO;
        String uri = metadata.getStringProperty(keys[0]);
        if (TextUtils.isEmpty(uri)) {
            setImageTintList(null);
            uri = metadata.getStringProperty(keys[1 + mBackgroundTone]);
        } else {
            setImageTintList(ColorStateList.valueOf(mLogoTint));
        }

        return TextUtils.isEmpty(uri) ? null : Uri.parse(uri);
    }
}
