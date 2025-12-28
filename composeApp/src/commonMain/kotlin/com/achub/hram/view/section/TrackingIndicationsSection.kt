package com.achub.hram.view.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.ble.models.HrNotification
import com.achub.hram.ble.models.HramBleDevice
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Heading3
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.White
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
fun TrackingIndicationsSection(bleNotification: BleNotification, device: BleDevice?) {
    Column(horizontalAlignment = CenterHorizontally) {
        HeartIndicationRow(bleNotification = bleNotification)
        Text(
            modifier = Modifier.align(CenterHorizontally),
            text = LocalTime.fromSecondOfDay(bleNotification.elapsedTime.toInt()).format(dateFormat),
            style = Heading3
        )
        Spacer(modifier = Modifier.height(Dimen16))
        device?.let {
            Text(
                modifier = Modifier.padding(Dimen32),
                text = "${device.name} from ${device.manufacturer}",
                style = LabelMediumBold.copy(color = White.copy(alpha = 0.7f))
            )
        }
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
        device = HramBleDevice(
            name = "HRM Belt 42282",
            identifier = "00:11:22:33:44:55",
            manufacturer = "Decathlon",
        )
    )
}
