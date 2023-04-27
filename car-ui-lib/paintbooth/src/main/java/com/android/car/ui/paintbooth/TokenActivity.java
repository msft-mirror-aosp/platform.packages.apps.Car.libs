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
import android.util.Pair;
import android.widget.Switch;
import android.widget.Toast;

import com.android.car.oem.tokens.Token;
import com.android.car.ui.core.CarUi;
import com.android.car.ui.paintbooth.overlays.OverlayManager;
import com.android.car.ui.recyclerview.CarUiRecyclerView;
import com.android.car.ui.toolbar.NavButtonMode;
import com.android.car.ui.toolbar.Tab;
import com.android.car.ui.toolbar.ToolbarController;

import java.util.ArrayList;
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

        CarUiRecyclerView list = requireViewById(R.id.list);
        TokenDemoAdapter adapter = new TokenDemoAdapter(createColorList());
        list.setAdapter(adapter);

        ToolbarController toolbar = CarUi.requireToolbar(this);
        toolbar.setTitle(getTitle());
        toolbar.setNavButtonMode(NavButtonMode.BACK);

        List<Tab> tabs = new ArrayList<>();
        tabs.add(Tab.builder().setText("Color").setIcon(
                getDrawable(R.drawable.car_ui_icon_edit)).setSelectedListener(
                tab -> list.setAdapter(new TokenDemoAdapter(createColorList()))).build());
        tabs.add(Tab.builder().setText("Text").setIcon(
                getDrawable(R.drawable.car_ui_icon_edit)).setSelectedListener(
                tab -> list.setAdapter(new TokenDemoAdapter(createTextList()))).build());
        tabs.add(Tab.builder().setText("Shape").setIcon(
                getDrawable(R.drawable.car_ui_icon_edit)).setSelectedListener(
                tab -> list.setAdapter(new TokenDemoAdapter(createCornerRadiusList()))).build());
        toolbar.setTabs(tabs, 0);

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

    private List<Pair<String, Integer>> createColorList() {
        List<Pair<String, Integer>> list = new ArrayList<>();

        list.add(new Pair<>("colorPrimary", R.styleable.OemTokens_oemColorPrimary));
        list.add(new Pair<>("colorOnPrimary", R.styleable.OemTokens_oemColorOnPrimary));
        list.add(new Pair<>("colorPrimaryContainer",
                R.styleable.OemTokens_oemColorPrimaryContainer));
        list.add(new Pair<>("colorPrimaryOnContainer",
                R.styleable.OemTokens_oemColorOnPrimaryContainer));

        list.add(new Pair<>("colorSecondary", R.styleable.OemTokens_oemColorSecondary));
        list.add(new Pair<>("colorOnSecondary", R.styleable.OemTokens_oemColorOnSecondary));
        list.add(new Pair<>("colorSecondaryContainer",
                R.styleable.OemTokens_oemColorSecondaryContainer));
        list.add(new Pair<>("colorSecondaryOnContainer",
                R.styleable.OemTokens_oemColorOnSecondaryContainer));

        list.add(new Pair<>("colorTertiary", R.styleable.OemTokens_oemColorTertiary));
        list.add(new Pair<>("colorOnTertiary", R.styleable.OemTokens_oemColorOnTertiary));
        list.add(new Pair<>("colorTertiaryContainer",
                R.styleable.OemTokens_oemColorTertiaryContainer));
        list.add(new Pair<>("colorTertiaryOnContainer",
                R.styleable.OemTokens_oemColorOnTertiaryContainer));

        list.add(new Pair<>("colorError", R.styleable.OemTokens_oemColorError));
        list.add(new Pair<>("colorOnError", R.styleable.OemTokens_oemColorOnError));
        list.add(new Pair<>("colorErrorContainer", R.styleable.OemTokens_oemColorErrorContainer));
        list.add(new Pair<>("colorErrorOnContainer",
                R.styleable.OemTokens_oemColorOnErrorContainer));

        list.add(new Pair<>("colorSurface", R.styleable.OemTokens_oemColorSurface));
        list.add(new Pair<>("colorOnSurface", R.styleable.OemTokens_oemColorOnSurface));
        list.add(new Pair<>("colorSurfaceVariant", R.styleable.OemTokens_oemColorSurfaceVariant));
        list.add(new Pair<>("colorOnSurfaceVariant",
                R.styleable.OemTokens_oemColorOnSurfaceVariant));

        list.add(new Pair<>("colorOutline", R.styleable.OemTokens_oemColorOutline));

        return list;
    }

    private List<Pair<String, Integer>> createTextList() {
        List<Pair<String, Integer>> list = new ArrayList<>();
        list.add(new Pair<>("textAppearanceDisplayLarge",
                R.styleable.OemTokens_oemTextAppearanceDisplayLarge));
        list.add(new Pair<>("textAppearanceDisplayMedium",
                R.styleable.OemTokens_oemTextAppearanceDisplayMedium));
        list.add(new Pair<>("textAppearanceDisplaySmall",
                R.styleable.OemTokens_oemTextAppearanceDisplaySmall));

        list.add(new Pair<>("textAppearanceHeadlineLarge",
                R.styleable.OemTokens_oemTextAppearanceHeadlineLarge));
        list.add(new Pair<>("textAppearanceHeadlineMedium",
                R.styleable.OemTokens_oemTextAppearanceHeadlineMedium));
        list.add(new Pair<>("textAppearanceHeadlineSmall",
                R.styleable.OemTokens_oemTextAppearanceHeadlineSmall));

        list.add(new Pair<>("textAppearanceTitleLarge",
                R.styleable.OemTokens_oemTextAppearanceTitleLarge));
        list.add(new Pair<>("textAppearanceTitleMedium",
                R.styleable.OemTokens_oemTextAppearanceTitleMedium));
        list.add(new Pair<>("textAppearanceTitleSmall",
                R.styleable.OemTokens_oemTextAppearanceTitleSmall));

        list.add(new Pair<>("textAppearanceLabelLarge",
                R.styleable.OemTokens_oemTextAppearanceLabelLarge));
        list.add(new Pair<>("textAppearanceLabelMedium",
                R.styleable.OemTokens_oemTextAppearanceLabelMedium));
        list.add(new Pair<>("textAppearanceLabelSmall",
                R.styleable.OemTokens_oemTextAppearanceLabelSmall));

        list.add(new Pair<>("textAppearanceBodyLarge",
                R.styleable.OemTokens_oemTextAppearanceBodyLarge));
        list.add(new Pair<>("textAppearanceBodyMedium",
                R.styleable.OemTokens_oemTextAppearanceBodyMedium));
        list.add(new Pair<>("textAppearanceBodySmall",
                R.styleable.OemTokens_oemTextAppearanceBodySmall));

        return list;
    }

    private List<Pair<String, Integer>> createCornerRadiusList() {
        List<Pair<String, Integer>> list = new ArrayList<>();
        list.add(new Pair<>("shapeCornerNone",
                R.styleable.OemTokens_oemShapeCornerNone));
        list.add(new Pair<>("shapeCornerExtraSmall",
                R.styleable.OemTokens_oemShapeCornerExtraSmall));
        list.add(new Pair<>("shapeCornerSmall",
                R.styleable.OemTokens_oemShapeCornerSmall));
        list.add(new Pair<>("shapeCornerMedium",
                R.styleable.OemTokens_oemShapeCornerMedium));
        list.add(new Pair<>("shapeCornerLarge",
                R.styleable.OemTokens_oemShapeCornerLarge));
        list.add(new Pair<>("shapeCornerExtraLarge",
                R.styleable.OemTokens_oemShapeCornerExtraLarge));
        list.add(new Pair<>("shapeCornerFull",
                R.styleable.OemTokens_oemShapeCornerFull));

        return list;
    }
}
