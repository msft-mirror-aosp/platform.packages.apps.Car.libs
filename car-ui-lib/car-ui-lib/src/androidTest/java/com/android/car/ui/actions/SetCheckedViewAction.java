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
package com.android.car.ui.actions;

import static org.hamcrest.Matchers.isA;

import android.view.View;
import android.widget.Checkable;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * This matcher will be used to toggle the checkable view depending on its previous state.
 *
 * Adapted from google3/java/com/google/android/apps/chromecast/app/testing/UiTestUtils.java
 */
public class SetCheckedViewAction implements ViewAction {

    final boolean mChecked;

    public SetCheckedViewAction(boolean checked) {
        mChecked = checked;
    }

    @Override
    public Matcher<View> getConstraints() {
        return new BaseMatcher<View>() {
            @Override
            public boolean matches(Object item) {
                return isA(Checkable.class).matches(item);
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
                mismatchDescription.appendText("expected: " + Checkable.class);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("View should be checkable e.g. Switch");
            }
        };
    }

    @Override
    public String getDescription() {
        return "Set the checkable state of view";
    }

    @Override
    public void perform(UiController uiController, View view) {
        Checkable checkableView = (Checkable) view;
        checkableView.setChecked(mChecked);
    }
}
