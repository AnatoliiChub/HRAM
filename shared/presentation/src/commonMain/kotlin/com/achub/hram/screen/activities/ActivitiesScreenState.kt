package com.achub.hram.screen.activities

import com.achub.hram.models.ActivityInfo
import com.achub.hram.models.GraphLimitsUi
import com.achub.hram.models.HighlightedItemUi
import com.achub.hram.view.cards.ActivityGraphInfo
import com.achub.hram.view.cards.AvgHrBucketByActivity
import org.jetbrains.compose.resources.StringResource

data class ActivitiesUiState(
    val activities: List<ActivityGraphInfo> = emptyList(),
    val highlightedItem: HighlightedItemUi? = null,
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

private const val MAX_HR_FACTOR = 1.2f

fun ActivityInfo.toGraphInfo(): ActivityGraphInfo {
    val uiBuckets = buckets.map { AvgHrBucketByActivity(it.bucketNumber, it.avgHr, it.elapsedTime) }
    return ActivityGraphInfo(
        id = id,
        name = name,
        startDate = startDate,
        duration = duration,
        buckets = uiBuckets,
        totalRecords = totalRecords,
        avgHr = avgHr,
        maxHr = maxHr,
        minHr = minHr,
        limits = GraphLimitsUi(
            minX = 0f,
            maxX = uiBuckets.maxOfOrNull { it.elapsedTime }?.toFloat() ?: 1f,
            minY = 0f,
            maxY = (uiBuckets.maxOfOrNull { it.avgHr } ?: 1f) * MAX_HR_FACTOR,
        ),
    )
}
