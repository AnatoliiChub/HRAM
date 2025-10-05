package com.achub.hram.view.indications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achub.hram.data.model.HrNotifications
import com.achub.hram.style.Gray
import com.achub.hram.style.Red
import com.achub.hram.style.White
import com.achub.hram.view.HeartBeatingAnim
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_battery_full
import org.jetbrains.compose.resources.vectorResource

@Composable
fun HeartLabelRow(
    modifier: Modifier = Modifier,
    hrNotifications: HrNotifications
) {
    val isEmpty = hrNotifications.isEmpty()
    val hrBpm = hrNotifications.hrBpm
    val batteryLevel = hrNotifications.batteryLevel
    val heartColor = if (isEmpty) Gray else Red
    Row(verticalAlignment = CenterVertically) {
        HeartBeatingAnim(hrBpm, modifier, heartColor)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "${if (hrNotifications.isEmpty()) "--" else hrBpm} BPM",
                style = TextStyle(color = White, fontWeight = W700, fontSize = 32.sp)
            )
            val hasBatteryLevel = batteryLevel != HrNotifications.NO_BATTERY_LEVEL
            val secondaryLabel = if (isEmpty)  " No connection" else "  :  $batteryLevel %"
            val secondaryLabelColor = if (isEmpty) Red else White
            Row(verticalAlignment = CenterVertically) {
                AnimatedVisibility(hasBatteryLevel) {
                    Image(
                        modifier = Modifier.size(32.dp),
                        imageVector = vectorResource(Res.drawable.ic_battery_full),
                        colorFilter = ColorFilter.tint(White),
                        contentDescription = null
                    )
                }
                Text(
                    modifier = Modifier,
                    text = secondaryLabel,
                    style = TextStyle(
                        color = secondaryLabelColor,
                        fontWeight = W400,
                        fontSize = 20.sp,
                        letterSpacing = (-1.2).sp
                    )
                )
            }
        }
    }
}
