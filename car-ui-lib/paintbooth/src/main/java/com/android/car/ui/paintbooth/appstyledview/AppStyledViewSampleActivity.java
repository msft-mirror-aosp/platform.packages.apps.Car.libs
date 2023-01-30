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
package com.android.car.ui.paintbooth.appstyledview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.android.car.ui.appstyledview.AppStyledDialogController;
import com.android.car.ui.appstyledview.AppStyledViewController.AppStyledViewNavIcon;
import com.android.car.ui.core.CarUi;
import com.android.car.ui.paintbooth.R;
import com.android.car.ui.toolbar.NavButtonMode;
import com.android.car.ui.toolbar.ToolbarController;

/**
 * Sample activity to show app styled Dialog fragment.
 */
public class AppStyledViewSampleActivity extends AppCompatActivity {
    private AppStyledDialogController mAppStyledDialogController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.app_styled_view_sample_activity);

        ToolbarController toolbar = CarUi.requireToolbar(this);
        toolbar.setTitle(getTitle());
        toolbar.setNavButtonMode(NavButtonMode.BACK);
        toolbar.setLogo(R.drawable.ic_launcher);

        mAppStyledDialogController = new AppStyledDialogController(this);
        View appStyledTestView = LayoutInflater.from(
                        mAppStyledDialogController.createContentViewConfigurationContext(this))
                .inflate(R.layout.app_styled_view_test_sample, null, false);

        mAppStyledDialogController.setOnNavIconClickListener(
                () -> mAppStyledDialogController.dismiss());
        mAppStyledDialogController.setOnDismissListener(() -> showSystemBars());

        Button btn = findViewById(R.id.show_app_styled_fragment);
        btn.setOnClickListener(v -> {
            mAppStyledDialogController.setContentView(appStyledTestView);
            mAppStyledDialogController.setNavIcon(AppStyledViewNavIcon.CLOSE);
            hideSystemBars();
            mAppStyledDialogController.show();
        });
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }

        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        // Hide the system bars
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    private void showSystemBars() {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }

        // Show the system bars
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAppStyledDialogController != null) {
            mAppStyledDialogController.dismiss();
            showSystemBars();
        }
    }
}
