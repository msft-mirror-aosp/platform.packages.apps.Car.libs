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

import static junit.framework.Assert.assertEquals;

import static org.junit.Assert.assertThrows;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.oem.tokens.test.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public final class TokenTest {
    @Before
    public void setUp() {
        Token.setTestingOverride(true);
    }

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
    public void testLightDarkThemeTokenInstallerRun() {
        AtomicInteger lightColor = new AtomicInteger();
        AtomicInteger darkColor = new AtomicInteger();

        try (ActivityScenario<TokenTestActivity> scenario = ActivityScenario.launch(
                TokenTestActivity.class)) {
            scenario.onActivity(activity -> {
                TypedArray a = activity.getTheme().obtainStyledAttributes(
                        new int[]{R.attr.oemColorSurface, android.R.attr.isLightTheme});
                lightColor.set(a.getColor(0, -1));
                assertThat(a.getBoolean(1, false)).isEqualTo(true);
                a.recycle();
            });
        }

        try (ActivityScenario<TokenDarkThemeTestActivity> scenario =
                     ActivityScenario.launch(TokenDarkThemeTestActivity.class)) {
            scenario.onActivity(activity -> {
                TypedArray b = activity.getTheme().obtainStyledAttributes(
                        new int[]{R.attr.oemColorSurface, android.R.attr.isLightTheme});
                darkColor.set(b.getColor(0, -1));
                assertThat(b.getBoolean(1, true)).isEqualTo(false);
                b.recycle();
            });
        }

        assertThat(lightColor.get()).isNotEqualTo(-1);
        assertThat(darkColor.get()).isNotEqualTo(-1);
        assertThat(lightColor.get()).isNotEqualTo(darkColor.get());
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
                int colorStaticApi = Token.getColor(activity, R.attr.oemColorPrimary);

                TypedValue tv = new TypedValue();
                TypedArray attributes = activity.getTheme().obtainStyledAttributes(
                        R.style.OemStyle, new int[]{R.attr.testColorPrimary});
                attributes.getValue(0, tv);

                assertThat(colorStaticApi).isEqualTo(tv.data);
                attributes.recycle();
            });
        }
    }

    @Test
    public void testColor_tokenInstallerRunNoOverride() {
        try (ActivityScenario<TokenTestNoOverrideActivity> scenario = ActivityScenario.launch(
                TokenTestNoOverrideActivity.class)) {
            scenario.onActivity(activity -> {
                TypedValue tv1 = new TypedValue();
                TypedValue tv2 = new TypedValue();
                activity.getTheme().resolveAttribute(R.attr.oemColorPrimary, tv1, true);
                TypedArray attributes = activity.obtainStyledAttributes(
                        R.style.TestTokenTheme_NoOverride, R.styleable.OemTokens);
                attributes.getValue(R.styleable.OemTokens_oemColorPrimary, tv2);

                assertThat(tv1.data).isEqualTo(tv2.data);
            });
        }
    }

    @Test
    public void testColor_tokenInstallerNotRun_throwsException() {
        try (ActivityScenario<NoTokenTestActivity> scenario = ActivityScenario.launch(
                NoTokenTestActivity.class)) {
            scenario.onActivity(activity -> {
                Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                        Token.getColor(activity, R.attr.oemColorPrimary));
                assertEquals("Context must be token compatible.", exception.getMessage());
            });
        }
    }

    @Test
    public void testTextAppearance_tokenInstallerRun() {
        try (ActivityScenario<TokenTestActivity> scenario = ActivityScenario.launch(
                TokenTestActivity.class)) {
            scenario.onActivity(activity -> {
                Context oemContext = Token.createOemStyledContext(activity);
                int textStaticApi = Token.getTextAppearance(oemContext,
                        R.attr.oemTextAppearanceDisplayLarge);

                TypedValue tv = new TypedValue();
                TypedArray attributes = activity.getTheme().obtainStyledAttributes(
                        R.style.OemStyle, new int[]{R.attr.testTextAppearanceDisplayLarge});
                attributes.getValue(0, tv);

                assertThat(textStaticApi).isEqualTo(tv.data);
                assertThat(textStaticApi).isEqualTo(R.style.FakeTextAppearance);
                attributes.recycle();
            });
        }
    }

    @Test
    public void testTextAppearance_tokenInstallerRunNoOverride() {
        try (ActivityScenario<TokenTestNoOverrideActivity> scenario = ActivityScenario.launch(
                TokenTestNoOverrideActivity.class)) {
            scenario.onActivity(activity -> {
                TypedValue tv1 = new TypedValue();
                TypedValue tv2 = new TypedValue();
                activity.getTheme().resolveAttribute(R.attr.oemTextAppearanceDisplayLarge, tv1,
                        true);
                TypedArray attributes = activity.obtainStyledAttributes(
                        R.style.TestTokenTheme_NoOverride, R.styleable.OemTokens);
                attributes.getValue(R.styleable.OemTokens_oemTextAppearanceDisplayLarge, tv2);

                assertThat(tv1.data).isEqualTo(tv2.data);
                assertThat(tv1.data).isEqualTo(0);
                assertThat(tv2.data).isEqualTo(0);
            });
        }
    }

    @Test
    public void testCorner_tokenInstallerRun() {
        try (ActivityScenario<TokenTestActivity> scenario = ActivityScenario.launch(
                TokenTestActivity.class)) {
            scenario.onActivity(activity -> {
                Context oemContext = Token.createOemStyledContext(activity);
                float cornerStaticApi = Token.getCornerRadius(oemContext,
                        R.attr.oemShapeCornerLarge);

                TypedValue tv = new TypedValue();
                TypedArray attributes = activity.getTheme().obtainStyledAttributes(
                        R.style.OemStyle, new int[]{R.attr.testShapeCornerLarge});
                attributes.getValue(0, tv);

                assertThat(cornerStaticApi).isEqualTo(
                        activity.getResources().getDimension(R.dimen.fake_corner));
                attributes.recycle();
            });
        }
    }

    @Test
    public void testIsOemStyled() {
        try (ActivityScenario<TokenTestActivity> scenario = ActivityScenario.launch(
                TokenTestActivity.class)) {
            scenario.onActivity(activity -> {

                boolean isColorPrimaryOemStyled = Token.isOemStyled(activity,
                        R.attr.oemColorPrimary);
                assertThat(isColorPrimaryOemStyled).isTrue();
            });
        }
    }

    @Test
    public void testIsNotOemStyled() {
        try (ActivityScenario<TokenTestActivity> scenario = ActivityScenario.launch(
                TokenTestActivity.class)) {
            scenario.onActivity(activity -> {

                boolean isColorPrimaryOemStyled = Token.isOemStyled(activity,
                        R.attr.oemColorOnPrimary);
                assertThat(isColorPrimaryOemStyled).isFalse();
            });
        }
    }
}
