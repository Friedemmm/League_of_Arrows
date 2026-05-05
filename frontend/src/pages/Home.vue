<template>
  <div class="home-page">
    <div class="container">
      <div class="home-grid">

        <!-- ══════════════ LEFT COLUMN: Top Arqueros + Podium ══════════════ -->
        <section class="col-left">
          <!-- Section header: title / meta-row / divider -->
          <div class="section-header">
            <h1 class="section-title home-section-title">Top Arqueros</h1>
            <div class="section-meta-row">
              <p class="section-subtitle">Promedio de puntos por flecha</p>
              <RouterLink to="/leaderboard" class="link-more" id="link-ranking-completo">
                Ver Top 50 arqueros <span class="arrow">›</span>
              </RouterLink>
            </div>
            <hr class="header-rule" />
          </div>

          <!-- Podium — pure CSS three-tier layout -->
          <PodiumTop3 :archers="topArchers" />

        </section><!-- /.col-left -->

        <!-- ══════════════ RIGHT COLUMN: Eventos ══════════════ -->
        <section class="col-right">
          <div class="section-header">
            <h1 class="section-title home-section-title">Eventos</h1>
            <div class="section-meta-row">
              <p class="section-subtitle">Próximos 3 eventos</p>
              <RouterLink to="/tournaments" class="link-more" id="link-calendario">
                Calendario <span class="arrow">›</span>
              </RouterLink>
            </div>
            <hr class="header-rule" />
          </div>

          <div class="eventos-list">
            <div
              v-for="t in upcomingEvents"
              :key="t.tournamentId"
              class="evento-row"
              :id="`evento-${t.tournamentId}`"
            >
              <!-- Left: Date column -->
              <div class="evento-date-col">
                <span class="evento-day">{{ eventDay(t) }}</span>
                <span class="evento-month-year">{{ eventMonthYear(t) }}</span>
                <span class="evento-status" :class="t.active ? 'status-active' : 'status-ended'">
                  {{ t.active ? 'En Curso' : 'Finalizado' }}
                </span>
              </div>

              <!-- Right: Event card -->
              <div class="evento-card">
                <div class="evento-card-top">
                  <div class="evento-badges">
                    <span class="badge badge-blue">{{ t.categoryId ? `Cat. ${t.categoryId}` : 'General' }}</span>
                    <span class="evento-full-date">{{ formatFullDate(t.startDate) }}</span>
                  </div>
                </div>
                <h3 class="evento-name">{{ t.name }}</h3>
                <ul class="evento-details">
                  <li>Categoría: <strong>{{ t.categoryId ? `Cat. ${t.categoryId}` : 'General' }}</strong></li>
                  <li v-if="t.active">Participación: <strong>Cupo abierto</strong>. Consulta bases y horarios.</li>
                  <li v-else>Torneo finalizado. Consulta los resultados y el ranking.</li>
                </ul>
              </div>
            </div>

            <div v-if="upcomingEvents.length === 0" class="empty-state text-center text-muted" style="padding:3rem;">
              No hay eventos próximos disponibles.
            </div>
          </div>
        </section>

      </div><!-- /.home-grid -->
    </div><!-- /.container -->
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTournaments } from '../api/tournaments'
import { getTopMonth, getLeaderboard } from '../api/archers'
import PodiumTop3 from '../components/PodiumTop3.vue'

const topArchers  = ref([])
const tournaments = ref([])
const loading     = ref(true)
const loadError   = ref(null)

/**
 * Normalise a LeaderboardDTO (materialized view) to the shape PodiumTop3 expects:
 *   API:      { globalPosition, archerId, name, tournamentsPlayed, arrowsFired, totalScore, avgPointsPerArrow }
 *   Banner:   { archerName, avgPointsPerArrow, avatarUrl }
 */
function normalizeLeaderboard(a) {
  return {
    archerId:          a.archerId,
    archerName:        a.name ?? 'Arquero',
    avgPointsPerArrow: a.avgPointsPerArrow ?? 0,
    avatarUrl:         null,
  }
}

/**
 * Fallback normaliser using top-month data (monthlyScore only).
 * avgPointsPerArrow is estimated as monthlyScore / typical arrows fired.
 */
function normalizeMonthly(a) {
  return {
    archerId:          a.archerId,
    archerName:        a.name ?? 'Arquero',
    avgPointsPerArrow: a.monthlyScore != null ? +(a.monthlyScore / 75).toFixed(4) : 0,
    avatarUrl:         null,
  }
}

// Show only the next 3 upcoming or active tournaments, sorted by start date
const upcomingEvents = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return tournaments.value
    .filter(t => t.active || new Date(t.startDate) >= today)
    .sort((a, b) => new Date(a.startDate) - new Date(b.startDate))
    .slice(0, 3)
})

function eventDay(t) {
  if (!t.startDate) return '—'
  return new Date(t.startDate).getDate()
}

function eventMonthYear(t) {
  if (!t.startDate) return ''
  const d = new Date(t.startDate)
  const month = d.toLocaleDateString('es-ES', { month: 'short' }).toUpperCase()
  return `${month} · ${d.getFullYear()}`
}

