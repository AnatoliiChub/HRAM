package com.achub.hram.view.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.achub.hram.data.model.TrackingStatus
import com.achub.hram.view.HRCheckBoxLabel
import com.achub.hram.view.LocationCheckBoxLabel
import com.achub.hram.view.indications.WarningLabelRow

@Composable
fun TrackingStatusCheckBoxSection(
    trackingStatus: TrackingStatus,
    isCheckBoxEnabled: Boolean,
    onHrCheckBox: () -> Unit,
    onLocationCheckBox: () -> Unit
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
        LocationCheckBoxLabel(
            isChecked = trackingStatus.trackGps,
            isEnabled = isCheckBoxEnabled
        ) { onLocationCheckBox() }
        Spacer(Modifier.height(32.dp))
    }
}