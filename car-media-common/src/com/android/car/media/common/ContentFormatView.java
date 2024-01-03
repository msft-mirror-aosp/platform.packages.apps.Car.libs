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

import static com.android.car.media.common.MediaConstants.KEY_CONTENT_FORMAT_TINTABLE_LARGE_ICON_URI;
import static com.android.car.media.common.MediaConstants.KEY_CONTENT_FORMAT_TINTABLE_SMALL_ICON_URI;
import static com.android.car.media.common.R.styleable.ContentFormatView;
import static com.android.car.media.common.R.styleable.ContentFormatView_logoSize;
import static com.android.car.media.common.R.styleable.ContentFormatView_logoTint;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Renders one of the content format logos defined in the {@link MediaItemMetadata}'s extras.
 * View attributes:
 * <li> ContentFormatView_logoSize determines whether the view will show a small or a large
 * logo (if present in the extras).</li>
 * <li> ContentFormatView_logoTint specifies the color the logo will be tinted with, IIF a tintable
 * logo has been provided.</li>
 */
public class ContentFormatView extends ImageView {

    private static final int LOGO_LARGE = 1;

    private final int mLogoTint;
    private final int mLogoSize;

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
        mLogoTint = a.getColor(ContentFormatView_logoTint,
            context.getResources().getColor(R.color.default_logoTint, null));
        mLogoSize = a.getInteger(ContentFormatView_logoSize,
            context.getResources().getInteger(R.integer.default_logoSize));
        a.recycle();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        Drawable clone = null;
        if (drawable != null) {
            clone = drawable.getConstantState().newDrawable();
            clone.setTint(mLogoTint);
        }
        super.setImageDrawable(clone);
    }

    /** Selects the right logo to display. **/
    public Uri prepareToDisplay(MediaItemMetadata metadata) {
        String key = (mLogoSize == LOGO_LARGE) ? KEY_CONTENT_FORMAT_TINTABLE_LARGE_ICON_URI :
                KEY_CONTENT_FORMAT_TINTABLE_SMALL_ICON_URI;
        String uri = metadata.getStringProperty(key);
        return TextUtils.isEmpty(uri) ? null : Uri.parse(uri);
    }
}
