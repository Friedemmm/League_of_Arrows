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

        <!-- ══ Two-column layout when a past tournament is selected ══ -->
        <div class="t-layout" :class="{ 'has-panel': selectedTournament }">

          <!-- LEFT: tournament cards -->
          <div class="t-cards-col">

            <!-- ACTIVE tournaments -->
            <template v-if="showSection('active')">
              <div class="t-section-label">
                <span class="t-status-dot dot-active"></span> En Curso
              </div>
              <div class="tournament-grid mb-section">
                <div
                  v-for="t in activeTournaments"
                  :key="t.tournamentId"
                  class="t-card lol-card t-card--active"
                  :class="{ 't-card--selected': selectedTournament?.tournamentId === t.tournamentId }"
                  :id="`tournament-card-${t.tournamentId}`"
                  @click="selectTournament(t)"
                  style="cursor:pointer;"
                >
                  <div class="t-top">
                    <h3 class="t-name">{{ t.name }}</h3>
                    <span class="badge badge-success">En Curso</span>
                  </div>
                  <div v-if="t.categoryName" class="t-category">
                    <span class="material-icons" style="font-size:0.75rem;vertical-align:middle;">category</span>
                    {{ t.categoryName }}
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
                    <span class="t-view-hint">
                      <span class="material-icons" style="font-size:0.85rem;vertical-align:middle;">leaderboard</span>
                      Ver podio en vivo
                    </span>
                  </div>
                </div>
              </div>
            </template>

            <!-- UPCOMING tournaments -->
            <template v-if="showSection('upcoming')">
              <div class="t-section-label">
                <span class="t-status-dot dot-upcoming"></span> Próximamente
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
                  <div v-if="t.categoryName" class="t-category">
                    <span class="material-icons" style="font-size:0.75rem;vertical-align:middle;">category</span>
                    {{ t.categoryName }}
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

            <!-- PAST tournaments -->
            <template v-if="showSection('past')">
              <div class="t-section-label">
                <span class="t-status-dot dot-past"></span> Finalizados
              </div>
              <div class="tournament-grid">
                <div
                  v-for="t in pastTournaments"
                  :key="t.tournamentId"
                  class="t-card lol-card t-card--past"
                  :class="{ 't-card--selected': selectedTournament?.tournamentId === t.tournamentId }"
                  :id="`tournament-card-${t.tournamentId}`"
                  @click="selectTournament(t)"
                >
                  <div class="t-top">
                    <h3 class="t-name">{{ t.name }}</h3>
                    <span class="badge badge-muted">Finalizado</span>
                  </div>
                  <div v-if="t.categoryName" class="t-category">
                    <span class="material-icons" style="font-size:0.75rem;vertical-align:middle;">category</span>
                    {{ t.categoryName }}
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
                    <span class="t-view-hint">
                      <span class="material-icons" style="font-size:0.85rem;vertical-align:middle;">leaderboard</span>
                      Ver podio
                    </span>
                  </div>
                </div>
              </div>
            </template>

            <div v-if="nothingToShow" class="empty-msg">
              <p class="text-muted text-center">No hay torneos disponibles para este filtro.</p>
            </div>
          </div><!-- /.t-cards-col -->

          <!-- RIGHT: Past tournament podium panel -->
          <Transition name="slide-right">
            <div class="podium-panel lol-card" v-if="selectedTournament" id="past-podium-panel">
              <div class="podium-panel-header">
                <div>
                  <h3 class="podium-panel-title">{{ selectedTournament.name }}</h3>
                  <p class="podium-panel-sub">
                    {{ selectedTournament.active ? 'Podio en vivo (actualizable)' : 'Resultados finales' }}
                  </p>
                </div>
                <div class="podium-panel-actions">
                  <!-- Refresh for active (live) tournaments -->
                  <button
                    v-if="selectedTournament?.active"
                    class="podium-close"
                    :disabled="podiumLoading"
                    @click="refreshPodium"
                    title="Actualizar podio"
                  >
                    <span class="material-icons" :style="podiumLoading ? 'animation: spin 1s linear infinite' : ''">refresh</span>
                  </button>
                  <button class="podium-close" @click="selectedTournament = null; podium = []">
                    <span class="material-icons">close</span>
                  </button>
                </div>
              </div>

              <div v-if="podiumLoading" class="loading-center" style="padding:2rem 0;">
                <div class="spinner"></div>
              </div>

              <div v-else-if="podium.length === 0" class="text-center text-muted" style="padding:2rem;">
                Sin resultados registrados.
              </div>

              <div v-else class="podium-list">
                <div
                  v-for="p in podium"
                  :key="p.position"
                  class="podium-entry"
                  :class="`pos-${p.position}`"
                  :id="`podium-pos-${p.position}`"
                >
                  <div class="podium-medal" :class="`medal-${p.position}`">
                    {{ p.position }}
                  </div>
                  <div class="podium-info">
                    <span class="podium-name">{{ p.archerName }}</span>
                    <span class="podium-score">{{ p.finalScore }} pts</span>
                  </div>
                  <span class="material-icons podium-trophy"
                    v-if="p.position <= 3">
                    {{ p.position === 1 ? 'military_tech' : 'emoji_events' }}
                  </span>
                </div>
              </div>
            </div>
          </Transition>

        </div><!-- /.t-layout -->
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTournaments } from '@/api/tournaments'
import api from '@/api/axios'

