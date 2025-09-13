package com.achub.hram.view.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Red
import com.achub.hram.style.White
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseHRDeviceDialog(
    onConfirmClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    devices: List<String>,
) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }

    BaseDialog(
        title = "Please, choose a heart rate device:",
        onDismissRequest = onDismissRequest,
        isButtonEnabled = selected != null,
        onConfirmClick = { onConfirmClick(selected ?: "") }
    ) {
        PullToRefreshBox(
            modifier = Modifier.height(210.dp),
            isRefreshing = isLoading,
            onRefresh = onRefresh,
        ) {
            DeviceList(devices = devices, selected) {
                selected = it
            }
        }
    }
}

@Composable
fun DeviceList(devices: List<String>, selected: String?, onSelected: (String) -> Unit = {}) {
    LazyColumn {
        items(devices) { deviceId ->
            Text(
                text = deviceId,
                color = if (selected == deviceId) Red else White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(deviceId) }
                    .padding(16.dp)
            )
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
            "Device 1",
            "Device 2",
            "Device 3",
            "Device 4",
            "Device 5",
        )
    )
}