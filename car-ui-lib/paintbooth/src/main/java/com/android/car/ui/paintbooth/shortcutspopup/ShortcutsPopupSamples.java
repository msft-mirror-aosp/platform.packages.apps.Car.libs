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

package com.android.car.ui.paintbooth.shortcutspopup;

import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.car.ui.paintbooth.R;
import com.android.car.ui.paintbooth.theme.AbstractSampleActivity;
import com.android.car.ui.shortcutspopup.CarUiShortcutsPopup;

/**
 * Reference for {@link CarUiShortcutsPopup}
 */
public class ShortcutsPopupSamples extends AbstractSampleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shortcuts_popup_activity);
        ImageView button = findViewById(R.id.sample_btn1);
        String stopAppActionName = "Stop app";
        String reRunAppActionName = "Rerun app";
        button.setOnClickListener(v -> {
            CarUiShortcutsPopup carUiShortcutsPopup = new CarUiShortcutsPopup.Builder()
                    .addShortcut(
                            new CarUiShortcutsPopup.ShortcutItem() {
                                @Override
                                public CarUiShortcutsPopup.ItemData data() {
                                    return new CarUiShortcutsPopup.ItemData(
                                            R.drawable.car_ui_icon_add, stopAppActionName);
                                }

                                @Override
                                public boolean onClick() {
                                    Toast.makeText(ShortcutsPopupSamples.this,
                                            stopAppActionName + " Action",
                                            Toast.LENGTH_SHORT).show();
                                    return true;
                                }

                                @Override
                                public boolean isEnabled() {
                                    return false;
                                }
                            })
                    .addShortcut(new CarUiShortcutsPopup.ShortcutItem() {
                        @Override
                        public CarUiShortcutsPopup.ItemData data() {
                            return new CarUiShortcutsPopup.ItemData(R.drawable.car_ui_icon_add,
                                    reRunAppActionName);
                        }

                        @Override
                        public boolean onClick() {
                            if (isEnabled()) {
                                Toast.makeText(ShortcutsPopupSamples.this,
                                        reRunAppActionName + " Action",
                                        Toast.LENGTH_LONG).show();
                            }
                            return true;
                        }

                        @Override
                        public boolean isEnabled() {
                            return true;
                        }
                    }).build(ShortcutsPopupSamples.this, v);
            carUiShortcutsPopup.show();
        });

        ViewGroup rootView = findViewById(R.id.rootView);
        rootView.setOnDragListener((v, event) -> {
            float x = event.getX();
            float y = event.getY();
            if (event.getAction() == DragEvent.ACTION_DROP) {
                button.setX(x);
                button.setY(y);
                button.invalidate();
            }
            return true;
        });

        button.setOnLongClickListener(v -> {
            v.startDragAndDrop(null, new View.DragShadowBuilder(v), null, 0);
            return true;
        });

    }
}
