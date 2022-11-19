package de.mm20.launcher2.ui.launcher.search.wikipedia

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.mm20.launcher2.search.data.Wikipedia
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.DefaultToolbarAction
import de.mm20.launcher2.ui.component.Toolbar
import de.mm20.launcher2.ui.component.ToolbarAction
import de.mm20.launcher2.ui.ktx.toDp
import de.mm20.launcher2.ui.launcher.sheets.LocalBottomSheetManager
import de.mm20.launcher2.ui.locals.LocalFavoritesEnabled
import de.mm20.launcher2.ui.utils.htmlToAnnotatedString

@Composable
fun WikipediaItem(
    modifier: Modifier = Modifier,
    wikipedia: Wikipedia,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel = remember(wikipedia) { WikipediaItemVM(wikipedia) }

    Column(
        modifier = modifier.clickable {
            viewModel.launch(context)
        }
    ) {
        if (!wikipedia.image.isNullOrEmpty()) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                model = wikipedia.image,
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        }
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = wikipedia.label,
                style = MaterialTheme.typography.titleLarge
            )
            val tags by remember(viewModel) { viewModel.getTags() }.collectAsState(emptyList())
            if (tags.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 1.dp, bottom = 4.dp),
                    text = tags.joinToString(separator = " #", prefix = "#"),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(id = R.string.wikipedia_source),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = htmlToAnnotatedString(wikipedia.text),
                style = MaterialTheme.typography.bodySmall
            )
        }
        val toolbarActions = mutableListOf<ToolbarAction>()

        if (LocalFavoritesEnabled.current) {
            val isPinned by viewModel.isPinned.collectAsState(false)
            val favAction = if (isPinned) {
                DefaultToolbarAction(
                    label = stringResource(R.string.menu_favorites_unpin),
                    icon = Icons.Rounded.Star,
                    action = {
                        viewModel.unpin()
                        onBack?.invoke()
                    }
                )
            } else {
                DefaultToolbarAction(
                    label = stringResource(R.string.menu_favorites_pin),
                    icon = Icons.Rounded.StarOutline,
                    action = {
                        viewModel.pin()
                        onBack?.invoke()
                    })
            }
            toolbarActions.add(favAction)
        }

        toolbarActions.add(
            DefaultToolbarAction(
                label = stringResource(R.string.menu_share),
                icon = Icons.Rounded.Share,
                action = {
                    viewModel.share(context)
                }
            )
        )

        val sheetManager = LocalBottomSheetManager.current
        toolbarActions.add(DefaultToolbarAction(
            label = stringResource(R.string.menu_customize),
            icon = Icons.Rounded.Edit,
            action = { sheetManager.showCustomizeSearchableModal(wikipedia) }
        ))

        Toolbar(
            leftActions = if (onBack != null) listOf(
                DefaultToolbarAction(
                    stringResource(id = R.string.menu_back),
                    icon = Icons.Rounded.ArrowBack,
                    action = onBack
                )
            ) else emptyList(),
            rightActions = toolbarActions
        )
    }
}

@Composable
fun WikipediaItemGridPopup(
    wikipedia: Wikipedia,
    show: Boolean,
    animationProgress: Float,
    origin: Rect,
    onDismiss: () -> Unit
) {
    AnimatedContent(
        targetState = show,
        transitionSpec = {
            fadeIn(tween(200)) with
                    fadeOut(tween(200, 200)) using
                    SizeTransform { _, _ ->
                        tween(300)
                    }
        }
    ) { targetState ->
        if (targetState) {
            WikipediaItem(
                modifier = Modifier
                    .fillMaxWidth(),
                wikipedia = wikipedia,
                onBack = onDismiss
            )
        } else {
            Box(
                modifier = Modifier
                    .requiredWidth(origin.width.toDp())
                    .requiredHeight(origin.height.toDp())
            )
        }
    }
}