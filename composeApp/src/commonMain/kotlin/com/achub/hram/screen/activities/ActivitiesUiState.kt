package com.achub.hram.screen.activities

import com.achub.hram.data.db.entity.ActivityGraphInfo
import com.achub.hram.data.models.HighlightedItem
import org.jetbrains.compose.resources.StringResource

data class ActivitiesUiState(
    val activities: List<ActivityGraphInfo> = emptyList(),
    val highlightedItem: HighlightedItem? = null,
    val selectedActivitiesId: Set<String> = emptySet(),
    val dialog: ActivitiesScreenDialog? = null
) {
    val isSelectionMode: Boolean
        get() = selectedActivitiesId.isNotEmpty()
}

sealed class ActivitiesScreenDialog {
    data class ReNameActivity(val activityName: String, val error: StringResource? = null) : ActivitiesScreenDialog()

    data object ActivityDeletionDialog : ActivitiesScreenDialog()
}
