<template>
  <div class="page-wrapper">
    <div class="container">
      <h1 class="section-title">Trending Archers</h1>
      <p class="text-secondary mb-4" style="margin-top:-1rem;">
        Top performing archers over the last 30 days.
      </p>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <div v-else>
        <!-- Hero top archer -->
        <div class="top-hero lol-card mb-4" v-if="archers.length">
          <div class="top-hero-rank">#1 This Month</div>
          <div class="top-hero-name">{{ archers[0].archerName }}</div>
          <div class="top-hero-score">{{ archers[0].monthlyScore }} Total Points</div>
        </div>

        <!-- Ranked list -->
        <div class="trending-list">
          <div
            v-for="(archer, i) in archers"
            :key="archer.archerId"
            class="trending-row lol-card"
            :id="`trending-archer-${archer.archerId}`"
          >
            <div class="tr-rank" :class="rankClass(i)">{{ i + 1 }}</div>
            <div class="tr-info">
              <div class="tr-name">{{ archer.archerName }}</div>
              <div class="tr-sub">Arquero</div>
            </div>
            <div class="tr-score-block">
              <div class="tr-score">{{ archer.monthlyScore }}</div>
              <div class="tr-score-label">pts</div>
            </div>
            <div class="tr-bar-wrap">
              <div
                class="tr-bar"
                :style="{ width: barWidth(archer.totalScore) + '%' }"
              ></div>
            </div>
          </div>

          <div v-if="archers.length === 0" class="text-center text-muted" style="padding:3rem;">
            No trending data available this month.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTopMonth } from '@/api/archers'

const archers = ref([])
const loading = ref(true)

const maxScore = computed(() =>
  archers.value.length ? Math.max(...archers.value.map(a => a.monthlyScore || 0)) : 1
)

function barWidth(score) {
  return Math.round(((score || 0) / maxScore.value) * 100)
}

function rankClass(i) {
  if (i === 0) return 'rank-gold'
  if (i === 1) return 'rank-silver'
  if (i === 2) return 'rank-bronze'
  return ''
}

onMounted(async () => {
  try {
    const res = await getTopMonth()
    // Backend TopArcherDTO: { archerId, name, monthlyScore }
    archers.value = (Array.isArray(res.data) ? res.data : []).map(a => ({
      archerId:     a.archerId,
      archerName:   a.name ?? 'Arquero',
      monthlyScore: a.monthlyScore ?? 0,
    }))
  } catch { /* ignore */ } finally { loading.value = false }
})
</script>

<style scoped>
.top-hero {
  text-align: center;
  padding: 2.5rem 2rem;
  background: linear-gradient(135deg, rgba(200,155,60,0.08), rgba(9,20,40,0.95));
  border-color: var(--lol-gold);
  box-shadow: var(--glow-gold);
}

.top-hero-rank {
  font-family: 'Cinzel', serif;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: var(--lol-gold);
  margin-bottom: 0.5rem;
}

.top-hero-name {
  font-family: 'Cinzel', serif;
  font-size: 2rem;
  font-weight: 700;
  color: var(--lol-gold-light);
  margin-bottom: 0.3rem;
}

.top-hero-score {
  font-size: 1rem;
  color: var(--text-secondary);
}

.trending-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.trending-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.85rem 1.2rem;
}

.tr-rank {
  font-family: 'Cinzel', serif;
  font-size: 1.2rem;
  font-weight: 700;
  min-width: 2rem;
  text-align: center;
  color: var(--text-muted);
}

.rank-gold   { color: var(--lol-gold-bright); }
.rank-silver { color: #c0c0c0; }
.rank-bronze { color: #cd7f32; }

.tr-info { flex: 1; }

.tr-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.92rem;
}

.tr-sub {
  font-size: 0.72rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.tr-score-block {
  text-align: right;
  min-width: 60px;
}

.tr-score {
  font-family: 'Cinzel', serif;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--lol-gold-bright);
  line-height: 1;
}

.tr-score-label {
  font-size: 0.65rem;
  color: var(--text-muted);
  text-transform: uppercase;
}

.tr-bar-wrap {
  width: 120px;
  height: 4px;
  background: rgba(200,155,60,0.1);
  border-radius: 2px;
  overflow: hidden;
}

.tr-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--lol-gold-dark), var(--lol-gold));
  border-radius: 2px;
  transition: width 0.6s ease;
}
</style>
