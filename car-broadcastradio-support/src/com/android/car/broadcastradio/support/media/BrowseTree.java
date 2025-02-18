/**
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.car.broadcastradio.support.media;

import android.graphics.Bitmap;
import android.hardware.radio.ProgramList;
import android.hardware.radio.ProgramSelector;
import android.hardware.radio.RadioManager;
import android.hardware.radio.RadioManager.BandDescriptor;
import android.hardware.radio.RadioMetadata;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.MediaBrowserServiceCompat.BrowserRoot;
import androidx.media.MediaBrowserServiceCompat.Result;

import com.android.car.broadcastradio.support.Program;
import com.android.car.broadcastradio.support.R;
import com.android.car.broadcastradio.support.platform.ImageResolver;
import com.android.car.broadcastradio.support.platform.ProgramInfoExt;
import com.android.car.broadcastradio.support.platform.ProgramSelectorExt;
import com.android.car.broadcastradio.support.platform.RadioMetadataExt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of MediaBrowserService logic regarding browser tree.
 */
public class BrowseTree {
    private static final String TAG = "BcRadioApp.BrowseTree";

    /**
     * Used as a long extra field to indicate the Broadcast Radio folder type of the media item.
     * The value should be one of the following:
     * <ul>
     * <li>{@link #BCRADIO_FOLDER_TYPE_PROGRAMS}</li>
     * <li>{@link #BCRADIO_FOLDER_TYPE_FAVORITES}</li>
     * <li>{@link #BCRADIO_FOLDER_TYPE_BAND}</li>
     * </ul>
     *
     * @see android.media.MediaDescription#getExtras()
     */
    public static final String EXTRA_BCRADIO_FOLDER_TYPE =
            "android.media.extra.EXTRA_BCRADIO_FOLDER_TYPE";

    /**
     * The type of folder that contains a list of Broadcast Radio programs available
     * to tune at the moment.
     */
    public static final long BCRADIO_FOLDER_TYPE_PROGRAMS = 1;

    /**
     * The type of folder that contains a list of Broadcast Radio programs added
     * to favorites (not necessarily available to tune at the moment).
     *
     * If this folder has {@link android.media.browse.MediaBrowser.MediaItem#FLAG_PLAYABLE} flag
     * set, it can be used to play some program from the favorite list (selection depends on the
     * radio app implementation).
     */
    public static final long BCRADIO_FOLDER_TYPE_FAVORITES = 2;

    /**
     * The type of folder that contains the list of all Broadcast Radio channels
     * (frequency values valid in the current region) for a given band.
     * Each band (like AM, FM) has its own, separate folder.
     * These lists include all channels, whether or not some program is tunable through it.
     *
     * If this folder has {@link android.media.browse.MediaBrowser.MediaItem#FLAG_PLAYABLE} flag
     * set, it can be used to tune to some channel within a given band (selection depends on the
     * radio app implementation).
     */
    public static final long BCRADIO_FOLDER_TYPE_BAND = 3;

    /**
     * Non-localized name of the band.
     *
     * For now, it can only take one of the following values:
     *  - AM;
     *  - FM;
     *  - DAB;
     *  - SXM.
     *
     * However, in future releases the list might get extended.
     */
    public static final String EXTRA_BCRADIO_BAND_NAME_EN =
            "android.media.extra.EXTRA_BCRADIO_BAND_NAME_EN";

    /**
     * Key for radio metadata parcelable in extra
     */
    public static final String EXTRA_BCRADIO_METADATA =
            "android.media.extra.EXTRA_BCRADIO_METADATA";

    /**
     * General play intent action.
     *
     * MediaBrowserService of the radio app must handle this command to perform general
     * "play" command. It usually means starting playback of recently tuned station.
     */
    public static final String ACTION_PLAY_BROADCASTRADIO =
            "android.car.intent.action.PLAY_BROADCASTRADIO";

    private static final String NODE_ROOT = "root_id";
    public static final String NODE_PROGRAMS = "programs_id";
    public static final String NODE_FAVORITES = "favorites_id";

