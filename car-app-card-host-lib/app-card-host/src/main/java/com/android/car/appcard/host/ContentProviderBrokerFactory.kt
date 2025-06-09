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

package com.android.car.appcard.host

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import com.android.car.appcard.AppCardContentProvider
import java.util.function.Consumer
import java.util.stream.Collectors

/** An [BrokerFactory] that supplies [ContentProviderBroker]s */
internal class ContentProviderBrokerFactory(
    private val context: Context,
    private val contentResolver: ContentResolver,
    private val packageManager: PackageManager,
    private val expectedPermission: String,
    private val callback: AppCardObserver.AppCardObserverCallback,
) : BrokerFactory {
    override fun connectToAllCompatibleApplications(): Map<ApplicationIdentifier, AppCardBroker> {
        return refreshCompatibleApplicationInternal()
    }

    /** Add any newly added [AppCardContentProvider] */
    override fun refreshCompatibleApplication(
        identifierSet: Set<ApplicationIdentifier>
    ): Map<ApplicationIdentifier, AppCardBroker> {
        val packageSet = identifierSet.stream().map { it.packageName }.collect(Collectors.toSet())
        return refreshCompatibleApplicationInternal(packageSet)
    }

    private fun refreshCompatibleApplicationInternal(
        packageSet: Set<String>? = null
    ): Map<ApplicationIdentifier, AppCardBroker> {
        val result = mutableMapOf<ApplicationIdentifier, AppCardBroker>()
        compatibleProviderResolveInfos.forEach(
            Consumer { resolveInfo: ResolveInfo ->
                val packageName = resolveInfo.providerInfo.packageName
                val authority = resolveInfo.providerInfo.authority

                if (checkProviderInfo(resolveInfo.providerInfo)) return@Consumer

                val identifier = ApplicationIdentifier(packageName, authority)

                packageSet?.let {
                    if (it.contains(identifier.packageName)) {
                        return@Consumer
                    }
                }

                try {
                    val contentProviderClient =
                        contentResolver.acquireUnstableContentProviderClient(authority)
                    contentProviderClient?.let {
                        result[identifier] =
                            ContentProviderBroker(
                                identifier,
                                contentResolver,
                                contentProviderClient,
                                AppCardObserver(handler = null, callback),
                            )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception occurred when trying to connect to content provider: $e")
                }
            }
        )
        return result
    }

    /** @return true if provider info is incorrectly defined */
    private fun checkProviderInfo(providerInfo: ProviderInfo): Boolean {
        val readPermission = providerInfo.readPermission
        if (expectedPermission != readPermission) {
            logIfDebuggable("$providerInfo doesn't have the correct read permission")
            return true
        }

        val writePermission = providerInfo.writePermission
        if (expectedPermission != writePermission) {
            logIfDebuggable("$providerInfo doesn't have the correct write permission")
            return true
        }

        val grantUriPermissions = providerInfo.grantUriPermissions
        if (grantUriPermissions) {
            logIfDebuggable("$providerInfo: grantUriPermissions must be false")
            return true
        }

        val forceUriPermissions = providerInfo.forceUriPermissions
        if (forceUriPermissions) {
            logIfDebuggable("$providerInfo: forceUriPermissions must be false")
            return true
        }

        providerInfo.uriPermissionPatterns?.let {
            logIfDebuggable("$providerInfo: uriPermissionPatterns must be null")
            return true
        }

        return false
    }

    private fun logIfDebuggable(msg: String) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, msg)
    }

    private val compatibleProviderResolveInfos: List<ResolveInfo>
        get() {
            val intent = Intent(APP_CARD_INTENT_ACTION)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentContentProviders(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
                )
            } else {
                packageManager.queryIntentContentProviders(intent, PackageManager.MATCH_ALL)
            }
        }

    companion object {
        private const val TAG = "ContentProviderBrokerProvider"
        internal const val APP_CARD_INTENT_ACTION = "com.android.car.appcard.APP_CARD_PROVIDER"
    }
}
