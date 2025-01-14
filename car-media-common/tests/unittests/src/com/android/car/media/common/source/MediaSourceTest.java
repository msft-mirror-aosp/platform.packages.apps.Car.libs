/*
 * Copyright 2024 The Android Open Source Project
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

package com.android.car.media.common.source;

import static com.android.car.media.common.source.MediaSource.ANDROIDX_CAR_APP_LAUNCHABLE;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.media.common.R;
import com.android.car.testing.common.InstantTaskExecutorRule;
import com.android.car.testing.common.TestLifecycleOwner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MediaSourceTest {

    private static final String TEST_MBS_PKG = "com.android.car.test.mbs";
    private static final String TEST_MBS_CLS = ".class";
    private static final ComponentName TEST_MBS_CN =
            new ComponentName(TEST_MBS_PKG, TEST_MBS_CLS);
    private static final String TEST_MBS_COMPONENT = TEST_MBS_PKG
            + "/" + TEST_MBS_CLS;

    @Rule
    public final MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Rule
    public final InstantTaskExecutorRule mTaskExecutorRule = new InstantTaskExecutorRule();
    @Rule
    public final TestLifecycleOwner mLifecycleOwner = new TestLifecycleOwner();

    @Mock
    private Context mMockContext;
    @Mock
    private Resources mMockResources;
    @Mock
    private PackageManager mMockPackageManager;

    @Before
    public void setUp() {
        when(mMockContext.getPackageManager()).thenReturn(mMockPackageManager);
    }

    @Test
    public void isMediaTemplate_isMBSLaunchable_returnsTrue() {
        mockHasLaunchableMBS(/* isLaunchable= */ true);

        assertThat(MediaSource.isMediaTemplate(mMockContext, TEST_MBS_CN)).isTrue();
    }

    @Test
    public void isMediaTemplate_isNotMBSLaunchable_returnsFalse() {
        mockHasLaunchableMBS(/* isLaunchable= */ false);

        assertThat(MediaSource.isMediaTemplate(mMockContext, TEST_MBS_CN)).isFalse();
    }

    @Test
    public void isMediaTemplate_isLegacyApp_returnsTrue() {
        mockNoMBS();
        mockIsLegacyApp(/* isLegacyApp= */ true);

        assertThat(MediaSource.isMediaTemplate(mMockContext, TEST_MBS_CN)).isTrue();
    }

    @Test
    public void isMediaTemplate_isNotLegacyApp_returnsFalse() {
        mockNoMBS();
        mockIsLegacyApp(/* isLegacyApp= */ false);

        assertThat(MediaSource.isMediaTemplate(mMockContext, TEST_MBS_CN)).isFalse();
    }

    @Test
    public void isAudioMediaSource_isCustomMediaPackage_returnsTrue() {
        when(mMockContext.getResources()).thenReturn(mMockResources);
        when(mMockResources.getStringArray(eq(R.array.custom_media_packages)))
                .thenReturn(new String[]{TEST_MBS_COMPONENT});
        mockNoMBS();
        mockIsLegacyApp(/* isLegacyApp= */ false);

        assertThat(MediaSource.isAudioMediaSource(mMockContext, TEST_MBS_CN)).isTrue();
    }

    private void mockNoMBS() {
        List<ResolveInfo> resolveInfoList = new ArrayList<>();
        when(mMockPackageManager.queryIntentServices(any(Intent.class),
                eq(PackageManager.GET_META_DATA))).thenReturn(resolveInfoList);
    }

    private void mockHasLaunchableMBS(boolean isLaunchable) {
        List<ResolveInfo> resolveInfoList = new ArrayList<>();
        resolveInfoList.add(constructResolveInfo(isLaunchable));
        when(mMockPackageManager.queryIntentServices(any(Intent.class),
                eq(PackageManager.GET_META_DATA))).thenReturn(resolveInfoList);
    }

    private ResolveInfo constructResolveInfo(boolean isLaunchable) {
        ResolveInfo info = new ResolveInfo();
        info.serviceInfo = new ServiceInfo();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ANDROIDX_CAR_APP_LAUNCHABLE, isLaunchable);
        info.serviceInfo.metaData = bundle;
        return info;
    }

    private void mockIsLegacyApp(boolean isLegacyApp) {
        when(mMockPackageManager.getLaunchIntentForPackage(eq(TEST_MBS_PKG)))
                .thenReturn(isLegacyApp ? null : new Intent());
    }
}
