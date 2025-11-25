package com.achub.hram.screen.activities

import com.achub.hram.data.db.entity.ActivityGraphInfo
import com.achub.hram.data.models.HighlightedItem

data class ActivitiesUiState(
    val list: List<ActivityGraphInfo>,
    val highlightedItem: HighlightedItem? = null,
    val selectedActivitiesId: Set<String> = emptySet(),
    val dialog: ActivitiesScreenDialog? = null
)

sealed class ActivitiesScreenDialog {
    data class ReNameActivity(val activityName: String, val error: String? = null) : ActivitiesScreenDialog()
    data object ActivityDeletionDialog : ActivitiesScreenDialog()
}
