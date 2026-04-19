package com.achub.hram.view.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.achub.hram.models.GraphLimitsUi
import kotlin.math.abs
import kotlin.math.max

@Composable
fun AreaChart(
    modifier: Modifier,
    chartData: ChartData,
    style: ChartStyle,
    onHighLight: (Pair<Float, Float>?) -> Unit,
    bubble: @Composable (xText: String, yText: String) -> Unit
) {
    val points = chartData.points
    if (points.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val size = remember { mutableStateOf(IntSize.Zero) }
    val bubbleSize = remember { mutableStateOf(IntSize.Zero) }
    val plotDims = rememberPlotDimensions(chartData, style, size.value, textMeasurer)
    val selectedPoint = chartData.highLighted
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(selectedPoint) {
        if (selectedPoint != null) haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    val paths = rememberChartPaths(points, chartData.limits, plotDims)

    Box(modifier) {
        ChartBubble(selectedPoint, chartData.limits, plotDims, bubbleSize, style, bubble)

        Canvas(
            modifier = Modifier.matchParentSize()
                .onSizeChanged { size.value = it }
                .chartInput(points, chartData.limits, plotDims, onHighLight)
        ) {
            drawChartContent(style, plotDims, paths, chartData.limits, textMeasurer, selectedPoint)
        }
    }
}

private fun DrawScope.drawChartContent(
    style: ChartStyle,
    plotDims: PlotDimensions,
    paths: ChartPaths,
    limits: GraphLimitsUi,
    textMeasurer: TextMeasurer,
    selectedPoint: Pair<Float, Float>?
) {
    drawGridLines(style, plotDims)
    drawChartData(paths, style)
    drawAxes(style, plotDims)
    drawLabels(limits, style, plotDims, textMeasurer)
    drawSelection(selectedPoint, limits, plotDims, style)
}

private data class PlotDimensions(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val width: Float,
    val height: Float,
    val xLabelPositions: List<Pair<Int, Float>>,
    val yLabelWidths: List<Float>,
    val maxYLabelHeight: Float
)

@Composable
private fun rememberPlotDimensions(
    chartData: ChartData,
    style: ChartStyle,
    size: IntSize,
    textMeasurer: TextMeasurer
): PlotDimensions {
    val limits = chartData.limits
    val yLastIdx = style.yLabelCount - 1
    val xLastIdx = style.xLabelCount - 1

    return remember(limits, style, size) {
        val yLabelsWidth = (0..yLastIdx).map {
            val yValue = limits.minY + it * (limits.maxY - limits.minY) / yLastIdx
            textMeasurer.measure(
                AnnotatedString(style.yLabelFormatter(yValue)),
                style.axisTextStyle
            ).size.width.toFloat()
        }
        val maxYLabelWidth = (yLabelsWidth.maxOrNull() ?: 0f) + style.yAxisOffset
        val maxYLabelHeight = (0..yLastIdx).maxOf {
            val yValue = limits.minY + it * (limits.maxY - limits.minY) / yLastIdx
            textMeasurer.measure(
                AnnotatedString(style.yLabelFormatter(yValue)),
                style.axisTextStyle
            ).size.height.toFloat()
        }
        val maxXLabelHeight = (0..xLastIdx).maxOf {
            textMeasurer.measure(AnnotatedString(it.toString()), style.axisTextStyle).size.height.toFloat()
        } +
            style.xAxisOffset

        val plotRight = size.width.toFloat()
        val plotBottom = (size.height - maxXLabelHeight).coerceAtLeast(0f)
        val plotWidth = (plotRight - maxYLabelWidth).coerceAtLeast(1f)
        val plotHeight = (plotBottom - 0f).coerceAtLeast(1f)

        val xLabelPositions = (0..xLastIdx).map {
            val xValue = (limits.minX + (it.toFloat() * (limits.maxX - limits.minX) / xLastIdx)).toInt()
            val ratio = (xValue - limits.minX) / (limits.maxX - limits.minX)
            xValue to (maxYLabelWidth + ratio * plotWidth)
        }

        PlotDimensions(
            left = maxYLabelWidth,
            top = 0f,
            right = plotRight,
            bottom = plotBottom,
            width = plotWidth,
            height = plotHeight,
            xLabelPositions = xLabelPositions,
            yLabelWidths = yLabelsWidth,
            maxYLabelHeight = maxYLabelHeight
        )
    }
}

private data class ChartPaths(val line: Path, val area: Path)

@Composable
private fun rememberChartPaths(
    points: List<Pair<Float, Float>>,
    limits: GraphLimitsUi,
    dims: PlotDimensions
): ChartPaths = remember(points, limits, dims) {
    val linePath = Path()
    val areaPath = Path()
    if (points.isEmpty()) return@remember ChartPaths(linePath, areaPath)

    fun map(p: Pair<Float, Float>): Offset {
        val fx = (p.first - limits.minX) / (limits.maxX - limits.minX)
        val fy = (p.second - limits.minY) / (limits.maxY - limits.minY)
        return Offset(dims.left + fx * dims.width, dims.bottom - fy * dims.height)
    }

    val first = map(points.first())
    linePath.moveTo(first.x, first.y)
    areaPath.moveTo(first.x, dims.bottom)
    areaPath.lineTo(first.x, first.y)

    points.drop(1).forEach {
        val pt = map(it)
        linePath.lineTo(pt.x, pt.y)
        areaPath.lineTo(pt.x, pt.y)
    }
    areaPath.lineTo(map(points.last()).x, dims.bottom)
    areaPath.close()
    ChartPaths(linePath, areaPath)
}

private fun Modifier.chartInput(
    points: List<Pair<Float, Float>>,
    limits: GraphLimitsUi,
    dims: PlotDimensions,
    onHighLight: (Pair<Float, Float>?) -> Unit
): Modifier = this.pointerInput(points, limits, dims) {
    detectTapGestures(onTap = { offset ->
        onHighLight(mapPixelToData(offset, points, limits, dims))
    })
}.pointerInput(points, limits, dims) {
    detectDragGesturesAfterLongPress(
        onDragStart = { offset -> onHighLight(mapPixelToData(offset, points, limits, dims)) },
        onDrag = { change, _ -> onHighLight(mapPixelToData(change.position, points, limits, dims)) },
        onDragEnd = { onHighLight(null) }
    )
}

private fun mapPixelToData(
    offset: Offset,
    points: List<Pair<Float, Float>>,
    limits: GraphLimitsUi,
    dims: PlotDimensions
): Pair<Float, Float>? {
    if (dims.width <= 0f) return null
    val fx = (offset.x - dims.left) / dims.width
    val x = limits.minX + fx * (limits.maxX - limits.minX)
    return points.minByOrNull { abs(it.first - x) }
}

private fun mapDataToPixel(
    p: Pair<Float, Float>,
    limits: GraphLimitsUi,
    dims: PlotDimensions
): Offset {
    val fx = (p.first - limits.minX) / (limits.maxX - limits.minX)
    val fy = (p.second - limits.minY) / (limits.maxY - limits.minY)
    return Offset(dims.left + fx * dims.width, dims.bottom - fy * dims.height)
}

@Composable
private fun ChartBubble(
    selectedPoint: Pair<Float, Float>?,
    limits: GraphLimitsUi,
    dims: PlotDimensions,
    bubbleSize: MutableState<IntSize>,
    style: ChartStyle,
    bubble: @Composable (xText: String, yText: String) -> Unit
) {
    if (selectedPoint == null) return
    val spOffset = mapDataToPixel(selectedPoint, limits, dims)

    val bubbleModifier = Modifier.offset {
        val halfWidth = bubbleSize.value.width / 2
        var x = spOffset.x.toInt() - halfWidth
        val y = dims.top.toInt() - bubbleSize.value.height
        x = style.calculateX(x, dims.left, dims.right, bubbleSize.value)
        IntOffset(x, y - style.bubbleYOffset.toInt())
    }.onSizeChanged { bubbleSize.value = it }

    Box(bubbleModifier) {
        bubble(style.xLabelFormatter(selectedPoint.first), style.yLabelFormatter(selectedPoint.second))
    }
}

private fun DrawScope.drawGridLines(
    style: ChartStyle,
    dims: PlotDimensions
) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(style.gridLineDashLength, style.gridLineDashLength), 0f)
    val yLast = style.yLabelCount - 1
    for (i in 1 until style.yLabelCount) {
        val yPos = dims.bottom - i * dims.height / yLast
        drawLine(
            style.gridLineColor,
            Offset(dims.left, yPos),
            Offset(dims.right, yPos),
            style.gridLineWidth,
            pathEffect = dash
        )
    }
    dims.xLabelPositions.forEach { (_, xPos) ->
        drawLine(
            style.gridLineColor,
            Offset(xPos, dims.top),
            Offset(xPos, dims.bottom),
            style.gridLineWidth,
            pathEffect = dash
        )
    }
}

