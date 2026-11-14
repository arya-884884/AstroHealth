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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.HazardType
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

data class GuideArticle(
    val id: String,
    val title: String,
    val category: String,
    val whyExamIsNeeded: String,
    val physiologicalImpact: String,
    val whenWarrantsAttention: String,
    val preventiveAction: String
)

@Composable
fun EducationGuideScreen(
    modifier: Modifier = Modifier
) {
    var expandedId by remember { mutableStateOf<String?>("art_sans") }

    val articles = listOf(
        GuideArticle(
            id = "art_sans",
            title = "Spaceflight-Associated Neuro-ocular Syndrome (SANS)",
            category = "GRAVITY & VISION",
            whyExamIsNeeded = "Astronauts frequently experience uncorrectable hyperopic shifts, visual blurriness, and scotomas during missions. B-scan ultrasound measures the optic nerve sheath diameter (ONSD) to detect early optic disc edema before permanent retinal damage occurs.",
            physiologicalImpact = "Without gravity's downward pull, ~1.5 to 2.0 liters of venous blood and interstitial fluid shift cephalad. This increases central venous pressure and retards cerebrospinal fluid (CSF) reabsorption, raising intracranial pressure (ICP) and flattening the posterior globe.",
            whenWarrantsAttention = "ONSD > 5.50 mm is Advisory. ONSD > 5.80 mm warrants immediate countermeasure intervention. Sudden visual acuity loss, cotton wool spots on fundoscopy, or severe throbbing morning headaches require emergency protocols.",
            preventiveAction = "Perform 30-45 minutes of Lower Body Negative Pressure (LBNP / Chibis suit) to pull fluid toward the lower extremities. Sleep in a head-up tilted posture (5-10°). Restrict dietary sodium to under 2,000 mg/day."
        ),
        GuideArticle(
            id = "art_radiation",
            title = "Space Radiation & Solar Particle Events (SPE)",
            category = "SPACE RADIATION",
            whyExamIsNeeded = "Outside Earth's protective magnetosphere (Moon, Mars, Deep Space), astronauts are exposed to Galactic Cosmic Rays (GCR) and unpredictable high-energy Solar Particle Events (SPE). Real-time active dosimeters (TEPC) and passive badges are vital to calculate cumulative organ doses.",
            physiologicalImpact = "High-LET (linear energy transfer) heavy ions (iron, oxygen, carbon nuclei) pierce spacecraft hulls, producing complex double-strand DNA breaks that lead to cancer, acute radiation sickness, microvascular endothelial damage, and central nervous system degradation.",
            whenWarrantsAttention = "Daily dose > 1.5 mSv/day warrants habitat shielding audit. Dose rate > 5.0 mSv/day or acute SPE alarm requires immediate evacuation to the water-shielded storm bunker core.",
            preventiveAction = "Retreat to designated water-shielded shelter (water wall / polyethylene food storage perimeter). Don personal radiation vests. Administer antioxidant radioprotective cocktail (Vitamin C, E, selenium, alpha-lipoic acid)."
        ),
        GuideArticle(
            id = "art_bone",
            title = "Osteopenia & Trabecular Bone Resorption",
            category = "MICROGRAVITY SKELETAL",
            whyExamIsNeeded = "Astronauts lose 1.0% to 1.5% of bone mineral density per month in weight-bearing bones (lumbar vertebrae, femur, calcaneus). Regular biomarker screening and DEXA proxy scans prevent catastrophic fractures upon landing in a gravity well.",
            physiologicalImpact = "Bone remodeling relies on mechanical loading (mechanotransduction). In weightlessness, osteoclast activity skyrockets while osteoblast bone formation drops, liberating massive amounts of calcium into the bloodstream and sharply raising nephrolithiasis (kidney stone) risk.",
            whenWarrantsAttention = "Failure to complete 150 minutes of daily resistance loading for > 3 consecutive days, bone mineral density T-score drop > -0.5, or flank pain indicating renal colic.",
            preventiveAction = "Heavy axial mechanical loading on the Advanced Resistive Exercise Device (ARED) exceeding 600 lbs equivalent load. Maintain daily supplementation of 1,000 IU Vitamin D3, Vitamin K2, and adequate calcium. High hydration to prevent kidney stones."
        ),
        GuideArticle(
            id = "art_cardio",
            title = "Cardiovascular Deconditioning & Space Anemia",
            category = "CARDIOVASCULAR",
            whyExamIsNeeded = "Spaceflight induces cardiac muscle remodeling, reduction in stroke volume, and baroreceptor sensitivity decline. Without countermeasures, astronauts faint or suffer orthostatic collapse when re-entering a gravity environment.",
            physiologicalImpact = "Upon initial fluid shift, the body senses fluid overload and reduces circulating plasma volume by ~15-20% through diuresis. Red blood cell mass also drops (space anemia). The left ventricular myocardium atrophies from reduced cardiac workload.",
            whenWarrantsAttention = "Resting tachycardia (>100 bpm), narrow pulse pressure (<25 mmHg), dizzy spells upon postural changes, or abnormal ECG rhythm patterns.",
            preventiveAction = "Daily high-intensity sprint intervals on the CEVIS cycle ergometer. Pre-landing oral fluid loading (1 liter isotonic saline with 8 salt tablets 2 hours before g-transition). Wear pneumatic anti-gravity compression garments."
        ),
        GuideArticle(
            id = "art_circadian",
            title = "Circadian Dysregulation & Confinement Stress",
            category = "ISOLATION & CONFINEMENT",
            whyExamIsNeeded = "Astronauts on the ISS experience 16 sunrises and sunsets every 24 hours. In transit to Mars, perpetual artificial lighting and isolation strain neuro-behavioral health. Sleep hours and PANAS psychological metrics track readiness.",
            physiologicalImpact = "Desynchronization of the suprachiasmatic nucleus suppresses natural melatonin production, leading to chronic sleep debt, slowed reaction times, cognitive vigilance lapses, and elevated interpersonal conflict in confined crews.",
            whenWarrantsAttention = "Sleep duration < 5.5 hours for 2 consecutive nights, elevated fatigue ratings, or persistent cognitive slowdown on psychomotor vigilance tests.",
            preventiveAction = "Dynamic Solid-State LED lighting: high-intensity blue-enriched light (480nm) upon waking, shifting to amber blue-depleted light 90 min before sleep. Micro-dose exogenous melatonin (0.5 mg). Virtual Reality Earth nature relaxation modules."
        ),
        GuideArticle(
            id = "art_closed_env",
            title = "Hostile Closed Environment & CO2 Accumulation",
            category = "HABITAT ATMOSPHERE",
            whyExamIsNeeded = "In zero-g, warm exhaled breath does not rise naturally by convection, forming pocket bubbles of high CO2 around an astronaut's head if airflow is stagnant. Continuous environmental and core temp monitoring prevents hypercapnia.",
            physiologicalImpact = "Carbon dioxide concentrations as low as 0.5% (3,800 ppm, compared to 0.04% on Earth) cause 'space headaches', visual disturbances, lethargy, cognitive impairment, and exacerbate SANS intracranial pressure.",
            whenWarrantsAttention = "Habitat CO2 > 3,500 ppm, persistent morning headaches, cognitive confusion, or core body temperature exceeding 37.8°C.",
            preventiveAction = "Verify cabin air circulation fans in sleep quarters. Activate CDRA (Carbon Dioxide Removal Assembly) high-scrub mode. Flush portable oxygen masks if acute hypercapnia symptoms develop."
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
            .testTag("education_guide_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "NASA HUMAN RESEARCH PROGRAM (HRP)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                )
                Text(
                    text = "Spaceflight Hazards & Clinical Guide",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = StarWhite
                    )
                )
                Text(
                    text = "Reference manual explaining the 5 hazards of spaceflight, why measurements are taken, risk thresholds, and evidence-based interventions.",
                    style = MaterialTheme.typography.bodySmall.copy(color = SlateGray)
                )
            }
        }

        // NASA 5 Hazards Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Hazards",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "THE 5 HAZARDS OF HUMAN SPACEFLIGHT (RIDGE)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    HazardPillRow(code = "R", name = "Space Radiation", desc = "Galactic cosmic rays & solar particle events; DNA damage.")
                    HazardPillRow(code = "I", name = "Isolation & Confinement", desc = "Circadian disruption, sensory monotony, sleep deprivation.")
                    HazardPillRow(code = "D", name = "Distance from Earth", desc = "Up to 24-min comm delay; autonomous crew triage required.")
                    HazardPillRow(code = "G", name = "Gravity Fields", desc = "Microgravity fluid shift, SANS, 1% bone density loss/mo.")
                    HazardPillRow(code = "E", name = "Hostile Closed Environment", desc = "Elevated CO2 pockets, cabin microbiome, acoustic stress.")
                }
            }
        }

        // Article List Header
        item {
            Text(
                text = "CLINICAL MEASUREMENTS & PHYSIOLOGY ARTICLES",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGray
                )
            )
        }

        items(articles) { article ->
            val isExpanded = expandedId == article.id
            ArticleAccordionCard(
                article = article,
                isExpanded = isExpanded,
                onToggle = { expandedId = if (isExpanded) null else article.id }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HazardPillRow(code: String, name: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF1E2E55)
        ) {
            Text(
                text = code,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyan,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SlateGray,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun ArticleAccordionCard(
    article: GuideArticle,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("article_card_${article.id}")
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSpaceSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) ElectricBlue else Color(0xFF1E2F54)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = SlateGray
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Why needed
                    GuideSubSection(
                        heading = "WHY THIS MEASUREMENT IS PERFORMED IN SPACE",
                        content = article.whyExamIsNeeded,
                        accentColor = ElectricBlue
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Physiological impact
                    GuideSubSection(
                        heading = "HOW SPACE HAZARDS AFFECT THIS INDICATOR",
                        content = article.physiologicalImpact,
                        accentColor = StatusWarningOrange
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // When warrants attention
                    GuideSubSection(
                        heading = "WHEN RESULTS WARRANT FLIGHT SURGEON ATTENTION",
                        content = article.whenWarrantsAttention,
                        accentColor = StatusCriticalRed
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preventive actions
                    GuideSubSection(
                        heading = "ACTIONS TAKEN TO PREVENT CRITICAL CONDITIONS",
                        content = article.preventiveAction,
                        accentColor = StatusNominalGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideSubSection(
    heading: String,
    content: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DeepSpaceSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22355F))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = heading,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 9.5.sp,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = StarWhite.copy(alpha = 0.95f),
                    lineHeight = 18.sp,
                    fontSize = 11.5.sp
                )
            )
        }
    }
}
