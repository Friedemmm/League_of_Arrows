<template>
  <div class="page-wrapper tournaments-page">
    <div class="container">

      <!-- ══ Section header ══ -->
      <div class="section-header t-header">
        <h1 class="section-title">Eventos</h1>
        <div class="section-meta-row t-meta-row">
          <p class="section-subtitle">Historial y estado de competiciones</p>
          <div class="filter-tabs">
            <button
              v-for="f in filters"
              :key="f.value"
              class="btn btn-sm"
              :class="activeFilter === f.value ? 'btn-gold' : 'btn-ghost'"
              :id="`filter-${f.value}`"
              @click="activeFilter = f.value"
            >{{ f.label }}</button>
          </div>
        </div>
        <hr class="header-rule t-rule" />
      </div>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <template v-else>

        <!-- ══ ACTIVE tournaments ══ -->
        <template v-if="showSection('active')">
          <div class="t-section-label">
            <span class="t-status-dot dot-active"></span>
            En Curso
          </div>
          <div class="tournament-grid mb-section">
            <div
              v-for="t in activeTournaments"
              :key="t.tournamentId"
              class="t-card lol-card t-card--active"
              :id="`tournament-card-${t.tournamentId}`"
            >
              <div class="t-top">
                <h3 class="t-name">{{ t.name }}</h3>
                <span class="badge badge-success">En Curso</span>
              </div>
              <div class="t-dates">
                <span class="t-date-item">
                  <span class="t-date-label">Inicio</span>
                  <span>{{ formatDate(t.startDate) }}</span>
                </span>
                <span class="t-sep">—</span>
                <span class="t-date-item">
                  <span class="t-date-label">Fin</span>
                  <span>{{ formatDate(t.endDate) }}</span>
                </span>
              </div>
              <div class="t-divider"></div>
              <div class="t-footer">
                <RouterLink
                  to="/history"
                  :id="`btn-join-${t.tournamentId}`"
                  class="btn btn-gold btn-sm"
                >Inscribirse</RouterLink>
                <RouterLink
                  to="/leaderboard"
                  :id="`btn-results-${t.tournamentId}`"
                  class="btn btn-outline btn-sm"
                >Ranking</RouterLink>
              </div>
            </div>
          </div>
        </template>

        <!-- ══ UPCOMING tournaments ══ -->
        <template v-if="showSection('upcoming')">
          <div class="t-section-label">
            <span class="t-status-dot dot-upcoming"></span>
            Próximamente
          </div>
          <div class="tournament-grid mb-section">
            <div
              v-for="t in upcomingTournaments"
              :key="t.tournamentId"
              class="t-card lol-card t-card--upcoming"
              :id="`tournament-card-${t.tournamentId}`"
            >
              <div class="t-top">
                <h3 class="t-name">{{ t.name }}</h3>
                <span class="badge badge-blue">Próximo</span>
              </div>
              <div class="t-dates">
                <span class="t-date-item">
                  <span class="t-date-label">Inicio</span>
                  <span>{{ formatDate(t.startDate) }}</span>
                </span>
                <span class="t-sep">—</span>
                <span class="t-date-item">
                  <span class="t-date-label">Fin</span>
                  <span>{{ formatDate(t.endDate) }}</span>
                </span>
              </div>
              <div class="t-divider"></div>
              <div class="t-footer">
                <span class="t-upcoming-note">Abierto pronto</span>
              </div>
            </div>
          </div>
        </template>

        <!-- ══ PAST tournaments ══ -->
        <template v-if="showSection('past')">
          <div class="t-section-label">
            <span class="t-status-dot dot-past"></span>
            Finalizados
          </div>
          <div class="tournament-grid">
            <div
              v-for="t in pastTournaments"
              :key="t.tournamentId"
              class="t-card lol-card t-card--past"
              :id="`tournament-card-${t.tournamentId}`"
            >
              <div class="t-top">
                <h3 class="t-name">{{ t.name }}</h3>
                <span class="badge badge-muted">Finalizado</span>
              </div>
              <div class="t-dates">
                <span class="t-date-item">
                  <span class="t-date-label">Inicio</span>
                  <span>{{ formatDate(t.startDate) }}</span>
                </span>
                <span class="t-sep">—</span>
                <span class="t-date-item">
                  <span class="t-date-label">Fin</span>
                  <span>{{ formatDate(t.endDate) }}</span>
                </span>
              </div>
              <div class="t-divider"></div>
              <div class="t-footer">
                <RouterLink
                  to="/leaderboard"
                  :id="`btn-results-past-${t.tournamentId}`"
                  class="btn btn-outline btn-sm"
                >Ver Resultados</RouterLink>
              </div>
            </div>
          </div>
        </template>

        <!-- Empty state -->
        <div v-if="nothingToShow" class="empty-msg">
          <p class="text-muted text-center">No hay torneos disponibles para este filtro.</p>
        </div>

      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTournaments } from '@/api/tournaments'

