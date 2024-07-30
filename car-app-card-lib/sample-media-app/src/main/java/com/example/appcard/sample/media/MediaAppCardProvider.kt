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

package com.example.appcard.sample.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.annotation.Px
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.android.car.appcard.AppCardContext
import com.android.car.appcard.ImageAppCard
import com.android.car.appcard.component.Header
import com.android.car.appcard.component.Image
import com.android.car.apps.common.imaging.ImageBinder
import com.android.car.media.common.MediaItemMetadata
import com.android.car.media.common.source.MediaModels
import com.android.car.media.common.source.MediaSource

/** Supplies the [SimpleAppCardContentProvider] with a now playing [ImageAppCard] */
internal class MediaAppCardProvider(
  private val appCardId: String,
  private val context: Context,
  private val updater: SimpleAppCardContentProvider.AppCardUpdater,
  private val lifecycleOwner: LifecycleOwner,
) {
  private var appCardContext: AppCardContext? = null
  private var maxArtSize: Size? = null
  private var maxHeaderIconSize: Size? = null
  private var albumArtBinder: ImageBinder<MediaItemMetadata.ArtworkRef>? = null
  private var albumImageBitmap: Drawable? = null
  private val mediaModels = MediaModels(context)
  private val mediaSourceObserver = Observer<Any> {
    updateModel()
  }
  private val metadataObserver = Observer<Any> { updateModelMetadata() }
  private var appName: CharSequence? = null
  private var artistName: CharSequence? = null
  private var songTitle: CharSequence? = null
  private var appIcon: Drawable? = null
  private var isRemoved = true

  /** @return an [AppCard] according to given [AppCardContext] */
  fun getAppCard(ctx: AppCardContext?): ImageAppCard {
    ctx?.let { updateAppCardContext(it) }

    // Only setup the observers if app card was previously removed
    if (isRemoved) {
      setupObservers()
      isRemoved = false
    }

    return if (mediaModels.mediaSourceViewModel.primaryMediaSource.value == null) {
      getBlankAppCard()
    } else {
      getActiveAppCard()
    }
  }

  private fun getActiveAppCard(): ImageAppCard {
    if (albumImageBitmap == null || maxArtSize == null || maxHeaderIconSize == null) {
      return getBlankAppCard()
    }

    val image = Image.newBuilder(IMAGE_ID)
      .setColorFilter(Image.ColorFilter.NO_TINT)
      .setContentScale(Image.ContentScale.FIT)
      .setImageData(
        drawableToBitmap(
          albumImageBitmap!!,
          maxArtSize!!.width,
          maxArtSize!!.height
        )
      )
      .build()

    val appImage = Image.newBuilder(HEADER_IMAGE_ID)
      .setColorFilter(Image.ColorFilter.NO_TINT)
      .setContentScale(Image.ContentScale.FILL_BOUNDS)
      .setImageData(
        drawableToBitmap(appIcon!!, maxHeaderIconSize!!.width, maxHeaderIconSize!!.height)
      )
      .build()

    return ImageAppCard.newBuilder(appCardId)
      .setImage(image)
      .setHeader(
        Header.newBuilder(HEADER_ID)
          .setTitle(appName.toString())
          .setImage(appImage)
          .build()
      )
      .setPrimaryText(songTitle.toString())
      .setSecondaryText(artistName.toString())
      .build()
  }

  fun updateAppCardContext(ctx: AppCardContext) {
    appCardContext = ctx

    // Get maximum center image size
    val size = ctx.imageAppCardContext.getMaxImageSize(ImageAppCard::class.java)
    val side = size.width.coerceAtMost(size.height)
    maxArtSize = Size(side, side)

    // Get maximum header image size
    val headerSize = ctx.imageAppCardContext.getMaxImageSize(Header::class.java)
    val headerSide = headerSize.width.coerceAtMost(headerSize.height)
    maxHeaderIconSize = Size(headerSide, headerSide)

    updater.sendUpdate(getActiveAppCard())
  }

  fun appCardRemoved() {
    isRemoved = true
  }

  private fun setupObservers() {
    albumArtBinder = maxArtSize?.let {
      ImageBinder<MediaItemMetadata.ArtworkRef>(
        ImageBinder.PlaceholderType.FOREGROUND, it
      ) { drawable ->
        run {
          albumImageBitmap = drawable
          updater.sendUpdate(getActiveAppCard())
        }
      }
    }

    // Observing using the lifecycle owner must be done on the main thread
    Handler(Looper.getMainLooper()).post {
      mediaModels.mediaSourceViewModel.primaryMediaSource.observe(
        lifecycleOwner,
        mediaSourceObserver
      )
      mediaModels.playbackViewModel.metadata.observe(lifecycleOwner, metadataObserver)
    }
  }

  private fun getBlankAppCard(): ImageAppCard {
    val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_zero_state)
      ?: return ImageAppCard.newBuilder(appCardId)
        .setPrimaryText(BLANK_NO_IMAGE_PRIMARY_TEXT)
        .setPrimaryText(BLANK_SECONDARY_TEXT)
        .build()

    // Get maximum center image size
    val size = appCardContext?.imageAppCardContext?.getMaxImageSize(ImageAppCard::class.java)
    val side = size?.let { Math.min(it.width, it.height) } ?: DEFAULT_IMAGE_SIDE

    val image = Image.newBuilder(IMAGE_ID)
      .setImageData(drawableToBitmap(drawable, side, side))
      .setColorFilter(Image.ColorFilter.NO_TINT)
      .build()

    val builder = ImageAppCard.newBuilder(appCardId)
      .setImage(image)
      .setPrimaryText(BLANK_PRIMARY_TEXT)
      .setSecondaryText(BLANK_SECONDARY_TEXT)

    getBlankAppCardHeader()?.let {
      builder.setHeader(it)
    }

    return builder.build()
  }

  private fun getBlankAppCardHeader(): Header? {
    // Get maximum center header size
    val headerSize = appCardContext?.imageAppCardContext?.getMaxImageSize(Header::class.java)
    val headerSide = headerSize?.let { Math.min(it.width, it.height) } ?: DEFAULT_HEADER_IMAGE_SIDE

    val headerDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_music) ?: return null

    val headerBitmap = drawableToBitmap(headerDrawable, headerSide, headerSide)
    val headerImage = Image.newBuilder(HEADER_IMAGE_ID)
      .setImageData(headerBitmap)
      .setColorFilter(Image.ColorFilter.TINT)
      .setContentScale(Image.ContentScale.FILL_BOUNDS)
      .build()

    return Header.newBuilder(HEADER_ID)
      .setImage(headerImage)
      .build()
  }

  private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
    val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(bitmap)
    val left = 0
    val top = 0
    drawable.setBounds(left, top, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
  }

  private fun updateModel() {
    val mediaSource: MediaSource? = mediaModels.mediaSourceViewModel.primaryMediaSource.value
    if (!mediaSourceChanged()) return

    if (mediaSource != null) {
      if (Log.isLoggable(TAG, Log.INFO)) {
        Log.i(TAG, "Setting Media view to source " + mediaSource.getDisplayName(context))
      }

      appName = mediaSource.getDisplayName(context)
      appIcon = mediaSource.icon
      updater.sendUpdate(getActiveAppCard())
    } else {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "Not resetting media widget for apps that do not support media browse")
      }
    }
  }

  private fun mediaSourceChanged(): Boolean {
    val mediaSource: MediaSource? = mediaModels.mediaSourceViewModel.primaryMediaSource.value
    if (mediaSource == null && (appName != null || appIcon != null)) {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "new media source is null (app name: $appName)")
      }

      return true
    }

    if (mediaSource != null && (appName !== mediaSource.getDisplayName(context) ||
        appIcon !== mediaSource.icon)
    ) {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "new media source is $mediaSource")
      }

      return true
    }
    return false
  }

  private fun updateMetadata() {
    val metadata: MediaItemMetadata? = mediaModels.playbackViewModel.metadata.value
    if (metadata == null) {
      clearMetadata()
    } else {
      songTitle = metadata.title
      artistName = metadata.subtitle
      albumArtBinder?.setImage(context, metadata.artworkKey)
    }
  }

  private fun updateModelMetadata() {
    if (!metadataChanged()) return

    updateMetadata()
    updater.sendUpdate(getAppCard(appCardContext))
  }

  private fun metadataChanged(): Boolean {
    val metadata: MediaItemMetadata? = mediaModels.playbackViewModel.metadata.value
    if (metadata == null && (songTitle != null || artistName != null)) {
      return true
    }

    return metadata != null && (songTitle !== metadata.title || artistName !== metadata.subtitle)
  }

  private fun clearMetadata() {
    songTitle = null
    artistName = null
    val newRef = null
    albumArtBinder?.setImage(context, newRef)
  }

  companion object {
    private const val TAG = "MediaAppCardProvider"
    private const val IMAGE_ID = "image"
    private const val HEADER_ID = "header"
    private const val HEADER_IMAGE_ID = "headerImage"
    private const val BLANK_NO_IMAGE_PRIMARY_TEXT = "No media in session"
    private const val BLANK_PRIMARY_TEXT = "Select Media"
    private const val BLANK_SECONDARY_TEXT = ""
    @Px private const val DEFAULT_HEADER_IMAGE_SIDE = 1000
    @Px private const val DEFAULT_IMAGE_SIDE = 100
  }
}
