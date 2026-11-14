/**
 * AstroHealth — Astronaut Health Monitoring Dashboard
 * Pure HTML, CSS, JavaScript (Zero External Libraries)
 */

(function () {
  'use strict';

  function getSavedTheme() {
    try {
      return localStorage.getItem('vitalhealth_theme') || 'light';
    } catch (_) {
      return 'light';
    }
  }

  // ================= State Management =================
  const state = {
    theme: getSavedTheme(),
    isLive: true,
    updateIntervalMs: 3000,
    intervalId: null,
    missionDay: 42,
    missionStartTime: Date.now() - (42 * 86400000 + 14 * 3600000 + 32 * 60000),

    // Astronaut Health & Exposure Parameters
    vitals: {
      heartRate: 74,       // BPM (Normal: 60 - 100)
      oxygenLevel: 98.4,   // % SpO2 (Critical if < 90%)
      bodyTemp: 36.8,      // °C (Normal: 36.5 - 37.5)
      sleepDuration: 7.6,  // hours (Goal: 8.0)
      exerciseTime: 145,   // min (Target: 150)
      radiationDose: 0.44  // mSv/day (Normal: < 0.80, Critical: >= 1.50)
    },

    healthStatus: 'NORMAL', // 'NORMAL' | 'WARNING' | 'CRITICAL'
    alertMessage: 'All vital signs, radiation exposure, and cabin life-support indicators are within safe long-duration limits.',
    alertGuidance: 'Recommended action: Continue scheduled daily resistive exercise, hydration, and 8-hour sleep cycle.',
    forcedAlertMode: null,  // null | 'CRITICAL_O2' | 'RADIATION_SPIKE' | 'WARNING_HR'
    selectedHazard: 'radiation',

    // History & Filtering
    history: [],
    maxHistory: 14,
    searchQuery: '',
    tableRange: 'today',

    // Calendar State (November 2026)
    calYear: 2026,
    calMonth: 10, // 0-indexed (10 = November)
    calSelectedDay: 9
  };

  // Seed initial time-varying data points
  const seedTimes = ['09:00', '09:30', '10:00', '10:30', '11:00', '11:30', '12:00', '12:30', '13:00', '13:30', '14:00', '14:30'];
  seedTimes.forEach((lbl, i) => {
    const hr = Math.round(70 + Math.sin(i * 0.7) * 7 + (Math.random() * 4 - 2));
    const ox = +(98.1 + Math.cos(i * 0.5) * 0.8 + (Math.random() * 0.4 - 0.2)).toFixed(1);
    const temp = +(36.6 + Math.random() * 0.4).toFixed(1);
    const rad = +(0.38 + Math.abs(Math.sin(i * 0.4)) * 0.14).toFixed(2);
    state.history.push({
      id: 100 + i,
      time: lbl,
      heartRate: hr,
      oxygenLevel: ox,
      bodyTemp: temp,
      radiationDose: rad,
      sleepDuration: +(7.2 + (i % 3) * 0.2).toFixed(1),
      exerciseTime: 120 + i * 2,
      status: 'NORMAL'
    });
  });

  // ================= DOM Elements =================
  const htmlRoot = document.documentElement;
  const themeToggleBtn = document.getElementById('themeToggleBtn');
  const sidebarThemeBtn = document.getElementById('sidebarThemeBtn');
  const missionTimeDisplay = document.getElementById('missionTimeDisplay');
  const liveToggleBtn = document.getElementById('liveToggleBtn');
  const liveToggleText = document.getElementById('liveToggleText');
  const manualSampleBtn = document.getElementById('manualSampleBtn');
  const simCriticalBtn = document.getElementById('simCriticalBtn');
  const simRadiationBtn = document.getElementById('simRadiationBtn');
  const simWarningBtn = document.getElementById('simWarningBtn');
  const simNormalBtn = document.getElementById('simNormalBtn');
  const resetBtn = document.getElementById('resetBtn');
  const alertBadge = document.getElementById('alertBadge');
  const notifBell = document.getElementById('notifBell');
  const searchInput = document.getElementById('searchInput');

  // Cards
  const valHeartRate = document.getElementById('valHeartRate');
  const tagHeartRate = document.getElementById('tagHeartRate');
  const trendHeartRate = document.getElementById('trendHeartRate');
  const valOxygen = document.getElementById('valOxygen');
  const tagOxygen = document.getElementById('tagOxygen');
  const trendOxygen = document.getElementById('trendOxygen');
  const valTemp = document.getElementById('valTemp');
  const tagTemp = document.getElementById('tagTemp');
  const trendTemp = document.getElementById('trendTemp');
  const valSleep = document.getElementById('valSleep');
  const tagSleep = document.getElementById('tagSleep');
  const trendSleep = document.getElementById('trendSleep');
  const sleepProgressBar = document.getElementById('sleepProgressBar');
  const valExercise = document.getElementById('valExercise');
  const tagExercise = document.getElementById('tagExercise');
  const trendExercise = document.getElementById('trendExercise');
  const exerciseProgressBar = document.getElementById('exerciseProgressBar');
  const valRadiation = document.getElementById('valRadiation');
  const tagRadiation = document.getElementById('tagRadiation');
  const trendRadiation = document.getElementById('trendRadiation');

  // Alert Banner
  const globalAlertBanner = document.getElementById('globalAlertBanner');
  const mainStatusBadge = document.getElementById('mainStatusBadge');
  const alertDescription = document.getElementById('alertDescription');
  const alertGuidance = document.getElementById('alertGuidance');
  const alertTimestamp = document.getElementById('alertTimestamp');
  const alertIconBox = document.getElementById('alertIconBox');

  // Hazards Watch
  const hzTagRadiation = document.getElementById('hzTagRadiation');
  const hzValRadiation = document.getElementById('hzValRadiation');
  const hzTagGravity = document.getElementById('hzTagGravity');
  const hzValGravity = document.getElementById('hzValGravity');
  const hzTagIsolation = document.getElementById('hzTagIsolation');
  const hzValIsolation = document.getElementById('hzValIsolation');
  const hzTagEnvironment = document.getElementById('hzTagEnvironment');
  const hzValEnvironment = document.getElementById('hzValEnvironment');
  const hzTagDistance = document.getElementById('hzTagDistance');
  const hzValDistance = document.getElementById('hzValDistance');
  const hazardDetailTitle = document.getElementById('hazardDetailTitle');
  const hazardDetailRisk = document.getElementById('hazardDetailRisk');
  const hazardDetailText = document.getElementById('hazardDetailText');

  // Chart & Tooltip
  const mainTelemetryChart = document.getElementById('mainTelemetryChart');
  const chartTooltip = document.getElementById('chartTooltip');
  const ttTime = document.getElementById('ttTime');
  const ttHr = document.getElementById('ttHr');
  const ttOx = document.getElementById('ttOx');
  const chartAxisLabels = document.getElementById('chartAxisLabels');
  const sparkHeartRate = document.getElementById('sparkHeartRate');
  const sparkOxygen = document.getElementById('sparkOxygen');
  const sparkTemp = document.getElementById('sparkTemp');
  const sparkRadiation = document.getElementById('sparkRadiation');

  // Table & Filter
  const telemetryTableBody = document.getElementById('telemetryTableBody');
  const tableTimeFilter = document.getElementById('tableTimeFilter');

  // Calendar
  const calMonthTitle = document.getElementById('calMonthTitle');
  const calDaysGrid = document.getElementById('calDaysGrid');
  const calPrevMonth = document.getElementById('calPrevMonth');
  const calNextMonth = document.getElementById('calNextMonth');
  const calSelectedTitle = document.getElementById('calSelectedTitle');
  const calSelectedDesc = document.getElementById('calSelectedDesc');

  // Mobile sidebar & nav
  const mobileToggleBtn = document.getElementById('mobileToggleBtn');
  const mobileCloseBtn = document.getElementById('mobileCloseBtn');
  const sidebar = document.getElementById('sidebar');
  const navGroupToggle = document.getElementById('navGroupToggle');
  const navMonitorGroup = document.getElementById('navMonitorGroup');

  // ================= Theme Management =================
  function initTheme() {
    htmlRoot.setAttribute('data-theme', state.theme);
  }

  function toggleTheme() {
    state.theme = state.theme === 'light' ? 'dark' : 'light';
    htmlRoot.setAttribute('data-theme', state.theme);
    try {
      localStorage.setItem('vitalhealth_theme', state.theme);
    } catch (_) {
      // Ignore storage errors in restricted webviews
    }
    renderMainChart();
    renderSparklines();
  }

  themeToggleBtn.addEventListener('click', toggleTheme);
  if (sidebarThemeBtn) {
    sidebarThemeBtn.addEventListener('click', (e) => {
      e.preventDefault();
      toggleTheme();
    });
  }

  // Mobile drawer & navigation
  if (mobileToggleBtn && sidebar) {
    mobileToggleBtn.addEventListener('click', () => sidebar.classList.add('open'));
  }
  if (mobileCloseBtn && sidebar) {
    mobileCloseBtn.addEventListener('click', () => sidebar.classList.remove('open'));
  }
  if (navGroupToggle && navMonitorGroup) {
    navGroupToggle.addEventListener('click', () => navMonitorGroup.classList.toggle('open'));
  }

  document.querySelectorAll('.nav-item[data-target], .sub-item[data-target]').forEach((link) => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      document.querySelectorAll('.nav-item, .sub-item').forEach((el) => el.classList.remove('active'));
      link.classList.add('active');
      const targetEl = document.getElementById(link.getAttribute('data-target'));
      if (targetEl) {
        targetEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
      if (sidebar && sidebar.classList.contains('open')) {
        sidebar.classList.remove('open');
      }
    });
  });

  if (notifBell) {
    notifBell.addEventListener('click', () => {
      const banner = document.getElementById('alertBannerSection');
      if (banner) banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
  }

  // ================= Mission Day & Live Time Indicator =================
  function updateMissionTime() {
    const elapsed = Date.now() - state.missionStartTime;
    const totalSeconds = Math.floor(elapsed / 1000);
    const days = Math.floor(totalSeconds / 86400);
    const hours = Math.floor((totalSeconds % 86400) / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    state.missionDay = days;
    const pad = (n) => String(n).padStart(2, '0');
    missionTimeDisplay.textContent = `MET DAY ${days} • ${pad(hours)}:${pad(minutes)}:${pad(seconds)} UTC`;
  }
  setInterval(updateMissionTime, 1000);
  updateMissionTime();

  // ================= Health Status & Alert Evaluation =================
  function evaluateHealthStatus(vitals) {
    let status = 'NORMAL';
    let message = 'All vital signs, radiation exposure, and cabin life-support indicators are within safe long-duration limits.';
    let guidance = 'Recommended action: Continue scheduled daily resistive exercise, hydration, and 8-hour sleep cycle.';

    if (vitals.oxygenLevel < 90.0) {
      status = 'CRITICAL';
      message = `CRITICAL ALERT: Oxygen saturation dropped to ${vitals.oxygenLevel}% (< 90%) in closed habitat.`;
      guidance = 'Immediate action: Put on emergency oxygen mask, inspect cabin air scrubbers and pressure seals, and avoid physical exertion.';
    } else if (vitals.radiationDose >= 1.5) {
      status = 'CRITICAL';
      message = `CRITICAL ALERT: Elevated ionizing radiation dose rate (${vitals.radiationDose.toFixed(2)} mSv/day).`;
      guidance = 'Immediate action: Move to the shielded storm shelter bay, suspend extravehicular activity, and log personal dosimeter totals.';
    } else if (vitals.heartRate > 125 || vitals.heartRate < 45) {
      status = 'CRITICAL';
      message = `CRITICAL ALERT: Heart rate reached ${vitals.heartRate} BPM outside safe cardiac limits.`;
      guidance = 'Immediate action: Stop strenuous activity, hydrate with electrolytes, and run a 12-lead ECG self-check.';
    } else if (vitals.bodyTemp > 38.5 || vitals.bodyTemp < 35.5) {
      status = 'CRITICAL';
      message = `CRITICAL ALERT: Core body temperature shifted to ${vitals.bodyTemp} °C.`;
      guidance = 'Immediate action: Inspect thermal garment loop, monitor for immune fever/infection signs, and notify ground flight surgeon.';
    } else if (vitals.oxygenLevel < 95.0) {
      status = 'WARNING';
      message = `WARNING: Mild oxygen drop detected (${vitals.oxygenLevel}%) in sealed module.`;
      guidance = 'Recommended action: Perform paced deep breathing, verify ventilation fan flow, and re-test pulse oximeter in 5 minutes.';
    } else if (vitals.radiationDose >= 0.85) {
      status = 'WARNING';
      message = `WARNING: Radiation dose rate rising (${vitals.radiationDose.toFixed(2)} mSv/day).`;
      guidance = 'Recommended action: Limit time near unshielded hull segments and prepare storm shelter positioning if rates climb.';
    } else if (vitals.heartRate > 100 || vitals.heartRate < 55) {
      status = 'WARNING';
      message = `WARNING: Heart rate shifted from resting baseline (${vitals.heartRate} BPM) due to fluid-shift or exertion stress.`;
      guidance = 'Recommended action: Pause workout load, drink 400 mL water, and rest seated for 15 minutes before re-measuring.';
    } else if (vitals.bodyTemp > 37.5 || vitals.bodyTemp < 36.2) {
      status = 'WARNING';
      message = `WARNING: Body temperature drifted to ${vitals.bodyTemp} °C.`;
      guidance = 'Recommended action: Adjust cabin cooling airflow and check for early signs of immune strain.';
    } else if (vitals.sleepDuration < 6.0) {
      status = 'WARNING';
      message = `WARNING: Sleep duration low (${vitals.sleepDuration} hrs) — confinement & circadian fatigue risk.`;
      guidance = 'Recommended action: Dim cabin lighting before sleep shift, reduce stimulants, and schedule a 45-minute recovery rest.';
    }

    state.healthStatus = status;
    state.alertMessage = message;
    state.alertGuidance = guidance;
    return { status, message, guidance };
  }

  // ================= Random Health Data Generator (Varies Over Time) =================
  let packetCounter = 120;

  function generateRandomVitals() {
    if (state.forcedAlertMode === 'CRITICAL_O2') {
      state.vitals.oxygenLevel = +(85.5 + Math.random() * 4.0).toFixed(1); // < 90%
      state.vitals.heartRate = Math.round(98 + Math.random() * 16);
      state.vitals.bodyTemp = +(37.1 + Math.random() * 0.4).toFixed(1);
      state.vitals.radiationDose = +(0.45 + Math.random() * 0.1).toFixed(2);
    } else if (state.forcedAlertMode === 'RADIATION_SPIKE') {
      state.vitals.radiationDose = +(1.62 + Math.random() * 0.45).toFixed(2); // >= 1.50 mSv/day
      state.vitals.heartRate = Math.round(86 + Math.random() * 12);
      state.vitals.oxygenLevel = +(97.2 + Math.random() * 1.2).toFixed(1);
      state.vitals.bodyTemp = +(37.0 + Math.random() * 0.3).toFixed(1);
    } else if (state.forcedAlertMode === 'WARNING_HR') {
      state.vitals.oxygenLevel = +(94.2 + Math.random() * 1.8).toFixed(1);
      state.vitals.heartRate = Math.round(108 + Math.random() * 12);
      state.vitals.bodyTemp = +(37.7 + Math.random() * 0.4).toFixed(1);
      state.vitals.radiationDose = +(0.52 + Math.random() * 0.15).toFixed(2);
    } else {
      const hrDelta = (Math.random() - 0.48) * 5;
      const oxDelta = (Math.random() - 0.48) * 0.5;
      const tempDelta = (Math.random() - 0.5) * 0.14;
      const sleepDelta = (Math.random() - 0.5) * 0.1;
      const radDelta = (Math.random() - 0.5) * 0.06;

      state.vitals.heartRate = Math.round(Math.min(96, Math.max(62, state.vitals.heartRate + hrDelta)));
      state.vitals.oxygenLevel = +Math.min(99.8, Math.max(95.8, state.vitals.oxygenLevel + oxDelta)).toFixed(1);
      state.vitals.bodyTemp = +Math.min(37.4, Math.max(36.5, state.vitals.bodyTemp + tempDelta)).toFixed(1);
      state.vitals.sleepDuration = +Math.min(8.4, Math.max(6.8, state.vitals.sleepDuration + sleepDelta)).toFixed(1);
      state.vitals.exerciseTime = Math.min(150, state.vitals.exerciseTime + (Math.random() > 0.65 ? 1 : 0));
      state.vitals.radiationDose = +Math.min(0.68, Math.max(0.32, state.vitals.radiationDose + radDelta)).toFixed(2);
    }

    evaluateHealthStatus(state.vitals);

    const now = new Date();
    const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
    packetCounter += 1;

    state.history.push({
      id: packetCounter,
      time: timeStr,
      heartRate: state.vitals.heartRate,
      oxygenLevel: state.vitals.oxygenLevel,
      bodyTemp: state.vitals.bodyTemp,
      radiationDose: state.vitals.radiationDose,
      sleepDuration: state.vitals.sleepDuration,
      exerciseTime: state.vitals.exerciseTime,
      status: state.healthStatus
    });

    if (state.history.length > state.maxHistory) {
      state.history.shift();
    }

    updateUI();
  }

  // ================= Spaceflight Hazard Detail Content =================
  function getHazardDetails(hazardKey) {
    const v = state.vitals;
    switch (hazardKey) {
      case 'radiation':
        return {
          title: 'Space Radiation • Cellular & Immune Protection',
          risk: `Current Dose: ${v.radiationDose.toFixed(2)} mSv/day`,
          text: v.radiationDose >= 1.2
            ? 'Dosimeter alerts indicate elevated solar or cosmic particle flux. Move to the high-density storm shelter bay, minimize extravehicular tasks, and stay hydrated with antioxidant nutrition.'
            : 'Personal dosimeters track ionizing particle exposure that can gradually affect immune cells, DNA repair, and blood vessels. Shielded habitat zones keep daily dosage within safe margins.'
        };
      case 'gravity':
        return {
          title: 'Altered Gravity • Bone, Muscle & Fluid Shifts',
          risk: `Daily Exercise: ${v.exerciseTime} / 150 min`,
          text: 'Weightlessness shifts body fluids toward the head and reduces load on bones and muscles, raising risks for bone density loss, vision pressure changes, and orthostatic intolerance. Daily resistive and aerobic exercise preserves cardiovascular and skeletal strength.'
        };
      case 'isolation':
        return {
          title: 'Isolation & Confinement • Behavioral & Sleep Health',
          risk: `Sleep Duration: ${v.sleepDuration.toFixed(1)} / 8.0 hrs`,
          text: 'Months in a confined spacecraft without natural daylight cues can disrupt circadian rhythms, mood, and cognitive focus. Structured sleep schedules, blue-enriched morning light, and daily crew check-ins prevent behavioral fatigue.'
        };
      case 'environment':
        return {
          title: 'Hostile Closed Environment • Cabin Air & Immune Watch',
          risk: `SpO₂: ${v.oxygenLevel.toFixed(1)}% • Temp: ${v.bodyTemp.toFixed(1)} °C`,
          text: 'Recycled cabin air, microbial buildup, and trace gases inside sealed modules directly affect respiratory oxygen exchange and immune balance. Continuous SpO₂ and core temperature tracking detects hypoxia or thermal stress early.'
        };
      case 'distance':
      default:
        return {
          title: 'Distance from Earth • Autonomous Self-Assessment',
          risk: 'Signal Latency: 6m 40s one-way',
          text: 'Deep-space distance delays real-time communication with ground flight surgeons. Onboard physiological monitoring, biological experiment insights, and clear alert thresholds empower astronauts to diagnose symptoms and act independently.'
        };
    }
  }

  function renderHazardsPanel(hrStatus, oxStatus, tempStatus, sleepStatus, radStatus) {
    if (hzTagRadiation) {
      updateTag(hzTagRadiation, radStatus);
      hzValRadiation.textContent = `${state.vitals.radiationDose.toFixed(2)} mSv/day • ${radStatus === 'Normal' ? 'Shielding nominal' : 'Shelter check advised'}`;
    }
    if (hzTagGravity) {
      const gravStatus = state.vitals.exerciseTime < 90 ? 'Warning' : 'Normal';
      updateTag(hzTagGravity, gravStatus);
      hzValGravity.textContent = `Fluid & bone guard • ${state.vitals.exerciseTime}m workout`;
    }
    if (hzTagIsolation) {
      updateTag(hzTagIsolation, sleepStatus);
      hzValIsolation.textContent = `${state.vitals.sleepDuration.toFixed(1)}h sleep • ${sleepStatus === 'Normal' ? 'Circadian stable' : 'Recovery needed'}`;
    }
    if (hzTagEnvironment) {
      const envStatus = oxStatus === 'Critical' || tempStatus === 'Critical'
        ? 'Critical'
        : (oxStatus === 'Warning' || tempStatus === 'Warning' ? 'Warning' : 'Normal');
      updateTag(hzTagEnvironment, envStatus);
      hzValEnvironment.textContent = `SpO₂ ${state.vitals.oxygenLevel.toFixed(1)}% • Temp ${state.vitals.bodyTemp.toFixed(1)}°C`;
    }
    if (hzTagDistance) {
      updateTag(hzTagDistance, 'Normal');
      hzValDistance.textContent = '6m 40s lag • Onboard triage active';
    }

    const detail = getHazardDetails(state.selectedHazard);
    if (hazardDetailTitle) hazardDetailTitle.textContent = detail.title;
    if (hazardDetailRisk) hazardDetailRisk.textContent = detail.risk;
    if (hazardDetailText) hazardDetailText.textContent = detail.text;
  }

  document.querySelectorAll('.hazard-tile[data-hazard]').forEach((tile) => {
    tile.addEventListener('click', () => {
      document.querySelectorAll('.hazard-tile').forEach((t) => t.classList.remove('active'));
      tile.classList.add('active');
      state.selectedHazard = tile.getAttribute('data-hazard') || 'radiation';
      const detail = getHazardDetails(state.selectedHazard);
      if (hazardDetailTitle) hazardDetailTitle.textContent = detail.title;
      if (hazardDetailRisk) hazardDetailRisk.textContent = detail.risk;
      if (hazardDetailText) hazardDetailText.textContent = detail.text;
    });
  });

  // ================= UI Render & Update =================
  function updateUI() {
    valHeartRate.textContent = state.vitals.heartRate;
    valOxygen.textContent = state.vitals.oxygenLevel.toFixed(1);
    valTemp.textContent = state.vitals.bodyTemp.toFixed(1);
    valSleep.textContent = state.vitals.sleepDuration.toFixed(1);
    valExercise.textContent = state.vitals.exerciseTime;
    if (valRadiation) valRadiation.textContent = state.vitals.radiationDose.toFixed(2);

    const tempF = ((state.vitals.bodyTemp * 9) / 5 + 32).toFixed(1);
    trendTemp.textContent = `${tempF} °F`;

    const sleepPct = Math.min(100, Math.round((state.vitals.sleepDuration / 8.0) * 100));
    trendSleep.textContent = `${sleepPct}% Goal`;
    sleepProgressBar.style.width = `${sleepPct}%`;

    const exPct = Math.min(100, Math.round((state.vitals.exerciseTime / 150) * 100));
    trendExercise.textContent = `${exPct}% Target`;
    exerciseProgressBar.style.width = `${exPct}%`;

    // Card Status Tags
    const hrStatus = state.vitals.heartRate > 125 || state.vitals.heartRate < 45 ? 'Critical'
      : (state.vitals.heartRate > 100 || state.vitals.heartRate < 55 ? 'Warning' : 'Normal');
    const oxStatus = state.vitals.oxygenLevel < 90 ? 'Critical'
      : (state.vitals.oxygenLevel < 95 ? 'Warning' : 'Normal');
    const tempStatus = state.vitals.bodyTemp > 38.5 || state.vitals.bodyTemp < 35.5 ? 'Critical'
      : (state.vitals.bodyTemp > 37.5 || state.vitals.bodyTemp < 36.2 ? 'Warning' : 'Normal');
    const sleepStatus = state.vitals.sleepDuration < 6.0 ? 'Warning' : 'Normal';
    const radStatus = state.vitals.radiationDose >= 1.5 ? 'Critical'
      : (state.vitals.radiationDose >= 0.85 ? 'Warning' : 'Normal');

    updateTag(tagHeartRate, hrStatus);
    updateTag(tagOxygen, oxStatus);
    updateTag(tagTemp, tempStatus);
    updateTag(tagSleep, sleepStatus);
    updateTag(tagExercise, 'Normal');
    if (tagRadiation) updateTag(tagRadiation, radStatus);

    trendHeartRate.textContent = hrStatus === 'Normal' ? '● Normal' : `▲ ${hrStatus}`;
    trendHeartRate.className = 'stat-trend ' + (hrStatus === 'Critical' ? 'danger' : (hrStatus === 'Warning' ? 'warning' : 'positive'));

    trendOxygen.textContent = oxStatus === 'Normal' ? '● Optimal' : (oxStatus === 'Warning' ? '▼ Low O₂' : '✕ CRITICAL');
    trendOxygen.className = 'stat-trend ' + (oxStatus === 'Critical' ? 'danger' : (oxStatus === 'Warning' ? 'warning' : 'positive'));

    if (trendRadiation) {
      trendRadiation.textContent = radStatus === 'Normal' ? '● Shielded' : (radStatus === 'Warning' ? '▲ Rising' : '✕ STORM ALERT');
      trendRadiation.className = 'stat-trend ' + (radStatus === 'Critical' ? 'danger' : (radStatus === 'Warning' ? 'warning' : 'positive'));
    }

    // Global Alert Banner
    globalAlertBanner.className = `alert-banner ${state.healthStatus.toLowerCase()}`;
    mainStatusBadge.className = `status-pill ${state.healthStatus.toLowerCase()}`;
    mainStatusBadge.textContent = `HEALTH STATUS: ${state.healthStatus}`;
    alertDescription.textContent = state.alertMessage;
    if (alertGuidance) alertGuidance.textContent = state.alertGuidance;
    alertTimestamp.textContent = `Updated ${new Date().toLocaleTimeString()} • Auto-Monitoring Active`;

    if (state.healthStatus === 'CRITICAL') {
      alertIconBox.innerHTML = `
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="8" x2="12" y2="12"></line>
          <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>`;
      alertBadge.textContent = '!';
      alertBadge.style.display = 'flex';
    } else if (state.healthStatus === 'WARNING') {
      alertIconBox.innerHTML = `
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
          <line x1="12" y1="9" x2="12" y2="13"></line>
          <line x1="12" y1="17" x2="12.01" y2="17"></line>
        </svg>`;
      alertBadge.textContent = '1';
      alertBadge.style.display = 'flex';
    } else {
      alertIconBox.innerHTML = `
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </svg>`;
      alertBadge.style.display = 'none';
    }

    renderHazardsPanel(hrStatus, oxStatus, tempStatus, sleepStatus, radStatus);
    renderMainChart();
    renderSparklines();
    renderTable();
  }

  function updateTag(element, statusLabel) {
    element.textContent = statusLabel;
    element.className = 'status-tag ' + statusLabel.toLowerCase();
  }

  // ================= Canvas Chart: Main Telemetry Spline Waveform =================
  let chartCoords = [];

  function renderMainChart(highlightIdx = -1) {
    if (!mainTelemetryChart) return;
    const ctx = mainTelemetryChart.getContext('2d');
    const width = mainTelemetryChart.width;
    const height = mainTelemetryChart.height;
    const isDark = state.theme === 'dark';

    ctx.clearRect(0, 0, width, height);

    const padLeft = 40;
    const padRight = 20;
    const padTop = 20;
    const padBottom = 26;
    const plotW = width - padLeft - padRight;
    const plotH = height - padTop - padBottom;

    ctx.strokeStyle = isDark ? '#1e2b48' : '#e5e9f2';
    ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i++) {
      const y = padTop + (plotH / 4) * i;
      ctx.beginPath();
      ctx.moveTo(padLeft, y);
      ctx.lineTo(width - padRight, y);
      ctx.stroke();

      ctx.fillStyle = isDark ? '#64748b' : '#94a3b8';
      ctx.font = '10px ui-monospace, monospace';
      ctx.textAlign = 'right';
      const labelVal = Math.round(115 - i * 15);
      ctx.fillText(labelVal, padLeft - 8, y + 3);
    }

    const data = state.history;
    if (data.length < 2) return;

    const getHrY = (val) => padTop + plotH - ((val - 50) / 80) * plotH;
    const getOxY = (val) => padTop + plotH - ((val - 82) / 20) * plotH;
    const getX = (idx) => padLeft + (idx / (data.length - 1)) * plotW;

    chartCoords = data.map((d, i) => ({
      x: getX(i),
      hrY: getHrY(d.heartRate),
      oxY: getOxY(d.oxygenLevel),
      point: d
    }));

    // Oxygen Area
    ctx.beginPath();
    ctx.moveTo(chartCoords[0].x, height - padBottom);
    chartCoords.forEach((pt, i) => {
      if (i === 0) ctx.lineTo(pt.x, pt.oxY);
      else {
        const prev = chartCoords[i - 1];
        const cpX = (prev.x + pt.x) / 2;
        ctx.bezierCurveTo(cpX, prev.oxY, cpX, pt.oxY, pt.x, pt.oxY);
      }
    });
    ctx.lineTo(chartCoords[chartCoords.length - 1].x, height - padBottom);
    ctx.closePath();
    const oxGrad = ctx.createLinearGradient(0, padTop, 0, height - padBottom);
    oxGrad.addColorStop(0, 'rgba(99, 102, 241, 0.24)');
    oxGrad.addColorStop(1, 'rgba(99, 102, 241, 0.0)');
    ctx.fillStyle = oxGrad;
    ctx.fill();

    // Oxygen Stroke
    ctx.beginPath();
    chartCoords.forEach((pt, i) => {
      if (i === 0) ctx.moveTo(pt.x, pt.oxY);
      else {
        const prev = chartCoords[i - 1];
        const cpX = (prev.x + pt.x) / 2;
        ctx.bezierCurveTo(cpX, prev.oxY, cpX, pt.oxY, pt.x, pt.oxY);
      }
    });
    ctx.strokeStyle = '#6366f1';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    // Heart Rate Stroke
    ctx.beginPath();
    chartCoords.forEach((pt, i) => {
      if (i === 0) ctx.moveTo(pt.x, pt.hrY);
      else {
        const prev = chartCoords[i - 1];
        const cpX = (prev.x + pt.x) / 2;
        ctx.bezierCurveTo(cpX, prev.hrY, cpX, pt.hrY, pt.x, pt.hrY);
      }
    });
    ctx.strokeStyle = '#06b6d4';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    // Highlight or End Dots
    const activeIdx = highlightIdx >= 0 && highlightIdx < chartCoords.length ? highlightIdx : chartCoords.length - 1;
    const active = chartCoords[activeIdx];

    if (highlightIdx >= 0) {
      ctx.save();
      ctx.setLineDash([4, 4]);
      ctx.strokeStyle = isDark ? 'rgba(255,255,255,0.25)' : 'rgba(30,41,59,0.25)';
      ctx.beginPath();
      ctx.moveTo(active.x, padTop);
      ctx.lineTo(active.x, height - padBottom);
      ctx.stroke();
      ctx.restore();
    }

    [
      { y: active.hrY, color: '#06b6d4' },
      { y: active.oxY, color: '#6366f1' }
    ].forEach(({ y, color }) => {
      ctx.beginPath();
      ctx.arc(active.x, y, 4.5, 0, Math.PI * 2);
      ctx.fillStyle = color;
      ctx.fill();
      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 2;
      ctx.stroke();
    });

    // Update bottom X-axis labels dynamically
    if (chartAxisLabels) {
      const step = Math.max(1, Math.floor(data.length / 6));
      const labels = data.filter((_, i) => i % step === 0 || i === data.length - 1).map((d) => d.time);
      chartAxisLabels.innerHTML = labels.map((t) => `<span>${t}</span>`).join('');
    }
  }

  // Interactive Chart Tooltip
  if (mainTelemetryChart && chartTooltip) {
    const handleInspect = (clientX) => {
      if (!chartCoords.length) return;
      const rect = mainTelemetryChart.getBoundingClientRect();
      const relX = ((clientX - rect.left) / rect.width) * mainTelemetryChart.width;
      let nearestIdx = 0;
      let minDist = Infinity;
      chartCoords.forEach((pt, idx) => {
        const dist = Math.abs(pt.x - relX);
        if (dist < minDist) {
          minDist = dist;
          nearestIdx = idx;
        }
      });

      const chosen = chartCoords[nearestIdx];
      ttTime.textContent = `${chosen.point.time} UTC`;
      ttHr.textContent = `${chosen.point.heartRate} BPM`;
      ttOx.textContent = `${chosen.point.oxygenLevel}%`;
      chartTooltip.style.display = 'block';
      chartTooltip.style.left = `${Math.min(82, Math.max(18, ((clientX - rect.left) / rect.width) * 100))}%`;
      renderMainChart(nearestIdx);
    };

    mainTelemetryChart.addEventListener('mousemove', (e) => handleInspect(e.clientX));
    mainTelemetryChart.addEventListener('touchstart', (e) => {
      if (e.touches && e.touches[0]) handleInspect(e.touches[0].clientX);
    }, { passive: true });
    mainTelemetryChart.addEventListener('mouseleave', () => {
      chartTooltip.style.display = 'none';
      renderMainChart(-1);
    });
  }

  // ================= Sparklines for Stat Cards =================
  function renderSparklines() {
    drawMiniSparkline(sparkHeartRate, state.history.map((d) => d.heartRate), '#06b6d4');
    drawMiniSparkline(sparkOxygen, state.history.map((d) => d.oxygenLevel), '#6366f1');
    drawMiniSparkline(sparkTemp, state.history.map((d) => d.bodyTemp), '#f59e0b');
    drawMiniSparkline(sparkRadiation, state.history.map((d) => d.radiationDose), '#f43f5e');
  }

  function drawMiniSparkline(canvas, values, strokeColor) {
    if (!canvas || values.length < 2) return;
    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const h = canvas.height;
    ctx.clearRect(0, 0, w, h);

    const min = Math.min(...values);
    const max = Math.max(...values);
    const range = (max - min) || 1;

    ctx.beginPath();
    values.forEach((v, i) => {
      const x = (i / (values.length - 1)) * (w - 6) + 3;
      const y = h - 4 - ((v - min) / range) * (h - 8);
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.strokeStyle = strokeColor;
    ctx.lineWidth = 2;
    ctx.lineCap = 'round';
    ctx.stroke();
  }

  // ================= Table Rendering & Filtering =================
  function renderTable() {
    if (!telemetryTableBody) return;

    const query = state.searchQuery.trim().toLowerCase();
    const limit = state.tableRange === 'today' ? 6 : (state.tableRange === 'weekly' ? 8 : 12);

    const rows = [...state.history].reverse().slice(0, limit).filter((row) => {
      if (!query) return true;
      const packetId = `#MET-${state.missionDay}-${row.id}`.toLowerCase();
      return (
        packetId.includes(query) ||
        row.time.toLowerCase().includes(query) ||
        row.status.toLowerCase().includes(query) ||
        String(row.heartRate).includes(query) ||
        String(row.oxygenLevel).includes(query) ||
        String(row.radiationDose).includes(query)
      );
    });

    if (rows.length === 0) {
      telemetryTableBody.innerHTML = `
        <tr>
          <td colspan="9" style="text-align:center; padding: 24px; color: var(--text-muted);">
            No matching telemetry packets found for "${state.searchQuery}".
          </td>
        </tr>`;
      return;
    }

    telemetryTableBody.innerHTML = rows.map((row) => {
      const st = row.status.toLowerCase();
      const stLabel = row.status.charAt(0) + row.status.slice(1).toLowerCase();

      return `
        <tr>
          <td><strong style="font-family: var(--font-mono); font-size: 0.78rem;">#MET-${state.missionDay}-${row.id}</strong></td>
          <td>${row.time} UTC</td>
          <td><strong>${row.heartRate}</strong> BPM</td>
          <td style="${row.oxygenLevel < 90 ? 'color: #ef4444; font-weight: 800;' : ''}">${row.oxygenLevel}%</td>
          <td>${row.bodyTemp}°C</td>
          <td style="${row.radiationDose >= 1.5 ? 'color: #ef4444; font-weight: 800;' : ''}">${row.radiationDose.toFixed(2)} mSv/d</td>
          <td>${row.sleepDuration}h</td>
          <td>${row.exerciseTime}m</td>
          <td><span class="badge-status ${st}">● ${stLabel}</span></td>
        </tr>`;
    }).join('');
  }

  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      state.searchQuery = e.target.value;
      renderTable();
    });
  }

  if (tableTimeFilter) {
    tableTimeFilter.querySelectorAll('.tab-pill').forEach((btn) => {
      btn.addEventListener('click', () => {
        tableTimeFilter.querySelectorAll('.tab-pill').forEach((b) => b.classList.remove('active'));
        btn.classList.add('active');
        state.tableRange = btn.getAttribute('data-range') || 'today';
        renderTable();
      });
    });
  }

  // ================= Interactive Calendar Generator =================
  const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
  const calendarEvents = {
    4: 'Cardiovascular & fluid-shift checkup',
    9: 'Active Mission Day 42 • Routine vitals, dosimeter sync & daily workout',
    13: 'Dosimeter audit & radiation shield verification',
    16: 'Extravehicular suit check & hypoxia readiness',
    20: 'Circadian sleep & behavioral wellness review',
    24: 'Bone density & resistive exercise assessment'
  };

  function renderCalendar() {
    if (!calDaysGrid || !calMonthTitle) return;

    const year = state.calYear;
    const month = state.calMonth;
    calMonthTitle.textContent = `${monthNames[month]} ${year}`;

    const firstDayOfWeek = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const prevMonthDays = new Date(year, month, 0).getDate();

    let html = '';

    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
      html += `<div class="cal-day other-month">${prevMonthDays - i}</div>`;
    }

    for (let d = 1; d <= daysInMonth; d++) {
      const isSelected = d === state.calSelectedDay;
      const hasAppt = d === 4 || d === 9 || d === 20;
      const hasMeeting = d === 13 || d === 16;
      const hasSurgery = d === 24;

      let dotHtml = '';
      if (hasAppt) dotHtml += '<span class="cal-dot-indicator appt"></span>';
      if (hasMeeting) dotHtml += '<span class="cal-dot-indicator meeting"></span>';
      if (hasSurgery) dotHtml += '<span class="cal-dot-indicator surgery"></span>';

      html += `
        <div class="cal-day ${isSelected ? 'today active-mission' : ''}" data-day="${d}">
          ${d}
          ${dotHtml}
        </div>`;
    }

    calDaysGrid.innerHTML = html;

    calDaysGrid.querySelectorAll('.cal-day[data-day]').forEach((cell) => {
      cell.addEventListener('click', () => {
        const day = parseInt(cell.getAttribute('data-day'), 10);
        state.calSelectedDay = day;
        renderCalendar();
        if (calSelectedTitle && calSelectedDesc) {
          calSelectedTitle.textContent = `${monthNames[month]} ${day}, ${year}`;
          calSelectedDesc.textContent = calendarEvents[day] || 'Standard daily biometric monitoring & countermeasure exercise';
        }
      });
    });
  }

  if (calPrevMonth) {
    calPrevMonth.addEventListener('click', () => {
      state.calMonth -= 1;
      if (state.calMonth < 0) {
        state.calMonth = 11;
        state.calYear -= 1;
      }
      state.calSelectedDay = 1;
      renderCalendar();
    });
  }

  if (calNextMonth) {
    calNextMonth.addEventListener('click', () => {
      state.calMonth += 1;
      if (state.calMonth > 11) {
        state.calMonth = 0;
        state.calYear += 1;
      }
      state.calSelectedDay = 1;
      renderCalendar();
    });
  }

  // ================= Event Listeners & Alert Triggers =================
  function startLiveLoop() {
    if (state.intervalId) clearInterval(state.intervalId);
    state.intervalId = setInterval(generateRandomVitals, state.updateIntervalMs);
  }

  liveToggleBtn.addEventListener('click', () => {
    state.isLive = !state.isLive;
    if (state.isLive) {
      liveToggleBtn.classList.add('active');
      liveToggleText.textContent = 'Live Telemetry: ON (3s)';
      startLiveLoop();
    } else {
      liveToggleBtn.classList.remove('active');
      liveToggleText.textContent = 'Live Telemetry: PAUSED';
      if (state.intervalId) clearInterval(state.intervalId);
    }
  });

  manualSampleBtn.addEventListener('click', () => generateRandomVitals());

  simCriticalBtn.addEventListener('click', () => {
    state.forcedAlertMode = 'CRITICAL_O2';
    generateRandomVitals();
  });

  if (simRadiationBtn) {
    simRadiationBtn.addEventListener('click', () => {
      state.forcedAlertMode = 'RADIATION_SPIKE';
      generateRandomVitals();
    });
  }

  simWarningBtn.addEventListener('click', () => {
    state.forcedAlertMode = 'WARNING_HR';
    generateRandomVitals();
  });

  const resetNormal = () => {
    state.forcedAlertMode = null;
    state.vitals.heartRate = 74;
    state.vitals.oxygenLevel = 98.4;
    state.vitals.bodyTemp = 36.8;
    state.vitals.sleepDuration = 7.6;
    state.vitals.radiationDose = 0.44;
    generateRandomVitals();
  };

  simNormalBtn.addEventListener('click', resetNormal);
  resetBtn.addEventListener('click', (e) => {
    e.preventDefault();
    resetNormal();
  });

  // ================= Initialization =================
  initTheme();
  renderCalendar();
  evaluateHealthStatus(state.vitals);
  updateUI();
  startLiveLoop();
})();
