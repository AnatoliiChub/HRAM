package com.achub.hram.view.section

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import com.achub.hram.data.model.TrackingIndications
import com.achub.hram.style.Heading1
import com.achub.hram.style.White
import com.achub.hram.view.DistanceLabelRow
import com.achub.hram.view.HeartLabelRow

@Composable
fun TrackingIndicationsSection(indications: TrackingIndications) {
    Column {
        HeartLabelRow(hrBpm = indications.heartRate)
        DistanceLabelRow(distance = indications.distance)
        Text(
            modifier = Modifier.align(CenterHorizontally),
            text = indications.duration,
            style = Heading1.copy(color = White, fontWeight = W500)
        )
    }
}