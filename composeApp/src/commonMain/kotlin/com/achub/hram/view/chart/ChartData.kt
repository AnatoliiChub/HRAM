package com.achub.hram.view.chart

import com.achub.hram.data.models.GraphLimits

data class ChartData(
    val points: List<Pair<Float, Float>>,
    val limits: GraphLimits,
    val highLighted: Pair<Float, Float>? = null,
)
