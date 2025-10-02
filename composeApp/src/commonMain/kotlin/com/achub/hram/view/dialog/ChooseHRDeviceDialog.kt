package com.achub.hram.view.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.achub.hram.data.model.BleDevice
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.Red
import com.achub.hram.style.White
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChooseHRDeviceDialog(
    onConfirmClick: (BleDevice) -> Unit,
    onDismissRequest: () -> Unit,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    devices: List<BleDevice>,
) {
    var selected by rememberSaveable { mutableStateOf<BleDevice?>(null) }
    val retryState = selected == null
    val title = if (isLoading) "Scanning for HR-devices" else "Please, choose a heart rate device to Pair:"
    BaseDialog(
        title = title,
        onDismissRequest = onDismissRequest,
        isButtonVisible = !isLoading,
        onButtonClick = {
            if (retryState) {
                onRefresh()
            } else {
                selected?.let { onConfirmClick(it) }
            }
        },
        buttonTitle = if (retryState) "Retry" else "Confirm"
    ) {
        if (isLoading) {
            Spacer(Modifier.height(16.dp))
            LinearWavyProgressIndicator(
                modifier = Modifier.height(48.dp),
                trackColor = White,
                color = Red,
                wavelength = 44.dp,
                gapSize = 32.dp,
                waveSpeed = 0.dp,
                amplitude = 1f
            )
        }
        Spacer(Modifier.height(16.dp))
        DeviceList(devices = devices, selected) {
            if (isLoading) return@DeviceList
            selected = if (selected == it) null else it
        }
    }
}

@Composable
fun DeviceList(devices: List<BleDevice>, selected: BleDevice?, onSelected: (BleDevice) -> Unit = {}) {
    LazyColumn(Modifier.fillMaxWidth().padding(16.dp)) {
        items(devices) { device ->
            val color = if (selected?.identifier == device.identifier) Red else White
            Text(
                text = device.name,
                color = color,
                style = LabelMediumBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(device) }

            )
            Text(
                text = device.identifier,
                color = color,
                style = LabelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(device) }

            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
@Preview
fun ChooseHRDeviceDialogPreview() {
    ChooseHRDeviceDialog(
        onConfirmClick = {},
        onDismissRequest = {},
        onRefresh = {},
        isLoading = true,
        devices = listOf(
            BleDevice("Hrm1", "00:11:22:33:44:55"),
            BleDevice("Hrm2", "66:77:88:99:AA:BB"),
            BleDevice("Hrm3", "CC:DD:EE:FF:00:11"),
        )
    )
}