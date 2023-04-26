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

package com.chassis.car.ui.plugin;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.ui.CarUiText;
import com.android.car.ui.appstyledview.AppStyledViewControllerImpl;
import com.android.car.ui.plugin.PluginContextWrapper;
import com.android.car.ui.plugin.oemapis.Consumer;
import com.android.car.ui.plugin.oemapis.FocusAreaOEMV1;
import com.android.car.ui.plugin.oemapis.FocusParkingViewOEMV1;
import com.android.car.ui.plugin.oemapis.Function;
import com.android.car.ui.plugin.oemapis.InsetsOEMV1;
import com.android.car.ui.plugin.oemapis.PluginFactoryOEMV6;
import com.android.car.ui.plugin.oemapis.TextOEMV1;
import com.android.car.ui.plugin.oemapis.appstyledview.AppStyledViewControllerOEMV3;
import com.android.car.ui.plugin.oemapis.preference.PreferenceOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.AdapterOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.ContentListItemOEMV2;
import com.android.car.ui.plugin.oemapis.recyclerview.HeaderListItemOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.ListItemOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.RecyclerViewAttributesOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.RecyclerViewOEMV2;
import com.android.car.ui.plugin.oemapis.recyclerview.ViewHolderOEMV1;
import com.android.car.ui.plugin.oemapis.toolbar.ToolbarControllerOEMV2;
import com.android.car.ui.recyclerview.CarUiContentListItem;
import com.android.car.ui.recyclerview.CarUiHeaderListItem;
import com.android.car.ui.recyclerview.CarUiListItem;
import com.android.car.ui.recyclerview.CarUiListItemAdapter;
import com.android.car.ui.recyclerview.CarUiRecyclerViewImpl;
import com.android.car.ui.utils.CarUiUtils;


import com.chassis.car.ui.plugin.appstyledview.AppStyledViewControllerAdapterProxy;
import com.chassis.car.ui.plugin.preference.PreferenceAdapterProxy;
import com.chassis.car.ui.plugin.recyclerview.CarListItemAdapterAdapterProxy;
import com.chassis.car.ui.plugin.recyclerview.RecyclerViewAdapterProxy;
import com.chassis.car.ui.plugin.toolbar.BaseLayoutInstallerProxy;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * An implementation of the plugin factory that delegates back to the car-ui-lib implementation.
 * The main benefit of this is so that customizations can be applied to the car-ui-lib via a RRO
 * without the need to target each app specifically. Note: it only applies to the components that
 * come through the plugin system.
 */
public class PluginFactoryImpl implements PluginFactoryOEMV6 {

    private final Context mPluginContext;
    private WeakReference<Context> mRecentUiContext;
    Map<Context, Context> mAppToPluginContextMap = new WeakHashMap<>();

    public PluginFactoryImpl(Context pluginContext) {
        mPluginContext = pluginContext;
    }

    @Override
    public void setRotaryFactories(
            Function<Context, FocusParkingViewOEMV1> focusParkingViewFactory,
            Function<Context, FocusAreaOEMV1> focusAreaFactory) {
    }

    @Nullable
    @Override
    public ToolbarControllerOEMV2 installBaseLayoutAround(
            @NonNull Context sourceContext,
            @NonNull View contentView,
            @Nullable Consumer<InsetsOEMV1> insetsChangedListener,
            boolean toolbarEnabled,
            boolean fullscreen) {

        Context pluginContext = getPluginUiContext(sourceContext);
        return BaseLayoutInstallerProxy.installBaseLayoutAround(
                pluginContext,
                contentView,
                insetsChangedListener,
                toolbarEnabled,
                fullscreen);
    }

    @Override
    public boolean customizesBaseLayout() {
        return false;
    }

    @Override
    public PreferenceOEMV1 createCarUiPreference(@NonNull Context sourceContext) {
        Context pluginContext = getPluginUiContext(sourceContext);
        return new PreferenceAdapterProxy(pluginContext, sourceContext);
    }

