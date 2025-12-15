package com.achub.hram.view.dialogs.choosedevice

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.style.Dimen24
import com.achub.hram.style.Dimen32
import com.achub.hram.view.components.HRProgress
import com.achub.hram.view.components.dialog.DialogButton
import com.achub.hram.view.components.dialog.DialogElevatedCard
import com.achub.hram.view.components.dialog.DialogMessage
import com.achub.hram.view.components.dialog.DialogTitle
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.dialog_choose_hr_device_connect
import hram.composeapp.generated.resources.dialog_choose_hr_device_connect_device
import hram.composeapp.generated.resources.dialog_choose_hr_device_connecting
import hram.composeapp.generated.resources.dialog_choose_hr_device_no_device_found
import hram.composeapp.generated.resources.dialog_choose_hr_device_no_device_message
import hram.composeapp.generated.resources.dialog_choose_hr_device_retry
import hram.composeapp.generated.resources.dialog_choose_hr_device_scanning
import hram.composeapp.generated.resources.dialog_choose_hr_device_scanning_message
import hram.composeapp.generated.resources.dialog_choose_hr_device_select_device_message
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
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
    val title = when {
        isDeviceConfirmed -> Res.string.dialog_choose_hr_device_connecting
        isLoading -> Res.string.dialog_choose_hr_device_scanning
        devices.isNotEmpty() -> Res.string.dialog_choose_hr_device_connect_device
        else -> Res.string.dialog_choose_hr_device_no_device_found
    }
    val message = provideDialogMessage(isLoading, devices)

    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        val btnText = if (retryState) {
            Res.string.dialog_choose_hr_device_retry
        } else {
            Res.string.dialog_choose_hr_device_connect
        }
        val onBtnClick: () -> Unit = {
            when {
                selected != null -> selected?.let { onConfirmClick(it) }
                retryState -> onRefresh()
            }
        }
        DialogElevatedCard(animate = isLoading) {
            Column(modifier = Modifier.padding(Dimen24), horizontalAlignment = CenterHorizontally) {
                DialogTitle(title = title)
                Spacer(Modifier.height(Dimen24))

                if (isLoading) {
                    HRProgress(isLoading, cycleDuration = loadingDuration)
                } else {
                    DialogMessage(message = stringResource(message))
                }

                if (!isDeviceConfirmed) {
                    Content(devices, selected, isLoading, btnText, onSelected = { selected = it }, onBtnClick)
                }
            }
        }
    }
}

@Composable
private fun Content(
    devices: List<BleDevice>,
    selected: BleDevice?,
    isLoading: Boolean,
    btnText: StringResource,
    onSelected: (BleDevice?) -> Unit,
    onBtnClick: () -> Unit
) {
    Column(horizontalAlignment = CenterHorizontally) {
        if (devices.isNotEmpty()) Spacer(Modifier.height(Dimen24))
        DeviceList(devices, selected) { onSelected(if (selected == it) null else it) }
        Spacer(Modifier.height(Dimen32))
        if (!isLoading || selected != null) {
            DialogButton(
                text = stringResource(btnText),
                onClick = onBtnClick
            )
        }
    }
}

private fun provideDialogMessage(isLoading: Boolean, devices: List<BleDevice>) = when {
    isLoading -> Res.string.dialog_choose_hr_device_scanning_message
    devices.isEmpty() -> Res.string.dialog_choose_hr_device_no_device_message
    else -> Res.string.dialog_choose_hr_device_select_device_message
}

@Composable
private fun DeviceList(devices: List<BleDevice>, selected: BleDevice?, onClick: (BleDevice) -> Unit) {
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
@Preview
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
