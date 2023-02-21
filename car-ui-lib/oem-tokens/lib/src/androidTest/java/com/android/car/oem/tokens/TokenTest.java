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
package com.android.car.oem.tokens;

import static com.google.common.truth.Truth.assertThat;

import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.oem.tokens.test.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class TokenTest {

    @Test
    public void testTokenInstallerNotRun() {
        try (ActivityScenario<NoTokenTestActivity> scenario = ActivityScenario.launch(
                NoTokenTestActivity.class)) {
            scenario.onActivity(activity -> {
                TypedArray a = activity.getTheme().obtainStyledAttributes(
                        new int[]{R.attr.oemTokenOverrideEnabled, R.attr.oemColorPrimary});

                assertThat(a.getType(0)).isEqualTo(TypedValue.TYPE_NULL);
                assertThat(a.getType(1)).isEqualTo(TypedValue.TYPE_NULL);

                a.recycle();
            });
        }
    }

    @Test
    public void testTokenInstallerRun() {
        try (ActivityScenario<TokenTestActivity> scenario = ActivityScenario.launch(
                TokenTestActivity.class)) {
            scenario.onActivity(activity -> {
                TypedArray a = activity.getTheme().obtainStyledAttributes(
                        new int[]{R.attr.oemTokenOverrideEnabled, R.attr.oemColorPrimary});

                assertThat(a.getBoolean(0, false)).isTrue();
                assertThat(a.getType(1)).isNotEqualTo(TypedValue.TYPE_NULL);

                a.recycle();
            });
        }
    }

    @Test
    public void testTokenInstallerRunNoOverride() {
        try (ActivityScenario<TokenTestNoOverrideActivity> scenario = ActivityScenario.launch(
                TokenTestNoOverrideActivity.class)) {
            scenario.onActivity(activity -> {
                TypedArray a = activity.getTheme().obtainStyledAttributes(
                        new int[]{R.attr.oemTokenOverrideEnabled, R.attr.oemColorPrimary});

                assertThat(a.getBoolean(0, true)).isFalse();
                assertThat(a.getType(1)).isNotEqualTo(TypedValue.TYPE_NULL);

                a.recycle();
            });
        }
    }

    @Test
    public void testColor_tokenInstallerRun() {
        try (ActivityScenario<TokenTestActivity> scenario = ActivityScenario.launch(
                TokenTestActivity.class)) {
            scenario.onActivity(activity -> {
                int colorStaticApi = Token.getColor(activity,
                        R.styleable.OemTokens_oemColorPrimary);
                TypedValue tv = new TypedValue();
                activity.getTheme().resolveAttribute(R.attr.oemColorPrimary, tv, true);

                assertThat(colorStaticApi).isEqualTo(tv.data);
            });
        }
    }

    @Test
    public void testColor_tokenInstallerRunNoOverride() {
        try (ActivityScenario<TokenTestNoOverrideActivity> scenario = ActivityScenario.launch(
                TokenTestNoOverrideActivity.class)) {
            scenario.onActivity(activity -> {
                int colorStaticApi = Token.getColor(activity,
                        R.styleable.OemTokens_oemColorPrimary);
                TypedValue tv = new TypedValue();
                activity.getTheme().resolveAttribute(R.attr.oemColorPrimary, tv, true);

                assertThat(colorStaticApi).isEqualTo(tv.data);
            });
        }
    }

    @Test
    public void testTextAppearance_tokenInstallerRun() {
        try (ActivityScenario<TokenTestActivity> scenario = ActivityScenario.launch(
                TokenTestActivity.class)) {
            scenario.onActivity(activity -> {
                int textStaticApi = Token.getTextAppearance(activity,
                        R.styleable.OemTokens_oemTextAppearanceDisplayLarge);
                TypedValue tv = new TypedValue();
                activity.getTheme().resolveAttribute(R.attr.oemTextAppearanceDisplayLarge, tv,
                        true);

                assertThat(textStaticApi).isEqualTo(tv.data);
            });
        }
    }

    @Test
    public void testTextAppearance_tokenInstallerRunNoOverride() {
        try (ActivityScenario<TokenTestNoOverrideActivity> scenario = ActivityScenario.launch(
                TokenTestNoOverrideActivity.class)) {
            scenario.onActivity(activity -> {
                int textStaticApi = Token.getTextAppearance(activity,
                        R.styleable.OemTokens_oemTextAppearanceDisplayLarge);
                TypedValue tv = new TypedValue();
                activity.getTheme().resolveAttribute(R.attr.oemTextAppearanceDisplayLarge, tv,
                        true);

                assertThat(textStaticApi).isEqualTo(tv.data);
            });
        }
    }

    @Test
    public void testCorner_tokenInstallerRun() {
        try (ActivityScenario<TokenTestActivity> scenario = ActivityScenario.launch(
                TokenTestActivity.class)) {
            scenario.onActivity(activity -> {
                int cornerStaticApi = Token.getCornerRadius(activity,
                        R.styleable.OemTokens_oemShapeCornerLarge);
                TypedValue tv = new TypedValue();
                activity.getTheme().resolveAttribute(R.attr.oemShapeCornerLarge, tv, true);

                assertThat(cornerStaticApi).isEqualTo(tv.data);
            });
        }
    }

    @Test
    public void testCorner_tokenInstallerRunNoOverride() {
        try (ActivityScenario<TokenTestNoOverrideActivity> scenario = ActivityScenario.launch(
                TokenTestNoOverrideActivity.class)) {
            scenario.onActivity(activity -> {
                int cornerStaticApi = Token.getCornerRadius(activity,
                        R.styleable.OemTokens_oemShapeCornerLarge);
                TypedValue tv = new TypedValue();
                activity.getTheme().resolveAttribute(R.attr.oemShapeCornerLarge, tv, true);

                assertThat(cornerStaticApi).isEqualTo(tv.data);
            });
        }
    }
}
