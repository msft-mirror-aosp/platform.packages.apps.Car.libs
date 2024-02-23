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

package com.android.car.media.common.source;

import android.car.Car;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.service.media.MediaBrowserService;
import android.support.v4.media.session.MediaControllerCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.car.apps.common.BitmapUtils;
import com.android.car.apps.common.IconCropper;
import com.android.car.media.common.R;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;


/**
 * This represents a source of media content. It provides convenient methods to access media source
 * metadata, such as application name and icon. Media content can either be derived from a
 * {@link MediaBrowserService} or a {@link MediaControllerCompat}.
 */
public class MediaSource {
    private static final String TAG = "MediaSource";

    @Nullable
    private final ComponentName mBrowseService;
    @Nullable
    private final MediaControllerCompat mMediaController;
    @NonNull
    private final CharSequence mDisplayName;
    @NonNull
    private final Drawable mIcon;
    @NonNull
    private final IconCropper mIconCropper;

    /**
     * Creates a {@link MediaSource} for the given {@link ComponentName}
     */
    @Nullable
    public static MediaSource create(@NonNull Context ctx, @NonNull ComponentName componentName) {
        ServiceInfo serviceInfo = getBrowseServiceInfo(ctx, componentName);

        String className = serviceInfo != null ? serviceInfo.name : null;
        if (TextUtils.isEmpty(className)) {
            Log.w(TAG, "No MediaBrowserService for component " + componentName.flattenToString());
            return null;
        }

        try {
            String packageName = componentName.getPackageName();
            CharSequence displayName = extractDisplayName(ctx, serviceInfo, packageName);
            Drawable icon = extractIcon(ctx, serviceInfo, packageName);
            ComponentName browseService = new ComponentName(packageName, className);
            return new MediaSource(browseService, null, displayName, icon, new IconCropper(ctx));
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Component not found " + componentName.flattenToString());
            return null;
        }
    }

    /**
     * Creates a {@link MediaSource} for the given {@link MediaControllerCompat}
     */
    @Nullable
    public static MediaSource create(@NonNull Context context,
            @NonNull MediaControllerCompat mediaController) {
        String packageName = mediaController.getPackageName();
        try {
            ServiceInfo serviceInfo = null;
            ComponentName componentName = extractServiceComponentName(mediaController);
            if (componentName != null) {
                serviceInfo = getBrowseServiceInfo(context, componentName);
                String className = serviceInfo != null ? serviceInfo.name : null;
                if (TextUtils.isEmpty(className)) {
                    serviceInfo = null;
                }
            }

            CharSequence displayName = extractDisplayName(context, serviceInfo, packageName);
            Drawable icon = extractIcon(context, serviceInfo, packageName);

            return new MediaSource(/* componentName= */ null, mediaController, displayName, icon,
                new IconCropper(context));
        } catch (NameNotFoundException e) {
            Log.w(TAG, "App not found " + packageName);
            return null;
        }
    }

    @VisibleForTesting
    public MediaSource(@Nullable ComponentName browseService,
            @Nullable MediaControllerCompat mediaController, @NonNull CharSequence displayName,
            @NonNull Drawable icon, @NonNull IconCropper iconCropper) {
        mBrowseService = browseService;
        mMediaController = mediaController;
        mDisplayName = displayName;
        mIcon = icon;
        mIconCropper = iconCropper;
    }

    /**
     * @return the {@link ServiceInfo} corresponding to a {@link MediaBrowserService} in the media
     * source, or null if the media source doesn't implement {@link MediaBrowserService}. A non-null
     * result doesn't imply that this service is accessible. The consumer code should attempt to
     * connect and handle rejections gracefully.
     */
    @Nullable
    private static ServiceInfo getBrowseServiceInfo(@NonNull Context context,
            @NonNull ComponentName componentName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent();
        intent.setAction(MediaBrowserService.SERVICE_INTERFACE);
        intent.setPackage(componentName.getPackageName());
        List<ResolveInfo> resolveInfos = packageManager.queryIntentServices(intent,
                PackageManager.GET_RESOLVED_FILTER);
        if (resolveInfos == null || resolveInfos.isEmpty()) {
            return null;
        }
        String className = componentName.getClassName();
        if (TextUtils.isEmpty(className)) {
            return resolveInfos.get(0).serviceInfo;
        }
        for (ResolveInfo resolveInfo : resolveInfos) {
            ServiceInfo result = resolveInfo.serviceInfo;
            if (result.name.equals(className)) {
                return result;
            }
        }
        return null;
    }

