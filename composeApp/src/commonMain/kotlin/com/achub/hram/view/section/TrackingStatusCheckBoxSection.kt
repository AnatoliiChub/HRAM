package com.achub.hram.view.section

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.achub.hram.data.models.TrackingStatus
import com.achub.hram.view.components.HRCheckBoxLabel
import com.achub.hram.view.indications.WarningLabelRow

@Composable
fun TrackingStatusCheckBoxSection(
    trackingStatus: TrackingStatus,
    isCheckBoxEnabled: Boolean,
    onHrCheckBox: () -> Unit,
) {
    if (trackingStatus.atLeastOneTrackingEnabled.not()) {
        WarningLabelRow(label = "Choose at least one tracking option")
    }
    Column {
        HRCheckBoxLabel(
            isChecked = trackingStatus.trackHR,
            isEnabled = isCheckBoxEnabled,
            connectedDevice = trackingStatus.hrDevice?.name
        ) { onHrCheckBox() }
    }
}