const tournaments       = ref([])
const loading           = ref(true)
const activeFilter      = ref('all')
const selectedTournament = ref(null)   // works for both active & past
const podium            = ref([])
const podiumLoading     = ref(false)

const filters = [
  { value: 'all',      label: 'Todos' },
  { value: 'active',   label: 'En Curso' },
  { value: 'upcoming', label: 'Próximos' },
  { value: 'past',     label: 'Finalizados' },
]

const today = new Date()
today.setHours(0, 0, 0, 0)

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

async function loadPodium(tournamentId) {
  podiumLoading.value = true
  try {
    const res = await api.get(`/tournaments/${tournamentId}/podium`)
    podium.value = res.data ?? []
  } catch {
    podium.value = []
  } finally {
    podiumLoading.value = false
  }
}

async function selectTournament(t) {
  // Toggle off if same
  if (selectedTournament.value?.tournamentId === t.tournamentId) {
    selectedTournament.value = null
    podium.value = []
    return
  }
  selectedTournament.value = t
  podium.value = []
  await loadPodium(t.tournamentId)
}

// Refresh podium for active tournaments (live data)
async function refreshPodium() {
  if (!selectedTournament.value) return
  await loadPodium(selectedTournament.value.tournamentId)
}

onMounted(async () => {
  try {
    const res = await getTournaments()
    tournaments.value = Array.isArray(res.data) ? res.data : []
  } catch { /* ignore */ } finally { loading.value = false }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

/* ── Page layout ─────────────────────────────── */
.tournaments-page {
  padding: calc(var(--header-height) + 2rem) 0 4rem;
  min-height: 100vh;
}

/* Suppress title pseudo-element dividers */
.section-header .section-title::before,
.section-header .section-title::after { display: none; }

.t-rule {
  width: 100%;
  background: linear-gradient(to right, rgba(200,170,110,0.55) 0%, rgba(200,170,110,0.55) 92%, transparent 100%);
}
.t-header:hover .t-rule::after {
  transform: scaleX(1);
  transition: transform 0.35s ease-out;
}

.filter-tabs { display: flex; gap: 0.4rem; flex-shrink: 0; }

/* ── Two-column layout ─────────────────────── */
.t-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
  transition: grid-template-columns 0.3s ease;
}
.t-layout.has-panel {
  grid-template-columns: 1fr 340px;
}
@media (max-width: 900px) {
  .t-layout.has-panel { grid-template-columns: 1fr; }
}

.t-cards-col { min-width: 0; }

/* ── Section separators ────────────────────── */
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
  width: 7px; height: 7px;
  border-radius: 50%; flex-shrink: 0;
}
.dot-active   { background: var(--lol-success, #0ac8b9); box-shadow: 0 0 6px rgba(10,200,185,0.7); }
.dot-upcoming { background: var(--lol-blue-glow, #00d4ff); box-shadow: 0 0 6px rgba(0,212,255,0.5); }
.dot-past     { background: var(--text-muted); }

.mb-section { margin-bottom: 2.5rem; }

/* ── Tournament cards ────────────────────────── */
.tournament-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}

.t-card { display: flex; flex-direction: column; gap: 0.8rem; cursor: default; }

.t-card--active   { border-color: rgba(10,200,185,0.3); }
.t-card--upcoming { border-color: rgba(0,150,255,0.2); }
.t-card--past     { opacity: 0.75; cursor: pointer; transition: opacity 0.2s, border-color 0.2s, transform 0.2s; }
.t-card--past:hover { opacity: 1; border-color: rgba(200,155,60,0.4); transform: translateY(-2px); }
.t-card--selected { opacity: 1 !important; border-color: var(--lol-gold) !important; box-shadow: 0 0 12px rgba(200,155,60,0.2); }

.t-top {
  display: flex; align-items: flex-start;
  justify-content: space-between; gap: 0.5rem;
}

.t-name { font-size: 1rem; font-family: 'Cinzel', serif; color: var(--lol-gold-light); flex: 1; }
.t-category {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.68rem;
  font-family: 'Cinzel', serif;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--lol-gold);
  opacity: 0.8;
}

.t-dates { display: flex; align-items: center; gap: 0.5rem; font-size: 0.82rem; }
.t-date-item { display: flex; flex-direction: column; gap: 1px; }
.t-date-label { font-size: 0.62rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--text-muted); }
.t-sep { color: var(--lol-gold-dark); }
.t-divider { height: 1px; background: linear-gradient(90deg, var(--lol-gold-dark), transparent); opacity: 0.5; }
.t-footer { display: flex; gap: 0.5rem; align-items: center; }

.t-upcoming-note {
  font-family: 'Cinzel', serif; font-size: 0.62rem;
  text-transform: uppercase; letter-spacing: 0.1em;
  color: var(--lol-blue-glow); opacity: 0.7;
}
.t-view-hint {
  font-family: 'Cinzel', serif; font-size: 0.62rem;
  text-transform: uppercase; letter-spacing: 0.1em;
  color: var(--lol-gold); opacity: 0.8;
  display: flex; align-items: center; gap: 0.3rem;
}

/* ── Podium Side Panel ──────────────────────── */
.podium-panel {
  position: sticky;
  top: calc(var(--header-height, 70px) + 1rem);
  max-height: calc(100vh - var(--header-height, 70px) - 3rem);
  overflow-y: auto;
  padding: 1.4rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-color: rgba(200,155,60,0.35);
}

.podium-panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.5rem;
}

