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

import android.net.Uri
import android.os.Handler
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class AppCardObserverTest {
  private var observer: AppCardObserver? = null
  private var actualAuthority: String? = null
  private var actualId: String? = null
  private var actualComponentId: String? = null

  private val callback = object : AppCardObserver.AppCardObserverCallback {
    override fun handleAppCardAppComponentRequest(
      authority: String,
      id: String,
      componentId: String
    ) {
      actualAuthority = authority
      actualId = id
      actualComponentId = componentId
    }
  }

  private val handler: Handler = mock<Handler> { it ->
    doAnswer { inv ->
      (inv.arguments[0] as Runnable).run()
    }.whenever(it).post(any<Runnable>())
  }

  @Test
  fun testOnChange_nullUri_callbackNotCalled() {
    observer = AppCardObserver(handler, callback)

    observer?.onChange(false, null)

    assertThat(actualAuthority).isNull()
  }

  @Test
  fun testOnChange_nullAuthority_callbackNotCalled() {
    observer = AppCardObserver(handler, callback)
    val uri = Uri.Builder().build()

    observer?.onChange(false, uri)

    assertThat(actualAuthority).isNull()
  }

  @Test
  fun testOnChange_onePathSegment_callbackNotCalled() {
    observer = AppCardObserver(handler, callback)
    val uri = Uri.Builder()
      .authority(TEST_AUTHORITY)
      .appendPath(TEST_ID)
      .build()

    observer?.onChange(false, uri)

    assertThat(actualAuthority).isNull()
  }

  @Test
  fun testOnChange_threePathSegment_callbackNotCalled() {
    observer = AppCardObserver(handler, callback)
    val uri = Uri.Builder()
      .authority(TEST_AUTHORITY)
      .appendPath(TEST_AUTHORITY)
      .appendPath(TEST_ID)
      .appendPath(TEST_COMPONENT_ID)
      .build()

    observer?.onChange(false, uri)

    assertThat(actualAuthority).isNull()
  }

  @Test
  fun testOnChange_twoPathSegments_callbackCalled() {
    observer = AppCardObserver(handler, callback)
    val uri = Uri.Builder()
      .authority(TEST_AUTHORITY)
      .appendPath(TEST_ID)
      .appendPath(TEST_COMPONENT_ID)
      .build()

    observer?.onChange(false, uri)

    assertThat(actualAuthority).isEqualTo(TEST_AUTHORITY)
    assertThat(actualId).isEqualTo(TEST_ID)
    assertThat(actualComponentId).isEqualTo(TEST_COMPONENT_ID)
  }

  companion object {
    private const val TEST_AUTHORITY = "TEST_AUTHORITY"
    private const val TEST_ID = "TEST_ID"
    private const val TEST_COMPONENT_ID = "TEST_COMPONENT_ID"
  }
}
