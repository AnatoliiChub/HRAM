package com.achub.hram.view.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.ext.dateFormat
import com.achub.hram.ext.formatTime
import com.achub.hram.ext.fromEpochSeconds
import com.achub.hram.models.GraphLimitsUi
import com.achub.hram.models.HighlightedItemUi
import com.achub.hram.style.Dimen12
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen2
import com.achub.hram.style.Dimen24
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Dimen320
import com.achub.hram.style.Dimen8
import com.achub.hram.style.LabelBigBold
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.view.chart.AreaChart
import com.achub.hram.view.chart.ChartBubble
import com.achub.hram.view.chart.ChartData
import com.achub.hram.view.chart.defaultChartStyle
import com.achub.hram.view.dialogs.InfoDialog
import hram.ui_lib.generated.resources.Res
import hram.ui_lib.generated.resources.activity_screen_avg_hr
import hram.ui_lib.generated.resources.activity_screen_created_at
import hram.ui_lib.generated.resources.activity_screen_elapsed_time
import hram.ui_lib.generated.resources.activity_screen_heart_indication_bpm
import hram.ui_lib.generated.resources.activity_screen_kcal_burnt
import hram.ui_lib.generated.resources.activity_screen_kcal_info
import hram.ui_lib.generated.resources.activity_screen_max_hr
import hram.ui_lib.generated.resources.activity_screen_min_hr
import hram.ui_lib.generated.resources.activity_screen_seconds_unit
import hram.ui_lib.generated.resources.activity_screen_unnamed_act
import hram.ui_lib.generated.resources.ic_info
import hram.ui_lib.generated.resources.ic_not_selected
import hram.ui_lib.generated.resources.ic_selected
import hram.ui_lib.generated.resources.month_names
import kotlinx.datetime.format.MonthNames
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun ActivityCard(
    modifier: Modifier,
    selected: Boolean,
    selectionEnabled: Boolean,
    activityInfo: ActivityGraphInfo,
    highLighted: HighlightedItemUi?,
    onHighlighted: (HighlightedItemUi?) -> Unit,
) {
    val buckets = activityInfo.buckets
    val chartData = ChartData(
        points = buckets.map { it.elapsedTime.toFloat() to it.avgHr },
        limits = activityInfo.limits,
        highLighted = if (activityInfo.id == highLighted?.activityId) highLighted.point else null
    )
    val monthNames = stringArrayResource(Res.array.month_names)
    val date = remember(monthNames) {
        dateFormat(MonthNames(monthNames))
            .format(activityInfo.startDate.fromEpochSeconds())
    }

    var showKcalInfo by remember { mutableStateOf(false) }

    val containerColor = if (selected) {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(Dimen2, RoundedCornerShape(Dimen12))
            .clip(RoundedCornerShape(Dimen12)),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        val secondsLabel = stringResource(Res.string.activity_screen_seconds_unit)
        val elapsedTime = formatTime(activityInfo.duration, secondsLabel)
        Column(modifier = Modifier.padding(Dimen16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActivityTitle(activityInfo.name)
                if (selectionEnabled) SelectionIndication(selected)
            }
            Spacer(Modifier.height(Dimen8))
            Text(
                text = stringResource(Res.string.activity_screen_created_at, date),
                style = LabelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(Dimen8))
            Text(
                text = stringResource(
                    Res.string.activity_screen_elapsed_time,
                    elapsedTime
                ),
                style = LabelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(Dimen16))
            HeartRateLabel(Res.string.activity_screen_avg_hr, activityInfo.avgHr)
            Spacer(Modifier.height(Dimen8))
            HeartRateLabel(Res.string.activity_screen_max_hr, activityInfo.maxHr)
            Spacer(Modifier.height(Dimen8))
            HeartRateLabel(Res.string.activity_screen_min_hr, activityInfo.minHr)
            KcalBurntRow(activityInfo.kcalBurnt, onInfoClick = { showKcalInfo = true })
            Spacer(Modifier.height(Dimen16))
            AreaChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimen16)
                    .height(Dimen320),
                chartData = chartData,
                onHighLight = { onHighlighted(it?.let { HighlightedItemUi(activityInfo.id, it) }) },
                style = defaultChartStyle()
            ) { xLabel, yLabel -> ChartBubble(xLabel, yLabel) }
        }
    }

    if (showKcalInfo) {
        InfoDialog(
            title = Res.string.activity_screen_kcal_burnt,
            message = stringResource(Res.string.activity_screen_kcal_info),
            onDismiss = { showKcalInfo = false },
            onButtonClick = { showKcalInfo = false }
        )
    }
}

@Composable
private fun KcalBurntRow(kcal: Double, onInfoClick: () -> Unit) = Row(
    verticalAlignment = Alignment.CenterVertically
) {
    Row(
        modifier = Modifier.clickable(onClick = onInfoClick)
            .padding(top = Dimen8, bottom = Dimen8, end = Dimen16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.activity_screen_kcal_burnt),
            style = LabelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.width(Dimen8))
        Icon(
            painter = painterResource(Res.drawable.ic_info),
            contentDescription = null,
            modifier = Modifier.size(Dimen24),
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
    Spacer(Modifier.weight(1f))
    Text(
        text = kcal.roundToInt().toString(),
        style = LabelMediumBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun RowScope.ActivityTitle(name: String) {
    Text(
        modifier = Modifier.weight(1f).padding(vertical = Dimen8),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        text = name.ifBlank {
            stringResource(Res.string.activity_screen_unnamed_act)
        },
        style = LabelBigBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SelectionIndication(selected: Boolean) {
    val iconRes =
        if (selected) Res.drawable.ic_selected else Res.drawable.ic_not_selected
    Icon(
        modifier = Modifier.size(Dimen32),
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun HeartRateLabel(stringRes: StringResource, value: Int) = Row {
    Text(
        text = stringResource(stringRes),
        style = LabelMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    )
    Spacer(Modifier.weight(1f))
    Text(
        text = stringResource(
            Res.string.activity_screen_heart_indication_bpm,
            value
        ),
        style = LabelMediumBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Preview
@Composable
@Suppress("MagicNumber")
private fun ActivityCardPreview() {
    val buckets = listOf(
        AvgHrBucketByActivity(1, elapsedTime = 60L, avgHr = 90f),
        AvgHrBucketByActivity(2, elapsedTime = 120L, avgHr = 110f),
        AvgHrBucketByActivity(3, elapsedTime = 180L, avgHr = 130f),
        AvgHrBucketByActivity(4, elapsedTime = 240L, avgHr = 125f),
        AvgHrBucketByActivity(5, elapsedTime = 300L, avgHr = 115f),
    )

    val info = ActivityGraphInfo(
        id = "1",
        name = "Evening Run",
        startDate = 1_700_000_000L,
        duration = 45 * 60L,
        buckets = buckets,
        avgHr = 115,
        maxHr = 135,
        minHr = 80,
        totalRecords = buckets.size,
        limits = GraphLimitsUi(
            minX = buckets.minOf { it.elapsedTime }.toFloat(),
            maxX = buckets.maxOf { it.elapsedTime }.toFloat(),
            minY = 60f,
            maxY = 190f
        )
    )
    val point = buckets[2].elapsedTime.toFloat() to buckets[2].avgHr

    ActivityCard(
        modifier = Modifier,
        selected = true,
        selectionEnabled = true,
        activityInfo = info,
        highLighted = HighlightedItemUi(info.id, point),
        onHighlighted = {}
    )
}
