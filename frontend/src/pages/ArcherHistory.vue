<template>
  <div class="page-wrapper">
    <div class="container">
      <h1 class="section-title">Tournament History</h1>
      <p class="text-secondary mb-4" style="margin-top:-1rem;">Your personal performance record across all competitions.</p>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <template v-else>
        <div class="stat-grid">
          <div class="stat-card">
            <div class="stat-value">{{ history.length }}</div>
            <div class="stat-label">Tournaments</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ totalScore }}</div>
            <div class="stat-label">Total Points</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ bestPosition }}</div>
            <div class="stat-label">Best Position</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ avgScore }}</div>
            <div class="stat-label">Avg Score</div>
          </div>
        </div>

        <div class="gold-divider"></div>

        <!-- Timeline -->
        <div class="history-timeline" v-if="history.length">
          <div
            v-for="(entry, i) in history"
            :key="i"
            class="timeline-item"
            :id="`history-entry-${i}`"
          >
            <div class="timeline-dot"></div>
            <div class="timeline-card lol-card">
              <div class="tl-header">
                <h3 class="tl-tournament">{{ entry.tournamentName }}</h3>
                <span class="tl-position" v-if="entry.position">
                  #{{ entry.position }}
                </span>
              </div>
              <div class="tl-meta">
                <span class="tl-score">{{ entry.score }} pts</span>
                <span class="tl-sep">·</span>
                <span class="tl-date">{{ formatDate(entry.endDate) }}</span>
                <span class="tl-sep">·</span>
                <span class="badge badge-blue" v-if="entry.categoryName">{{ entry.categoryName }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-state lol-card text-center" style="padding:3rem;">
          <p class="text-muted">You haven't entered any tournaments yet.</p>
          <RouterLink to="/tournaments" class="btn btn-gold mt-2">Browse Tournaments</RouterLink>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyHistory } from '@/api/archers'

const history = ref([])
const loading = ref(true)

const totalScore   = computed(() => history.value.reduce((sum, h) => sum + (h.score || 0), 0))
const avgScore     = computed(() => history.value.length
  ? (totalScore.value / history.value.length).toFixed(1) : '—')
const bestPosition = computed(() => {
  const positions = history.value.map(h => h.position).filter(Boolean)
  return positions.length ? Math.min(...positions) : '—'
})

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
}

onMounted(async () => {
  try {
    const res = await getMyHistory()
    history.value = res.data
  } catch { /* ignore */ } finally { loading.value = false }
})
</script>

<style scoped>
.history-timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
  position: relative;
  padding-left: 2rem;
}

.history-timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, var(--lol-gold), transparent);
}

.timeline-item {
  position: relative;
  margin-bottom: 1rem;
}

.timeline-dot {
  position: absolute;
  left: -1.6rem;
  top: 1.2rem;
  width: 10px;
  height: 10px;
  background: var(--lol-gold);
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(200,155,60,0.7);
}

.timeline-card { padding: 1rem 1.2rem; }

.tl-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.4rem;
}

.tl-tournament {
  font-size: 0.95rem;
  font-family: 'Cinzel', serif;
  color: var(--lol-gold-light);
}

.tl-position {
  font-family: 'Cinzel', serif;
  font-size: 1rem;
  font-weight: 700;
  color: var(--lol-gold-bright);
}

.tl-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.82rem;
  flex-wrap: wrap;
}

.tl-score {
  font-weight: 700;
  color: var(--lol-gold);
  font-family: 'Cinzel', serif;
}

.tl-sep { color: var(--text-muted); }
.tl-date { color: var(--text-muted); }

.empty-state { max-width: 400px; margin: 0 auto; }
</style>
