package com.achub.hram.view.chart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import com.achub.hram.style.Black
import com.achub.hram.style.Dimen120
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen4
import com.achub.hram.style.Dimen8
import com.achub.hram.style.LabelBigBold
import com.achub.hram.style.LabelSmall
import com.achub.hram.style.LightRed
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.chart_bubble_bpm
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChartBubble(xLabel: String, yLabel: String) {
    Card(
        modifier = Modifier.shadow(Dimen4, RoundedCornerShape(Dimen8))
            .widthIn(min = Dimen120),
        colors = CardDefaults.cardColors(containerColor = LightRed)
    ) {
        Column(Modifier.padding(Dimen16)) {
            Text(
                modifier = Modifier.padding(bottom = Dimen8),
                text = stringResource(Res.string.chart_bubble_bpm, yLabel),
                style = LabelBigBold.copy(color = Black)
            )
            Text(text = xLabel, style = LabelSmall.copy(color = Black))
        }
    }
}
