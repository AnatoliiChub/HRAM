package com.achub.hram.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.achub.hram.data.db.entity.ActivityGraphInfo
import com.achub.hram.formatTime
import com.achub.hram.fromEpochSeconds
import com.achub.hram.style.DarkGray
import com.achub.hram.style.Dimen12
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen2
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Dimen320
import com.achub.hram.style.Dimen48
import com.achub.hram.style.Dimen8
import com.achub.hram.style.LabelBigBold
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.LabelSmall
import com.achub.hram.view.chart.AreaChart
import com.achub.hram.view.chart.ChartBubble
import com.achub.hram.view.chart.ChartData
import com.achub.hram.view.chart.defaultChartStyle
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

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
fun ActivityCard(activityInfo: ActivityGraphInfo) {
    val activity = activityInfo.activity
    val buckets = activityInfo.buckets
    val chartData = ChartData(points = buckets.map { it.timestamp.toFloat() to it.avgHr }, limits = activityInfo.limits)

    val date = remember { DATE_TIME_FORMAT.format(activity.startDate.fromEpochSeconds()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(Dimen2, RoundedCornerShape(Dimen12))
            .clip(RoundedCornerShape(Dimen12))
            .clickable { /* TODO: navigate to details */ },
        colors = CardDefaults.cardColors(containerColor = DarkGray)
    ) {
        Column(modifier = Modifier.padding(Dimen16)) {
            Text(text = activity.name.ifBlank { "Unnamed Activity" }, style = LabelBigBold)
            Spacer(Modifier.height(Dimen8))
            Text(text = "Created at $date", style = LabelSmall)
            Spacer(Modifier.height(Dimen8))
            Text(text = "Elapsed time: ${formatTime(activity.duration)}", style = LabelMedium)

            //TODO REMOVE 2 FIELDS:
            Text(text = "Buckets: ${buckets.size}", style = LabelMedium)
            Text(text = "Total: ${activityInfo.totalRecords}", style = LabelMedium)

            Spacer(Modifier.height(Dimen48))

            AreaChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimen16)
                    .height(Dimen320),
                chartData = chartData,
                style = defaultChartStyle()
            ) { xLabel, yLabel -> ChartBubble(xLabel, yLabel) }
            Spacer(Modifier.height(Dimen32))
            Text(text = "Avg Heart Rate: ${activityInfo.avgHr} bpm", style = LabelMedium)
            Spacer(Modifier.height(Dimen8))
            Text(text = "Max Heart Rate: ${activityInfo.maxHr} bpm", style = LabelMedium)
            Spacer(Modifier.height(Dimen8))
            Text(text = "Min Heart Rate: ${activityInfo.minHr} bpm", style = LabelMedium)
        }
    }
}
