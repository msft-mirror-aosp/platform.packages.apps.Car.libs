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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.android.car.ui.testing.actions.ViewActions.waitForView;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.ui.test.R;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link CarUiShortcutsPopup}
 */
@RunWith(AndroidJUnit4.class)
public class CarUiShortcutsPopupTest {

    private Activity mActivity;
    private View mAnchorView;
    private View mAnchorViewLeftTop;
    private View mAnchorViewLeftBottom;
    private static final String ITEM1_NAME = "item1_name";
    private static final String ITEM2_NAME = "item2_name";
    private Toast mToast1Mock;
    private Toast mToast2Mock;
    private CarUiShortcutsPopup mCarUiShortcutsPopup;

    @Rule
    public ActivityScenarioRule<CarUiShortcutsPopupTestActivity> mActivityRule =
            new ActivityScenarioRule<>(
                    CarUiShortcutsPopupTestActivity.class);

    @Before
    public void setUp() {
        mActivityRule.getScenario().onActivity(activity -> {
            mActivity = activity;
            mToast1Mock = mock(Toast.class);
            mToast2Mock = mock(Toast.class);
            mAnchorView = mActivity.findViewById(R.id.anchorViewCenter);
            mAnchorViewLeftTop = mActivity.findViewById(R.id.anchorViewLeftTop);
            mAnchorViewLeftBottom = mActivity.findViewById(R.id.anchorViewLeftBottom);
        });
    }

    @After
    public void tearDown() {
        try {
            if (mCarUiShortcutsPopup != null && mCarUiShortcutsPopup.isShowing()) {
                mActivity.runOnUiThread(mCarUiShortcutsPopup::dismiss);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // TODO b/269302130 Remove Ignore annotation when fixed
    @Ignore
    @Test
    public void builderItemDataAndOnClickTest() {
        mCarUiShortcutsPopup = buildFromBuilder(mToast1Mock, mToast2Mock,
                mAnchorView);
        mActivity.runOnUiThread(mCarUiShortcutsPopup::show);
        //wait for the popup tobe draw
        onView(isRoot()).perform(waitForView(getPopupMatcher()));
        onView(withChild(withText(ITEM2_NAME))).check(matches(isDisplayed()));
        onView(withChild(withText(ITEM1_NAME))).check(matches(isDisplayed()))
                .check(isCompletelyAbove(withChild(withText(ITEM2_NAME))));
        //click first item, should show the toast.
        onView(withChild(withText(ITEM1_NAME))).perform(click());
        verify(mToast1Mock).show();
        onView(getPopupMatcher()).check(doesNotExist());
        mActivity.runOnUiThread(mCarUiShortcutsPopup::show);
        onView(isRoot()).perform(waitForView(getPopupMatcher()));
        //second Item is set to enabled false, so should not show toast.
        onView(withChild(withText(ITEM2_NAME))).perform(click());
        verify(mToast2Mock, never()).show();
    }

    // TODO b/269302130 Remove Ignore annotation when fixed
    @Ignore
    @Test
    public void onClickOutsidePopup_shouldDismissPopup() {
        Toast toast1Mock = mock(Toast.class);
        Toast toast2Mock = mock(Toast.class);
        mCarUiShortcutsPopup = buildFromConstructor(toast1Mock, toast2Mock,
                mAnchorView);
        mActivity.runOnUiThread(mCarUiShortcutsPopup::show);
        //wait for the popup tobe draw
        onView(isRoot()).perform(waitForView(getPopupMatcher())).check(
                matches(isDisplayed()));
        onView(isRoot()).perform(click());
        onView(getPopupMatcher()).check(doesNotExist());
    }

    @Test
    public void popupShowsTopOfAnchor_shortcutsItemsAreReversed() {
        mCarUiShortcutsPopup = buildFromConstructor(mock(Toast.class),
                mock(Toast.class),
                mAnchorViewLeftBottom);
        mActivity.runOnUiThread(mCarUiShortcutsPopup::show);
        //wait for the popup tobe draw
        onView(getPopupMatcher()).perform(waitForView(getPopupMatcher())).check(
                matches(isDisplayed()));
        //verify that the order of the Items are in order.
        onView(withChild(withText(ITEM2_NAME))).check(
                isCompletelyAbove(withChild(withText(ITEM1_NAME))));
    }

    @Test
    public void popupShowsBottomOfAnchor_shortcutsItemsAreShownInOrder() {
        mCarUiShortcutsPopup = buildFromConstructor(mock(Toast.class),
                mock(Toast.class),
                mAnchorViewLeftTop);
        mActivity.runOnUiThread(mCarUiShortcutsPopup::show);
        //wait for the popup tobe draw
        onView(getPopupMatcher()).perform(waitForView(getPopupMatcher())).check(
                matches(isDisplayed()));
        //verify that the order of the Items are in order.
        onView(withChild(withText(ITEM1_NAME))).check(
                isCompletelyAbove(withChild(withText(ITEM2_NAME))));
    }

    private CarUiShortcutsPopup buildFromBuilder(Toast toast1Mock, Toast toast2Mock, View anchor) {
        CarUiShortcutsPopup.Builder builder = new CarUiShortcutsPopup.Builder();
        builder.addShortcut(
                getShortItem(new CarUiShortcutsPopup.ItemData(R.drawable.ic_add, ITEM1_NAME), true,
                        toast1Mock));
        builder.addShortcut(
                getShortItem(
                        new CarUiShortcutsPopup.ItemData(R.drawable.ic_settings_gear, ITEM2_NAME),
                        false, toast2Mock));
        return builder.build(mActivity, anchor);
    }

    private CarUiShortcutsPopup buildFromConstructor(Toast toast1Mock, Toast toast2Mock,
            View anchor) {
        List<CarUiShortcutsPopup.ShortcutItem> list = new ArrayList<>();
        list.add(getShortItem(new CarUiShortcutsPopup.ItemData(R.drawable.ic_add, ITEM1_NAME), true,
                toast1Mock));
        list.add(getShortItem(
                new CarUiShortcutsPopup.ItemData(R.drawable.ic_settings_gear, ITEM2_NAME),
                false, toast2Mock));
        return new CarUiShortcutsPopup(mActivity, anchor, list);
    }

    private Matcher<View> getPopupMatcher() {
        return withClassName(containsString("PopupWindow"));
    }

    private CarUiShortcutsPopup.ShortcutItem getShortItem(CarUiShortcutsPopup.ItemData data,
            boolean isEnabled, Toast toastMock) {
        return new CarUiShortcutsPopup.ShortcutItem() {
            @Override
            public CarUiShortcutsPopup.ItemData data() {
                return data;
            }

            @Override
            public boolean onClick() {
                if (!isEnabled) {
                    return false;
                }
                toastMock.show();
                return true;
            }

            @Override
            public boolean isEnabled() {
                return isEnabled;
            }
        };
    }
}
