/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.car.ui.toolbar;

import static com.android.car.ui.utils.CarUiUtils.charSequenceToString;

import static java.util.stream.Collectors.toList;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.car.ui.imewidescreen.CarUiImeSearchListItem;
import com.android.car.ui.sharedlibrary.oemapis.toolbar.ToolbarControllerOEMV1;
import com.android.car.ui.toolbar.TabLayout.Tab;
import com.android.car.ui.toolbar.Toolbar.NavButtonMode;
import com.android.car.ui.toolbar.Toolbar.OnBackListener;
import com.android.car.ui.toolbar.Toolbar.OnHeightChangedListener;
import com.android.car.ui.toolbar.Toolbar.OnSearchCompletedListener;
import com.android.car.ui.toolbar.Toolbar.OnSearchListener;
import com.android.car.ui.toolbar.Toolbar.OnTabSelectedListener;
import com.android.car.ui.toolbar.Toolbar.State;
import com.android.car.ui.utils.CarUiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Adapts a {@link com.android.car.ui.sharedlibrary.oemapis.toolbar.ToolbarControllerOEMV1}
 * into a {@link ToolbarController}
 */
@SuppressWarnings("AndroidJdkLibsChecker")
public final class ToolbarControllerAdapterV1 implements ToolbarController {

    private static final String TAG = ToolbarControllerAdapterV1.class.getName();

    private final ToolbarControllerOEMV1 mOemToolbar;
    private final Context mContext;

    private ToolbarAdapterState mAdapterState = new ToolbarAdapterState();
    private final Set<OnTabSelectedListener> mOnTabSelectedListeners = new HashSet<>();
    private final Set<OnBackListener> mOnBackListeners = new HashSet<>();
    private final Set<OnSearchListener> mOnSearchListeners = new HashSet<>();
    private final Set<OnSearchCompletedListener> mOnSearchCompletedListeners = new HashSet<>();
    private final ProgressBarControllerAdapterV1 mProgressBar;
    private String mSearchHint;
    private List<MenuItem> mClientMenuItems = Collections.emptyList();

    public ToolbarControllerAdapterV1(
            @NonNull Context context,
            @NonNull ToolbarControllerOEMV1 oemToolbar) {
        mOemToolbar = oemToolbar;
        mProgressBar = new ProgressBarControllerAdapterV1(mOemToolbar.getProgressBar());
        mContext = context;

        Activity activity = CarUiUtils.getActivity(mContext);

        oemToolbar.setBackListener(() -> {
            boolean handled = false;
            for (OnBackListener listener : mOnBackListeners) {
                handled |= listener.onBack();
            }
            if (!handled && activity != null) {
                activity.onBackPressed();
            }
        });

        oemToolbar.setSearchListener(query -> {
            for (OnSearchListener listener : mOnSearchListeners) {
                listener.onSearch(query);
            }
        });
        oemToolbar.setSearchCompletedListener(() -> {
            for (OnSearchCompletedListener listener : mOnSearchCompletedListeners) {
                listener.onSearchCompleted();
            }
        });
    }

    @Override
    public boolean isTabsInSecondRow() {
        Log.w(TAG, "Unsupported operation isTabsInSecondRow() called, ignoring");
        return false;
    }

    @Override
    public void setTitle(int title) {
        setTitle(mContext.getString(title));
    }

    @Override
    public void setTitle(CharSequence title) {
        update(mAdapterState.copy().setTitle(charSequenceToString(title)).build());
    }

    @Override
    public CharSequence getTitle() {
        return mAdapterState.getTitle();
    }

    @Override
    public void setSubtitle(int title) {
        setSubtitle(mContext.getString(title));
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
        update(mAdapterState.copy().setSubtitle(charSequenceToString(subtitle)).build());
    }

    @Override
    public CharSequence getSubtitle() {
        return mAdapterState.getSubtitle();
    }

    @Override
    public int getTabCount() {
        return mAdapterState.getTabs().size();
    }

