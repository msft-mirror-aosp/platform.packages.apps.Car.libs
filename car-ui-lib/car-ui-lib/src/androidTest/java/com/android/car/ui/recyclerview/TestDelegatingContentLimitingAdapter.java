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
package com.android.car.ui.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class TestDelegatingContentLimitingAdapter
        extends RecyclerView.Adapter<TestViewHolder> {

    private final List<String> mItems;

    TestDelegatingContentLimitingAdapter(int numItems) {
        mItems = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            mItems.add("Item " + i);
        }
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext())
                .inflate(com.android.car.ui.test.R.layout.test_car_ui_recycler_view_list_item,
                        parent, false);
        return new TestViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TestViewHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public String getItemText(int i) {
        return "Item " + i;
    }

    static class WithContentLimiting extends TestDelegatingContentLimitingAdapter
            implements DelegatingContentLimitingAdapter.ContentLimiting {

        private int mScrollPositionWhenRestricted = -1;

        WithContentLimiting(int numItems) {
            super(numItems);
        }

        public void setScrollPositionWhenRestricted(int position) {
            mScrollPositionWhenRestricted = position;
        }

        @Override
        public int getScrollToPositionWhenRestricted() {
            return mScrollPositionWhenRestricted;
        }

        @Override
        public int computeAnchorIndexWhenRestricting() {
            return 0;
        }
    }
}
