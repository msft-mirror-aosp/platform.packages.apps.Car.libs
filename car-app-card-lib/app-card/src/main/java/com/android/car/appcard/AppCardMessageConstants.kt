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

/** Contains constants to be used for message identification in [AppCardContentProvider] */
object AppCardMessageConstants {

    /** Send all app cards supported by application */
    const val MSG_SEND_ALL_APP_CARDS = "MSG_SEND_ALL_APP_CARDS"

    /** A new app card has been added */
    const val MSG_APP_CARD_ADDED = "MSG_APP_CARD_ADDED"

    /** An app card has been removed */
    const val MSG_APP_CARD_REMOVED = "MSG_APP_CARD_REMOVED"

    /** An app card update is being requested */
    const val MSG_APP_CARD_UPDATE = "MSG_APP_CARD_UPDATE"

    /** An app card component update is being requested */
    const val MSG_APP_CARD_COMPONENT_UPDATE = "MSG_APP_CARD_COMPONENT_UPDATE"

    /** An app card has been interacted with */
    const val MSG_APP_CARD_INTERACTION = "MSG_APP_CARD_INTERACTION"

    /** An app card's [AppCardContext] has been updated */
    const val MSG_APP_CARD_CONTEXT_UPDATE = "MSG_APP_CARD_CONTEXT_UPDATE"

    /** An app card provider is being closed by host */
    const val MSG_CLOSE_PROVIDER = "MSG_CLOSE_PROVIDER"

    /** Contains constants used to identify the type of interaction that needs to be handled */
    object InteractionMessageConstants {

        /**
         * An app card component's [com.android.car.appcard.component.interaction.OnClickListener]
         * needs to be handled
         */
        const val MSG_INTERACTION_ON_CLICK = "MSG_INTERACTION_ON_CLICK"
    }
}
