package com.achub.hram.view.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.achub.hram.data.model.BleDevice
import com.achub.hram.logger
import com.achub.hram.view.DeviceListItem
import com.achub.hram.view.HRProgress
import com.achub.hram.view.dialog.base.DialogButton
import com.achub.hram.view.dialog.base.DialogElevatedCard
import com.achub.hram.view.dialog.base.DialogMessage
import com.achub.hram.view.dialog.base.DialogTitle
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val TAG = "HrConnectDialog"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HrConnectDialog(
    isLoading: Boolean,
    devices: List<BleDevice>,
    loadingDuration: Duration,
    isDeviceConfirmed: Boolean = false,
    onConfirmClick: (BleDevice) -> Unit,
    onRefresh: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    var selected by remember { mutableStateOf<BleDevice?>(null) }
    val retryState = !isLoading && selected == null
    val title = if (isDeviceConfirmed) "Connecting..." else if (isLoading) "Scanning..." else "Connect Device"
    val message = when {
        isLoading -> "Scanning for devices..."
        devices.isEmpty() -> "No devices found, please try again."
        else -> "Select your Heart rate device from the list or try to scan again."
    }
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        val btnText = if (retryState) "Retry" else "Connect"
        val onBtnClick: () -> Unit = {
            when {
                selected != null -> selected?.let {
                    logger(TAG) { "confirmed device: $it" }
                    onConfirmClick(it)
                }

                retryState -> onRefresh()
            }
        }

        DialogElevatedCard(animate = isLoading) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = CenterHorizontally
            ) {
                DialogTitle(title = title)
                Spacer(Modifier.height(24.dp))
                if (isLoading) {
                    HRProgress(isLoading, cycleDuration = loadingDuration)
                } else {
                    DialogMessage(message = message)
                }
                if (!isDeviceConfirmed) {
                    Column(horizontalAlignment = CenterHorizontally) {
                        if (devices.isNotEmpty()) Spacer(Modifier.height(24.dp))
                        DeviceList(devices, selected) {
                            logger(TAG) { "selected: $selected, it: $it" }
                            selected = if (selected == it) null else it
                        }
                        Spacer(Modifier.height(32.dp))
                        if (!isLoading || selected != null) DialogButton(text = btnText, onClick = onBtnClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<BleDevice>,
    selected: BleDevice?,
    onClick: (BleDevice) -> Unit = {},
) {
    LazyColumn(Modifier.fillMaxWidth()) {
        items(items = devices, key = { it.identifier }) { device ->
            val isSelected = selected?.identifier == device.identifier
            DeviceListItem(modifier = Modifier.animateItem(), isSelected = isSelected, device = device) {
                onClick(it)
            }
        }
    }
}

@Composable
@Preview()
fun ChooseHRDeviceDialogPreview() {
    Box(modifier = Modifier.padding().fillMaxWidth()) {
        HrConnectDialog(
            onConfirmClick = {},
            onDismissRequest = {},
            onRefresh = {},
            isLoading = true,
            devices = listOf(
                BleDevice("Hrm1", "00:11:22:33:44:55"),
                BleDevice("Hrm2", "66:77:88:99:AA:BB"),
                BleDevice("Hrm3", "CC:DD:EE:FF:00:11"),
            ),
            loadingDuration = 5.seconds
        )
    }
}
