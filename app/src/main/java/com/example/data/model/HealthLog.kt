package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_logs")
data class HealthLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val missionDay: Int = 42,
    val astronautName: String = "Commander Sarah Chen",
    val heartRate: Int = 72, // bpm
    val systolicBp: Int = 118, // mmHg
    val diastolicBp: Int = 76, // mmHg
    val bloodGlucose: Double = 92.0, // mg/dL
    val bodyTempCelsius: Double = 36.8, // °C
    val sleepHours: Double = 7.4, // hours
    val sleepQuality: Int = 8, // 1-10
    val moodScore: Int = 4, // 1 (Distressed) to 5 (Resilient/Peak)
    val dailyRadiationDoseMsv: Double = 0.82, // mSv
    val opticNerveSheathDiameterMm: Double = 5.2, // mm (SANS indicator: >5.8mm warning)
    val exerciseMinutes: Int = 140, // target 150 min
    val hydrationLiters: Double = 2.7, // Liters
    val co2LevelPpm: Int = 2800, // ppm on station
    val boneDensityIndex: Double = -0.15, // T-Score deviation
    val notes: String = "",
    val environment: String = MissionEnvironment.MARS_TRANSIT.name,
    val syncedToGround: Boolean = false
) {
    // Evaluation rules based on NASA Flight Medicine standards
    fun getHeartRateStatus(): HealthStatus = when {
        heartRate in 55..95 -> HealthStatus.NOMINAL
        heartRate in 50..105 -> HealthStatus.ADVISORY
        heartRate in 45..120 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }

    fun getBloodPressureStatus(): HealthStatus = when {
        systolicBp in 90..125 && diastolicBp in 60..84 -> HealthStatus.NOMINAL
        systolicBp in 126..139 || diastolicBp in 85..89 -> HealthStatus.ADVISORY
        systolicBp in 140..160 || diastolicBp in 90..100 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }

    fun getGlucoseStatus(): HealthStatus = when {
        bloodGlucose in 70.0..115.0 -> HealthStatus.NOMINAL
        bloodGlucose in 65.0..140.0 -> HealthStatus.ADVISORY
        bloodGlucose in 55.0..180.0 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }

    fun getTempStatus(): HealthStatus = when {
        bodyTempCelsius in 36.4..37.4 -> HealthStatus.NOMINAL
        bodyTempCelsius in 36.0..37.8 -> HealthStatus.ADVISORY
        bodyTempCelsius in 35.5..38.5 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }

    fun getRadiationStatus(): HealthStatus = when {
        dailyRadiationDoseMsv < 1.0 -> HealthStatus.NOMINAL
        dailyRadiationDoseMsv < 2.5 -> HealthStatus.ADVISORY
        dailyRadiationDoseMsv < 5.0 -> HealthStatus.WARNING // Solar active
        else -> HealthStatus.CRITICAL // Solar Particle Event / Shelter protocol
    }

    fun getSansStatus(): HealthStatus = when {
        opticNerveSheathDiameterMm < 5.5 -> HealthStatus.NOMINAL
        opticNerveSheathDiameterMm < 5.8 -> HealthStatus.ADVISORY
        opticNerveSheathDiameterMm < 6.2 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }

    fun getSleepStatus(): HealthStatus = when {
        sleepHours >= 7.0 -> HealthStatus.NOMINAL
        sleepHours >= 5.5 -> HealthStatus.ADVISORY
        sleepHours >= 4.0 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }

    fun getOverallStatus(): HealthStatus {
        val statuses = listOf(
            getHeartRateStatus(),
            getBloodPressureStatus(),
            getGlucoseStatus(),
            getTempStatus(),
            getRadiationStatus(),
            getSansStatus(),
            getSleepStatus()
        )
        return when {
            statuses.any { it == HealthStatus.CRITICAL } -> HealthStatus.CRITICAL
            statuses.any { it == HealthStatus.WARNING } -> HealthStatus.WARNING
            statuses.any { it == HealthStatus.ADVISORY } -> HealthStatus.ADVISORY
            else -> HealthStatus.NOMINAL
        }
    }

    fun getReadinessScore(): Int {
        var score = 100
        if (getHeartRateStatus() != HealthStatus.NOMINAL) score -= 8
        if (getBloodPressureStatus() != HealthStatus.NOMINAL) score -= 10
        if (getRadiationStatus() != HealthStatus.NOMINAL) score -= 15
        if (getSansStatus() != HealthStatus.NOMINAL) score -= 12
        if (getSleepStatus() != HealthStatus.NOMINAL) score -= 10
        if (exerciseMinutes < 120) score -= 10
        if (hydrationLiters < 2.0) score -= 8
        if (moodScore < 3) score -= 10
        return score.coerceIn(20, 100)
    }
}
