package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HealthLog
import com.example.data.model.HealthStatus
import com.example.ui.AstroHealthUiState
import com.example.ui.components.ChartMetricType
import com.example.ui.components.TelemetryChart
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.DeepSpaceSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateGray
import com.example.ui.theme.StarWhite
import com.example.ui.theme.StatusCriticalRed
import com.example.ui.theme.StatusNominalGreen
import com.example.ui.theme.StatusWarningOrange

@Composable
fun AnalyticsTrendsScreen(
    uiState: AstroHealthUiState,
    onDeleteLog: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(ChartMetricType.RADIATION) }
    val logs = uiState.logs

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
            .testTag("analytics_trends_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "LONG-DURATION TRENDS & HAZARD EXPOSURE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                )
                Text(
                    text = "Biometric Telemetry Analysis",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = StarWhite
                    )
                )
                Text(
                    text = "Track changes in physiology across Mission Elapsed Time (MET) to identify microgravity deconditioning and radiation accumulation.",
                    style = MaterialTheme.typography.bodySmall.copy(color = SlateGray)
                )
            }
        }

        // Metric Selector Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ChartMetricType.values().toList()) { metric ->
                    val isSelected = metric == selectedMetric
                    Surface(
                        modifier = Modifier
                            .testTag("tab_metric_${metric.name.lowercase()}")
                            .clickable { selectedMetric = metric },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) metric.lineAccent.copy(alpha = 0.2f) else DeepSpaceSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) metric.lineAccent else Color(0xFF24355D)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(metric.lineAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = metric.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) StarWhite else SlateGray
                                )
                            )
                        }
                    }
                }
            }
        }

        // Interactive Canvas Chart
        item {
            TelemetryChart(
                logs = logs,
                metricType = selectedMetric
            )
        }

        // Spaceflight Hazard Exposure Summary Card
        item {
            CumulativeHazardCard(logs = logs)
        }

        // Mission Log History Table
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECORDED MISSION LOGS (${logs.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray
                    )
                )
                Text(
                    text = "MET Day Descending",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SlateGray,
                        fontSize = 10.sp
                    )
                )
            }
        }

        items(logs.sortedByDescending { it.missionDay }) { log ->
            LogHistoryItem(
                log = log,
                onDelete = { onDeleteLog(log.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CumulativeHazardCard(logs: List<HealthLog>) {
    val totalRadiationMsv = logs.sumOf { it.dailyRadiationDoseMsv }
    val avgSleep = if (logs.isNotEmpty()) logs.map { it.sleepHours }.average() else 7.5
    val avgAred = if (logs.isNotEmpty()) logs.map { it.exerciseMinutes }.average() else 145.0
    val maxSans = logs.maxOfOrNull { it.opticNerveSheathDiameterMm } ?: 5.2

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263A6E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "NASA HRP RISK DOSIMETRY",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
            Text(
                text = "Cumulative Spaceflight Exposure",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HazardStatBox(
                    title = "Radiation Dose",
                    value = String.format("%.1f", totalRadiationMsv),
                    unit = "mSv Total",
                    subtitle = "Safe Limit: 600 mSv",
                    accent = StatusWarningOrange,
                    modifier = Modifier.weight(1f)
                )

                HazardStatBox(
                    title = "Avg Sleep",
                    value = String.format("%.1f", avgSleep),
                    unit = "hrs/night",
                    subtitle = "Target: > 7.0 hrs",
                    accent = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HazardStatBox(
                    title = "ARED Compliance",
                    value = String.format("%.0f", avgAred),
                    unit = "min/day",
                    subtitle = "Goal: 150 min/day",
                    accent = StatusNominalGreen,
                    modifier = Modifier.weight(1f)
                )

                HazardStatBox(
                    title = "Peak ONSD",
                    value = String.format("%.2f", maxSans),
                    unit = "mm (Eye)",
                    subtitle = "SANS limit: 5.80 mm",
                    accent = if (maxSans > 5.8) StatusCriticalRed else StatusNominalGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HazardStatBox(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DeepSpaceSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22355F))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SlateGray,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SlateGray,
                        fontSize = 9.sp
                    )
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SlateGray.copy(alpha = 0.8f),
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
private fun LogHistoryItem(
    log: HealthLog,
    onDelete: () -> Unit
) {
    val overall = log.getOverallStatus()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2F54))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(overall.color)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MET DAY ${log.missionDay}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = StarWhite,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = overall.color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = overall.label.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = overall.color,
                                fontSize = 8.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "HR: ${log.heartRate} bpm • BP: ${log.systolicBp}/${log.diastolicBp} • Rad: ${String.format("%.2f", log.dailyRadiationDoseMsv)} mSv • ONSD: ${String.format("%.2f", log.opticNerveSheathDiameterMm)} mm",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SlateGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp
                    )
                )

                if (log.notes.isNotBlank()) {
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonCyan.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Log",
                    tint = SlateGray.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
