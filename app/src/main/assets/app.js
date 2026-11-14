/**
 * VitalHealth — Astronaut Health Monitoring Dashboard
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

    // Astronaut Health Parameters
    vitals: {
      heartRate: 74,       // BPM (Normal: 60 - 100)
      oxygenLevel: 98.4,   // % SpO2 (Critical if < 90%)
      bodyTemp: 36.8,      // °C (Normal: 36.5 - 37.5)
      sleepDuration: 7.6,  // hours (Goal: 8.0)
      exerciseTime: 145    // min (Target: 150)
    },

    healthStatus: 'NORMAL', // 'NORMAL' | 'WARNING' | 'CRITICAL'
    alertMessage: 'All vital signs are within normal spaceflight boundaries.',
    forcedAlertMode: null,  // null | 'CRITICAL_O2' | 'WARNING_HR'

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
    state.history.push({
      id: 100 + i,
      time: lbl,
      heartRate: hr,
      oxygenLevel: ox,
      bodyTemp: temp,
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

  // Alert Banner
  const globalAlertBanner = document.getElementById('globalAlertBanner');
  const mainStatusBadge = document.getElementById('mainStatusBadge');
  const alertDescription = document.getElementById('alertDescription');
  const alertTimestamp = document.getElementById('alertTimestamp');
  const alertIconBox = document.getElementById('alertIconBox');

  // Chart & Tooltip
  const mainTelemetryChart = document.getElementById('mainTelemetryChart');
  const chartContainer = document.getElementById('chartContainer');
  const chartTooltip = document.getElementById('chartTooltip');
  const ttTime = document.getElementById('ttTime');
  const ttHr = document.getElementById('ttHr');
  const ttOx = document.getElementById('ttOx');
  const chartAxisLabels = document.getElementById('chartAxisLabels');
  const sparkHeartRate = document.getElementById('sparkHeartRate');
  const sparkOxygen = document.getElementById('sparkOxygen');
  const sparkTemp = document.getElementById('sparkTemp');

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
    let message = 'All vital signs are within normal spaceflight boundaries.';

    if (vitals.oxygenLevel < 90.0) {
      status = 'CRITICAL';
      message = `CRITICAL ALERT: Oxygen level dropped to ${vitals.oxygenLevel}% (< 90%). Deploy emergency oxygen mask and verify cabin air pressure immediately.`;
    } else if (vitals.heartRate > 125 || vitals.heartRate < 45) {
      status = 'CRITICAL';
      message = `CRITICAL ALERT: Heart rate is ${vitals.heartRate} BPM (critical boundary). Stop physical exertion and initiate rest stabilization.`;
    } else if (vitals.bodyTemp > 38.5 || vitals.bodyTemp < 35.5) {
      status = 'CRITICAL';
      message = `CRITICAL ALERT: Core body temperature at ${vitals.bodyTemp} °C. Immediate thermal suit check required.`;
    } else if (vitals.oxygenLevel < 95.0) {
      status = 'WARNING';
      message = `WARNING: Mild oxygen drop detected (${vitals.oxygenLevel}%). Perform deep breathing checks and inspect cabin airflow.`;
    } else if (vitals.heartRate > 100 || vitals.heartRate < 55) {
      status = 'WARNING';
      message = `WARNING: Heart rate outside resting baseline (${vitals.heartRate} BPM). Hydration and rest advised.`;
    } else if (vitals.bodyTemp > 37.5 || vitals.bodyTemp < 36.2) {
      status = 'WARNING';
      message = `WARNING: Body temperature shifted to ${vitals.bodyTemp} °C. Adjust suit thermal cooling.`;
    } else if (vitals.sleepDuration < 6.0) {
      status = 'WARNING';
      message = `WARNING: Sleep duration low (${vitals.sleepDuration} hrs). Schedule recovery rest window.`;
    }

    state.healthStatus = status;
    state.alertMessage = message;
    return { status, message };
  }

  // ================= Random Health Data Generator (Varies Over Time) =================
  let packetCounter = 120;

  function generateRandomVitals() {
    if (state.forcedAlertMode === 'CRITICAL_O2') {
      state.vitals.oxygenLevel = +(85.5 + Math.random() * 4.0).toFixed(1); // < 90%
      state.vitals.heartRate = Math.round(98 + Math.random() * 16);
      state.vitals.bodyTemp = +(37.1 + Math.random() * 0.4).toFixed(1);
    } else if (state.forcedAlertMode === 'WARNING_HR') {
      state.vitals.oxygenLevel = +(94.2 + Math.random() * 1.8).toFixed(1);
      state.vitals.heartRate = Math.round(108 + Math.random() * 12);
      state.vitals.bodyTemp = +(37.7 + Math.random() * 0.4).toFixed(1);
    } else {
      const hrDelta = (Math.random() - 0.48) * 5;
      const oxDelta = (Math.random() - 0.48) * 0.5;
      const tempDelta = (Math.random() - 0.5) * 0.14;
      const sleepDelta = (Math.random() - 0.5) * 0.1;

      state.vitals.heartRate = Math.round(Math.min(96, Math.max(62, state.vitals.heartRate + hrDelta)));
      state.vitals.oxygenLevel = +Math.min(99.8, Math.max(95.8, state.vitals.oxygenLevel + oxDelta)).toFixed(1);
      state.vitals.bodyTemp = +Math.min(37.4, Math.max(36.5, state.vitals.bodyTemp + tempDelta)).toFixed(1);
      state.vitals.sleepDuration = +Math.min(8.4, Math.max(6.8, state.vitals.sleepDuration + sleepDelta)).toFixed(1);
      state.vitals.exerciseTime = Math.min(150, state.vitals.exerciseTime + (Math.random() > 0.65 ? 1 : 0));
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
      sleepDuration: state.vitals.sleepDuration,
      exerciseTime: state.vitals.exerciseTime,
      status: state.healthStatus
    });

    if (state.history.length > state.maxHistory) {
      state.history.shift();
    }

    updateUI();
  }

  // ================= UI Render & Update =================
  function updateUI() {
    valHeartRate.textContent = state.vitals.heartRate;
    valOxygen.textContent = state.vitals.oxygenLevel.toFixed(1);
    valTemp.textContent = state.vitals.bodyTemp.toFixed(1);
    valSleep.textContent = state.vitals.sleepDuration.toFixed(1);
    valExercise.textContent = state.vitals.exerciseTime;

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

    updateTag(tagHeartRate, hrStatus);
    updateTag(tagOxygen, oxStatus);
    updateTag(tagTemp, tempStatus);
    updateTag(tagSleep, sleepStatus);
    updateTag(tagExercise, 'Normal');

    trendHeartRate.textContent = hrStatus === 'Normal' ? '● Normal' : `▲ ${hrStatus}`;
    trendHeartRate.className = 'stat-trend ' + (hrStatus === 'Critical' ? 'danger' : (hrStatus === 'Warning' ? 'warning' : 'positive'));

    trendOxygen.textContent = oxStatus === 'Normal' ? '● Optimal' : (oxStatus === 'Warning' ? '▼ Low O₂' : '✕ CRITICAL');
    trendOxygen.className = 'stat-trend ' + (oxStatus === 'Critical' ? 'danger' : (oxStatus === 'Warning' ? 'warning' : 'positive'));

    // Global Alert Banner
    globalAlertBanner.className = `alert-banner ${state.healthStatus.toLowerCase()}`;
    mainStatusBadge.className = `status-pill ${state.healthStatus.toLowerCase()}`;
    mainStatusBadge.textContent = `HEALTH STATUS: ${state.healthStatus}`;
    alertDescription.textContent = state.alertMessage;
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
        String(row.oxygenLevel).includes(query)
      );
    });

    if (rows.length === 0) {
      telemetryTableBody.innerHTML = `
        <tr>
          <td colspan="8" style="text-align:center; padding: 24px; color: var(--text-muted);">
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
    4: 'Scheduled blood pressure & cardiac checkup',
    9: 'Active Mission Day 42 • Routine vital monitoring & workout',
    13: 'Scheduled vision & fitness evaluation',
    16: 'Extravehicular spacewalk readiness check',
    20: 'Weekly sleep & hydration review',
    24: 'High-load resistance exercise assessment'
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
          calSelectedDesc.textContent = calendarEvents[day] || 'Standard daily biometric monitoring & exercise routine';
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
