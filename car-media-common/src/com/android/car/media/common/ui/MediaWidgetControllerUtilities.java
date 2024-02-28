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
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.car.apps.common.util.ViewUtils;
import com.android.car.media.common.CustomPlaybackAction;
import com.android.car.media.common.MediaLinkHandler;
import com.android.car.media.common.playback.PlaybackProgress;
import com.android.car.media.common.playback.PlaybackViewModel;
import com.android.car.media.common.playback.PlaybackViewModel.PlaybackController;
import com.android.car.media.common.playback.PlaybackViewModel.PlaybackStateWrapper;

import java.util.ArrayList;
import java.util.List;

/** Static utility functions used to give base logic to views in {@link MediaWidgetController} */
public final class MediaWidgetControllerUtilities {

    /**
     * Set text on the non-null TextView if the text is non-empty, or sets the
     * defaultText otherwise. Set the TextView VISIBLE if its contents are non-empty
     */
    public static void updateTextViewAndVisibility(@Nullable TextView view, CharSequence text,
            CharSequence defaultText) {
        updateTextViewAndVisibility(view, !TextUtils.isEmpty(text) ? text : defaultText);
    }

    /** Set text on the non-null TextView. Sets the TextView VISIBLE if the text is non-empty */
    public static void updateTextViewAndVisibility(@Nullable TextView view, CharSequence text) {
        if (view == null) {
            return;
        }
        view.setText(text);
        ViewUtils.setVisible(view, !TextUtils.isEmpty(text));
    }

    /** Set the mediaId associated with the {@link MediaLinkHandler} */
    public static void updateMediaLink(@Nullable MediaLinkHandler linkHandler,
            @Nullable String mediaId) {
        if (linkHandler != null) {
            linkHandler.setLinkedMediaId(mediaId);
        }
    }

    /**
     * Set the ImageView Drawable if its non-null and non-current, and set the ImageView to VISIBLE
     * if the Drawable is non-null
     */
    public static void updateImageViewDrawableAndVisibility(@Nullable ImageView view,
            @Nullable Drawable drawable) {
        if (view != null && drawable != null && view.getDrawable() != drawable) {
            view.setImageDrawable(drawable);
        }
        ViewUtils.setVisible(view, drawable != null);
    }

    /**
     * Update currTimeView {@link TextView} with {@link PlaybackProgress#getCurrentTimeText()}.
     * Update maxTimeView {@link TextView} with {@link PlaybackProgress#getMaxTimeText()}.
     * Update mTimeSeparator {@link TextView} dependent on non-null
     * {@link PlaybackProgress#getCurrentTimeText()} & {@link PlaybackProgress#getMaxTimeText()}
     * with an optional String separator to use
     */
    public static void updateProgressTimesAndSeparator(@Nullable TextView currTimeView,
            @Nullable TextView maxTimeView, @Nullable View timeSeparatorView,
            @Nullable CharSequence currTime, @Nullable CharSequence maxTime) {
        // Update currTimeView text
        updateTextViewAndVisibility(currTimeView, currTime);

        // Update maxTimeView text
        updateTextViewAndVisibility(maxTimeView, maxTime);

        // Update timeSeparatorView text
        ViewUtils.setVisible(timeSeparatorView, (timeSeparatorView != null
                && currTimeView != null && maxTimeView != null
                && !TextUtils.isEmpty(currTime) && !TextUtils.isEmpty(maxTime)));
    }

    /**
     * Set enabled and selected state on playButton based on
     * {@link PlaybackStateWrapper#isLoading()} and {@link PlaybackStateWrapper#isPlaying()}
     */
    public static void updatePlayButtonWithPlaybackState(@Nullable View playButton,
            PlaybackStateWrapper playbackState) {
        if (playButton != null) {
            playButton.setEnabled(!playbackState.isLoading());
            playButton.setSelected(playbackState.isPlaying());
        }
    }

