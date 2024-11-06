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
package com.android.car.ui.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * This class is a {link @BroadcastReceiver} that initiates a task on user unlock.
 */
public class CarUiUserUnlockedReceiver extends BroadcastReceiver {
    private final Thread mUnlockTask;

    public CarUiUserUnlockedReceiver(@NonNull Thread thread) {
        mUnlockTask = thread;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (this) {
            if (Intent.ACTION_USER_UNLOCKED.equals(intent.getAction())
                    && mUnlockTask.getState() == Thread.State.NEW) {
                mUnlockTask.start();
            }
            context.unregisterReceiver(this);
        }
    }
}
