<template>
  <div class="page-wrapper tournaments-page">
    <div class="container">

      <!-- Unified section header: title / [subtitle + filters] / rule -->
      <div class="section-header t-header">
        <h1 class="section-title">Tournaments</h1>
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

      <div v-else class="tournament-grid">
        <div
          v-for="t in filtered"
          :key="t.idTournament"
          class="t-card lol-card"
          :id="`tournament-card-${t.idTournament}`"
        >
          <div class="t-top">
            <h3 class="t-name">{{ t.name }}</h3>
            <span class="badge" :class="t.active ? 'badge-success' : 'badge-muted'">
              {{ t.active ? 'Active' : 'Ended' }}
            </span>
          </div>
          <div class="t-dates">
            <span class="t-date-item">
              <span class="t-date-label">Start</span>
              <span>{{ formatDate(t.startDate) }}</span>
            </span>
            <span class="t-sep">—</span>
            <span class="t-date-item">
              <span class="t-date-label">End</span>
              <span>{{ formatDate(t.endDate) }}</span>
            </span>
          </div>
          <div class="t-divider"></div>
          <div class="t-footer">
            <RouterLink
              v-if="t.active"
              to="/history"
              :id="`btn-join-${t.idTournament}`"
              class="btn btn-gold btn-sm"
            >Join</RouterLink>
            <RouterLink
              :to="`/leaderboard`"
              :id="`btn-results-${t.idTournament}`"
              class="btn btn-outline btn-sm"
            >Results</RouterLink>
          </div>
        </div>

        <div v-if="filtered.length === 0" class="empty-msg">
          <p class="text-muted text-center">No tournaments match this filter.</p>
        </div>
      </div>
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
  { value: 'all',    label: 'All' },
  { value: 'active', label: 'Active' },
  { value: 'ended',  label: 'Ended' },
]

const filtered = computed(() => {
  if (activeFilter.value === 'active') return tournaments.value.filter(t => t.active)
  if (activeFilter.value === 'ended')  return tournaments.value.filter(t => !t.active)
  return tournaments.value
})

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

onMounted(async () => {
  try {
    const res = await getTournaments()
    tournaments.value = res.data
  } catch { /* ignore */ } finally { loading.value = false }
})
</script>

<style scoped>
/* ── Page layout ─────────────────────────────────── */
.tournaments-page {
  padding: calc(var(--header-height) + 2rem) 0 4rem;
  min-height: 100vh;
}

/* ── Section header overrides for this page ──────── */
/* Suppress the ::before/::after dividers on h1 — hr.header-rule handles it */
.section-header .section-title::before,
.section-header .section-title::after {
  display: none;
}

/* Wider rule for Tournaments — extends to full container width */
.t-rule {
  width: 100%;
  background: linear-gradient(
    to right,
    rgba(200,170,110,0.55) 0%,
    rgba(200,170,110,0.55) 92%,
    transparent 100%
  );
}

/* Loading sweep on Tournaments header rule */
.t-header:hover .t-rule::after {
  transform: scaleX(1);
  transition: transform 0.35s ease-out;
}

/* Filters pinned to the right of the meta-row */
.filter-tabs { display: flex; gap: 0.4rem; flex-shrink: 0; }

/* ── Tournament cards grid ───────────────────────── */
.tournament-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.t-card { display: flex; flex-direction: column; gap: 0.8rem; }

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

.t-footer { display: flex; gap: 0.5rem; }

.empty-msg { grid-column: 1/-1; padding: 3rem 0; }
</style>