    @Nullable
    @Override
    public AppStyledViewControllerOEMV3 createAppStyledView(@NonNull Context sourceContext) {
        Context pluginContext = getPluginUiContext(sourceContext);
        // build the app styled controller that will be delegated to
        AppStyledViewControllerImpl appStyledViewController = new AppStyledViewControllerImpl(
                pluginContext);
        return new AppStyledViewControllerAdapterProxy(appStyledViewController);
    }

    @Nullable
    @Override
    public RecyclerViewOEMV2 createRecyclerView(@NonNull Context sourceContext,
            @Nullable RecyclerViewAttributesOEMV1 recyclerViewAttributesOEMV1) {
        Context pluginContext = getPluginUiContext(sourceContext);
        CarUiRecyclerViewImpl recyclerView =
                new CarUiRecyclerViewImpl(pluginContext, recyclerViewAttributesOEMV1);
        return new RecyclerViewAdapterProxy(pluginContext, recyclerView,
                recyclerViewAttributesOEMV1);
    }

    @Override
    public AdapterOEMV1<? extends ViewHolderOEMV1> createListItemAdapter(
            List<ListItemOEMV1> items) {
        List<? extends CarUiListItem> staticItems = CarUiUtils.convertList(items,
                PluginFactoryImpl::toStaticListItem);
        // Build the CarUiListItemAdapter that will be delegated to
        CarUiListItemAdapter carUiListItemAdapter = new CarUiListItemAdapter(staticItems);
        return new CarListItemAdapterAdapterProxy(carUiListItemAdapter, mRecentUiContext.get());
    }

    /**
     * The plugin was passed the list items as {@link ListItemOEMV1}s and thus must be converted
     * back to use the "original" {@link CarUiListItem}s that's expected by the
     * {@link CarUiListItemAdapter}
     */
    private static CarUiListItem toStaticListItem(ListItemOEMV1 item) {
        if (item instanceof HeaderListItemOEMV1) {
            HeaderListItemOEMV1 header = (HeaderListItemOEMV1) item;
            return new CarUiHeaderListItem(header.getTitle(), header.getBody());
        } else if (item instanceof ContentListItemOEMV2) {
            ContentListItemOEMV2 contentItem = (ContentListItemOEMV2) item;

            CarUiContentListItem listItem = new CarUiContentListItem(
                    toCarUiContentListItemAction(contentItem.getAction()));

            if (contentItem.getTitle() != null) {
                listItem.setTitle(toCarUiText(contentItem.getTitle()));
            }

            if (contentItem.getBody() != null) {
                listItem.setBody(toCarUiText(contentItem.getBody()));
            }

            listItem.setIcon(contentItem.getIcon());
            listItem.setPrimaryIconType(
                    toCarUiContentListItemIconType(contentItem.getPrimaryIconType()));

            if (contentItem.getAction() == ContentListItemOEMV2.Action.ICON) {
                CarUiContentListItem.OnClickListener listener =
                        contentItem.getSupplementalIconOnClickListener() != null
                                ? carUiContentListItem ->
                                contentItem.getSupplementalIconOnClickListener().accept(
                                        contentItem) : null;


                listItem.setSupplementalIcon(contentItem.getSupplementalIcon(), listener);
            }

            if (contentItem.getOnClickListener() != null) {
                CarUiContentListItem.OnClickListener listener =
                        contentItem.getOnClickListener() != null
                                ? carUiContentListItem ->
                                contentItem.getOnClickListener().accept(
                                        contentItem) : null;
                listItem.setOnItemClickedListener(listener);
            }

            listItem.setOnCheckedChangeListener((carUiContentListItem, checked) ->
                    carUiContentListItem.setChecked(checked));
            listItem.setActionDividerVisible(contentItem.isActionDividerVisible());
            listItem.setEnabled(contentItem.isEnabled());
            listItem.setChecked(contentItem.isChecked());
            listItem.setActivated(contentItem.isActivated());
            listItem.setSecure(contentItem.isSecure());
            return listItem;
        } else {
            throw new IllegalStateException("Unknown view type.");
        }
    }