    private static final String NODEPREFIX_BAND = "band:";
    public static final String NODE_BAND_AM = NODEPREFIX_BAND + "am";
    public static final String NODE_BAND_FM = NODEPREFIX_BAND + "fm";
    public static final String NODE_BAND_DAB = NODEPREFIX_BAND + "dab";

    private static final String NODEPREFIX_AMFMCHANNEL = "amfm:";
    private static final String NODEPREFIX_PROGRAM = "program:";

    private final BrowserRoot mRoot = new BrowserRoot(NODE_ROOT, null);

    private final Object mLock = new Object();
    private final @NonNull MediaBrowserServiceCompat mBrowserService;
    private final @Nullable ImageResolver mImageResolver;

    private List<MediaItem> mRootChildren;

    private final AmFmChannelList mAmChannels = new AmFmChannelList(
            NODE_BAND_AM, R.string.radio_am_text, "AM");
    private final AmFmChannelList mFmChannels = new AmFmChannelList(
            NODE_BAND_FM, R.string.radio_fm_text, "FM");
    private boolean mDABEnabled;

    private final ProgramList.OnCompleteListener mProgramListCompleteListener =
            this::onProgramListUpdated;
    @Nullable private ProgramList mProgramList;
    @Nullable private List<RadioManager.ProgramInfo> mProgramListSnapshot;
    @Nullable private List<MediaItem> mProgramListCache;
    private final List<Runnable> mProgramListTasks = new ArrayList<>();
    private final Map<String, ProgramSelector> mProgramSelectors = new HashMap<>();

    @Nullable Set<Program> mFavorites;
    @Nullable private List<MediaItem> mFavoritesCache;

    public BrowseTree(@NonNull MediaBrowserServiceCompat browserService,
            @Nullable ImageResolver imageResolver) {
        mBrowserService = Objects.requireNonNull(browserService);
        mImageResolver = imageResolver;
    }

    public BrowserRoot getRoot() {
        return mRoot;
    }

    private static MediaItem createChild(
            MediaDescriptionCompat.Builder descBuilder, String mediaId, String title,
            ProgramSelector sel, Bitmap icon, @Nullable Bundle extras) {
        descBuilder.setMediaId(mediaId)
                .setMediaUri(ProgramSelectorExt.toUri(sel))
                .setTitle(title)
                .setIconBitmap(icon);
        if (extras != null) {
            descBuilder.setExtras(extras);
        }
        return new MediaItem(descBuilder.build(), MediaItem.FLAG_PLAYABLE);
    }

    private static MediaItem createFolder(
            MediaDescriptionCompat.Builder descBuilder, String mediaId, String title,
            boolean isBrowseable, boolean isPlayable, long folderType, Bundle extras) {
        if (extras == null) extras = new Bundle();
        extras.putLong(EXTRA_BCRADIO_FOLDER_TYPE, folderType);

        MediaDescriptionCompat desc = descBuilder
                .setMediaId(mediaId).setTitle(title).setExtras(extras).build();

        int flags = 0;
        if (isBrowseable) flags |= MediaItem.FLAG_BROWSABLE;
        if (isPlayable) flags |= MediaItem.FLAG_PLAYABLE;
        return new MediaItem(desc, flags);
    }

    /**
     * Sets AM/FM region configuration.
     *
     * This method is meant to be called shortly after initialization, if AM/FM is supported.
     */
    public void setAmFmRegionConfig(@Nullable List<BandDescriptor> amFmBands) {
        List<BandDescriptor> amBands = new ArrayList<>();
        List<BandDescriptor> fmBands = new ArrayList<>();

        if (amFmBands != null) {
            for (BandDescriptor band : amFmBands) {
                final int freq = band.getLowerLimit();
                if (ProgramSelectorExt.isAmFrequency(freq)) {
                    amBands.add(band);
                } else if (ProgramSelectorExt.isFmFrequency(freq)) {
                    fmBands.add(band);
                }
            }
        }

        synchronized (mLock) {
            mAmChannels.setBands(amBands);
            mFmChannels.setBands(fmBands);
            mRootChildren = null;
            mBrowserService.notifyChildrenChanged(NODE_ROOT);
        }
    }

