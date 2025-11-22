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
import com.achub.hram.data.models.HrIndication
import com.achub.hram.style.Dimen132
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Gray
import com.achub.hram.style.HeadingMediumBold
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.Red
import com.achub.hram.style.White
import com.achub.hram.view.HeartBeatingAnimView
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_battery_full
import org.jetbrains.compose.resources.vectorResource

@Composable
fun HeartLabelRow(
    modifier: Modifier = Modifier,
    hrIndication: HrIndication
) {
    val isEmpty = hrIndication.isEmpty()
    val hrBpm = hrIndication.hrBpm
    val batteryLevel = hrIndication.batteryLevel
    val heartColor = if (isEmpty) Gray else Red
    Row(verticalAlignment = CenterVertically) {
        HeartBeatingAnimView(hrBpm, modifier, heartColor)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(min = Dimen132),
                text = "${if (hrIndication.isEmpty()) "--" else hrBpm} BPM",
                textAlign = TextAlign.Center,
                style = HeadingMediumBold
            )
            val hasBatteryLevel = batteryLevel != HrIndication.NO_BATTERY_LEVEL
            val secondaryLabel = if (isEmpty) " No connection" else "  :  $batteryLevel %"
            val secondaryLabelColor = if (isEmpty) Red else White
            Row(verticalAlignment = CenterVertically) {
                AnimatedVisibility(hasBatteryLevel) {
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
