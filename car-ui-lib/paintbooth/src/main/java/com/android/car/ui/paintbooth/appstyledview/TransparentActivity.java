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
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.android.car.ui.appstyledview.AppStyledDialogController;
import com.android.car.ui.appstyledview.AppStyledDialogController.NavIcon;
import com.android.car.ui.paintbooth.R;

/**
 * Sample transparent activity to show app styled Dialog fragment.
 */
public class TransparentActivity extends AppCompatActivity {
    private AppStyledDialogController mAppStyledDialogController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent_activity);

        mAppStyledDialogController = new AppStyledDialogController(this);
        View appStyledTestView = LayoutInflater.from(
                        mAppStyledDialogController.createContentViewConfigurationContext(this,
                                R.style.AppStyledDialogThemeSample))
                .inflate(R.layout.app_styled_view_test_sample, null, false);

        mAppStyledDialogController.setOnNavIconClickListener(
                () -> mAppStyledDialogController.dismiss());

        Button btn = findViewById(R.id.show_app_styled_view);
        btn.setOnClickListener(v -> {
            mAppStyledDialogController.setContentView(appStyledTestView);
            mAppStyledDialogController.setNavIconType(NavIcon.CLOSE);
            mAppStyledDialogController.show();
        });
        Button btn2 = findViewById(R.id.close_activity);
        btn2.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAppStyledDialogController != null) {
            mAppStyledDialogController.dismiss();
        }
    }
}
