/**
 * VitalHealth — Astronaut Health Monitoring Software
 * Autonomous Space Mission Telemetry System
 * Pure HTML, CSS, JavaScript
 */

(function () {
  'use strict';

  // ================= State Management =================
  const state = {
    theme: localStorage.getItem('vitalhealth_theme') || 'light',
    isLive: true,
    updateIntervalMs: 3000,
    intervalId: null,
    missionDay: 42,
    missionStartTime: Date.now() - (42 * 86400000 + 14 * 3600000 + 32 * 60000), // Day 42, 14:32:00 MET baseline
    
    // Current Astronaut Telemetry (STEP 1 & STEP 2)
    vitals: {
      heartRate: 74,       // BPM (normal: 60 - 100)
      oxygenLevel: 98.4,   // % SpO2 (normal: 95 - 100, < 90 is CRITICAL ALERT)
      bodyTemp: 36.8,      // °C (normal: 36.5 - 37.5)
      sleepDuration: 7.6,  // hours (normal: 7.0 - 8.5)
      exerciseTime: 145    // min (target: 150 min/day on ARED)
    },

    // Step 3 & Step 4: Health Status & Alerts
    healthStatus: 'NORMAL', // 'NORMAL' | 'WARNING' | 'CRITICAL'
    alertMessage: 'All vital signs are within nominal spaceflight boundaries. No microgravity decompensation detected.',
    alertCount: 0,
    forcedAlertMode: null, // null | 'CRITICAL_O2' | 'WARNING_HR'

    // History for chart and table
    history: [],
    maxHistory: 12
  };

  // Seed initial history
  const timeLabels = ['10:00', '10:30', '11:00', '11:30', '12:00', '12:30', '01:00', '01:30', '02:00', '02:30', '03:00', '03:30'];
  timeLabels.forEach((lbl, i) => {
    state.history.push({
      time: lbl,
      heartRate: Math.round(70 + Math.sin(i * 0.8) * 6 + (Math.random() * 4 - 2)),
      oxygenLevel: +(98.0 + Math.cos(i * 0.5) * 0.8 + (Math.random() * 0.4 - 0.2)).toFixed(1),
      bodyTemp: +(36.7 + Math.random() * 0.3).toFixed(1),
      sleepDuration: 7.6,
      exerciseTime: 120 + i * 2,
      status: 'NORMAL'
    });
  });

  // ================= DOM Elements =================
  const htmlRoot = document.documentElement;
  const themeToggleBtn = document.getElementById('themeToggleBtn');
  const missionTimeDisplay = document.getElementById('missionTimeDisplay');
  const liveToggleBtn = document.getElementById('liveToggleBtn');
  const liveToggleText = document.getElementById('liveToggleText');
  const manualSampleBtn = document.getElementById('manualSampleBtn');
  const simCriticalBtn = document.getElementById('simCriticalBtn');
  const simWarningBtn = document.getElementById('simWarningBtn');
  const simNormalBtn = document.getElementById('simNormalBtn');
  const resetBtn = document.getElementById('resetBtn');
  const alertBadge = document.getElementById('alertBadge');

  // Cards
  const valHeartRate = document.getElementById('valHeartRate');
  const tagHeartRate = document.getElementById('tagHeartRate');
  const valOxygen = document.getElementById('valOxygen');
  const tagOxygen = document.getElementById('tagOxygen');
  const valTemp = document.getElementById('valTemp');
  const tagTemp = document.getElementById('tagTemp');
  const valSleep = document.getElementById('valSleep');
  const tagSleep = document.getElementById('tagSleep');
  const valExercise = document.getElementById('valExercise');
  const tagExercise = document.getElementById('tagExercise');

  // Alert Banner
  const globalAlertBanner = document.getElementById('globalAlertBanner');
  const mainStatusBadge = document.getElementById('mainStatusBadge');
  const alertDescription = document.getElementById('alertDescription');
  const alertTimestamp = document.getElementById('alertTimestamp');
  const alertIconBox = document.getElementById('alertIconBox');

  // Canvases
  const mainTelemetryChart = document.getElementById('mainTelemetryChart');
  const sparkHeartRate = document.getElementById('sparkHeartRate');
  const sparkOxygen = document.getElementById('sparkOxygen');
  const sparkTemp = document.getElementById('sparkTemp');

  // Table
  const telemetryTableBody = document.getElementById('telemetryTableBody');

  // Mobile sidebar
  const mobileToggleBtn = document.getElementById('mobileToggleBtn');
  const mobileCloseBtn = document.getElementById('mobileCloseBtn');
  const sidebar = document.getElementById('sidebar');

  // ================= Theme Management =================
  function initTheme() {
    htmlRoot.setAttribute('data-theme', state.theme);
  }

  function toggleTheme() {
    state.theme = state.theme === 'light' ? 'dark' : 'light';
    htmlRoot.setAttribute('data-theme', state.theme);
    localStorage.setItem('vitalhealth_theme', state.theme);
    renderMainChart();
    renderSparklines();
  }

  themeToggleBtn.addEventListener('click', toggleTheme);

  // Mobile drawer
  if (mobileToggleBtn && sidebar) {
    mobileToggleBtn.addEventListener('click', () => sidebar.classList.add('open'));
  }
  if (mobileCloseBtn && sidebar) {
    mobileCloseBtn.addEventListener('click', () => sidebar.classList.remove('open'));
  }

  // ================= Step 5: Mission Day & Live Time Indicator =================
  function updateMissionTime() {
    const elapsed = Date.now() - state.missionStartTime;
    const totalSeconds = Math.floor(elapsed / 1000);
    const days = Math.floor(totalSeconds / 86400);
    const hours = Math.floor((totalSeconds % 86400) / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    const pad = (n) => String(n).padStart(2, '0');
    missionTimeDisplay.textContent = `MET DAY ${days} • ${pad(hours)}:${pad(minutes)}:${pad(seconds)} UTC`;
  }
  setInterval(updateMissionTime, 1000);
  updateMissionTime();

  // ================= STEP 3 & STEP 4: Health Status & Alert Condition =================
  /**
   * Evaluates the health status and alert conditions based on health parameters.
   * Example requirement: If oxygen level < 90% -> CRITICAL ALERT
   */
  function evaluateHealthStatus(vitals) {
    let status = 'NORMAL';
    let message = 'All vital signs are within nominal spaceflight boundaries. No microgravity decompensation detected.';

    // Condition 1 (CRITICAL ALERT - Explicit requirement from brief): Oxygen level < 90%
    if (vitals.oxygenLevel < 90.0) {
      status = 'CRITICAL';
      message = `CRITICAL ALERT: Severe Hypoxia Detected! Arterial Oxygen Saturation is ${vitals.oxygenLevel}% (< 90%). Deploy Emergency O₂ Mask Immediately & Verify Cabin PPO₂!`;
    }
    // Condition 2 (CRITICAL): Extreme Tachycardia
    else if (vitals.heartRate > 125 || vitals.heartRate < 45) {
      status = 'CRITICAL';
      message = `CRITICAL ALERT: Acute Cardiac Event! Heart Rate is ${vitals.heartRate} BPM. Initiate cardiac telemetry stabilization protocol.`;
    }
    // Condition 3 (WARNING): Oxygen mild drop (90 - 94.5%)
    else if (vitals.oxygenLevel < 94.5) {
      status = 'WARNING';
      message = `WARNING: Sub-optimal Oxygenation (SpO₂: ${vitals.oxygenLevel}%). Astronaut should perform deep ventilatory cycling and check mask seal.`;
    }
    // Condition 4 (WARNING): Tachycardia / Elevated Heart Rate
    else if (vitals.heartRate > 100 || vitals.heartRate < 55) {
      status = 'WARNING';
      message = `WARNING: Elevated Heart Rate (${vitals.heartRate} BPM). Rest protocol and hydration recommended.`;
    }
    // Condition 5 (WARNING): Core Temperature Elevation
    else if (vitals.bodyTemp > 37.8 || vitals.bodyTemp < 36.0) {
      status = 'WARNING';
      message = `WARNING: Core Body Temperature Drift (${vitals.bodyTemp} °C). Active thermal regulation garment adjustment recommended.`;
    }

    state.healthStatus = status;
    state.alertMessage = message;
    return { status, message };
  }

  // ================= Random Health Data Generator (Varying Over Time) =================
  function generateRandomVitals() {
    if (state.forcedAlertMode === 'CRITICAL_O2') {
      // Oxygen < 90% for testing critical alert
      state.vitals.oxygenLevel = +(86.0 + Math.random() * 3.5).toFixed(1); // 86.0 - 89.5%
      state.vitals.heartRate = Math.round(98 + Math.random() * 18);
      state.vitals.bodyTemp = +(37.1 + Math.random() * 0.3).toFixed(1);
    } else if (state.forcedAlertMode === 'WARNING_HR') {
      state.vitals.oxygenLevel = +(95.0 + Math.random() * 1.5).toFixed(1);
      state.vitals.heartRate = Math.round(112 + Math.random() * 10); // Warning tachycardia
      state.vitals.bodyTemp = +(37.9 + Math.random() * 0.4).toFixed(1); // Space fever
    } else {
      // Normal nominal flight variation
      const hrDelta = (Math.random() - 0.48) * 4;
      const oxDelta = (Math.random() - 0.48) * 0.4;
      const tempDelta = (Math.random() - 0.5) * 0.1;

      state.vitals.heartRate = Math.round(Math.min(96, Math.max(62, state.vitals.heartRate + hrDelta)));
      state.vitals.oxygenLevel = +Math.min(99.8, Math.max(96.0, state.vitals.oxygenLevel + oxDelta)).toFixed(1);
      state.vitals.bodyTemp = +Math.min(37.3, Math.max(36.5, state.vitals.bodyTemp + tempDelta)).toFixed(1);
      
      // Exercise time slowly increments
      state.vitals.exerciseTime = Math.min(150, state.vitals.exerciseTime + (Math.random() > 0.7 ? 1 : 0));
    }

    // Evaluate
    evaluateHealthStatus(state.vitals);

    // Record to history
    const now = new Date();
    const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
    state.history.push({
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
    // 1. Update parameter cards
    valHeartRate.textContent = state.vitals.heartRate;
    valOxygen.textContent = state.vitals.oxygenLevel;
    valTemp.textContent = state.vitals.bodyTemp;
    valSleep.textContent = state.vitals.sleepDuration;
    valExercise.textContent = state.vitals.exerciseTime;

    // Status Tags
    updateTag(tagHeartRate, state.vitals.heartRate > 100 ? (state.vitals.heartRate > 125 ? 'Critical' : 'Warning') : 'Normal');
    updateTag(tagOxygen, state.vitals.oxygenLevel < 90 ? 'Critical Alert' : (state.vitals.oxygenLevel < 95 ? 'Warning' : 'Normal'));
    updateTag(tagTemp, state.vitals.bodyTemp > 37.8 ? 'Warning' : 'Normal');
    updateTag(tagSleep, 'Normal');
    updateTag(tagExercise, 'Normal');

    // 2. Update Global Alert Banner (Step 3 & Step 4)
    globalAlertBanner.className = `alert-banner ${state.healthStatus.toLowerCase()}`;
    mainStatusBadge.className = `status-pill ${state.healthStatus.toLowerCase()}`;
    mainStatusBadge.textContent = `HEALTH STATUS: ${state.healthStatus}`;
    alertDescription.textContent = state.alertMessage;
    alertTimestamp.textContent = `Telemetry Updated ${new Date().toLocaleTimeString()} • Automatic Sentinel Active`;

    // Icon in alert box
    if (state.healthStatus === 'CRITICAL') {
      alertIconBox.innerHTML = `
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="8" x2="12" y2="12"></line>
          <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>
      `;
      alertBadge.textContent = '!';
      alertBadge.style.display = 'flex';
    } else if (state.healthStatus === 'WARNING') {
      alertIconBox.innerHTML = `
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
          <line x1="12" y1="9" x2="12" y2="13"></line>
          <line x1="12" y1="17" x2="12.01" y2="17"></line>
        </svg>
      `;
      alertBadge.textContent = '1';
      alertBadge.style.display = 'flex';
    } else {
      alertIconBox.innerHTML = `
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </svg>
      `;
      alertBadge.style.display = 'none';
    }

    // 3. Render charts and sparklines
    renderMainChart();
    renderSparklines();

    // 4. Update Table
    renderTable();
  }

  function updateTag(element, text) {
    element.textContent = text;
    element.className = 'status-tag ' + (text.toLowerCase().includes('critical') ? 'critical' : (text.toLowerCase().includes('warning') ? 'warning' : 'normal'));
  }

  // ================= Canvas Chart: Main Telemetry Spline Waveform =================
  function renderMainChart() {
    if (!mainTelemetryChart) return;
    const ctx = mainTelemetryChart.getContext('2d');
    const width = mainTelemetryChart.width;
    const height = mainTelemetryChart.height;
    const isDark = state.theme === 'dark';

    // Clear
    ctx.clearRect(0, 0, width, height);

    const padLeft = 40;
    const padRight = 20;
    const padTop = 20;
    const padBottom = 30;
    const plotW = width - padLeft - padRight;
    const plotH = height - padTop - padBottom;

    // Draw horizontal grid lines
    ctx.strokeStyle = isDark ? '#1e2b48' : '#e5e9f2';
    ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i++) {
      const y = padTop + (plotH / 4) * i;
      ctx.beginPath();
      ctx.moveTo(padLeft, y);
      ctx.lineTo(width - padRight, y);
      ctx.stroke();

      // Axis labels
      ctx.fillStyle = isDark ? '#64748b' : '#94a3b8';
      ctx.font = '10px JetBrains Mono';
      ctx.textAlign = 'right';
      const labelVal = Math.round(100 - i * 15);
      ctx.fillText(labelVal, padLeft - 8, y + 3);
    }

    const data = state.history;
    if (data.length < 2) return;

    // Normalize Heart Rate (50 - 130 bpm -> plotH)
    const getHrY = (val) => padTop + plotH - ((val - 50) / 80) * plotH;
    // Normalize Oxygen (85 - 100 % -> plotH)
    const getOxY = (val) => padTop + plotH - ((val - 85) / 15) * plotH;
    const getX = (idx) => padLeft + (idx / (data.length - 1)) * plotW;

    // 1. Draw Oxygen Area & Line (Purple #6366f1)
    ctx.beginPath();
    ctx.moveTo(getX(0), height - padBottom);
    for (let i = 0; i < data.length; i++) {
      const x = getX(i);
      const y = getOxY(data[i].oxygenLevel);
      if (i === 0) ctx.lineTo(x, y);
      else {
        const prevX = getX(i - 1);
        const prevY = getOxY(data[i - 1].oxygenLevel);
        const cpX = (prevX + x) / 2;
        ctx.bezierCurveTo(cpX, prevY, cpX, y, x, y);
      }
    }
    ctx.lineTo(getX(data.length - 1), height - padBottom);
    ctx.closePath();
    const oxGrad = ctx.createLinearGradient(0, padTop, 0, height - padBottom);
    oxGrad.addColorStop(0, 'rgba(99, 102, 241, 0.25)');
    oxGrad.addColorStop(1, 'rgba(99, 102, 241, 0.0)');
    ctx.fillStyle = oxGrad;
    ctx.fill();

    // Oxygen Stroke
    ctx.beginPath();
    for (let i = 0; i < data.length; i++) {
      const x = getX(i);
      const y = getOxY(data[i].oxygenLevel);
      if (i === 0) ctx.moveTo(x, y);
      else {
        const prevX = getX(i - 1);
        const prevY = getOxY(data[i - 1].oxygenLevel);
        const cpX = (prevX + x) / 2;
        ctx.bezierCurveTo(cpX, prevY, cpX, y, x, y);
      }
    }
    ctx.strokeStyle = '#6366f1';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    // 2. Draw Heart Rate Line (Cyan #06b6d4)
    ctx.beginPath();
    for (let i = 0; i < data.length; i++) {
      const x = getX(i);
      const y = getHrY(data[i].heartRate);
      if (i === 0) ctx.moveTo(x, y);
      else {
        const prevX = getX(i - 1);
        const prevY = getHrY(data[i - 1].heartRate);
        const cpX = (prevX + x) / 2;
        ctx.bezierCurveTo(cpX, prevY, cpX, y, x, y);
      }
    }
    ctx.strokeStyle = '#06b6d4';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    // Draw End Dots
    const lastIdx = data.length - 1;
    // Heart dot
    ctx.beginPath();
    ctx.arc(getX(lastIdx), getHrY(data[lastIdx].heartRate), 4, 0, Math.PI * 2);
    ctx.fillStyle = '#06b6d4';
    ctx.fill();
    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 2;
    ctx.stroke();

    // Oxygen dot
    ctx.beginPath();
    ctx.arc(getX(lastIdx), getOxY(data[lastIdx].oxygenLevel), 4, 0, Math.PI * 2);
    ctx.fillStyle = '#6366f1';
    ctx.fill();
    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 2;
    ctx.stroke();
  }

  // ================= Sparklines for Stat Cards =================
  function renderSparklines() {
    drawMiniSparkline(sparkHeartRate, state.history.map(d => d.heartRate), '#06b6d4');
    drawMiniSparkline(sparkOxygen, state.history.map(d => d.oxygenLevel), '#6366f1');
    drawMiniSparkline(sparkTemp, state.history.map(d => d.bodyTemp), '#f59e0b');
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

  // ================= Table Rendering =================
  function renderTable() {
    if (!telemetryTableBody) return;
    const reversed = [...state.history].reverse().slice(0, 6);

    telemetryTableBody.innerHTML = reversed.map((row, idx) => {
      const isCrit = row.oxygenLevel < 90 || row.heartRate > 125;
      const isWarn = !isCrit && (row.oxygenLevel < 95 || row.heartRate > 100 || row.bodyTemp > 37.8);
      const st = isCrit ? 'critical' : (isWarn ? 'warning' : 'normal');
      const stLabel = isCrit ? 'Critical' : (isWarn ? 'Warning' : 'Normal');

      return `
        <tr>
          <td><input type="checkbox" aria-label="Row select"></td>
          <td><strong style="font-family: var(--font-mono); font-size: 0.78rem;">#MET-${state.missionDay}-${100 + idx}</strong></td>
          <td>${row.time} UTC</td>
          <td><strong>${row.heartRate}</strong> BPM</td>
          <td style="${row.oxygenLevel < 90 ? 'color: #ef4444; font-weight: 800;' : ''}">${row.oxygenLevel}%</td>
          <td>${row.bodyTemp}°C</td>
          <td>${row.sleepDuration}h</td>
          <td>${row.exerciseTime}m</td>
          <td>
            <span class="badge-status ${st}">● ${stLabel}</span>
          </td>
          <td>
            <button class="action-row-btn" title="Inspect Telemetry">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </td>
        </tr>
      `;
    }).join('');
  }

  // ================= Calendar Generator =================
  function initCalendar() {
    const grid = document.getElementById('calDaysGrid');
    if (!grid) return;

    // July 2026: Day 1 starts on Wednesday (offset 3)
    const daysInMonth = 31;
    const startOffset = 3; 
    let html = '';

    // Prev month padding
    for (let i = 0; i < startOffset; i++) {
      html += `<div class="cal-day other-month">${28 + i}</div>`;
    }

    // Days
    for (let d = 1; d <= daysInMonth; d++) {
      const isToday = d === 13; // Highlighted day like reference image
      const hasAppt = d === 4 || d === 13 || d === 20;
      const hasMeeting = d === 8 || d === 16;
      const hasSurgery = d === 24 || d === 30;

      let dotHtml = '';
      if (hasAppt) dotHtml += '<span class="cal-dot-indicator appt"></span>';
      if (hasMeeting) dotHtml += '<span class="cal-dot-indicator meeting"></span>';
      if (hasSurgery) dotHtml += '<span class="cal-dot-indicator surgery"></span>';

      html += `
        <div class="cal-day ${isToday ? 'today active-mission' : ''}">
          ${d}
          ${dotHtml}
        </div>
      `;
    }

    grid.innerHTML = html;
  }

  // ================= Event Listeners & Simulator Triggers =================
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

  manualSampleBtn.addEventListener('click', () => {
    generateRandomVitals();
  });

  // STEP 4 SIMULATOR: Critical Alert Trigger (Oxygen < 90%)
  simCriticalBtn.addEventListener('click', () => {
    state.forcedAlertMode = 'CRITICAL_O2';
    generateRandomVitals();
  });

  // STEP 3 & 4: Warning Alert Trigger
  simWarningBtn.addEventListener('click', () => {
    state.forcedAlertMode = 'WARNING_HR';
    generateRandomVitals();
  });

  // Reset to Normal
  simNormalBtn.addEventListener('click', () => {
    state.forcedAlertMode = null;
    state.vitals.heartRate = 72;
    state.vitals.oxygenLevel = 98.6;
    state.vitals.bodyTemp = 36.8;
    generateRandomVitals();
  });

  resetBtn.addEventListener('click', (e) => {
    e.preventDefault();
    state.forcedAlertMode = null;
    state.vitals.heartRate = 74;
    state.vitals.oxygenLevel = 98.4;
    state.vitals.bodyTemp = 36.8;
    generateRandomVitals();
  });

  // ================= Initialization =================
  initTheme();
  initCalendar();
  evaluateHealthStatus(state.vitals);
  updateUI();
  startLiveLoop();

})();