    /**
     * Update non-null buttons in actions with skipPrev, skipNext, and custom actions. If
     * hideExtraButtons is true, any unfilled actions in the list will be set to GONE. If
     * defaultButtonDrawable is non-null, any any unfilled actions in the list are assigned this
     * Drawable and set to VISIBLE
     */
    public static void updateActionsWithPlaybackState(Context context, List<ImageButton> actions,
            PlaybackStateWrapper playbackState, @Nullable PlaybackController playbackController,
            @Nullable Drawable skipPrevDrawable, @Nullable Drawable skipNextDrawable,
            boolean hideExtraButtons, @Nullable Drawable defaultButtonDrawable) {
        boolean isSkipPrevEnabled = playbackState.isSkipPreviousEnabled();
        boolean isSkipPrevReserved = playbackState.iSkipPreviousReserved();
        boolean isSkipNextEnabled = playbackState.isSkipNextEnabled();
        boolean isSkipNextReserved = playbackState.isSkipNextReserved();
        List<PlaybackViewModel.RawCustomPlaybackAction> customActions = playbackState
                .getCustomActions();
        List<ImageButton> actionsToFill = new ArrayList<>();
        for (ImageButton button : actions) {
            if (button != null) {
                actionsToFill.add(button);
                if (defaultButtonDrawable != null) {
                    button.setImageDrawable(defaultButtonDrawable);
                }
            }
        }
        // Fill in buttons with previous and next if enabled or reserved
        boolean isSkipPrevHandled = false;
        boolean isSkipNextHandled = false;
        int startingSlotForCustomActions = 0;
        for (ImageButton button : actionsToFill) {
            if (!isSkipPrevHandled) {
                if (isSkipPrevEnabled || isSkipPrevReserved) {
                    if (skipPrevDrawable != null) {
                        button.setImageDrawable(skipPrevDrawable);
                    }
                    ViewUtils.setVisible(button, true);
                    button.setEnabled(isSkipPrevEnabled);
                    button.setOnClickListener(v -> {
                        if (playbackController != null) {
                            playbackController.skipToPrevious();
                        }
                    });
                    isSkipPrevHandled = true;
                    startingSlotForCustomActions++;
                    continue;
                }
                isSkipPrevHandled = true;
            }
            if (!isSkipNextHandled) {
                if (isSkipNextEnabled || isSkipNextReserved) {
                    if (skipNextDrawable != null) {
                        button.setImageDrawable(skipNextDrawable);
                    }
                    ViewUtils.setVisible(button, true);
                    button.setEnabled(isSkipNextEnabled);
                    button.setOnClickListener(v -> {
                        if (playbackController != null) {
                            playbackController.skipToNext();
                        }
                    });
                    startingSlotForCustomActions++;
                }
            }
            break;
        }

        // Fill in remaining buttons (if any) with custom actions
        int i = startingSlotForCustomActions;
        for (PlaybackViewModel.RawCustomPlaybackAction a : customActions) {
            if (i < actionsToFill.size()) {
                CustomPlaybackAction customAction = a.fetchDrawable(context);
                if (customAction != null) {
                    actionsToFill.get(i).setImageDrawable(customAction.mIcon);
                    ViewUtils.setVisible(actionsToFill.get(i), true);
                    actionsToFill.get(i).setOnClickListener(v -> {
                        if (playbackController != null) {
                            playbackController.doCustomAction(
                                    customAction.mAction, customAction.mExtras);
                        }
                    });
                }
                i++;
            } else {
                break;
            }
        }
        // If there are more buttons than actions, hide them
        if (hideExtraButtons) {
            for (int j = i; j < actionsToFill.size(); j++) {
                ViewUtils.setVisible(actionsToFill.get(j), false);
            }
        }
        // If there are more buttons than actions, apply default Drawable
        if (defaultButtonDrawable != null) {
            for (int j = i; j < actionsToFill.size(); j++) {
                actionsToFill.get(j).setImageDrawable(defaultButtonDrawable);
                ViewUtils.setVisible(actionsToFill.get(j), true);
            }
        }
    }
}
