<template>
  <div class="page-wrapper leaderboard-page">
    <div class="container">

      <!-- Unified section header: title / subtitle / rule -->
      <div class="section-header">
        <h1 class="section-title">Leaderboard</h1>
        <div class="section-meta-row">
          <p class="section-subtitle">Top 50 arqueros con mejor promedio de puntos por flecha</p>
        </div>
        <hr class="header-rule" />
      </div>

      <div v-if="loading" class="loading-center">
        <div class="spinner"></div>
      </div>

      <template v-else>
        <!-- Podium (top 3) -->
        <div class="podium" v-if="archers.length >= 3">
          <!-- 2nd -->
          <div class="podium-item podium-2">
            <div class="podium-name">{{ archers[1]?.archerName }}</div>
            <div class="podium-score">{{ archers[1]?.avgPointsPerArrow?.toFixed(2) }} avg</div>
            <div class="podium-block">🥈</div>
            <div class="podium-rank">2nd</div>
          </div>
          <!-- 1st -->
          <div class="podium-item podium-1">
            <div class="podium-name">{{ archers[0]?.archerName }}</div>
            <div class="podium-score">{{ archers[0]?.avgPointsPerArrow?.toFixed(2) }} avg</div>
            <div class="podium-block">👑</div>
            <div class="podium-rank">Champion</div>
          </div>
          <!-- 3rd -->
          <div class="podium-item podium-3">
            <div class="podium-name">{{ archers[2]?.archerName }}</div>
            <div class="podium-score">{{ archers[2]?.avgPointsPerArrow?.toFixed(2) }} avg</div>
            <div class="podium-block">🥉</div>
            <div class="podium-rank">3rd</div>
          </div>
        </div>

        <div class="gold-divider"></div>

        <!-- Full Table -->
        <div class="lol-table-wrapper">
          <table class="lol-table" id="leaderboard-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Archer</th>
                <th>Category</th>
                <th>Avg Pts/Arrow</th>
                <th>Total Arrows</th>
                <th>Total Score</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(archer, i) in archers" :key="archer.archerId">
                <td>
                  <span class="rank-num" :class="`rank-${i+1 <= 3 ? i+1 : ''}`">{{ i + 1 }}</span>
                </td>
                <td class="archer-name-cell">{{ archer.archerName }}</td>
                <td><span class="badge badge-blue">{{ archer.categoryName || '—' }}</span></td>
                <td class="text-gold-bright" style="font-family:'Cinzel',serif;font-weight:700;">
                  {{ archer.avgPointsPerArrow?.toFixed(2) }}
                </td>
                <td>{{ archer.totalArrows }}</td>
                <td>{{ archer.totalScore }}</td>
              </tr>
              <tr v-if="archers.length === 0">
                <td colspan="6" class="text-center text-muted" style="padding:2rem;">
                  No leaderboard data available yet.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTopMonth } from '@/api/archers'

const archers = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getTopMonth()
    archers.value = res.data
  } catch { /* ignore */ } finally { loading.value = false }
})
</script>

<style scoped>
/* Page-level padding to clear the fixed header */
.leaderboard-page {
  padding: calc(var(--header-height) + 2rem) 0 4rem;
  min-height: 100vh;
}

/* Suppress title pseudo-element dividers — header-rule handles it */
.section-header .section-title::before,
.section-header .section-title::after { display: none; }

.rank-num {
  font-family: 'Cinzel', serif;
  font-weight: 700;
  font-size: 0.9rem;
}
.rank-1 { color: var(--lol-gold-bright); }
.rank-2 { color: #c0c0c0; }
.rank-3 { color: #cd7f32; }

.archer-name-cell {
  font-weight: 600;
  color: var(--text-primary);
}
</style>
