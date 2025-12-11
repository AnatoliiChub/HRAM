package com.achub.hram.view.indications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.achub.hram.ble.model.BleNotification
import com.achub.hram.ble.model.HrNotification
import com.achub.hram.style.Dimen132
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Gray
import com.achub.hram.style.HeadingMediumBold
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.Red
import com.achub.hram.style.White
import com.achub.hram.view.components.HeartBeatingAnimView
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_battery_full
import hram.composeapp.generated.resources.record_screen_heart_indication_battery_level
import hram.composeapp.generated.resources.record_screen_heart_indication_bpm
import hram.composeapp.generated.resources.record_screen_heart_indication_contact_off
import hram.composeapp.generated.resources.record_screen_heart_indication_no_connection
import hram.composeapp.generated.resources.record_screen_heart_indication_stub
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HeartIndicationRow(
    modifier: Modifier = Modifier,
    bleNotification: BleNotification
) {
    val isEmpty = bleNotification.isEmpty()
    val hrIndication = bleNotification.hrNotification
    val hrBpm = hrIndication?.hrBpm ?: 0
    val isSensorContactSupported = hrIndication?.isSensorContactSupported == true
    val isContactOn = hrIndication?.isContactOn == true
    val batteryLevel = bleNotification.batteryLevel
    val noBle = bleNotification.isBleConnected.not()
    val hrValueStub = isEmpty || noBle || (isSensorContactSupported && isContactOn.not()) || hrIndication == null
    val heartColor = if (hrValueStub) Gray else Red
    val hrLabel = if (hrValueStub) {
        stringResource(Res.string.record_screen_heart_indication_stub)
    } else {
        stringResource(Res.string.record_screen_heart_indication_bpm, hrBpm)
    }
    val secondaryLabel = when {
        noBle -> stringResource(Res.string.record_screen_heart_indication_no_connection)
        isContactOn.not() -> stringResource(Res.string.record_screen_heart_indication_contact_off)
        else -> stringResource(Res.string.record_screen_heart_indication_battery_level, batteryLevel)
    }
    val secondaryLabelColor = if (isEmpty) Red else White

    Row(verticalAlignment = CenterVertically) {
        HeartBeatingAnimView(hrValueStub.not(), modifier, heartColor)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(min = Dimen132),
                text = hrLabel,
                textAlign = TextAlign.Center,
                style = HeadingMediumBold
            )
            Row(verticalAlignment = CenterVertically) {
                AnimatedVisibility(bleNotification.isBleConnected && isContactOn) {
                    Image(
                        modifier = Modifier.size(Dimen32),
                        imageVector = vectorResource(Res.drawable.ic_battery_full),
                        colorFilter = ColorFilter.tint(White),
                        contentDescription = null
                    )
                }
                Text(
                    text = secondaryLabel,
                    style = LabelMedium.copy(color = secondaryLabelColor, letterSpacing = (-1.2).sp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun HeartIndicationRowPreview() {
    val hrNotification = HrNotification(hrBpm = 130, isSensorContactSupported = true, isContactOn = true)
    val bleNotification = BleNotification(
        hrNotification = hrNotification,
        batteryLevel = 85,
        isBleConnected = true,
        elapsedTime = 120L,
    )
    HeartIndicationRow(bleNotification = bleNotification)
}