private fun DrawScope.drawChartData(
    paths: ChartPaths,
    style: ChartStyle
) {
    drawPath(paths.area, style.areaColor)
    drawPath(paths.line, style.pathColor, style = Stroke(style.lineWidth))
}

private fun DrawScope.drawAxes(
    style: ChartStyle,
    dims: PlotDimensions
) {
    drawLine(style.axisLineColor, Offset(dims.left, dims.top), Offset(dims.left, dims.bottom), style.lineWidth)
    drawLine(style.axisLineColor, Offset(dims.left, dims.bottom), Offset(dims.right, dims.bottom), style.lineWidth)
}

private fun DrawScope.drawLabels(
    limits: GraphLimitsUi,
    style: ChartStyle,
    dims: PlotDimensions,
    textMeasurer: TextMeasurer
) {
    // X Labels
    dims.xLabelPositions.forEach { (xVal, xPosRaw) ->
        val text = style.xLabelFormatter(xVal.toFloat())
        val layout = textMeasurer.measure(AnnotatedString(text), style.axisTextStyle)
        val tw = layout.size.width.toFloat()
        val xPos = (xPosRaw - tw / 2f).coerceIn(dims.left, max(dims.left, dims.right - tw))
        drawText(textMeasurer, text, Offset(xPos, dims.bottom + style.xAxisOffset), style.axisTextStyle)
    }
    // Y Labels
    val yLast = style.yLabelCount - 1
    for (i in 0..yLast) {
        val yVal = limits.minY + i * (limits.maxY - limits.minY) / yLast
        val yPos = calculateY(i, dims.bottom - i * dims.height / yLast, dims.maxYLabelHeight, yLast)
        val text = style.yLabelFormatter(yVal)
        val tw = dims.yLabelWidths[i]
        drawText(textMeasurer, text, Offset(dims.left - style.yAxisOffset - tw, yPos), style.axisTextStyle)
    }
}

