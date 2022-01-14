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

package com.android.car.ui.appstyledview;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.ui.TestActivity;
import com.android.car.ui.appstyledview.AppStyledViewController.AppStyledViewNavIcon;
import com.android.car.ui.test.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link AppStyledDialogController}.
 */
@RunWith(AndroidJUnit4.class)
public class AppStyledDialogControllerTest {
    private AppStyledDialogController mAppStyledDialogController;

    @Rule
    public ActivityScenarioRule<TestActivity> mActivityRule =
            new ActivityScenarioRule<>(TestActivity.class);

    private TestActivity mActivity;

    @Before
    public void setUp() throws Throwable {
        mActivityRule.getScenario().onActivity(activity -> {
            mAppStyledDialogController = new AppStyledDialogController(activity);
            mAppStyledDialogController.setAppStyledViewController(
                    new AppStyledViewControllerImpl(activity), activity);
            mActivity = activity;
        });
    }

    @Test
    public void show_shouldDisplayDialog() {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View appStyledTestView = inflater.inflate(R.layout.app_styled_view_sample, null,
                false);

        mActivity.runOnUiThread(() -> {
            mAppStyledDialogController.setContentView(appStyledTestView);
            mAppStyledDialogController.show();
        });

        String text = "app styled view";

        onView(withText(text))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void setNavIcon_showCloseIcon() {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View appStyledTestView = inflater.inflate(R.layout.app_styled_view_sample, null,
                false);

        mActivity.runOnUiThread(() -> {
            mAppStyledDialogController.setContentView(appStyledTestView);
            mAppStyledDialogController.setNavIcon(AppStyledViewNavIcon.CLOSE);
            mAppStyledDialogController.show();
        });

        Dialog dialog = mAppStyledDialogController.getAppStyledDialog();

        onView(withId(R.id.car_ui_app_styled_view_icon_close))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void setOnCloseClickListener_shouldInvokeCallback() {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View appStyledTestView = inflater.inflate(R.layout.app_styled_view_sample, null,
                false);

        Runnable callback = mock(Runnable.class);

        mActivity.runOnUiThread(() -> {
            mAppStyledDialogController.setContentView(appStyledTestView);
            mAppStyledDialogController.setNavIcon(AppStyledViewNavIcon.BACK);
            mAppStyledDialogController.setOnNavIconClickListener(callback);
            mAppStyledDialogController.show();
        });

        onView(withId(R.id.car_ui_app_styled_view_icon_close))
                .inRoot(isDialog())
                .perform(click());

        verify(callback).run();
    }

    @Test
    public void setOnDismissListener_shouldInvokeCallback() {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View appStyledTestView = inflater.inflate(R.layout.app_styled_view_sample, null,
                false);

        Runnable callback = mock(Runnable.class);

        mActivity.runOnUiThread(() -> {
            mAppStyledDialogController.setContentView(appStyledTestView);
            mAppStyledDialogController.setNavIcon(AppStyledViewNavIcon.BACK);
            mAppStyledDialogController.setOnDismissListener(callback);
            mAppStyledDialogController.show();
        });

        onView(withId(R.id.car_ui_app_styled_view_icon_close))
                .inRoot(isDialog())
                .perform(click());
    }

    @Test
    public void getContentView_equalsSetView() {
        LayoutInflater inflater = LayoutInflater.from(mActivityRule.getActivity());

        View appStyledTestView = inflater.inflate(R.layout.app_styled_view_sample, null,
                false);

        mActivityRule.runOnUiThread(() -> {
            mAppStyledDialogController.setContentView(appStyledTestView);
            mAppStyledDialogController.show();
        });

        assertEquals(appStyledTestView, mAppStyledDialogController.getContentView());
    }

    @Test
    public void getContentView_nullWhenNotSet() {
        assertNull(mAppStyledDialogController.getContentView());
    }
}
