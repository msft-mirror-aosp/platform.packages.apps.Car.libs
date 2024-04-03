/*
 * Copyright 2018 The Android Open Source Project
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

import static androidx.car.app.mediaextensions.analytics.event.AnalyticsEvent.VIEW_COMPONENT_QUEUE_LIST;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import static com.android.car.media.common.ui.PlaybackCardControllerUtilities.updateTextViewAndVisibility;
import static com.android.car.ui.recyclerview.RangeFilter.INVALID_INDEX;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.android.car.apps.common.imaging.ImageViewBinder;
import com.android.car.apps.common.util.ViewUtils;
import com.android.car.media.common.MediaItemMetadata;
import com.android.car.media.common.R;
import com.android.car.media.common.analytics.AnalyticsHelper;
import com.android.car.media.common.browse.MediaItemsRepository;
import com.android.car.media.common.playback.PlaybackViewModel;
import com.android.car.ui.recyclerview.CarUiRecyclerView;
import com.android.car.ui.recyclerview.ContentLimiting;
import com.android.car.ui.recyclerview.ScrollingLimitedViewHolder;
import com.android.car.uxr.LifeCycleObserverUxrContentLimiter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * A {@link Fragment} that implements the playback queue experience. It observes a {@link
 * PlaybackViewModel} and updates its information depending on the currently playing media source
 * through the {@link android.media.session.MediaSession} API.
 */
public class PlaybackQueueController {

    private static final String TAG = "PlaybackQueueController";

    private final LifecycleOwner mLifecycleOwner;
    private final LifeCycleObserverUxrContentLimiter mUxrContentLimiter;
    private final int mUxrConfigurationId;
    private final PlaybackViewModel mPlaybackViewModel;
    private final MediaItemsRepository mMediaItemsRepository;
    private QueueItemsAdapter mQueueAdapter;
    private boolean mIsActuallyVisible = false;
    private List<String> mPrevVisibleItems = new ArrayList<>();
    private final CarUiRecyclerView mQueue;
    @LayoutRes
    private final int mQueueItemLayout;
    private PlaybackQueueCallback mPlaybackQueueCallback;
    private DefaultItemAnimator mItemAnimator;

    private PlaybackViewModel.PlaybackController mController;
    private Long mActiveQueueItemId;

    private boolean mShowTimeForActiveQueueItem;
    private boolean mShowIconForActiveQueueItem;
    private boolean mShowThumbnailForQueueItem;
    private boolean mShowSubtitleForQueueItem;

    /**
     * The callbacks used to communicate the user interactions to the queue fragment listeners.
     */
    public interface PlaybackQueueCallback {

        /**
         * Will be called when a queue item is selected by the user.
         **/
        void onQueueItemClicked(MediaItemMetadata item);
    }

    /**
     * The view holder for the queue items.
     */
    public class QueueViewHolder extends RecyclerView.ViewHolder {

        private final View mView;
        private final ViewGroup mThumbnailContainer;
        private final ImageView mThumbnail;
        private final View mSpacer;
        private final TextView mTitle;
        private final TextView mSubtitle;
        private final TextView mCurrentTime;
        private final TextView mMaxTime;
        private final TextView mTimeSeparator;
        private final ImageView mActiveIcon;

        private final ImageViewBinder<MediaItemMetadata.ArtworkRef> mThumbnailBinder;

        QueueViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mThumbnailContainer = itemView.findViewById(R.id.thumbnail_container);
            mThumbnail = itemView.findViewById(R.id.thumbnail);
            mSpacer = itemView.findViewById(R.id.spacer);
            mTitle = itemView.findViewById(R.id.queue_list_item_title);
            mSubtitle = itemView.findViewById(R.id.queue_list_item_subtitle);
            mCurrentTime = itemView.findViewById(R.id.current_time);
            mMaxTime = itemView.findViewById(R.id.max_time);
            mTimeSeparator = itemView.findViewById(R.id.separator);
            mActiveIcon = itemView.findViewById(R.id.now_playing_icon);

            int max = itemView.getContext().getResources().getInteger(
                    com.android.car.media.common.R.integer.media_items_bitmap_max_size_px);
            Size maxArtSize = new Size(max, max);
            mThumbnailBinder = new ImageViewBinder<>(maxArtSize, mThumbnail);
        }

