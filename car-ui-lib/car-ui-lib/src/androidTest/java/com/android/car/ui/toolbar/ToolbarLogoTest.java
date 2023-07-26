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

package com.android.car.ui.toolbar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;

import static com.android.car.ui.testing.matchers.ViewMatchers.withDrawable;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.android.car.ui.TestActivity;
import com.android.car.ui.core.CarUi;
import com.android.car.ui.pluginsupport.PluginFactorySingleton;
import com.android.car.ui.test.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Unit tests for {@link ToolbarController} logo behavior with nav icon.
 */
@SuppressWarnings("AndroidJdkLibsChecker")
@RunWith(Parameterized.class)
public class ToolbarLogoTest {

    @Parameterized.Parameters
    public static Object[] data() {
        // It's important to do no plugin first, so that the plugin will
        // still be enabled when this test finishes
        return new Object[]{false, true};
    }

    private final boolean mPluginEnabled;
    private final String mNavIconContentDescription;

    public ToolbarLogoTest(boolean pluginEnabled) {
        mPluginEnabled = pluginEnabled;
        PluginFactorySingleton.setPluginEnabledForTesting(mPluginEnabled);
        mNavIconContentDescription = InstrumentationRegistry.getInstrumentation().getContext()
                .getResources().getString(R.string.car_ui_toolbar_nav_icon_content_description);
    }

    @Rule
    public final ActivityScenarioRule<TestActivity> mScenarioRule =
            new ActivityScenarioRule<>(TestActivity.class);

    @Test
    public void test_logoListener_withNavIcon_fillNavIconWithLogo_reserveNavIconSpace() {
        test_logoListener(true, true, true);
    }

    @Test
    public void test_logoListener_withNavIcon_fillNavIconWithLogo_dontReserveNavIconSpace() {
        test_logoListener(true, true, false);
    }

    @Test
    public void test_logoListener_withNavIcon_dontFillNavIconWithLogo_reserveNavIconSpace() {
        test_logoListener(true, false, true);
    }

    @Test
    public void test_logoListener_withNavIcon_dontFillNavIconWithLogo_dontReserveNavIconSpace() {
        test_logoListener(true, false, false);
    }

    @Test
    public void test_logoListener_noNavIcon_fillNavIconWithLogo_reserveNavIconSpace() {
        test_logoListener(false, true, true);
    }

    @Test
    public void test_logoListener_noNavIcon_fillNavIconWithLogo_dontReserveNavIconSpace() {
        test_logoListener(false, true, false);
    }

    @Test
    public void test_logoListener_noNavIcon_dontFillNavIconWithLogo_reserveNavIconSpace() {
        test_logoListener(false, false, true);
    }

    @Test
    public void test_logoListener_noNavIcon_dontFillNavIconWithLogo_dontReserveNavIconSpace() {
        test_logoListener(false, false, false);
    }

    private void test_logoListener(
            boolean withNavIcon, boolean logoFillsNavIcon, boolean reserveNavIconSpace) {
        Runnable logoListener = mock(Runnable.class);
        // install the toolbar with given configuration and logo listener
        mScenarioRule.getScenario().onActivity(activity -> {
            Context testableContext = spy(activity);
            Resources testableResources = spy(activity.getResources());
            when(testableContext.getResources()).thenReturn(testableResources);
            doReturn(logoFillsNavIcon).when(testableResources).getBoolean(
                    R.bool.car_ui_toolbar_logo_fills_nav_icon_space);
            doReturn(reserveNavIconSpace).when(testableResources).getBoolean(
                    R.bool.car_ui_toolbar_nav_icon_reserve_space);

            // Instantiate toolbar with testableContext to mock resources
            ToolbarController toolbar = CarUi.installBaseLayoutAround(
                    testableContext,
                    activity.requireViewById(android.R.id.content),
                    null,
                    true
            );

            // Configure toolbar
            toolbar.setNavButtonMode(withNavIcon ? NavButtonMode.BACK : NavButtonMode.DISABLED);
            toolbar.setLogo(R.drawable.ic_launcher);
            toolbar.setOnLogoClickListener(logoListener);
        });

        // Clicking the logo should invoke its listener. It should not invoke when clicking the
        // nav icon
        onView(withDrawable(InstrumentationRegistry.getInstrumentation().getContext()
                .getDrawable(R.drawable.ic_launcher))).perform(click());
        verify(logoListener, times(1)).run();
        if (withNavIcon) {
            onView(withContentDescription(mNavIconContentDescription)).perform(click());
            verify(logoListener, times(1)).run();
        }
    }
}
