<template>
  <div class="page-wrapper leaderboard-page">
    <div class="container">

      <!-- ══ Section header ══ -->
      <div class="section-header lb-header">
        <h1 class="section-title">Leaderboard</h1>

        <!-- Tabs only in header (no countdown here) -->
        <div class="section-meta-row lb-meta">
          <div class="lb-tabs">
            <button
              id="tab-daily"
              class="lb-tab"
              :class="{ active: tab === 'daily' }"
              @click="tab = 'daily'"
            >
              Top 50 Promedio Diario
            </button>
            <button
              id="tab-monthly"
              class="lb-tab"
              :class="{ active: tab === 'monthly' }"
              @click="tab = 'monthly'"
            >
              Top 10 Mejores del Mes
            </button>
          </div>
        </div>

        <hr class="header-rule lb-rule" />
      </div>

      <!-- ══ Loading ══ -->
      <div v-if="loading" class="loading-center">
        <div class="spinner"></div>
      </div>

      <template v-else>

        <!-- ══ DAILY TAB — materialized view top 50 ══ -->
        <template v-if="tab === 'daily'">

          <!-- Countdown: only on the historical/daily tab -->
          <div class="daily-info-bar">
            <p class="daily-description">
              TOP 50 ARQUEROS POR PROMEDIO DE PUNTOS POR FLECHA
            </p>
            <div class="lb-countdown" title="Próxima actualización de la vista materializada">
              
              <span class="countdown-value">{{ countdownStr }}</span>
            </div>
          </div>

          <div class="lol-table-wrapper">
            <table class="lol-table" id="leaderboard-daily-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Arquero</th>
                  <th>Torneos</th>
                  <th>Flechas</th>
                  <th>Total Pts</th>
                  <th>Avg Pts/Flecha</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="a in daily" :key="a.archerId" :id="`daily-row-${a.archerId}`">
                  <td>
                    <span class="rank-num" :class="a.globalPosition <= 3 ? `rank-${a.globalPosition}` : ''">
                      {{ a.globalPosition }}
                    </span>
                  </td>
                  <td class="archer-name-cell">{{ a.name }}</td>
                  <td class="text-muted">{{ a.tournamentsPlayed }}</td>
                  <td class="text-muted">{{ a.arrowsFired }}</td>
                  <td>{{ a.totalScore }}</td>
                  <td class="text-gold-bright avg-cell">
                    {{ a.avgPointsPerArrow }}
                  </td>
                </tr>
                <tr v-if="daily.length === 0">
                  <td colspan="6" class="text-center text-muted" style="padding:2rem;">
                    Sin datos en el ranking histórico aún.<br/>
                    <small>Los arqueros aparecen aquí una vez que tienen rondas y flechas registradas.</small>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>

        <!-- ══ MONTHLY TAB — top performers last 30 days ══ -->
        <template v-else>

          <div class="daily-info-bar">
            <p class="daily-description">
              TOP 10 MEJORES SUMA DE PUNTOS POR FLECHAS DEL MES
            </p>
          </div>

          <div class="lol-table-wrapper">
            <table class="lol-table" id="leaderboard-monthly-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Arquero</th>
                  <th>Puntaje Mensual</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(a, i) in monthly" :key="a.archerId" :id="`monthly-row-${a.archerId}`">
                  <td>
                    <span class="rank-num" :class="i + 1 <= 3 ? `rank-${i+1}` : ''">
                      {{ i + 1 }}
                    </span>
                  </td>
                  <td class="archer-name-cell">{{ a.archerName }}</td>
                  <td class="text-gold-bright avg-cell">
                    {{ a.monthlyScore }}
                  </td>
                </tr>
                <tr v-if="monthly.length === 0">
                  <td colspan="3" class="text-center text-muted" style="padding:2rem;">
                    Sin datos para el mes actual.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>


      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getLeaderboard, getTopMonth } from '@/api/archers'

const tab     = ref('daily')
const loading = ref(true)

const daily   = ref([])
const monthly = ref([])

