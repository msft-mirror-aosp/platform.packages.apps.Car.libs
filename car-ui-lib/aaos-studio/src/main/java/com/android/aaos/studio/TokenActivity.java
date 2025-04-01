/*
 * Copyright (C) 2025 The Android Open Source Project
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
package com.android.aaos.studio;

import android.app.Activity;
import android.content.om.FabricatedOverlay;
import android.content.om.OverlayIdentifier;
import android.content.om.OverlayManager;
import android.content.om.OverlayManagerTransaction;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.car.oem.tokens.Token;
import com.android.car.ui.core.CarUi;
import com.android.car.ui.recyclerview.CarUiRecyclerView;
import com.android.car.ui.toolbar.MenuItem;
import com.android.car.ui.toolbar.NavButtonMode;
import com.android.car.ui.toolbar.Tab;
import com.android.car.ui.toolbar.ToolbarController;

import com.google.ux.material.libmonet.hct.Hct;
import com.google.ux.material.libmonet.scheme.SchemeVibrant;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that shows displays the values of OEM design tokens.
 */
public class TokenActivity extends Activity {
    private static final String TAG = "TokenActivity";
    private static final String OWNING_PACKAGE = "com.android.aaos.studio";
    private static final String TARGET_PACKAGE = "oem.demo.sharedlib";
    private static final String OVERLAY_NAME = "FabricatedThemeTokenLib27";

    private OverlayManager mOverlayManager;

    private int mPrimaryRed;
    private int mPrimaryGreen;
    private int mPrimaryBlue;
    private View mPrimaryColorView;

    private SchemeVibrant mScheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Token.applyOemTokenStyle(this);

        setContentView(R.layout.token_activity);

        mOverlayManager = getSystemService(android.content.om.OverlayManager.class);

        CarUiRecyclerView list = requireViewById(R.id.list);
        TokenDemoAdapter adapter = new TokenDemoAdapter(createColorList());
        list.setAdapter(adapter);

        ToolbarController toolbar = CarUi.requireToolbar(this);
        toolbar.setTitle(getTitle());
        toolbar.setNavButtonMode(NavButtonMode.BACK);

        List<Tab> tabs = new ArrayList<>();
        tabs.add(Tab.builder()
                .setText("Color")
                .setIcon(getDrawable(R.drawable.car_ui_icon_edit))
                .setSelectedListener(
                        tab -> list.setAdapter(new TokenDemoAdapter(createColorList())))
                .build());
        tabs.add(Tab.builder()
                .setText("Text")
                .setIcon(getDrawable(R.drawable.car_ui_icon_edit))
                .setSelectedListener(
                        tab -> list.setAdapter(new TokenDemoAdapter(createTextList())))
                .build());
        tabs.add(Tab.builder()
                .setText("Shape")
                .setIcon(getDrawable(R.drawable.car_ui_icon_edit))
                .setSelectedListener(
                        tab -> list.setAdapter(new TokenDemoAdapter(createCornerRadiusList())))
                .build());
        toolbar.setTabs(tabs, 0);

        String tokenLibPackageName = Token.getTokenSharedLibPackageName(getPackageManager());
        if (tokenLibPackageName == null) {
            Toast.makeText(this, "OEM design token shared library not found",
                    Toast.LENGTH_LONG).show();
            return;
        }

        SeekBar seekBar1 = findViewById(R.id.seekbar1);
        SeekBar seekBar2 = findViewById(R.id.seekbar2);
        SeekBar seekBar3 = findViewById(R.id.seekbar3);
        mPrimaryColorView = findViewById(R.id.primary_color);

        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(MenuItem.builder(this)
                .setTitle("Enable RRO")
                .setOnClickListener(i -> {
                    int seedColor = Color.argb(255, mPrimaryRed, mPrimaryGreen, mPrimaryBlue);
                    Hct seed = Hct.fromInt(seedColor);
                    mScheme = new SchemeVibrant(seed, true, 0.0);
                    updateOverlay();
                })
                .build());
        menuItems.add(MenuItem.builder(this)
                .setTitle("Disable RRO")
                .setOnClickListener(i -> disableOverlay())
                .build());
        toolbar.setMenuItems(menuItems);

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPrimaryRed = progress;
                updatePrimary();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPrimaryGreen = progress;
                updatePrimary();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPrimaryBlue = progress;
                updatePrimary();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updatePrimary() {
        int color = Color.rgb(mPrimaryRed, mPrimaryGreen, mPrimaryBlue);
        ((GradientDrawable) mPrimaryColorView.getBackground()).setColor(color);
    }

