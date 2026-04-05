package com.achub.hram.view.chart

import com.achub.hram.models.GraphLimitsUi

data class ChartData(
    val points: List<Pair<Float, Float>>,
    val limits: GraphLimitsUi,
    val highLighted: Pair<Float, Float>? = null,
)
