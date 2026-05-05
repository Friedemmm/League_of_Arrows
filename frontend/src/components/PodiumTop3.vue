<template>
  <div class="podium-container" v-if="archers.length >= 3">

    <!-- ── Three banners: 2nd | 1st | 3rd ── -->
    <div class="podium-stage">
      <div
        v-for="slot in displaySlots"
        :key="slot.rank"
        :class="['banner', `banner-r${slot.rank}`]"
        :id="`podium-banner-${slot.rank}`"
      >
        <!-- Pointed bottom shape via clip-path on inner + drop-shadow glow on wrapper -->

        <!-- ① Rank badge ring (top of banner) -->
        <div class="rank-ring-wrap">
          <svg class="rank-ring-svg" viewBox="0 0 64 64" fill="none">
            <circle cx="32" cy="32" r="29" class="rr-outer" stroke-width="1.5"/>
            <line x1="32" y1="1"  x2="32" y2="11" class="rr-tick" stroke-width="1.5"/>
            <line x1="32" y1="53" x2="32" y2="63" class="rr-tick" stroke-width="1.5"/>
            <line x1="1"  y1="32" x2="11" y2="32" class="rr-tick" stroke-width="1.5"/>
            <line x1="53" y1="32" x2="63" y2="32" class="rr-tick" stroke-width="1.5"/>
            <circle cx="32" cy="32" r="20" class="rr-inner" stroke-width="1"/>
          </svg>
          <span class="rank-numeral">{{ slot.rank }}</span>
        </div>

        <!-- ② Ornamental avatar crest -->
        <div class="crest-wrap">
          <!-- Rank-differentiated SVG ring -->
          <svg class="crest-svg" viewBox="0 0 130 130" fill="none">
            <!-- outer thick ring -->
            <circle cx="65" cy="65" r="61" class="crest-outer" stroke-width="2.5"/>
            <!-- cardinal tick marks -->
            <line x1="65" y1="2"   x2="65" y2="16"  class="crest-tick" stroke-width="2"/>
            <line x1="65" y1="114" x2="65" y2="128" class="crest-tick" stroke-width="2"/>
            <line x1="2"  y1="65"  x2="16" y2="65"  class="crest-tick" stroke-width="2"/>
            <line x1="114" y1="65" x2="128" y2="65" class="crest-tick" stroke-width="2"/>
            <!-- diagonal corner dots -->
            <circle cx="21" cy="21" r="3" class="crest-dot"/>
            <circle cx="109" cy="21" r="3" class="crest-dot"/>
            <circle cx="21" cy="109" r="3" class="crest-dot"/>
            <circle cx="109" cy="109" r="3" class="crest-dot"/>
            <!-- inner double ring for 1st place -->
            <circle v-if="slot.rank === 1" cx="65" cy="65" r="50" class="crest-inner" stroke-width="1"/>
            <circle v-if="slot.rank === 1" cx="65" cy="65" r="45" class="crest-inner-dash" stroke-width="1" stroke-dasharray="4 5"/>
          </svg>

          <!-- Avatar portrait or initials -->
          <div class="avatar-circle">
            <img v-if="slot.archer.avatarUrl" :src="slot.archer.avatarUrl" :alt="slot.archer.archerName" class="avatar-img"/>
            <span v-else class="avatar-initials">{{ initials(slot.archer.archerName) }}</span>
          </div>
        </div>

        <!-- ③ Player info -->
        <div class="player-info">
          <p class="player-name">{{ slot.archer.archerName }}</p>
          <p class="player-score">{{ slot.archer.avgPointsPerArrow?.toFixed(2) }}</p>
          <p class="score-unit">PTS / FLECHA</p>
        </div>

        <!-- ④ Bottom chevron decoration -->
        <div class="banner-chevron" aria-hidden="true">
          <svg viewBox="0 0 60 20" fill="none">
            <polyline points="6,4 30,16 54,4"  class="chev" stroke-width="2"   stroke-linecap="round" stroke-linejoin="round"/>
            <polyline points="12,12 30,20 48,12" class="chev" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>

      </div><!-- /.banner -->
    </div><!-- /.podium-stage -->

    <!-- ── Midnight countdown — BELOW all three banners ── -->
    <div class="midnight-timer">
      <p class="timer-label">ACTUALIZA EN</p>
      <p class="timer-value">{{ countdownStr }}</p>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

