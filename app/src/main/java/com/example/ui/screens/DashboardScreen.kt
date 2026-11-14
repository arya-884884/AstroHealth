package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BodyZone
import com.example.data.model.HealthLog
import com.example.data.model.HealthStatus
import com.example.data.model.MissionEnvironment
import com.example.ui.AstroHealthUiState
import com.example.ui.components.AstronautBodyMap
import com.example.ui.components.VitalTelemetryCard
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
fun DashboardScreen(
    uiState: AstroHealthUiState,
    onSelectZone: (BodyZone) -> Unit,
    onOpenLogging: () -> Unit,
    onNavigateToProtocols: () -> Unit,
    onNavigateToTrends: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latest = uiState.latestLog
    val env = uiState.currentEnvironment
    val overallStatus = latest?.getOverallStatus() ?: HealthStatus.NOMINAL

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header: Spacecraft HUD & Readiness
        item {
            MissionHeroBanner(
                uiState = uiState,
                onOpenLogging = onOpenLogging,
                onNavigateToRoutine = onNavigateToRoutine
            )
        }

        // Triage Status Alert Banner
        item {
            TriageAlertBanner(
                status = overallStatus,
                latest = latest,
                env = env,
                onNavigateToProtocols = onNavigateToProtocols
            )
        }

        // Interactive Anatomical Hazard Map
        item {
            AstronautBodyMap(
                latestLog = latest,
                selectedZone = uiState.selectedZone,
                onSelectZone = onSelectZone
            )
        }

        // Primary Health Indicators Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BIOMETRIC INDICATORS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                    Text(
                        text = "Continuous Flight Telemetry",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    )
                }

                Surface(
                    modifier = Modifier
                        .testTag("view_trends_btn")
                        .clickable { onNavigateToTrends() },
                    shape = RoundedCornerShape(8.dp),
                    color = DeepSpaceSurfaceVariant
                ) {
                    Text(
                        text = "VIEW CHARTS →",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    )
                }
            }
        }

        // Telemetry Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VitalTelemetryCard(
                        title = "Heart Rate",
                        value = "${latest?.heartRate ?: 72}",
                        unit = "BPM",
                        targetRange = "55-95 BPM",
                        status = latest?.getHeartRateStatus() ?: HealthStatus.NOMINAL,
                        icon = Icons.Default.Favorite,
                        progressFraction = ((latest?.heartRate ?: 72) - 40f) / 80f,
                        whyInSpace = "Fluid shift & stroke vol",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTrends
                    )

                    VitalTelemetryCard(
                        title = "Blood Pressure",
                        value = "${latest?.systolicBp ?: 118}/${latest?.diastolicBp ?: 76}",
                        unit = "mmHg",
                        targetRange = "90/60 - 120/80",
                        status = latest?.getBloodPressureStatus() ?: HealthStatus.NOMINAL,
                        icon = Icons.Default.Speed,
                        progressFraction = ((latest?.systolicBp ?: 118) - 80f) / 80f,
                        whyInSpace = "Cephalad venous pooling",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTrends
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VitalTelemetryCard(
                        title = "Daily Radiation",
                        value = String.format("%.2f", latest?.dailyRadiationDoseMsv ?: 0.79),
                        unit = "mSv/d",
                        targetRange = "< 1.0 mSv/d",
                        status = latest?.getRadiationStatus() ?: HealthStatus.NOMINAL,
                        icon = Icons.Default.Sensors,
                        progressFraction = ((latest?.dailyRadiationDoseMsv ?: 0.79).toFloat()) / 3.0f,
                        whyInSpace = "Cosmic ray DNA damage",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTrends
                    )

                    VitalTelemetryCard(
                        title = "Optic Nerve (SANS)",
                        value = String.format("%.1f", latest?.opticNerveSheathDiameterMm ?: 5.2),
                        unit = "mm",
                        targetRange = "< 5.5 mm",
                        status = latest?.getSansStatus() ?: HealthStatus.NOMINAL,
                        icon = Icons.Default.Visibility,
                        progressFraction = ((latest?.opticNerveSheathDiameterMm ?: 5.2).toFloat() - 4.0f) / 3.0f,
                        whyInSpace = "Intracranial pressure",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTrends
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VitalTelemetryCard(
                        title = "Sleep Cycle",
                        value = String.format("%.1f", latest?.sleepHours ?: 7.6),
                        unit = "hrs",
                        targetRange = "7.0 - 8.5 hrs",
                        status = latest?.getSleepStatus() ?: HealthStatus.NOMINAL,
                        icon = Icons.Default.Nightlight,
                        progressFraction = ((latest?.sleepHours ?: 7.6).toFloat()) / 9.0f,
                        whyInSpace = "Circadian confinement",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTrends
                    )

                    VitalTelemetryCard(
                        title = "Blood Glucose",
                        value = String.format("%.0f", latest?.bloodGlucose ?: 90.0),
                        unit = "mg/dL",
                        targetRange = "70 - 115 mg/dL",
                        status = latest?.getGlucoseStatus() ?: HealthStatus.NOMINAL,
                        icon = Icons.Default.Thermostat,
                        progressFraction = ((latest?.bloodGlucose ?: 90.0).toFloat() - 60f) / 100f,
                        whyInSpace = "Insulin resistance in 0g",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTrends
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VitalTelemetryCard(
                        title = "ARED Exercise",
                        value = "${latest?.exerciseMinutes ?: 150}",
                        unit = "min",
                        targetRange = "≥ 150 min/d",
                        status = if ((latest?.exerciseMinutes ?: 150) >= 120) HealthStatus.NOMINAL else HealthStatus.ADVISORY,
                        icon = Icons.Default.FitnessCenter,
                        progressFraction = ((latest?.exerciseMinutes ?: 150).toFloat()) / 180f,
                        whyInSpace = "Osteopenia & muscle loss",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRoutine
                    )

                    VitalTelemetryCard(
                        title = "Hydration Intake",
                        value = String.format("%.1f", latest?.hydrationLiters ?: 2.9),
                        unit = "L",
                        targetRange = "2.5 - 3.5 L",
                        status = if ((latest?.hydrationLiters ?: 2.9) >= 2.4) HealthStatus.NOMINAL else HealthStatus.ADVISORY,
                        icon = Icons.Default.WaterDrop,
                        progressFraction = ((latest?.hydrationLiters ?: 2.9).toFloat()) / 4.0f,
                        whyInSpace = "Renal calculi & hypovolemia",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRoutine
                    )
                }
            }
        }

        // Quick Action Command Buttons
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF20325C))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "FLIGHT SURGEON ACTIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenLogging,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_log_indicator"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Log",
                                tint = Color(0xFF070B18)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Log Vitals",
                                color = Color(0xFF070B18),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onNavigateToProtocols,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_protocols"),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepSpaceSurfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "Protocols",
                                tint = ElectricBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Protocols",
                                color = ElectricBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Footer spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MissionHeroBanner(
    uiState: AstroHealthUiState,
    onOpenLogging: () -> Unit,
    onNavigateToRoutine: () -> Unit
) {
    val env = uiState.currentEnvironment
    val latest = uiState.latestLog
    val missionDay = latest?.missionDay ?: 42

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mission_hero_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263C70))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Hero background image
            Image(
                painter = painterResource(id = R.drawable.astro_hero_banner_1794229733270),
                contentDescription = "Space Mission Telemetry Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.45f
            )

            // Dark gradient overlay for pristine legibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x77070B19),
                                Color(0xEE070B19),
                                Color(0xFF070B19)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top row: Mission ID & Environment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xAA0B142B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MET DAY $missionDay • HERMES-1",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    // Comm delay indicator
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xAA121D3D)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CellTower,
                                contentDescription = "Comm latency",
                                tint = SlateGray,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${env.commDelaySeconds / 60}m Earth Delay",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SlateGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mid row: Astronaut name + Readiness Gauge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Commander Sarah Chen",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = StarWhite
                            )
                        )
                        Text(
                            text = "${env.title} • ${env.gravityG}g Gravity",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ElectricBlue
                            )
                        )
                    }

                    // Circular Readiness Ring
                    Box(
                        modifier = Modifier
                            .testTag("crew_readiness_ring")
                            .size(62.dp)
                            .clickable { onNavigateToRoutine() },
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { uiState.crewReadinessScore / 100f },
                            modifier = Modifier.size(62.dp),
                            color = if (uiState.crewReadinessScore >= 85) StatusNominalGreen else StatusWarningOrange,
                            trackColor = Color(0xFF1E2B4A),
                            strokeWidth = 6.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.crewReadinessScore}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StarWhite,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = "READY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    color = SlateGray,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TriageAlertBanner(
    status: HealthStatus,
    latest: HealthLog?,
    env: MissionEnvironment,
    onNavigateToProtocols: () -> Unit
) {
    val isAlert = status != HealthStatus.NOMINAL

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("triage_alert_banner")
            .let { if (isAlert) it.clickable { onNavigateToProtocols() } else it },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) status.color.copy(alpha = 0.14f) else DeepSpaceSurfaceVariant.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAlert) status.color.copy(alpha = 0.6f) else Color(0xFF22355E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(status.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAlert) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = status.label,
                    tint = status.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAlert) "HEALTH ADVISORY DETECTED" else "ALL FLIGHT INDICATORS NOMINAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = status.color
                    )
                )
                Text(
                    text = if (isAlert) {
                        "Crew autonomous countermeasures advised for SANS ocular & fluid balance."
                    } else {
                        "Zero-G adaptation stable. Daily countermeasure compliance 100%."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = StarWhite.copy(alpha = 0.9f)
                    )
                )
            }

            if (isAlert) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = status.color
                ) {
                    Text(
                        text = "ACTION",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF070B19),
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
