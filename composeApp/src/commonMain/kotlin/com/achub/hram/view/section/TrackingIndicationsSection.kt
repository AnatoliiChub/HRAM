package com.achub.hram.view.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.ble.models.HrNotification
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Heading3
import com.achub.hram.utils.formatElapsedTime
import com.achub.hram.view.indications.HeartIndicationRow

@Composable
fun TrackingIndicationsSection(bleNotification: BleNotification) {
    Column(horizontalAlignment = CenterHorizontally) {
        HeartIndicationRow(bleNotification = bleNotification)
        Text(
            modifier = Modifier.align(CenterHorizontally),
            text = formatElapsedTime(bleNotification.elapsedTime),
            style = Heading3
        )
        Spacer(modifier = Modifier.height(Dimen16))
    }
}

@Preview
@Composable
private fun TrackingIndicationsSectionPreview() {
    val hrNotification = HrNotification(hrBpm = 128, isSensorContactSupported = true, isContactOn = true)
    val bleNotification = BleNotification(
        hrNotification = hrNotification,
        batteryLevel = 90,
        isBleConnected = true,
        elapsedTime = 1268L,
    )
    TrackingIndicationsSection(
        bleNotification = bleNotification,
    )
}
