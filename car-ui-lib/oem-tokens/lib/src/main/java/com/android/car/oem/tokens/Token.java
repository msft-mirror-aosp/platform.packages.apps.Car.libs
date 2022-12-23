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
package com.android.car.oem.tokens;

import static android.util.TypedValue.TYPE_ATTRIBUTE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.SharedLibraryInfo;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleableRes;

import java.util.List;

/**
 * Public interface for general CarUi static functions.
 */
public class Token {
    private static final String TOKEN_SHARED_LIBRARY_NAME = "com.android.oem.tokens";

    /**
     * Return the library name for the OEM design token shared library installed on device.
     */
    static String getTokenSharedLibraryName() {
        return TOKEN_SHARED_LIBRARY_NAME;
    }

    /**
     * Return the package name for the OEM design token shared library installed on device.
     */
    public static String getTokenSharedLibPackageName(@NonNull PackageManager packageManager) {
        List<SharedLibraryInfo> sharedLibs = packageManager.getSharedLibraries(0);
        for (SharedLibraryInfo info : sharedLibs) {
            if (info.getName().equals(TOKEN_SHARED_LIBRARY_NAME)) {
                return info.getDeclaringPackage().getPackageName();
            }
        }

        return null;
    }

    /**
     * Return {@code true} if there is an OEM design token shared library installed on device.
     */
    public static boolean isTokenSharedLibInstalled(@NonNull PackageManager packageManager) {
        String packageName = getTokenSharedLibPackageName(packageManager);
        if (packageName == null) {
            return false;
        }

        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Return a {@link ContextThemeWrapper} that includes OEM provided values for design tokens.
     * <p>
     * If OEM customized token values are unavailable on the system , the {@code Context} object is
     * returned with library default token values.
     */
    @NonNull
    public static Context createOemStyledContext(@NonNull Context context) {
        ContextThemeWrapper oemContext = new ContextThemeWrapper(context, R.style.OemTokens);

        int oemStyleOverride = context.getResources().getIdentifier("OemStyle",
                "style", Token.getTokenSharedLibraryName());
        if (oemStyleOverride == 0) {
            return context;
        }

        oemContext.getTheme().applyStyle(oemStyleOverride, true);

        return oemContext;
    }

    /**
     * Return the OEM provided color value corresponding to the color resource.
     * <p>
     * If OEM customized token color values are unavailable on the system , the library default
     * color token value is returned.
     */
    @ColorInt
    public static int getColor(@NonNull Context context, @ColorRes int colorId) {
        Context oemContext = createOemStyledContext(context);
        return oemContext.getResources().getColor(colorId, oemContext.getTheme());
    }

    /**
     * Return {@code true} if there is an available OEM provided value for the design token that
     * corresponds to the styleable resource.
     */
    public static boolean isOemStyled(Context context, @StyleableRes int styleableId) {
        int oemStyleOverride = context.getResources().getIdentifier("OemStyle",
                "style", Token.getTokenSharedLibraryName());
        if (oemStyleOverride == 0) {
            return false;
        }

        TypedArray libAttributes = context.obtainStyledAttributes(R.style.OemTokens,
                R.styleable.OemTokens);
        TypedValue tv = new TypedValue();
        if (libAttributes.getType(styleableId) != TYPE_ATTRIBUTE) {
            return false;
        }
        libAttributes.getValue(styleableId, tv);

        int[] attrs = {tv.data};

        TypedArray sharedLibAttributes = context.obtainStyledAttributes(oemStyleOverride, attrs);
        int type = sharedLibAttributes.getType(0);
        boolean isOemStyled = type != 0;

        libAttributes.recycle();
        sharedLibAttributes.recycle();
        return isOemStyled;
    }
}
