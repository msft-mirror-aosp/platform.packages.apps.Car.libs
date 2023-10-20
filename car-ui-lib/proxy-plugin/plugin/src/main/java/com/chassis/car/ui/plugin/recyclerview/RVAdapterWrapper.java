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

package com.chassis.car.ui.plugin.recyclerview;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.android.car.ui.plugin.oemapis.recyclerview.AdapterDataObserverOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.AdapterOEMV1;
import com.android.car.ui.plugin.oemapis.recyclerview.AdapterOEMV2;
import com.android.car.ui.plugin.oemapis.recyclerview.RecyclerViewOEMV3;
import com.android.car.ui.plugin.oemapis.recyclerview.ViewHolderOEMV1;

/**
 * Wrapper class that passes the data to car-ui via AdapterOEMV1 interface
 */
public final class RVAdapterWrapper extends Adapter<RVAdapterWrapper.ViewHolderWrapper> {

    @NonNull
    private final AdapterOEMV2 mAdapter;

    @NonNull
    private final AdapterDataObserverOEMV1 mAdapterDataObserver = new AdapterDataObserverOEMV1() {
        @Override
        @SuppressLint("NotifyDataSetChanged")
        public void onChanged() {
            RVAdapterWrapper.super.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            RVAdapterWrapper.super.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount,
                @Nullable Object payload) {
            RVAdapterWrapper.super.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            RVAdapterWrapper.super.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            RVAdapterWrapper.super.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemMoved(int fromPosition, int toPosition) {
            RVAdapterWrapper.super.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onStateRestorationPolicyChanged() {
            RVAdapterWrapper.this.updateStateRestorationPolicy();
        }
    };

    public RVAdapterWrapper(@NonNull AdapterOEMV1<?> adapter) {
        this(from(adapter));
    }

    public RVAdapterWrapper(@NonNull AdapterOEMV2<?> adapter) {
        this.mAdapter = adapter;
        RVAdapterWrapper.super.setHasStableIds(adapter.hasStableIds());
        updateStateRestorationPolicy();
    }

    private void updateStateRestorationPolicy() {
        switch (mAdapter.getStateRestorationPolicyInt()) {
            case 2:
                RVAdapterWrapper.super.setStateRestorationPolicy(
                        RecyclerView.Adapter.StateRestorationPolicy.PREVENT);
                break;
            case 1:
                RVAdapterWrapper.super.setStateRestorationPolicy(
                        RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
                break;
            case 0:
            default:
                RVAdapterWrapper.super.setStateRestorationPolicy(
                        RecyclerView.Adapter.StateRestorationPolicy.ALLOW);
        }
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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mAdapter.onAttachedToRecyclerView(null);
    }

    @Override
    public void onBindViewHolder(ViewHolderWrapper holder, int position) {
        mAdapter.bindViewHolder(holder.getViewHolder(), position);
    }

    @Override
    public ViewHolderWrapper onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderWrapper(mAdapter.createViewHolder(parent, viewType));
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mAdapter.onDetachedFromRecyclerView(null);
    }

    @Override
    public boolean onFailedToRecycleView(ViewHolderWrapper holder) {
        return mAdapter.onFailedToRecycleView(holder.getViewHolder());
    }

    @Override
    public void onViewAttachedToWindow(ViewHolderWrapper holder) {
        mAdapter.onViewAttachedToWindow(holder.getViewHolder());
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolderWrapper holder) {
        mAdapter.onViewDetachedFromWindow(holder.getViewHolder());
    }

    @Override
    public void onViewRecycled(ViewHolderWrapper holder) {
        mAdapter.onViewRecycled(holder.getViewHolder());
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        if (!super.hasObservers()) {
            mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
        }
        super.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        if (!super.hasObservers()) {
            mAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
        }
    }

    /**
     * Holds views for each element in the list.
     */
    public static class ViewHolderWrapper extends RecyclerView.ViewHolder {
        @NonNull
        private ViewHolderOEMV1 mViewHolder;

        ViewHolderWrapper(@NonNull ViewHolderOEMV1 viewHolder) {
            super(viewHolder.getItemView());
            mViewHolder = viewHolder;
        }

        @NonNull
        public ViewHolderOEMV1 getViewHolder() {
            return mViewHolder;
        }
    }

    private static <V extends ViewHolderOEMV1> AdapterOEMV2 from(AdapterOEMV1<V> adapter) {
        return new AdapterOEMV2<V>() {

            @Override
            public int getItemCount() {
                return adapter.getItemCount();
            }

            @Override
            public long getItemId(int position) {
                return adapter.getItemId(position);
            }

            @Override
            public int getItemViewType(int position) {
                return adapter.getItemViewType(position);
            }

            @Override
            public void notifyDataSetChanged() {
                // ignore, not supported.
            }

            @Override
            public void notifyItemRangeChanged(int positionStart, int itemCount) {
                // ignore, not supported.
            }

            @Override
            public void notifyItemRangeChanged(int positionStart, int itemCount,
                    @Nullable Object payload) {
                // ignore, not supported.
            }

            @Override
            public void notifyItemRangeInserted(int positionStart, int itemCount) {
                // ignore, not supported.
            }

            @Override
            public void notifyItemRangeRemoved(int positionStart, int itemCount) {
                // ignore, not supported.
            }

            @Override
            public void notifyItemMoved(int fromPosition, int toPosition) {
                // ignore, not supported.
            }

            @Override
            public void setStateRestorationPolicy(int strategy) {
                // ignore, not supported.
            }

            @Override
            public int getStateRestorationPolicyInt() {
                return adapter.getStateRestorationPolicyInt();
            }

            @Override
            public void onAttachedToRecyclerView(@NonNull RecyclerViewOEMV3 recyclerView) {
                adapter.onAttachedToRecyclerView(null);
            }

            @Override
            public void bindViewHolder(@NonNull V holder, int position) {
                adapter.bindViewHolder(holder, position);
            }

            @NonNull
            @Override
            public V createViewHolder(@NonNull ViewGroup parent, int viewType) {
                return adapter.createViewHolder(parent, viewType);
            }

            @Override
            public void onDetachedFromRecyclerView(@NonNull RecyclerViewOEMV3 recyclerView) {
                adapter.onDetachedFromRecyclerView(null);
            }

            @Override
            public boolean onFailedToRecycleView(@NonNull V holder) {
                return adapter.onFailedToRecycleView(holder);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull V holder) {
                adapter.onViewAttachedToWindow(holder);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull V holder) {
                adapter.onViewDetachedFromWindow(holder);
            };

            @Override
            public void onViewRecycled(@NonNull V holder) {
                adapter.onViewRecycled(holder);
            }

            @Override
            public void registerAdapterDataObserver(@NonNull AdapterDataObserverOEMV1 observer) {
                adapter.registerAdapterDataObserver(observer);
            }

            @Override
            public void unregisterAdapterDataObserver(@NonNull AdapterDataObserverOEMV1 observer) {
                adapter.unregisterAdapterDataObserver(observer);
            }

            @Override
            public boolean hasStableIds() {
                return adapter.hasStableIds();
            }

            @Override
            public void setMaxItems(int maxItems) {
                adapter.setMaxItems(maxItems);
            }
        };
    }
}
