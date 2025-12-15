package com.achub.hram.screen.activities

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen8
import com.achub.hram.view.ActivityCard
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

@Composable
fun ActivitiesScreen() {
    val viewModel = koinViewModel<ActivitiesViewModel>()
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val haptic = LocalHapticFeedback.current
    val selectedIds = state.selectedActivitiesId
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .align(Center)
                .padding(Dimen8),
            verticalArrangement = Arrangement.spacedBy(Dimen8)
        ) {
            items(items = state.activities, key = { it.activity.id }) { activityInfo ->
                val id = activityInfo.activity.id
                ActivityCard(
                    modifier = Modifier.combinedClickable(
                        indication = null,
                        interactionSource = MutableInteractionSource(),
                        onLongClick = {
                            viewModel.onActivityLongClick(id)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onClick = { viewModel.onHighlighted(null) }
                    ),
                    selected = selectedIds.contains(id),
                    selectionEnabled = selectedIds.isNotEmpty(),
                    activityInfo = activityInfo,
                    highLighted = state.highlightedItem,
                    onHighlighted = { viewModel.onHighlighted(it) }
                )
            }
        }
        if (selectedIds.isNotEmpty()) {
            Toolbar(
                selectedIds = selectedIds,
                onDelete = { viewModel.showActivityDeletionDialog() },
                onEdit = { viewModel.showNameActivityDialog() }
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
fun BoxScope.Toolbar(selectedIds: Set<String>, onDelete: () -> Unit = {}, onEdit: () -> Unit = {}) {
    FloatingToolbar(
        modifier = Modifier.align(Alignment.BottomEnd)
            .padding(Dimen16),
        selected = selectedIds
    ) { opt ->
        when (opt) {
            ActivityOptions.DELETE -> onDelete()
            ActivityOptions.EDIT -> onEdit()
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
                    onButonClick = { onActivityNameConfirmed() }
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
                    onButonClick = {
                        onDeleteActivities()
                        onDismiss()
                    }
                )
            }
        }
    }
}
