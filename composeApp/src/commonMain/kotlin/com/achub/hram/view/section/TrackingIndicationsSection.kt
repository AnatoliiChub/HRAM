package com.achub.hram.view.section

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import com.achub.hram.data.model.IndicationSection
import com.achub.hram.style.Heading3
import com.achub.hram.view.indications.DistanceLabelRow
import com.achub.hram.view.indications.HeartLabelRow
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char

val dateFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
    char(':')
    second()
}

@Composable
fun TrackingIndicationsSection(indications: IndicationSection) {
    Column(horizontalAlignment = CenterHorizontally) {
        HeartLabelRow(hrNotifications = indications.hrNotifications)
        DistanceLabelRow(distance = indications.distance)
        Text(
            modifier = Modifier.align(CenterHorizontally),
            text = LocalTime.fromSecondOfDay(indications.duration.toInt()).format(dateFormat),
            style = Heading3
        )
    }
}