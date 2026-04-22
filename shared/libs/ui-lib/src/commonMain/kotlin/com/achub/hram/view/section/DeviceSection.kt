package com.achub.hram.view.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.achub.hram.models.DeviceUi
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Dimen48
import com.achub.hram.style.HramTheme
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.LabelSmall
import com.achub.hram.view.components.HrButton
import hram.ui_lib.generated.resources.Res
import hram.ui_lib.generated.resources.connect_device
import hram.ui_lib.generated.resources.device_from
import hram.ui_lib.generated.resources.disconnect_device
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeviceSection(device: DeviceUi?, onConnectClick: () -> Unit, onDisconnectClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    if (device == null) {
        HrButton(
            modifier = Modifier.height(Dimen48),
            onClick = onConnectClick,
            enabled = true,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Dimen32),
                text = stringResource(Res.string.connect_device).uppercase(),
                style = LabelSmall.copy(color = colorScheme.secondary.copy(alpha = 0.8f)),
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
            style = LabelMediumBold.copy(color = colorScheme.onBackground.copy(alpha = 0.7f), textAlign = Center)
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
                style = LabelSmall.copy(color = colorScheme.secondary.copy(alpha = 0.8f)),
            )
        }
    }
}

@Preview
@Composable
private fun DeviceSectionPreview() {
    Column {
        HramTheme(darkTheme = false) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DeviceSection(device = null, onConnectClick = {}, onDisconnectClick = {})
                    Spacer(Modifier.height(16.dp))
                    DeviceSection(
                        device = DeviceUi(name = "Polar H10", identifier = "00:11:22", manufacturer = "Polar"),
                        onConnectClick = {},
                        onDisconnectClick = {}
                    )
                }
            }
        }
        HramTheme(darkTheme = true) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DeviceSection(device = null, onConnectClick = {}, onDisconnectClick = {})
                    Spacer(Modifier.height(16.dp))
                    DeviceSection(
                        device = DeviceUi(name = "Polar H10", identifier = "00:11:22", manufacturer = "Polar"),
                        onConnectClick = {},
                        onDisconnectClick = {}
                    )
                }
            }
        }
    }
}