    /**
     * Configures the BrowseTree to include a DAB node or not
     */
    public void setDABEnabled(boolean enabled) {
        synchronized (mLock) {
            if (mDABEnabled != enabled) {
                mDABEnabled = enabled;
                mRootChildren = null;
                mBrowserService.notifyChildrenChanged(NODE_ROOT);
            }
        }
    }

    private void onProgramListUpdated() {
        synchronized (mLock) {
            mProgramListSnapshot = mProgramList.toList();
            mProgramListCache = null;
            mBrowserService.notifyChildrenChanged(NODE_PROGRAMS);

            for (Runnable task : mProgramListTasks) {
                task.run();
            }
            mProgramListTasks.clear();
        }
    }

    /**
     * Binds program list.
     *
     * This method is meant to be called shortly after opening a new tuner session.
     */
    public void setProgramList(@Nullable ProgramList programList) {
        synchronized (mLock) {
            boolean rootChanged = (mProgramList == null) != (programList == null);
            if (mProgramList != null) {
                mProgramList.removeOnCompleteListener(mProgramListCompleteListener);
            }
            mProgramList = programList;
            if (programList != null) {
                mProgramList.addOnCompleteListener(mProgramListCompleteListener);
            }
            if (rootChanged) {
                mRootChildren = null;
                mBrowserService.notifyChildrenChanged(NODE_ROOT);
            }
        }
    }

    private List<MediaItem> getPrograms() {
        synchronized (mLock) {
            if (mProgramListSnapshot == null) {
                Log.w(TAG, "There is no snapshot of the program list");
                return null;
            }

            if (mProgramListCache != null) return mProgramListCache;
            mProgramListCache = new ArrayList<>();

            MediaDescriptionCompat.Builder dbld = new MediaDescriptionCompat.Builder();

            for (RadioManager.ProgramInfo program : mProgramListSnapshot) {
                ProgramSelector sel = program.getSelector();
                String mediaId = selectorToMediaId(sel);
                mProgramSelectors.put(mediaId, sel);

                Bitmap icon = null;
                RadioMetadata meta = program.getMetadata();
                if (meta != null && mImageResolver != null) {
                    long id = RadioMetadataExt.getGlobalBitmapId(meta,
                            RadioMetadata.METADATA_KEY_ICON);
                    if (id != 0) icon = mImageResolver.resolve(id);
                }

                if (meta != null) {
                    Bundle extras = new Bundle();
                    extras.putParcelable(EXTRA_BCRADIO_METADATA, program.getMetadata());
                    mProgramListCache.add(createChild(dbld, mediaId,
                            ProgramInfoExt.getProgramName(program, 0), program.getSelector(), icon,
                            extras));
                } else {
                    mProgramListCache.add(createChild(dbld, mediaId,
                            ProgramInfoExt.getProgramName(program, 0), program.getSelector(), icon,
                            /* extra= */ null));
                }


            }

            if (mProgramListCache.size() == 0) {
                Log.v(TAG, "Program list is empty");
            }
            return mProgramListCache;
        }
    }

    private void sendPrograms(final Result<List<MediaItem>> result) {
        synchronized (mLock) {
            if (mProgramListSnapshot != null) {
                result.sendResult(getPrograms());
            } else {
                Log.d(TAG, "Program list is not ready yet");
                result.detach();
                mProgramListTasks.add(() -> result.sendResult(getPrograms()));
            }
        }
    }

    /**
     * Updates favorites list.
     */
    public void setFavorites(@Nullable Set<Program> favorites) {
        synchronized (mLock) {
            boolean rootChanged = (mFavorites == null) != (favorites == null);
            mFavorites = favorites;
            mFavoritesCache = null;
            mBrowserService.notifyChildrenChanged(NODE_FAVORITES);
            if (rootChanged) {
                mRootChildren = null;
                mBrowserService.notifyChildrenChanged(NODE_ROOT);
            }
        }
    }

    private List<MediaItem> getFavorites() {
        synchronized (mLock) {
            if (mFavorites == null) return null;
            if (mFavoritesCache != null) return mFavoritesCache;
            mFavoritesCache = new ArrayList<>();

            MediaDescriptionCompat.Builder dbld = new MediaDescriptionCompat.Builder();

            for (Program fav : mFavorites) {
                ProgramSelector sel = fav.getSelector();
                String mediaId = selectorToMediaId(sel);
                mProgramSelectors.putIfAbsent(mediaId, sel);  // prefer program list entries
                mFavoritesCache.add(createChild(dbld, mediaId, fav.getName(), sel, fav.getIcon(),
                        /* extras= */ null));
            }

            return mFavoritesCache;
        }
    }