    /**
     * @return a proper app name. Checks service label first. If failed, uses application label
     * as fallback.
     */
    @NonNull
    private static CharSequence extractDisplayName(@NonNull Context context,
            @Nullable ServiceInfo serviceInfo, @NonNull String packageName)
            throws PackageManager.NameNotFoundException {
        if (serviceInfo != null && serviceInfo.labelRes != 0) {
            return serviceInfo.loadLabel(context.getPackageManager());
        }
        ApplicationInfo applicationInfo =
                context.getPackageManager().getApplicationInfo(packageName,
                        PackageManager.GET_META_DATA);
        return applicationInfo.loadLabel(context.getPackageManager());
    }

    /**
     * @return a proper icon. Checks service icon first. If failed, uses application icon as
     * fallback.
     */
    @NonNull
    private static Drawable extractIcon(@NonNull Context context, @Nullable ServiceInfo serviceInfo,
            @NonNull String packageName) throws PackageManager.NameNotFoundException {
        Drawable appIcon = serviceInfo != null ? serviceInfo.loadIcon(context.getPackageManager())
                : context.getPackageManager().getApplicationIcon(packageName);

        return BitmapUtils.maybeFlagDrawable(context, appIcon);
    }

    /**
     * @return the browse service associated with the media session if provided, null otherwise.
     */
    @Nullable
    private static ComponentName extractServiceComponentName(MediaControllerCompat controller) {
        if (controller.getExtras() == null || controller.getExtras()
                .getString(Car.CAR_EXTRA_BROWSE_SERVICE_FOR_SESSION) == null) {
            return null;
        }
        String serviceNameString =
                controller.getExtras().getString(Car.CAR_EXTRA_BROWSE_SERVICE_FOR_SESSION);

        return new ComponentName(controller.getPackageName(), serviceNameString);
    }

    /**
     * @return media source human readable name for display.
     */
    @NonNull
    public CharSequence getDisplayName(Context context) {
        ServiceInfo serviceInfo = null;
        if (mBrowseService != null) {
            serviceInfo = getBrowseServiceInfo(context, mBrowseService);
        }
        try {
            return extractDisplayName(context, serviceInfo, getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getDisplayName: " + e);
            return mDisplayName;
        }
    }

    /**
     * @return the package name of this media source.
     */
    @NonNull
    public String getPackageName() {
        if (mBrowseService != null) {
            return mBrowseService.getPackageName();
        } else if (mMediaController != null) {
            return mMediaController.getPackageName();
        }
        Log.e(TAG, "getPackageName() has null BrowseService and Controller");
        return ""; // Should never happen
    }

    /**
     * @return a {@link ComponentName} referencing this media source's {@link MediaBrowserService}.
     */
    @Nullable
    public ComponentName getBrowseServiceComponentName() {
        return mBrowseService;
    }

    /**
     * @return a {@link Drawable} as the media source's icon.
     */
    @NonNull
    public Drawable getIcon() {
        return mIcon;
    }

    /**
     * Returns this media source's icon cropped to a predefined shape (see
     * {@link #IconCropper(Context)} on where and how the shape is defined).
     */
    public Bitmap getCroppedPackageIcon() {
        return mIconCropper.crop(mIcon);
    }

    /**
     * @return {@link MediaControllerCompat} of this media source
     */
    @Nullable
    public MediaControllerCompat getMediaController() {
        return mMediaController;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaSource that = (MediaSource) o;
        if (mBrowseService != null) {
            return Objects.equals(mBrowseService, that.mBrowseService);
        }
        return Objects.equals(mMediaController, that.mMediaController);
    }

    @Override
    public int hashCode() {
        if (mBrowseService != null) {
            return Objects.hash(mBrowseService);
        } else if (mMediaController != null) {
            return Objects.hash(mMediaController);
        }
        Log.e(TAG, "hashCode() has null BrowseService and Controller");
        return 0; // Should never happen
    }

    @Override
    @NonNull
    public String toString() {
        if (mBrowseService != null) {
            mBrowseService.flattenToString();
        } else if (mMediaController != null) {
            return mMediaController.toString();
        }
        Log.e(TAG, "toString() has null BrowseService and Controller");
        return ""; // Should never happen
    }

    /**
     * @return an intent to open the media source selector, or null if no source selector is
     * configured.
     * @param popup Whether the intent should point to the regular app selector (false), which
     *              would open the selected media source in Media Center, or the "popup" version
     *              (true), which would just select the source and dismiss itself.
     */
    @Nullable
    public static Intent getSourceSelectorIntent(Context context, boolean popup) {
        String uri = context.getString(popup ? R.string.launcher_popup_intent
                : R.string.launcher_intent);
        try {
            return uri != null && !uri.isEmpty() ? Intent.parseUri(uri, Intent.URI_INTENT_SCHEME)
                    : null;
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Wrong app-launcher intent: " + uri, e);
        }
    }
}