/**
 * Props:
 *  archers – array ordered [0]=1st, [1]=2nd, [2]=3rd
 *  Each element: { archerName, avgPointsPerArrow, avatarUrl?, emblems? }
 *  emblems is an optional array of up to 3 icon URL strings.
 */
const props = defineProps({
  archers: { type: Array, required: true },
})

// Visual order: left=2nd | centre=1st | right=3rd
const displaySlots = computed(() => [
  { rank: 2, archer: props.archers[1] },
  { rank: 1, archer: props.archers[0] },
  { rank: 3, archer: props.archers[2] },
])

function initials(name = '') {
  return name.split(' ').map(w => w[0] ?? '').join('').slice(0, 2).toUpperCase() || '??'
}

// ── Midnight countdown ─────────────────────────────────────────────────
const countdownStr = ref('--h --m --s')

function calcMidnight() {
  const now = new Date()
  const midnight = new Date(now)
  midnight.setHours(24, 0, 0, 0)
  const diff = midnight - now
  if (diff <= 0) { countdownStr.value = '00H 00M 00S'; return }
  const h = Math.floor(diff / 3_600_000)
  const m = Math.floor((diff % 3_600_000) / 60_000)
  const s = Math.floor((diff % 60_000) / 1_000)
  countdownStr.value =
    `${String(h).padStart(2,'0')}H ${String(m).padStart(2,'0')}M ${String(s).padStart(2,'0')}S`
}

let _tick = null
onMounted(() => { calcMidnight(); _tick = setInterval(calcMidnight, 1_000) })
onUnmounted(() => clearInterval(_tick))
</script>

<style scoped>
/* ─── Rank-scoped CSS tokens set per banner ────────────────────────── */
.banner-r1 {
  --rc:      var(--gold-rich);
  --rc-glow: 0 0 22px rgba(200,155,60,0.55), 0 0 6px rgba(200,155,60,0.9);
  --rc-dim:  rgba(200,155,60,0.25);
}
.banner-r2 {
  --rc:      rgb(185,186,190);
  --rc-glow: 0 0 16px rgba(185,186,190,0.4), 0 0 4px rgba(185,186,190,0.7);
  --rc-dim:  rgba(185,186,190,0.2);
}
.banner-r3 {
  --rc:      rgb(165,108,48);
  --rc-glow: 0 0 16px rgba(165,108,48,0.4), 0 0 4px rgba(165,108,48,0.7);
  --rc-dim:  rgba(165,108,48,0.2);
}

/* ─── Stage layout ─────────────────────────────────────────────────── */
.podium-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  width: 100%;
  padding: 1.5rem 0 0;
}

.podium-stage {
  display: flex;
  align-items: flex-end;      /* pedestals share one bottom baseline */
  justify-content: center;
  gap: 0.9rem;
  width: 100%;
}

/* ─── Banner card ──────────────────────────────────────────────────── */
.banner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.55rem;
  padding: 1.1rem 0.9rem 0.6rem;
  background: linear-gradient(175deg, var(--bg-panel-alt) 0%, rgba(5,8,20,0.97) 100%);
  border: 1px solid var(--rc);
  box-shadow: var(--rc-glow), inset 0 0 28px var(--rc-dim);
  /* Pointed bottom via clip-path; generous bottom padding keeps content clear */
  clip-path: polygon(0 0, 100% 0, 100% 84%, 50% 100%, 0 84%);
  padding-bottom: 2.8rem;
  position: relative;
}

/* Staircase heights */
.banner-r1 { min-height: 360px; width: 175px; }
.banner-r2 { min-height: 298px; width: 150px; }
.banner-r3 { min-height: 278px; width: 142px; }

/* ─── ① Rank badge ring ────────────────────────────────────────────── */
.rank-ring-wrap {
  position: relative;
  width: 46px;
  height: 46px;
  flex-shrink: 0;
}
.banner-r1 .rank-ring-wrap { width: 52px; height: 52px; }

