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

import android.content.pm.PackageManager;
import android.content.pm.SharedLibraryInfo;

import androidx.annotation.NonNull;

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
}
