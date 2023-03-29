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

package com.android.car.ui.paintbooth;

import android.graphics.drawable.GradientDrawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.car.oem.tokens.Token;

import java.util.List;

/**
 * A {@link RecyclerView.Adapter} that can display demo token values.
 */
public class TokenDemoAdapter extends
        RecyclerView.Adapter<TokenDemoAdapter.TokenDemoItemViewHolder> {

    static final int VIEW_TYPE_LIST_COLOR = 1;
    static final int VIEW_TYPE_LIST_TEXT = 2;
    static final int VIEW_TYPE_LIST_SHAPE = 3;


    private final List<Pair<String, Integer>> mItems;

    public TokenDemoAdapter(List<Pair<String, Integer>> items) {
        mItems = items;
    }

    @NonNull
    @Override
    public TokenDemoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.car_ui_recycler_view_list_item, parent, false);
        return new TokenDemoItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TokenDemoItemViewHolder holder, int position) {
        Pair<String, Integer> tokenInfo = mItems.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_LIST_COLOR:
                holder.mText.setText(tokenInfo.first);
                int color = Token.getColor(holder.itemView.getContext(), tokenInfo.second);
                holder.mText.setTextColor(color);
                break;
            case VIEW_TYPE_LIST_TEXT:
                holder.mText.setText(tokenInfo.first);
                int textAppearanceId = Token.getTextAppearance(holder.itemView.getContext(),
                        tokenInfo.second);
                holder.mText.setTextAppearance(textAppearanceId);
                break;
            case VIEW_TYPE_LIST_SHAPE:
                int cornerRadius = Token.getCornerRadius(holder.itemView.getContext(),
                        tokenInfo.second);
                GradientDrawable background = new GradientDrawable();
                background.setColor(
                        holder.itemView.getResources().getColor(android.R.color.holo_blue_dark));
                background.setCornerRadius(cornerRadius);
                holder.mText.setText(tokenInfo.first);
                holder.mText.setBackground(background);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + holder.getItemViewType());
        }
    }

    @Override
    public int getItemViewType(int position) {
        Pair<String, Integer> tokenInfo = mItems.get(position);
        if (tokenInfo.first.contains("color")) {
            return VIEW_TYPE_LIST_COLOR;
        } else if (tokenInfo.first.contains("textAppearance")) {
            return VIEW_TYPE_LIST_TEXT;
        } else if (tokenInfo.first.contains("shapeCorner")) {
            return VIEW_TYPE_LIST_SHAPE;
        }

        throw new IllegalStateException("Unknown view type.");

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class TokenDemoItemViewHolder extends RecyclerView.ViewHolder {
        TextView mText;

        TokenDemoItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.textTitle);
        }
    }
}
