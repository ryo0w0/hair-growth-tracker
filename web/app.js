// ===== Hair Growth Tracker - Main App Logic =====

const DEFAULT_DAILY_RATE_MM = 0.37; // mm per day
const STORAGE_KEY = 'hairGrowthData';

// --- State ---
let state = {
  startDate: null,
  startLengthMm: 0,
  goalLengthMm: null,
  dailyRateMm: DEFAULT_DAILY_RATE_MM,
  reminderDay: 1,
  logs: [], // [{date: 'YYYY-MM-DD', measuredMm: float}]
};

// --- Persistence ---
function saveState() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function loadState() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (raw) {
    try {
      const parsed = JSON.parse(raw);
      state = { ...state, ...parsed };
    } catch (e) {
      console.error('State parse error', e);
    }
  }
}

// --- Calculations ---
function getDaysSinceStart() {
  if (!state.startDate) return 0;
  const start = new Date(state.startDate);
  const now = new Date();
  const diff = now - start;
  return Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)));
}

function getEstimatedLengthMm() {
  const days = getDaysSinceStart();
  // Use effective rate: from latest log if available
  let effectiveRate = state.dailyRateMm;
  if (state.logs.length >= 2) {
    const sorted = [...state.logs].sort((a, b) => new Date(a.date) - new Date(b.date));
    const first = sorted[0];
    const last = sorted[sorted.length - 1];
    const daysBetween = (new Date(last.date) - new Date(first.date)) / (1000 * 60 * 60 * 24);
    if (daysBetween > 0) {
      effectiveRate = (last.measuredMm - first.measuredMm) / daysBetween;
      if (effectiveRate > 0) state.dailyRateMm = effectiveRate;
    }
  }

  // Use latest measured as base if available
  if (state.logs.length > 0) {
    const sorted = [...state.logs].sort((a, b) => new Date(a.date) - new Date(b.date));
    const latest = sorted[sorted.length - 1];
    const latestDate = new Date(latest.date);
    const today = new Date();
    const daysSinceLatest = Math.max(0, Math.floor((today - latestDate) / (1000 * 60 * 60 * 24)));
    return latest.measuredMm + (daysSinceLatest * effectiveRate);
  }

  return state.startLengthMm + (days * state.dailyRateMm);
}

function getDaysToGoal() {
  if (!state.goalLengthMm) return null;
  const currentMm = getEstimatedLengthMm();
  if (currentMm >= state.goalLengthMm) return 0;
  return Math.ceil((state.goalLengthMm - currentMm) / state.dailyRateMm);
}

function getProgressPercent() {
  if (!state.goalLengthMm || !state.startLengthMm) return 0;
  const current = getEstimatedLengthMm();
  const total = state.goalLengthMm - state.startLengthMm;
  if (total <= 0) return 100;
  return Math.min(100, Math.max(0, ((current - state.startLengthMm) / total) * 100));
}

function shouldShowReminder() {
  const today = new Date();
  if (today.getDate() !== state.reminderDay) return false;
  const todayStr = today.toISOString().split('T')[0];
  const alreadyLoggedToday = state.logs.some(l => l.date === todayStr);
  return !alreadyLoggedToday;
}

// --- UI Updates ---
function updateDashboard() {
  const days = getDaysSinceStart();
  const lengthMm = getEstimatedLengthMm();
  const lengthCm = lengthMm / 10;
  const daysToGoal = getDaysToGoal();
  const pct = getProgressPercent();
  const monthlyMm = (state.dailyRateMm * 30.44).toFixed(1);

  document.getElementById('days-count').textContent = days;
  document.getElementById('current-length').textContent = lengthMm.toFixed(1);
  document.getElementById('current-length-cm').textContent = `= ${lengthCm.toFixed(1)} cm`;
  document.getElementById('today-growth').textContent = `+${state.dailyRateMm.toFixed(2)}`;
  document.getElementById('monthly-growth').textContent = `+${monthlyMm}`;

  if (daysToGoal !== null) {
    document.getElementById('days-to-goal').textContent = daysToGoal === 0 ? '🎉達成!' : daysToGoal;
    document.getElementById('goal-unit').textContent = daysToGoal === 0 ? '' : '日後';
  } else {
    document.getElementById('days-to-goal').textContent = '—';
    document.getElementById('goal-unit').textContent = '未設定';
  }

  document.getElementById('progress-bar').style.width = `${pct.toFixed(1)}%`;
  document.getElementById('progress-pct').textContent = `${pct.toFixed(0)}%`;
  document.getElementById('progress-start').textContent = `${state.startLengthMm}mm`;
  document.getElementById('progress-goal').textContent = state.goalLengthMm ? `${state.goalLengthMm}mm` : '—';

  const banner = document.getElementById('reminder-banner');
  banner.style.display = shouldShowReminder() ? 'block' : 'none';
}

function updateSettings() {
  if (state.startDate) document.getElementById('start-date').value = state.startDate;
  document.getElementById('start-length').value = state.startLengthMm || '';
  document.getElementById('goal-length').value = state.goalLengthMm || '';
  document.getElementById('daily-rate').value = state.dailyRateMm;
  document.getElementById('reminder-day').value = state.reminderDay;
}

