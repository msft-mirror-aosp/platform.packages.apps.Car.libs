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

package com.android.car.ui.recyclerview;

import static com.google.common.truth.Truth.assertThat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.rule.ActivityTestRule;

import com.android.car.ui.test.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ContentLimitingAdapterTest {

    @Rule
    public ActivityTestRule<CarUiRecyclerViewTestActivity> mActivityRule =
            new ActivityTestRule<>(CarUiRecyclerViewTestActivity.class);

    private ContentLimitingAdapter<TestViewHolder> mContentLimitingAdapter;

    @Before
    public void setUp() {
        mContentLimitingAdapter = new TestContentLimitingAdapter(50);
    }

    private static class TestContentLimitingAdapter extends ContentLimitingAdapter<TestViewHolder> {

        private final List<String> mItems;

        TestContentLimitingAdapter(int numItems) {
            mItems = new ArrayList<>();
            for (int i = 0; i < numItems; i++) {
                mItems.add("Item " + i);
            }
        }

        @Override
        protected TestViewHolder onCreateViewHolderImpl(@NonNull ViewGroup parent,
                int viewType) {
            View layout = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.test_car_ui_recycler_view_list_item, parent, false);
            return new TestViewHolder(layout);
        }

        @Override
        protected void onBindViewHolderImpl(TestViewHolder holder, int position) {
            holder.bind(mItems.get(position));
        }

        @Override
        protected int getUnrestrictedItemCount() {
            return mItems.size();
        }

        @Override
        public int getConfigurationId() {
            return 0;
        }
    }

    private static class TestViewHolder extends RecyclerView.ViewHolder {

        private CharSequence mText;

        TestViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(CharSequence text) {
            mText = text;
            TextView textView = itemView.requireViewById(R.id.textTitle);
            textView.setText(text);
        }

        CharSequence getText() {
            return mText;
        }
    }

    @Test
    public void setMaxItem_toLowerThanTotalItems() {
        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(50);
        RecyclerView.ViewHolder last = getItemAtPosition(49);
        isTestViewHolderWithText(last, "Item 49");

        // Switch to limited
        mContentLimitingAdapter.setMaxItems(20);

        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(21);
        RecyclerView.ViewHolder secondToLast = getItemAtPosition(19);
        isTestViewHolderWithText(secondToLast, "Item 19");

        last = getItemAtPosition(20);
        assertThat(last).isInstanceOf(ScrollingLimitedViewHolder.class);

        // Switch back to unlimited
        mContentLimitingAdapter.setMaxItems(-1);

        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(50);
        last = getItemAtPosition(49);
        isTestViewHolderWithText(last, "Item 49");
    }

    @Test
    public void setMaxItem_toOne() {
        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(50);
        RecyclerView.ViewHolder last = getItemAtPosition(49);
        isTestViewHolderWithText(last, "Item 49");

        mContentLimitingAdapter.setMaxItems(1);

        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(2);
        RecyclerView.ViewHolder secondToLast = getItemAtPosition(0);
        isTestViewHolderWithText(secondToLast, "Item 0");

        last = getItemAtPosition(1);
        assertThat(last).isInstanceOf(ScrollingLimitedViewHolder.class);

        // Switch back to unlimited
        mContentLimitingAdapter.setMaxItems(-1);

        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(50);
        last = getItemAtPosition(49);
        isTestViewHolderWithText(last, "Item 49");
    }

    @Test
    public void setMaxItem_toZero() {
        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(50);
        RecyclerView.ViewHolder last = getItemAtPosition(49);
        isTestViewHolderWithText(last, "Item 49");

        mContentLimitingAdapter.setMaxItems(0);

        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(1);
        last = getItemAtPosition(0);
        assertThat(last).isInstanceOf(ScrollingLimitedViewHolder.class);

        // Switch back to unlimited
        mContentLimitingAdapter.setMaxItems(-1);

        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(50);
        last = getItemAtPosition(49);
        isTestViewHolderWithText(last, "Item 49");
    }

    @Test
    public void setMaxItem_toHigherThanTotalItems() {
        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(50);
        RecyclerView.ViewHolder last = getItemAtPosition(49);
        isTestViewHolderWithText(last, "Item 49");

        mContentLimitingAdapter.setMaxItems(70);

        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(50);
        RecyclerView.ViewHolder secondToLast = getItemAtPosition(48);
        isTestViewHolderWithText(secondToLast, "Item 48");

        last = getItemAtPosition(49);
        isTestViewHolderWithText(last, "Item 49");

        // Switch back to unlimited
        mContentLimitingAdapter.setMaxItems(-1);

        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(50);
        last = getItemAtPosition(49);
        isTestViewHolderWithText(last, "Item 49");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setMaxItem_toZeroOnAnEmptyList() {
        mContentLimitingAdapter = new TestContentLimitingAdapter(0);
        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(0);

        mContentLimitingAdapter.setMaxItems(0);

        assertThat(mContentLimitingAdapter.getItemCount()).isEqualTo(0);
        getItemAtPosition(0);
    }

    private RecyclerView.ViewHolder getItemAtPosition(int position) {
        int viewType = mContentLimitingAdapter.getItemViewType(position);
        RecyclerView.ViewHolder viewHolder =
                mContentLimitingAdapter.createViewHolder(
                        new LinearLayout(mActivityRule.getActivity().getApplicationContext()),
                        viewType);
        mContentLimitingAdapter.bindViewHolder(viewHolder, position);
        return viewHolder;
    }

    private void isTestViewHolderWithText(RecyclerView.ViewHolder secondToLast, String s) {
        assertThat(secondToLast).isInstanceOf(TestViewHolder.class);
        TestViewHolder testViewHolder = (TestViewHolder) secondToLast;
        assertThat(testViewHolder.getText()).isEqualTo(s);
    }
}
