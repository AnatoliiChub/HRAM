package com.achub.hram.screen.activities

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.ext.getPlatform
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen216
import com.achub.hram.style.Dimen48
import com.achub.hram.style.Dimen8
import com.achub.hram.view.cards.ActivityCard
import com.achub.hram.view.components.ActivityOptions
import com.achub.hram.view.components.FloatingToolbar
import com.achub.hram.view.dialogs.InfoDialog
import com.achub.hram.view.dialogs.NameActivityDialog
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.dialog_activity_deletion_button_text
import hram.composeapp.generated.resources.dialog_activity_deletion_message
import hram.composeapp.generated.resources.dialog_activity_deletion_title
import hram.composeapp.generated.resources.dialog_rename_activity_message
import hram.composeapp.generated.resources.dialog_rename_activity_title
import hram.composeapp.generated.resources.text_activities
import hram.composeapp.generated.resources.text_activity
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivitiesScreen(onListUpdated: (Boolean) -> Unit = {}) {
    val viewModel = koinViewModel<ActivitiesViewModel>()
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val haptic = LocalHapticFeedback.current
    val selectedIds = state.selectedActivitiesId
    val isDesktop = getPlatform().isDesktop()

    val gridState = rememberLazyGridState()
    val loadMore = remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            lastVisibleItemIndex > (totalItemsNumber - 2) && totalItemsNumber > 0
        }
    }

    LaunchedEffect(loadMore.value) {
        if (loadMore.value) viewModel.loadMore()
    }

    LaunchedEffect(state.activities) {
        onListUpdated(state.activities.isNotEmpty())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = if (isDesktop) GridCells.Fixed(2) else GridCells.Fixed(1),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimen8),
            contentPadding = if (isDesktop) PaddingValues(bottom = Dimen216) else PaddingValues(),
            verticalArrangement = Arrangement.spacedBy(Dimen8),
            horizontalArrangement = Arrangement.spacedBy(Dimen8)
        ) {
            items(items = state.activities, key = { it.id }) { activityInfo ->
                val id = activityInfo.id
                if (activityInfo.buckets.isNotEmpty()) {
                    ActivityCard(
                        modifier = Modifier.combinedClickable(
                            indication = null,
                            interactionSource = MutableInteractionSource(),
                            onLongClick = {
                                viewModel.toggleSelection(id)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onClick = {
                                viewModel.onHighlighted(null)
                                if (state.isSelectionMode) viewModel.toggleSelection(id)
                            }
                        ),
                        selected = selectedIds.contains(id),
                        selectionEnabled = state.isSelectionMode,
                        activityInfo = activityInfo,
                        highLighted = state.highlightedItem,
                        onHighlighted = { viewModel.onHighlighted(it) }
                    )
                }
            }

            if (state.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.size(Dimen48),
                            trackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        if (selectedIds.isNotEmpty()) {
            Toolbar(
                selectedIds = selectedIds,
                onDelete = { viewModel.showActivityDeletionDialog() },
                onEdit = { viewModel.showNameActivityDialog() },
                onExport = { selectedIds.firstOrNull()?.let { viewModel.exportActivity(it) } }
            )
        }
    }
    val dialog = state.dialog
    Dialog(
        dialog = dialog,
        state = state,
        onDismiss = viewModel::dismissDialog,
        onActivityNameChanged = viewModel::onActivityNameChanged,
        onActivityNameConfirmed = viewModel::onActivityNameConfirmed,
        onDeleteActivities = { viewModel.deleteActivities(state.selectedActivitiesId) }
    )
}

@Composable
fun BoxScope.Toolbar(
    selectedIds: Set<String>,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    onExport: () -> Unit = {}
) {
    FloatingToolbar(
        modifier = Modifier.align(if (getPlatform().isDesktop()) Alignment.TopStart else Alignment.BottomEnd)
            .padding(Dimen16),
        selected = selectedIds
    ) { opt ->
        when (opt) {
            ActivityOptions.DELETE -> onDelete()
            ActivityOptions.EDIT -> onEdit()
            ActivityOptions.EXPORT -> onExport()
        }
    }
}

@Composable
private fun Dialog(
    dialog: ActivitiesScreenDialog?,
    state: ActivitiesUiState,
    onDismiss: () -> Unit,
    onActivityNameChanged: (String) -> Unit,
    onActivityNameConfirmed: () -> Unit,
    onDeleteActivities: () -> Unit
) {
    if (dialog != null) {
        when (dialog) {
            is ActivitiesScreenDialog.ReNameActivity -> {
                NameActivityDialog(
                    title = Res.string.dialog_rename_activity_title,
                    message = Res.string.dialog_rename_activity_message,
                    name = dialog.activityName,
                    error = dialog.error,
                    dismissable = true,
                    onNameChanged = onActivityNameChanged,
                    onDismiss = onDismiss,
                    onButtonClick = { onActivityNameConfirmed() }
                )
            }

            is ActivitiesScreenDialog.ActivityDeletionDialog -> {
                val messageParam = if (state.selectedActivitiesId.size > 1) {
                    Res.string.text_activities
                } else {
                    Res.string.text_activity
                }
                InfoDialog(
                    title = Res.string.dialog_activity_deletion_title,
                    message = stringResource(
                        Res.string.dialog_activity_deletion_message,
                        stringResource(messageParam)
                    ),
                    buttonText = Res.string.dialog_activity_deletion_button_text,
                    onDismiss = onDismiss,
                    onButtonClick = {
                        onDeleteActivities()
                        onDismiss()
                    }
                )
            }
        }
    }
}