.podium-panel-title {
  font-family: 'Cinzel', serif;
  font-size: 0.9rem;
  color: var(--lol-gold-light);
  margin-bottom: 0.2rem;
}

.podium-panel-sub {
  font-size: 0.72rem;
  color: var(--text-muted);
}

.podium-close {
  background: none; border: none; cursor: pointer;
  color: var(--text-muted); transition: color 0.2s;
  display: flex; align-items: center;
  flex-shrink: 0;
}
.podium-close:hover { color: var(--lol-gold); }
.podium-close:disabled { opacity: 0.5; cursor: default; }
.podium-close .material-icons { font-size: 1.1rem; }

.podium-panel-actions { display: flex; gap: 0.3rem; align-items: center; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.podium-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.podium-entry {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.7rem 0.9rem;
  border-radius: 6px;
  background: rgba(255,255,255,0.03);
  border: 1px solid transparent;
  transition: border-color 0.2s;
}

.pos-1 { border-color: rgba(200,155,60,0.4); background: rgba(200,155,60,0.06); }
.pos-2 { border-color: rgba(160,170,180,0.3); background: rgba(160,170,180,0.04); }
.pos-3 { border-color: rgba(180,120,60,0.3); background: rgba(180,120,60,0.04); }

.podium-medal {
  width: 32px; height: 32px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-family: 'Cinzel', serif;
  font-size: 0.85rem;
  font-weight: 700;
  flex-shrink: 0;
}
.medal-1 { background: rgba(200,155,60,0.2); color: #c89b3c; border: 1.5px solid #c89b3c; }
.medal-2 { background: rgba(160,170,180,0.15); color: #a0aab4; border: 1.5px solid #a0aab4; }
.medal-3 { background: rgba(180,120,60,0.15); color: #b4783c; border: 1.5px solid #b4783c; }
.podium-medal:not(.medal-1):not(.medal-2):not(.medal-3) {
  background: rgba(255,255,255,0.05); color: var(--text-muted);
  border: 1.5px solid var(--lol-border);
}

.podium-info { display: flex; flex-direction: column; gap: 2px; flex: 1; min-width: 0; }
.podium-name {
  font-family: 'Cinzel', serif; font-size: 0.82rem;
  font-weight: 600; color: var(--text-primary);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.podium-score { font-size: 0.72rem; color: var(--lol-gold); font-family: 'Cinzel', serif; }

.podium-trophy { font-size: 1.1rem; flex-shrink: 0; }
.pos-1 .podium-trophy { color: #c89b3c; }
.pos-2 .podium-trophy { color: #a0aab4; }
.pos-3 .podium-trophy { color: #b4783c; }

/* ── Transitions ─────────────────────────────── */
.slide-right-enter-active,
.slide-right-leave-active { transition: all 0.3s ease; }
.slide-right-enter-from { opacity: 0; transform: translateX(30px); }
.slide-right-leave-to   { opacity: 0; transform: translateX(30px); }

.empty-msg { padding: 3rem 0; }
</style>
