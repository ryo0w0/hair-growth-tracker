// ===== Ruler Display Module =====
// Draws a real-scale ruler on canvas using screen PPI

function drawRuler() {
  const canvas = document.getElementById('ruler-canvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  const ppi = parseFloat(document.getElementById('screen-ppi').value) || 160;
  const ppm = ppi / 25.4; // pixels per mm

  // Determine ruler length: show up to estimated hair length + 20mm
  const stateRaw = localStorage.getItem('hairGrowthData');
  let lengthMm = 100; // default
  if (stateRaw) {
    try {
      const s = JSON.parse(stateRaw);
      // Use latest log or calculation
      if (s.logs && s.logs.length > 0) {
        const sorted = s.logs.sort((a, b) => new Date(b.date) - new Date(a.date));
        lengthMm = sorted[0].measuredMm;
      } else if (s.startLengthMm && s.startDate) {
        const days = Math.floor((new Date() - new Date(s.startDate)) / 86400000);
        lengthMm = s.startLengthMm + days * (s.dailyRateMm || 0.37);
      }
    } catch (e) {}
  }

  const rulerLengthMm = Math.max(50, lengthMm + 20);
  const W = rulerLengthMm * ppm;
  const H = 80;

  canvas.width = W;
  canvas.height = H;

  ctx.clearRect(0, 0, W, H);

  // Ruler body
  ctx.fillStyle = '#FFF9C4';
  ctx.fillRect(0, 0, W, H);
  ctx.strokeStyle = '#F9A825';
  ctx.lineWidth = 2;
  ctx.strokeRect(1, 1, W - 2, H - 2);

  // Draw ticks
  for (let mm = 0; mm <= rulerLengthMm; mm++) {
    const x = mm * ppm;
    const isCm = mm % 10 === 0;
    const is5mm = mm % 5 === 0;
    const tickH = isCm ? 36 : is5mm ? 24 : 14;

    ctx.beginPath();
    ctx.strokeStyle = isCm ? '#5D4037' : '#8D6E63';
    ctx.lineWidth = isCm ? 2 : 1;
    ctx.moveTo(x, H - 1);
    ctx.lineTo(x, H - 1 - tickH);
    ctx.stroke();

    if (isCm) {
      ctx.fillStyle = '#3E2723';
      ctx.font = 'bold 11px Noto Sans JP';
      ctx.textAlign = 'center';
      ctx.fillText(`${mm / 10}`, x, H - tickH - 6);
    }
  }

  // Hair length marker
  const markerX = lengthMm * ppm;
  ctx.beginPath();
  ctx.strokeStyle = '#E53935';
  ctx.lineWidth = 2;
  ctx.setLineDash([4, 3]);
  ctx.moveTo(markerX, 0);
  ctx.lineTo(markerX, H);
  ctx.stroke();
  ctx.setLineDash([]);

  ctx.fillStyle = '#E53935';
  ctx.font = 'bold 10px Noto Sans JP';
  ctx.textAlign = 'left';
  ctx.fillText(`📏 ${lengthMm.toFixed(1)}mm`, markerX + 4, 14);

  // cm label
  ctx.fillStyle = '#6750A4';
  ctx.font = '10px Noto Sans JP';
  ctx.textAlign = 'right';
  ctx.fillText('cm', W - 4, H - 40);
}

// Auto-detect PPI attempt
document.addEventListener('DOMContentLoaded', () => {
  const detectBtn = document.getElementById('detect-ppi');
  if (detectBtn) {
    detectBtn.addEventListener('click', () => {
      // Use devicePixelRatio as hint; a rough baseline
      const dpr = window.devicePixelRatio || 1;
      const estimatedPpi = Math.round(96 * dpr);
      document.getElementById('screen-ppi').value = estimatedPpi;
      alert(`推定PPI: ${estimatedPpi}\n(実際の値はデバイスの仕様書で確認してください)`);
      drawRuler();
    });
  }
});

window.drawRuler = drawRuler;