        void bind(MediaItemMetadata item) {
            mView.setOnClickListener(v -> onQueueItemClicked(item));

            ViewUtils.setVisible(mThumbnailContainer, mShowThumbnailForQueueItem);
            if (mShowThumbnailForQueueItem) {
                Context context = mView.getContext();
                mThumbnailBinder.setImage(context, item != null ? item.getArtworkKey() : null);
            }

            ViewUtils.setVisible(mSpacer, !mShowThumbnailForQueueItem);

            updateTextViewAndVisibility(mTitle, item.getTitle());

            boolean active = mActiveQueueItemId != null && Objects.equals(mActiveQueueItemId,
                    item.getQueueId());
            if (active) {
                if (mCurrentTime != null) {
                    mCurrentTime.setText(mQueueAdapter.getCurrentTime());
                }
                if (mMaxTime != null) {
                    mMaxTime.setText(mQueueAdapter.getMaxTime());
                }
            }
            boolean shouldShowTime =
                    mShowTimeForActiveQueueItem && active && mQueueAdapter.getTimeVisible();
            ViewUtils.setVisible(mCurrentTime, shouldShowTime);
            ViewUtils.setVisible(mMaxTime, shouldShowTime);
            ViewUtils.setVisible(mTimeSeparator, shouldShowTime);

            mView.setSelected(active);

            boolean shouldShowIcon = mShowIconForActiveQueueItem && active;
            ViewUtils.setVisible(mActiveIcon, shouldShowIcon);

            if (mShowSubtitleForQueueItem) {
                updateTextViewAndVisibility(mSubtitle, item.getSubtitle());
            } else {
                ViewUtils.setVisible(mSubtitle, false);
            }
        }

        void onViewAttachedToWindow() {
            if (mShowThumbnailForQueueItem) {
                Context context = mView.getContext();
                mThumbnailBinder.maybeRestartLoading(context);
            }
        }

