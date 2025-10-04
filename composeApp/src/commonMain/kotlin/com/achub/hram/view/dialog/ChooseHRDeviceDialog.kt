package com.achub.hram.view.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.achub.hram.data.model.BleDevice
import com.achub.hram.style.DarkGray
import com.achub.hram.view.DeviceListItem
import com.achub.hram.view.HRProgress
import com.achub.hram.view.dialog.base.DialogButton
import com.achub.hram.view.dialog.base.DialogElevatedCard
import com.achub.hram.view.dialog.base.DialogTitle
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChooseHRDeviceDialog(
    isLoading: Boolean,
    devices: List<BleDevice>,
    loadingDuration: Duration,
    onConfirmClick: (BleDevice) -> Unit,
    onRefresh: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf<BleDevice?>(null) }
    val retryState = selected == null
    val title = if (isLoading) "Scanning for HR-devices" else "Please, choose a heart rate device to Pair:"
    val backgroundCardColor = DarkGray
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        val onClick: () -> Unit = {
            when {
                isLoading -> {}
                retryState -> onRefresh()
                else -> selected?.let { onConfirmClick(it) }
            }
        }
        DialogElevatedCard(backgroundCardColor) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = CenterHorizontally
            ) {
                DialogTitle(title)
                Spacer(Modifier.height(24.dp))
                AnimatedVisibility(isLoading) {
                    HRProgress(isLoading, cycleDuration = loadingDuration)
                }
                Spacer(Modifier.height(24.dp))
                DeviceList(devices, selected, isLoading) {
                    selected = if (selected == it) null else it
                }
                Spacer(Modifier.height(24.dp))
                AnimatedVisibility(!isLoading, enter = expandIn(), exit = shrinkOut()) {
                    DialogButton(text = if (retryState) "Retry" else "Connect", onClick = onClick)
                }
            }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<BleDevice>,
    selected: BleDevice?,
    isLoading: Boolean,
    onClick: ((BleDevice) -> Unit) = {},
) {
    LazyColumn(Modifier.fillMaxWidth()) {
        items(items = devices, key = { it.identifier }) { device ->
            val isSelected = selected?.identifier == device.identifier
            DeviceListItem(modifier = Modifier.animateItem(), isSelected = isSelected, device = device) {
                if (isLoading) return@DeviceListItem
                onClick(it)
            }
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
        ),
        loadingDuration = 5.seconds
    )
}