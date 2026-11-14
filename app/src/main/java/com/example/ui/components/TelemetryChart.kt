package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HealthLog
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.DeepSpaceSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateGray
import com.example.ui.theme.StarWhite
import com.example.ui.theme.StatusCriticalRed
import com.example.ui.theme.StatusNominalGreen
import com.example.ui.theme.StatusWarningOrange

enum class ChartMetricType(
    val title: String,
    val unit: String,
    val lineAccent: Color,
    val normalMin: Float,
    val normalMax: Float,
    val isUpperThresholdDanger: Boolean = true
) {
    HEART_RATE("Heart Rate", "bpm", NeonCyan, 55f, 95f),
    BLOOD_PRESSURE("Systolic BP", "mmHg", ElectricBlue, 90f, 125f),
    RADIATION("Daily Radiation", "mSv", StatusWarningOrange, 0f, 1.2f),
    SLEEP("Sleep Architecture", "hrs", Color(0xFFB57EDC), 7f, 9f, false),
    SANS_EYE("Optic Nerve (ONSD)", "mm", StatusCriticalRed, 4.5f, 5.5f)
}

@Composable
fun TelemetryChart(
    logs: List<HealthLog>,
    metricType: ChartMetricType,
    modifier: Modifier = Modifier
) {
    if (logs.isEmpty()) return

    val sortedLogs = logs.sortedBy { it.missionDay }
    var selectedIndex by remember(logs.size) { mutableStateOf<Int?>(null) }

    val dataPoints: List<Pair<Int, Float>> = sortedLogs.map { log ->
        val value = when (metricType) {
            ChartMetricType.HEART_RATE -> log.heartRate.toFloat()
            ChartMetricType.BLOOD_PRESSURE -> log.systolicBp.toFloat()
            ChartMetricType.RADIATION -> log.dailyRadiationDoseMsv.toFloat()
            ChartMetricType.SLEEP -> log.sleepHours.toFloat()
            ChartMetricType.SANS_EYE -> log.opticNerveSheathDiameterMm.toFloat()
        }
        Pair(log.missionDay, value)
    }

    val values = dataPoints.map { it.second }
    val minVal = (values.minOrNull() ?: 0f).coerceAtMost(metricType.normalMin * 0.9f)
    val maxVal = (values.maxOrNull() ?: 100f).coerceAtLeast(metricType.normalMax * 1.15f)
    val valRange = if (maxVal - minVal == 0f) 1f else maxVal - minVal

    val activeLog = selectedIndex?.let { sortedLogs.getOrNull(it) } ?: sortedLogs.lastOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("telemetry_chart_${metricType.name.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2F57))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Chart Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MISSION ELAPSED TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = metricType.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    )
                }

                // Active Scrubber Info
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DeepSpaceSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, metricType.lineAccent.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MET D${activeLog?.missionDay ?: 0}: ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SlateGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        )
                        val activeVal = when (metricType) {
                            ChartMetricType.HEART_RATE -> "${activeLog?.heartRate ?: 0} bpm"
                            ChartMetricType.BLOOD_PRESSURE -> "${activeLog?.systolicBp ?: 0}/${activeLog?.diastolicBp ?: 0} mmHg"
                            ChartMetricType.RADIATION -> "${activeLog?.dailyRadiationDoseMsv ?: 0.0} mSv"
                            ChartMetricType.SLEEP -> "${activeLog?.sleepHours ?: 0.0} hrs"
                            ChartMetricType.SANS_EYE -> "${activeLog?.opticNerveSheathDiameterMm ?: 0.0} mm"
                        }
                        Text(
                            text = activeVal,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = metricType.lineAccent,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B18))
                    .border(1.dp, Color(0xFF14203D), RoundedCornerShape(12.dp))
                    .pointerInput(dataPoints) {
                        detectTapGestures { tapOffset ->
                            val width = size.width
                            val step = width / (dataPoints.size - 1).coerceAtLeast(1)
                            val clickedIndex = ((tapOffset.x + step / 2) / step).toInt().coerceIn(0, dataPoints.lastIndex)
                            selectedIndex = clickedIndex
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw Horizontal Gridlines & Baseline
                    val gridColor = Color(0xFF1A2748)
                    for (i in 0..4) {
                        val y = h * (i / 4f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Safe threshold reference zone
                    val normalTopY = h * (1f - (metricType.normalMax - minVal) / valRange).coerceIn(0f, h)
                    val normalBottomY = h * (1f - (metricType.normalMin - minVal) / valRange).coerceIn(0f, h)
                    drawRect(
                        color = StatusNominalGreen.copy(alpha = 0.08f),
                        topLeft = Offset(0f, normalTopY.coerceAtMost(normalBottomY)),
                        size = androidx.compose.ui.geometry.Size(
                            w,
                            kotlin.math.abs(normalBottomY - normalTopY).coerceAtLeast(4f)
                        )
                    )

                    // Reference threshold line
                    drawLine(
                        color = StatusNominalGreen.copy(alpha = 0.35f),
                        start = Offset(0f, normalTopY),
                        end = Offset(w, normalTopY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Compute Coordinates
                    val stepX = if (dataPoints.size > 1) w / (dataPoints.size - 1) else w
                    val points = dataPoints.mapIndexed { index, pair ->
                        val x = index * stepX
                        val y = h * (1f - (pair.second - minVal) / valRange)
                        Offset(x, y.coerceIn(0f, h))
                    }

                    // Build Curve Path & Area Path
                    val linePath = Path()
                    val areaPath = Path()

                    if (points.isNotEmpty()) {
                        linePath.moveTo(points[0].x, points[0].y)
                        areaPath.moveTo(points[0].x, h)
                        areaPath.lineTo(points[0].x, points[0].y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2f
                            linePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            areaPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }

                        areaPath.lineTo(points.last().x, h)
                        areaPath.close()

                        // Draw Gradient Area under Curve
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    metricType.lineAccent.copy(alpha = 0.30f),
                                    metricType.lineAccent.copy(alpha = 0.0f)
                                ),
                                startY = 0f,
                                endY = h
                            )
                        )

                        // Draw Curve Line
                        drawPath(
                            path = linePath,
                            color = metricType.lineAccent,
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                        )

                        // Draw Data Dots
                        points.forEachIndexed { idx, pt ->
                            val isSelected = selectedIndex == idx
                            drawCircle(
                                color = if (isSelected) StarWhite else metricType.lineAccent,
                                radius = if (isSelected) 6f else 4f,
                                center = pt
                            )
                            if (isSelected) {
                                drawCircle(
                                    color = metricType.lineAccent,
                                    radius = 11f,
                                    center = pt,
                                    style = Stroke(width = 2f)
                                )
                                // Vertical scrubber indicator line
                                drawLine(
                                    color = StarWhite.copy(alpha = 0.6f),
                                    start = Offset(pt.x, 0f),
                                    end = Offset(pt.x, h),
                                    strokeWidth = 1.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // X-Axis Labels (MET Days)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dataPoints.forEach { pt ->
                    Text(
                        text = "D${pt.first}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SlateGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Legend & Safety Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(StatusNominalGreen.copy(alpha = 0.8f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NASA Safe Zone: ${metricType.normalMin.toInt()}-${metricType.normalMax.toInt()} ${metricType.unit}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SlateGray,
                            fontSize = 10.5.sp
                        )
                    )
                }

                Text(
                    text = "Tap node to inspect",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SlateGray.copy(alpha = 0.7f),
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}