.rank-ring-svg { width: 100%; height: 100%; }

.rr-outer { stroke: var(--rc); }
.rr-inner { stroke: var(--border-subtle); stroke-dasharray: 3 4; }
.rr-tick  { stroke: var(--rc); }

.rank-numeral {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Cinzel', serif;
  font-size: 0.95rem;
  font-weight: 900;
  color: var(--rc);
  letter-spacing: 0.04em;
}
.banner-r1 .rank-numeral { font-size: 1.1rem; text-shadow: var(--glow-gold); }

/* ─── ② Ornamental avatar crest ────────────────────────────────────── */
.crest-wrap {
  position: relative;
  flex-shrink: 0;
}
.banner-r1 .crest-wrap { width: 108px; height: 108px; }
.banner-r2 .crest-wrap,
.banner-r3 .crest-wrap { width: 88px; height: 88px; }

.crest-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.crest-outer      { stroke: var(--rc); }
.crest-tick       { stroke: var(--rc); }
.crest-dot        { fill: var(--rc); }
.crest-inner      { stroke: var(--rc); opacity: 0.5; }
.crest-inner-dash { stroke: var(--rc); opacity: 0.3; }

/* Avatar content centred inside the crest ring */
.avatar-circle {
  position: absolute;
  inset: 16%;
  border-radius: 50%;
  background: var(--bg-panel);
  border: 2px solid var(--rc);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
.avatar-initials {
  font-family: 'Cinzel', serif;
  font-size: 1rem;
  font-weight: 800;
  color: var(--text-title);
}
.banner-r1 .avatar-initials { font-size: 1.2rem; text-shadow: var(--glow-gold); }

/* ─── ③ Player info — pushed to bottom with auto top margin ─────────── */
.player-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  margin-top: auto;          /* pushes the whole info block to the bottom */
  padding: 0 0.3rem;
  width: 100%;
}

.player-name {
  font-family: 'Cinzel', serif;
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--text-title);
  text-align: center;
  letter-spacing: 0.05em;
  /* Allow two-line wrapping for long names */
  white-space: normal;
  word-break: break-word;
  line-height: 1.3;
  min-height: 2.2em;         /* reserves height for two lines */
  margin: 0 0 0.4rem;
}
.banner-r1 .player-name { font-size: 0.88rem; }

.player-score {
  font-family: 'Cinzel', serif;
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--gold-mid);
  line-height: 1;
  margin: 0;
}
.banner-r1 .player-score {
  font-size: 1.65rem;
  color: var(--gold-pale);
  text-shadow: var(--glow-gold);
}

.score-unit {
  font-family: 'Inter', sans-serif;
  font-size: 0.5rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.12em;
  margin: 0.15rem 0 0;
}

/* ─── ⑤ Chevron decoration ──────────────────────────────────────────── */
.banner-chevron {
  width: 44px;
  margin-top: auto;
}
.banner-r1 .banner-chevron { width: 52px; }
.banner-chevron svg { width: 100%; height: auto; }
.chev { stroke: var(--rc); opacity: 0.65; }

/* ─── Midnight timer (below all banners) ────────────────────────────── */
.midnight-timer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.timer-label {
  font-family: 'Inter', sans-serif;
  font-size: 0.58rem;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  color: var(--text-muted);
  margin: 0;
}
.timer-value {
  font-family: 'Cinzel', serif;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--cyan);
  letter-spacing: 0.08em;
  text-shadow: var(--glow-cyan);
  margin: 0;
}

/* ─── Responsive ────────────────────────────────────────────────────── */
@media (max-width: 600px) {
  .podium-stage { gap: 0.4rem; }
  .banner-r1 { min-height: 290px; width: 136px; }
  .banner-r2 { min-height: 238px; width: 116px; }
  .banner-r3 { min-height: 222px; width: 110px; }
  .banner-r1 .crest-wrap { width: 84px; height: 84px; }
  .banner-r2 .crest-wrap,
  .banner-r3 .crest-wrap { width: 70px; height: 70px; }
  .banner-r1 .player-score { font-size: 1.25rem; }
  .player-score { font-size: 1rem; }
  .timer-value { font-size: 0.85rem; }
}
</style>
