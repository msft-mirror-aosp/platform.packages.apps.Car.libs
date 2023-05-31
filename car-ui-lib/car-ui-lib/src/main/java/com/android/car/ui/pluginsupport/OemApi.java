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
package com.android.car.ui.pluginsupport;

import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;

class OemApi implements Comparable<OemApi> {

    private static final String TAG = "carui";

    public final Class<?> oemFactoryClass;
    public final Class<?> adapterClass;
    public final int version;

    OemApi(String oemFactoryClassName, Class<?> adapterClass, int version) {
        this.oemFactoryClass = loadClass(oemFactoryClassName);
        this.adapterClass = adapterClass;
        this.version = version;
    }

    public PluginFactory getAdapter(Object oemFactory) {
        PluginFactory adapter = null;
        try {
            Constructor<?> constructor = adapterClass.getDeclaredConstructor(
                    oemFactoryClass);
            constructor.setAccessible(true);
            adapter = (PluginFactory) constructor.newInstance(oemFactory);
        } catch (ReflectiveOperationException e) {
            Log.e(TAG, adapterClass + " must have a constructor that accepts "
                    + oemFactoryClass);
        }
        return adapter;
    }

    @Override
    public int compareTo(OemApi o) {
        return o.version - this.version;
    }

    private static Class<?> loadClass(@Nullable String className) {
        if (className == null) {
            return null;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
