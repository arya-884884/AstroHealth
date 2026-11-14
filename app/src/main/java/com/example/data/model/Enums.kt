package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StatusAdvisoryYellow
import com.example.ui.theme.StatusCriticalRed
import com.example.ui.theme.StatusNominalGreen
import com.example.ui.theme.StatusWarningOrange

enum class HealthStatus(val label: String, val color: Color) {
    NOMINAL("Nominal", StatusNominalGreen),
    ADVISORY("Advisory", StatusAdvisoryYellow),
    WARNING("Warning", StatusWarningOrange),
    CRITICAL("Critical", StatusCriticalRed)
}

enum class MissionEnvironment(
    val title: String,
    val shortName: String,
    val gravityG: Double,
    val commDelaySeconds: Int,
    val radiationBaselineMsv: Double,
    val description: String
) {
    ISS_LEO(
        title = "International Space Station",
        shortName = "ISS (LEO)",
        gravityG = 0.0,
        commDelaySeconds = 2,
        radiationBaselineMsv = 0.5,
        description = "Low Earth Orbit. Earth magnetosphere provides partial radiation shielding. Real-time ground comm."
    ),
    LUNAR_GATEWAY(
        title = "Artemis Lunar Base / Gateway",
        shortName = "Artemis (Moon)",
        gravityG = 0.166,
        commDelaySeconds = 3,
        radiationBaselineMsv = 1.3,
        description = "Beyond Van Allen belts. Deep space radiation exposure and abrasive regolith lunar dust."
    ),
    MARS_TRANSIT(
        title = "Mars Transit Vehicle (Hermes)",
        shortName = "Mars Transit",
        gravityG = 0.0,
        commDelaySeconds = 850, // ~14 min
        radiationBaselineMsv = 1.8,
        description = "Deep space cruise. High cosmic ray & solar particle event risk. ~14-22 min Earth communication delay."
    ),
    MARS_SURFACE(
        title = "Mars Surface Outpost (Ares Base)",
        shortName = "Mars Outpost",
        gravityG = 0.38,
        commDelaySeconds = 1200, // ~20 min
        radiationBaselineMsv = 0.9,
        description = "Thin CO2 atmosphere, hypogravity, pervasive perchlorates in dust, 20-min transmission latency."
    )
}

enum class HazardType(
    val title: String,
    val code: String,
    val description: String
) {
    SPACE_RADIATION(
        title = "Space Radiation",
        code = "R",
        description = "Galactic cosmic rays (GCR) and solar particle events (SPE) damage DNA, elevating cancer and CNS risks."
    ),
    ISOLATION_CONFINEMENT(
        title = "Isolation & Confinement",
        code = "I",
        description = "Prolonged confinement in small modules leads to circadian disruptions, mood shifts, and sleep debt."
    ),
    DISTANCE_FROM_EARTH(
        title = "Distance from Earth",
        code = "D",
        description = "Up to 24-minute one-way comm delay requires crew autonomy for all medical triage and emergencies."
    ),
    GRAVITY_FIELDS(
        title = "Altered Gravity Fields",
        code = "G",
        description = "Microgravity shifts fluids cephalad, causes ~1% bone density loss per month, and muscle atrophy."
    ),
    HOSTILE_CLOSED_ENV(
        title = "Hostile Closed Environment",
        code = "E",
        description = "Accumulation of CO2, toxic trace contaminants, microgravity pathogens, and acoustic stress."
    )
}

enum class BodyZone(
    val title: String,
    val hazard: HazardType,
    val subtitle: String,
    val riskSummary: String
) {
    HEAD_OCULAR(
        title = "Head & Eyes (SANS)",
        hazard = HazardType.GRAVITY_FIELDS,
        subtitle = "Intracranial Pressure & Vision",
        riskSummary = "Cephalad fluid shift raises intracranial pressure, flattening the posterior globe and causing optic disc edema (SANS)."
    ),
    CARDIOVASCULAR(
        title = "Cardiovascular & Fluids",
        hazard = HazardType.GRAVITY_FIELDS,
        subtitle = "Fluid Shift & Blood Pressure",
        riskSummary = "Heart shrinks due to reduced fluid volume; causes orthostatic intolerance and fainting upon re-entering gravity."
    ),
    SPINE_SKELETAL(
        title = "Spine & Skeletal System",
        hazard = HazardType.GRAVITY_FIELDS,
        subtitle = "Bone Loss & Spinal Elongation",
        riskSummary = "Spine elongates by up to 3 inches in 0g causing lumbar pain; osteoclasts break down trabecular bone in hip and spine."
    ),
    MUSCULAR_LEGS(
        title = "Lower Limbs & Calves",
        hazard = HazardType.GRAVITY_FIELDS,
        subtitle = "Postural Muscle Atrophy",
        riskSummary = "Without gravity bearing weight, calf and thigh slow-twitch muscle mass declines rapidly without daily ARED exercise."
    ),
    RADIATION_DOSIMETRY(
        title = "Systemic Dosimeter & DNA",
        hazard = HazardType.SPACE_RADIATION,
        subtitle = "Cosmic & Solar Radiation",
        riskSummary = "Ionizing radiation penetrates hull, increasing oxidative stress, acute radiation sickness risk, and long-term malignancy."
    ),
    BEHAVIORAL_COGNITIVE(
        title = "Cognitive & Sleep (Brain)",
        hazard = HazardType.ISOLATION_CONFINEMENT,
        subtitle = "Circadian & Neuro-Behavioral",
        riskSummary = "Artificial lighting and 90-minute orbital days disrupt melatonin; sensory monotony induces cognitive fatigue and mood changes."
    )
}
