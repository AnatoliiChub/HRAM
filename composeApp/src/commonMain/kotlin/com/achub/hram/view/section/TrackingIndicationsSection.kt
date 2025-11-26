package com.achub.hram.view.section

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import com.achub.hram.data.models.BleIndication
import com.achub.hram.style.Heading3
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
fun TrackingIndicationsSection(bleIndication: BleIndication) {
    Column(horizontalAlignment = CenterHorizontally) {
        HeartLabelRow(bleIndication = bleIndication)
        Text(
            modifier = Modifier.align(CenterHorizontally),
            text = LocalTime.fromSecondOfDay(bleIndication.elapsedTime.toInt()).format(dateFormat),
            style = Heading3
        )
    }
}
