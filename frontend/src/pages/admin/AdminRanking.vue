<template>
  <div class="page-wrapper">
    <div class="container">
      <h1 class="section-title">Tournament Rankings</h1>

      <!-- Tournament Selector -->
      <div class="lol-card mb-4">
        <div class="flex gap-2" style="align-items:flex-end;flex-wrap:wrap;">
          <div class="form-group" style="flex:1;min-width:200px;margin-bottom:0;">
            <label class="form-label" for="ranking-tournament-select">Select Tournament</label>
            <select id="ranking-tournament-select" class="form-select" v-model="selectedId">
              <option value="" disabled>— Choose a tournament —</option>
              <option v-for="t in tournaments" :key="t.idTournament" :value="t.idTournament">
                {{ t.name }}
              </option>
            </select>
          </div>
          <button class="btn btn-gold" id="btn-load-rankings" @click="loadRankings" :disabled="!selectedId || loading">
            Load Rankings
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <template v-else-if="podium.length || rankings.length">
        <!-- Podium -->
        <h2 class="section-title">Podium</h2>
        <div class="podium" v-if="podium.length >= 3">
          <div class="podium-item podium-2">
            <div class="podium-name">{{ podium[1]?.archerName }}</div>
            <div class="podium-score">{{ podium[1]?.totalScore }} pts</div>
            <div class="podium-block">🥈</div>
            <div class="podium-rank">2nd Place</div>
          </div>
          <div class="podium-item podium-1">
            <div class="podium-name">{{ podium[0]?.archerName }}</div>
            <div class="podium-score">{{ podium[0]?.totalScore }} pts</div>
            <div class="podium-block">👑</div>
            <div class="podium-rank">Champion</div>
          </div>
          <div class="podium-item podium-3">
            <div class="podium-name">{{ podium[2]?.archerName }}</div>
            <div class="podium-score">{{ podium[2]?.totalScore }} pts</div>
            <div class="podium-block">🥉</div>
            <div class="podium-rank">3rd Place</div>
          </div>
        </div>

        <div class="gold-divider"></div>

        <!-- Full rankings table -->
        <h2 class="section-title">Full Standings</h2>
        <div class="lol-table-wrapper">
          <table class="lol-table" id="rankings-table">
            <thead>
              <tr>
                <th>Pos</th>
                <th>Archer</th>
                <th>Total Score</th>
                <th>Updated</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in rankings" :key="r.idRanking" :id="`ranking-row-${r.idRanking}`">
                <td>
                  <span class="rank-num" :class="r.position <= 3 ? `rank-${r.position}` : ''">
                    {{ r.position }}
                  </span>
                </td>
                <td>{{ r.archerName }}</td>
                <td class="text-gold-bright" style="font-family:'Cinzel',serif;font-weight:700;">
                  {{ r.totalScore }}
                </td>
                <td class="text-muted" style="font-size:0.78rem;">{{ formatDate(r.updatedAt) }}</td>
              </tr>
              <tr v-if="rankings.length === 0">
                <td colspan="4" class="text-center text-muted" style="padding:2rem;">
                  No rankings available for this tournament yet.
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
import { getTournaments, getPodium } from '@/api/tournaments'
import api from '@/api/axios'

const tournaments = ref([])
const selectedId  = ref('')
const podium      = ref([])
const rankings    = ref([])
const loading     = ref(false)

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

async function loadRankings() {
  if (!selectedId.value) return
  loading.value = true
  podium.value  = []
  rankings.value = []
  try {
    const [podiumRes, rankRes] = await Promise.all([
      getPodium(selectedId.value),
      api.get(`/tournaments/${selectedId.value}/rankings`).catch(() => ({ data: [] })),
    ])
    podium.value   = podiumRes.data
    rankings.value = rankRes.data
  } catch { /* ignore */ } finally { loading.value = false }
}

onMounted(async () => {
  try { const res = await getTournaments(); tournaments.value = res.data }
  catch { /* ignore */ }
})
</script>

<style scoped>
.rank-num { font-family: 'Cinzel', serif; font-weight: 700; }
.rank-1 { color: var(--lol-gold-bright); }
.rank-2 { color: #c0c0c0; }
.rank-3 { color: #cd7f32; }
</style>
