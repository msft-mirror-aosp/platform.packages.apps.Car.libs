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
package com.android.car.ui.pluginsupport;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.pm.PackageInfo;

import androidx.test.core.content.pm.PackageInfoBuilder;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PluginSpecifierTest {

    @Test
    public void test_empty_pluginspecifier_matches_anything() {
        PluginSpecifier pluginSpecifier = PluginSpecifier.builder()
                .build();

        PackageInfo packageInfo = PackageInfoBuilder.newBuilder()
                .setPackageName("com.android.car.testplugin").build();
        packageInfo.setLongVersionCode(100);

        assertTrue(pluginSpecifier.matches(packageInfo));
    }

    @Test
    public void test_pluginspecifier_doesnt_match_different_package_name() {
        PluginSpecifier pluginSpecifier = PluginSpecifier.builder()
                .setPackageName("com.android.car.testplugin")
                .build();

        PackageInfo packageInfo = PackageInfoBuilder.newBuilder()
                .setPackageName("com.android.car.testplugin2").build();

        assertFalse(pluginSpecifier.matches(packageInfo));
    }

    @Test
    public void test_pluginspecifier_matches_same_package_name() {
        PluginSpecifier pluginSpecifier = PluginSpecifier.builder()
                .setPackageName("com.android.car.testplugin")
                .build();

        PackageInfo packageInfo = PackageInfoBuilder.newBuilder()
                .setPackageName("com.android.car.testplugin").build();

        assertTrue(pluginSpecifier.matches(packageInfo));
    }

    @Test
    public void test_pluginspecifier_doesnt_match_versioncode() {
        PluginSpecifier pluginSpecifier = PluginSpecifier.builder()
                .setPackageName("com.android.car.testplugin")
                .setMaxVersion(5)
                .build();

        PackageInfo packageInfo = PackageInfoBuilder.newBuilder()
                .setPackageName("com.android.car.testplugin").build();
        packageInfo.setLongVersionCode(6);

        assertFalse(pluginSpecifier.matches(packageInfo));
    }

    @Test
    public void test_pluginspecifier_matches_versioncode() {
        PluginSpecifier pluginSpecifier = PluginSpecifier.builder()
                .setPackageName("com.android.car.testplugin")
                .setMaxVersion(5)
                .build();

        PackageInfo packageInfo = PackageInfoBuilder.newBuilder()
                .setPackageName("com.android.car.testplugin").build();
        packageInfo.setLongVersionCode(4);

        assertTrue(pluginSpecifier.matches(packageInfo));
    }
}
