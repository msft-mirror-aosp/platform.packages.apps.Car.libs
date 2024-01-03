/**
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

package com.android.car.broadcastradio.support.platform;

import android.hardware.radio.ProgramSelector;
import android.hardware.radio.RadioManager;
import android.hardware.radio.RadioMetadata;

import com.google.common.truth.Expect;

import org.junit.Rule;
import org.junit.Test;

import java.util.Comparator;

public final class ProgramInfoExtTest {

    private static final long FM_FREQUENCY = 88_500;
    private static final String RDS_VALUE = "TestRds";
    private static final String PROGRAM_NAME_VALUE = "TestProgramName";
    private static final String TITLE_VALUE = "TestTitle";
    private static final String ARTIST_VALUE = "TestArtist";
    private static final String ALBUM_VALUE = "TestAlbum";
    private static final ProgramSelector.Identifier FM_IDENTIFIER = new ProgramSelector.Identifier(
            ProgramSelector.IDENTIFIER_TYPE_AMFM_FREQUENCY, FM_FREQUENCY);
    private static final ProgramSelector FM_SELECTOR = new ProgramSelector(
            ProgramSelector.PROGRAM_TYPE_FM, FM_IDENTIFIER, /* secondaryIds= */ null,
            /* vendorIds= */ null);
    private static final RadioMetadata RADIO_METADATA = new RadioMetadata.Builder()
            .putString(RadioMetadata.METADATA_KEY_RDS_PS, RDS_VALUE)
            .putString(RadioMetadata.METADATA_KEY_PROGRAM_NAME, PROGRAM_NAME_VALUE)
            .putString(RadioMetadata.METADATA_KEY_TITLE, TITLE_VALUE)
            .putString(RadioMetadata.METADATA_KEY_ARTIST, ARTIST_VALUE)
            .putString(RadioMetadata.METADATA_KEY_ALBUM, ALBUM_VALUE).build();
    private static final RadioMetadata EMPTY_RADIO_METADATA = new RadioMetadata.Builder().build();
    private static final RadioManager.ProgramInfo FM_INFO = new RadioManager.ProgramInfo(
            FM_SELECTOR, FM_IDENTIFIER, FM_IDENTIFIER, /* relatedContents= */ null,
            /* infoFlags= */ 0, /* signalQuality= */ 1, RADIO_METADATA,
            /* vendorInfo= */ null);

    private static final Comparator<ProgramSelector> SELECTOR_COMPARATOR =
            new ProgramSelectorExt.ProgramSelectorComparator();
    private static final Comparator<RadioManager.ProgramInfo> PROGRAM_INFO_COMPARATOR =
            new ProgramInfoExt.ProgramInfoComparator();

    @Rule
    public final Expect mExpect = Expect.create();

    @Test
    public void getMetadata() {
        mExpect.withMessage("FM radio metadata")
                .that(ProgramInfoExt.getMetadata(FM_INFO)).isEqualTo(RADIO_METADATA);
    }

    @Test
    public void getMetadata_withNullMetadata() {
        RadioManager.ProgramInfo infoWithNullMetadata = new RadioManager.ProgramInfo(
                FM_SELECTOR, FM_IDENTIFIER, FM_IDENTIFIER, /* relatedContents= */ null,
                /* infoFlags= */ 0, /* signalQuality= */ 1, /* metadata= */ null,
                /* vendorInfo= */ null);

        mExpect.withMessage("FM radio metadata with null metadata")
                .that(ProgramInfoExt.getMetadata(infoWithNullMetadata))
                .isEqualTo(EMPTY_RADIO_METADATA);
    }

    @Test
    public void compare_withSelectorsOfDifferentTypes() {
        ProgramSelector fmSel2 = ProgramSelectorExt.createAmFmSelector(FM_FREQUENCY + 200);
        RadioManager.ProgramInfo fmInfo2 = new RadioManager.ProgramInfo(fmSel2,
                fmSel2.getPrimaryId(), fmSel2.getPrimaryId(), /* relatedContents= */ null,
                /* infoFlags= */ 1, /* signalQuality= */ 0, new RadioMetadata.Builder().build(),
                /* vendorInfo= */ null);
        int expectedResult = SELECTOR_COMPARATOR.compare(fmSel2, FM_SELECTOR);

        mExpect.withMessage("Comparison between FM stations")
                .that(PROGRAM_INFO_COMPARATOR.compare(fmInfo2, FM_INFO)).isEqualTo(expectedResult);
    }
}
