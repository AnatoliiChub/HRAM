package com.achub.hram.view.chart

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

data class ChartStyle(
    val bubbleXOverlap: Float,
    val bubbleYOffset: Float,
    val lineWidth: Float,
    val gridLineWidth: Float,
    val gridLineDashLength: Float,
    val xAxisOffset: Float,
    val yAxisOffset: Float,
    val axisTextStyle: TextStyle,
    val yLabelCount: Int,
    val xLabelCount: Int,
    val gridLineColor: Color,
    val axisLineColor: Color,
    val pathColor: Color,
    val areaColor: Color,
    val yLabelFormatter: (Float) -> String,
    val xLabelFormatter: (Float) -> String,
)
