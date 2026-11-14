package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BodyZone
import com.example.data.model.HealthLog
import com.example.data.model.HealthStatus
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.DeepSpaceSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateGray
import com.example.ui.theme.StarWhite
import com.example.ui.theme.StatusCriticalRed
import com.example.ui.theme.StatusNominalGreen
import com.example.ui.theme.StatusRadiationMagenta
import com.example.ui.theme.StatusWarningOrange

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AstronautBodyMap(
    latestLog: HealthLog?,
    selectedZone: BodyZone?,
    onSelectZone: (BodyZone) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("astronaut_body_map_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.4f), ElectricBlue.copy(alpha = 0.15f))
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ANATOMICAL HAZARD MAP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                    Text(
                        text = "NASA HRP Spaceflight Risk Scan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DeepSpaceSurfaceVariant
                ) {
                    Text(
                        text = "TAP HOTSPOT TO INSPECT",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = SlateGray,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body visualization area
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF070B18))
                    .border(1.dp, Color(0xFF1E2D54), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                // Pulsing animation for node beacons
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.45f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                // Background grid and holographic astronaut silhouette
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f

                    // Subtle coordinate grid
                    val gridColor = Color(0xFF141F3C)
                    for (x in 0 until size.width.toInt() step 40) {
                        drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), strokeWidth = 1f)
                    }
                    for (y in 0 until size.height.toInt() step 40) {
                        drawLine(gridColor, Offset(0f, y.toFloat()), Offset(w, y.toFloat()), strokeWidth = 1f)
                    }

                    // Silhouette: Holographic astronaut wireframe / silhouette
                    val cyanGlow = NeonCyan.copy(alpha = 0.25f)
                    val outlineStroke = Stroke(width = 2.5f)

                    // Head / Helmet
                    val headRadius = h * 0.08f
                    val headCenter = Offset(cx, h * 0.17f)
                    drawCircle(color = Color(0xFF142442), radius = headRadius, center = headCenter)
                    drawCircle(color = cyanGlow, radius = headRadius, center = headCenter, style = outlineStroke)

                    // Visor
                    drawRoundRect(
                        color = ElectricBlue.copy(alpha = 0.35f),
                        topLeft = Offset(cx - headRadius * 0.7f, headCenter.y - headRadius * 0.45f),
                        size = Size(headRadius * 1.4f, headRadius * 0.9f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )

                    // Neck & Torso
                    val torsoTop = headCenter.y + headRadius
                    val torsoHeight = h * 0.30f
                    val torsoWidth = w * 0.22f

                    val torsoPath = Path().apply {
                        moveTo(cx - torsoWidth * 0.5f, torsoTop + 10f)
                        lineTo(cx + torsoWidth * 0.5f, torsoTop + 10f)
                        lineTo(cx + torsoWidth * 0.4f, torsoTop + torsoHeight)
                        lineTo(cx - torsoWidth * 0.4f, torsoTop + torsoHeight)
                        close()
                    }
                    drawPath(torsoPath, color = Color(0xFF101B34))
                    drawPath(torsoPath, color = cyanGlow, style = outlineStroke)

                    // Arms
                    // Left arm
                    drawLine(
                        color = cyanGlow,
                        start = Offset(cx - torsoWidth * 0.5f, torsoTop + 15f),
                        end = Offset(cx - torsoWidth * 0.85f, torsoTop + torsoHeight * 0.7f),
                        strokeWidth = 3f
                    )
                    // Right arm
                    drawLine(
                        color = cyanGlow,
                        start = Offset(cx + torsoWidth * 0.5f, torsoTop + 15f),
                        end = Offset(cx + torsoWidth * 0.85f, torsoTop + torsoHeight * 0.7f),
                        strokeWidth = 3f
                    )

                    // Legs & Spine
                    val pelvisY = torsoTop + torsoHeight
                    val legWidth = torsoWidth * 0.3f
                    val legLength = h * 0.34f

                    // Left leg
                    val leftLegPath = Path().apply {
                        moveTo(cx - legWidth * 1.3f, pelvisY)
                        lineTo(cx - legWidth * 0.2f, pelvisY)
                        lineTo(cx - legWidth * 0.4f, pelvisY + legLength)
                        lineTo(cx - legWidth * 1.5f, pelvisY + legLength)
                        close()
                    }
                    drawPath(leftLegPath, color = Color(0xFF0F1A33))
                    drawPath(leftLegPath, color = cyanGlow, style = outlineStroke)

                    // Right leg
                    val rightLegPath = Path().apply {
                        moveTo(cx + legWidth * 0.2f, pelvisY)
                        lineTo(cx + legWidth * 1.3f, pelvisY)
                        lineTo(cx + legWidth * 1.5f, pelvisY + legLength)
                        lineTo(cx + legWidth * 0.4f, pelvisY + legLength)
                        close()
                    }
                    drawPath(rightLegPath, color = Color(0xFF0F1A33))
                    drawPath(rightLegPath, color = cyanGlow, style = outlineStroke)

                    // Spine indicator line
                    drawLine(
                        color = ElectricBlue.copy(alpha = 0.5f),
                        start = Offset(cx, torsoTop + 15f),
                        end = Offset(cx, pelvisY),
                        strokeWidth = 2f
                    )

                    // Orbital scan ring
                    drawOval(
                        color = NeonCyan.copy(alpha = 0.12f),
                        topLeft = Offset(cx - w * 0.35f, h * 0.36f),
                        size = Size(w * 0.70f, h * 0.25f),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Interactive touch hotspots overlayed at exact relative coordinates
                // 1. Head & Ocular (SANS) - Center top
                BodyHotspotButton(
                    zone = BodyZone.HEAD_OCULAR,
                    status = latestLog?.getSansStatus() ?: HealthStatus.NOMINAL,
                    isSelected = selectedZone == BodyZone.HEAD_OCULAR,
                    pulseScale = pulseScale,
                    onClick = { onSelectZone(BodyZone.HEAD_OCULAR) },
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 28.dp)
                )

                // 2. Cognitive & Sleep (Brain) - Top Left
                BodyHotspotButton(
                    zone = BodyZone.BEHAVIORAL_COGNITIVE,
                    status = latestLog?.getSleepStatus() ?: HealthStatus.NOMINAL,
                    isSelected = selectedZone == BodyZone.BEHAVIORAL_COGNITIVE,
                    pulseScale = pulseScale,
                    onClick = { onSelectZone(BodyZone.BEHAVIORAL_COGNITIVE) },
                    modifier = Modifier.align(Alignment.TopStart).offset(x = 18.dp, y = 38.dp)
                )

                // 3. Dosimeter & Radiation - Top Right
                BodyHotspotButton(
                    zone = BodyZone.RADIATION_DOSIMETRY,
                    status = latestLog?.getRadiationStatus() ?: HealthStatus.NOMINAL,
                    isSelected = selectedZone == BodyZone.RADIATION_DOSIMETRY,
                    pulseScale = pulseScale,
                    onClick = { onSelectZone(BodyZone.RADIATION_DOSIMETRY) },
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-18).dp, y = 38.dp)
                )

                // 4. Cardiovascular (Heart / Fluid Shift) - Center chest
                BodyHotspotButton(
                    zone = BodyZone.CARDIOVASCULAR,
                    status = latestLog?.getHeartRateStatus() ?: HealthStatus.NOMINAL,
                    isSelected = selectedZone == BodyZone.CARDIOVASCULAR,
                    pulseScale = pulseScale,
                    onClick = { onSelectZone(BodyZone.CARDIOVASCULAR) },
                    modifier = Modifier.align(Alignment.Center).offset(y = (-30).dp)
                )

                // 5. Spine & Skeletal (Bone Density) - Mid torso
                BodyHotspotButton(
                    zone = BodyZone.SPINE_SKELETAL,
                    status = HealthStatus.NOMINAL,
                    isSelected = selectedZone == BodyZone.SPINE_SKELETAL,
                    pulseScale = pulseScale,
                    onClick = { onSelectZone(BodyZone.SPINE_SKELETAL) },
                    modifier = Modifier.align(Alignment.Center).offset(y = 20.dp)
                )

                // 6. Muscular & Calves (ARED / Atrophy) - Lower legs
                BodyHotspotButton(
                    zone = BodyZone.MUSCULAR_LEGS,
                    status = if ((latestLog?.exerciseMinutes ?: 150) < 120) HealthStatus.ADVISORY else HealthStatus.NOMINAL,
                    isSelected = selectedZone == BodyZone.MUSCULAR_LEGS,
                    pulseScale = pulseScale,
                    onClick = { onSelectZone(BodyZone.MUSCULAR_LEGS) },
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-20).dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Zone Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BodyZone.values().forEach { zone ->
                    val isSelected = selectedZone == zone
                    val status = when (zone) {
                        BodyZone.HEAD_OCULAR -> latestLog?.getSansStatus() ?: HealthStatus.NOMINAL
                        BodyZone.CARDIOVASCULAR -> latestLog?.getHeartRateStatus() ?: HealthStatus.NOMINAL
                        BodyZone.RADIATION_DOSIMETRY -> latestLog?.getRadiationStatus() ?: HealthStatus.NOMINAL
                        BodyZone.BEHAVIORAL_COGNITIVE -> latestLog?.getSleepStatus() ?: HealthStatus.NOMINAL
                        BodyZone.SPINE_SKELETAL -> HealthStatus.NOMINAL
                        BodyZone.MUSCULAR_LEGS -> if ((latestLog?.exerciseMinutes ?: 150) < 120) HealthStatus.ADVISORY else HealthStatus.NOMINAL
                    }

                    Surface(
                        modifier = Modifier
                            .testTag("zone_chip_${zone.name.lowercase()}")
                            .clickable { onSelectZone(zone) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else DeepSpaceSurfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) NeonCyan else Color(0xFF283B63)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(status.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = zone.title.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) StarWhite else SlateGray
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
private fun BodyHotspotButton(
    zone: BodyZone,
    status: HealthStatus,
    isSelected: Boolean,
    pulseScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("hotspot_${zone.name.lowercase()}")
            .size(54.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing wave
        Box(
            modifier = Modifier
                .size((34 * if (isSelected) pulseScale else 1f).dp)
                .clip(CircleShape)
                .background(status.color.copy(alpha = if (isSelected) 0.35f else 0.15f))
        )

        // Inner glowing core button
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) status.color else DeepSpaceSurfaceVariant)
                .border(2.dp, status.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) DeepSpaceSurface else status.color)
            )
        }
    }
}

private fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)
