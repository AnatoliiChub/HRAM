package com.achub.hram.screen.activities

import com.achub.hram.data.db.entity.ActivityGraphInfo
import com.achub.hram.data.models.HighlightedItem

data class ActivitiesUiState(
    val list: List<ActivityGraphInfo>,
    val highlightedItem: HighlightedItem? = null
)
