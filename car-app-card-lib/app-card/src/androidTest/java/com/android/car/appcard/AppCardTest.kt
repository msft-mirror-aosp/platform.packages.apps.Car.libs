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
package com.android.car.appcard

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.car.appcard.component.Component
import com.android.car.appcard.component.ProgressBar.Companion.newBuilder
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppCardTest {
    private lateinit var appCard: AppCard

    @Before
    fun setUp() {
        appCard = TestAppCard(id = "")
    }

    @Test
    fun testSetId_nonNull() {
        appCard = TestAppCard(ID)

        assertThat(appCard.id).isEqualTo(ID)
    }

    @Test
    fun testEquals_sameObject_returnTrue() {
        appCard = TestAppCard(ID)

        assertThat(appCard.equals(appCard)).isTrue()
    }

    @Test
    fun testEquals_allFieldsEqual_returnTrue() {
        assertThat(TestAppCard(ID) == TestAppCard(ID)).isTrue()
    }

    @Test
    fun testEquals_idNotEqual_returnFalse() {
        assertThat(TestAppCard(ID) == TestAppCard(id = ID + ID)).isFalse()
    }

    @Test
    fun testEquals_differentClass_returnFalse() {
        val progressBar = newBuilder(ID, min = 0, max = 1).build()

        assertThat(TestAppCard(ID).equals(progressBar)).isFalse()
    }

    private class TestAppCard(id: String) : AppCard(id) {
        override fun toByteArray(): ByteArray = ByteArray(size = 0)

        override fun verifyUniquenessOfComponentIds(): Boolean = false

        override fun updateComponent(component: Component) {}
    }

    companion object {
        private const val ID = "ID"
    }
}