function formatFullDate(d) {
  if (!d) return '—'
  const dt = new Date(d)
  return dt.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

onMounted(async () => {
  loading.value   = true
  loadError.value = null

  // ── Top Arqueros: prefer materialized view for real avg/arrow ──────
  try {
    const res = await getLeaderboard()
    const data = Array.isArray(res.data) ? res.data : []
    if (data.length >= 3) {
      topArchers.value = data.slice(0, 6).map(normalizeLeaderboard)
    } else {
      // Fallback: use top-month endpoint
      const res2 = await getTopMonth()
      topArchers.value = (Array.isArray(res2.data) ? res2.data : [])
        .slice(0, 6)
        .map(normalizeMonthly)
    }
  } catch (e) {
    console.error('[Home] leaderboard error:', e.message)
    // Try monthly fallback
    try {
      const res2 = await getTopMonth()
      topArchers.value = (Array.isArray(res2.data) ? res2.data : [])
        .slice(0, 6)
        .map(normalizeMonthly)
    } catch (e2) {
      loadError.value = 'No se pudo cargar el ranking. Verifica que el backend esté activo.'
    }
  }

  // ── Tournaments ───────────────────────────────────────────────────
  try {
    const res = await getTournaments()
    tournaments.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error('[Home] /tournaments error:', e.message)
  }

  loading.value = false
})
</script>

<style scoped>
/* ═══════════════════════════════════════
   PAGE
   ═══════════════════════════════════════ */
.home-page {
  position: relative;
  overflow-x: hidden;
  /* Top padding accounts for the fixed header + breathing room.
     Applied here (not on .top-section) so BOTH columns start at
     the identical vertical position — this is the alignment fix. */
  padding: calc(var(--header-height) + 2rem) 0 4rem;
  min-height: 100vh;
}

/* ── Two-column grid ─────────────────────────────────────────
   LEFT  62% : podium  (larger share so banners have room)
   RIGHT 38% : events list
   ──────────────────────────────────────────────────────────── */
.home-grid {
  display: grid;
  grid-template-columns: 62fr 38fr;   /* podium gets more horizontal space */
  gap: 5rem;                          /* safe gutter prevents collision     */
  align-items: start;
}

/* Podium column: anchor content to the left of its wider cell */
.col-left {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

/* Events column: stretch to fill its cell for balanced look */
.col-right {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

/* Responsive: stack on small screens */
@media (max-width: 960px) {
  .home-grid {
    grid-template-columns: 1fr;
    gap: 2.5rem;
  }
  .col-left  { align-items: center; }
  .col-right { align-items: stretch; }
}

/* Scale down for inline section use — preserves padding-bottom for divider */
.home-section-title {
  font-size: 2rem !important;
  letter-spacing: 0.12em !important;
  /* Do NOT override margin-bottom or padding-bottom — pseudo-elements need them */
}

.link-more {
  font-family: 'Cinzel', serif;
  font-size: 0.72rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--lol-blue-glow);
  text-decoration: none;
  white-space: nowrap;
  transition: color 0.2s, text-shadow 0.2s;
  padding-top: 0.3rem;
}

.link-more:hover {
  color: #50e8ff;
  text-shadow: 0 0 8px rgba(0,212,255,0.4);
}

.link-more .arrow {
  font-size: 1rem;
  margin-left: 2px;
}

/* ═══════════════════════════════════════
   TOP ARQUEROS — PODIUM
   ═══════════════════════════════════════ */
/* ═══════════════════════════════════════
   SECTION HEADERS — unified layout
   title / [subtitle · · · button] / rule
   ═══════════════════════════════════════ */
.section-header {
  display: flex;
  flex-direction: column;
  gap: 0;
  margin-bottom: 1.5rem;
  width: 100%;          /* fills col-left even with align-items: flex-start */
}

/* The row: subtitle on the left, button on the far right */
.section-meta-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  width: 100%;          /* ensures space-between works across full column width */
  gap: 1rem;
  margin-top: 0.15rem;
}

/* Full-width gold gradient rule */
.header-rule {
  position: relative;
  width: 100%;
  height: 1px;
  border: none;
  background: linear-gradient(
    to right,
    rgba(200,170,110,0.5) 0%,
    rgba(200,170,110,0.5) 85%,
    transparent 100%
  );
  margin: 0.4rem 0 0 0;
  overflow: visible;
}

/* Loading sweep: bright gold line grows left→right on hover */
.header-rule::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to right,
    #C89B3C 0%,
    #EBDCB2 50%,
    #C89B3C 85%,
    transparent 100%
  );
  transform: scaleX(0);
  transform-origin: left center;
  transition: none;               /* instant reset on leave */
}

/* Trigger from section-header hover */
.section-header:hover .header-rule::after {
  transform: scaleX(1);
  transition: transform 0.35s ease-out;
}

/* Override ::before/::after on .section-title so header-rule is the only divider */
.section-header .section-title::before,
.section-header .section-title::after {
  display: none;
}

