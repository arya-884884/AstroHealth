package com.example

import com.example.data.model.HealthLog
import com.example.data.model.HealthStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `nominal health log evaluates to nominal status`() {
    val log = HealthLog(
      heartRate = 70,
      systolicBp = 118,
      diastolicBp = 76,
      bloodGlucose = 90.0,
      bodyTempCelsius = 36.8,
      sleepHours = 7.5,
      dailyRadiationDoseMsv = 0.8,
      opticNerveSheathDiameterMm = 5.2
    )
    assertEquals(HealthStatus.NOMINAL, log.getOverallStatus())
    assertEquals(100, log.getReadinessScore())
  }

  @Test
  fun `elevated radiation triggers warning status`() {
    val log = HealthLog(
      heartRate = 72,
      systolicBp = 120,
      diastolicBp = 80,
      dailyRadiationDoseMsv = 5.5, // Solar storm
      opticNerveSheathDiameterMm = 5.2
    )
    assertEquals(HealthStatus.CRITICAL, log.getRadiationStatus())
    assertEquals(HealthStatus.CRITICAL, log.getOverallStatus())
  }
}