function updateHistory() {
  const list = document.getElementById('history-list');
  if (state.logs.length === 0) {
    list.innerHTML = '<div style="color:#888;text-align:center;padding:16px;">記録がありません</div>';
    return;
  }
  const sorted = [...state.logs].sort((a, b) => new Date(b.date) - new Date(a.date));
  list.innerHTML = sorted.map(l => `
    <div class="history-item">
      <span class="hi-date">${l.date}</span>
      <span class="hi-val">${l.measuredMm.toFixed(1)} mm (${(l.measuredMm/10).toFixed(1)} cm)</span>
    </div>
  `).join('');

  drawGrowthChart();
}

function drawGrowthChart() {
  const canvas = document.getElementById('growth-chart');
  const ctx = canvas.getContext('2d');
  const sorted = [...state.logs].sort((a, b) => new Date(a.date) - new Date(b.date));
  if (sorted.length < 1) return;

  const W = canvas.width, H = canvas.height;
  const PAD = 30;
  ctx.clearRect(0, 0, W, H);

  const values = sorted.map(l => l.measuredMm);
  const minV = Math.min(...values) * 0.9;
  const maxV = Math.max(...values) * 1.1;
  const range = maxV - minV || 1;

  const toX = (i) => PAD + (i / Math.max(sorted.length - 1, 1)) * (W - PAD * 2);
  const toY = (v) => PAD + (1 - (v - minV) / range) * (H - PAD * 2);

  // Draw goal line
  if (state.goalLengthMm) {
    const gy = toY(state.goalLengthMm);
    if (gy >= PAD && gy <= H - PAD) {
      ctx.beginPath();
      ctx.strokeStyle = '#E53935';
      ctx.setLineDash([6, 4]);
      ctx.moveTo(PAD, gy);
      ctx.lineTo(W - PAD, gy);
      ctx.stroke();
      ctx.setLineDash([]);
      ctx.fillStyle = '#E53935';
      ctx.font = '10px Noto Sans JP';
      ctx.fillText('目標', W - PAD - 24, gy - 4);
    }
  }

  // Draw line
  ctx.beginPath();
  ctx.strokeStyle = '#6750A4';
  ctx.lineWidth = 2;
  sorted.forEach((l, i) => {
    const x = toX(i), y = toY(l.measuredMm);
    i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
  });
  ctx.stroke();

  // Draw dots
  sorted.forEach((l, i) => {
    const x = toX(i), y = toY(l.measuredMm);
    ctx.beginPath();
    ctx.arc(x, y, 4, 0, Math.PI * 2);
    ctx.fillStyle = '#6750A4';
    ctx.fill();
  });

  // Axis labels
  ctx.fillStyle = '#49454F';
  ctx.font = '9px Noto Sans JP';
  if (sorted.length > 0) {
    ctx.fillText(sorted[0].date.slice(5), PAD, H - 4);
    if (sorted.length > 1) {
      const lx = toX(sorted.length - 1);
      ctx.fillText(sorted[sorted.length-1].date.slice(5), lx - 20, H - 4);
    }
  }
}

// --- Event Handlers ---
function setupTabs() {
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById(`tab-${btn.dataset.tab}`).classList.add('active');
      if (btn.dataset.tab === 'history') updateHistory();
      if (btn.dataset.tab === 'ruler') window.drawRuler && window.drawRuler();
    });
  });
}

function setupSettings() {
  document.getElementById('save-settings').addEventListener('click', () => {
    const startDateVal = document.getElementById('start-date').value;
    const startLen = parseFloat(document.getElementById('start-length').value);
    const goalLen = parseFloat(document.getElementById('goal-length').value);
    const dailyRate = parseFloat(document.getElementById('daily-rate').value);
    const reminderDay = parseInt(document.getElementById('reminder-day').value);

    if (startDateVal) state.startDate = startDateVal;
    if (!isNaN(startLen) && startLen >= 0) state.startLengthMm = startLen;
    if (!isNaN(goalLen) && goalLen > 0) state.goalLengthMm = goalLen;
    if (!isNaN(dailyRate) && dailyRate > 0) state.dailyRateMm = dailyRate;
    if (!isNaN(reminderDay) && reminderDay >= 1 && reminderDay <= 28) state.reminderDay = reminderDay;

    saveState();
    updateDashboard();
    alert('✅ 設定を保存しました！');
  });
}

function setupLogForm() {
  document.getElementById('log-btn').addEventListener('click', () => {
    const val = parseFloat(document.getElementById('measure-input').value);
    if (isNaN(val) || val <= 0) {
      alert('正しい計測値を入力してください (mm)');
      return;
    }
    const today = new Date().toISOString().split('T')[0];
    // Remove existing today log
    state.logs = state.logs.filter(l => l.date !== today);
    state.logs.push({ date: today, measuredMm: val });
    saveState();
    document.getElementById('measure-input').value = '';
    updateDashboard();
    updateHistory();
    alert(`✅ ${val}mm を記録しました！`);
  });
}

// --- Init ---
function init() {
  loadState();
  setupTabs();
  setupSettings();
  setupLogForm();
  updateSettings();
  updateDashboard();

  // Auto refresh every minute
  setInterval(updateDashboard, 60 * 1000);
}

document.addEventListener('DOMContentLoaded', init);
