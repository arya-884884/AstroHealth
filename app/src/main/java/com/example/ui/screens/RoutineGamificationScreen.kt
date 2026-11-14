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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyTask
import com.example.data.model.MissionEnvironment
import com.example.ui.AstroHealthUiState
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.DeepSpaceSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateGray
import com.example.ui.theme.StarWhite
import com.example.ui.theme.StatusNominalGreen
import com.example.ui.theme.StatusWarningOrange

@Composable
fun RoutineGamificationScreen(
    uiState: AstroHealthUiState,
    onToggleTask: (String) -> Unit,
    onChangeEnvironment: (MissionEnvironment) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = uiState.dailyTasks.count { it.isCompleted }
    val totalCount = uiState.dailyTasks.size.coerceAtLeast(1)
    val progress = completedCount.toFloat() / totalCount

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
            .testTag("routine_gamification_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "ASTRONAUT HABIT & COUNTERMEASURE TRACKER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                )
                Text(
                    text = "Mission Routine & Readiness",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = StarWhite
                    )
                )
                Text(
                    text = "Consistent daily adherence to exercise, shielding, and exams is the primary defense against spaceflight physiological decline.",
                    style = MaterialTheme.typography.bodySmall.copy(color = SlateGray)
                )
            }
        }

        // Gamification Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = "Rank",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CREW SPECIALIST • LV 4",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Text(
                                text = "${uiState.crewXp} Readiness XP",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StarWhite,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StatusNominalGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusNominalGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = "Streak",
                                    tint = StatusNominalGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${uiState.missionStreakDays}d Streak",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StatusNominalGreen
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Routine Completion",
                            style = MaterialTheme.typography.bodySmall.copy(color = SlateGray)
                        )
                        Text(
                            text = "$completedCount of $totalCount Tasks ($((progress * 100).toInt())%)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = StarWhite,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan,
                        trackColor = DeepSpaceSurfaceVariant
                    )
                }
            }
        }

        // Mission Environment Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22355F))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Environment",
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MISSION OPERATIONAL ENVIRONMENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Select active spacecraft or surface habitat to recalibrate baseline gravity, radiation hazard index, and Earth communication latency.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SlateGray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(MissionEnvironment.values().toList()) { env ->
                            val isSelected = env == uiState.currentEnvironment
                            Surface(
                                modifier = Modifier
                                    .testTag("env_selector_${env.name.lowercase()}")
                                    .clickable { onChangeEnvironment(env) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ElectricBlue.copy(alpha = 0.22f) else DeepSpaceSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ElectricBlue else Color(0xFF22335C)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = env.shortName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) StarWhite else SlateGray
                                        )
                                    )
                                    Text(
                                        text = "${env.gravityG}g • ~${env.commDelaySeconds / 60}m Delay",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) NeonCyan else SlateGray,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Routine Checklist Tasks
        item {
            Text(
                text = "TODAY'S MISSION ROUTINE TASKS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGray
                )
            )
        }

        items(uiState.dailyTasks) { task ->
            DailyTaskItem(
                task = task,
                onToggle = { onToggleTask(task.id) }
            )
        }

        // Astronaut Flight Badges
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22355F))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FLIGHT CREW INSIGNIA & BADGES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BadgeItem(
                            title = "Zero-G Spine",
                            subtitle = "ARED 40d Streak",
                            icon = Icons.Default.FitnessCenter,
                            isUnlocked = true,
                            modifier = Modifier.weight(1f)
                        )
                        BadgeItem(
                            title = "Solar Shield",
                            subtitle = "TEPC Badging",
                            icon = Icons.Default.Shield,
                            isUnlocked = true,
                            modifier = Modifier.weight(1f)
                        )
                        BadgeItem(
                            title = "Mars Pioneer",
                            subtitle = "MET Day 42",
                            icon = Icons.Default.RocketLaunch,
                            isUnlocked = true,
                            modifier = Modifier.weight(1f)
                        )
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
private fun DailyTaskItem(
    task: DailyTask,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}")
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFF0F1E3A) else DeepSpaceSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (task.isCompleted) StatusNominalGreen.copy(alpha = 0.5f) else Color(0xFF1E2F54)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = StatusNominalGreen,
                    uncheckedColor = SlateGray
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) StarWhite.copy(alpha = 0.8f) else StarWhite
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1B2C52)
                    ) {
                        Text(
                            text = "+${task.xp} XP",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SlateGray,
                        fontSize = 11.5.sp
                    )
                )

                if (task.targetUnit.isNotBlank()) {
                    Text(
                        text = "Target: ${task.targetUnit} • ${task.hazardTag.title}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ElectricBlue,
                            fontSize = 9.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DeepSpaceSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF283B68))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = StarWhite,
                    fontSize = 11.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SlateGray,
                    fontSize = 9.sp
                )
            )
        }
    }
}
