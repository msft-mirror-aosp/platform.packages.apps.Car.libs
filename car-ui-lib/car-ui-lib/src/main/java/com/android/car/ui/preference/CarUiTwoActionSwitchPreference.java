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

package com.android.car.ui.preference;

import static com.android.car.ui.utils.CarUiUtils.requireViewByRefId;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import com.android.car.ui.R;
import com.android.car.ui.utils.CarUiUtils;

import java.util.function.Consumer;

/**
 * A preference that has a switch that can be toggled independently of pressing the main
 * body of the preference.
 */
@SuppressWarnings("AndroidJdkLibsChecker")
public class CarUiTwoActionSwitchPreference extends CarUiTwoActionBasePreference {
    @Nullable
    protected Consumer<Boolean> mSecondaryActionOnClickListener;
    private boolean mSecondaryActionChecked;
    private final boolean mSwitchWidgetFocusable = getContext().getResources().getBoolean(
            R.bool.car_ui_preference_two_action_switch_widget_focusable);
    @NonNull
    private Switch mSwitchWidget;

    public CarUiTwoActionSwitchPreference(Context context,
            AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CarUiTwoActionSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CarUiTwoActionSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CarUiTwoActionSwitchPreference(Context context) {
        super(context);
    }

    @Override
    protected void init(@Nullable AttributeSet attrs) {
        super.init(attrs);
        setLayoutResourceInternal(R.layout.car_ui_preference_two_action_switch);
    }

    @Override
    protected void performSecondaryActionClickInternal() {
        mSecondaryActionChecked = !mSecondaryActionChecked;
        notifyChanged();
        if (mSecondaryActionOnClickListener != null) {
            mSecondaryActionOnClickListener.accept(mSecondaryActionChecked);
        }
    }

    @Override
    public void performSecondaryActionClick() {
        super.performSecondaryActionClick();
        // Setting a click listener on the underlying switch widget is necessary to support certain
        // rotary configurations. This causes an unwanted toggle due to the switch's parent class
        // (CompoundButton) always toggling when clicked. So we monitor the state of the
        // underlying switch and override it when it doesn't match this preference's state.
        if (mSwitchWidget.isChecked() != isSecondaryActionChecked()) {
            mSwitchWidget.setChecked(isSecondaryActionChecked());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View firstActionContainer = requireViewByRefId(holder.itemView,
                R.id.car_ui_first_action_container);
        View secondActionContainer = requireViewByRefId(holder.itemView,
                R.id.car_ui_second_action_container);
        View secondaryAction = requireViewByRefId(holder.itemView,
                R.id.car_ui_secondary_action);
        mSwitchWidget = requireViewByRefId(holder.itemView,
                R.id.car_ui_secondary_action_concrete);
        boolean firstActionEnabledViewState = isEnabled() || isUxRestricted()
                || isClickableWhileDisabled();
        boolean secondaryActionEnabledViewState = isSecondaryActionEnabled() || isUxRestricted()
                || isClickableWhileDisabled();

        holder.itemView.setFocusable(false);
        holder.itemView.setClickable(false);
        firstActionContainer.setOnClickListener(this::performClickUnrestricted);
        firstActionContainer.setEnabled(firstActionEnabledViewState);
        firstActionContainer.setFocusable(firstActionEnabledViewState);

        secondActionContainer.setVisibility(mSecondaryActionVisible ? View.VISIBLE : View.GONE);
        mSwitchWidget.setChecked(mSecondaryActionChecked);

        secondaryAction.setEnabled(secondaryActionEnabledViewState);
        mSwitchWidget.setEnabled(secondaryActionEnabledViewState);
        secondaryAction.setOnClickListener(v -> performSecondaryActionClick());
        mSwitchWidget.setOnClickListener(v -> performSecondaryActionClick());
        // When a switch is enabled, its onTouchEvent listens for drag events so that the switch can
        // be toggled when the thumb is dragged. When this preference's secondary action is ux
        // restricted or is disabled and clickable, the thumb shouldn't be draggable. So, intercept
        // touch events and return true (which eats the event). Return false when the secondary
        // action is enabled and not ux restricted (which will flow into normal touch behavior).
        mSwitchWidget.setOnTouchListener((view, event) -> {
            if ((!isSecondaryActionEnabled() && isClickableWhileDisabled()) || isUxRestricted()) {
                // When handling touch events, the touch listener is responsible for performing a
                // click if applicable. Only perform click when click is finished (i.e., ACTION_UP).
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                return true;
            }
            // Don't handle the touch event, fallback to default behavior
            return false;
        });
        if (mSwitchWidgetFocusable) {
            secondaryAction.setFocusable(false);
            mSwitchWidget.setFocusable(secondaryActionEnabledViewState);
        } else {
            mSwitchWidget.setFocusable(false);
            secondaryAction.setFocusable(secondaryActionEnabledViewState);
        }

        CarUiUtils.makeAllViewsEnabledAndUxRestricted(secondaryAction, isSecondaryActionEnabled(),
                isUxRestricted());
    }

    /**
     * Sets the checked state of the switch in the secondary action space.
     * @param checked Whether the switch should be checked or not.
     */
    public void setSecondaryActionChecked(boolean checked) {
        mSecondaryActionChecked = checked;
        notifyChanged();
    }

    /**
     * Returns the checked state of the switch in the secondary action space.
     * @return Whether the switch is checked or not.
     */
    public boolean isSecondaryActionChecked() {
        return mSecondaryActionChecked;
    }

    /**
     * Sets the on-click listener of the secondary action button.
     * The listener is called with the current checked state of the switch.
     */
    public void setOnSecondaryActionClickListener(@Nullable Consumer<Boolean> onClickListener) {
        mSecondaryActionOnClickListener = onClickListener;
        notifyChanged();
    }
}
