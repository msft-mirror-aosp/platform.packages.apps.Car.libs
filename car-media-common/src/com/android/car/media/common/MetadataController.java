/*
 * Copyright 2019 The Android Open Source Project
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

package com.android.car.media.common;

import static com.android.car.apps.common.util.LiveDataFunctions.combine;
import static com.android.car.apps.common.util.LiveDataFunctions.pair;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.android.car.apps.common.TappableTextView;
import com.android.car.apps.common.imaging.ImageBinder;
import com.android.car.apps.common.imaging.ImageViewBinder;
import com.android.car.apps.common.imaging.UriArtRef;
import com.android.car.apps.common.util.ViewUtils;
import com.android.car.media.common.browse.MediaItemsRepository;
import com.android.car.media.common.playback.PlaybackViewModel;

/**
 * Common controller for displaying current track's metadata.
 */
public class MetadataController {

    private PlaybackViewModel.PlaybackController mController;
    private final ImageViewBinder<MediaItemMetadata.ArtworkRef> mAlbumArtBinder;
    private final ImageViewBinder<ImageBinder.ImageRef> mLogoBinder;

    private final MediaLinkHandler mSubtitleLinker;
    private final MediaLinkHandler mDescriptionLinker;

    private boolean mTrackingTouch;
    private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Allow to set the progress via the rotary controller. It will do nothing
                    // when the user uses touch screen because no view will be focused in touch
                    // mode.
                    if (mController != null && fromUser && seekBar.isFocused()) {
                        mController.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mTrackingTouch = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (mTrackingTouch && mController != null) {
                        mController.seekTo(seekBar.getProgress());
                    }
                    mTrackingTouch = false;
                }
            };

    /**
     * Create a new MetadataController that operates on the provided Views
     * <p>
     * Note: when the text of a TextView is empty, its visibility will be set to View.INVISIBLE
     * instead of View.GONE. Thus the views stay in the same position, and the constraint chains of
     * the layout won't be disrupted.
     *
     * @param lifecycleOwner    The lifecycle scope for the Views provided to this controller.
     * @param playbackViewModel The Model to provide metadata for display.
     * @param repository        Optional {@link MediaItemsRepository}.
     * @param title             Displays the track's title. Must not be {@code null}.
     * @param subtitle          Displays the track's subtitle. May be {@code null}.
     * @param description       Displays the track's description. May be {@code null}.
     * @param outerSeparator    Displays the separator between the description and the time. May be
     *                          {@code null}.
     * @param currentTime       Displays the track's current position as text. May be {@code null}.
     * @param innerSeparator    Displays the separator between the currentTime and the maxTime. May
     *                          be {@code null}.
     * @param maxTime           Displays the track's duration as text. May be {@code null}.
     * @param seekBar           Displays the track's progress visually. May be {@code null}.
     * @param albumArt          Displays the track's album art. May be {@code null}.
     * @param appIcon           Displays the app icon.
     * @param maxArtSize        Maximum size of the track's album art.
     * @param logoView          Displays the content format logo (if any).  May be {@code null}.
     * @param delegate          Optional {@link MediaLinkHandler.MediaLinkDelegate}.
     */
    public MetadataController(@NonNull LifecycleOwner lifecycleOwner,
            @NonNull PlaybackViewModel playbackViewModel, @Nullable MediaItemsRepository repository,
            @NonNull TextView title, @Nullable TextView subtitle, @Nullable TextView description,
            @Nullable TextView outerSeparator, @Nullable TextView currentTime,
            @Nullable TextView innerSeparator, @Nullable TextView maxTime,
            @Nullable SeekBar seekBar, @Nullable ImageView albumArt, @Nullable ImageView appIcon,
            Size maxArtSize, @Nullable ContentFormatView logoView,
            @Nullable MediaLinkHandler.MediaLinkDelegate delegate) {

        mSubtitleLinker = new MediaLinkHandler(repository, delegate, subtitle);
        mDescriptionLinker = new MediaLinkHandler(repository, delegate, description);

        Context context = title.getContext();
        mAlbumArtBinder = new ImageViewBinder<>(maxArtSize, albumArt);
        mLogoBinder = new ImageViewBinder<>(maxArtSize, logoView);

        playbackViewModel.getPlaybackController().observe(lifecycleOwner,
                controller -> mController = controller);

        playbackViewModel.getMetadata().observe(lifecycleOwner,
                metadata -> {
                    if (metadata == null) {
                        ViewUtils.setVisible(title, false);
                        ViewUtils.setVisible(subtitle, false);
                        ViewUtils.setVisible(description, false);
                        ViewUtils.setVisible(albumArt, false);
                        ViewUtils.setVisible(logoView, false);
                        return;
                    }
                    CharSequence titleName = metadata.getTitle();
                    if (TextUtils.isEmpty(titleName)) {
                        titleName = context.getString(R.string.metadata_default_title);
                    }
                    title.setText(titleName);
                    ViewUtils.setVisible(title, true);

                    if (subtitle != null) {
                        CharSequence subtitleText = metadata.getSubtitle();
                        subtitle.setText(subtitleText);
                        ViewUtils.setVisible(subtitle, !TextUtils.isEmpty(subtitleText));

                        mSubtitleLinker.setLinkedMediaId(metadata.getSubtitleLinkMediaId());
                    }

                    mDescriptionLinker.setLinkedMediaId(metadata.getDescriptionLinkMediaId());

                    ViewUtils.setVisible(albumArt, true);

                    mAlbumArtBinder.setImage(context, metadata.getArtworkKey());

                    if (logoView != null) {
                        Uri logoUri = logoView.prepareToDisplay(metadata);
                        ViewUtils.setVisible(logoView, (logoUri != null));
                        mLogoBinder.setImage(context, new UriArtRef(logoUri));
                    }
                });

        playbackViewModel.getProgress().observe(lifecycleOwner,
                playbackProgress -> {
                    boolean hasTime = playbackProgress.hasTime();
                    ViewUtils.setVisible(currentTime, hasTime);
                    ViewUtils.setVisible(innerSeparator, hasTime);
                    ViewUtils.setVisible(maxTime, hasTime);

                    if (currentTime != null) {
                        currentTime.setText(playbackProgress.getCurrentTimeText());
                    }
                    if (maxTime != null) {
                        maxTime.setText(playbackProgress.getMaxTimeText());
                    }
                    if (seekBar != null) {
                        seekBar.setVisibility(hasTime ? View.VISIBLE : View.INVISIBLE);
                        seekBar.setMax((int) playbackProgress.getMaxProgress());
                        if (!mTrackingTouch) {
                            seekBar.setProgress((int) playbackProgress.getProgress());
                        }
                    }
                });

        if (seekBar != null) {
            playbackViewModel.getPlaybackStateWrapper().observe(lifecycleOwner,
                    state -> {
                        boolean enabled = state != null && state.isSeekToEnabled();
                        mTrackingTouch = false;
                        if (seekBar.getThumb() != null) {
                            seekBar.getThumb().mutate().setAlpha(enabled ? 255 : 0);
                        }
                        final boolean shouldHandleTouch = seekBar.getThumb() != null && enabled;
                        seekBar.setOnTouchListener(
                                (v, event) -> !shouldHandleTouch /* consumeEvent */);
                    });
            seekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
            playbackViewModel.getPlaybackStateWrapper().observe(lifecycleOwner,
                    state -> mTrackingTouch = false);
        }

        if (outerSeparator != null) {
            combine(playbackViewModel.getMetadata(), playbackViewModel.getProgress(),
                    (metadata, progress) -> metadata != null
                            && !TextUtils.isEmpty(metadata.getDescription()) && progress.hasTime())
                    .observe(lifecycleOwner,
                            visible -> ViewUtils.setVisible(outerSeparator, visible));
        }

        if (description != null) {
            pair(playbackViewModel.getMetadata(), playbackViewModel.getProgress()).observe(
                    lifecycleOwner, pair -> {
                        CharSequence descriptionText =
                                pair.first == null ? null : pair.first.getDescription();
                        description.setText(descriptionText);

                        boolean hasDescriptionText = !TextUtils.isEmpty(descriptionText);
                        boolean hasTime = pair.second.hasTime();
                        if (hasDescriptionText) {
                            ViewUtils.setVisible(description, true);
                        } else if (hasTime) {
                            if (description instanceof TappableTextView) {
                                ((TappableTextView) description).hideView(true);
                            } else {
                                // In layout file, subtitle is constrained to description. When
                                // album name is empty but progress is not empty, the visibility of
                                // description should be INVISIBLE instead of GONE, otherwise the
                                // constraint will be broken.
                                ViewUtils.setInvisible(description, true);
                            }
                        } else {
                            ViewUtils.setVisible(description, false);
                        }
                    });
        }

        if (appIcon != null && appIcon.getContext().getResources().getBoolean(
                R.bool.show_playback_source_id)) {
            playbackViewModel.getMediaSource()
                    .observe(lifecycleOwner, mediaSource -> {
                        if (mediaSource != null) {
                            Drawable icon = mediaSource.getIcon();
                            appIcon.setImageDrawable(icon);
                        }
                    });
        }
    }

    /** @deprecated callers should specify the ContentFormatView.  */
    @Deprecated
    public MetadataController(@NonNull LifecycleOwner lifecycleOwner,
            @NonNull PlaybackViewModel playbackViewModel, @Nullable MediaItemsRepository repository,
            @NonNull TextView title, @Nullable TextView subtitle, @Nullable TextView description,
            @Nullable TextView outerSeparator, @Nullable TextView currentTime,
            @Nullable TextView innerSeparator, @Nullable TextView maxTime,
            @Nullable SeekBar seekBar, @Nullable ImageView albumArt, @Nullable ImageView appIcon,
            Size maxArtSize, @Nullable MediaLinkHandler.MediaLinkDelegate delegate) {
        this(lifecycleOwner, playbackViewModel, repository, title, subtitle, description,
                outerSeparator, currentTime, innerSeparator, maxTime, seekBar, albumArt, appIcon,
                maxArtSize, null, delegate);
    }
}
