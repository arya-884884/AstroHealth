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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BodyZone
import com.example.data.model.HealthLog
import com.example.data.model.HealthStatus
import com.example.ui.AstroHealthUiState
import com.example.ui.components.AstronautBodyMap
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
fun BodyScannerScreen(
    uiState: AstroHealthUiState,
    onSelectZone: (BodyZone) -> Unit,
    onNavigateToProtocols: () -> Unit,
    onOpenLogging: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedZone = uiState.selectedZone ?: BodyZone.HEAD_OCULAR
    val latest = uiState.latestLog

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
            .testTag("body_scanner_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "ASTRONAUT PHYSIOLOGY SCANNER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                )
                Text(
                    text = "NASA HRP 5 Hazards Body Impact",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = StarWhite
                    )
                )
                Text(
                    text = "Examine how microgravity, space radiation, and closed environments alter human organ systems during long-duration flight.",
                    style = MaterialTheme.typography.bodySmall.copy(color = SlateGray)
                )
            }
        }

        // Interactive Body Map
        item {
            AstronautBodyMap(
                latestLog = latest,
                selectedZone = selectedZone,
                onSelectZone = onSelectZone
            )
        }

        // Detailed Inspector for Selected Zone
        item {
            ZoneClinicalDetailCard(
                zone = selectedZone,
                latest = latest,
                onNavigateToProtocols = onNavigateToProtocols,
                onOpenLogging = onOpenLogging
            )
        }

        // All Body Systems List
        item {
            Text(
                text = "ALL MONITORED ORGAN SYSTEMS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGray
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(BodyZone.values().toList()) { zone ->
            ZoneSummaryListItem(
                zone = zone,
                isSelected = zone == selectedZone,
                latest = latest,
                onClick = { onSelectZone(zone) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ZoneClinicalDetailCard(
    zone: BodyZone,
    latest: HealthLog?,
    onNavigateToProtocols: () -> Unit,
    onOpenLogging: () -> Unit
) {
    val zoneStatus = when (zone) {
        BodyZone.HEAD_OCULAR -> latest?.getSansStatus() ?: HealthStatus.NOMINAL
        BodyZone.CARDIOVASCULAR -> latest?.getHeartRateStatus() ?: HealthStatus.NOMINAL
        BodyZone.RADIATION_DOSIMETRY -> latest?.getRadiationStatus() ?: HealthStatus.NOMINAL
        BodyZone.BEHAVIORAL_COGNITIVE -> latest?.getSleepStatus() ?: HealthStatus.NOMINAL
        BodyZone.SPINE_SKELETAL -> HealthStatus.NOMINAL
        BodyZone.MUSCULAR_LEGS -> if ((latest?.exerciseMinutes ?: 150) < 120) HealthStatus.ADVISORY else HealthStatus.NOMINAL
    }

    val (currentMetric, normalRef, hazardCause, countermeasure) = when (zone) {
        BodyZone.HEAD_OCULAR -> Quad(
            "Optic Nerve Sheath: ${String.format("%.2f", latest?.opticNerveSheathDiameterMm ?: 5.2)} mm",
            "Nominal Threshold: < 5.50 mm (Warning > 5.80 mm)",
            "Cephalad fluid shift raises intracranial pressure (ICP), flattening posterior globe, causing papilledema and cotton wool spots (SANS).",
            "Lower Body Negative Pressure (LBNP / Chibis suit), sodium restriction < 2.0g/d, head-up sleep angle, and CO2 habitat scrubbing."
        )
        BodyZone.CARDIOVASCULAR -> Quad(
            "Blood Pressure: ${latest?.systolicBp ?: 118}/${latest?.diastolicBp ?: 76} mmHg • HR: ${latest?.heartRate ?: 72} bpm",
            "Nominal: BP 90-120/60-80 mmHg • HR 55-95 bpm",
            "In zero gravity, absence of hydrostatic pressure gradient redistributes ~1.5L of blood cephalad, reducing left ventricular volume.",
            "High-intensity interval cycle ergometer sprints, pre-landing oral fluid loading with salt tablets, G-suit compression cuffs."
        )
        BodyZone.SPINE_SKELETAL -> Quad(
            "Bone Mineral Density Index: ${String.format("%.2f", latest?.boneDensityIndex ?: -0.15)} T-Score (MET Day ${latest?.missionDay ?: 42})",
            "Allowable Loss: < 1.0% per month with ARED compliance",
            "Lack of weight-bearing mechanical strain stimulates osteoclastic resorption; spinal intervertebral discs expand up to 7 cm causing severe back pain.",
            "Heavy axial resistance loading on ARED (>600 lbs equivalent), 1000 IU Vitamin D3 + K2 daily, Bisphosphonate infusions if indicated."
        )
        BodyZone.MUSCULAR_LEGS -> Quad(
            "Daily ARED Resistance: ${latest?.exerciseMinutes ?: 150} min (Target: 150 min)",
            "Target: ≥ 150 minutes daily resistive & aerobic work",
            "Postural slow-twitch muscle fibers (soleus and gastrocnemius) atrophy by up to 20% on long flights without vigorous daily resistance.",
            "ARED vacuum resistance deadlifts, squats, heel raises, and CEVIS cycle ergometer with vibration isolation."
        )
        BodyZone.RADIATION_DOSIMETRY -> Quad(
            "Daily Dose: ${String.format("%.2f", latest?.dailyRadiationDoseMsv ?: 0.79)} mSv/day (Cumulative: ~33 mSv)",
            "Normal Baseline: < 1.0 mSv/d • SPE Shelter Alert: > 5.0 mSv/d",
            "Galactic cosmic rays (GCR) and solar energetic protons penetrate hull, causing DNA double-strand breaks and reactive oxygen species.",
            "Retreat to water-shielded aft storm shelter bunker, don personal shielding vests, ingest antioxidant cocktail (Vitamin C/E/ALA)."
        )
        BodyZone.BEHAVIORAL_COGNITIVE -> Quad(
            "Sleep Duration: ${String.format("%.1f", latest?.sleepHours ?: 7.6)} hrs • Mood: ${latest?.moodScore ?: 4}/5",
            "Target: 7.0 - 8.5 hours continuous sleep • Mood ≥ 3",
            "90-minute orbital sunrise/sunset cycles disrupt suprachiasmatic nucleus; sensory deprivation and confinement induce cognitive fatigue.",
            "Dynamic Solid-State LED lighting (480nm blue light morning, amber evening), Earth VR nature immersion, structured team comm."
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("zone_clinical_detail_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, zoneStatus.color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Zone header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = zone.hazard.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = zone.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = StarWhite
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = zoneStatus.color.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, zoneStatus.color)
                ) {
                    Text(
                        text = zoneStatus.label.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = zoneStatus.color,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Reading Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DeepSpaceSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22355F))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ASTRONAUT TELEMETRY READING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SlateGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentMetric,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StarWhite,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = normalRef,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Space Hazard Physiology
            Text(
                text = "HOW SPACE HAZARDS AFFECT THIS SYSTEM",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = hazardCause,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = StarWhite.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Prescribed Countermeasure
            Text(
                text = "NASA PRESCRIBED COUNTERMEASURES",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusNominalGreen
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = countermeasure,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = StarWhite.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToProtocols,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Protocol",
                        tint = Color(0xFF070B18),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Treatment Protocol",
                        color = Color(0xFF070B18),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onOpenLogging,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepSpaceSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Update Reading",
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoneSummaryListItem(
    zone: BodyZone,
    isSelected: Boolean,
    latest: HealthLog?,
    onClick: () -> Unit
) {
    val status = when (zone) {
        BodyZone.HEAD_OCULAR -> latest?.getSansStatus() ?: HealthStatus.NOMINAL
        BodyZone.CARDIOVASCULAR -> latest?.getHeartRateStatus() ?: HealthStatus.NOMINAL
        BodyZone.RADIATION_DOSIMETRY -> latest?.getRadiationStatus() ?: HealthStatus.NOMINAL
        BodyZone.BEHAVIORAL_COGNITIVE -> latest?.getSleepStatus() ?: HealthStatus.NOMINAL
        BodyZone.SPINE_SKELETAL -> HealthStatus.NOMINAL
        BodyZone.MUSCULAR_LEGS -> if ((latest?.exerciseMinutes ?: 150) < 120) HealthStatus.ADVISORY else HealthStatus.NOMINAL
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("zone_list_item_${zone.name.lowercase()}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DeepSpaceSurfaceVariant else DeepSpaceSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NeonCyan else Color(0xFF1E2F54)
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
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(status.color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = zone.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                )
                Text(
                    text = zone.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SlateGray,
                        fontSize = 11.sp
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = status.color.copy(alpha = 0.15f)
            ) {
                Text(
                    text = status.label.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = status.color,
                        fontSize = 9.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Inspect",
                tint = SlateGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
