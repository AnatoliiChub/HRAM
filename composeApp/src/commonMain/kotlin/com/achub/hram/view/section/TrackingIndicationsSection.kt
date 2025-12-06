package com.achub.hram.view.section

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import com.achub.hram.ble.model.BleNotification
import com.achub.hram.style.Heading3
import com.achub.hram.view.indications.HeartIndicationRow
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
fun TrackingIndicationsSection(bleNotification: BleNotification) {
    Column(horizontalAlignment = CenterHorizontally) {
        HeartIndicationRow(bleNotification = bleNotification)
        Text(
            modifier = Modifier.align(CenterHorizontally),
            text = LocalTime.fromSecondOfDay(bleNotification.elapsedTime.toInt()).format(dateFormat),
            style = Heading3
        )
    }
}
