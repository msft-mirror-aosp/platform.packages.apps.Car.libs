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

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.drawable.toBitmap
import com.android.car.appcard.AppCardMessageConstants.InteractionMessageConstants.MSG_INTERACTION_ON_CLICK
import com.android.car.appcard.ImageAppCard
import com.android.car.appcard.component.Button
import com.android.car.appcard.component.Component
import com.android.car.appcard.component.Header
import com.android.car.appcard.component.Image
import com.android.car.appcard.component.ProgressBar
import com.android.car.appcard.host.AppCardComponentContainer
import com.android.car.appcard.host.AppCardContainer
import com.android.car.appcard.host.ApplicationIdentifier

/** Jetpack compose specific stateful representation of [ImageAppCard] */
class ImageAppCardContainerState(
  appCardContainer: AppCardContainer,
  private val packageManager: PackageManager,
  private val viewModel: HostViewModel,
) : AppCardContainerState {
  override var identifier: MutableState<ApplicationIdentifier> =
    mutableStateOf(appCardContainer.appId)

  override val appCardId: String
    get() = imageAppCard.id

  private var imageAppCard = appCardContainer.appCard as ImageAppCard
  private var primaryText: MutableState<String?> =
    mutableStateOf(imageAppCard.primaryText)
  private var secondaryText: MutableState<String?> =
    mutableStateOf(imageAppCard.secondaryText)
  private var image: MutableState<Image?> = mutableStateOf(imageAppCard.image)
  private var header: MutableState<Header?> = mutableStateOf(imageAppCard.header)
  private var progressBar: MutableState<ProgressBar?> =
    mutableStateOf(imageAppCard.progressBar)
  private var buttons = imageAppCard.buttons.map { button ->
    mutableStateOf(button)
  }.toMutableStateList()

  override fun update(other: AppCardContainer): Boolean {
    if (identifier.value != other.appId) {
      viewModel.logIfDebuggable(
        TAG,
        msg = "Unable to update App Card: Application identifier mismatch"
      )
      return false
    }
    if (imageAppCard.id != other.appCard.id) {
      viewModel.logIfDebuggable(
        TAG,
        msg = "Unable to update App Card: App card ID mismatch"
      )
      return false
    }

    imageAppCard = other.appCard as ImageAppCard
    primaryText.value = imageAppCard.primaryText
    secondaryText.value = imageAppCard.secondaryText
    image.value = imageAppCard.image
    header.value = imageAppCard.header
    progressBar.value = imageAppCard.progressBar

    buttons.clear()
    buttons.addAll(imageAppCard.buttons.map { button -> mutableStateOf(button) })

    return true
  }

  override fun update(other: AppCardComponentContainer): Boolean {
    if (identifier.value != other.appId) {
      viewModel.logIfDebuggable(
        TAG,
        msg = "Unable to update App Card Component: Application identifier mismatch"
      )
      return false
    }
    if (imageAppCard.id != other.appCardId) {
      viewModel.logIfDebuggable(
        TAG,
        msg = "Unable to update App Card Component: App Card ID mismatch"
      )
      return false
    }

    return update(other.component)
  }

  override fun update(component: Component): Boolean {
    if (component is Image) {
      var orig: Image? = null
      image.value?.let {
        orig = Image.fromMessage(it.toMessage())
      }
      orig?.let {
        if (it != component && it.updateComponent(component)) {
          image.value = it
          return true
        }
      }
    }

    if (component is Button || component is Image) {
      var orig: Button? = null
      var matchedIndex = -1
      buttons.forEachIndexed { index, button ->
        orig = Button.fromMessage(button.value.toMessage())
        orig?.let {
          if (it != component && it.updateComponent(component)) {
            matchedIndex = index
            return@forEachIndexed
          }
        }
      }
      if (matchedIndex != -1) {
        orig?.let {
          buttons[matchedIndex].value = it
        }
        return true
      }
    }

    if (component is Header) {
      var orig: Header? = null
      header.value?.let {
        orig = Header.fromMessage(it.toMessage())
      }
      orig?.let {
        if (it != component && it.updateComponent(component)) {
          header.value = it
          return true
        }
      }
    }

    if (component is ProgressBar) {
      var orig: ProgressBar? = null
      progressBar.value?.let {
        orig = ProgressBar.fromMessage(it.toMessage())
      }
      orig?.let {
        if (it != component && it.updateComponent(component)) {
          progressBar.value = it
          return true
        }
      }
    }

    viewModel.logIfDebuggable(
      TAG,
      msg = "Unable to update App Card Component: Component update conditions not met"
    )
    return false
  }

  @Composable
  override fun AppCard() {
    val logMsg = "createAppCard: $identifier + ${imageAppCard.id}"
    viewModel.logIfDebuggable(TAG, logMsg)

    ElevatedCard(
      elevation = CardDefaults.cardElevation(
        defaultElevation = dimensionResource(R.dimen.card_elevation)
      ),
      modifier = Modifier
        .size(
          width = dimensionResource(R.dimen.card_width),
          height = dimensionResource(R.dimen.card_height)
        )
        .padding(dimensionResource(R.dimen.card_padding)),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primary.copy(
          alpha = SURFACE_3_PRIMARY_ALPHA
        ).compositeOver(MaterialTheme.colorScheme.surface)
      ),
      shape = MaterialTheme.shapes.medium
    ) {
      val header = remember { header }
      val image = remember { image }
      val primaryText = remember { primaryText }
      val secondaryText = remember { secondaryText }
      val buttons = remember { buttons }
      val buttonsExist = buttons.size > 0
      val imageExists = image.value != null
      val progressBarExists = progressBar.value != null

      Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.size(
          width = dimensionResource(R.dimen.card_width),
          height = dimensionResource(R.dimen.card_height)
        )
      ) {
        CreateHeader(
          imageAppCard.id,
          identifier.value,
          header.value,
          identifier.value.packageName
        )

        image.value?.let {
          Box(
            modifier = Modifier
              .weight(floatResource(R.dimen.image_box_weight))
              .padding(all = dimensionResource(R.dimen.image_padding))
          ) {
            GetImage(
              imageAppCard.id,
              identifier.value,
              it
            )
          }
        }

        Column(
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.width(dimensionResource(R.dimen.card_width))
        ) {
          primaryText.value?.let {
            Text(
              modifier = Modifier
                .fillMaxWidth()
                .padding(
                  start = dimensionResource(R.dimen.primary_text_horizontal_padding),
                  end = dimensionResource(R.dimen.primary_text_horizontal_padding),
                  bottom = dimensionResource(R.dimen.primary_text_bottom_padding)
                ),
              style = MaterialTheme.typography.headlineLarge,
              text = it,
              color = MaterialTheme.colorScheme.onBackground,
              maxLines = integerResource(R.integer.primary_text_max_lines),
              overflow = TextOverflow.Ellipsis,
              textAlign = TextAlign.Start
            )
          }

          secondaryText.value?.let {
            Text(
              modifier = Modifier
                .fillMaxWidth()
                .padding(
                  start = dimensionResource(R.dimen.secondary_text_horizontal_padding),
                  end = dimensionResource(R.dimen.secondary_text_horizontal_padding),
                  bottom = dimensionResource(R.dimen.secondary_text_bottom_padding)
                ),
              style = MaterialTheme.typography.titleLarge,
              text = it,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = integerResource(R.integer.secondary_text_max_lines),
              overflow = TextOverflow.Ellipsis,
              textAlign = TextAlign.Start
            )
          }

          if (progressBarExists) {
            CreateProgressBar(
              imageAppCard.id,
              identifier.value,
              progressBar
            )
          } else if (buttonsExist) {
            HorizontalDivider(
              modifier = Modifier
                .fillMaxWidth()
                .padding(
                  start = dimensionResource(R.dimen.progressbar_horizontal_padding),
                  end = dimensionResource(R.dimen.progressbar_horizontal_padding),
                  top = dimensionResource(R.dimen.progressbar_top_padding),
                  bottom = dimensionResource(R.dimen.progressbar_bottom_padding)
                )
            )
          }

          if (buttonsExist) {
            CreateButtonRow(mutableStateOf(buttons.size))
          } else if (!imageExists && progressBarExists) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height))
            ) {}
          }
        }
      }
    }
  }

  @Composable
  private fun CreateButtonRow(numButtons: MutableState<Int>) {
    viewModel.logIfDebuggable(
      TAG,
      msg = "createButtonRow: $identifier + $appCardId + ${buttons.size}"
    )

    Row(
      horizontalArrangement = Arrangement.SpaceAround,
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          bottom = dimensionResource(R.dimen.button_row_padding)
        ),
    ) {
      buttons.forEachIndexed { index, button ->
        if (index == 0) {
          Spacer(modifier = Modifier.width(dimensionResource(R.dimen.button_row_padding)))
        }

        Box(
          modifier = Modifier.weight(
            floatResource(R.dimen.button_box_weight) / numButtons.value.toFloat()
          )
        ) {
          CreateButton(button, numButtons)
        }

        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.button_row_padding)))
      }
    }
  }

  @Composable
  private fun CreateButton(button: MutableState<Button>, numButtons: MutableState<Int>) {
    viewModel.logIfDebuggable(
      TAG,
      msg = "createButton: $identifier + $appCardId + ${button.value.componentId}"
    )

    val onClick = {
      viewModel.logIfDebuggable(
        TAG,
        msg = "clicked: $identifier + $appCardId + ${button.value.componentId}"
      )

      viewModel.sendInteraction(
        identifier.value,
        appCardId,
        button.value.componentId,
        MSG_INTERACTION_ON_CLICK
      )
    }

    var colorFilter: ColorFilter? = null

    button.value.image?.let {
      if (it.colorFilter == Image.ColorFilter.TINT) {
        colorFilter = if (button.value.buttonType == Button.ButtonType.PRIMARY) {
          ColorFilter.tint(
            color = MaterialTheme.colorScheme.onPrimary,
            blendMode = BlendMode.SrcIn
          )
        } else {
          ColorFilter.tint(
            color = MaterialTheme.colorScheme.secondary,
            blendMode = BlendMode.SrcIn
          )
        }
      }
    }

    if (button.value.buttonType == Button.ButtonType.NO_BACKGROUND) {
      TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(),
        modifier = Modifier
          .height(dimensionResource(R.dimen.button_height))
          .fillMaxWidth(),
      ) {
        button.value.image?.imageData?.let {
          Image(
            bitmap = it.asImageBitmap(),
            contentDescription = stringResource(R.string.button_icon_content_desc),
            modifier = Modifier.size(
              width = dimensionResource(R.dimen.card_button_icon_size),
              height = dimensionResource(R.dimen.card_button_icon_size),
            ),
            contentScale = ContentScale.Fit,
            colorFilter = colorFilter,
          )
        }
      }
      return
    }

    val tooManyButtonsForText =
      numButtons.value > integerResource(R.integer.number_buttons_to_show_text_with_image)
    val shouldShowText = !tooManyButtonsForText || button.value.image?.imageData == null
    val contentPadding = if (shouldShowText) {
      PaddingValues(horizontal = dimensionResource(R.dimen.button_content_padding))
    } else {
      PaddingValues()
    }

    Button(
      onClick = onClick,
      colors = getButtonStyling(button.value.buttonType),
      contentPadding = contentPadding,
      modifier = Modifier
        .height(dimensionResource(R.dimen.button_height))
        .fillMaxWidth(),
    ) {
      button.value.image?.imageData?.let {
        Image(
          bitmap = it.asImageBitmap(),
          contentDescription = stringResource(R.string.button_icon_content_desc),
          modifier = Modifier.size(
            width = dimensionResource(R.dimen.card_button_icon_size),
            height = dimensionResource(R.dimen.card_button_icon_size),
          ),
          contentScale = ContentScale.Fit,
          colorFilter = colorFilter,
        )
      }

      if (shouldShowText) {
        button.value.text?.let {
          val maxLines = integerResource(R.integer.button_text_max_lines)

          Text(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.CenterVertically),
            text = it,
            style = MaterialTheme.typography.titleLarge,
            color = if (button.value.buttonType == Button.ButtonType.PRIMARY) {
              MaterialTheme.colorScheme.onPrimary
            } else {
              MaterialTheme.colorScheme.secondary
            },
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }

  @Composable
  private fun getButtonStyling(type: Button.ButtonType): ButtonColors {
    val backgroundColor = if (type == Button.ButtonType.PRIMARY) {
      MaterialTheme.colorScheme.primary
    } else {
      MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = if (type == Button.ButtonType.PRIMARY) {
      MaterialTheme.colorScheme.onPrimary
    } else {
      MaterialTheme.colorScheme.secondary
    }

    return ButtonDefaults.buttonColors(
      containerColor = backgroundColor,
      contentColor = contentColor,
      disabledContainerColor = backgroundColor,
      disabledContentColor = contentColor,
    )
  }

  @Composable
  fun CreateProgressBar(
    appCardId: String,
    identifier: ApplicationIdentifier,
    progressBarState: MutableState<ProgressBar?>,
  ) {
    val progressBar = progressBarState.value ?: return
    val componentId = progressBarState.value?.componentId
    viewModel.logIfDebuggable(
      TAG,
      msg = "createProgressBar: $identifier + $appCardId + $componentId"
    )

    val current = progressBar.progress.toFloat() - progressBar.min.toFloat()
    val max = progressBar.max.toFloat() - progressBar.min.toFloat()
    val progress = current / max

    LinearProgressIndicator(
      progress = { progress },
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          start = dimensionResource(R.dimen.progressbar_horizontal_padding),
          end = dimensionResource(R.dimen.progressbar_horizontal_padding),
          top = dimensionResource(R.dimen.progressbar_top_padding),
          bottom = dimensionResource(R.dimen.progressbar_bottom_padding)
        )
        .height(dimensionResource(id = R.dimen.progress_bar_track_height)),
    )
  }

  @Composable
  private fun CreateHeader(
    appCardId: String,
    identifier: ApplicationIdentifier,
    header: Header?,
    packageName: String,
  ) {
    viewModel.logIfDebuggable(
      TAG,
      msg = "createAppCard: $identifier + $appCardId + ${header?.componentId}"
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          start = dimensionResource(R.dimen.header_padding),
          end = dimensionResource(R.dimen.header_padding),
          top = dimensionResource(R.dimen.header_padding)
        ),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val text = header?.title ?: ""
      Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.weight(floatResource(R.dimen.header_text_weight)),
        style = MaterialTheme.typography.titleLarge,
        maxLines = integerResource(R.integer.header_text_max_lines),
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Start
      )

      header?.logo?.let {
        it.imageData?.let { bitmap ->
          Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.app_icon_content_desc),
            modifier = Modifier.size(dimensionResource(R.dimen.card_header_icon_size)),
            contentScale = ContentScale.Fit,
            colorFilter = if (it.colorFilter == Image.ColorFilter.TINT) {
              ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
            } else {
              null
            }
          )
        }
      } ?: GetAppIcon(packageName)
    }
  }

  @Composable
  private fun GetAppIcon(packageName: String) {
    packageManager?.getApplicationIcon(packageName)?.toBitmap()?.asImageBitmap()?.let {
      Image(
        bitmap = it,
        contentDescription = stringResource(R.string.app_icon_content_desc),
        modifier = Modifier.size(dimensionResource(R.dimen.card_header_icon_size)),
        contentScale = ContentScale.Fit
      )
    }
  }

  @Composable
  private fun GetImage(
    appCardId: String,
    identifier: ApplicationIdentifier,
    image: Image,
  ) {
    viewModel.logIfDebuggable(
      TAG,
      msg = "getImage: $identifier + $appCardId + ${image.componentId}"
    )

    image.imageData?.asImageBitmap()?.let { bitmap ->
      Image(
        bitmap = bitmap,
        contentDescription = stringResource(R.string.image_content_desc),
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        colorFilter = if (image.colorFilter == Image.ColorFilter.TINT) {
          ColorFilter.tint(
            color = MaterialTheme.colorScheme.primary
          )
        } else {
          null
        }
      )
    }
  }

  companion object {
    private const val TAG = "ImageAppCardContainerState"
    private const val SURFACE_3_PRIMARY_ALPHA = 0.11f

    @Composable
    private fun floatResource(resId: Int): Float = LocalContext.current.resources.getFloat(resId)
  }
}
