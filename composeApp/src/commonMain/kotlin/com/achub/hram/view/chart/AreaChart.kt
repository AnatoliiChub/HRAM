package com.achub.hram.view.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
    with(style) {
        val points = chartData.points
        val textMeasurer = rememberTextMeasurer()
        val size: MutableState<IntSize> = remember { mutableStateOf(IntSize(0, 0)) }
        var plotLeft by remember { mutableStateOf(0f) }
        var plotTop by remember { mutableStateOf(0f) }
        var plotRight by remember { mutableStateOf(0f) }
        var plotBottom by remember { mutableStateOf(0f) }
        var plotWidth by remember { mutableStateOf(0f) }
        var plotHeight by remember { mutableStateOf(0f) }
        var selectedPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
        val bubbleSize = remember { mutableStateOf(IntSize.Zero) }
        val yLastLabelNumber by remember { mutableStateOf(yLabelCount - 1) }
        val xLastLabelNumber by remember { mutableStateOf(xLabelCount - 1) }
        val gridLinePath = remember {
            PathEffect.dashPathEffect(floatArrayOf(gridLineDashLength, gridLineDashLength), 0f)
        }
        val haptic = LocalHapticFeedback.current
        LaunchedEffect(selectedPoint) {
            if (selectedPoint != null) haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
        val (minX, maxX, minY, maxY) = chartData.limits

        val yLabelsWidth = remember(minY, maxY) {
            (0..yLastLabelNumber).map {
                val yValue = minY + it * (maxY - minY) / yLastLabelNumber
                val text = yLabelFormatter(yValue)
                textMeasurer.measure(text = AnnotatedString(text), style = axisTextStyle).size.width.toFloat()
            }
        }.let { it + yAxisOffset }

        val maxYLabelWidth = remember(minY, maxY) { yLabelsWidth.maxOrNull() ?: 0f }

        val maxYLabelHeight = remember(minY, maxY) {
            (0..yLastLabelNumber).maxOf {
                val yValue = minY + it * (maxY - minY) / yLastLabelNumber
                val text = yLabelFormatter(yValue)
                textMeasurer.measure(text = AnnotatedString(text), style = axisTextStyle).size.height.toFloat()
            }
        }

        val maxXLabelHeight = remember(minX, maxX) {
            (0..xLastLabelNumber).maxOf {
                val xValue = minX + it * (maxX - minX) / xLastLabelNumber
                val text = xValue.toString()
                textMeasurer.measure(text = AnnotatedString(text), style = axisTextStyle).size.height.toFloat()
            }
        }.let { it + xAxisOffset }

        val xLabelAndPosition = remember(minX, maxX, plotWidth, maxYLabelWidth) {
            (0..xLastLabelNumber).map {
                // Compute raw value, then round
                val rawXValue = minX + (it.toFloat() * (maxX - minX) / xLastLabelNumber)
                val xValue = rawXValue.toInt()

                // Recompute position *from the rounded value*
                // ratio = (roundedValue - minX) / (maxX - minX)
                val ratio = (xValue - minX) / (maxX - minX)
                Pair(xValue, maxYLabelWidth + ratio * plotWidth)
            }
        }
        selectedPoint = chartData.highLighted

        fun mapToPixel(p: Pair<Float, Float>): Offset {
            val x = p.first
            val y = p.second
            val fx = (x - minX) / (maxX - minX)
            val fy = (y - minY) / (maxY - minY)
            val px = plotLeft + fx * plotWidth
            val py = plotBottom - fy * plotHeight
            return Offset(px, py)
        }

        fun mapPixelToData(offset: Offset): Pair<Float, Float>? {
            val fx = (offset.x - plotLeft) / plotWidth
            val x = minX + fx * (maxX - minX)
            //find nearest x in points
            val nearestPoint = points.minByOrNull { abs(it.first - x) }
            return nearestPoint
        }

        val (linePath, areaPath) = remember(points, minX, maxX, minY, maxY, size.value) {
            val linePath = Path()
            val areaPath = Path()
            plotLeft = maxYLabelWidth
            plotRight = size.value.width.toFloat()
            plotTop = 0f
            plotBottom = size.value.height - maxXLabelHeight
            plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)
            plotHeight = (plotBottom - plotTop).coerceAtLeast(1f)

            val firstOffset = mapToPixel(points.first())
            linePath.moveTo(firstOffset.x, firstOffset.y)
            areaPath.moveTo(firstOffset.x, plotBottom)
            areaPath.lineTo(firstOffset.x, firstOffset.y)

            for (i in 1 until points.size) {
                val pt = mapToPixel(points[i])
                linePath.lineTo(pt.x, pt.y)
                areaPath.lineTo(pt.x, pt.y)
            }
            val lastOffset = mapToPixel(points.last())
            areaPath.lineTo(lastOffset.x, plotBottom)
            areaPath.close()

            Pair(linePath, areaPath)
        }

        Box(modifier) {
            //draw bubble at selected point
            selectedPoint?.let { sp ->
                val bubbleModifier = Modifier.offset {
                    val halfButtonWidth = bubbleSize.value.width / 2
                    var x = mapToPixel(sp).x.toInt() - halfButtonWidth
                    val y = plotTop.toInt() - bubbleSize.value.height
                    if (x < plotLeft - bubbleXOverlap) {
                        x = plotLeft.toInt() - bubbleXOverlap.toInt()
                    } else if (x > plotRight - bubbleSize.value.width + bubbleXOverlap) {
                        x = plotRight.toInt() - bubbleSize.value.width + bubbleXOverlap.toInt()
                    }
                    IntOffset(x, y - bubbleYOffset.toInt())
                }.onSizeChanged { bubbleSize.value = it }

                Box(bubbleModifier) { bubble(xLabelFormatter(sp.first), yLabelFormatter(sp.second)) }
            }
            //draw
            Canvas(
                modifier = Modifier.matchParentSize()
                    .onSizeChanged { canvasSize -> size.value = IntSize(canvasSize.width, canvasSize.height) }
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onHighLight(mapPixelToData(it)) })
                    }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onHighLight(mapPixelToData(it)) },
                            onDrag = { change, _ -> onHighLight(mapPixelToData(change.position)) },
                            onDragEnd = { onHighLight(null) }
                        )
                    }
            ) {
                if (points.isEmpty()) return@Canvas

                plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)
                plotHeight = (plotBottom - plotTop).coerceAtLeast(1f)

                // draw horizontal grid lines dash lines
                for (i in 1 until yLabelCount) {
                    val yPos = plotBottom - i * plotHeight / yLastLabelNumber
                    drawLine(
                        gridLineColor,
                        Offset(plotLeft, yPos),
                        Offset(plotRight, yPos),
                        strokeWidth = gridLineWidth,
                        pathEffect = gridLinePath
                    )
                }
                // draw vertical grid lines dash lines
                for (i in 0..xLastLabelNumber) {
                    val xPos = xLabelAndPosition[i].second
                    drawLine(
                        gridLineColor,
                        Offset(xPos, plotTop),
                        Offset(xPos, plotBottom),
                        strokeWidth = gridLineWidth,
                        pathEffect = gridLinePath
                    )
                }
                // draw area path
                drawPath(path = areaPath, color = areaColor)

                // draw line path
                drawPath(path = linePath, color = pathColor, style = Stroke(width = lineWidth))
                // draw Y axis
                drawLine(
                    axisLineColor,
                    Offset(plotLeft, plotTop),
                    Offset(plotLeft, plotBottom),
                    strokeWidth = lineWidth
                )
                // draw X axis
                drawLine(
                    axisLineColor,
                    Offset(plotLeft, plotBottom),
                    Offset(plotRight, plotBottom),
                    strokeWidth = lineWidth
                )
                // Draw X-axis labels
                for (i in 0..xLastLabelNumber) {
                    val xPosRaw = xLabelAndPosition[i].second
                    // Prepare label
                    val text = xLabelFormatter(xLabelAndPosition[i].first.toFloat())
                    val textLayout = textMeasurer.measure(text = AnnotatedString(text), style = axisTextStyle)
                    val textWidth = textLayout.size.width.toFloat()

                    // Center & clamp inside plot
                    val min = plotLeft
                    val max = plotRight - textWidth
                    val xPos = (xPosRaw - textWidth / 2f).coerceIn(min, max(min, max))

                    drawText(
                        textMeasurer = textMeasurer,
                        text = text,
                        style = axisTextStyle,
                        topLeft = Offset(xPos, plotBottom + xAxisOffset)
                    )
                }

                // Draw Y-axis labels
                for (i in 0..yLastLabelNumber) {
                    val yValue = minY + i * (maxY - minY) / yLastLabelNumber
                    var yPos = plotBottom - i * plotHeight / yLastLabelNumber
                    if (i == 0) yPos -= maxYLabelHeight else if (i != yLastLabelNumber) yPos -= maxYLabelHeight / 2
                    val labelWidth = yLabelsWidth[i]
                    drawText(
                        textMeasurer = textMeasurer,
                        text = yLabelFormatter(yValue),
                        style = axisTextStyle,
                        topLeft = Offset(plotLeft - yAxisOffset - labelWidth, yPos)
                    )
                }

                // Draw selected point and vertical line
                selectedPoint?.let { sp ->
                    val spOffset = mapToPixel(sp)
                    drawLine(
                        pathColor,
                        Offset(spOffset.x, spOffset.y),
                        Offset(spOffset.x, plotTop - bubbleYOffset),
                        strokeWidth = lineWidth
                    )
                    drawCircle(
                        pathColor,
                        radius = lineWidth * 2.5f,
                        center = spOffset
                    )
                }
            }
        }
    }
}