    private List<MediaItem> getRootChildren() {
        synchronized (mLock) {
            if (mRootChildren != null) return mRootChildren;
            mRootChildren = new ArrayList<>();

            MediaDescriptionCompat.Builder dbld = new MediaDescriptionCompat.Builder();
            if (mProgramList != null) {
                mRootChildren.add(createFolder(dbld, NODE_PROGRAMS,
                        mBrowserService.getString(R.string.program_list_text),
                        true, false, BCRADIO_FOLDER_TYPE_PROGRAMS, null));
            }
            if (mFavorites != null) {
                mRootChildren.add(createFolder(dbld, NODE_FAVORITES,
                        mBrowserService.getString(R.string.favorites_list_text),
                        true, true, BCRADIO_FOLDER_TYPE_FAVORITES, null));
            }

            MediaItem amRoot = mAmChannels.getBandRoot();
            if (amRoot != null) mRootChildren.add(amRoot);
            MediaItem fmRoot = mFmChannels.getBandRoot();
            if (fmRoot != null) mRootChildren.add(fmRoot);

            if (mDABEnabled) {
                mRootChildren.add(createFolder(dbld, NODE_BAND_DAB,
                        mBrowserService.getString(R.string.radio_dab_text),
                        false, true, BCRADIO_FOLDER_TYPE_BAND, null));
            }

            return mRootChildren;
        }
    }

    private class AmFmChannelList {
        public final @NonNull String mMediaId;
        private final @StringRes int mBandName;
        private final @NonNull String mBandNameEn;
        private @Nullable List<BandDescriptor> mBands;
        private @Nullable List<MediaItem> mChannels;

        private AmFmChannelList(@NonNull String mediaId, @StringRes int bandName,
                @NonNull String bandNameEn) {
            mMediaId = Objects.requireNonNull(mediaId);
            mBandName = bandName;
            mBandNameEn = Objects.requireNonNull(bandNameEn);
        }

        public void setBands(List<BandDescriptor> bands) {
            synchronized (mLock) {
                mBands = bands;
                mChannels = null;
                mBrowserService.notifyChildrenChanged(mMediaId);
            }
        }

        private boolean isEmpty() {
            if (mBands == null) {
                Log.w(TAG, "AM/FM configuration not set");
                return true;
            }
            return mBands.isEmpty();
        }

        public @Nullable MediaItem getBandRoot() {
            if (isEmpty()) return null;
            Bundle extras = new Bundle();
            extras.putString(EXTRA_BCRADIO_BAND_NAME_EN, mBandNameEn);
            return createFolder(new MediaDescriptionCompat.Builder(), mMediaId,
                    mBrowserService.getString(mBandName), true, true, BCRADIO_FOLDER_TYPE_BAND,
                    extras);
        }

        public List<MediaItem> getChannels() {
            synchronized (mLock) {
                if (mChannels != null) return mChannels;
                if (isEmpty()) return null;
                mChannels = new ArrayList<>();

                MediaDescriptionCompat.Builder dbld = new MediaDescriptionCompat.Builder();

                for (BandDescriptor band : mBands) {
                    final int lowerLimit = band.getLowerLimit();
                    final int upperLimit = band.getUpperLimit();
                    final int spacing = band.getSpacing();
                    for (int ch = lowerLimit; ch <= upperLimit; ch += spacing) {
                        ProgramSelector sel = ProgramSelectorExt.createAmFmSelector(ch);
                        mChannels.add(createChild(dbld, NODEPREFIX_AMFMCHANNEL + ch,
                                ProgramSelectorExt.getDisplayName(sel, 0), sel, /* icon= */ null,
                                /* extra= */ null));
                    }
                }

                return mChannels;
            }
        }
    }

