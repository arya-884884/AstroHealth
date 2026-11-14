package com.example.data.repository

import com.example.data.local.HealthLogDao
import com.example.data.model.CountermeasureProtocol
import com.example.data.model.DailyTask
import com.example.data.model.GroundCommMessage
import com.example.data.model.HazardType
import com.example.data.model.HealthLog
import com.example.data.model.HealthStatus
import com.example.data.model.MissionEnvironment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AstroHealthRepository(
    private val healthLogDao: HealthLogDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val allLogs: Flow<List<HealthLog>> = healthLogDao.getAllLogs()
    val latestLog: Flow<HealthLog?> = healthLogDao.getLatestLog()

    private val _currentEnvironment = MutableStateFlow(MissionEnvironment.MARS_TRANSIT)
    val currentEnvironment: StateFlow<MissionEnvironment> = _currentEnvironment.asStateFlow()

    private val _dailyTasks = MutableStateFlow<List<DailyTask>>(emptyList())
    val dailyTasks: StateFlow<List<DailyTask>> = _dailyTasks.asStateFlow()

    private val _commMessages = MutableStateFlow<List<GroundCommMessage>>(emptyList())
    val commMessages: StateFlow<List<GroundCommMessage>> = _commMessages.asStateFlow()

    init {
        scope.launch {
            seedDefaultLogsIfEmpty()
            initializeDailyTasks()
            initializeGroundComm()
        }
    }

    fun setEnvironment(env: MissionEnvironment) {
        _currentEnvironment.value = env
    }

    suspend fun insertLog(log: HealthLog): Long {
        val id = healthLogDao.insertLog(log)
        // Mark vitals task as completed
        toggleTask("task_vitals_morning", true)
        return id
    }

    suspend fun deleteLog(id: Long) {
        healthLogDao.deleteLogById(id)
    }

    fun toggleTask(taskId: String, isCompleted: Boolean? = null) {
        _dailyTasks.value = _dailyTasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(isCompleted = isCompleted ?: !task.isCompleted)
            } else task
        }
    }

    fun sendUplinkToGround(notes: String) {
        val env = _currentEnvironment.value
        val now = System.currentTimeMillis()
        val delayMin = (env.commDelaySeconds / 60).coerceAtLeast(1)

        val outMsg = GroundCommMessage(
            id = "msg_${now}",
            timestamp = now,
            sender = "Astronaut Telemetry Uplink (Crew)",
            message = "Biometric telemetry packet dispatched to Flight Surgeon. Mission Elapsed Time MET: 42d 14h. Notes: $notes",
            delayMinutes = delayMin,
            isOutbound = true,
            status = "TRANSMITTING"
        )

        _commMessages.value = listOf(outMsg) + _commMessages.value

        // Simulate flight surgeon response after brief interactive delay
        scope.launch {
            kotlinx.coroutines.delay(2500)
            val ackMsg = GroundCommMessage(
                id = "resp_${now}",
                timestamp = System.currentTimeMillis(),
                sender = "Flight Surgeon - Dr. Vance, Houston",
                message = "Telemetry packet received. Cardiovascular fluid balance and ONSD ocular readings reviewed. Continue daily ARED 150m protocol and hydrate with electrolyte sachet prior to sleep cycle.",
                delayMinutes = delayMin,
                isOutbound = false,
                status = "ACKNOWLEDGED"
            )
            _commMessages.value = listOf(ackMsg) + _commMessages.value
        }
    }

    private suspend fun seedDefaultLogsIfEmpty() {
        val existing = healthLogDao.getAllLogs().first()
        if (existing.isEmpty()) {
            val baseTime = System.currentTimeMillis() - 7 * 86400000L
            val baseline = listOf(
                HealthLog(
                    missionDay = 36,
                    timestamp = baseTime + 1 * 86400000L,
                    heartRate = 66,
                    systolicBp = 115,
                    diastolicBp = 74,
                    bloodGlucose = 89.0,
                    bodyTempCelsius = 36.7,
                    sleepHours = 7.8,
                    sleepQuality = 8,
                    moodScore = 4,
                    dailyRadiationDoseMsv = 0.72,
                    opticNerveSheathDiameterMm = 5.0,
                    exerciseMinutes = 150,
                    hydrationLiters = 2.8,
                    notes = "Nominal transit day. High energy during ARED deadlifts."
                ),
                HealthLog(
                    missionDay = 37,
                    timestamp = baseTime + 2 * 86400000L,
                    heartRate = 69,
                    systolicBp = 118,
                    diastolicBp = 76,
                    bloodGlucose = 92.0,
                    bodyTempCelsius = 36.8,
                    sleepHours = 7.1,
                    sleepQuality = 7,
                    moodScore = 4,
                    dailyRadiationDoseMsv = 0.80,
                    opticNerveSheathDiameterMm = 5.1,
                    exerciseMinutes = 145,
                    hydrationLiters = 2.7,
                    notes = "Ultrasound of internal jugular vein conducted; no retrograde flow."
                ),
                HealthLog(
                    missionDay = 38,
                    timestamp = baseTime + 3 * 86400000L,
                    heartRate = 74,
                    systolicBp = 124,
                    diastolicBp = 82,
                    bloodGlucose = 98.0,
                    bodyTempCelsius = 37.0,
                    sleepHours = 6.4,
                    sleepQuality = 6,
                    moodScore = 3,
                    dailyRadiationDoseMsv = 1.65, // Minor solar flare event
                    opticNerveSheathDiameterMm = 5.3,
                    exerciseMinutes = 120,
                    hydrationLiters = 2.4,
                    notes = "Solar particle activity minor spike. Relocated to aft shielded sleep bay."
                ),
                HealthLog(
                    missionDay = 39,
                    timestamp = baseTime + 4 * 86400000L,
                    heartRate = 71,
                    systolicBp = 120,
                    diastolicBp = 78,
                    bloodGlucose = 91.0,
                    bodyTempCelsius = 36.9,
                    sleepHours = 7.5,
                    sleepQuality = 8,
                    moodScore = 4,
                    dailyRadiationDoseMsv = 0.88,
                    opticNerveSheathDiameterMm = 5.2,
                    exerciseMinutes = 155,
                    hydrationLiters = 3.0,
                    notes = "Full recovery. Eye fundoscopy clear; posterior globe curvature nominal."
                ),
                HealthLog(
                    missionDay = 40,
                    timestamp = baseTime + 5 * 86400000L,
                    heartRate = 68,
                    systolicBp = 116,
                    diastolicBp = 75,
                    bloodGlucose = 88.0,
                    bodyTempCelsius = 36.6,
                    sleepHours = 8.0,
                    sleepQuality = 9,
                    moodScore = 5,
                    dailyRadiationDoseMsv = 0.76,
                    opticNerveSheathDiameterMm = 5.1,
                    exerciseMinutes = 160,
                    hydrationLiters = 3.1,
                    notes = "Excellent sleep cycle. Earth VR meditation session completed."
                ),
                HealthLog(
                    missionDay = 41,
                    timestamp = baseTime + 6 * 86400000L,
                    heartRate = 75,
                    systolicBp = 122,
                    diastolicBp = 80,
                    bloodGlucose = 94.0,
                    bodyTempCelsius = 36.8,
                    sleepHours = 6.8,
                    sleepQuality = 7,
                    moodScore = 4,
                    dailyRadiationDoseMsv = 0.84,
                    opticNerveSheathDiameterMm = 5.25,
                    exerciseMinutes = 135,
                    hydrationLiters = 2.6,
                    notes = "Routine maintenance on water reclamation loop; slight physical fatigue."
                ),
                HealthLog(
                    missionDay = 42,
                    timestamp = System.currentTimeMillis(),
                    heartRate = 72,
                    systolicBp = 118,
                    diastolicBp = 76,
                    bloodGlucose = 90.0,
                    bodyTempCelsius = 36.8,
                    sleepHours = 7.6,
                    sleepQuality = 8,
                    moodScore = 4,
                    dailyRadiationDoseMsv = 0.79,
                    opticNerveSheathDiameterMm = 5.2,
                    exerciseMinutes = 150,
                    hydrationLiters = 2.9,
                    notes = "Today's assessment: Optimal flight readiness. Ready for scheduled airlock inspection."
                )
            )
            healthLogDao.insertAll(baseline)
        }
    }

    private fun initializeDailyTasks() {
        _dailyTasks.value = listOf(
            DailyTask(
                id = "task_vitals_morning",
                title = "Morning Biometric Triage",
                category = "VITALS",
                description = "Record BP, HR, O2, core temperature, and blood glucose upon waking.",
                xp = 120,
                isCompleted = true,
                hazardTag = HazardType.GRAVITY_FIELDS,
                targetUnit = "Daily Log"
            ),
            DailyTask(
                id = "task_ared_resistance",
                title = "ARED 2.5h Resistance Protocol",
                category = "EXERCISE",
                description = "Squat, deadlift, and heel raises on vacuum-cylinder resistance device to counteract osteopenia.",
                xp = 250,
                isCompleted = true,
                hazardTag = HazardType.GRAVITY_FIELDS,
                targetUnit = "150 min"
            ),
            DailyTask(
                id = "task_dosimeter_sync",
                title = "TEPC Dosimeter Sync",
                category = "RADIATION",
                description = "Synchronize personal crew radiation badge with habitat avionics telemetry bus.",
                xp = 100,
                isCompleted = false,
                hazardTag = HazardType.SPACE_RADIATION,
                targetUnit = "mSv Check"
            ),
            DailyTask(
                id = "task_ocular_sans",
                title = "Ocular Ultrasound & Fundoscopy",
                category = "EXAM",
                description = "Measure optic nerve sheath diameter (ONSD) to screen for Spaceflight-Associated Neuro-ocular Syndrome.",
                xp = 200,
                isCompleted = false,
                hazardTag = HazardType.GRAVITY_FIELDS,
                targetUnit = "< 5.8 mm"
            ),
            DailyTask(
                id = "task_lbnp_chibis",
                title = "LBNP Lower Body Negative Pressure",
                category = "CARDIO",
                description = "30-minute session in Chibis suit to pull pooled cephalic blood back to lower extremities.",
                xp = 180,
                isCompleted = false,
                hazardTag = HazardType.GRAVITY_FIELDS,
                targetUnit = "30 min"
            ),
            DailyTask(
                id = "task_panas_mood",
                title = "Confinement PANAS Mood Log",
                category = "COGNITIVE",
                description = "Self-assess sleep architecture, cognitive alertness, and team cohesion dynamics.",
                xp = 100,
                isCompleted = true,
                hazardTag = HazardType.ISOLATION_CONFINEMENT,
                targetUnit = "Audit"
            )
        )
    }

    private fun initializeGroundComm() {
        _commMessages.value = listOf(
            GroundCommMessage(
                id = "msg_init_1",
                timestamp = System.currentTimeMillis() - 7200000L,
                sender = "Flight Surgeon - Dr. Vance, Houston",
                message = "Morning crew telemetry received. Blood pressure regulation looks rock-solid across all modules. Keep vigilant on daily ARED compliance.",
                delayMinutes = 14,
                isOutbound = false,
                status = "ACKNOWLEDGED"
            )
        )
    }

    fun getProtocols(): List<CountermeasureProtocol> {
        return listOf(
            CountermeasureProtocol(
                id = "prot_spe_radiation",
                title = "Solar Particle Event (SPE) Storm Shelter",
                hazard = HazardType.SPACE_RADIATION,
                symptomTrigger = "Radiation rate > 5.0 mSv/day or SPE Warning from Solar Physics Sensor",
                urgency = HealthStatus.CRITICAL,
                summary = "Emergency protocol to protect crew bone marrow and GI tract against high-energy proton bombardment during coronal mass ejections.",
                steps = listOf(
                    "Immediately secure active experiments and sound module shelter alert.",
                    "Retreat to designated water-shielded habitat core (aft storm bunker surrounded by polyethylene and greywater tanks).",
                    "Don high-density personal shielding vests if available.",
                    "Take prescribed radioprotective antioxidant formulation (Vitamin C, E, selenium, alpha-lipoic acid).",
                    "Maintain continuous personal dosimeter monitoring; log hourly mSv rate."
                ),
                clinicalRationale = "Water and hydrogen-rich materials absorb high-energy solar protons effectively without generating dangerous Bremsstrahlung secondary radiation.",
                equipmentRequired = "Passive & Active Dosimeters (CPD/TEPC), Storm Shelter Water Wall, Radioprotectant Kit"
            ),
            CountermeasureProtocol(
                id = "prot_sans_ocular",
                title = "SANS / Elevated Intracranial Pressure",
                hazard = HazardType.GRAVITY_FIELDS,
                symptomTrigger = "Optic Nerve Sheath Diameter > 5.8 mm, visual blurriness, cotton wool spots, persistent morning headache",
                urgency = HealthStatus.WARNING,
                summary = "Prevent progressive hyperopic shifts, choroidal folds, and permanent vision degradation caused by headward fluid shift in microgravity.",
                steps = listOf(
                    "Perform B-scan ocular ultrasound with High-Resolution linear probe to verify ONSD and posterior globe flattening.",
                    "Don Lower Body Negative Pressure (LBNP / Chibis) device for 45 minutes at -25 to -35 mmHg to draw fluid toward feet.",
                    "Limit dietary sodium intake to < 2.0g/day to minimize fluid retention.",
                    "Engage in inclined sleeping configuration or head-up restraint harness (5-10° angle).",
                    "Verify habitat carbon dioxide levels (ensure CO2 is kept below 2.5 mmHg / 3,000 ppm)."
                ),
                clinicalRationale = "In 0g, lack of gravity hydrostatic column causes 1.5-2.0 liters of fluid to permanently shift to the head and neck, elevating central venous pressure and retarding optic CSF drainage.",
                equipmentRequired = "Ultrasound 2 System, LBNP Vacuum Chamber / Chibis suit, Tonopen, Fundoscope"
            ),
            CountermeasureProtocol(
                id = "prot_orthostatic_cardio",
                title = "Cardiovascular Deconditioning & Re-entry Preparation",
                hazard = HazardType.GRAVITY_FIELDS,
                symptomTrigger = "Resting tachycardia (>100 bpm), post-exercise dizziness, narrow pulse pressure (<25 mmHg)",
                urgency = HealthStatus.ADVISORY,
                summary = "Counteract cardiac muscle atrophy, baroreceptor reset, and hypovolemia prior to planetary descent or lunar/martian surface EVA.",
                steps = listOf(
                    "Commence pre-landing fluid loading: ingest 1 liter of isotonic saline solution with 8 salt tablets 2 hours before g-load transition.",
                    "Don anti-gravity compression garments (G-suit or pneumatic calf cuffs set to 40 mmHg).",
                    "Increase high-intensity interval cycle ergometer sprint intervals to 40 min to stimulate stroke volume.",
                    "Monitor continuous ECG rhythm for premature ventricular contractions (PVCs).",
                    "Hydrate to maintain minimum 3.0 liters per 24-hour cycle."
                ),
                clinicalRationale = "Spaceflight decreases total red blood cell mass (space anemia) and reduces left ventricular mass by ~8-10%, predisposing astronauts to syncopal collapse upon gravity re-exposure.",
                equipmentRequired = "Cycle Ergometer with Vibration Isolation (CEVIS), Gradient Compression Garments, Oral Rehydration Salts"
            ),
            CountermeasureProtocol(
                id = "prot_isolation_circadian",
                title = "Circadian Dysregulation & Confinement Fatigue",
                hazard = HazardType.ISOLATION_CONFINEMENT,
                symptomTrigger = "Sleep duration < 5.5 hours for 2 consecutive days, cognitive lag, elevated irritability",
                urgency = HealthStatus.ADVISORY,
                summary = "Mitigate desynchronized circadian pacemakers caused by artificial habitat lighting, confinement stress, and communication latency.",
                steps = listOf(
                    "Activate Solid-State LED dynamic lighting to High-Kelvin Blue-Enriched Mode (480nm, 10,000 lux) for 60 min post-waking.",
                    "Shift habitat lighting to Blue-Depleted Amber Mode 90 minutes before scheduled sleep period.",
                    "Administer 0.5 mg micro-dose exogenous melatonin 45 minutes before sleep if circadian phase-advance is required.",
                    "Complete 15-minute Earth sensory VR immersion session (forest/ocean binaural audio).",
                    "Review mission schedule with crew commander to allocate protected 8.5-hour sleep window."
                ),
                clinicalRationale = "Suprachiasmatic nucleus (SCN) entrainment requires precise melanopsin photo-pigment stimulation. Circadian drift degrades psychomotor vigilance.",
                equipmentRequired = "Dynamic SSL Lighting, Blue-blocking glasses, VR Sensory Immersion Headset, Actigraphy watch"
            ),
            CountermeasureProtocol(
                id = "prot_bone_ared",
                title = "Osteopenia & Trabecular Bone Loss Countermeasure",
                hazard = HazardType.GRAVITY_FIELDS,
                symptomTrigger = "Bone remodeling marker elevation or prolonged mission elapsed time (>60 days)",
                urgency = HealthStatus.NOMINAL,
                summary = "Daily high-load resistive skeletal loading to counter the ~1.0% to 1.5% per month bone mineral density loss in femur and lumbar spine.",
                steps = listOf(
                    "Perform heavy axial loads on ARED (Advanced Resistive Exercise Device) exceeding 600 lbs equivalent force.",
                    "Perform eccentric heel drops and stomping motions to stimulate osteocyte mechanotransduction.",
                    "Ensure daily dietary intake of 800-1000 mg Calcium and 1,000 IU Vitamin D3 supplementation.",
                    "Measure spinal elongation using thoracic sliding scale; perform spinal compression stretches."
                ),
                clinicalRationale = "Without gravitational ground reaction forces, osteoclasts resorb bone faster than osteoblasts deposit new hydroxyapatite, elevating hypercalciuria and kidney stone risk.",
                equipmentRequired = "ARED (Advanced Resistive Exercise Device), Vitamin D3/K2 rations, DEXA Bone Proxy"
            )
        )
    }
}
