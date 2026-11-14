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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HealthLog
import com.example.data.model.HealthStatus
import com.example.data.model.MissionEnvironment
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalLoggingSheet(
    latestLog: HealthLog?,
    currentEnvironment: MissionEnvironment,
    onDismiss: () -> Unit,
    onSaveLog: (HealthLog) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var missionDay by remember { mutableIntStateOf((latestLog?.missionDay ?: 42) + 1) }
    var heartRateText by remember { mutableStateOf("${latestLog?.heartRate ?: 72}") }
    var systolicBpText by remember { mutableStateOf("${latestLog?.systolicBp ?: 118}") }
    var diastolicBpText by remember { mutableStateOf("${latestLog?.diastolicBp ?: 76}") }
    var glucoseText by remember { mutableStateOf(String.format("%.0f", latestLog?.bloodGlucose ?: 92.0)) }
    var tempText by remember { mutableStateOf(String.format("%.1f", latestLog?.bodyTempCelsius ?: 36.8)) }
    var sleepHours by remember { mutableDoubleStateOf(latestLog?.sleepHours ?: 7.5) }
    var radiationText by remember { mutableStateOf(String.format("%.2f", latestLog?.dailyRadiationDoseMsv ?: 0.82)) }
    var sansText by remember { mutableStateOf(String.format("%.2f", latestLog?.opticNerveSheathDiameterMm ?: 5.20)) }
    var exerciseMinutes by remember { mutableIntStateOf(latestLog?.exerciseMinutes ?: 150) }
    var hydrationLiters by remember { mutableDoubleStateOf(latestLog?.hydrationLiters ?: 2.8) }
    var moodScore by remember { mutableIntStateOf(latestLog?.moodScore ?: 4) }
    var notesText by remember { mutableStateOf("") }

    // Live preview health log for real-time triage feedback
    val previewLog by remember {
        derivedStateOf {
            HealthLog(
                missionDay = missionDay,
                heartRate = heartRateText.toIntOrNull() ?: 72,
                systolicBp = systolicBpText.toIntOrNull() ?: 118,
                diastolicBp = diastolicBpText.toIntOrNull() ?: 76,
                bloodGlucose = glucoseText.toDoubleOrNull() ?: 90.0,
                bodyTempCelsius = tempText.toDoubleOrNull() ?: 36.8,
                sleepHours = sleepHours,
                dailyRadiationDoseMsv = radiationText.toDoubleOrNull() ?: 0.8,
                opticNerveSheathDiameterMm = sansText.toDoubleOrNull() ?: 5.2,
                exerciseMinutes = exerciseMinutes,
                hydrationLiters = hydrationLiters,
                moodScore = moodScore,
                notes = notesText,
                environment = currentEnvironment.name
            )
        }
    }

    val overallStatus = previewLog.getOverallStatus()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepSpaceSurface,
        dragHandle = null
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .testTag("vital_logging_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AUTONOMOUS DIAGNOSTIC INTAKE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                        Text(
                            text = "Log Astronaut Health Telemetry",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = StarWhite
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_logging")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SlateGray
                        )
                    }
                }
            }

            // Quick Space Scenario Presets
            item {
                Column {
                    Text(
                        text = "QUICK MISSION PRESETS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            PresetChip(
                                label = "Nominal Rest Day",
                                color = StatusNominalGreen,
                                onClick = {
                                    heartRateText = "68"
                                    systolicBpText = "116"
                                    diastolicBpText = "75"
                                    glucoseText = "88"
                                    tempText = "36.7"
                                    sleepHours = 7.8
                                    radiationText = "0.74"
                                    sansText = "5.10"
                                    exerciseMinutes = 150
                                    hydrationLiters = 2.9
                                    moodScore = 4
                                    notesText = "Full routine completed. Stable vital signs."
                                }
                            )
                        }
                        item {
                            PresetChip(
                                label = "Post-EVA Exertion",
                                color = ElectricBlue,
                                onClick = {
                                    heartRateText = "98"
                                    systolicBpText = "132"
                                    diastolicBpText = "84"
                                    glucoseText = "104"
                                    tempText = "37.2"
                                    sleepHours = 6.2
                                    radiationText = "1.15"
                                    sansText = "5.30"
                                    exerciseMinutes = 165
                                    hydrationLiters = 3.4
                                    moodScore = 4
                                    notesText = "Completed 6-hour lunar surface spacewalk. Elevated pulse from manual core sample drilling."
                                }
                            )
                        }
                        item {
                            PresetChip(
                                label = "Solar Flare Alert (SPE)",
                                color = StatusCriticalRed,
                                onClick = {
                                    heartRateText = "86"
                                    systolicBpText = "128"
                                    diastolicBpText = "82"
                                    glucoseText = "96"
                                    tempText = "37.0"
                                    sleepHours = 5.5
                                    radiationText = "5.80" // High radiation
                                    sansText = "5.25"
                                    exerciseMinutes = 90
                                    hydrationLiters = 2.5
                                    moodScore = 2
                                    notesText = "Coronal Mass Ejection detected by solar observatory. Relocated to water shelter."
                                }
                            )
                        }
                        item {
                            PresetChip(
                                label = "Fluid Shift / SANS Warning",
                                color = StatusWarningOrange,
                                onClick = {
                                    heartRateText = "76"
                                    systolicBpText = "135"
                                    diastolicBpText = "88"
                                    glucoseText = "91"
                                    tempText = "36.9"
                                    sleepHours = 6.0
                                    radiationText = "0.85"
                                    sansText = "6.10" // SANS Warning
                                    exerciseMinutes = 120
                                    hydrationLiters = 2.2
                                    moodScore = 3
                                    notesText = "Visual blurriness noted during microscope task. Optic disc elevation on ultrasound."
                                }
                            )
                        }
                    }
                }
            }

            // Real-Time NASA Triage Feedback Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = overallStatus.color.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, overallStatus.color.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(overallStatus.color.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (overallStatus == HealthStatus.NOMINAL) Icons.Default.Info else Icons.Default.Warning,
                                contentDescription = overallStatus.label,
                                tint = overallStatus.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "EVALUATION: ${overallStatus.label.uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = overallStatus.color
                                )
                            )
                            val feedbackMsg = when (overallStatus) {
                                HealthStatus.NOMINAL -> "All biometrics within nominal NASA spaceflight envelopes. Readiness 95%."
                                HealthStatus.ADVISORY -> "Mild drift detected. Review hydration and sleep schedule."
                                HealthStatus.WARNING -> "SANS ocular enlargement or blood pressure elevated. Autonomous countermeasures triggered."
                                HealthStatus.CRITICAL -> "CRITICAL: Radiation dose or intracranial pressure exceeds safe thresholds! Follow emergency protocol."
                            }
                            Text(
                                text = feedbackMsg,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = StarWhite.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }
                }
            }

            // Input Fields
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedSpaceField(
                        label = "Mission Day (MET)",
                        value = "$missionDay",
                        onValueChange = { missionDay = it.toIntOrNull() ?: missionDay },
                        unit = "Day",
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedSpaceField(
                        label = "Heart Rate",
                        value = heartRateText,
                        onValueChange = { heartRateText = it },
                        unit = "BPM",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedSpaceField(
                        label = "Systolic BP",
                        value = systolicBpText,
                        onValueChange = { systolicBpText = it },
                        unit = "mmHg",
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedSpaceField(
                        label = "Diastolic BP",
                        value = diastolicBpText,
                        onValueChange = { diastolicBpText = it },
                        unit = "mmHg",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedSpaceField(
                        label = "Blood Glucose",
                        value = glucoseText,
                        onValueChange = { glucoseText = it },
                        unit = "mg/dL",
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedSpaceField(
                        label = "Core Body Temp",
                        value = tempText,
                        onValueChange = { tempText = it },
                        unit = "°C",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedSpaceField(
                        label = "Radiation Dose",
                        value = radiationText,
                        onValueChange = { radiationText = it },
                        unit = "mSv/d",
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedSpaceField(
                        label = "Optic Nerve (ONSD)",
                        value = sansText,
                        onValueChange = { sansText = it },
                        unit = "mm",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sleep Slider
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSpaceSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Daily Sleep Duration",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = StarWhite
                                )
                            )
                            Text(
                                text = "${String.format("%.1f", sleepHours)} hrs",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Slider(
                            value = sleepHours.toFloat(),
                            onValueChange = { sleepHours = it.toDouble() },
                            valueRange = 3f..11f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color(0xFF283B68)
                            )
                        )
                    }
                }
            }

            // Exercise & Hydration
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedSpaceField(
                        label = "ARED Exercise",
                        value = "$exerciseMinutes",
                        onValueChange = { exerciseMinutes = it.toIntOrNull() ?: exerciseMinutes },
                        unit = "min",
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedSpaceField(
                        label = "Hydration Intake",
                        value = String.format("%.1f", hydrationLiters),
                        onValueChange = { hydrationLiters = it.toDoubleOrNull() ?: hydrationLiters },
                        unit = "L",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Mood & Notes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSpaceSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Psychological & Confinement Score (PANAS)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = StarWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val labels = listOf("1 Distress", "2 Strain", "3 Coping", "4 Resilient", "5 Peak")
                            (1..5).forEach { score ->
                                val isSelected = moodScore == score
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) NeonCyan else DeepSpaceSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) NeonCyan else Color(0xFF2E416E)
                                    ),
                                    modifier = Modifier
                                        .clickable { moodScore = score }
                                        .padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = "$score",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF070B18) else StarWhite
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Clinical Notes & Mission Observations", color = SlateGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes"),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF283B68),
                        focusedContainerColor = DeepSpaceSurfaceVariant,
                        unfocusedContainerColor = DeepSpaceSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Save Button
            item {
                Button(
                    onClick = { onSaveLog(previewLog) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_save_log"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = Color(0xFF070B18)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Commit Health Log to Flight Record",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF070B18)
                        )
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun OutlinedSpaceField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SlateGray, fontSize = 11.sp) },
        trailingIcon = {
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.testTag("field_${label.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = StarWhite,
            unfocusedTextColor = StarWhite,
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = Color(0xFF263963),
            focusedContainerColor = DeepSpaceSurfaceVariant,
            unfocusedContainerColor = DeepSpaceSurfaceVariant
        ),
        singleLine = true
    )
}

@Composable
private fun PresetChip(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}
