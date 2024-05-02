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

package com.example.appcard.samplehost

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.car.appcard.AppCard
import com.android.car.appcard.AppCardContentProvider
import com.android.car.appcard.AppCardContext
import com.android.car.appcard.host.AppCardComponentContainer
import com.android.car.appcard.host.AppCardContainer
import com.android.car.appcard.host.AppCardHost
import com.android.car.appcard.host.AppCardListener
import com.android.car.appcard.host.ApplicationIdentifier

/** An [AndroidViewModel] that registers itself as an [AppCardListener] */
class HostViewModel(application: Application) : AndroidViewModel(application), AppCardListener {
  private lateinit var appCardHost: AppCardHost
  private lateinit var stateFactory: AppCardContainerStateFactory

  /**
   * Keep track of the latest [AppCardContext] (if all [AppCard]s will have the same context)
   * or associate each [AppCard] with its own [AppCardContext]
   */
  private lateinit var cardContext: AppCardContext

  val allAppCards = mutableStateMapOf<String, AppCardContainerState>()
  val isDebuggable = MutableLiveData(true)
  var appCardContextState: AppCardContextState? = null

  fun setAppCardHost(host: AppCardHost) {
    appCardHost = host
    // Register view model as [AppCardListener] so that changes to app cards can
    // propagate to all views that use this view model
    appCardHost.registerListener(listener = this)
  }

  fun setStateFactory(factory: AppCardContainerStateFactory) {
    stateFactory = factory
  }

  /**
   * A new [AppCardContainer] has been received, either update an existing view
   * or create a new view representing the [AppCardContainer]
   */
  override fun onAppCardReceived(appCard: AppCardContainer) {
    logIfDebuggable(msg = "onAppCardReceived: $appCard")

    synchronized(lock = allAppCards) {
      var updated = false
      val curAppCard = allAppCards[getKey(appCard)]

      curAppCard?.let {
        if (it.update(appCard)) {
          allAppCards[getKey(appCard)] = it
          updated = true
        }
      }

      if (!updated) {
        stateFactory.getState(appCard)?.let {
          allAppCards.put(getKey(appCard), it)
        }
      }
    }
  }

  private fun getKey(appCard: AppCardContainer) = "${appCard.appId} + ${appCard.appCard.id}"

  private fun getKey(component: AppCardComponentContainer) =
    "${component.appId} + ${component.appCardId}"

  /** A new [AppCardComponentContainer] has been received, update an existing view */
  override fun onComponentReceived(component: AppCardComponentContainer) {
    logIfDebuggable(msg = "onComponentReceived: $component")

    synchronized(lock = allAppCards) {
      allAppCards[getKey(component)]?.update(component)
    }
  }

  /** A package has been removed from the system, remove all associated views */
  override fun onProviderRemoved(packageName: String, authority: String?) {
    logIfDebuggable(msg = "onPackageRemoved: $packageName")

    synchronized(lock = allAppCards) {
      val keys = allAppCards.keys
      keys.forEach {
        val identifier = allAppCards[it]?.identifier?.value
        val isSamePackage = identifier?.containsPackage(packageName) ?: false

        var isSameAuthority = true
        authority?.let {
          isSameAuthority = identifier?.containsAuthority(authority) ?: false
        }

        if (isSamePackage && isSameAuthority) {
          allAppCards.remove(it)
        }
      }
    }
  }

  /** A package has been added tp the system, refresh [AppCard]s if needed */
  override fun onProviderAdded(packageName: String, authority: String?) {
    logIfDebuggable(msg = "onPackageAdded: $packageName")

    appCardHost.getAllAppCards(cardContext)
  }

  /**
   * An error has occurred when communicating with an [AppCardContentProvider]
   * Handle error
   */
  override fun onPackageCommunicationError(
    identifier: ApplicationIdentifier,
    throwable: Throwable,
  ) = logIfDebuggable(msg = "onPackageCommunicationError: $identifier $throwable")

  /**
   * Ask [AppCardHost] to connect to new [AppCardContentProvider] and then retrieve new [AppCard]s
   */
  fun refresh(appCardContext: AppCardContext) {
    cardContext = appCardContext
    logIfDebuggable(msg = "refresh: $appCardContext")

    appCardHost.refreshCompatibleApplication()

    appCardHost.getAllAppCards(appCardContext)
  }

  /** Remove all [AppCard]s */
  fun onStop() {
    logIfDebuggable(msg = "onStop()")

    removeAllAppCards()
  }

  /** Remove all [AppCard]s then destroy [AppCardHost] */
  fun onDestroy() {
    logIfDebuggable(msg = "onDestroy()")

    appCardHost.destroy()
  }

  private fun removeAllAppCards() {
    synchronized(lock = allAppCards) {
      allAppCards.forEach {
        appCardHost.notifyAppCardRemoved(
          it.value.identifier.value,
          it.value.appCardId
        )
      }

      allAppCards.clear()
    }
  }

  /** Notify [AppCardHost] that a component inside [AppCard] has been interacted with */
  fun sendInteraction(
    identifier: ApplicationIdentifier,
    appCardId: String,
    componentId: String,
    interactionType: String,
  ) = appCardHost.notifyAppCardInteraction(identifier, appCardId, componentId, interactionType)

  /** Update each [AppCard] with new [AppCardContext] */
  fun sendContextUpdate(state: AppCardContextState) {
    synchronized(lock = allAppCards) {
      appCardContextState = state
      cardContext = state.toAppCardContext()

      allAppCards.forEach {
        val identifier = it.value.identifier.value
        val appCardId = it.value.appCardId

        appCardHost.sendAppCardContextUpdate(cardContext, identifier, appCardId)
      }
    }
  }

  private fun logIfDebuggable(msg: String) = logIfDebuggable(TAG, msg)

  fun logIfDebuggable(tag: String, msg: String) {
    if (isDebuggable.value!!) {
      Log.d(tag, msg)
    }
  }

  companion object {
    private const val TAG = "HostViewModel"
  }
}
