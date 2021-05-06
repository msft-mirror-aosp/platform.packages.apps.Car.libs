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

package com.android.car.ui.widget;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;
import android.view.ViewGroup;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.android.car.ui.CarUiText;
import com.android.car.ui.TestActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link CarUiTextViewTest}.
 */
public class CarUiTextViewTest {
    private static final CharSequence LONG_CHAR_SEQUENCE =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor "
                    + "incididunt ut labore et dolore magna aliqua. Netus et malesuada fames ac "
                    + "turpis egestas maecenas pharetra convallis. At urna condimentum mattis "
                    + "pellentesque id nibh tortor. Purus in mollis nunc sed id semper risus in. "
                    + "Turpis massa tincidunt dui ut ornare lectus sit amet. Porttitor lacus "
                    + "luctus accumsan tortor posuere ac. Augue eget arcu dictum varius. Massa "
                    + "tempor nec feugiat nisl pretium fusce id velit ut. Fames ac turpis egestas"
                    + " sed tempus urna et pharetra pharetra. Tellus orci ac auctor augue mauris "
                    + "augue neque gravida. Purus viverra accumsan in nisl nisi scelerisque eu. "
                    + "Ut lectus arcu bibendum at varius vel pharetra. Penatibus et magnis dis "
                    + "parturient montes nascetur ridiculus mus. Suspendisse sed nisi lacus sed "
                    + "viverra tellus in hac habitasse.";

    @Rule
    public ActivityScenarioRule<TestActivity> mActivityRule =
            new ActivityScenarioRule<>(TestActivity.class);

    private TestActivity mActivity;

    @Before
    public void setUp() {
        mActivityRule.getScenario().onActivity(activity -> {
            mActivity = activity;
        });
    }

    @Test
    public void testTruncateToVariant_startAsViewGone() {
        CarUiTextView textView = CarUiTextView.create(mActivity);
        List<CharSequence> list = new ArrayList<>();
        list.add(LONG_CHAR_SEQUENCE);
        String variant = "Second string";
        list.add(variant);
        textView.setText(new CarUiText.Builder(list).setMaxLines(1).build());
        ViewGroup container = mActivity.findViewById(
                com.android.car.ui.test.R.id.test_container);
        container.post(() -> container.setVisibility(View.GONE));
        container.post(() -> container.addView(textView));
        container.post(() -> container.setVisibility(View.VISIBLE));

        onView(withText(variant)).check(matches(isDisplayed()));
    }
}