    @Override
    public int getTabPosition(Tab tab) {
        List<TabAdapterV1> tabs = mAdapterState.getTabs();
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).getClientTab() == tab) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void addTab(Tab clientTab) {
        ToolbarAdapterState.Builder newStateBuilder = mAdapterState.copy();
        newStateBuilder.addTab(new TabAdapterV1(mContext, clientTab, () -> {
            List<TabAdapterV1> tabs = mAdapterState.getTabs();
            int selectedIndex = -1;
            for (int i = 0; i < tabs.size(); i++) {
                if (tabs.get(i).getClientTab() == clientTab) {
                    selectedIndex = i;
                    break;
                }
            }
            // We need to update selectedIndex in our state, but don't need to call update(),
            // as this change originated in the shared library so it already knows about it.
            mAdapterState = mAdapterState.copy().setSelectedTab(selectedIndex).build();

            for (OnTabSelectedListener listener : mOnTabSelectedListeners) {
                listener.onTabSelected(clientTab);
            }
        }));
        if (mAdapterState.getSelectedTab() < 0) {
            newStateBuilder.setSelectedTab(0);
        }
        update(newStateBuilder.build());
    }

    @Override
    public void clearAllTabs() {
        update(mAdapterState.copy()
                .setTabs(Collections.emptyList())
                .setSelectedTab(-1)
                .build());
    }

    @Override
    public Tab getTab(int position) {
        List<TabAdapterV1> tabs = mAdapterState.getTabs();
        if (position < 0 || position >= tabs.size()) {
            throw new IllegalArgumentException("Tab position is invalid: " + position);
        }
        TabAdapterV1 tab = tabs.get(position);
        return tab.getClientTab();
    }

    @Override
    public void selectTab(int position) {
        if (position < 0 || position >= mAdapterState.getTabs().size()) {
            throw new IllegalArgumentException("Tab position is invalid: " + position);
        }
        update(mAdapterState.copy().setSelectedTab(position).build());
    }

    @Override
    public void setShowTabsInSubpage(boolean showTabs) {
        update(mAdapterState.copy().setShowTabsInSubpage(showTabs).build());
    }

    @Override
    public boolean getShowTabsInSubpage() {
        return mAdapterState.getShowTabsInSubpage();
    }

    @Override
    public void setLogo(@IdRes int resId) {
        if (resId == 0) {
            setLogo(null);
        } else {
            setLogo(mContext.getDrawable(resId));
        }
    }

    @Override
    public void setLogo(Drawable drawable) {
        update(mAdapterState.copy().setLogo(drawable).build());
    }

    @Override
    public void setSearchHint(int resId) {
        setSearchHint(mContext.getString(resId));
    }

    @Override
    public void setSearchHint(CharSequence hint) {
        mSearchHint = charSequenceToString(hint);
        mOemToolbar.setSearchHint(mSearchHint);
    }

    @Override
    public CharSequence getSearchHint() {
        return mSearchHint;
    }

    @Override
    public void setSearchIcon(int resId) {
        setSearchIcon(mContext.getDrawable(resId));
    }

    @Override
    public void setSearchIcon(Drawable d) {
        mOemToolbar.setSearchIcon(d);
    }

    @Override
    public void setNavButtonMode(NavButtonMode style) {
        update(mAdapterState.copy().setNavButtonMode(style).build());
    }

    @Override
    public NavButtonMode getNavButtonMode() {
        return mAdapterState.getNavButtonMode();
    }

    @Override
    public void setBackgroundShown(boolean shown) {
        Log.w(TAG, "Unsupported operation setBackgroundShown() called, ignoring");
    }

    @Override
    public boolean getBackgroundShown() {
        return true;
    }

    @Override
    public void setMenuItems(@Nullable List<MenuItem> items) {
        mClientMenuItems = items;
        update(mAdapterState.copy()
                .setMenuItems(convertList(items, MenuItemAdapterV1::new))
                .build());
    }

    @Override
    public List<MenuItem> setMenuItems(int resId) {
        List<MenuItem> menuItems = MenuItemXmlParserUtil.readMenuItemList(mContext, resId);
        setMenuItems(menuItems);
        return menuItems;
    }

    @NonNull
    @Override
    public List<MenuItem> getMenuItems() {
        return mClientMenuItems;
    }

    @Nullable
    @Override
    public MenuItem findMenuItemById(int id) {
        for (MenuItem item : getMenuItems()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public MenuItem requireMenuItemById(int id) {
        MenuItem result = findMenuItemById(id);

        if (result == null) {
            throw new IllegalArgumentException("ID does not reference a MenuItem on this Toolbar");
        }

        return result;
    }

    @Override
    public void setShowMenuItemsWhileSearching(boolean showMenuItems) {
        update(mAdapterState.copy().setShowMenuItemsWhileSearching(showMenuItems).build());
    }

    @Override
    public boolean getShowMenuItemsWhileSearching() {
        return mAdapterState.getShowMenuItemsWhileSearching();
    }

    @Override
    public void setSearchQuery(String query) {
        mOemToolbar.setSearchQuery(query);
    }

    @Override
    public void setState(State state) {
        update(mAdapterState.copy().setState(state).build());
    }

    /**
     * This method takes a new {@link ToolbarAdapterState} and compares it to the current
     * {@link #mAdapterState}. It then sends any differences it detects to the shared library
     * toolbar.
     *
     * This is also the core of the logic that adapts from the client's toolbar interface to
     * the OEM apis toolbar interface. For example, when you are in the HOME state and add tabs,
     * it will call setTitle(null) on the shared library toolbar. This is because the client
     * interface
     */
    private void update(ToolbarAdapterState newAdapterState) {
        ToolbarAdapterState oldAdapterState = mAdapterState;
        mAdapterState = newAdapterState;

        boolean gainingTitleOrSubtitle = newAdapterState.hasTitleOrSubtitle()
                && !oldAdapterState.hasTitleOrSubtitle();
        boolean losingTitleOrSubtitle = !newAdapterState.hasTitleOrSubtitle()
                && oldAdapterState.hasTitleOrSubtitle();
        boolean gainingLogo = newAdapterState.hasLogo() && !oldAdapterState.hasLogo();
        boolean losingLogo = !newAdapterState.hasLogo() && oldAdapterState.hasLogo();

        if (gainingTitleOrSubtitle) {
            mOemToolbar.setTitle(newAdapterState.getTitle());
            mOemToolbar.setSubtitle(newAdapterState.getSubtitle());
        } else if (losingTitleOrSubtitle) {
            mOemToolbar.setTitle(null);
            mOemToolbar.setSubtitle(null);
        } else if (newAdapterState.hasTitleOrSubtitle()) {
            if (!TextUtils.equals(newAdapterState.getTitle(), oldAdapterState.getTitle())) {
                mOemToolbar.setTitle(newAdapterState.getTitle());
            }
            if (!TextUtils.equals(newAdapterState.getSubtitle(), oldAdapterState.getSubtitle())) {
                mOemToolbar.setSubtitle(newAdapterState.getSubtitle());
            }
        }

        if (gainingLogo) {
            mOemToolbar.setLogo(newAdapterState.getLogo());
        } else if (losingLogo) {
            mOemToolbar.setLogo(null);
        } else if (newAdapterState.hasLogo() && newAdapterState.getLogoDirty()) {
            mOemToolbar.setLogo(newAdapterState.getLogo());
        }

        if (newAdapterState.getState() != oldAdapterState.getState()) {
            switch (newAdapterState.getState()) {
                case SEARCH:
                    mOemToolbar.setSearchMode(ToolbarControllerOEMV1.SEARCH_MODE_SEARCH);
                    break;
                case EDIT:
                    mOemToolbar.setSearchMode(ToolbarControllerOEMV1.SEARCH_MODE_EDIT);
                    break;
                default:
                    mOemToolbar.setSearchMode(ToolbarControllerOEMV1.SEARCH_MODE_DISABLED);
            }
        }

        if (oldAdapterState.hasBackButton() != newAdapterState.hasBackButton()
                || (newAdapterState.hasBackButton()
                && oldAdapterState.getNavButtonMode() != newAdapterState.getNavButtonMode())) {
            if (!newAdapterState.hasBackButton()) {
                mOemToolbar.setNavButtonMode(ToolbarControllerOEMV1.NAV_BUTTON_MODE_DISABLED);
            } else if (newAdapterState.getNavButtonMode() == NavButtonMode.CLOSE) {
                mOemToolbar.setNavButtonMode(ToolbarControllerOEMV1.NAV_BUTTON_MODE_CLOSE);
            } else if (newAdapterState.getNavButtonMode() == NavButtonMode.DOWN) {
                mOemToolbar.setNavButtonMode(ToolbarControllerOEMV1.NAV_BUTTON_MODE_DOWN);
            } else {
                mOemToolbar.setNavButtonMode(ToolbarControllerOEMV1.NAV_BUTTON_MODE_BACK);
            }
        }

        boolean gainingTabs = newAdapterState.hasTabs() && !oldAdapterState.hasTabs();
        boolean losingTabs = !newAdapterState.hasTabs() && oldAdapterState.hasTabs();
        if (gainingTabs) {
            mOemToolbar.setTabs(newAdapterState.getTabs(), newAdapterState.getSelectedTab());
        } else if (losingTabs) {
            mOemToolbar.setTabs(Collections.emptyList(), -1);
        } else if (newAdapterState.hasTabs() && newAdapterState.getTabsDirty()) {
            mOemToolbar.setTabs(newAdapterState.getTabs(), newAdapterState.getSelectedTab());
        } else if (newAdapterState.hasTabs()
                && newAdapterState.getSelectedTab() != oldAdapterState.getSelectedTab()) {
            mOemToolbar.selectTab(newAdapterState.getSelectedTab());
        }

        boolean gainingMenuItem = newAdapterState.hasMenuItems() && !oldAdapterState.hasMenuItems();
        boolean losingMenuItems = !newAdapterState.hasMenuItems() && oldAdapterState.hasMenuItems();
        if (gainingMenuItem) {
            mOemToolbar.setMenuItems(newAdapterState.getShownMenuItems());
        } else if (losingMenuItems) {
            mOemToolbar.setMenuItems(Collections.emptyList());
        } else if (newAdapterState.hasMenuItems() && !Objects.equals(
                newAdapterState.getShownMenuItems(), oldAdapterState.getShownMenuItems())) {
            mOemToolbar.setMenuItems(newAdapterState.getShownMenuItems());
        }
    }

    @Override
    public State getState() {
        return mAdapterState.getState();
    }

    @Override
    public void registerToolbarHeightChangeListener(OnHeightChangedListener listener) {
        Log.w(TAG, "Unsupported operation registerToolbarHeightChangeListener() called, ignoring");
    }

    @Override
    public boolean unregisterToolbarHeightChangeListener(OnHeightChangedListener listener) {
        Log.w(TAG,
                "Unsupported operation unregisterToolbarHeightChangeListener() called, ignoring");
        return false;
    }

    @Override
    public void registerOnTabSelectedListener(OnTabSelectedListener listener) {
        mOnTabSelectedListeners.add(listener);
    }

    @Override
    public boolean unregisterOnTabSelectedListener(OnTabSelectedListener listener) {
        return mOnTabSelectedListeners.remove(listener);
    }

    @Override
    public void registerOnSearchListener(OnSearchListener listener) {
        mOnSearchListeners.add(listener);
    }

    @Override
    public boolean unregisterOnSearchListener(OnSearchListener listener) {
        return mOnSearchListeners.remove(listener);
    }

    @Override
    public boolean canShowSearchResultItems() {
        return mOemToolbar.canShowSearchResultItems();
    }

    @Override
    public boolean canShowSearchResultsView() {
        return mOemToolbar.canShowSearchResultsView();
    }

    @Override
    public void setSearchResultsView(View view) {
        mOemToolbar.setSearchResultsView(view);
    }

    @Override
    public void setSearchResultsInputViewIcon(Drawable drawable) {
        mOemToolbar.setSearchResultsInputViewIcon(drawable);
    }

    @Override
    public void setSearchResultItems(List<? extends CarUiImeSearchListItem> searchItems) {
        mOemToolbar.setSearchResultItems(convertList(searchItems, SearchItemAdapterV1::new));
    }

    @Override
    public void registerOnSearchCompletedListener(OnSearchCompletedListener listener) {
        mOnSearchCompletedListeners.add(listener);
    }

    @Override
    public boolean unregisterOnSearchCompletedListener(OnSearchCompletedListener listener) {
        return mOnSearchCompletedListeners.add(listener);
    }

    @Override
    public void registerOnBackListener(OnBackListener listener) {
        mOnBackListeners.add(listener);
    }

    @Override
    public boolean unregisterOnBackListener(OnBackListener listener) {
        return mOnBackListeners.remove(listener);
    }

    @Override
    public ProgressBarController getProgressBar() {
        return mProgressBar;
    }

    /**
     * Given a list of T and a function to convert from T to U, return a list of U.
     *
     * This will create a new list.
     */
    private <T, U> List<U> convertList(List<T> list, Function<T, U> f) {
        if (list == null) {
            return null;
        }

        List<U> result = new ArrayList<>();
        for (T item : list) {
            result.add(f.apply(item));
        }
        return result;
    }

    private static class ToolbarAdapterState {
        private final State mState;
        private final boolean mShowTabsInSubpage;
        @NonNull
        private final List<TabAdapterV1> mTabs;
        @NonNull
        private final List<MenuItemAdapterV1> mMenuItems;
        private final int mSelectedTab;
        private final String mTitle;
        private final String mSubtitle;
        private final Drawable mLogo;
        private final boolean mShowMenuItemsWhileSearching;
        private final boolean mTabsDirty;
        private final boolean mLogoDirty;
        private final NavButtonMode mNavButtonMode;

        ToolbarAdapterState() {
            mState = State.HOME;
            mShowTabsInSubpage = false;
            mTabs = Collections.emptyList();
            mMenuItems = Collections.emptyList();
            mSelectedTab = -1;
            mTitle = null;
            mSubtitle = null;
            mLogo = null;
            mShowMenuItemsWhileSearching = false;
            mTabsDirty = false;
            mLogoDirty = false;
            mNavButtonMode = NavButtonMode.BACK;
        }

        private ToolbarAdapterState(Builder builder) {
            mState = builder.mState;
            mShowTabsInSubpage = builder.mShowTabsInSubpage;
            mTabs = builder.mTabs;
            mMenuItems = builder.mMenuItems;
            mSelectedTab = builder.mSelectedTab;
            mTitle = builder.mTitle;
            mSubtitle = builder.mSubtitle;
            mLogo = builder.mLogo;
            mShowMenuItemsWhileSearching = builder.mShowMenuItemsWhileSearching;
            mTabsDirty = builder.mTabsDirty;
            mLogoDirty = builder.mLogoDirty;
            mNavButtonMode = builder.mNavButtonMode;
        }

        public State getState() {
            return mState;
        }

        public boolean getShowTabsInSubpage() {
            return mShowTabsInSubpage;
        }

        @NonNull
        public List<TabAdapterV1> getTabs() {
            return mTabs;
        }

        public int getSelectedTab() {
            return mSelectedTab;
        }

        @NonNull
        public List<MenuItemAdapterV1> getMenuItems() {
            return mMenuItems;
        }

        public String getTitle() {
            return mTitle == null ? "" : mTitle;
        }

        public String getSubtitle() {
            return mSubtitle;
        }

        public Drawable getLogo() {
            return mLogo;
        }

        public boolean getTabsDirty() {
            return mTabsDirty;
        }

        public boolean getLogoDirty() {
            return mLogoDirty;
        }

        public boolean getShowMenuItemsWhileSearching() {
            return mShowMenuItemsWhileSearching;
        }

        public NavButtonMode getNavButtonMode() {
            return mNavButtonMode;
        }

        private boolean hasLogo() {
            State state = getState();
            return (state == State.HOME || state == State.SUBPAGE) && getLogo() != null;
        }

        private boolean hasTitleOrSubtitle() {
            State state = getState();
            return state == State.HOME || state == State.SUBPAGE;
        }

        private boolean hasTabs() {
            State state = getState();
            return (state == State.HOME || (state == State.SUBPAGE && getShowTabsInSubpage()))
                    && !getTabs().isEmpty();
        }

        private boolean hasMenuItems() {
            return getShownMenuItems().size() > 0;
        }

        private List<MenuItemAdapterV1> getShownMenuItems() {
            State state = getState();
            if (state == State.EDIT) {
                return mShowMenuItemsWhileSearching ? mMenuItems : Collections.emptyList();
            } else if (state == State.SEARCH) {
                return mShowMenuItemsWhileSearching
                        ? mMenuItems.stream().filter(i -> !i.isSearch()).collect(toList())
                        : Collections.emptyList();
            } else {
                return mMenuItems;
            }
        }

        private boolean hasBackButton() {
            return getState() != State.HOME;
        }

        public Builder copy() {
            return new Builder(this);
        }

        public static class Builder {
            private final ToolbarAdapterState mStateClonedFrom;
            private boolean mWasChanged = false;
            private State mState;
            private boolean mShowTabsInSubpage;
            @NonNull
            private List<TabAdapterV1> mTabs;
            @NonNull
            private List<MenuItemAdapterV1> mMenuItems;
            private int mSelectedTab;
            private String mTitle;
            private String mSubtitle;
            private Drawable mLogo;
            private boolean mShowMenuItemsWhileSearching;
            private boolean mTabsDirty = false;
            private boolean mLogoDirty = false;
            private NavButtonMode mNavButtonMode;

            Builder(ToolbarAdapterState state) {
                mStateClonedFrom = state;
                mState = state.getState();
                mShowTabsInSubpage = state.getShowTabsInSubpage();
                mTabs = state.getTabs();
                mMenuItems = state.getMenuItems();
                mShowMenuItemsWhileSearching = state.getShowMenuItemsWhileSearching();
                mSelectedTab = state.getSelectedTab();
                mTitle = state.getTitle();
                mSubtitle = state.getSubtitle();
                mLogo = state.getLogo();
                mNavButtonMode = state.getNavButtonMode();
            }

            public ToolbarAdapterState build() {
                if (!mWasChanged) {
                    return mStateClonedFrom;
                } else {
                    return new ToolbarAdapterState(this);
                }
            }

            public Builder setState(State state) {
                if (state != mState) {
                    mState = state;
                    mWasChanged = true;
                }
                return this;
            }

            public Builder setShowTabsInSubpage(boolean showTabsInSubpage) {
                if (mShowTabsInSubpage != showTabsInSubpage) {
                    mShowTabsInSubpage = showTabsInSubpage;
                    mWasChanged = true;
                }
                return this;
            }

            public Builder setTabs(
                    @NonNull List<TabAdapterV1> tabs) {
                if (!Objects.equals(tabs, mTabs)) {
                    mTabs = Collections.unmodifiableList(tabs);
                    mWasChanged = true;
                    mTabsDirty = true;
                }
                return this;
            }

            public Builder addTab(@NonNull TabAdapterV1 tab) {
                List<TabAdapterV1> newTabs = new ArrayList<>(mTabs);
                newTabs.add(tab);
                mTabs = Collections.unmodifiableList(newTabs);
                mWasChanged = true;
                mTabsDirty = true;
                return this;
            }

            public Builder setSelectedTab(int selectedTab) {
                if (mSelectedTab != selectedTab) {
                    mSelectedTab = selectedTab;
                    mWasChanged = true;
                }
                return this;
            }

            public Builder setTitle(String title) {
                if (!Objects.equals(mTitle, title)) {
                    mTitle = title;
                    mWasChanged = true;
                }
                return this;
            }

            public Builder setSubtitle(String subtitle) {
                if (!Objects.equals(mSubtitle, subtitle)) {
                    mSubtitle = subtitle;
                    mWasChanged = true;
                }
                return this;
            }

            public Builder setLogo(Drawable logo) {
                if (mLogo != logo) {
                    mLogo = logo;
                    mWasChanged = true;
                    mLogoDirty = true;
                }
                return this;
            }

            public Builder setShowMenuItemsWhileSearching(boolean showMenuItemsWhileSearching) {
                if (mShowMenuItemsWhileSearching != showMenuItemsWhileSearching) {
                    mShowMenuItemsWhileSearching = showMenuItemsWhileSearching;
                    mWasChanged = true;
                }
                return this;
            }

            public Builder setMenuItems(List<MenuItemAdapterV1> menuItems) {
                if (menuItems == null) {
                    menuItems = Collections.emptyList();
                }

                if (!Objects.equals(mMenuItems, menuItems)) {
                    mMenuItems = menuItems;
                    mWasChanged = true;
                }
                return this;
            }

            public Builder setNavButtonMode(NavButtonMode newMode) {
                if (newMode != mNavButtonMode) {
                    mNavButtonMode = newMode;
                    mWasChanged = true;
                }
                return this;
            }
        }
    }
}