// ── Countdown: only relevant for the daily tab ───────────────────────
const countdownStr = ref('--H --M --S')

function calcMidnight() {
  const now      = new Date()
  const midnight = new Date(now)
  midnight.setHours(24, 0, 0, 0)
  const diff = midnight - now
  if (diff <= 0) { countdownStr.value = '00H 00M 00S'; return }
  const h = Math.floor(diff / 3_600_000)
  const m = Math.floor((diff % 3_600_000) / 60_000)
  const s = Math.floor((diff % 60_000)    / 1_000)
  countdownStr.value =
    `${String(h).padStart(2,'0')}H ${String(m).padStart(2,'0')}M ${String(s).padStart(2,'0')}S`
}

let _tick = null

onMounted(async () => {
  calcMidnight()
  _tick = setInterval(calcMidnight, 1_000)

  const [lbRes, topRes] = await Promise.allSettled([
    getLeaderboard(),
    getTopMonth(),
  ])

  if (lbRes.status === 'fulfilled') {
    const data = Array.isArray(lbRes.value.data) ? lbRes.value.data : []
    daily.value = data.map(a => ({
      ...a,
      avgPointsPerArrow: a.avgPointsPerArrow != null
        ? Number(a.avgPointsPerArrow).toFixed(4)
        : '—',
    }))
  }

  if (topRes.status === 'fulfilled') {
    const data = Array.isArray(topRes.value.data) ? topRes.value.data : []
    monthly.value = data.map(a => ({
      archerId:     a.archerId,
      archerName:   a.name ?? 'Arquero',
      monthlyScore: a.monthlyScore ?? 0,
    }))
  }

  loading.value = false
})

onUnmounted(() => clearInterval(_tick))
</script>

<style scoped>
.leaderboard-page {
  padding: calc(var(--header-height) + 2rem) 0 4rem;
  min-height: 100vh;
}

/* Suppress title pseudo dividers */
.section-header .section-title::before,
.section-header .section-title::after { display: none; }

/* Header meta row — tabs only */
.lb-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* ── Tab buttons ── */
.lb-tabs {
  display: flex;
  gap: 0.25rem;
}

.lb-tab {
  font-family: 'Cinzel', serif;
  font-size: 0.65rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  padding: 0.35rem 0.9rem;
  background: transparent;
  border: 1px solid var(--border-subtle);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
}

.lb-tab:hover {
  border-color: var(--lol-gold-dark);
  color: var(--lol-gold-light);
}

.lb-tab.active {
  background: rgba(200,155,60,0.12);
  border-color: var(--lol-gold);
  color: var(--lol-gold-bright);
  box-shadow: 0 0 8px rgba(200,155,60,0.2);
}

/* ── Rule ── */
.lb-rule {
  width: 100%;
  background: linear-gradient(to right, rgba(200,170,110,0.55) 0%, rgba(200,170,110,0.55) 92%, transparent 100%);
}

/* ── Daily info bar: description left · countdown right ── */
.daily-info-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
  padding: 0.6rem 1rem;
  background: rgba(200,155,60,0.05);
  border: 1px solid rgba(200,155,60,0.15);
  border-radius: 4px;
}

.daily-description {
  font-size: 0.78rem;
  color: var(--text-muted);
  margin: 0;
}

/* ── Countdown ── */
.lb-countdown {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.countdown-label {
  font-family: 'Inter', sans-serif;
  font-size: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  color: var(--text-muted);
}

.countdown-value {
  font-family: 'Cinzel', serif;
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--cyan);
  letter-spacing: 0.08em;
  text-shadow: var(--glow-cyan);
}

/* ── Table cells ── */
.rank-num { font-family: 'Cinzel', serif; font-weight: 700; font-size: 0.9rem; }
.rank-1 { color: var(--lol-gold-bright); }
.rank-2 { color: #c0c0c0; }
.rank-3 { color: #cd7f32; }

.archer-name-cell { font-weight: 600; color: var(--text-primary); }

.avg-cell {
  font-family: 'Cinzel', serif;
  font-weight: 700;
}
</style>
