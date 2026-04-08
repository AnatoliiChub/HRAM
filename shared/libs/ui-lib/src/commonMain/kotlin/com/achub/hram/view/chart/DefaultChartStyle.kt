package com.achub.hram.view.chart

import androidx.compose.runtime.Composable
import com.achub.hram.ext.dpToPx
import com.achub.hram.ext.formatTime
import com.achub.hram.style.Dimen1
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen2
import com.achub.hram.style.Dimen4
import com.achub.hram.style.Dimen8
import com.achub.hram.style.LabelSmall
import com.achub.hram.style.Red
import com.achub.hram.style.Red20
import com.achub.hram.style.White10
import com.achub.hram.style.White30
import hram.ui_lib.generated.resources.Res
import hram.ui_lib.generated.resources.seconds_unit
import org.jetbrains.compose.resources.stringResource

const val Y_LABEL_COUNT = 5
const val X_LABEL_COUNT = 4

@Composable
fun defaultChartStyle(): ChartStyle {
    val secondsLabel = stringResource(Res.string.seconds_unit)
    return ChartStyle(
        bubbleXOverlap = Dimen16.dpToPx(),
        bubbleYOffset = Dimen8.dpToPx(),
        lineWidth = Dimen2.dpToPx(),
        gridLineWidth = Dimen1.dpToPx(),
        gridLineDashLength = Dimen8.dpToPx(),
        xAxisOffset = Dimen4.dpToPx(),
        yAxisOffset = Dimen4.dpToPx(),
        axisTextStyle = LabelSmall,
        yLabelCount = Y_LABEL_COUNT,
        xLabelCount = X_LABEL_COUNT,
        gridLineColor = White10,
        axisLineColor = White30,
        pathColor = Red,
        areaColor = Red20,
        yLabelFormatter = { "${it.toInt()}" },
        xLabelFormatter = { formatTime(it.toLong(), secondsLabel) }
    )
}
