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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationIdentifierTest {
    @Test
    fun testEquals_notSameType_returnFalse() {
        val id = ApplicationIdentifier(TEST_PACKAGE_NAME, TEST_AUTHORITY)

        assertThat(id.equals(Any())).isFalse()
    }

    @Test
    fun testEquals_notEqualPackageName_returnFalse() {
        val id = ApplicationIdentifier(TEST_PACKAGE_NAME, TEST_AUTHORITY)
        val other = ApplicationIdentifier(TEST_AUTHORITY, TEST_AUTHORITY)

        assertThat(id.equals(other)).isFalse()
    }

    @Test
    fun testEquals_notEqualAuthority_returnFalse() {
        val id = ApplicationIdentifier(TEST_PACKAGE_NAME, TEST_AUTHORITY)
        val other = ApplicationIdentifier(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)

        assertThat(id.equals(other)).isFalse()
    }

    @Test
    fun testEquals_isEqual_returnTrue() {
        val id = ApplicationIdentifier(TEST_PACKAGE_NAME, TEST_AUTHORITY)
        val other = ApplicationIdentifier(TEST_PACKAGE_NAME, TEST_AUTHORITY)

        assertThat(id.equals(other)).isTrue()
    }

    @Test
    fun testContainsPackage_isEqual_returnTrue() {
        val id = ApplicationIdentifier(TEST_PACKAGE_NAME, TEST_AUTHORITY)

        assertThat(id.containsPackage(TEST_PACKAGE_NAME)).isTrue()
    }

    @Test
    fun testContainsPackage_isNotEqual_returnTrue() {
        val id = ApplicationIdentifier(TEST_PACKAGE_NAME, TEST_AUTHORITY)

        assertThat(id.containsPackage(TEST_AUTHORITY)).isFalse()
    }

    @Test
    fun testContainsAuthority_isEqual_returnTrue() {
        val id = ApplicationIdentifier(TEST_PACKAGE_NAME, TEST_AUTHORITY)

        assertThat(id.containsAuthority(TEST_AUTHORITY)).isTrue()
    }

    @Test
    fun testContainsAuthority_isNotEqual_returnTrue() {
        val id = ApplicationIdentifier(TEST_PACKAGE_NAME, TEST_AUTHORITY)

        assertThat(id.containsAuthority(TEST_PACKAGE_NAME)).isFalse()
    }

    companion object {
        private const val TEST_PACKAGE_NAME = "TEST_PACKAGE_NAME"
        private const val TEST_AUTHORITY = "TEST_AUTHORITY"
    }
}