    private static CarUiText toCarUiText(TextOEMV1 text) {
        return new CarUiText.Builder(text.getTextVariants()).setMaxChars(
                text.getMaxChars()).setMaxLines(text.getMaxLines()).build();
    }

    private static List<CarUiText> toCarUiText(List<TextOEMV1> lines) {
        List<CarUiText> oemLines = new ArrayList<>();

        for (TextOEMV1 line : lines) {
            oemLines.add(new CarUiText.Builder(line.getTextVariants()).setMaxChars(
                    line.getMaxChars()).setMaxLines(line.getMaxLines()).build());
        }
        return oemLines;
    }

    private static CarUiContentListItem.Action toCarUiContentListItemAction(
            ContentListItemOEMV2.Action action) {
        switch (action) {
            case NONE:
                return CarUiContentListItem.Action.NONE;
            case SWITCH:
                return CarUiContentListItem.Action.SWITCH;
            case CHECK_BOX:
                return CarUiContentListItem.Action.CHECK_BOX;
            case RADIO_BUTTON:
                return CarUiContentListItem.Action.RADIO_BUTTON;
            case ICON:
                return CarUiContentListItem.Action.ICON;
            case CHEVRON:
                return CarUiContentListItem.Action.CHEVRON;
            default:
                throw new IllegalStateException("Unexpected list item action type");
        }
    }

    private static CarUiContentListItem.IconType toCarUiContentListItemIconType(
            ContentListItemOEMV2.IconType iconType) {
        switch (iconType) {
            case CONTENT:
                return CarUiContentListItem.IconType.CONTENT;
            case STANDARD:
                return CarUiContentListItem.IconType.STANDARD;
            case AVATAR:
                return CarUiContentListItem.IconType.AVATAR;
            default:
                throw new IllegalStateException("Unexpected list item icon type");
        }
    }

    /**
     * This method tries to return a ui context for usage in the plugin that has the same
     * configuration as the given source ui context.
     *
     * @param sourceContext A ui context, normally an Activity context.
     */
    private Context getPluginUiContext(@Nullable Context sourceContext) {
        Context uiContext = mAppToPluginContextMap.get(sourceContext);

        if (uiContext == null) {
            uiContext = mPluginContext;
            if (VERSION.SDK_INT >= 34 /* Android U */ && !uiContext.isUiContext()) {
                // On U and above we need a UiContext for initializing the proxy plugin.
                uiContext = uiContext
                        .createWindowContext(sourceContext.getDisplay(), TYPE_APPLICATION, null);
            }
        }

        Configuration currentConfiguration = uiContext.getResources().getConfiguration();
        Configuration newConfiguration = sourceContext.getResources().getConfiguration();
        if (currentConfiguration.diff(newConfiguration) != 0) {
            uiContext = uiContext.createConfigurationContext(newConfiguration);
        }

        // Only wrap uiContext the first time it's configured
        if (!(uiContext instanceof PluginContextWrapper)) {
            uiContext = new PluginContextWrapper(uiContext, sourceContext.getPackageName());
        }

        // Add a custom layout inflater that can handle things like CarUiTextView that is in the
        // layout files of the car-ui-lib static implementation
        LayoutInflater inflater = LayoutInflater.from(uiContext);
        if (inflater.getFactory2() == null) {
            inflater.setFactory2(new CarUiProxyLayoutInflaterFactory());
        }

        mAppToPluginContextMap.put(sourceContext, uiContext);

        // Store this uiContext as the most recently used uiContext. This is so that it's possible
        // to obtain a relevant plugin ui context without a source context. This is used with
        // createListItemAdapter, which does not receive a context as a parameter. Note: list items
        // are always used with a RecyclerView, so mRecentUiContext will be set in
        // createRecyclerView method, which should happen before createListItemAdapter.
        mRecentUiContext = new WeakReference<Context>(uiContext);

        return uiContext;
    }
}
