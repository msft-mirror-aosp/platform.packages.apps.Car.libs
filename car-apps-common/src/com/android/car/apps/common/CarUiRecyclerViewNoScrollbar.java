/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.car.apps.common;

import static com.android.car.ui.utils.RotaryConstants.ROTARY_CONTAINER;
import static com.android.car.ui.utils.RotaryConstants.ROTARY_HORIZONTALLY_SCROLLABLE;
import static com.android.car.ui.utils.RotaryConstants.ROTARY_VERTICALLY_SCROLLABLE;
import static com.android.car.ui.utils.ViewUtils.setRotaryScrollEnabled;

import android.annotation.SuppressLint;
import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.car.ui.preference.PreferenceFragment;
import com.android.car.ui.recyclerview.CarUiGridLayoutStyle;
import com.android.car.ui.recyclerview.CarUiLayoutStyle;
import com.android.car.ui.recyclerview.CarUiLinearLayoutStyle;
import com.android.car.ui.recyclerview.CarUiRecyclerView;
import com.android.car.ui.recyclerview.CarUiRecyclerViewContainer;
import com.android.car.ui.recyclerview.CarUiRecyclerViewImpl;
import com.android.car.ui.recyclerview.decorations.grid.GridDividerItemDecoration;
import com.android.car.ui.recyclerview.decorations.linear.LinearDividerItemDecoration;
import com.android.car.ui.utils.CarUxRestrictionsUtil;
import com.android.car.ui.utils.ViewUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * View that is very similar to {@link CarUiRecyclerViewImpl} that holds a {@link RecyclerView} but
 * does not instantiate scrollbars. Interaction with this view is similar to a {@code RecyclerView}
 * as it takes the same adapter and the layout manager.
 */
