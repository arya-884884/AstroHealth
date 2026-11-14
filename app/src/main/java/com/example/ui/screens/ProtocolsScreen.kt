package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.CountermeasureProtocol
import com.example.data.model.GroundCommMessage
import com.example.data.model.HealthStatus
import com.example.ui.AstroHealthUiState
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
fun ProtocolsScreen(
    uiState: AstroHealthUiState,
    onSendUplink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var uplinkInput by remember { mutableStateOf("") }
    var expandedProtocolId by remember { mutableStateOf<String?>(uiState.protocols.firstOrNull()?.id) }
    val stepChecks = remember { mutableStateMapOf<String, Boolean>() }

    val env = uiState.currentEnvironment

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
            .testTag("protocols_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "DEEP-SPACE AUTONOMOUS CLINICAL SUPPORT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                )
                Text(
                    text = "Flight Surgeon Countermeasures",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = StarWhite
                    )
                )
                Text(
                    text = "When communications with Earth are delayed or blacked out, execute these validated protocols to treat acute spaceflight health hazards.",
                    style = MaterialTheme.typography.bodySmall.copy(color = SlateGray)
                )
            }
        }

        // Earth Comm Latency Warning Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = "Earth Delay",
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "COMMUNICATION LATENCY: ${env.commDelaySeconds / 60}m ${env.commDelaySeconds % 60}s",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "One-way radio travel time to Houston Mission Control. Crew is in Autonomous Triage Mode.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = StarWhite.copy(alpha = 0.9f)
                            )
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "CLINICAL ACTION PROTOCOLS (${uiState.protocols.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGray
                )
            )
        }

        // Protocols List
        items(uiState.protocols) { protocol ->
            val isExpanded = expandedProtocolId == protocol.id
            ProtocolCard(
                protocol = protocol,
                isExpanded = isExpanded,
                stepChecks = stepChecks,
                onToggleExpand = {
                    expandedProtocolId = if (isExpanded) null else protocol.id
                },
                onToggleStep = { stepKey ->
                    stepChecks[stepKey] = !(stepChecks[stepKey] ?: false)
                }
            )
        }

        // Ground Telemetry Uplink Simulator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263C70))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = "Uplink",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRANSMIT TELEMETRY PACKET TO EARTH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Encapsulates latest vitals, dosimeter data, and clinical notes for transmission via Deep Space Network (DSN) to Flight Surgeon Dr. Vance.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SlateGray)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uplinkInput,
                        onValueChange = { uplinkInput = it },
                        placeholder = { Text("E.g. Completed LBNP protocol. Eye fundoscopy stable; headache subsided.", color = SlateGray, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_uplink_notes"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF253966),
                            focusedContainerColor = DeepSpaceSurfaceVariant,
                            unfocusedContainerColor = DeepSpaceSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (uplinkInput.isNotBlank()) {
                                onSendUplink(uplinkInput)
                                uplinkInput = ""
                            } else {
                                onSendUplink("Routine scheduled telemetry transmission.")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_dispatch_uplink"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color(0xFF070B18)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dispatch Telemetry Packet (${env.commDelaySeconds / 60}m Latency)",
                            color = Color(0xFF070B18),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Comm History
                    Text(
                        text = "FLIGHT SURGEON COMMS LOG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SlateGray,
                            fontSize = 10.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    uiState.commMessages.forEach { msg ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (msg.isOutbound) Color(0xFF14203D) else Color(0xFF0E2B27),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (msg.isOutbound) ElectricBlue.copy(alpha = 0.4f) else StatusNominalGreen.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = msg.sender,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (msg.isOutbound) ElectricBlue else StatusNominalGreen
                                        )
                                    )
                                    Text(
                                        text = msg.status,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SlateGray,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = StarWhite.copy(alpha = 0.95f),
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProtocolCard(
    protocol: CountermeasureProtocol,
    isExpanded: Boolean,
    stepChecks: Map<String, Boolean>,
    onToggleExpand: () -> Unit,
    onToggleStep: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("protocol_card_${protocol.id}")
            .clickable(onClick = onToggleExpand),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) protocol.urgency.color else Color(0xFF1E2F54)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(protocol.urgency.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = protocol.hazard.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = protocol.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = StarWhite
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = protocol.urgency.color.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = protocol.urgency.label.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = protocol.urgency.color,
                            fontSize = 9.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = SlateGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Trigger Condition
            Text(
                text = "Trigger: ${protocol.symptomTrigger}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SlateGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            )

            // Expanded Content
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = protocol.summary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = StarWhite.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "INTERACTIVE ACTION CHECKLIST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    protocol.steps.forEachIndexed { index, step ->
                        val stepKey = "${protocol.id}_step_$index"
                        val isChecked = stepChecks[stepKey] ?: false

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { onToggleStep(stepKey) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChecked) DeepSpaceSurfaceVariant else Color(0xFF0C1324),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isChecked) StatusNominalGreen.copy(alpha = 0.5f) else Color(0xFF1B2949)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onToggleStep(stepKey) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = StatusNominalGreen,
                                        uncheckedColor = SlateGray
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${index + 1}. $step",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isChecked) StarWhite.copy(alpha = 0.7f) else StarWhite,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clinical Rationale & Equipment
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0A1224),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2E52))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "CLINICAL RATIONALE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectricBlue,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = protocol.clinicalRationale,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SlateGray,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "EQUIPMENT: ${protocol.equipmentRequired}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
