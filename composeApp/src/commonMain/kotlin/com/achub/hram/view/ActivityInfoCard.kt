package com.achub.hram.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.achub.hram.data.db.entity.ActivityGraphInfo
import com.achub.hram.fromEpochSeconds
import com.achub.hram.style.DarkGray
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.LabelSmall
import com.achub.hram.style.Red
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char


val DATE_TIME_FORMAT = LocalDateTime.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    year()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
}

@Composable
fun ActivityCard(activityInfo: ActivityGraphInfo) {
    val activity = activityInfo.activity
    val buckets = activityInfo.buckets

    val avgHr = if (buckets.isNotEmpty()) {
        buckets.map { it.avgHr }.average().toInt()
    } else 0

    val date = remember { DATE_TIME_FORMAT.format(activity.startDate.fromEpochSeconds()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { /* TODO: navigate to details */ },
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = activity.name.ifBlank { "Unnamed Activity" },
                style = LabelMediumBold,
            )

            Text(
                text = "Created: $date",
                style = LabelSmall
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Activity duration in sec: ${activity.duration}",
                style = LabelMedium
            )

            Text(
                text = "Buckets: ${buckets.size}",
                style = LabelMedium
            )

            Text(
                text = "Average HR: $avgHr bpm",
                style = LabelMedium.copy(color = Red)
            )
        }
    }
}