@SuppressLint("CustomViewStyleable")
public final class CarUiRecyclerViewNoScrollbar extends FrameLayout
        implements CarUiRecyclerView, ViewUtils.LazyLayoutView,
        PreferenceFragment.AndroidxRecyclerViewProvider {

    /**
     * exact copy of {@link RecyclerView#LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE}
     */
    private static final Class<?>[] LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE =
            new Class<?>[]{Context.class, AttributeSet.class, int.class, int.class};

    @Nullable
    private RecyclerView.Adapter<?> mAdapter;
    @NonNull
    private RecyclerView mRecyclerView;

    private final CarUxRestrictionsUtil.OnUxRestrictionsChangedListener mListener =
            new UxRestrictionChangedListener();

    @NonNull
    private final CarUxRestrictionsUtil mCarUxRestrictionsUtil;
    @Nullable
    private GridDividerItemDecoration mDividerItemDecorationGrid;
    @Nullable
    private RecyclerView.ItemDecoration mDividerItemDecorationLinear;
    private int mNumOfColumns;

    // Set to true when when styled attributes are read and initialized.
    private boolean mIsInitialized;
    private boolean mEnableDividers;

    @NonNull
    private final Set<Runnable> mOnLayoutCompletedListeners = new HashSet<>();

    @Nullable
    private CarUiLayoutStyle mLayoutStyle;

    @NonNull
    private final List<OnScrollListener> mScrollListeners = new ArrayList<>();

    @NonNull
    private final RecyclerView.OnScrollListener mOnScrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    for (OnScrollListener listener : mScrollListeners) {
                        listener.onScrollStateChanged(CarUiRecyclerViewNoScrollbar.this,
                                toInternalScrollState(newState));
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    for (OnScrollListener listener : mScrollListeners) {
                        listener.onScrolled(CarUiRecyclerViewNoScrollbar.this, dx, dy);
                    }
                }
            };

    public CarUiRecyclerViewNoScrollbar(@NonNull Context context) {
        this(context, null, 0);
    }

    public CarUiRecyclerViewNoScrollbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarUiRecyclerViewNoScrollbar(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        mCarUxRestrictionsUtil = CarUxRestrictionsUtil.getInstance(context);
        init(context, attrs, defStyle);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return mRecyclerView.canScrollHorizontally(direction);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mRecyclerView.canScrollVertically(direction);
    }

    /**
     * Initialize the state of the recyclerview
     *
     * @param attrs  AttributeSet that came via xml layout files
     */
    private void init(Context context,
            @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(
                attrs,
                com.android.car.ui.R.styleable.CarUiRecyclerView,
                defStyleAttr,
                0);

        @LayoutRes int layout = com.android.car.ui.R.layout.car_ui_recycler_view_no_scrollbar;

        LayoutInflater factory = LayoutInflater.from(context);
        View rootView = factory.inflate(layout, this, true);
        ViewGroup recyclerViewContainer = requireViewById(
            com.android.car.ui.R.id.car_ui_recycler_view);
        if (recyclerViewContainer instanceof CarUiRecyclerViewContainer) {
            // To keep backwards compatibility CarUiRecyclerViewContainer is a FrameLayout
            // that has a RecyclerView at index 0
            mRecyclerView = (RecyclerView) recyclerViewContainer.getChildAt(0);
        } else {
            mRecyclerView = (RecyclerView) recyclerViewContainer;
        }

        boolean rotaryScrollEnabled = a.getBoolean(
            com.android.car.ui.R.styleable.CarUiRecyclerView_rotaryScrollEnabled,
            /* defValue=*/ false);
        int orientation = a.getInt(
            com.android.car.ui.R.styleable.CarUiRecyclerView_android_orientation,
            LinearLayout.VERTICAL);
        initRotaryScroll(mRecyclerView, rotaryScrollEnabled, orientation);

        @CarUiRecyclerViewLayout int carUiRecyclerViewLayout =
                a.getInt(com.android.car.ui.R.styleable.CarUiRecyclerView_layoutStyle,
                    CarUiRecyclerViewLayout.LINEAR);
        mNumOfColumns = a.getInt(com.android.car.ui.R.styleable.CarUiRecyclerView_numOfColumns,
            /* defValue= */ 2);
        mEnableDividers = a.getBoolean(
            com.android.car.ui.R.styleable.CarUiRecyclerView_enableDivider, /* defValue= */ false);

        mDividerItemDecorationLinear = new LinearDividerItemDecoration(
                ContextCompat.getDrawable(context, R.drawable.recyclerview_divider));

        mDividerItemDecorationGrid =
                new GridDividerItemDecoration(
                        ContextCompat.getDrawable(context,
                            com.android.car.ui.R.drawable.car_ui_divider),
                        ContextCompat.getDrawable(context,
                            com.android.car.ui.R.drawable.car_ui_divider),
                        mNumOfColumns);

        mIsInitialized = true;

        // Set to false so the items below the toolbar are visible.
        mRecyclerView.setClipToPadding(true);
        // Check if a layout manager has already been set via XML
        String layoutManagerInXml = a.getString(
            com.android.car.ui.R.styleable.CarUiRecyclerView_layoutManager);
        if (!TextUtils.isEmpty(layoutManagerInXml)) {
            createLayoutManager(context, layoutManagerInXml, attrs, defStyleAttr, 0);
        } else if (carUiRecyclerViewLayout == CarUiRecyclerViewLayout.GRID) {
            setLayoutManager(new GridLayoutManager(getContext(), mNumOfColumns));
        } else {
            // carUiRecyclerViewLayout == CarUiRecyclerViewLayout.LINEAR
            // Also the default case
            setLayoutManager(new LinearLayoutManager(getContext()));
        }

        if (isVerticalFadingEdgeEnabled()) {
            mRecyclerView.setVerticalFadingEdgeEnabled(true);
            mRecyclerView.setFadingEdgeLength(getVerticalFadingEdgeLength());
            setVerticalFadingEdgeEnabled(false);
            setFadingEdgeLength(0);
        }
        if (isHorizontalFadingEdgeEnabled()) {
            mRecyclerView.setHorizontalFadingEdgeEnabled(true);
            mRecyclerView.setFadingEdgeLength(getHorizontalFadingEdgeLength());
            setHorizontalFadingEdgeEnabled(false);
            setFadingEdgeLength(0);
        }

        a.recycle();

        mRecyclerView.setVerticalScrollBarEnabled(false);
        mRecyclerView.setHorizontalScrollBarEnabled(false);
    }

    @Override
    public void setLayoutManager(@Nullable RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof GridLayoutManager) {
            setLayoutStyle(CarUiGridLayoutStyle.from(layoutManager));
        } else {
            setLayoutStyle(CarUiLinearLayoutStyle.from(layoutManager));
        }
    }

    @Nullable
    public RecyclerView.LayoutManager getLayoutManager() {
        return mRecyclerView.getLayoutManager();
    }

    @Override
    public CarUiLayoutStyle getLayoutStyle() {
        return mLayoutStyle;
    }

    @Override
    public boolean hasFixedSize() {
        return false;
    }

    @Override
    public void setLayoutStyle(CarUiLayoutStyle layoutStyle) {
        mLayoutStyle = layoutStyle;
        if (layoutStyle == null) {
            mRecyclerView.setLayoutManager(null);
            return;
        }

        RecyclerView.LayoutManager layoutManager;
        if (layoutStyle.getLayoutType() == CarUiRecyclerViewLayout.LINEAR) {
            layoutManager = new LinearLayoutManager(getContext(),
                    layoutStyle.getOrientation(),
                    layoutStyle.getReverseLayout()) {
                @Override
                public void onLayoutCompleted(RecyclerView.State state) {
                    super.onLayoutCompleted(state);
                    // Iterate through a copied set instead of the original set because the original
                    // set might be modified during iteration.
                    Set<Runnable> onLayoutCompletedListeners =
                            new HashSet<>(mOnLayoutCompletedListeners);
                    for (Runnable runnable : onLayoutCompletedListeners) {
                        runnable.run();
                    }
                }
            };
        } else {
            layoutManager = new GridLayoutManager(getContext(),
                    layoutStyle.getSpanCount(),
                    layoutStyle.getOrientation(),
                    layoutStyle.getReverseLayout()) {
                @Override
                public void onLayoutCompleted(RecyclerView.State state) {
                    super.onLayoutCompleted(state);
                    // Iterate through a copied set instead of the original set because the original
                    // set might be modified during iteration.
                    Set<Runnable> onLayoutCompletedListeners =
                            new HashSet<>(mOnLayoutCompletedListeners);
                    for (Runnable runnable : onLayoutCompletedListeners) {
                        runnable.run();
                    }
                }
            };
            if (layoutStyle instanceof CarUiGridLayoutStyle) {
                ((GridLayoutManager) layoutManager).setSpanSizeLookup(
                        ((CarUiGridLayoutStyle) layoutStyle).getSpanSizeLookup());
            }
        }

        // Cannot setup item decorations before stylized attributes have been read.
        if (mIsInitialized) {
            addItemDecorations(layoutManager);
        }
        mRecyclerView.setLayoutManager(layoutManager);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void invalidateItemDecorations() {
        mRecyclerView.invalidateItemDecorations();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method will never return true if this view has no items in its adapter. This
     * is fine since an RecyclerView with empty items is not able to restore focus inside it.
     */
    @Override
    public boolean isLayoutCompleted() {
        RecyclerView.Adapter adapter = getAdapter();
        return adapter != null && adapter.getItemCount() > 0 && !mRecyclerView.isComputingLayout();
    }

    @Override
    public void addOnLayoutCompleteListener(@Nullable Runnable runnable) {
        if (runnable != null) {
            mOnLayoutCompletedListeners.add(runnable);
        }
    }

    @Override
    public void removeOnLayoutCompleteListener(@Nullable Runnable runnable) {
        if (runnable != null) {
            mOnLayoutCompletedListeners.remove(runnable);
        }
    }

    @Override
    public RecyclerView.ViewHolder findViewHolderForAdapterPosition(int position) {
        return mRecyclerView.findViewHolderForAdapterPosition(position);
    }

    @Override
    public RecyclerView.ViewHolder findViewHolderForLayoutPosition(int position) {
        return mRecyclerView.findViewHolderForLayoutPosition(position);
    }

    @Override
    public RecyclerView.Adapter<?> getAdapter() {
        return mRecyclerView.getAdapter();
    }

    @Override
    public int getChildLayoutPosition(View child) {
        return mRecyclerView.getChildLayoutPosition(child);
    }

    @Override
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public int getRecyclerViewChildCount() {
        if (mRecyclerView.getLayoutManager() != null) {
            return mRecyclerView.getLayoutManager().getChildCount();
        } else {
            return 0;
        }
    }

    @Override
    public View getRecyclerViewChildAt(int index) {
        if (mRecyclerView.getLayoutManager() != null) {
            return mRecyclerView.getLayoutManager().getChildAt(index);
        } else {
            return null;
        }
    }

    @Override
    public int getRecyclerViewChildPosition(View child) {
        if (mRecyclerView.getLayoutManager() != null) {
            return mRecyclerView.getLayoutManager().getPosition(child);
        } else {
            return -1;
        }
    }

    @Override
    public View findViewByPosition(int position) {
        if (mRecyclerView.getLayoutManager() != null) {
            return mRecyclerView.getLayoutManager().findViewByPosition(position);
        } else {
            return null;
        }
    }

    private static int toInternalScrollState(int state) {
        /* default to RecyclerView.SCROLL_STATE_IDLE */
        int internalState = SCROLL_STATE_IDLE;
        switch (state) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                internalState = SCROLL_STATE_DRAGGING;
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                internalState = SCROLL_STATE_SETTLING;
                break;
        }
        return internalState;
    }

    @Override
    public int getScrollState() {
        return toInternalScrollState(mRecyclerView.getScrollState());
    }

    @Override
    public void addOnScrollListener(OnScrollListener scrollListener) {
        if (mScrollListeners.isEmpty()) {
            mRecyclerView.addOnScrollListener(mOnScrollListener);
        }
        mScrollListeners.add(scrollListener);
    }

    @Override
    public void clearOnChildAttachStateChangeListeners() {
        mRecyclerView.clearOnChildAttachStateChangeListeners();
    }

    @Override
    public void clearOnScrollListeners() {
        mScrollListeners.clear();
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
    }

    @Override
    public void addItemDecoration(
            @NonNull RecyclerView.ItemDecoration decor) {
        mRecyclerView.addItemDecoration(decor);
    }

    @Override
    public void addItemDecoration(
            @NonNull RecyclerView.ItemDecoration decor, int index) {
        mRecyclerView.addItemDecoration(decor, index);
    }

    @Override
    public void addOnChildAttachStateChangeListener(
            RecyclerView.OnChildAttachStateChangeListener listener) {
        mRecyclerView.addOnChildAttachStateChangeListener(listener);
    }

    @NonNull
    @Override
    public RecyclerView.ItemDecoration getItemDecorationAt(int index) {
        return mRecyclerView.getItemDecorationAt(index);
    }

    @Override
    public int getItemDecorationCount() {
        return mRecyclerView.getItemDecorationCount();
    }

    @Override
    public void removeItemDecorationAt(int index) {
        mRecyclerView.removeItemDecorationAt(index);
    }

    @Override
    public void removeOnChildAttachStateChangeListener(
            RecyclerView.OnChildAttachStateChangeListener listener) {
        mRecyclerView.removeOnChildAttachStateChangeListener(listener);
    }

    @Override
    public void removeItemDecoration(
            @NonNull RecyclerView.ItemDecoration decor) {
        mRecyclerView.removeItemDecoration(decor);
    }

    @Override
    public int findFirstCompletelyVisibleItemPosition() {
        return ((LinearLayoutManager) Objects.requireNonNull(mRecyclerView.getLayoutManager()))
                .findFirstCompletelyVisibleItemPosition();
    }

    @Override
    public int findFirstVisibleItemPosition() {
        return ((LinearLayoutManager) Objects.requireNonNull(mRecyclerView.getLayoutManager()))
                .findFirstVisibleItemPosition();
    }

    @Override
    public int findLastCompletelyVisibleItemPosition() {
        return ((LinearLayoutManager) Objects.requireNonNull(mRecyclerView.getLayoutManager()))
                .findLastCompletelyVisibleItemPosition();
    }

    @Override
    public int findLastVisibleItemPosition() {
        return ((LinearLayoutManager) Objects.requireNonNull(mRecyclerView.getLayoutManager()))
                .findLastVisibleItemPosition();
    }

    @Override
    public void setSpanSizeLookup(@NonNull GridLayoutManager.SpanSizeLookup spanSizeLookup) {
        if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
            ((GridLayoutManager) mRecyclerView.getLayoutManager())
                    .setSpanSizeLookup(spanSizeLookup);
        }
    }

    // This method should not be invoked before item decorations are initialized by the #init()
    // method.
    private void addItemDecorations(RecyclerView.LayoutManager layoutManager) {
        // remove existing Item decorations.
        mRecyclerView.removeItemDecoration(Objects.requireNonNull(mDividerItemDecorationGrid));
        mRecyclerView.removeItemDecoration(Objects.requireNonNull(mDividerItemDecorationLinear));

        if (layoutManager instanceof GridLayoutManager) {
            if (mEnableDividers) {
                mRecyclerView.addItemDecoration(
                        Objects.requireNonNull(mDividerItemDecorationGrid));
            }
            setNumOfColumns(((GridLayoutManager) layoutManager).getSpanCount());
        } else {
            if (mEnableDividers) {
                mRecyclerView.addItemDecoration(
                        Objects.requireNonNull(mDividerItemDecorationLinear));
            }
        }
    }

    /**
     * If this view's {@code rotaryScrollEnabled} attribute is set to true, sets the content
     * description so that the {@code RotaryService} will treat it as a scrollable container and
     * initializes this view accordingly.
     */
    private void initRotaryScroll(@NonNull ViewGroup recyclerView,
            boolean rotaryScrollEnabled, int orientation) {
        if (rotaryScrollEnabled) {
            setRotaryScrollEnabled(
                    recyclerView, /* isVertical= */ orientation == LinearLayout.VERTICAL);
        }

        // If rotary scrolling is enabled, set a generic motion event listener to convert
        // SOURCE_ROTARY_ENCODER scroll events into SOURCE_MOUSE scroll events that RecyclerView
        // knows how to handle.
        recyclerView.setOnGenericMotionListener(rotaryScrollEnabled ? (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                if (event.getSource() == InputDevice.SOURCE_ROTARY_ENCODER) {
                    MotionEvent mouseEvent = MotionEvent.obtain(event);
                    mouseEvent.setSource(InputDevice.SOURCE_MOUSE);
                    recyclerView.onGenericMotionEvent(mouseEvent);
                    return true;
                }
            }
            return false;
        } : null);

        // If rotary scrolling is enabled, mark this view as focusable. This view will be focused
        // when no focusable elements are visible.
        recyclerView.setFocusable(rotaryScrollEnabled);

        // Focus this view before descendants so that the RotaryService can focus this view when it
        // wants to.
        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        // Disable the default focus highlight. No highlight should appear when this view is
        // focused.
        recyclerView.setDefaultFocusHighlightEnabled(false);

        // This recyclerView is a rotary container if it's not a scrollable container.
        if (!rotaryScrollEnabled) {
            recyclerView.setContentDescription(ROTARY_CONTAINER);
        }
    }

    @Override
    public void requestLayout() {
        super.requestLayout();

        if (mIsInitialized) {
            Parcelable recyclerViewState = null;
            if (mRecyclerView.getLayoutManager() != null) {
                recyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
            }
            mRecyclerView.requestLayout();
            if (mRecyclerView.getLayoutManager() != null && recyclerViewState != null) {
                mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            }
        }
    }

    @Override
    public void removeOnScrollListener(OnScrollListener scrollListener) {
        mScrollListeners.remove(scrollListener);
        if (mScrollListeners.isEmpty()) {
            mRecyclerView.removeOnScrollListener(mOnScrollListener);
        }
    }

    /**
     * Sets the number of columns in which grid needs to be divided.
     */
    private void setNumOfColumns(int numberOfColumns) {
        mNumOfColumns = numberOfColumns;
        if (mDividerItemDecorationGrid != null) {
            mDividerItemDecorationGrid.setNumOfColumns(mNumOfColumns);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mCarUxRestrictionsUtil.register(mListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCarUxRestrictionsUtil.unregister(mListener);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        // Check position before padding as clipToPadding breaks findFirstVisibleItemPosition
        // expectation
        int currentPosition = findFirstVisibleItemPosition();
        // This needs to happen before updating the scrollbar padding because it affects the
        // visibility of items.
        mRecyclerView.setPadding(0, top, 0, bottom);
        super.setPadding(left, 0, right, 0);
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        // Check position before padding as clipToPadding breaks findFirstVisibleItemPosition
        // expectation
        int currentPosition = findFirstVisibleItemPosition();
        // This needs to happen before updating the scrollbar padding because it affects the
        // visibility of items.
        mRecyclerView.setPaddingRelative(0, top, 0, bottom);
        super.setPaddingRelative(start, 0, end, 0);
    }

    @Override
    public int getPaddingTop() {
        return mRecyclerView.getPaddingTop();
    }

    @Override
    public int getPaddingBottom() {
        return mRecyclerView.getPaddingBottom();
    }

    @Override
    public void smoothScrollBy(int dx, int dy) {
        mRecyclerView.smoothScrollBy(dx, dy);
    }

    @Override
    public void smoothScrollToPosition(int position) {
        mRecyclerView.smoothScrollToPosition(position);
    }

    @Override
    public boolean post(Runnable runnable) {
        return mRecyclerView.post(runnable);
    }

    @Override
    public void scrollToPosition(int position) {
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    public void scrollToPositionWithOffset(int position, int offset) {
        ((LinearLayoutManager) Objects.requireNonNull(mRecyclerView.getLayoutManager()))
                .scrollToPositionWithOffset(position, offset);
    }

    @Override
    public void scrollBy(int x, int y) {
        mRecyclerView.scrollBy(x, y);
    }

    @Override
    public void setContentDescription(CharSequence contentDescription) {
        boolean rotaryScrollEnabled = contentDescription != null
                && (ROTARY_HORIZONTALLY_SCROLLABLE.contentEquals(contentDescription)
                || ROTARY_VERTICALLY_SCROLLABLE.contentEquals(contentDescription));
        int orientation = getLayoutStyle() == null ? LinearLayout.VERTICAL
                : getLayoutStyle().getOrientation();
        initRotaryScroll(mRecyclerView, rotaryScrollEnabled, orientation);
        // Only change this view's content description when not related to rotary scroll. Don't
        // change its content description when related to rotary scroll, because the content
        // description should be set on its inner recyclerview in this case.
        if (!rotaryScrollEnabled) {
            super.setContentDescription(contentDescription);
        }
    }

    @Override
    public void setAdapter(@Nullable RecyclerView.Adapter<?> adapter) {
        if (mAdapter instanceof OnAttachListener) {
            ((OnAttachListener) mAdapter).onDetachedFromCarUiRecyclerView(this);
        }
        mAdapter = adapter;
        mRecyclerView.setAdapter(adapter);
        if (adapter instanceof OnAttachListener) {
            ((OnAttachListener) adapter).onAttachedToCarUiRecyclerView(this);
        }
    }

    @Override
    public void setItemAnimator(RecyclerView.ItemAnimator itemAnimator) {
        mRecyclerView.setItemAnimator(itemAnimator);
    }

    @Override
    public void setHasFixedSize(boolean hasFixedSize) {
        mRecyclerView.setHasFixedSize(hasFixedSize);
    }

    @Override
    public void setOnFlingListener(RecyclerView.OnFlingListener listener) {
        mRecyclerView.setOnFlingListener(listener);
    }

    private OrientationHelper createOrientationHelper() {
        if (mLayoutStyle.getOrientation() == CarUiLayoutStyle.VERTICAL) {
            return OrientationHelper.createVerticalHelper(mRecyclerView.getLayoutManager());
        } else {
            return OrientationHelper.createHorizontalHelper(mRecyclerView.getLayoutManager());
        }
    }

    @Override
    public int getEndAfterPadding() {
        if (mLayoutStyle == null) return 0;
        return createOrientationHelper().getEndAfterPadding();
    }

    @Override
    public int getStartAfterPadding() {
        if (mLayoutStyle == null) return 0;
        return createOrientationHelper().getStartAfterPadding();
    }

    @Override
    public int getTotalSpace() {
        if (mLayoutStyle == null) return 0;
        return createOrientationHelper().getTotalSpace();
    }

    @Override
    public int getDecoratedStart(View child) {
        if (mLayoutStyle == null) return 0;
        return createOrientationHelper().getDecoratedStart(child);
    }

    @Override
    public int getDecoratedEnd(View child) {
        if (mLayoutStyle == null) return 0;
        return createOrientationHelper().getDecoratedEnd(child);
    }

    @Override
    public int getDecoratedMeasuredHeight(View child) {
        if (mRecyclerView.getLayoutManager() != null) {
            return mRecyclerView.getLayoutManager().getDecoratedMeasuredHeight(child);
        } else {
            return 0;
        }
    }

    @Override
    public int getDecoratedMeasuredWidth(View child) {
        if (mRecyclerView.getLayoutManager() != null) {
            return mRecyclerView.getLayoutManager().getDecoratedMeasuredWidth(child);
        } else {
            return 0;
        }
    }

    @Override
    public int getDecoratedMeasurementInOther(View child) {
        if (mLayoutStyle == null) return 0;
        return createOrientationHelper().getDecoratedMeasurementInOther(child);
    }

    @Override
    public int getDecoratedMeasurement(View child) {
        if (mLayoutStyle == null) return 0;
        return createOrientationHelper().getDecoratedMeasurement(child);
    }

    private class UxRestrictionChangedListener implements
            CarUxRestrictionsUtil.OnUxRestrictionsChangedListener {

        @Override
        public void onRestrictionsChanged(@NonNull CarUxRestrictions carUxRestrictions) {
            RecyclerView.Adapter<?> adapter = mRecyclerView.getAdapter();
            // If the adapter does not implement ItemCap, then the max items on it cannot be
            // updated.
            if (!(adapter instanceof ItemCap)) {
                return;
            }

            int maxItems = ItemCap.UNLIMITED;
            if ((carUxRestrictions.getActiveRestrictions()
                    & CarUxRestrictions.UX_RESTRICTIONS_LIMIT_CONTENT)
                    != 0) {
                maxItems = carUxRestrictions.getMaxCumulativeContentItems();
            }

            int originalCount = adapter.getItemCount();
            ((ItemCap) adapter).setMaxItems(maxItems);
            int newCount = adapter.getItemCount();

            if (newCount == originalCount) {
                return;
            }

            if (newCount < originalCount) {
                adapter.notifyItemRangeRemoved(newCount, originalCount - newCount);
            } else {
                adapter.notifyItemRangeInserted(originalCount, newCount - originalCount);
            }
        }
    }

    /**
     * Instantiate and set a LayoutManager, if specified in the attributes. exact copy of
     * {@link RecyclerView#createLayoutManager(Context, String, AttributeSet, int, int)}
     */
    private void createLayoutManager(Context context, String className, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        if (className != null) {
            className = className.trim();
            if (!className.isEmpty()) {
                className = getFullClassName(context, className);
                try {
                    ClassLoader classLoader;
                    if (isInEditMode()) {
                        // Stupid layoutlib cannot handle simple class loaders.
                        classLoader = this.getClass().getClassLoader();
                    } else {
                        classLoader = context.getClassLoader();
                    }
                    Class<? extends RecyclerView.LayoutManager> layoutManagerClass =
                            Class.forName(className, false, classLoader)
                                    .asSubclass(RecyclerView.LayoutManager.class);
                    Constructor<? extends RecyclerView.LayoutManager> constructor;
                    Object[] constructorArgs = null;
                    try {
                        constructor = layoutManagerClass
                                .getConstructor(LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE);
                        constructorArgs = new Object[]{context, attrs, defStyleAttr, defStyleRes};
                    } catch (NoSuchMethodException e) {
                        try {
                            constructor = layoutManagerClass.getConstructor();
                        } catch (NoSuchMethodException e1) {
                            e1.initCause(e);
                            throw new IllegalStateException(attrs.getPositionDescription()
                                    + ": Error creating LayoutManager " + className, e1);
                        }
                    }
                    constructor.setAccessible(true);
                    setLayoutManager(constructor.newInstance(constructorArgs));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Unable to find LayoutManager " + className, e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Could not instantiate the LayoutManager: " + className, e);
                } catch (InstantiationException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Could not instantiate the LayoutManager: " + className, e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Cannot access non-public constructor " + className, e);
                } catch (ClassCastException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Class is not a LayoutManager " + className, e);
                }
            }
        }
    }

    /**
     * exact copy of {@link RecyclerView#getFullClassName(Context, String)}
     */
    private String getFullClassName(Context context, String className) {
        if (className.charAt(0) == '.') {
            return context.getPackageName() + className;
        }
        if (className.contains(".")) {
            return className;
        }
        return RecyclerView.class.getPackage().getName() + '.' + className;
    }
}