    private void disableOverlay() {
        OverlayManagerTransaction.Builder transaction =
                new OverlayManagerTransaction.Builder()
                        .unregisterFabricatedOverlay(
                                new OverlayIdentifier(OWNING_PACKAGE, OVERLAY_NAME));
        mOverlayManager.commit(transaction.build());
    }

    private void updateOverlay() {
        disableOverlay();

        FabricatedOverlay overlay = new FabricatedOverlay.Builder(OWNING_PACKAGE, OVERLAY_NAME,
                TARGET_PACKAGE)
                .setResourceValue("com.android.oem.tokens:bool/enable_oem_tokens",
                        TypedValue.TYPE_INT_BOOLEAN, 1, null)
                // Set color resources
                .setResourceValue("com.android.oem.tokens:color/color_primary",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getPrimary(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_primary",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnPrimary(), null)
                .setResourceValue("com.android.oem.tokens:color/color_primary_container",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getPrimaryContainer(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_primary_container",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnPrimaryContainer(), null)
                .setResourceValue("com.android.oem.tokens:color/color_secondary",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSecondary(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_secondary",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnSecondary(), null)
                .setResourceValue("com.android.oem.tokens:color/color_secondary_container",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSecondaryContainer(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_secondary_container",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnSecondaryContainer(), null)
                .setResourceValue("com.android.oem.tokens:color/color_tertiary",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getTertiary(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_tertiary",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnTertiary(), null)
                .setResourceValue("com.android.oem.tokens:color/color_tertiary_container",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getTertiaryContainer(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_tertiary_container",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnTertiaryContainer(), null)
                .setResourceValue("com.android.oem.tokens:color/color_error",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getError(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_error",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnError(), null)
                .setResourceValue("com.android.oem.tokens:color/color_error_container",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getErrorContainer(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_error_container",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnErrorContainer(), null)
                .setResourceValue("com.android.oem.tokens:color/color_background",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getBackground(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_background",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnBackground(), null)
                .setResourceValue("com.android.oem.tokens:color/color_surface",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSurface(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_surface",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnSurface(), null)
                .setResourceValue("com.android.oem.tokens:color/color_surface_variant",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSurfaceVariant(), null)
                .setResourceValue("com.android.oem.tokens:color/color_on_surface_variant",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOnSurfaceVariant(), null)
                .setResourceValue("com.android.oem.tokens:color/color_outline",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOutline(), null)
                .setResourceValue("com.android.oem.tokens:color/color_outline_variant",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getOutlineVariant(), null)
                .setResourceValue("com.android.oem.tokens:color/color_scrim",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getScrim(), null)
                .setResourceValue("com.android.oem.tokens:color/color_surface_dim",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSurfaceDim(), null)
                .setResourceValue("com.android.oem.tokens:color/color_surface_bright",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSurfaceBright(), null)
                .setResourceValue("com.android.oem.tokens:color/color_surface_container",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSurfaceContainer(), null)
                .setResourceValue("com.android.oem.tokens:color/color_surface_container_low",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSurfaceContainerLow(), null)
                .setResourceValue("com.android.oem.tokens:color/color_surface_container_lowest",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSurfaceContainerLowest(), null)
                .setResourceValue("com.android.oem.tokens:color/color_surface_container_high",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSurfaceContainerHigh(), null)
                .setResourceValue("com.android.oem.tokens:color/color_surface_container_highest",
                        TypedValue.TYPE_INT_COLOR_ARGB8, mScheme.getSurfaceContainerHighest(), null)
                .build();

        OverlayManagerTransaction.Builder transaction =
                new OverlayManagerTransaction.Builder()
                        .registerFabricatedOverlay(overlay)
                        .setEnabled(overlay.getIdentifier(), true)
                        .setEnabled(overlay.getIdentifier(), true, 0);

        mOverlayManager.commit(transaction.build());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private List<Pair<String, Integer>> createColorList() {
        List<Pair<String, Integer>> list = new ArrayList<>();

        list.add(new Pair<>("colorPrimary", R.attr.oemColorPrimary));
        list.add(new Pair<>("colorOnPrimary", R.attr.oemColorOnPrimary));
        list.add(new Pair<>("colorPrimaryContainer",
                R.attr.oemColorPrimaryContainer));
        list.add(new Pair<>("colorPrimaryOnContainer",
                R.attr.oemColorOnPrimaryContainer));

        list.add(new Pair<>("colorSecondary", R.attr.oemColorSecondary));
        list.add(new Pair<>("colorOnSecondary", R.attr.oemColorOnSecondary));
        list.add(new Pair<>("colorSecondaryContainer",
                R.attr.oemColorSecondaryContainer));
        list.add(new Pair<>("colorSecondaryOnContainer",
                R.attr.oemColorOnSecondaryContainer));

        list.add(new Pair<>("colorTertiary", R.attr.oemColorTertiary));
        list.add(new Pair<>("colorOnTertiary", R.attr.oemColorOnTertiary));
        list.add(new Pair<>("colorTertiaryContainer",
                R.attr.oemColorTertiaryContainer));
        list.add(new Pair<>("colorTertiaryOnContainer",
                R.attr.oemColorOnTertiaryContainer));

        list.add(new Pair<>("colorError", R.attr.oemColorError));
        list.add(new Pair<>("colorOnError", R.attr.oemColorOnError));
        list.add(new Pair<>("colorErrorContainer", R.attr.oemColorErrorContainer));
        list.add(new Pair<>("colorErrorOnContainer",
                R.attr.oemColorOnErrorContainer));

        list.add(new Pair<>("colorBackground", R.attr.oemColorBackground));
        list.add(new Pair<>("colorOnBackground", R.attr.oemColorOnBackground));
        list.add(new Pair<>("colorSurface", R.attr.oemColorSurface));
        list.add(new Pair<>("colorOnSurface", R.attr.oemColorOnSurface));
        list.add(new Pair<>("colorSurfaceVariant", R.attr.oemColorSurfaceVariant));
        list.add(new Pair<>("colorOnSurfaceVariant",
                R.attr.oemColorOnSurfaceVariant));

        list.add(new Pair<>("colorOutline", R.attr.oemColorOutline));

        list.add(new Pair<>("colorSurfaceDim", R.attr.oemColorSurfaceDim));
        list.add(new Pair<>("colorSurfaceBright", R.attr.oemColorSurfaceBright));
        list.add(new Pair<>("colorSurfaceContainer",
                R.attr.oemColorSurfaceContainer));
        list.add(new Pair<>("colorSurfaceContainerLow", R.attr.oemColorSurfaceContainerLow));
        list.add(new Pair<>("colorSurfaceContainerLowest", R.attr.oemColorSurfaceContainerLowest));
        list.add(new Pair<>("colorSurfaceContainerHigh",
                R.attr.oemColorSurfaceContainerHigh));
        list.add(new Pair<>("colorSurfaceContainerHighest",
                R.attr.oemColorSurfaceContainerHighest));
        list.add(new Pair<>("colorShadow",
                R.attr.oemColorShadow));

        list.add(new Pair<>("colorBlue", R.attr.oemColorBlue));
        list.add(new Pair<>("colorOnBlue",
                R.attr.oemColorOnBlue));
        list.add(new Pair<>("colorBlueContainer", R.attr.oemColorBlueContainer));
        list.add(new Pair<>("colorOnBlueContainer",
                R.attr.oemColorOnBlueContainer));

        list.add(new Pair<>("colorGreen", R.attr.oemColorGreen));
        list.add(new Pair<>("colorOnGreen",
                R.attr.oemColorOnGreenContainer));
        list.add(new Pair<>("colorGreenContainer", R.attr.oemColorGreenContainer));
        list.add(new Pair<>("colorOnGreenContainer",
                R.attr.oemColorOnGreenContainer));

        list.add(new Pair<>("colorYellow", R.attr.oemColorYellow));
        list.add(new Pair<>("colorOnYellow",
                R.attr.oemColorOnYellow));
        list.add(new Pair<>("colorYellowContainer", R.attr.oemColorYellowContainer));
        list.add(new Pair<>("colorOnYellowContainer",
                R.attr.oemColorOnYellowContainer));

        return list;
    }

    private List<Pair<String, Integer>> createTextList() {
        List<Pair<String, Integer>> list = new ArrayList<>();
        list.add(new Pair<>("textAppearanceDisplayLarge",
                R.attr.oemTextAppearanceDisplayLarge));
        list.add(new Pair<>("textAppearanceDisplayMedium",
                R.attr.oemTextAppearanceDisplayMedium));
        list.add(new Pair<>("textAppearanceDisplaySmall",
                R.attr.oemTextAppearanceDisplaySmall));

        list.add(new Pair<>("textAppearanceHeadlineLarge",
                R.attr.oemTextAppearanceHeadlineLarge));
        list.add(new Pair<>("textAppearanceHeadlineMedium",
                R.attr.oemTextAppearanceHeadlineMedium));
        list.add(new Pair<>("textAppearanceHeadlineSmall",
                R.attr.oemTextAppearanceHeadlineSmall));

        list.add(new Pair<>("textAppearanceTitleLarge",
                R.attr.oemTextAppearanceTitleLarge));
        list.add(new Pair<>("textAppearanceTitleMedium",
                R.attr.oemTextAppearanceTitleMedium));
        list.add(new Pair<>("textAppearanceTitleSmall",
                R.attr.oemTextAppearanceTitleSmall));

        list.add(new Pair<>("textAppearanceLabelLarge",
                R.attr.oemTextAppearanceLabelLarge));
        list.add(new Pair<>("textAppearanceLabelMedium",
                R.attr.oemTextAppearanceLabelMedium));
        list.add(new Pair<>("textAppearanceLabelSmall",
                R.attr.oemTextAppearanceLabelSmall));

        list.add(new Pair<>("textAppearanceBodyLarge",
                R.attr.oemTextAppearanceBodyLarge));
        list.add(new Pair<>("textAppearanceBodyMedium",
                R.attr.oemTextAppearanceBodyMedium));
        list.add(new Pair<>("textAppearanceBodySmall",
                R.attr.oemTextAppearanceBodySmall));

        return list;
    }

    private List<Pair<String, Integer>> createCornerRadiusList() {
        List<Pair<String, Integer>> list = new ArrayList<>();
        list.add(new Pair<>("shapeCornerNone",
                R.attr.oemShapeCornerNone));
        list.add(new Pair<>("shapeCornerExtraSmall",
                R.attr.oemShapeCornerExtraSmall));
        list.add(new Pair<>("shapeCornerSmall",
                R.attr.oemShapeCornerSmall));
        list.add(new Pair<>("shapeCornerMedium",
                R.attr.oemShapeCornerMedium));
        list.add(new Pair<>("shapeCornerLarge",
                R.attr.oemShapeCornerLarge));
        list.add(new Pair<>("shapeCornerExtraLarge",
                R.attr.oemShapeCornerExtraLarge));
        list.add(new Pair<>("shapeCornerFull",
                R.attr.oemShapeCornerFull));

        return list;
    }
}
