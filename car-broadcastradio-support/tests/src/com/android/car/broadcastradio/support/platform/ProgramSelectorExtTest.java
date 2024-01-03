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

import static org.junit.Assert.assertThrows;

import android.hardware.radio.ProgramSelector;

import com.google.common.truth.Expect;

import org.junit.Rule;
import org.junit.Test;

public final class ProgramSelectorExtTest {

    private static final long AM_FREQUENCY_VALUE = 700;
    private static final long FM_FREQUENCY_VALUE = 88_500;
    private static final long HD_FREQUENCY_VALUE = 97_100;
    private static final int HD_SUBCHANNEL_VALUE = 1;
    private static final int HD_STATION_ID_VALUE = 0x01;
    private static final long DAB_FREQUENCY_VALUE = 220_352;
    private static final long DAB_ENSEMBLE_VALUE = 0x1001;
    private static final long DAB_SID_VALUE = 0x112;
    private static final int DAB_ECC_VALUE = 0xA0;
    private static final int DAB_SCIDS_VALUE = 1;
    private static final ProgramSelector.Identifier FM_IDENTIFIER = new ProgramSelector.Identifier(
            ProgramSelector.IDENTIFIER_TYPE_AMFM_FREQUENCY, FM_FREQUENCY_VALUE);
    private static final ProgramSelector.Identifier HD_STATION_EXT_IDENTIFIER =
            new ProgramSelector.Identifier(ProgramSelector.IDENTIFIER_TYPE_HD_STATION_ID_EXT,
                    (HD_FREQUENCY_VALUE << 36) | ((long) HD_SUBCHANNEL_VALUE << 32)
                            | HD_STATION_ID_VALUE);
    private static final ProgramSelector.Identifier DAB_DMB_SID_EXT_IDENTIFIER =
            new ProgramSelector.Identifier(ProgramSelector.IDENTIFIER_TYPE_DAB_DMB_SID_EXT,
                    ((long) DAB_SCIDS_VALUE << 40) | ((long) DAB_ECC_VALUE << 32)
                            | DAB_SID_VALUE);
    private static final ProgramSelector.Identifier DAB_ENSEMBLE_IDENTIFIER =
            new ProgramSelector.Identifier(ProgramSelector.IDENTIFIER_TYPE_DAB_ENSEMBLE,
                    DAB_ENSEMBLE_VALUE);
    private static final ProgramSelector.Identifier DAB_FREQUENCY_IDENTIFIER =
            new ProgramSelector.Identifier(ProgramSelector.IDENTIFIER_TYPE_DAB_FREQUENCY,
                    DAB_FREQUENCY_VALUE);
    private static final ProgramSelector FM_SELECTOR = new ProgramSelector(
            ProgramSelector.PROGRAM_TYPE_FM, FM_IDENTIFIER, /* secondaryIds= */ null,
            /* vendorIds= */ null);
    private static final ProgramSelector HD_SELECTOR = new ProgramSelector(
            ProgramSelector.PROGRAM_TYPE_FM_HD, HD_STATION_EXT_IDENTIFIER,
            new ProgramSelector.Identifier[]{}, /* vendorIds= */ null);
    private static final ProgramSelector DAB_SELECTOR = new ProgramSelector(
            ProgramSelector.PROGRAM_TYPE_DAB, DAB_DMB_SID_EXT_IDENTIFIER,
            new ProgramSelector.Identifier[]{DAB_FREQUENCY_IDENTIFIER, DAB_ENSEMBLE_IDENTIFIER},
            /* vendorIds= */ null);

    @Rule
    public final Expect mExpect = Expect.create();

    @Test
    public void isAmFrequency_withAmFrequency() {
        mExpect.withMessage("AM frequency")
                .that(ProgramSelectorExt.isAmFrequency(AM_FREQUENCY_VALUE)).isTrue();
    }

    @Test
    public void isAmFrequency_withFmFrequency() {
        mExpect.withMessage("Non-AM frequency")
                .that(ProgramSelectorExt.isAmFrequency(FM_FREQUENCY_VALUE)).isFalse();
    }

    @Test
    public void isFmFrequency_withAmFrequency() {
        mExpect.withMessage("Non-FM frequency")
                .that(ProgramSelectorExt.isFmFrequency(AM_FREQUENCY_VALUE)).isFalse();
    }

    @Test
    public void isFmFrequency_withFmFrequency() {
        mExpect.withMessage("FM frequency")
                .that(ProgramSelectorExt.isFmFrequency(FM_FREQUENCY_VALUE)).isTrue();
    }

    @Test
    public void createAmFmSelector() {
        mExpect.withMessage("FM selector").that(ProgramSelectorExt.createAmFmSelector(
                FM_FREQUENCY_VALUE)).isEqualTo(FM_SELECTOR);
    }

    @Test
    public void createAmFmSelector_withInvalidFrequency_throwsException() {
        long invalidFrequency = -1;

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            ProgramSelectorExt.createAmFmSelector(invalidFrequency);
        });

        mExpect.withMessage("Invalid frequency exception").that(thrown).hasMessageThat()
                .contains("illegal frequency value");
    }

    @Test
    public void hasId_forSelectorWithIdAsPrimaryId() {
        mExpect.withMessage("AM/FM id in selector").that(ProgramSelectorExt.hasId(FM_SELECTOR,
                ProgramSelector.IDENTIFIER_TYPE_AMFM_FREQUENCY)).isTrue();
    }

    @Test
    public void hasId_forSelectorWithIdInSecondaryIds() {
        mExpect.withMessage("DAB ensemble id in selector").that(ProgramSelectorExt.hasId(
                DAB_SELECTOR, ProgramSelector.IDENTIFIER_TYPE_DAB_FREQUENCY)).isTrue();
    }

    @Test
    public void hasId_forSelectorWithoutId() {
        mExpect.withMessage("Id type beyond selector ids").that(ProgramSelectorExt.hasId(
                HD_SELECTOR, ProgramSelector.IDENTIFIER_TYPE_AMFM_FREQUENCY)).isFalse();
    }
}