.top-section {
  /* No padding-top here — it lives on .home-page so both columns align */
}

.podium-stage {
  display: flex;
  justify-content: center;
  margin-top: 0.5rem;
  padding: 0 1rem;
}

.podium-wrapper {
  position: relative;
  width: 100%;
  max-width: 700px;
}

.podium-image {
  width: 100%;
  height: auto;
  display: block;
  /* no filter — transparent PNG renders against the aurora bg */
}

/* ═══════════════════════════════════════
   PODIUM PANELS — Minimalist, anchored
   ═══════════════════════════════════════ */
.podium-archers {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  pointer-events: none;
  z-index: 2;
}

/* Flat panel — no float, no bounce, no blur card */
.archer-panel {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 0.3rem 0.9rem 0.35rem;
  background: rgba(4, 12, 22, 0.78);
  border: 1px solid rgba(200, 155, 60, 0.28);
  border-bottom: 2px solid rgba(200, 155, 60, 0.5); /* anchors to pedestal */
  pointer-events: auto;
  /* No border-radius — flat panel feel */
  /* No transition translateY — static & anchored */
}

/* Position panels above their respective pedestals */
.pos-1 {
  top: 2%;
  left: 50%;
  transform: translateX(-50%);
}
.pos-2 {
  top: 10%;
  left: 8%;
}
.pos-3 {
  top: 14%;
  right: 7%;
}

/* Rank number — tiny, muted */
.panel-rank {
  font-family: 'Cinzel', serif;
  font-size: 0.5rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: rgba(200,170,110,0.5);
  text-transform: uppercase;
  line-height: 1;
}
.panel-rank.gold { color: rgba(200,155,60,0.85); }

/* Archer name — clean serif, cream */
.panel-name {
  font-family: 'Cinzel', serif;
  font-size: 0.78rem;
  font-weight: 700;
  color: #EBDCB2;
  text-align: center;
  letter-spacing: 0.04em;
  line-height: 1.2;
  white-space: nowrap;
}

/* Score — muted gold below name */
.panel-score {
  font-family: 'Inter', sans-serif;
  font-size: 0.68rem;
  font-weight: 600;
  color: rgba(200,155,60,0.75);
  letter-spacing: 0.06em;
  line-height: 1;
}
.panel-score.gold {
  color: #C89B3C;
  font-weight: 700;
}

/* ═══════════════════════════════════════
   EVENTOS — Date left / Card right
   ═══════════════════════════════════════ */
.eventos-section {
  /* no extra padding — alignment is controlled by .home-page */
}

.eventos-list {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.evento-row {
  display: flex;
  align-items: stretch;         /* date-col and card share the same height */
  gap: 1.5rem;
}

/* Left date column — stretches to full row height */
.evento-date-col {
  flex-shrink: 0;
  width: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;      /* vertically center date within full card height */
  padding: 0 0 0 0.8rem;
  border-left: 3px solid var(--lol-gold-dark);
}

.evento-day {
  font-family: 'Cinzel', serif;
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.evento-month-year {
  font-family: 'Cinzel', serif;
  font-size: 0.58rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
  margin-top: 2px;
}

.evento-status {
  font-family: 'Cinzel', serif;
  font-size: 0.65rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-top: 6px;
}

.status-active { color: var(--lol-success, #0ac8b9); }
.status-ended  { color: var(--text-muted); }

/* Right event card */
.evento-card {
  flex: 1;
  padding: 1rem 1.4rem;
  background: var(--bg-panel);
  border: 1px solid var(--border-subtle);
  border-radius: var(--border-radius);
  transition: border-color 0.25s;
}

.evento-card:hover {
  border-color: var(--border-gold);
}

.evento-card-top {
  margin-bottom: 0.5rem;
}

.evento-badges {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.evento-full-date {
  font-size: 0.72rem;
  color: var(--text-muted);
}

.evento-name {
  font-family: 'Cinzel', serif;
  font-size: 1.05rem;
  color: var(--text-primary);
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.evento-details {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.evento-details li {
  font-size: 0.8rem;
  color: var(--text-secondary);
  position: relative;
  padding-left: 0.8rem;
}

.evento-details li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--lol-gold-dark);
}

.evento-details li strong {
  color: var(--text-primary);
  font-weight: 600;
}

/* ═══════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════ */
@media (max-width: 768px) {
  .podium-wrapper { max-width: 100%; }
  .pos-1 { top: 0%; }
  .pos-2 { top: 8%; left: 3%; }
  .pos-3 { top: 12%; right: 3%; }
  .archer-podium-card { min-width: 75px; padding: 0.3rem 0.5rem; }
  .archer-avatar { width: 28px; height: 28px; font-size: 0.55rem; }
  .archer-podium-name { font-size: 0.65rem; }
  .archer-podium-score { font-size: 0.7rem; }

  .evento-row { flex-direction: column; gap: 0.5rem; }
  .evento-date-col { flex-direction: row; width: auto; gap: 0.5rem; align-items: center; }
  .evento-day { font-size: 1.3rem; }
  .section-header { flex-direction: column; gap: 0.5rem; }
}
</style>
