package com.achub.hram.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.achub.hram.data.model.BleDevice
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.Red
import com.achub.hram.style.White

@Composable
fun DeviceListItem(
    modifier: Modifier,
    isSelected: Boolean,
    device: BleDevice,
    onSelected: (BleDevice) -> Unit
) {
    val color = if (isSelected) Red else White
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelected(device) }) {
        Text(
            text = device.name,
            color = color,
            style = LabelMediumBold,
        )
        Text(
            text = device.identifier,
            color = color,
            style = LabelMedium,
        )
    }
}