/*
 * Copyright (C) 2023 The Android Open Source Project
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
package com.android.car.ui.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.android.car.ui.plugin.oemapis.recyclerview.AdapterDataObserverOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.AdapterOEMV2;
import com.android.car.ui.plugin.oemapis.recyclerview.RecyclerViewOEMV3;
import com.android.car.ui.plugin.oemapis.recyclerview.ViewHolderOEMV1;

import java.util.ArrayList;
import java.util.List;

/**
 * AdapterV2 class for delegating calls to AdapterOEMV2
 */
public final class RecyclerViewAdapterAdapterV2
        implements AdapterOEMV2<RecyclerViewAdapterAdapterV2.ViewHolderAdapterV1> {

    @NonNull
    private final Adapter mAdapter;
    @NonNull
    private final Context mAppContext;
    @NonNull
    private List<AdapterDataObserverOEMV1> mAdapterDataObservers = new ArrayList<>();

    private AdapterDataObserver mAdapterDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            for (AdapterDataObserverOEMV1 observer : mAdapterDataObservers) {
                observer.onChanged();
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            for (AdapterDataObserverOEMV1 observer : mAdapterDataObservers) {
                observer.onItemRangeChanged(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount,
                                       @Nullable Object payload) {
            for (AdapterDataObserverOEMV1 observer : mAdapterDataObservers) {
                observer.onItemRangeChanged(positionStart, itemCount, payload);
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            for (AdapterDataObserverOEMV1 observer : mAdapterDataObservers) {
                observer.onItemRangeInserted(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            for (AdapterDataObserverOEMV1 observer : mAdapterDataObservers) {
                observer.onItemRangeRemoved(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            for (AdapterDataObserverOEMV1 observer : mAdapterDataObservers) {
                for (int i = 0; i < itemCount; i++) {
                    observer.onItemMoved(fromPosition + i, toPosition + i);
                }
            }
        }

        @Override
        public void onStateRestorationPolicyChanged() {
            for (AdapterDataObserverOEMV1 observer : mAdapterDataObservers) {
                observer.onStateRestorationPolicyChanged();
            }
        }
    };

    public RecyclerViewAdapterAdapterV2(@NonNull Context appContext,
                                        @NonNull RecyclerView.Adapter<?> adapter) {
        mAppContext = appContext;
        mAdapter = adapter;
    }

    @NonNull
    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyItemRangeChanged(int positionStart, int itemCount) {
        mAdapter.notifyItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        mAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
    }

    @Override
    public void notifyItemRangeInserted(int positionStart, int itemCount) {
        mAdapter.notifyItemRangeInserted(positionStart, itemCount);
    }

    @Override
    public void notifyItemRangeRemoved(int positionStart, int itemCount) {
        mAdapter.notifyItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public void notifyItemMoved(int fromPosition, int toPosition) {
        mAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void setStateRestorationPolicy(int policy) {
        switch (policy) {
            case AdapterOEMV2.PREVENT:
                mAdapter.setStateRestorationPolicy(StateRestorationPolicy.PREVENT);
                break;
            case AdapterOEMV2.PREVENT_WHEN_EMPTY:
                mAdapter.setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
                break;
            case AdapterOEMV2.ALLOW:
            default:
                mAdapter.setStateRestorationPolicy(StateRestorationPolicy.ALLOW);
        }
    }

    @Override
    public int getStateRestorationPolicyInt() {
        switch (mAdapter.getStateRestorationPolicy()) {
            case PREVENT:
                return AdapterOEMV2.PREVENT;
            case PREVENT_WHEN_EMPTY:
                return AdapterOEMV2.PREVENT_WHEN_EMPTY;
            case ALLOW:
            default:
                return AdapterOEMV2.ALLOW;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerViewOEMV3 recyclerView) {
        // TODO: can we return something other than null here?
        mAdapter.onAttachedToRecyclerView(null);
        mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
    }

    @Override
    public void bindViewHolder(ViewHolderAdapterV1 holder, int position) {
        mAdapter.bindViewHolder(holder.getViewHolder(), position);
    }

    @Override
    public ViewHolderAdapterV1 createViewHolder(ViewGroup parent, int viewType) {
        // Return a view created with the app context so that a LayoutInflator created from this
        // view can find resources as expected.
        FrameLayout fakeParent = new FrameLayout(mAppContext);
        return new ViewHolderAdapterV1(mAdapter.createViewHolder(fakeParent, viewType));
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerViewOEMV3 recyclerView) {
        mAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
        // TODO: can we return something other than null here?
        mAdapter.onDetachedFromRecyclerView(null);
    }

    @Override
    public boolean onFailedToRecycleView(ViewHolderAdapterV1 holder) {
        return mAdapter.onFailedToRecycleView(holder.getViewHolder());
    }

    @Override
    public void onViewAttachedToWindow(ViewHolderAdapterV1 holder) {
        mAdapter.onViewAttachedToWindow(holder.getViewHolder());
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolderAdapterV1 holder) {
        mAdapter.onViewDetachedFromWindow(holder.getViewHolder());
    }

    @Override
    public void onViewRecycled(ViewHolderAdapterV1 holder) {
        mAdapter.onViewRecycled(holder.getViewHolder());
    }

    @Override
    public void registerAdapterDataObserver(AdapterDataObserverOEMV1 observer) {
        if (observer == null) {
            return;
        }
        mAdapterDataObservers.add(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(AdapterDataObserverOEMV1 observer) {
        if (observer == null) {
            return;
        }
        mAdapterDataObservers.remove(observer);
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public void setMaxItems(int maxItems) {
        if (mAdapter instanceof CarUiRecyclerView.ItemCap) {
            ((CarUiRecyclerView.ItemCap) mAdapter).setMaxItems(maxItems);
        }
    }

    static class ViewHolderAdapterV1 implements ViewHolderOEMV1 {

        private RecyclerView.ViewHolder mViewHolder;

        ViewHolderAdapterV1(@NonNull RecyclerView.ViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @NonNull
        public ViewHolder getViewHolder() {
            return mViewHolder;
        }

        @Override
        public boolean isRecyclable() {
            return mViewHolder.isRecyclable();
        }

        @NonNull
        @Override
        public View getItemView() {
            return mViewHolder.itemView;
        }
    }
}
