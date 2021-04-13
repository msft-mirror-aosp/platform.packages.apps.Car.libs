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

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.car.ui.appstyledview.AppStyledDialogController;
import com.android.car.ui.appstyledview.AppStyledViewController.AppStyledViewNavIcon;
import com.android.car.ui.baselayout.Insets;
import com.android.car.ui.baselayout.InsetsChangedListener;
import com.android.car.ui.core.CarUi;
import com.android.car.ui.paintbooth.R;
import com.android.car.ui.toolbar.Toolbar;
import com.android.car.ui.toolbar.ToolbarController;

/**
 * Sample activity to show app styled Dialog fragment.
 */
public class AppStyledViewSampleActivity extends AppCompatActivity implements
        InsetsChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.app_styled_view_sample_activity);

        ToolbarController toolbar = CarUi.getToolbar(this);
        toolbar.setTitle(getTitle());
        toolbar.setNavButtonMode(Toolbar.NavButtonMode.BACK);
        toolbar.setLogo(R.drawable.ic_launcher);

        AppStyledDialogController controller = new AppStyledDialogController(this);
        int width = controller.getAppStyledViewDialogWidth();

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        config.smallestScreenWidthDp = width;
        // fake the min screen size so resources load from the corresponding folders. For eg.
        // layout-sw400dp
        Context testContext = createConfigurationContext(config);

        Context contextThemeWrapper = new ContextThemeWrapper(testContext,
                R.style.AppStyledDialogThemeSample);

        View appStyledTestView = LayoutInflater.from(contextThemeWrapper)
                .inflate(R.layout.app_styled_view_test_sample, null, false);

        Button btn = findViewById(R.id.show_app_styled_fragment);
        btn.setOnClickListener(v -> {
            controller.setContentView(appStyledTestView);
            controller.setNavIcon(AppStyledViewNavIcon.CLOSE);
            controller.show();
        });
    }

    @Override
    public void onCarUiInsetsChanged(@NonNull Insets insets) {
        requireViewById(R.id.app_styled_sample_content)
                .setPadding(0, insets.getTop(), 0, insets.getBottom());
        requireViewById(android.R.id.content)
                .setPadding(insets.getLeft(), 0, insets.getRight(), 0);
    }
}
