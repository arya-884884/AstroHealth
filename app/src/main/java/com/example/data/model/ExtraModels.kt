package com.example.data.model

data class DailyTask(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val xp: Int,
    val isCompleted: Boolean = false,
    val hazardTag: HazardType,
    val targetUnit: String = ""
)

data class CountermeasureProtocol(
    val id: String,
    val title: String,
    val hazard: HazardType,
    val symptomTrigger: String,
    val urgency: HealthStatus,
    val summary: String,
    val steps: List<String>,
    val clinicalRationale: String,
    val equipmentRequired: String
)

data class GroundCommMessage(
    val id: String,
    val timestamp: Long,
    val sender: String,
    val message: String,
    val delayMinutes: Int,
    val isOutbound: Boolean = false,
    val status: String = "DELIVERED"
)