const tournaments  = ref([])
const loading      = ref(true)
const activeFilter = ref('all')

const filters = [
  { value: 'all',      label: 'Todos' },
  { value: 'active',   label: 'En Curso' },
  { value: 'upcoming', label: 'Próximos' },
  { value: 'past',     label: 'Finalizados' },
]

const today = new Date()
today.setHours(0, 0, 0, 0)

/**
 * Classify a tournament.
 * - active   → is_active = true
 * - upcoming → is_active = false AND startDate > today
 * - past     → is_active = false AND startDate <= today (already started / ended)
 */
const activeTournaments = computed(() =>
  tournaments.value.filter(t => t.active)
)

const upcomingTournaments = computed(() =>
  tournaments.value.filter(t => !t.active && new Date(t.startDate) > today)
    .sort((a, b) => new Date(a.startDate) - new Date(b.startDate))
)

const pastTournaments = computed(() =>
  tournaments.value.filter(t => !t.active && new Date(t.startDate) <= today)
    .sort((a, b) => new Date(b.startDate) - new Date(a.startDate))
)

function showSection(type) {
  if (activeFilter.value !== 'all' && activeFilter.value !== type) return false
  if (type === 'active')   return activeTournaments.value.length > 0
  if (type === 'upcoming') return upcomingTournaments.value.length > 0
  if (type === 'past')     return pastTournaments.value.length > 0
  return false
}

const nothingToShow = computed(() => {
  const f = activeFilter.value
  if (f === 'all')      return tournaments.value.length === 0
  if (f === 'active')   return activeTournaments.value.length === 0
  if (f === 'upcoming') return upcomingTournaments.value.length === 0
  if (f === 'past')     return pastTournaments.value.length === 0
  return false
})

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' })
}

onMounted(async () => {
  try {
    const res = await getTournaments()
    tournaments.value = Array.isArray(res.data) ? res.data : []
  } catch { /* ignore */ } finally { loading.value = false }
})
</script>

<style scoped>
/* ── Page layout ─────────────────────────────────── */
.tournaments-page {
  padding: calc(var(--header-height) + 2rem) 0 4rem;
  min-height: 100vh;
}

/* Suppress title pseudo-element dividers */
.section-header .section-title::before,
.section-header .section-title::after { display: none; }

/* Wider rule */
.t-rule {
  width: 100%;
  background: linear-gradient(to right, rgba(200,170,110,0.55) 0%, rgba(200,170,110,0.55) 92%, transparent 100%);
}
.t-header:hover .t-rule::after {
  transform: scaleX(1);
  transition: transform 0.35s ease-out;
}

/* Filters */
.filter-tabs { display: flex; gap: 0.4rem; flex-shrink: 0; }

/* ── Section separators ────────────────────────── */
.t-section-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Cinzel', serif;
  font-size: 0.65rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: var(--text-muted);
  margin-bottom: 0.75rem;
}

.t-status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-active   { background: var(--lol-success, #0ac8b9); box-shadow: 0 0 6px rgba(10,200,185,0.7); }
.dot-upcoming { background: var(--lol-blue-glow, #00d4ff); box-shadow: 0 0 6px rgba(0,212,255,0.5); }
.dot-past     { background: var(--text-muted); }

.mb-section { margin-bottom: 2.5rem; }

/* ── Tournament cards grid ───────────────────────── */
.tournament-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.t-card { display: flex; flex-direction: column; gap: 0.8rem; }

.t-card--active   { border-color: rgba(10,200,185,0.3); }
.t-card--upcoming { border-color: rgba(0,150,255,0.2); }
.t-card--past     { opacity: 0.75; }
.t-card--past:hover { opacity: 1; }

.t-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.5rem;
}

.t-name {
  font-size: 1rem;
  font-family: 'Cinzel', serif;
  color: var(--lol-gold-light);
  flex: 1;
}

.t-dates {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.82rem;
}

.t-date-item { display: flex; flex-direction: column; gap: 1px; }

.t-date-label {
  font-size: 0.62rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--text-muted);
}

.t-sep { color: var(--lol-gold-dark); }

.t-divider {
  height: 1px;
  background: linear-gradient(90deg, var(--lol-gold-dark), transparent);
  opacity: 0.5;
}

.t-footer { display: flex; gap: 0.5rem; align-items: center; }

.t-upcoming-note {
  font-family: 'Cinzel', serif;
  font-size: 0.62rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--lol-blue-glow);
  opacity: 0.7;
}

.empty-msg { padding: 3rem 0; }
</style>
