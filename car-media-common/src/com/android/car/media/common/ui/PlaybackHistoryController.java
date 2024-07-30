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

package com.android.car.media.common.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.android.car.apps.common.imaging.ImageViewBinder;
import com.android.car.media.common.MediaItemMetadata;
import com.android.car.media.common.R;
import com.android.car.media.common.playback.PlaybackViewModel;
import com.android.car.media.common.source.MediaModels;
import com.android.car.media.common.source.MediaSource;
import com.android.car.ui.recyclerview.CarUiRecyclerView;
import com.android.car.ui.recyclerview.ContentLimiting;
import com.android.car.ui.recyclerview.ContentLimitingAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Fragment} that implements the playback history experience.
 */
public class PlaybackHistoryController {

    private static final String TAG = PlaybackHistoryController.class.getSimpleName();
    private ViewGroup mContainer;
    private CarUiRecyclerView mRecyclerView;
    private PlaybackCardViewModel mPlaybackCardViewModel;
    private LifecycleOwner mLifecycleOwner;
    private MediaHistoryListAdapter mMediaHistoryListAdapter;
    private int mUxrConfigurationId;
    @LayoutRes
    private int mItemLayout;
    @LayoutRes
    private int mHeaderLayout;

    public PlaybackHistoryController(
            LifecycleOwner lifecycleOwner,
            PlaybackCardViewModel playbackCardViewModel,
            ViewGroup container,
            @LayoutRes int itemResource,
            @LayoutRes int headerResource,
            int uxrConfigurationId) {
        mLifecycleOwner = lifecycleOwner;
        mPlaybackCardViewModel = playbackCardViewModel;
        mMediaHistoryListAdapter = new MediaHistoryListAdapter();
        mContainer = container;
        mItemLayout = itemResource;
        mHeaderLayout = headerResource;
        mUxrConfigurationId = uxrConfigurationId;
    }

