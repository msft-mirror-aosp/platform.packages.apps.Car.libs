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
package com.android.car.ui.paintbooth;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.android.car.oem.tokens.Token;
import com.android.car.ui.core.CarUi;
import com.android.car.ui.paintbooth.overlays.OverlayManager;
import com.android.car.ui.toolbar.NavButtonMode;
import com.android.car.ui.toolbar.ToolbarController;

import java.util.List;
import java.util.Map;

/**
 * Activity that shows displays the values of OEM design tokens.
 */
public class TokenActivity extends Activity {
    private static final String TAG = "TokenActivity";
    private OverlayManager mOverlayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.token_activity);

        ToolbarController toolbar = CarUi.requireToolbar(this);
        toolbar.setTitle(getTitle());
        toolbar.setNavButtonMode(NavButtonMode.BACK);

        Switch oemSwitch = requireViewById(R.id.oem_switch1);

        String tokenLibPackageName = Token.getTokenSharedLibPackageName(getPackageManager());
        if (tokenLibPackageName == null) {
            Toast.makeText(this, "OEM design token shared library not found",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            mOverlayManager = OverlayManager.getInstance(this);
            Map<String, List<OverlayManager.OverlayInfo>> overlays =
                    OverlayManager.getInstance(this).getOverlays();

            List<OverlayManager.OverlayInfo> tokenOverlays = overlays.get(tokenLibPackageName);
            if (tokenOverlays == null) {
                Toast.makeText(this, "No token overlays found", Toast.LENGTH_LONG).show();
                return;
            }

            OverlayManager.OverlayInfo overlay = tokenOverlays.get(0);
            oemSwitch.setChecked(overlay.isEnabled());
            oemSwitch.setEnabled(true);
            oemSwitch.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> applyOverlay(overlay.getPackageName(), isChecked));
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Can't load overlay manager: ", e);
        }

    }

    private void applyOverlay(String overlayPackage, boolean enableOverlay) {
        try {
            mOverlayManager.applyOverlay(overlayPackage, enableOverlay);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "Can't apply overlay: ", e);
        }
    }
}
