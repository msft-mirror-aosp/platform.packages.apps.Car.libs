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
package com.android.car.oem.tokens;

import static android.util.TypedValue.TYPE_ATTRIBUTE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.SharedLibraryInfo;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * Public interface for general CarUi static functions.
 */
public class Token {
    private static final String TOKEN_SHARED_LIBRARY_NAME = "com.android.oem.tokens";
    private static final String TEST_TOKEN_SHARED_LIBRARY_NAME = "com.android.car.oem.tokens.test";

    private static boolean sTestingOverrideEnabled = false;

    /**
     * Return the library name for the OEM design token shared library installed on device.
     */
    static String getTokenSharedLibraryName() {
        if (sTestingOverrideEnabled) {
            return TEST_TOKEN_SHARED_LIBRARY_NAME;
        }

        return TOKEN_SHARED_LIBRARY_NAME;
    }

    @VisibleForTesting
    static void setTestingOverride(boolean enabled) {
        sTestingOverrideEnabled = enabled;
    }

    /**
     * Return the package name for the OEM design token shared library installed on device.
     */
    @Nullable
    public static String getTokenSharedLibPackageName(@NonNull PackageManager packageManager) {
        if (sTestingOverrideEnabled) {
            return TEST_TOKEN_SHARED_LIBRARY_NAME;
        }

        List<SharedLibraryInfo> sharedLibs = packageManager.getSharedLibraries(0);
        for (SharedLibraryInfo info : sharedLibs) {
            if (info.getName().equals(getTokenSharedLibraryName())) {
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
        if (context instanceof OemContextWrapper) {
            return context;
        }

        int oemStyleOverride = context.getResources().getIdentifier("OemStyle",
                "style", Token.getTokenSharedLibraryName());
        if (oemStyleOverride == 0) {
            return new OemContextWrapper(context, R.style.OemTokensBase);
        }

        OemContextWrapper oemContext = new OemContextWrapper(context, R.style.OemTokens);
        oemContext.getTheme().applyStyle(oemStyleOverride, true);

        return oemContext;
    }

    /**
     * Return the OEM provided corner radius corresponding to the attribute.
     * <p>
     * If OEM customized token corner radius values are unavailable on the system , the library
     * default corner radius token value is returned.
     */
    @Px
    public static float getCornerRadius(@NonNull Context context, @AttrRes int attr) {
        checkContext(context);
        TypedValue tv = getThemeTypedValue(context, attr);
        return TypedValue.complexToDimension(tv.data,
                context.getResources().getDisplayMetrics());
    }

    /**
     * Return the OEM provided text appearance resource id corresponding to the attribute.
     * <p>
     * If OEM customized token text appearance values are unavailable on the system , the library
     * default text appearance token value is returned.
     */
    @StyleRes
    public static int getTextAppearance(@NonNull Context context, @AttrRes int attr) {
        checkContext(context);
        TypedValue tv = getThemeTypedValue(context, attr);
        return tv.resourceId;
    }

    /**
     * Return the OEM provided color value corresponding to the attribute.
     * <p>
     * If OEM customized token color values are unavailable on the system, the library default color
     * token value is returned.
     */
    @ColorInt
    public static int getColor(@NonNull Context context, @AttrRes int attr) {
        checkContext(context);
        TypedValue tv = getThemeTypedValue(context, attr);

        if (tv.resourceId == 0) {
            return tv.data;
        }

        return ContextCompat.getColor(context, tv.resourceId);
    }

    /**
     * Return {@code true} if there is an available OEM provided value for the design token that
     * corresponds to the attribute.
     */
    public static boolean isOemStyled(Context context, @AttrRes int attr) {
        context = context.getApplicationContext();
        int oemStyleOverride = context.getResources().getIdentifier("OemStyle",
                "style", Token.getTokenSharedLibraryName());
        if (oemStyleOverride == 0) {
            return false;
        }

        TypedArray libAttributes = context.getTheme().obtainStyledAttributes(R.style.OemTokens,
                new int[]{attr});
        TypedValue tv = new TypedValue();
        if (libAttributes.getType(0) != TYPE_ATTRIBUTE) {
            libAttributes.recycle();
            return false;
        }
        libAttributes.getValue(0, tv);

        int[] attrs = new int[]{tv.data};

        TypedArray sharedLibAttributes = context.obtainStyledAttributes(oemStyleOverride, attrs);
        int type = sharedLibAttributes.getType(0);
        boolean isOemStyled = type != 0;

        libAttributes.recycle();
        sharedLibAttributes.recycle();
        return isOemStyled;
    }

    private static void checkContext(@NonNull Context context) {
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                new int[]{ R.attr.oemColorPrimary});
        if (attributes.getType(0) == (TypedValue.TYPE_NULL)) {
            throw new IllegalArgumentException(
                    "Context must be token compatible.");
        }
        attributes.recycle();
    }

    @NonNull
    private static TypedValue getThemeTypedValue(@NonNull Context oemContext, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        oemContext.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue;
    }
}
