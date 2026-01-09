package com.achub.hram.view.section

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Dimen48
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.LabelSmall
import com.achub.hram.style.Red
import com.achub.hram.style.White
import com.achub.hram.view.components.HrButton
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.record_screen_connect_device
import hram.composeapp.generated.resources.record_screen_device_from
import hram.composeapp.generated.resources.record_screen_disconnect_device
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeviceSection(device: BleDevice?, onConnectClick: () -> Unit, onDisconnectClick: () -> Unit) {
    if (device == null) {
        HrButton(
            modifier = Modifier.height(Dimen48),
            onClick = onConnectClick,
            enabled = true,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Dimen32),
                text = stringResource(Res.string.record_screen_connect_device).uppercase(),
                style = LabelSmall.copy(color = Red.copy(alpha = it)),
            )
        }
    } else {
        Text(
            modifier = Modifier.padding(Dimen32),
            text = stringResource(
                Res.string.record_screen_device_from,
                device.name,
                device.manufacturer ?: ""
            ),
            style = LabelMediumBold.copy(color = White.copy(alpha = 0.7f))
        )
        Spacer(Modifier.size(Dimen16))
        HrButton(
            modifier = Modifier.height(Dimen48),
            onClick = onDisconnectClick,
            enabled = true,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Dimen32),
                text = stringResource(Res.string.record_screen_disconnect_device).uppercase(),
                style = LabelSmall.copy(color = Red.copy(alpha = it)),
            )
        }
    }
}
