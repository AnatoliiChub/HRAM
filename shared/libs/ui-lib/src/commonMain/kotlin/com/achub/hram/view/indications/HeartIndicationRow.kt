package com.achub.hram.view.indications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.achub.hram.models.BleNotificationUi
import com.achub.hram.models.HrNotificationUi
import com.achub.hram.style.Dimen32
import com.achub.hram.style.HeadingMediumBold
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.Red
import com.achub.hram.view.components.HeartBeatingAnimView
import hram.ui_lib.generated.resources.Res
import hram.ui_lib.generated.resources.heart3d
import hram.ui_lib.generated.resources.heart_indication_battery_level
import hram.ui_lib.generated.resources.heart_indication_bpm
import hram.ui_lib.generated.resources.heart_indication_contact_off
import hram.ui_lib.generated.resources.heart_indication_no_connection
import hram.ui_lib.generated.resources.heart_indication_stub
import hram.ui_lib.generated.resources.ic_battery_full
import hram.ui_lib.generated.resources.ic_heart_contact_off
import hram.ui_lib.generated.resources.ic_heart_disconnected
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

private const val MAX_BPM_STUB = 555
private const val LETTER_SPACING_REDUCTION = -1.2

private data class HeartIndicationState(
    val hrValueStub: Boolean,
    val hrLabel: String,
    val secondaryLabel: String,
    val heartIcon: DrawableResource,
    val isContactOn: Boolean
)

@Composable
private fun calculateHeartIndicationState(bleNotification: BleNotificationUi): HeartIndicationState {
    val hrIndication = bleNotification.hrNotification
    val isSensorContactSupported = hrIndication?.isSensorContactSupported == true
    val isContactOn = hrIndication?.isContactOn == true
    val noBle = bleNotification.isBleConnected.not()
    val isContactOff = isSensorContactSupported && !isContactOn
    val hrValueStub = bleNotification.isEmpty() || noBle || isContactOff || hrIndication == null

    val hrLabel = if (hrValueStub) {
        stringResource(Res.string.heart_indication_stub)
    } else {
        stringResource(Res.string.heart_indication_bpm, hrIndication.hrBpm)
    }

    val secondaryLabel = when {
        noBle -> stringResource(Res.string.heart_indication_no_connection)
        isContactOff -> stringResource(Res.string.heart_indication_contact_off)
        else -> stringResource(Res.string.heart_indication_battery_level, bleNotification.batteryLevel)
    }

    val heartIcon = when {
        noBle -> Res.drawable.ic_heart_disconnected
        isContactOff -> Res.drawable.ic_heart_contact_off
        else -> Res.drawable.heart3d
    }

    return HeartIndicationState(hrValueStub, hrLabel, secondaryLabel, heartIcon, isContactOn)
}

@Composable
fun HeartIndicationRow(
    bleNotification: BleNotificationUi,
    heartPosUpdated: (Offset) -> Unit
) {
    val state = calculateHeartIndicationState(bleNotification)

    Row(verticalAlignment = CenterVertically) {
        HeartIcon(state.hrValueStub, state.heartIcon, heartPosUpdated)
        HeartDataColumn(state.hrLabel, state.secondaryLabel, bleNotification, state.isContactOn)
    }
}

@Composable
private fun HeartIcon(
    hrValueStub: Boolean,
    heartIcon: DrawableResource,
    heartPosUpdated: (Offset) -> Unit
) {
    HeartBeatingAnimView(
        isBeating = !hrValueStub,
        modifier = Modifier.onGloballyPositioned {
            val position = it.positionOnScreen()
            val center = Offset(
                x = position.x + it.size.width / 2f,
                y = position.y + it.size.height / 2f
            )
            heartPosUpdated(center)
        },
        icon = heartIcon,
    )
}

@Composable
private fun HeartDataColumn(
    hrLabel: String,
    secondaryLabel: String,
    bleNotification: BleNotificationUi,
    isContactOn: Boolean
) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val secondaryLabelColor = if (bleNotification.isEmpty()) Red else onBackground
    val maxHeartLabel = stringResource(Res.string.heart_indication_bpm, MAX_BPM_STUB)
    val textMeasurer = rememberTextMeasurer()
    val maxWidthPx = remember {
        textMeasurer.measure(maxHeartLabel, HeadingMediumBold).size.width
    }
    val maxWidth = with(LocalDensity.current) { maxWidthPx.toDp() }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(min = maxWidth),
            text = hrLabel,
            textAlign = TextAlign.Center,
            style = HeadingMediumBold,
            color = onBackground
        )
        SecondaryLabelRow(secondaryLabel, secondaryLabelColor, bleNotification.isBleConnected && isContactOn)
    }
}

@Composable
private fun SecondaryLabelRow(
    text: String,
    color: Color,
    showBatteryIcon: Boolean
) {
    Row(verticalAlignment = CenterVertically) {
        AnimatedVisibility(showBatteryIcon) {
            Image(
                modifier = Modifier.size(Dimen32),
                imageVector = vectorResource(Res.drawable.ic_battery_full),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                contentDescription = null
            )
        }
        Text(
            text = text,
            style = LabelMedium.copy(color = color, letterSpacing = LETTER_SPACING_REDUCTION.sp)
        )
    }
}

@Preview
@Composable
private fun HeartIndicationRowPreview() {
    val hrNotification = HrNotificationUi(hrBpm = 130, isSensorContactSupported = true, isContactOn = true)
    val bleNotification = BleNotificationUi(
        hrNotification = hrNotification,
        batteryLevel = 85,
        isBleConnected = true,
        elapsedTime = 120L,
    )
    HeartIndicationRow(bleNotification = bleNotification, heartPosUpdated = {})
}