private fun DrawScope.drawSelection(
    sp: Pair<Float, Float>?,
    limits: GraphLimitsUi,
    dims: PlotDimensions,
    style: ChartStyle
) {
    if (sp == null) return
    val spOffset = mapDataToPixel(sp, limits, dims)
    drawLine(
        style.pathColor,
        Offset(spOffset.x, spOffset.y),
        Offset(spOffset.x, dims.top - style.bubbleYOffset),
        strokeWidth = style.lineWidth
    )
    drawCircle(
        style.pathColor,
        radius = style.lineWidth * 2.5f,
        center = spOffset
    )
}

private fun calculateY(
    i: Int,
    yPos: Float,
    maxYLabelHeight: Float,
    yLastLabelNumber: Int
): Float {
    return if (i == 0) {
        yPos - maxYLabelHeight
    } else if (i != yLastLabelNumber) {
        yPos - maxYLabelHeight / 2
    } else {
        yPos
    }
}

private fun ChartStyle.calculateX(
    x: Int,
    plotLeft: Float,
    plotRight: Float,
    bubbleSize: IntSize
) = when {
    x < plotLeft - bubbleXOverlap -> plotLeft.toInt() - bubbleXOverlap.toInt()

    x > plotRight - bubbleSize.width + bubbleXOverlap ->
        plotRight.toInt() - bubbleSize.width + bubbleXOverlap.toInt()

    else -> x
}
