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

/** An object containing an app card application's package name & authority */
class ApplicationIdentifier(val packageName: String, val authority: String) {
    override fun equals(other: Any?): Boolean {
        if (other !is ApplicationIdentifier) return false

        return packageName == other.packageName && authority == other.authority
    }

    /** @return `true` if given package name matches package name inside [ApplicationIdentifier] */
    fun containsPackage(packageName: String) = this.packageName == packageName

    /** @return `true` if given authority matches authority inside [ApplicationIdentifier] */
    fun containsAuthority(authority: String) = this.authority == authority

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + authority.hashCode()
        return result
    }

    override fun toString() = "$authority : $packageName"
}
