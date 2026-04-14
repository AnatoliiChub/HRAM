package com.achub.hram.view.section

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import com.achub.hram.models.DeviceUi
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Dimen48
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.LabelSmall
import com.achub.hram.style.Red
import com.achub.hram.style.White
import com.achub.hram.view.components.HrButton
import hram.ui_lib.generated.resources.Res
import hram.ui_lib.generated.resources.connect_device
import hram.ui_lib.generated.resources.device_from
import hram.ui_lib.generated.resources.disconnect_device
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeviceSection(device: DeviceUi?, onConnectClick: () -> Unit, onDisconnectClick: () -> Unit) {
    if (device == null) {
        HrButton(
            modifier = Modifier.height(Dimen48),
            onClick = onConnectClick,
            enabled = true,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Dimen32),
                text = stringResource(Res.string.connect_device).uppercase(),
                style = LabelSmall.copy(color = Red.copy(alpha = it)),
            )
        }
    } else {
        Text(
            modifier = Modifier.padding(horizontal = Dimen32),
            text = stringResource(
                Res.string.device_from,
                device.name,
                device.manufacturer ?: ""
            ),
            style = LabelMediumBold.copy(color = White.copy(alpha = 0.7f), textAlign = Center)
        )
        Spacer(Modifier.size(Dimen16))
        HrButton(
            modifier = Modifier.height(Dimen48),
            onClick = onDisconnectClick,
            enabled = true,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Dimen32),
                text = stringResource(Res.string.disconnect_device).uppercase(),
                style = LabelSmall.copy(color = Red.copy(alpha = it)),
            )
        }
    }
}
