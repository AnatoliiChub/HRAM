package com.achub.hram.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.data.db.entity.ActivityEntity
import com.achub.hram.data.db.entity.ActivityGraphInfo
import com.achub.hram.data.db.entity.AvgHrBucketByActivity
import com.achub.hram.data.models.GraphLimits
import com.achub.hram.data.models.HighlightedItem
import com.achub.hram.ext.formatTime
import com.achub.hram.ext.fromEpochSeconds
import com.achub.hram.style.DarkGray
import com.achub.hram.style.Dimen12
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen2
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Dimen320
import com.achub.hram.style.Dimen8
import com.achub.hram.style.Gray40
import com.achub.hram.style.LabelBigBold
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.White80
import com.achub.hram.view.chart.AreaChart
import com.achub.hram.view.chart.ChartBubble
import com.achub.hram.view.chart.ChartData
import com.achub.hram.view.chart.defaultChartStyle
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.activity_screen_avg_hr
import hram.composeapp.generated.resources.activity_screen_created_at
import hram.composeapp.generated.resources.activity_screen_elapsed_time
import hram.composeapp.generated.resources.activity_screen_heart_indication_bpm
import hram.composeapp.generated.resources.activity_screen_max_hr
import hram.composeapp.generated.resources.activity_screen_min_hr
import hram.composeapp.generated.resources.activity_screen_unnamed_act
import hram.composeapp.generated.resources.ic_not_selected
import hram.composeapp.generated.resources.ic_selected
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

const val Y_LABEL_COUNT = 5
const val X_LABEL_COUNT = 4
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
fun ActivityCard(
    modifier: Modifier,
    selected: Boolean,
    selectionEnabled: Boolean,
    activityInfo: ActivityGraphInfo,
    highLighted: HighlightedItem?,
    onHighlighted: (HighlightedItem?) -> Unit
) {
    val activity = activityInfo.activity
    val buckets = activityInfo.buckets
    val chartData = ChartData(
        points = buckets.map { it.timestamp.toFloat() to it.avgHr },
        limits = activityInfo.limits,
        highLighted = if (activity.id == highLighted?.activityId) highLighted.point else null
    )

    val date = remember { DATE_TIME_FORMAT.format(activity.startDate.fromEpochSeconds()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(Dimen2, RoundedCornerShape(Dimen12))
            .clip(RoundedCornerShape(Dimen12)),
        colors = CardDefaults.cardColors(containerColor = if (selected) Gray40 else DarkGray)
    ) {
        val elapsedTime = formatTime(activity.duration)
        Column(modifier = Modifier.padding(Dimen16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Title(activity)
                if (selectionEnabled) SelectionIndication(selected)
            }
            Spacer(Modifier.height(Dimen8))
            Text(text = stringResource(Res.string.activity_screen_created_at, date), style = LabelMedium)
            Spacer(Modifier.height(Dimen8))
            Text(text = stringResource(Res.string.activity_screen_elapsed_time, elapsedTime), style = LabelMedium)
            Spacer(Modifier.height(Dimen16))
            HeartRateLabel(Res.string.activity_screen_avg_hr, activityInfo.avgHr)
            Spacer(Modifier.height(Dimen8))
            HeartRateLabel(Res.string.activity_screen_max_hr, activityInfo.maxHr)
            Spacer(Modifier.height(Dimen8))
            HeartRateLabel(Res.string.activity_screen_min_hr, activityInfo.minHr)
            Spacer(Modifier.height(Dimen32))
            AreaChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimen16)
                    .height(Dimen320),
                chartData = chartData,
                onHighLight = { onHighlighted(it?.let { HighlightedItem(activity.id, it) }) },
                style = defaultChartStyle()
            ) { xLabel, yLabel -> ChartBubble(xLabel, yLabel) }
        }
    }
}

@Composable
private fun RowScope.Title(activity: ActivityEntity) {
    Text(
        modifier = Modifier.weight(1f).padding(vertical = Dimen8),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        text = activity.name.ifBlank { stringResource(Res.string.activity_screen_unnamed_act) },
        style = LabelBigBold,
    )
}

@Composable
private fun SelectionIndication(selected: Boolean) {
    val iconRes = if (selected) Res.drawable.ic_selected else Res.drawable.ic_not_selected
    Icon(
        modifier = Modifier.size(Dimen32),
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = White80
    )
}

@Composable
fun HeartRateLabel(stringRes: StringResource, value: Int) = Row {
    Text(text = stringResource(stringRes), style = LabelMedium)
    Spacer(Modifier.weight(1f))
    Text(text = stringResource(Res.string.activity_screen_heart_indication_bpm, value), style = LabelMediumBold)
}

@Preview
@Composable
private fun ActivityCardPreview() {
    val sampleActivity = ActivityEntity(
        id = "1",
        name = "Evening Run",
        startDate = 1_700_000_000L,
        duration = 45 * 60L,
    )

    val buckets = listOf(
        AvgHrBucketByActivity(1, timestamp = 60L, avgHr = 90f),
        AvgHrBucketByActivity(2, timestamp = 120L, avgHr = 110f),
        AvgHrBucketByActivity(3, timestamp = 180L, avgHr = 130f),
        AvgHrBucketByActivity(4, timestamp = 240L, avgHr = 125f),
        AvgHrBucketByActivity(5, timestamp = 300L, avgHr = 115f),
    )

    val info = ActivityGraphInfo(
        activity = sampleActivity,
        buckets = buckets,
        avgHr = 115,
        maxHr = 135,
        minHr = 80,
        totalRecords = buckets.size,
        limits = GraphLimits(
            minX = buckets.minOf { it.timestamp }.toFloat(),
            maxX = buckets.maxOf { it.timestamp }.toFloat(),
            minY = 60f,
            maxY = 190f
        )
    )
    val point = buckets[2].timestamp.toFloat() to buckets[2].avgHr

    ActivityCard(
        modifier = Modifier,
        selected = true,
        selectionEnabled = true,
        activityInfo = info,
        highLighted = HighlightedItem(sampleActivity.id, point),
        onHighlighted = {}
    )
}
