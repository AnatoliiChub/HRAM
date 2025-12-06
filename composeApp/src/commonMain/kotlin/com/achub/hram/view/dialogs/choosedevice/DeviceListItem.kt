package com.achub.hram.view.dialogs.choosedevice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.achub.hram.ble.model.BleDevice
import com.achub.hram.style.Dimen4
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.LabelSmall
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
            .clip(RoundedCornerShape(Dimen4))
            .fillMaxWidth()
            .clickable { onSelected(device) }
    ) {
        Text(text = device.name, color = color, style = LabelMediumBold)
        Text(text = device.identifier, color = color, style = LabelSmall)
    }
}
