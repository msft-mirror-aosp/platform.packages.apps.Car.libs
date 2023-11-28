/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.car.apps.common;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.android.car.ui.utils.DirectManipulationHelper;

/**
 * A helper class for {@link CarSeekBar}.
 */
public class CarSeekBarRotaryHelper {

    private SeekBar mSeekBar;
    private boolean mAdjustable;
    private boolean mInDirectManipulationMode;

    private final View.OnKeyListener mSeekBarKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // Don't allow events through if there is no SeekBar, the SeekBar is disabled,
            // or we're in non-adjustable mode.
            if (mSeekBar == null || !mSeekBar.isEnabled() || !mAdjustable) {
                return false;
            }

            // Consume nudge events in direct manipulation mode.
            if (mInDirectManipulationMode
                    && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    || keyCode == KeyEvent.KEYCODE_DPAD_UP
                    || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
                return true;
            }

            // Handle events to enter or exit direct manipulation mode.
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    setInDirectManipulationMode(v, !mInDirectManipulationMode);
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mInDirectManipulationMode) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        setInDirectManipulationMode(v, false);
                    }
                    return true;
                }
            }

            // Don't propagate confirm keys to the SeekBar to prevent a ripple effect on the thumb.
            if (isConfirmKey(keyCode)) {
                return false;
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                return mSeekBar.onKeyDown(keyCode, event);
            } else {
                return mSeekBar.onKeyUp(keyCode, event);
            }
        }
    };

    private final View.OnFocusChangeListener mSeekBarFocusChangeListener =
            (v, hasFocus) -> {
                if (!hasFocus && mInDirectManipulationMode && mSeekBar != null) {
                    setInDirectManipulationMode(v, false);
                }
            };

    private final View.OnGenericMotionListener mSeekBarScrollListener = (v, event) -> {
        if (!mInDirectManipulationMode || !mAdjustable || mSeekBar == null) {
            return false;
        }
        int adjustment = Math.round(event.getAxisValue(MotionEvent.AXIS_SCROLL));
        if (adjustment == 0) {
            return false;
        }
        int count = Math.abs(adjustment);
        int keyCode = adjustment < 0 ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
        KeyEvent downEvent = new KeyEvent(event.getDownTime(), event.getEventTime(),
                KeyEvent.ACTION_DOWN, keyCode, /* repeat= */ 0);
        KeyEvent upEvent = new KeyEvent(event.getDownTime(), event.getEventTime(),
                KeyEvent.ACTION_UP, keyCode, /* repeat= */ 0);
        for (int i = 0; i < count; i++) {
            mSeekBar.onKeyDown(keyCode, downEvent);
            mSeekBar.onKeyUp(keyCode, upEvent);
        }
        return true;
    };

    private void setInDirectManipulationMode(View view, boolean enable) {
        mInDirectManipulationMode = enable;
        DirectManipulationHelper.enableDirectManipulationMode(mSeekBar, enable);
        // The Seekbar is highlighted when it's focused with one exception. In direct
        // manipulation (DM) mode, the SeekBar's thumb is highlighted instead. In DM mode, the
        // SeekBar are selected. The Seekbar's highlight is drawn when it's focused but not
        // selected, while the SeekBar's thumb highlight is drawn when the SeekBar is selected.
        view.setSelected(enable);
        mSeekBar.setSelected(enable);
    }

    public CarSeekBarRotaryHelper(SeekBar seekBar, boolean adjustable) {
        mSeekBar = seekBar;
        mAdjustable = adjustable;

        mSeekBar.setOnKeyListener(mSeekBarKeyListener);
        mSeekBar.setOnFocusChangeListener(mSeekBarFocusChangeListener);
        mSeekBar.setOnGenericMotionListener(mSeekBarScrollListener);
    }

    public void setAdjustable(boolean adjustable) {
        mAdjustable = adjustable;
    }

    private boolean isConfirmKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                return true;
            default:
                return false;
        }
    }
}