    /**
     * Loads subtree children.
     *
     * This method is meant to be used in MediaBrowserService's onLoadChildren callback.
     */
    public void loadChildren(final String parentMediaId, final Result<List<MediaItem>> result) {
        if (parentMediaId == null || result == null) return;

        if (NODE_ROOT.equals(parentMediaId)) {
            result.sendResult(getRootChildren());
        } else if (NODE_PROGRAMS.equals(parentMediaId)) {
            sendPrograms(result);
        } else if (NODE_FAVORITES.equals(parentMediaId)) {
            result.sendResult(getFavorites());
        } else if (parentMediaId.equals(mAmChannels.mMediaId)) {
            result.sendResult(mAmChannels.getChannels());
        } else if (parentMediaId.equals(mFmChannels.mMediaId)) {
            result.sendResult(mFmChannels.getChannels());
        } else {
            Log.w(TAG, "Invalid parent media ID: " + parentMediaId);
            result.sendResult(null);
        }
    }

    private static @NonNull String selectorToMediaId(@NonNull ProgramSelector sel) {
        ProgramSelector.Identifier id = sel.getPrimaryId();
        String mediaId = NODEPREFIX_PROGRAM + id.getType() + '/' + id.getValue();
        if (id.getType() == ProgramSelector.IDENTIFIER_TYPE_DAB_SID_EXT
                || id.getType() == ProgramSelector.IDENTIFIER_TYPE_DAB_DMB_SID_EXT) {
            ProgramSelector.Identifier[] seondaryIds = sel.getSecondaryIds();
            ProgramSelector.Identifier ensembleId = null;
            ProgramSelector.Identifier frequencyId = null;
            for (ProgramSelector.Identifier secondaryId : seondaryIds) {
                if (secondaryId.getType() == ProgramSelector.IDENTIFIER_TYPE_DAB_ENSEMBLE
                        && ensembleId == null) {
                    ensembleId = secondaryId;
                } else if (secondaryId.getType() == ProgramSelector.IDENTIFIER_TYPE_DAB_FREQUENCY
                        && frequencyId == null) {
                    frequencyId = secondaryId;
                }
            }
            if (ensembleId != null) {
                mediaId += ';' + ensembleId.getType() + '/' + ensembleId.getValue();
            }
            if (frequencyId != null) {
                mediaId += ';' + frequencyId.getType() + '/' + frequencyId.getValue();
            }
        }
        return mediaId;
    }

    /**
     * Resolves mediaId to a tunable {@link ProgramSelector}.
     *
     * This method is meant to be used in MediaSession's onPlayFromMediaId callback.
     */
    public @Nullable ProgramSelector parseMediaId(@Nullable String mediaId) {
        if (mediaId == null) return null;

        if (mediaId.startsWith(NODEPREFIX_AMFMCHANNEL)) {
            String freqStr = mediaId.substring(NODEPREFIX_AMFMCHANNEL.length());
            int freqInt;
            try {
                freqInt = Integer.parseInt(freqStr);
            } catch (NumberFormatException ex) {
                Log.e(TAG, "Invalid frequency", ex);
                return null;
            }
            return ProgramSelectorExt.createAmFmSelector(freqInt);
        } else if (mediaId.startsWith(NODEPREFIX_PROGRAM)) {
            return mProgramSelectors.get(mediaId);
        } else if (mediaId.equals(NODE_FAVORITES)) {
            if (mFavorites == null || mFavorites.isEmpty()) return null;
            return mFavorites.iterator().next().getSelector();
        } else if (mediaId.equals(NODE_PROGRAMS)) {
            if (mProgramListSnapshot == null || mProgramListSnapshot.isEmpty()) return null;
            return mProgramListSnapshot.get(0).getSelector();
        } else if (mediaId.equals(NODE_BAND_AM)) {
            if (mAmChannels.mBands == null || mAmChannels.mBands.isEmpty()) return null;
            return ProgramSelectorExt.createAmFmSelector(mAmChannels.mBands.get(0).getLowerLimit());
        } else if (mediaId.equals(NODE_BAND_FM)) {
            if (mFmChannels.mBands == null || mFmChannels.mBands.isEmpty()) return null;
            return ProgramSelectorExt.createAmFmSelector(mFmChannels.mBands.get(0).getLowerLimit());
        }
        return null;
    }
}