        void onViewDetachedFromWindow() {
            if (mShowThumbnailForQueueItem) {
                Context context = mView.getContext();
                mThumbnailBinder.maybeCancelLoading(context);
            }
        }
    }

    private class QueueItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements ContentLimiting {

        private static final int CLAMPED_MESSAGE_VIEW_TYPE = -1;
        private static final int QUEUE_ITEM_VIEW_TYPE = 0;

        private UxrPivotFilter mUxrPivotFilter;
        private List<MediaItemMetadata> mQueueItems = Collections.emptyList();
        private String mCurrentTimeText = "";
        private String mMaxTimeText = "";
        /**
         * Index in {@link #mQueueItems}.
         */
        private Integer mActiveItemIndex;
        private boolean mTimeVisible;
        private Integer mScrollingLimitedMessageResId;

        QueueItemsAdapter() {
            mUxrPivotFilter = UxrPivotFilter.PASS_THROUGH;
        }

        @Override
        public void setMaxItems(int maxItems) {
            if (maxItems >= 0) {
                mUxrPivotFilter = new UxrPivotFilterImpl(this, maxItems);
            } else {
                mUxrPivotFilter = UxrPivotFilter.PASS_THROUGH;
            }
            applyFilterToQueue();
        }

        @Override
        public void setScrollingLimitedMessageResId(int resId) {
            if (mScrollingLimitedMessageResId == null || mScrollingLimitedMessageResId != resId) {
                mScrollingLimitedMessageResId = resId;
                mUxrPivotFilter.invalidateMessagePositions();
            }
        }

        @Override
        public int getConfigurationId() {
            return mUxrConfigurationId;
        }

        void setItems(@Nullable List<MediaItemMetadata> items) {
            List<MediaItemMetadata> newQueueItems =
                    new ArrayList<>(items != null ? items : Collections.emptyList());
            if (newQueueItems.equals(mQueueItems)) {
                return;
            }
            mQueueItems = newQueueItems;
            updateActiveItem(/* listIsNew */ true);
        }

        private int getActiveItemIndex() {
            return mActiveItemIndex != null ? mActiveItemIndex : 0;
        }

        private int getQueueSize() {
            return (mQueueItems != null) ? mQueueItems.size() : 0;
        }


        /**
         * Returns the position of the active item if there is one, otherwise returns
         *
         * @link UxrPivotFilter#INVALID_POSITION}.
         */
        private int getActiveItemPosition() {
            if (mActiveItemIndex == null) {
                return UxrPivotFilter.INVALID_POSITION;
            }
            return mUxrPivotFilter.indexToPosition(mActiveItemIndex);
        }

        private void invalidateActiveItemPosition() {
            int position = getActiveItemPosition();
            if (position != UxrPivotFilterImpl.INVALID_POSITION) {
                notifyItemChanged(position);
            }
        }

        private void scrollToActiveItemPosition() {
            int position = getActiveItemPosition();
            if (position != UxrPivotFilterImpl.INVALID_POSITION) {
                mQueue.scrollToPosition(position);
            }
        }

        private void applyFilterToQueue() {
            mUxrPivotFilter.recompute(getQueueSize(), getActiveItemIndex());
            notifyDataSetChanged();
        }

        /**
         * Implements findFirstCompletelyVisibleItemPosition with range filter
         * <p>
         *     Converts position in RV to index in adapter data.
         * </p>
         */
        public int findFirstVisibleItemIndex() {
            int rvPos = mQueue.findFirstCompletelyVisibleItemPosition();
            if (rvPos == RecyclerView.NO_POSITION) return RecyclerView.NO_POSITION;
            if (mUxrPivotFilter.positionToIndex(rvPos) == INVALID_INDEX) rvPos++;
            return mUxrPivotFilter.positionToIndex(rvPos);
        }

        /**
         * Implements findLastCompletelyVisibleItemPosition with range filter
         * <p>
         *     Converts position in RV to index in adapter data.
         * </p>
         */
        public int findLastVisibleItemIndex() {
            int rvPos = mQueue.findLastCompletelyVisibleItemPosition();
            if (rvPos == RecyclerView.NO_POSITION) return RecyclerView.NO_POSITION;
            if (mUxrPivotFilter.positionToIndex(rvPos) == INVALID_INDEX) rvPos--;
            return mUxrPivotFilter.positionToIndex(rvPos);
        }

        // Updates mActiveItemPos, then scrolls the queue to mActiveItemPos.
        // It should be called when the active item (mActiveQueueItemId) changed or
        // the queue items (mQueueItems) changed.
        void updateActiveItem(boolean listIsNew) {
            if (mQueueItems == null || mActiveQueueItemId == null) {
                mActiveItemIndex = null;
                applyFilterToQueue();
                return;
            }
            Integer activeItemPos = null;
            for (int i = 0; i < mQueueItems.size(); i++) {
                if (Objects.equals(mQueueItems.get(i).getQueueId(), mActiveQueueItemId)) {
                    activeItemPos = i;
                    break;
                }
            }

            // Invalidate the previous active item so it gets redrawn as a normal one.
            invalidateActiveItemPosition();

            mActiveItemIndex = activeItemPos;
            if (listIsNew) {
                applyFilterToQueue();
            } else {
                mUxrPivotFilter.updatePivotIndex(getActiveItemIndex());
            }

            scrollToActiveItemPosition();
            invalidateActiveItemPosition();
        }

        void setCurrentTime(String currentTime) {
            if (!mCurrentTimeText.equals(currentTime)) {
                mCurrentTimeText = currentTime;
                invalidateActiveItemPosition();
            }
        }

        void setMaxTime(String maxTime) {
            if (!mMaxTimeText.equals(maxTime)) {
                mMaxTimeText = maxTime;
                invalidateActiveItemPosition();
            }
        }

        void setTimeVisible(boolean visible) {
            if (mTimeVisible != visible) {
                mTimeVisible = visible;
                invalidateActiveItemPosition();
            }
        }

        String getCurrentTime() {
            return mCurrentTimeText;
        }

        String getMaxTime() {
            return mMaxTimeText;
        }

        boolean getTimeVisible() {
            return mTimeVisible;
        }

        @Override
        public final int getItemViewType(int position) {
            if (mUxrPivotFilter.positionToIndex(position) == UxrPivotFilterImpl.INVALID_INDEX) {
                return CLAMPED_MESSAGE_VIEW_TYPE;
            } else {
                return QUEUE_ITEM_VIEW_TYPE;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == CLAMPED_MESSAGE_VIEW_TYPE) {
                return ScrollingLimitedViewHolder.create(parent);
            }
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new QueueViewHolder(inflater.inflate(mQueueItemLayout, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
            if (vh instanceof QueueViewHolder) {
                int index = mUxrPivotFilter.positionToIndex(position);
                if (index != UxrPivotFilterImpl.INVALID_INDEX) {
                    int size = mQueueItems.size();
                    if (0 <= index && index < size) {
                        QueueViewHolder holder = (QueueViewHolder) vh;
                        holder.bind(mQueueItems.get(index));
                    } else {
                        Log.e(TAG, "onBindViewHolder pos: " + position + " gave index: "
                                + index + " out of bounds size: " + size + " "
                                + mUxrPivotFilter.toString());
                    }
                } else {
                    Log.e(TAG, "onBindViewHolder invalid position " + position + " "
                            + mUxrPivotFilter.toString());
                }
            } else if (vh instanceof ScrollingLimitedViewHolder) {
                ScrollingLimitedViewHolder holder = (ScrollingLimitedViewHolder) vh;
                holder.bind(mScrollingLimitedMessageResId);
            } else {
                throw new IllegalArgumentException("unknown holder class " + vh.getClass());
            }
        }

        @Override
        public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder vh) {
            super.onViewAttachedToWindow(vh);
            if (vh instanceof QueueViewHolder) {
                QueueViewHolder holder = (QueueViewHolder) vh;
                holder.onViewAttachedToWindow();
            }
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder vh) {
            super.onViewDetachedFromWindow(vh);
            if (vh instanceof QueueViewHolder) {
                QueueViewHolder holder = (QueueViewHolder) vh;
                holder.onViewDetachedFromWindow();
            }
        }

        @Override
        public int getItemCount() {
            return mUxrPivotFilter.getFilteredCount();
        }

        @Override
        public long getItemId(int position) {
            int index = mUxrPivotFilter.positionToIndex(position);
            if (index != UxrPivotFilterImpl.INVALID_INDEX) {
                return mQueueItems.get(position).getQueueId();
            } else {
                return RecyclerView.NO_ID;
            }
        }
    }

    /**
     * Construct a PlaybackQueueController. If clients do not have a separate layout for the
     * queue, where the queue is already inflated within the container, they should pass
     * {@link Resources.ID_NULL} as the LayoutRes resource. If clients do not require a
     * UxrContentLimiter, they should pass null for uxrContentLimiter and the int passed for
     * uxrConfigurationId will be ignored.
     */
    public PlaybackQueueController(
            ViewGroup container,
            @LayoutRes int resource,
            @LayoutRes int itemResource,
            LifecycleOwner lifecycleOwner,
            PlaybackViewModel playbackViewModel,
            MediaItemsRepository itemsRepository,
            @Nullable LifeCycleObserverUxrContentLimiter uxrContentLimiter,
            int uxrConfigurationId) {
        mLifecycleOwner = lifecycleOwner;
        mPlaybackViewModel = playbackViewModel;
        mMediaItemsRepository = itemsRepository;

        if (resource != Resources.ID_NULL) {
            LayoutInflater inflater = LayoutInflater.from(container.getContext());
            View view = inflater.inflate(resource, container, false);
            container.addView(view);
        }
        mQueueItemLayout = itemResource;

        Resources res = container.getContext().getResources();
        mQueue = container.findViewById(R.id.queue_list);

        mShowTimeForActiveQueueItem = true;
        mShowIconForActiveQueueItem = false;
        mShowThumbnailForQueueItem = true;
        mShowSubtitleForQueueItem = false;

        mPlaybackViewModel.getPlaybackController().observe(lifecycleOwner,
                controller -> mController = controller);
        mUxrConfigurationId = uxrConfigurationId;
        initQueue();

        mUxrContentLimiter = uxrContentLimiter;
        if (mUxrContentLimiter != null) {
            mUxrContentLimiter.setAdapter(mQueueAdapter);
            lifecycleOwner.getLifecycle().addObserver(mUxrContentLimiter);
        }
    }

    public void setShowTimeForActiveQueueItem(boolean show) {
        mShowTimeForActiveQueueItem = show;
    }

    public void setShowIconForActiveQueueItem(boolean show) {
        mShowIconForActiveQueueItem = show;
    }

    public void setShowThumbnailForQueueItem(boolean show) {
        mShowThumbnailForQueueItem = show;
    }

    public void setShowSubtitleForQueueItem(boolean show) {
        mShowSubtitleForQueueItem = show;
    }

    /** Calls {@link RecyclerView#setVerticalFadingEdgeEnabled(boolean)} */
    public void setVerticalFadingEdgeLengthEnabled(boolean enabled) {
        mQueue.setVerticalFadingEdgeEnabled(enabled);
    }

    public void setCallback(PlaybackQueueCallback callback) {
        mPlaybackQueueCallback = callback;
    }

    /**
     * Tells the controller what is actually happening to its view, so that it can be
     * considered hidden right when a hiding animation starts.
     */
    public void onActualVisibilityChanged(boolean isVisible) {
        if (mIsActuallyVisible != isVisible) {
            mIsActuallyVisible = isVisible;
            sendVisibleItemsIncremental(isVisible, false);
        }
    }

    private void sendVisibleItemsIncremental(boolean isShown, boolean fromScroll) {
        if (isShown) {
            int currFirst = mQueueAdapter.findFirstVisibleItemIndex();
            int currLast = mQueueAdapter.findLastVisibleItemIndex();
            mPrevVisibleItems = AnalyticsHelper.sendVisibleItemsInc(VIEW_COMPONENT_QUEUE_LIST,
                    mMediaItemsRepository, null, mPrevVisibleItems, mQueueAdapter.mQueueItems,
                    currFirst, currLast, fromScroll);
        } else {
            mPrevVisibleItems = AnalyticsHelper.sendVisibleItemsInc(VIEW_COMPONENT_QUEUE_LIST,
                    mMediaItemsRepository, null, mPrevVisibleItems, mQueueAdapter.mQueueItems,
                    NO_POSITION, NO_POSITION, false);
        }
    }

    private void initQueue() {
        mQueueAdapter = new QueueItemsAdapter();

        mPlaybackViewModel.getPlaybackStateWrapper().observe(getLifecycleOwner(),
                state -> {
                    Long itemId = (state != null) ? state.getActiveQueueItemId() : null;
                    if (!Objects.equals(mActiveQueueItemId, itemId)) {
                        mActiveQueueItemId = itemId;
                        mQueueAdapter.updateActiveItem(/* listIsNew */ false);
                    }
                });
        mQueue.setAdapter(mQueueAdapter);
        mQueue.addOnScrollListener(new CarUiRecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(CarUiRecyclerView recyclerView, int dx, int dy) {
                //dx and dy are 0 when items in RV change or layout is requested. We should
                // use this to trigger querying what is visible.
                sendVisibleItemsIncremental(true, (dx != 0 || dy != 0));
            }

            @Override
            public void onScrollStateChanged(CarUiRecyclerView recyclerView, int newState) {}
        });
        // Disable item changed animation.
        mItemAnimator = new DefaultItemAnimator();
        mItemAnimator.setSupportsChangeAnimations(false);
        mQueue.setItemAnimator(mItemAnimator);
        mPlaybackViewModel.getQueue().observe(getLifecycleOwner(), this::setQueue);

        mPlaybackViewModel.getProgress().observe(
                getLifecycleOwner(),
                playbackProgress -> {
                    mQueueAdapter.setCurrentTime(playbackProgress.getCurrentTimeText().toString());
                    mQueueAdapter.setMaxTime(playbackProgress.getMaxTimeText().toString());
                    mQueueAdapter.setTimeVisible(playbackProgress.hasTime());
                });
    }

    void setQueue(List<MediaItemMetadata> queueItems) {
        mQueueAdapter.setItems(queueItems);
        if (mIsActuallyVisible) {
            sendVisibleItemsIncremental(/* visible */ true, /* fromScroll */ false);
        }
    }

    private void onQueueItemClicked(MediaItemMetadata item) {
        if (mController != null) {
            mController.skipToQueueItem(item.getQueueId());
        }
        if (mPlaybackQueueCallback != null) {
            mPlaybackQueueCallback.onQueueItemClicked(item);
        }
    }

    private LifecycleOwner getLifecycleOwner() {
        return mLifecycleOwner;
    }
}
