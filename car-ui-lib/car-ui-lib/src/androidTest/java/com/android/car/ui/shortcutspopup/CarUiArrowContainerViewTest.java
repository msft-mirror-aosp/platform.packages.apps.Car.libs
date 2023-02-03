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

package com.android.car.ui.shortcutspopup;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static com.android.car.ui.testing.actions.ViewActions.waitForView;

import static junit.framework.TestCase.assertEquals;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.ui.test.R;
import com.android.car.ui.testing.matchers.ViewMatchers;

import junit.framework.AssertionFailedError;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Unit tests for {@link CarUiArrowContainerView}.
 */
@RunWith(AndroidJUnit4.class)
public class CarUiArrowContainerViewTest {

    private CarUiArrowContainerView mCarUiArrowContainerView;
    private Activity mActivity;

    @Rule
    public ActivityScenarioRule<CarUiArrowContainerViewTestActivity> mActivityRule =
            new ActivityScenarioRule<>(
                    CarUiArrowContainerViewTestActivity.class);

    @Before
    public void setUp() {
        mActivityRule.getScenario().onActivity(activity -> {
            mActivity = activity;
            mCarUiArrowContainerView = (activity.findViewById(R.id.textViewContainer));
        });
    }

    @Test
    public void setOrientationAny_shouldAlwaysBeVertical() {
        mCarUiArrowContainerView.setOrientation(LinearLayout.VERTICAL);
        onView(isRoot()).perform(waitForView(withId(R.id.textView)));
        onView(withId(R.id.textViewContainer)).check(matches(matchesVertical()));
        mCarUiArrowContainerView.setOrientation(LinearLayout.HORIZONTAL);
        onView(isRoot()).perform(waitForView(withId(R.id.textView)));
        onView(withId(R.id.textViewContainer)).check(matches(matchesVertical()));
    }

    @Test
    public void setHasArrowToFalse_shouldShowNoArrow() {
        mActivity.runOnUiThread(() -> {
            mCarUiArrowContainerView.setHasArrow(false);
        });
        onView(isRoot()).perform(waitForView(withId(R.id.textViewContainer)));
        onView(withId(R.id.textViewContainer)).check(matches(hasChildCount(1)));
    }

    @Test
    public void setHasArrowToTrue_shouldShowArrow() {
        mActivity.runOnUiThread(() -> {
            mCarUiArrowContainerView.setHasArrow(true);
        });
        onView(isRoot()).perform(waitForView(withId(R.id.textViewContainer)));
        onView(withId(R.id.textViewContainer)).check(matches(hasChildCount(2)));
    }

    @Test
    public void setArrowHeightAndGravityToTop() {
        int arrowHeight = 50;
        mActivity.runOnUiThread(() -> {
            mCarUiArrowContainerView.setArrowHeight(arrowHeight);
            mCarUiArrowContainerView.setArrowGravityTop(true);
        });
        onView(isRoot()).perform(waitForView(withId(R.id.textViewContainer)));
        onView(withId(R.id.textViewContainer)).check(
                matches(hasArrowHeight(arrowHeight, true)));
    }

    @Test
    public void setArrowHeightAndGravityToBottom() {
        int arrowHeight = 40;
        mActivity.runOnUiThread(() -> {
            mCarUiArrowContainerView.setArrowHeight(arrowHeight);
            mCarUiArrowContainerView.setArrowGravityTop(false);
        });
        onView(isRoot()).perform(waitForView(withId(R.id.textViewContainer)));
        onView(withId(R.id.textViewContainer)).check(
                matches(hasArrowHeight(arrowHeight, false)));
    }

    @Test
    public void setEnabled_shouldEnableForContentView() {
        mActivity.runOnUiThread((() -> {
            mCarUiArrowContainerView.setEnabled(true);
        }));
        onView(isRoot()).perform(waitForView(withId(R.id.textView)));
        onView(withId(R.id.textView)).check(matches(isEnabled()));
        mActivity.runOnUiThread(() -> {
            mCarUiArrowContainerView.setEnabled(false);
        });
        onView(isRoot()).perform(waitForView(withId(R.id.textView)));
        onView(withId(R.id.textView)).check(matches(not(isEnabled())));
    }

    @Test
    public void setDrawableContentId_shouldSetDrawableForContentView() {
        onView(isRoot()).perform(waitForView(withId(R.id.textView)));
        onView(withId(R.id.textView)).check(
                matches(ViewMatchers.withBackgroundDrawable(mActivity,
                        R.drawable.bg_arrow_container_default)));

        int contentViewDrawable = R.drawable.bg_arrow_container;
        mActivity.runOnUiThread((() -> {
            mCarUiArrowContainerView.setContentDrawableId(contentViewDrawable);
        }));
        onView(isRoot()).perform(waitForView(withId(R.id.textView)));
        onView(withId(R.id.textView)).check(
                matches(ViewMatchers.withBackgroundDrawable(mActivity, contentViewDrawable)));
    }

    @Test
    public void doNotSetContentViewId_shouldThrowException() {
        // Pass null to attrs to create CarUiArrowContainerView with no specified carUiContentView.
        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> mCarUiArrowContainerView =
                new CarUiArrowContainerView(mActivity.getApplicationContext(), null));
        assertEquals("Attribute app:carUiContentView must be specified when using "
                         + "CarUiArrowContainerView", ex.getMessage());
    }

    private Matcher<? super View> hasArrowHeight(int arrowHeight, boolean isArrowTop) {
        return new TypeSafeMatcher<View>() {
            private final String mExpectedString =
                    " CarUiArrowContainerView{arrowHeight="
                            + arrowHeight
                            + "}";

            @Override
            protected boolean matchesSafely(View item) {
                if (!(item instanceof CarUiArrowContainerView)) {
                    return false;
                }
                CarUiArrowContainerView containerView = (CarUiArrowContainerView) item;
                View arrowView;
                if (isArrowTop) {
                    arrowView = containerView.getChildAt(0);
                } else {
                    arrowView = containerView.getChildAt(containerView.getChildCount() - 1);
                }
                int actualArrowHeight = arrowView.getHeight();
                if (actualArrowHeight == arrowHeight) {
                    return true;
                }
                String actualString =
                        " Got: CarUiArrowContainerView{arrowHeight=" + actualArrowHeight + "}";
                String assertionError = "\nExpected: " + mExpectedString
                        + "\n"
                        + actualString;
                throw new AssertionFailedError(assertionError);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(mExpectedString);
            }
        };
    }

    private Matcher<? super View> matchesVertical() {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                if (!(item instanceof LinearLayout)) {
                    return false;
                }
                return ((LinearLayout) item).getOrientation() == LinearLayout.VERTICAL;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(
                        " Matches the orientation of the View of Type LinearLayout ");
            }
        };
    }
}
