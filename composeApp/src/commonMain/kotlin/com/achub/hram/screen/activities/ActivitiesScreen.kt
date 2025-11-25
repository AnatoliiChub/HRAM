package com.achub.hram.screen.activities

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.achub.hram.view.ActivityOptions
import com.achub.hram.view.FloatingToolbar
import com.achub.hram.view.dialogs.InfoDialog
import com.achub.hram.view.dialogs.NameActivityDialog
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ActivitiesScreen() {
    val viewModel = koinViewModel<ActivitiesViewModel>()
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val haptic = LocalHapticFeedback.current
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .align(Center)
                .padding(Dimen8),
            verticalArrangement = Arrangement.spacedBy(Dimen8)
        ) {
            items(items = state.list, key = { it.activity.id }) { activityInfo ->
                val id = activityInfo.activity.id
                ActivityCard(
                    modifier = Modifier.combinedClickable(
                        indication = null,
                        interactionSource = MutableInteractionSource(),
                        onLongClick = {
                            viewModel.onHighlighted(null)
                            viewModel.selectActivity(id)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onClick = { viewModel.onHighlighted(null) }),
                    selected = state.selectedActivitiesId.contains(id),
                    selectionEnabled = state.selectedActivitiesId.isNotEmpty(),
                    activityInfo = activityInfo,
                    highLighted = state.highlightedItem,
                    onHighlighted = { viewModel.onHighlighted(it) })
            }
        }
        if (state.selectedActivitiesId.isNotEmpty()) {
            FloatingToolbar(
                modifier = Modifier.align(Alignment.BottomEnd).padding(Dimen16),
                selected = state.selectedActivitiesId
            ) { option ->
                when (option) {
                    ActivityOptions.DELETE -> viewModel.showActivityDeletionDialog()
                    ActivityOptions.EDIT -> viewModel.showNameActivityDialog()
                }
            }
        }
        val dialog = state.dialog
        if (dialog != null) {
            when (dialog) {
                is ActivitiesScreenDialog.ReNameActivity -> {
                    NameActivityDialog(
                        title = "Rename your activity",
                        message = "Please enter a new name for your activity before saving.",
                        name = dialog.activityName,
                        error = dialog.error,
                        dismissable = true,
                        onNameChanged = viewModel::onActivityNameChanged,
                        onDismiss = viewModel::dismissDialog,
                        onButonClick = { viewModel.onActivityNameConfirmed() }
                    )
                }

                is ActivitiesScreenDialog.ActivityDeletionDialog -> InfoDialog(
                    title = "Confirm Activity Deletion",
                    message = "Are you sure you want to delete selected ${if (state.selectedActivitiesId.size > 1) "activities" else "activity"}?",
                    buttonText = "Confirm",
                    onDismiss = viewModel::dismissDialog,
                    onButonClick = {
                        viewModel.deleteActivities(selectedActivitiesId = state.selectedActivitiesId)
                        viewModel.dismissDialog()
                    }
                )
            }
        }
    }
}