    /**
     * Renders the view.
     */
    public void setupView() {
        mRecyclerView = mContainer.findViewById(R.id.history_list);
        if (mRecyclerView == null) {
            return;
        }
        if (mHeaderLayout != Resources.ID_NULL) {
            HeaderAdapter headerAdapter = new HeaderAdapter();
            ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter,
                    mMediaHistoryListAdapter);
            mRecyclerView.setAdapter(concatAdapter);
        } else {
            mRecyclerView.setAdapter(mMediaHistoryListAdapter);
        }
        mPlaybackCardViewModel.getHistoryList().observe(mLifecycleOwner, this::setHistoryListData);
    }

    private void setHistoryListData(List<MediaSource> mData) {
        mMediaHistoryListAdapter.setHistoryList(mData);
    }

    /**
     * The view holder for the active history items.
     */
    public class HistoryItemViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private MediaSource mMediaSource;
        private View mActiveView;
        private View mInactiveView;
        private TextView mMetadataTitleView;
        private TextView mAdditionalInfo;
        private ImageView mAppIcon;
        private ImageView mAlbumArt;
        private TextView mAppTitleInactive;
        private ImageView mAppIconInactive;
        private ImageViewBinder<MediaItemMetadata.ArtworkRef> mAlbumArtBinder;

        HistoryItemViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mActiveView = itemView.findViewById(R.id.history_card_container_active);
            mInactiveView = itemView.findViewById(R.id.history_card_container_inactive);
            mMetadataTitleView = itemView.findViewById(R.id.history_card_title_active);
            mAdditionalInfo = itemView.findViewById(R.id.history_card_subtitle_active);
            mAppIcon = itemView.findViewById(R.id.history_card_app_thumbnail);
            mAlbumArt = itemView.findViewById(R.id.history_card_album_art);
            mAppTitleInactive = itemView.findViewById(R.id.history_card_app_title_inactive);
            mAppIconInactive = itemView.findViewById(R.id.history_item_app_icon_inactive);
            int size = mAlbumArt.getMeasuredWidth();
            mAlbumArtBinder = new ImageViewBinder<>(new Size(size, size), mAlbumArt);
        }

        void bindView(MediaSource mediaSource) {
            mMediaSource = mediaSource;
            MediaModels models = new MediaModels(mContext, mediaSource);
            PlaybackViewModel playbackViewModel = models.getPlaybackViewModel();
            playbackViewModel.getMetadata().observe(mLifecycleOwner, this::updateView);
            playbackViewModel.getPlaybackController().observe(mLifecycleOwner,
                    this::setClickAction);
        }

        private void updateView(MediaItemMetadata mediaItemMetadata) {
            if (mediaItemMetadata == null) {
                PlaybackCardControllerUtilities.updateTextViewAndVisibility(mAppTitleInactive,
                        mMediaSource.getDisplayName(mContext));
                PlaybackCardControllerUtilities.updateImageViewDrawableAndVisibility(
                        mAppIconInactive, mMediaSource.getIcon());
                mActiveView.setVisibility(View.GONE);
                mInactiveView.setVisibility(View.VISIBLE);
                return;
            }

            PlaybackCardControllerUtilities.updateTextViewAndVisibility(mMetadataTitleView,
                    mediaItemMetadata.getTitle());
            PlaybackCardControllerUtilities.updateTextViewAndVisibility(mAdditionalInfo,
                    mMediaSource.getDisplayName(itemView.getContext()));
            PlaybackCardControllerUtilities.updateImageViewDrawableAndVisibility(mAppIcon,
                    mMediaSource.getIcon());
            mAlbumArtBinder.setImage(itemView.getContext(), mediaItemMetadata.getArtworkKey());
            mActiveView.setVisibility(View.VISIBLE);
            mInactiveView.setVisibility(View.GONE);
        }

        private void setClickAction(PlaybackViewModel.PlaybackController playbackController) {
            if (playbackController != null) {
                itemView.setOnClickListener(v -> playbackController.play());
            } else {
                itemView.setOnClickListener(v -> mContext.startActivity(mMediaSource.getIntent()));
            }
        }

        void onViewAttachedToWindow() {
            if (mAlbumArt != null) {
                mAlbumArtBinder.maybeRestartLoading(itemView.getContext());
            }
        }

        void onViewDetachedFromWindow() {
            if (mAlbumArt != null) {
                mAlbumArtBinder.maybeCancelLoading(itemView.getContext());
            }
        }
    }

    /**
     *
     */
    private class MediaHistoryListAdapter extends
            ContentLimitingAdapter<HistoryItemViewHolder> implements ContentLimiting {

        private List<MediaSource> mHistoryList = new ArrayList<>();

        public void setHistoryList(List<MediaSource> historyList) {
            mHistoryList.clear();
            mHistoryList.addAll(historyList);
            notifyDataSetChanged();
        }

        @Override
        protected HistoryItemViewHolder onCreateViewHolderImpl(@NonNull @NotNull ViewGroup parent,
                int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(mItemLayout, parent, false);
            return new HistoryItemViewHolder(itemView);
        }

        @Override
        protected void onBindViewHolderImpl(HistoryItemViewHolder holder, int position) {
            holder.bindView(mHistoryList.get(position));
        }

        @Override
        protected void onViewAttachedToWindowImpl(@NonNull HistoryItemViewHolder vh) {
            super.onViewAttachedToWindowImpl(vh);
            HistoryItemViewHolder holder = vh;
            holder.onViewAttachedToWindow();
        }

        @Override
        protected void onViewDetachedFromWindowImpl(@NonNull HistoryItemViewHolder vh) {
            super.onViewDetachedFromWindowImpl(vh);
            HistoryItemViewHolder holder = vh;
            holder.onViewDetachedFromWindow();
        }

        @Override
        public int getConfigurationId() {
            return mUxrConfigurationId;
        }

        @Override
        protected int getUnrestrictedItemCount() {
            return mHistoryList.size();
        }
    }


    /**
     * The view holder for the inactive history items.
     */
    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        HeaderViewHolder(View itemView) {
            super(itemView);
        }

        void bindView() {
            // no op.
        }
    }

    private class HeaderAdapter extends RecyclerView.Adapter<HeaderViewHolder> {

        @Override
        public HeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(mHeaderLayout, parent, false);
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(HeaderViewHolder viewHolder, int position) {
            viewHolder.bindView();
        }

        /** Since there is only one item in the header return 1 */
        @Override
        public int getItemCount() {
            return 1;
        }
    }
}
