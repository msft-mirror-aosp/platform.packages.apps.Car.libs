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
package com.android.car.ui.plugin.oemapis.recyclerview;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@code androidx.recyclerview.widget.RecyclerView}
 *
 * @deprecated Use {@link RecyclerViewOEMV3} instead
 */
@Deprecated
public interface RecyclerViewOEMV1 {

    /**
     * The RecyclerView is not currently scrolling.
     *
     * @see #getScrollState()
     */
    int SCROLL_STATE_IDLE = 0;

    /**
     * The RecyclerView is currently being dragged by outside input such as user touch input.
     *
     * @see #getScrollState()
     */
    int SCROLL_STATE_DRAGGING = 1;

    /**
     * The RecyclerView is currently animating to a final position while not under
     * outside control.
     *
     * @see #getScrollState()
     */
    int SCROLL_STATE_SETTLING = 2;

    /** {@code RecyclerView#setAdapter(Adapter)} */
    <V extends ViewHolderOEMV1> void setAdapter(@Nullable AdapterOEMV1<V> adapter);

    /** {@code RecyclerView#addOnScrollListener} */
    void addOnScrollListener(@NonNull OnScrollListenerOEMV1 listener);

    /** {@code RecyclerView#removeOnScrollListener} */
    void removeOnScrollListener(@NonNull OnScrollListenerOEMV1 listener);

    /** {@code RecyclerView#clearOnScrollListeners()} */
    void clearOnScrollListeners();

    /** {@code RecyclerView#scrollToPosition(int)} */
    void scrollToPosition(int position);

    /** {@code LinearLayoutManager#scrollToPositionWithOffset(int, int)} */
    void scrollToPositionWithOffset(int position, int offset);

    /** {@code RecyclerView#smoothScrollBy(int, int)} */
    void smoothScrollBy(int dx, int dy);

    /** {@code RecyclerView#smoothScrollToPosition(int)} */
    void smoothScrollToPosition(int position);

    /** {@code RecyclerView#setHasFixedSize(boolean)} */
    void setHasFixedSize(boolean hasFixedSize);

    /** {@code RecyclerView#hasFixedSize()} */
    boolean hasFixedSize();

    /**
     * set {@link LayoutStyleOEMV1}. This is the replacement for
     * {@code androidx.recyclerview.widget.RecyclerView.LayoutManager}
     */
    void setLayoutStyle(@Nullable LayoutStyleOEMV1 layoutStyle);

    /**
     * set {@link LayoutStyleOEMV1}. This is the replacement for
     * {@code androidx.recyclerview.widget.RecyclerView.LayoutManager}
     */
    @Nullable
    LayoutStyleOEMV1 getLayoutStyle();

    /**
     * Returns the view that will be displayed on the screen.
     */
    @NonNull
    View getView();

    /** {@link View#setPadding(int, int, int, int)} */
    void setPadding(int left, int top, int right, int bottom);

    /** {@link View#setPaddingRelative(int, int, int, int)} */
    void setPaddingRelative(int start, int top, int end, int bottom);

    /** {@code androidx.recyclerview.widget.RecyclerView#setClipToPadding(boolean)} */
    void setClipToPadding(boolean clipToPadding);

    /** see {@code LinearLayoutManager#findFirstCompletelyVisibleItemPosition()} */
    int findFirstCompletelyVisibleItemPosition();

    /** see {@code LinearLayoutManager#findFirstVisibleItemPosition()} */
    int findFirstVisibleItemPosition();

    /** see {@code LinearLayoutManager#findLastCompletelyVisibleItemPosition()} */
    int findLastCompletelyVisibleItemPosition();

    /** see {@code LinearLayoutManager#findLastVisibleItemPosition()} */
    int findLastVisibleItemPosition();

    /** see {@code RecyclerView#getScrollState()} */
    int getScrollState();

    /** see {@link View#setContentDescription(CharSequence)} */
    void setContentDescription(@Nullable CharSequence contentDescription);

    /** see {@link View#setAlpha(float)} */
    void setAlpha(float alpha);

    /** see {@code OrientationHelper#getEndAfterPadding()} */
    int getEndAfterPadding();

    /** see {@code OrientationHelper#getStartAfterPadding()} */
    int getStartAfterPadding();

    /** see {@code OrientationHelper#getTotalSpace()} */
    int getTotalSpace();

    /**
     * see {@code LayoutManager#getChildCount()}
     * Prefer this method over {@link android.view.ViewGroup#getChildCount()}
     */
    int getRecyclerViewChildCount();

    /**
     * see {@code LayoutManager#getChildAt(int)}
     * Prefer this method over {@link android.view.ViewGroup#getChildAt(int)}
     */
    View getRecyclerViewChildAt(int index);

    /**
     * see {@code LayoutManager#getPosition(View)}
     */
    int getRecyclerViewChildPosition(@NonNull View child);

    /**
     * see {@code RecyclerView#findViewHolderForAdapterPosition(int)}
     */
    @Nullable
    ViewHolderOEMV1 findViewHolderForAdapterPosition(int position);

    /**
     * see {@code RecyclerView#findViewHolderForLayoutPosition(int)}
     */
    @Nullable
    ViewHolderOEMV1 findViewHolderForLayoutPosition(int position);

    /** {@code RecyclerView#addOnChildAttachStateChangeListener} */
    void addOnChildAttachStateChangeListener(
            @NonNull OnChildAttachStateChangeListenerOEMV1 listener);

    /** {@code RecyclerView#removeOnChildAttachStateChangeListener} */
    void removeOnChildAttachStateChangeListener(
            @NonNull OnChildAttachStateChangeListenerOEMV1 listener);

    /** {@code RecyclerView#clearOnChildAttachStateChangeListener()} */
    void clearOnChildAttachStateChangeListener();

    /** {@code RecyclerView#getChildLayoutPosition} */
    int getChildLayoutPosition(@NonNull View child);

    /** {@code OrientationHelper#getDecoratedStart} */
    int getDecoratedStart(@NonNull View child);

    /** {@code OrientationHelper#getDecoratedEnd} */
    int getDecoratedEnd(@NonNull View child);

    /** {@code LayoutManager#getDecoratedMeasuredHeight} */
    int getDecoratedMeasuredHeight(@NonNull View child);

    /** {@code LayoutManager#getDecoratedMeasuredWidth} */
    int getDecoratedMeasuredWidth(@NonNull View child);

    /** {@code OrientationHelper#getDecoratedMeasurementInOther} */
    int getDecoratedMeasurementInOther(@NonNull View child);

    /** {@code OrientationHelper#getDecoratedMeasurement} */
    int getDecoratedMeasurement(@NonNull View child);

    /** {@code LayoutManager#findViewByPosition} */
    @Nullable
    View findViewByPosition(int position);
}
