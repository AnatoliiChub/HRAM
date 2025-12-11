package com.achub.hram.view.section

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.ble.model.BleDevice
import com.achub.hram.data.models.TrackingStatus
import com.achub.hram.view.components.HRCheckBoxLabel
import com.achub.hram.view.indications.WarningLabelRow
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.record_scree_choose_at_least_one_option

@Composable
fun TrackingStatusCheckBoxSection(
    trackingStatus: TrackingStatus,
    isCheckBoxEnabled: Boolean,
    onHrCheckBox: () -> Unit,
) {
    if (trackingStatus.atLeastOneTrackingEnabled.not()) {
        WarningLabelRow(label = Res.string.record_scree_choose_at_least_one_option)
    }
    Column {
        HRCheckBoxLabel(
            isChecked = trackingStatus.trackHR,
            isEnabled = isCheckBoxEnabled,
            connectedDevice = trackingStatus.hrDevice?.name
        ) { onHrCheckBox() }
    }
}

@Preview
@Composable
private fun TrackingStatusCheckBoxSectionPreview() {
    val trackingStatus = TrackingStatus(
        trackHR = true,
        hrDevice = BleDevice(name = "Polar H10", identifier = "00:11:22:33:44:55"),
    )
    TrackingStatusCheckBoxSection(
        trackingStatus = trackingStatus,
        isCheckBoxEnabled = true,
        onHrCheckBox = {},
    )
}
