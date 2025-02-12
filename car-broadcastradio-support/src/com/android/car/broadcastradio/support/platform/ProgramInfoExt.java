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

package com.android.car.broadcastradio.support.platform;

import android.graphics.Bitmap;
import android.hardware.radio.ProgramSelector;
import android.hardware.radio.RadioManager;
import android.hardware.radio.RadioManager.ProgramInfo;
import android.hardware.radio.RadioMetadata;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Proposed extensions to android.hardware.radio.RadioManager.ProgramInfo.
 *
 * They might eventually get pushed to the framework.
 */
public class ProgramInfoExt {
    private static final String TAG = "BcRadioApp.pinfoext";

    /**
     * If there is no suitable program name, return null instead of doing
     * a fallback to channel display name.
     */
    public static final int NAME_NO_CHANNEL_FALLBACK = 1 << 16;

    /**
     * Flags to control how to fetch program name with {@link #getProgramName}.
     *
     * Lower 16 bits are reserved for {@link ProgramSelectorExt.NameFlag}.
     */
    @IntDef(flag = true, value = {
        ProgramSelectorExt.NAME_NO_MODULATION,
        ProgramSelectorExt.NAME_MODULATION_ONLY,
        NAME_NO_CHANNEL_FALLBACK,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface NameFlag {}

    private static final String[] PROGRAM_NAME_ORDER = new String[] {
        RadioMetadata.METADATA_KEY_PROGRAM_NAME,
        RadioMetadata.METADATA_KEY_DAB_COMPONENT_NAME,
        RadioMetadata.METADATA_KEY_DAB_SERVICE_NAME,
        RadioMetadata.METADATA_KEY_DAB_ENSEMBLE_NAME,
        RadioMetadata.METADATA_KEY_RDS_PS,
    };

    private static final Map<String, String> RADIO_TO_MEDIA_METADATA_STRING_TYPE =
            new HashMap<>(13);
    private static final List<String> RADIO_METADATA_INT_TYPE =
            new ArrayList<>(3);
    private static final Map<String, String> RADIO_TO_MEDIA_METADATA_BITMAP_TYPE =
            new HashMap<>(2);
    static {
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_TITLE,
                MediaMetadataCompat.METADATA_KEY_TITLE);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_ARTIST,
                MediaMetadataCompat.METADATA_KEY_ARTIST);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_ALBUM,
                MediaMetadataCompat.METADATA_KEY_ALBUM);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_GENRE,
                MediaMetadataCompat.METADATA_KEY_GENRE);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_RDS_PS,
                RadioMetadata.METADATA_KEY_RDS_PS);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_RDS_RT,
                RadioMetadata.METADATA_KEY_RDS_RT);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_PROGRAM_NAME,
                RadioMetadata.METADATA_KEY_PROGRAM_NAME);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_DAB_ENSEMBLE_NAME,
                RadioMetadata.METADATA_KEY_DAB_ENSEMBLE_NAME);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_DAB_ENSEMBLE_NAME_SHORT,
                RadioMetadata.METADATA_KEY_DAB_ENSEMBLE_NAME_SHORT);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_DAB_SERVICE_NAME,
                RadioMetadata.METADATA_KEY_DAB_SERVICE_NAME);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_DAB_SERVICE_NAME_SHORT,
                RadioMetadata.METADATA_KEY_DAB_SERVICE_NAME_SHORT);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_DAB_COMPONENT_NAME,
                RadioMetadata.METADATA_KEY_DAB_COMPONENT_NAME);
        RADIO_TO_MEDIA_METADATA_STRING_TYPE.put(RadioMetadata.METADATA_KEY_DAB_COMPONENT_NAME_SHORT,
                RadioMetadata.METADATA_KEY_DAB_COMPONENT_NAME_SHORT);

        RADIO_METADATA_INT_TYPE.add(RadioMetadata.METADATA_KEY_RDS_PI);
        RADIO_METADATA_INT_TYPE.add(RadioMetadata.METADATA_KEY_RDS_PTY);
        RADIO_METADATA_INT_TYPE.add(RadioMetadata.METADATA_KEY_RBDS_PTY);

        RADIO_TO_MEDIA_METADATA_BITMAP_TYPE.put(RadioMetadata.METADATA_KEY_ART,
                MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
        RADIO_TO_MEDIA_METADATA_BITMAP_TYPE.put(RadioMetadata.METADATA_KEY_ICON,
                MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON);
    }

    /**
     * Returns program name suitable to display.
     *
     * <p>If there is no program name, it falls back to channel name. Flags related to
     * the channel name display will be forwarded to the channel name generation method.
     *
     * @param info {@link ProgramInfo} to get name from
     * @param flags Fallback method
     * @param programNameOrder {@link RadioMetadata} metadata keys to pull from {@link ProgramInfo}
     * for the program name
     */
    @NonNull
    public static String getProgramName(@NonNull ProgramInfo info, @NameFlag int flags,
            @NonNull String[] programNameOrder) {
        Objects.requireNonNull(info, "info can not be null.");
        Objects.requireNonNull(programNameOrder, "programNameOrder can not be null");

        RadioMetadata meta = info.getMetadata();
        if (meta != null) {
            for (String key : programNameOrder) {
                String value = meta.getString(key);
                if (value != null) return value;
            }
        }

        if ((flags & NAME_NO_CHANNEL_FALLBACK) != 0) return "";

        ProgramSelector sel = info.getSelector();

        // if it's AM/FM program, prefer to display currently used AF frequency
        if (ProgramSelectorExt.isAmFmProgram(sel)) {
            ProgramSelector.Identifier phy = info.getPhysicallyTunedTo();
            if (phy != null && phy.getType() == ProgramSelector.IDENTIFIER_TYPE_AMFM_FREQUENCY) {
                String chName = ProgramSelectorExt.formatAmFmFrequency(phy.getValue(), flags);
                if (chName != null) return chName;
            }
        }

        String selName = ProgramSelectorExt.getDisplayName(sel, flags);
        if (selName != null) return selName;

        Log.w(TAG, "ProgramInfo without a name nor channel name");
        return "";
    }

    /**
     * Returns program name suitable to display.
     *
     * <p>If there is no program name, it falls back to channel name. Flags related to
     * the channel name display will be forwarded to the channel name generation method.
     */
    @NonNull
    public static String getProgramName(@NonNull ProgramInfo info, @NameFlag int flags) {
        return getProgramName(info, flags, PROGRAM_NAME_ORDER);
    }

    /**
     * Proposed reimplementation of {@link RadioManager.ProgramInfo#getMetadata}.
     *
     * As opposed to the original implementation, it never returns null.
     */
    public static @NonNull RadioMetadata getMetadata(@NonNull ProgramInfo info) {
        RadioMetadata meta = info.getMetadata();
        if (meta != null) return meta;

        /* Creating new Metadata object on each get won't be necessary after we
         * push this code to the framework. */
        return (new RadioMetadata.Builder()).build();
    }

    /**
     * Converts {@link ProgramInfo} to {@link MediaMetadataCompat}.
     *
     * <p>This method is meant to be used for currently playing station in
     * {@link MediaSessionCompat}.
     *
     * <ul>The following {@link MediaMetadataCompat} keys will be populated in the
     * {@link MediaMetadataCompat}:
     *  <li>{@link MediaMetadataCompat#METADATA_KEY_DISPLAY_TITLE}</li>
     *  <li>{@link MediaMetadataCompat#METADATA_KEY_TITLE}</li>
     *  <li>{@link MediaMetadataCompat#METADATA_KEY_ARTIST}</li>
     *  <li>{@link MediaMetadataCompat#METADATA_KEY_ALBUM}</li>
     *  <li>{@link MediaMetadataCompat#METADATA_KEY_DISPLAY_SUBTITLE}</li>
     *  <li>{@link MediaMetadataCompat#METADATA_KEY_ALBUM_ART}</li>
     *  <li>{@link MediaMetadataCompat#METADATA_KEY_USER_RATING}</li>
     *  <li>{@link MediaMetadataCompat#METADATA_KEY_MEDIA_URI}</li>
     * <ul/>
     *
     * <p>Other radio-specific metadata types will be populated in the {@link MediaMetadataCompat}
     * directly with their key defined in {@link RadioMetadata} as custom keys.
     *
     * @param info {@link ProgramInfo} to convert
     * @param isFavorite {@code true}, if a given program is a favorite
     * @param imageResolver metadata images resolver/cache
     * @param programNameOrder order of keys to look for program name in {@link ProgramInfo}
     * @return {@link MediaMetadataCompat} object
     */
    @NonNull
    public static MediaMetadataCompat toMediaDisplayMetadata(@NonNull ProgramInfo info,
            boolean isFavorite, @Nullable ImageResolver imageResolver,
            @NonNull String[] programNameOrder) {
        Objects.requireNonNull(info, "info can not be null.");
        Objects.requireNonNull(programNameOrder, "programNameOrder can not be null.");

        MediaMetadataCompat.Builder bld = new MediaMetadataCompat.Builder();

        ProgramSelector selector;
        ProgramSelector.Identifier logicallyTunedTo = info.getLogicallyTunedTo();
        if (logicallyTunedTo != null && logicallyTunedTo.getType()
                == ProgramSelector.IDENTIFIER_TYPE_AMFM_FREQUENCY) {
            selector = ProgramSelectorExt.createAmFmSelector(logicallyTunedTo.getValue());
        } else {
            selector = info.getSelector();
        }
        String displayTitle = ProgramSelectorExt.getDisplayName(selector, info.getChannel());
        bld.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, displayTitle);
        String subtitle = getProgramName(info, /* flags= */ 0, programNameOrder);
        bld.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, subtitle);
        Uri mediaUri = ProgramSelectorExt.toUri(selector);
        if (mediaUri != null) {
            bld.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri.toString());
        }

        convertRadioMetadata(bld, imageResolver, info.getMetadata());

        bld.putRating(MediaMetadataCompat.METADATA_KEY_USER_RATING,
                RatingCompat.newHeartRating(isFavorite));

        return bld.build();
    }

    /**
     * Converts {@link ProgramInfo} to {@link MediaMetadataCompat}.
     *
     * {@see toMediaDisplayMetadata}
     */
    public static @NonNull MediaMetadataCompat toMediaMetadata(@NonNull ProgramInfo info,
            boolean isFavorite, @Nullable ImageResolver imageResolver) {
        return toMediaDisplayMetadata(info, isFavorite, imageResolver, PROGRAM_NAME_ORDER);
    }

    private static void convertRadioMetadata(MediaMetadataCompat.Builder bld,
                                             @Nullable ImageResolver imageResolver,
                                             @Nullable RadioMetadata meta) {
        if (meta == null) {
            return;
        }

        for (int i = 0; i < RADIO_METADATA_INT_TYPE.size(); i++) {
            String intTypeKey = RADIO_METADATA_INT_TYPE.get(i);
            putIntMetadata(bld, meta, intTypeKey, intTypeKey);
        }
        for (Map.Entry<String, String> entry : RADIO_TO_MEDIA_METADATA_STRING_TYPE.entrySet()) {
            putStringMetadata(bld, meta, entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : RADIO_TO_MEDIA_METADATA_BITMAP_TYPE.entrySet()) {
            putBitmapMetadata(bld, imageResolver, meta, entry.getKey(), entry.getValue());
        }
    }

    private static void putStringMetadata(MediaMetadataCompat.Builder bld,
                                          RadioMetadata radioMetadata, String radioMetadataKey,
                                          String metadataKey) {
        String value = radioMetadata.getString(radioMetadataKey);
        if (value == null) {
            return;
        }
        bld.putString(metadataKey, value);
    }

    private static void putIntMetadata(MediaMetadataCompat.Builder bld,
                                       RadioMetadata radioMetadata, String radioMetadataKey,
                                       String metadataKey) {
        int value = radioMetadata.getInt(radioMetadataKey);
        if (value == 0) {
            return;
        }
        bld.putLong(metadataKey, value);
    }

    private static void putBitmapMetadata(MediaMetadataCompat.Builder bld,
                                          @Nullable ImageResolver imageResolver,
                                          RadioMetadata meta, String radioMetadataKey,
                                          String metadataKey) {
        long imageId = RadioMetadataExt.getGlobalBitmapId(meta, radioMetadataKey);
        if (imageId == 0 || imageResolver == null) {
            return;
        }
        Bitmap bm = imageResolver.resolve(imageId);
        if (bm == null) {
            return;
        }
        bld.putBitmap(metadataKey, bm);
    }
    public static class ProgramInfoComparator implements Comparator<RadioManager.ProgramInfo> {
        @Override
        public int compare(RadioManager.ProgramInfo info1, RadioManager.ProgramInfo info2) {
            Comparator<ProgramSelector> selectorComparator =
                    new ProgramSelectorExt.ProgramSelectorComparator();
            return selectorComparator.compare(info1.getSelector(), info2.getSelector());
        }
    }

    /**
     * Compares if two {@link MediaMetadataCompat} objects contains the same string and integer
     * type radio metadata values.
     */
    public static boolean containsSameRadioMetadata(@Nullable MediaMetadataCompat metadata1,
                                                    @Nullable MediaMetadataCompat metadata2) {
        if (Objects.equals(metadata1, metadata2)) {
            return true;
        }
        if (metadata1 == null || metadata2 == null) {
            return false;
        }
        for (int i = 0; i < RADIO_METADATA_INT_TYPE.size(); i++) {
            String intTypeKey = RADIO_METADATA_INT_TYPE.get(i);
            if (Objects.equals(metadata1.getLong(intTypeKey), metadata2.getLong(intTypeKey))) {
                continue;
            }
            return false;
        }
        for (String metadataKey : RADIO_TO_MEDIA_METADATA_STRING_TYPE.values()) {
            if (Objects.equals(metadata1.getString(metadataKey),
                    metadata2.getString(metadataKey))) {
                continue;
            }
            return false;
        }
        return true;
    }
}
