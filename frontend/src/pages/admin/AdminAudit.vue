<template>
  <div class="page-wrapper admin-page">
    <div class="container">

      <!-- ── Page header ── -->
      <div class="admin-page-header">
        <button class="btn-back" id="btn-back-audit" @click="$router.push('/dashboard')">
          <span class="material-icons">arrow_back</span> Volver
        </button>
        <h1 class="page-title">
          <span class="material-icons page-title-icon">policy</span>
          Registro de Auditoría
        </h1>
        <p class="page-subtitle">Registro de solo lectura de todas las modificaciones de puntajes realizadas por administradores.</p>
        <hr class="page-rule" />
      </div>

      <!-- Filters -->
      <div class="lol-card mb-3">
        <div class="flex gap-2" style="flex-wrap:wrap;align-items:flex-end;">
          <div class="form-group" style="flex:1;min-width:180px;margin-bottom:0;">
            <label class="form-label" for="audit-filter-archer">
              <span class="material-icons" style="font-size:0.9rem;vertical-align:middle;">person_search</span>
              Filtrar por Arquero
            </label>
            <select id="audit-filter-archer" class="form-input" v-model.number="filterArcherId">
              <option :value="null">Todos los arqueros</option>
              <option v-for="a in archers" :key="a.archerId" :value="a.archerId">{{ a.name }}</option>
            </select>
          </div>
          <div class="form-group" style="flex:1;min-width:180px;margin-bottom:0;">
            <label class="form-label" for="audit-filter-tournament">
              <span class="material-icons" style="font-size:0.9rem;vertical-align:middle;">search</span>
              Filtrar por Torneo
            </label>
            <select id="audit-filter-tournament" class="form-input" v-model.number="filterTournamentId">
              <option :value="null">Todos los torneos</option>
              <option v-for="t in tournaments" :key="t.tournamentId" :value="t.tournamentId">{{ t.name }}</option>
            </select>
          </div>
          <div>
            <button class="btn btn-ghost btn-sm" id="btn-clear-audit-filter" @click="clearFilters">
              <span class="material-icons" style="font-size:1rem;">filter_alt_off</span> Limpiar
            </button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <div v-else class="lol-table-wrapper">
        <table class="lol-table" id="audit-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Arquero</th>
              <th>Torneo</th>
              <th>Puntaje Anterior</th>
              <th>Puntaje Nuevo</th>
              <th>Modificado Por</th>
              <th>Fecha</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in filtered" :key="log.idAudit" :id="`audit-row-${log.idAudit}`">
              <td class="text-muted">#{{ log.idAudit }}</td>
              <td>{{ log.archerName || `#${log.idArcher}` }}</td>
              <td>{{ log.tournamentName || `#${log.idTournament}` }}</td>
              <td><span class="badge badge-muted">{{ log.oldScore }}</span></td>
              <td>
                <span class="badge" :class="log.newScore > log.oldScore ? 'badge-success' : 'badge-danger'">
                  {{ log.newScore }}
                </span>
                <span class="change-arrow" :class="log.newScore > log.oldScore ? 'text-success' : 'text-danger'">
                  <span class="material-icons" style="font-size:0.9rem;vertical-align:middle;">
                    {{ log.newScore > log.oldScore ? 'arrow_upward' : 'arrow_downward' }}
                  </span>
                </span>
              </td>
              <td class="text-secondary">Admin #{{ log.modifiedBy }}</td>
              <td class="text-muted" style="font-size:0.78rem;">{{ formatDate(log.modifiedAt) }}</td>
            </tr>
            <tr v-if="filtered.length === 0">
              <td colspan="7" class="text-center text-muted" style="padding:2rem;">No se encontraron registros de auditoría.</td>
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
import { getArchers } from '@/api/archers'
import { getTournaments } from '@/api/tournaments'

const auditLogs         = ref([])
const archers           = ref([])
const tournaments       = ref([])
const loading           = ref(true)
const filterArcherId    = ref(null)
const filterTournamentId = ref(null)

const filtered = computed(() => {
  let data = auditLogs.value
  if (filterArcherId.value)     data = data.filter(l => l.idArcher === filterArcherId.value)
  if (filterTournamentId.value) data = data.filter(l => l.idTournament === filterTournamentId.value)
  return data
})

const archerName     = (id) => archers.value.find(a => a.archerId === id)?.name     || `#${id}`
const tournamentName = (id) => tournaments.value.find(t => t.tournamentId === id)?.name || `#${id}`

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function clearFilters() { filterArcherId.value = null; filterTournamentId.value = null }

onMounted(async () => {
  try {
    const [auditRes, archerRes, tourRes] = await Promise.all([
      api.get('/audit'),
      getArchers(),
      getTournaments(),
    ])
    auditLogs.value   = auditRes.data   // now has archerName & tournamentName embedded
    archers.value     = archerRes.data
    tournaments.value = tourRes.data
  } catch (e) {
    console.error('[AdminAudit] load error:', e.message)
  } finally { loading.value = false }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

.admin-page { padding: calc(var(--header-height) + 2rem) 0 4rem; min-height: 100vh; }
.admin-page-header { margin-bottom: 1.5rem; }

.btn-back {
  display: inline-flex; align-items: center; gap: 0.3rem;
  background: none; border: none; color: var(--text-muted); cursor: pointer;
  font-family: 'Cinzel', serif; font-size: 0.7rem; text-transform: uppercase;
  letter-spacing: 0.1em; padding: 0; margin-bottom: 1rem; transition: color 0.2s;
}
.btn-back:hover { color: var(--lol-gold); }
.btn-back .material-icons { font-size: 1rem; }

.page-title {
  font-size: 1.6rem; font-family: 'Cinzel', serif;
  display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.3rem;
}
.page-title-icon { font-size: 1.4rem; color: var(--lol-gold); }
.page-subtitle { font-size: 0.82rem; color: var(--text-muted); margin: 0; }
.page-rule { margin: 1rem 0 0; background: rgba(200,170,110,0.4); }

.change-arrow { margin-left: 2px; }
</style>
