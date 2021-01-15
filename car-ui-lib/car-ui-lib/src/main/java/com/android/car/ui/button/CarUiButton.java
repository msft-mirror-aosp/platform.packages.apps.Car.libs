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
package com.android.car.ui.button;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.ui.CarUiLayoutInflaterFactory;
import com.android.car.ui.R;
import com.android.car.ui.sharedlibrarysupport.SharedLibraryFactorySingleton;

import java.util.function.Consumer;

/**
 * This is an interface for a button who's appearance is customizable by the OEM.
 *
 * You can create a new CarUiButton using the {@code <CarUiButton>} tag in your layout file,
 * or, if needed, via the {@link #create(Context, CarUiButtonAttributes)} method. When adding a
 * CarUiButton to the layout file, you can use
 * {@link com.android.car.ui.core.CarUi#requireCarUiComponentById(View, int)}} to get access to it.
 */
public interface CarUiButton {

    /**
     * Creates a CarUiButton.
     *
     * Most of the time, you should prefer creating a CarUiButton with a {@code <CarUiButton>}
     * tag in your layout file. This is only for if you need to create a CarUiButton in java code.
     * The CarUiButton xml tag is enabled by the usage of {@link CarUiLayoutInflaterFactory}.
     *
     * After creating the CarUiButton, you can use {@link #getView()} to get a view that can
     * be added to the view hierarchy. Note that you will not be able to use
     * {@link com.android.car.ui.core.CarUi#requireCarUiComponentById(View, int)} on buttons created
     * this way.
     */
    static CarUiButton create(Context context, @Nullable CarUiButtonAttributes attributes) {
        CarUiButton controller = SharedLibraryFactorySingleton.get(context)
                .createButton(context, attributes);
        controller.getView().setTag(R.id.car_ui_component_reference, controller);
        return controller;
    }

    enum Size {
        SMALL,
        MEDIUM,
        LARGE,
    }

    /**
     * Sets the color scheme of the button.
     *
     * @see CarUiButtonColorScheme
     */
    void setColorScheme(@NonNull CarUiButtonColorScheme scheme);

    /**
     * Sets an icon to display on the button.
     */
    void setIcon(@Nullable Drawable icon);

    /**
     * Sets the title of the button.
     *
     * This should accommodate 20 'W's of width, after which the text may be cut off.
     *
     * @param title The title of the button.
     */
    void setTitle(@Nullable CharSequence title);

    /**
     * Sets the {@link Size} of the button. Your {@link android.view.ViewGroup.LayoutParams} have to
     * accommodate this, so it's recommended to use wrap_content for both width/height. If
     * LayoutParams restrict the button to too small of an area, it may be cut off.
     *
     * This can also change the design of the button to take advantage of the space.
     *
     * By default the size is {@link Size#MEDIUM}.
     *
     * @param size the size of the button.
     */
    void setSize(@NonNull Size size);

    /** Same as {@link View#setEnabled(boolean)} */
    void setEnabled(boolean enabled);

    /**
     * Same as {@link View#setOnClickListener(View.OnClickListener)}, but gives back this
     * ViewController instead of a view.
     */
    void setOnClickListener(@Nullable Consumer<CarUiButton> onClickListener);

    /**
     * Gets the view that corresponds to this ViewController. This should only be used
     * for adding a component created via java code to your hierarchy, do not attempt to change the
     * appearance of the view using any of it's methods.
     *
     * Instead of using this method, prefer relying on {@link CarUiLayoutInflaterFactory}
     * to put the view in your layout automatically.
     */
    View getView();
}
