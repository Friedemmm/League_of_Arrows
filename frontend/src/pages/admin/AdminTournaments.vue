<template>
  <div class="page-wrapper admin-page">
    <div class="container">

      <!-- Page header -->
      <div class="admin-page-header">
        <button class="btn-back" id="btn-back-tournaments" @click="$router.push('/dashboard')">
          <span class="material-icons">arrow_back</span> Back
        </button>
        <div class="header-row">
          <div>
            <h1 class="page-title">
              <span class="material-icons page-title-icon">emoji_events</span>
              Gestionar Eventos
            </h1>
            <p class="page-subtitle">Crea y administra torneos: pasados, presentes y futuros.</p>
          </div>
          <button class="btn btn-gold" id="btn-add-tournament" @click="openCreate">
            <span class="material-icons btn-icon">add_circle</span> Nuevo Torneo
          </button>
        </div>
        <hr class="page-rule" />
      </div>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <div v-else class="lol-table-wrapper">
        <table class="lol-table" id="tournaments-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Categoría</th>
              <th>Inicio</th>
              <th>Fin</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in tournaments" :key="t.tournamentId" :id="`tournament-row-${t.tournamentId}`">
              <td class="text-muted">#{{ t.tournamentId }}</td>
              <td class="fw-bold">{{ t.name }}</td>
              <td><span class="badge badge-blue">{{ t.categoryName || '—' }}</span></td>
              <td>{{ formatDate(t.startDate) }}</td>
              <td>{{ formatDate(t.endDate) }}</td>
              <td>
                <span class="badge" :class="statusClass(t)">{{ statusLabel(t) }}</span>
              </td>
              <td>
                <div class="flex gap-1">
                  <!-- Inscribir arquero -->
                  <button class="btn btn-gold btn-sm icon-btn"
                    :id="`btn-inscribe-tournament-${t.tournamentId}`"
                    @click="openInscribe(t)"
                    title="Inscribir arquero">
                    <span class="material-icons">person_add</span>
                  </button>
                  <button class="btn btn-ghost btn-sm icon-btn"
                    :id="`btn-edit-tournament-${t.tournamentId}`" @click="openEdit(t)">
                    <span class="material-icons">edit</span>
                  </button>
                  <button class="btn btn-danger btn-sm icon-btn"
                    :id="`btn-delete-tournament-${t.tournamentId}`" @click="confirmDelete(t)">
                    <span class="material-icons">delete</span>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!loading && tournaments.length === 0">
              <td colspan="7" class="text-center text-muted" style="padding:2rem;">No se encontraron torneos.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- ── Inscribe Archer Modal ── -->
      <Transition name="fade">
        <div class="modal-overlay" v-if="showInscribeModal" @click.self="showInscribeModal = false">
          <div class="modal-box">
            <div class="modal-header">
              <h3>
                <span class="material-icons" style="vertical-align:middle;margin-right:0.4rem;color:var(--lol-gold);">person_add</span>
                Inscribir Arquero
              </h3>
              <button class="modal-close" @click="showInscribeModal = false">
                <span class="material-icons">close</span>
              </button>
            </div>
            <div class="modal-body">
              <Transition name="slide-up">
                <div class="alert alert-error" v-if="inscribeError">
                  <span class="material-icons">warning</span> {{ inscribeError }}
                </div>
              </Transition>
              <Transition name="slide-up">
                <div class="alert alert-success" v-if="inscribeSuccess">
                  <span class="material-icons">check_circle</span> {{ inscribeSuccess }}
                </div>
              </Transition>

              <p class="text-secondary" style="margin-bottom:1rem;">
                Torneo: <strong class="text-gold">{{ inscribingT?.name }}</strong>
              </p>

              <div class="form-group">
                <label class="form-label" for="inscribe-archer-select">Seleccionar Arquero</label>
                <select id="inscribe-archer-select" class="form-input" v-model.number="inscribeArcherId">
                  <option :value="null" disabled>— Selecciona un arquero —</option>
                  <option v-for="a in archers" :key="a.archerId" :value="a.archerId">
                    {{ a.name }} <span v-if="a.categoryName">({{ a.categoryName }})</span>
                  </option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showInscribeModal = false">Cancelar</button>
              <button class="btn btn-gold" id="btn-confirm-inscribe" @click="doInscribe"
                :disabled="saving || !inscribeArcherId">
                <span class="material-icons btn-icon">how_to_reg</span>
                {{ saving ? 'Inscribiendo...' : 'Inscribir' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>

      <!-- Create/Edit Modal -->
      <Transition name="fade">
        <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
          <div class="modal-box">
            <div class="modal-header">
              <h3>{{ editingT ? 'Editar Torneo' : 'Nuevo Torneo' }}</h3>
              <button class="modal-close" @click="showModal = false">
                <span class="material-icons">close</span>
              </button>
            </div>
            <div class="modal-body">
              <Transition name="slide-up">
                <div class="alert alert-error" v-if="modalError">
                  <span class="material-icons">warning</span> {{ modalError }}
                </div>
              </Transition>
              <div class="form-group">
                <label class="form-label" for="t-name">Nombre del Torneo</label>
                <input id="t-name" class="form-input" v-model="form.name" placeholder="Nombre del torneo" />
              </div>
              <div class="form-group">
                <label class="form-label" for="t-category">Categoría</label>
                <select id="t-category" class="form-input" v-model.number="form.categoryId">
                  <option :value="null" disabled>Selecciona una categoría</option>
                  <option v-for="c in categories" :key="c.id_category ?? c.idCategory" :value="c.id_category ?? c.idCategory">{{ c.name }}</option>
                </select>
              </div>
              <div class="grid-2">
                <div class="form-group">
                  <label class="form-label" for="t-start">Fecha de Inicio</label>
                  <input id="t-start" class="form-input" type="date" v-model="form.startDate" />
                </div>
                <div class="form-group">
                  <label class="form-label" for="t-end">Fecha de Fin</label>
                  <input id="t-end" class="form-input" type="date" v-model="form.endDate" />
                </div>
              </div>
              <div class="form-group">
                <label class="form-label" style="display:flex;align-items:center;gap:0.5rem;cursor:pointer;">
                  <input type="checkbox" v-model="form.active" id="t-active" />
                  <span>Activo</span>
                </label>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showModal = false">Cancelar</button>
              <button class="btn btn-gold" id="btn-save-tournament" @click="saveTournament" :disabled="saving">
                {{ saving ? 'Guardando...' : 'Guardar' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>

      <!-- Delete Confirm -->
      <Transition name="fade">
        <div class="modal-overlay" v-if="showDeleteModal" @click.self="showDeleteModal = false">
          <div class="modal-box">
            <div class="modal-header">
              <h3>Eliminar Torneo</h3>
              <button class="modal-close" @click="showDeleteModal = false">
                <span class="material-icons">close</span>
              </button>
            </div>
            <div class="modal-body">
              <p class="text-secondary">¿Eliminar <strong class="text-gold">{{ deletingT?.name }}</strong>?
                Todos los datos asociados serán eliminados.</p>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showDeleteModal = false">Cancelar</button>
              <button class="btn btn-danger" id="btn-confirm-delete-tournament" @click="doDelete" :disabled="saving">
                {{ saving ? 'Eliminando...' : 'Eliminar' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getTournaments, createTournament, updateTournament, deleteTournament } from '@/api/tournaments'
import { getArchers } from '@/api/archers'
import api from '@/api/axios'

const tournaments     = ref([])
const categories      = ref([])
const archers         = ref([])
const loading         = ref(true)
const showModal       = ref(false)
const showDeleteModal = ref(false)
const showInscribeModal = ref(false)
const editingT        = ref(null)
const deletingT       = ref(null)
const inscribingT     = ref(null)
const inscribeArcherId = ref(null)
const inscribeError   = ref('')
const inscribeSuccess = ref('')
const saving          = ref(false)
const modalError      = ref('')

const form = reactive({ name: '', categoryId: null, startDate: '', endDate: '', active: true })

const today = new Date().toISOString().split('T')[0]

function statusLabel(t) {
  if (t.active) return 'Activo'
  if (t.endDate && t.endDate < today) return 'Finalizado'
  return 'Próximo'
}
function statusClass(t) {
  if (t.active) return 'badge-success'
  if (t.endDate && t.endDate < today) return 'badge-muted'
  return 'badge-blue'
}

function formatDate(d) {
  if (!d) return '—'
  return new Date(d + 'T00:00:00').toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

async function load() {
  loading.value = true
  try {
    const [tourRes, archerRes, catRes] = await Promise.all([
      getTournaments(),
      getArchers(),
      api.get('/categories'),
    ])
    tournaments.value = tourRes.data
    archers.value     = archerRes.data
    categories.value  = catRes.data
  } catch (e) {
    console.error('[AdminTournaments] load error:', e.message)
  }
  loading.value = false
}

function openCreate() {
  editingT.value = null
  Object.assign(form, { name: '', categoryId: null, startDate: '', endDate: '', active: true })
  modalError.value = ''
  showModal.value = true
}

function openEdit(t) {
  editingT.value = t
  Object.assign(form, {
    name:       t.name,
    categoryId: t.categoryId,
    startDate:  t.startDate,
    endDate:    t.endDate,
    active:     t.active,
  })
  modalError.value = ''
  showModal.value = true
}

function openInscribe(t) {
  inscribingT.value    = t
  inscribeArcherId.value = null
  inscribeError.value  = ''
  inscribeSuccess.value = ''
  showInscribeModal.value = true
}

function confirmDelete(t) { deletingT.value = t; showDeleteModal.value = true }

async function saveTournament() {
  modalError.value = ''
  saving.value = true
  try {
    if (editingT.value) await updateTournament(editingT.value.tournamentId, form)
    else await createTournament(form)
    showModal.value = false
    await load()
  } catch (e) {
    modalError.value = e.response?.data?.error || 'Failed to save tournament.'
  } finally { saving.value = false }
}

async function doDelete() {
  saving.value = true
  try {
    await deleteTournament(deletingT.value.tournamentId)
    showDeleteModal.value = false
    await load()
  } catch { /* ignore */ } finally { saving.value = false }
}

async function doInscribe() {
  inscribeError.value   = ''
  inscribeSuccess.value = ''
  saving.value = true
  try {
    await api.post('/inscriptions', {
      archerId:     inscribeArcherId.value,
      tournamentId: inscribingT.value.tournamentId,
    })
    const archerName = archers.value.find(a => a.archerId === inscribeArcherId.value)?.name || ''
    inscribeSuccess.value = `${archerName} inscrito en ${inscribingT.value.name} exitosamente.`
    inscribeArcherId.value = null
  } catch (e) {
    inscribeError.value = e.response?.data?.error || 'No se pudo inscribir al arquero. ¿Ya estaba inscrito?'
  } finally { saving.value = false }
}

onMounted(load)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

.admin-page { padding: calc(var(--header-height, 70px) + 2rem) 0 4rem; min-height: 100vh; }
.admin-page-header { margin-bottom: 1.5rem; }

.btn-back {
  display: inline-flex; align-items: center; gap: 0.3rem;
  background: none; border: none; color: var(--text-muted); cursor: pointer;
  font-family: 'Cinzel', serif; font-size: 0.7rem; text-transform: uppercase;
  letter-spacing: 0.1em; padding: 0; margin-bottom: 1rem; transition: color 0.2s;
}
.btn-back:hover { color: var(--lol-gold); }
.btn-back .material-icons { font-size: 1rem; }

.header-row { display: flex; align-items: flex-end; justify-content: space-between; gap: 1rem; flex-wrap: wrap; }

.page-title {
  font-size: 1.5rem; font-family: 'Cinzel', serif;
  display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.3rem;
}
.page-title-icon { font-size: 1.3rem; color: var(--lol-gold); }
.page-subtitle { font-size: 0.82rem; color: var(--text-muted); margin: 0; }
.page-rule { margin: 1rem 0 1.5rem; }

.icon-btn { display: inline-flex; align-items: center; padding: 0.3rem 0.5rem; }
.icon-btn .material-icons { font-size: 1rem; }
.btn-icon { display: inline-flex; align-items: center; gap: 0.3rem; }
.fw-bold { font-weight: 600; }

.alert-success {
  display: flex; align-items: center; gap: 0.5rem;
  background: rgba(10,200,185,0.12); border: 1px solid rgba(10,200,185,0.3);
  color: #0ac8b9; border-radius: 6px; padding: 0.6rem 0.9rem;
  font-size: 0.82rem; margin-bottom: 1rem;
}
</style>
