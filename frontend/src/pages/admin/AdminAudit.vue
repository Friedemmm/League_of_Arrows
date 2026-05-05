<template>
  <div class="page-wrapper">
    <div class="container">
      <h1 class="section-title">Audit Log</h1>
      <p class="text-secondary mb-4" style="margin-top:-1rem;">
        Records of all score modifications made by administrators.
      </p>

      <!-- Filters -->
      <div class="lol-card mb-3">
        <div class="flex gap-2" style="flex-wrap:wrap;">
          <div class="form-group" style="flex:1;min-width:160px;margin-bottom:0;">
            <label class="form-label" for="audit-filter-archer">Archer ID</label>
            <input id="audit-filter-archer" class="form-input" type="number" v-model.number="filterArcherId" placeholder="Filter by archer..." />
          </div>
          <div class="form-group" style="flex:1;min-width:160px;margin-bottom:0;">
            <label class="form-label" for="audit-filter-tournament">Tournament ID</label>
            <input id="audit-filter-tournament" class="form-input" type="number" v-model.number="filterTournamentId" placeholder="Filter by tournament..." />
          </div>
          <div style="display:flex;align-items:flex-end;">
            <button class="btn btn-ghost btn-sm" id="btn-clear-audit-filter" @click="clearFilters">Clear</button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <div v-else class="lol-table-wrapper">
        <table class="lol-table" id="audit-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Archer</th>
              <th>Tournament</th>
              <th>Old Score</th>
              <th>New Score</th>
              <th>Modified By</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in filtered" :key="log.idAudit" :id="`audit-row-${log.idAudit}`">
              <td class="text-muted">#{{ log.idAudit }}</td>
              <td>{{ log.idArcher }}</td>
              <td>{{ log.idTournament }}</td>
              <td><span class="badge badge-muted">{{ log.oldScore }}</span></td>
              <td>
                <span class="badge" :class="log.newScore > log.oldScore ? 'badge-success' : 'badge-danger'">
                  {{ log.newScore }}
                </span>
                <span class="change-arrow" :class="log.newScore > log.oldScore ? 'text-success' : 'text-danger'">
                  {{ log.newScore > log.oldScore ? '↑' : '↓' }}
                </span>
              </td>
              <td class="text-secondary">Admin #{{ log.modifiedBy }}</td>
              <td class="text-muted" style="font-size:0.78rem;">{{ formatDate(log.modifiedAt) }}</td>
            </tr>
            <tr v-if="filtered.length === 0">
              <td colspan="7" class="text-center text-muted" style="padding:2rem;">No audit records found.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/api/axios'

const auditLogs        = ref([])
const loading          = ref(true)
const filterArcherId   = ref(null)
const filterTournamentId = ref(null)

const filtered = computed(() => {
  let data = auditLogs.value
  if (filterArcherId.value)   data = data.filter(l => l.idArcher === filterArcherId.value)
  if (filterTournamentId.value) data = data.filter(l => l.idTournament === filterTournamentId.value)
  return data
})

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function clearFilters() { filterArcherId.value = null; filterTournamentId.value = null }

onMounted(async () => {
  try {
    const res = await api.get('/audit')
    auditLogs.value = res.data
  } catch { /* endpoint may not be exposed; show empty */ } finally { loading.value = false }
})
</script>

<style scoped>
.change-arrow { font-size: 0.9rem; margin-left: 4px; }
</style>
