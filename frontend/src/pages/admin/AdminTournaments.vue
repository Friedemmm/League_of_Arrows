<template>
  <div class="page-wrapper">
    <div class="container">
      <div class="flex-between mb-4">
        <h1 class="section-title" style="margin-bottom:0;">Manage Tournaments</h1>
        <button class="btn btn-gold" id="btn-add-tournament" @click="openCreate">+ New Tournament</button>
      </div>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <div v-else class="lol-table-wrapper">
        <table class="lol-table" id="tournaments-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Category</th>
              <th>Start</th>
              <th>End</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in tournaments" :key="t.idTournament" :id="`tournament-row-${t.idTournament}`">
              <td class="text-muted">#{{ t.idTournament }}</td>
              <td>{{ t.name }}</td>
              <td><span class="badge badge-blue">{{ t.categoryName || t.idCategory }}</span></td>
              <td>{{ formatDate(t.startDate) }}</td>
              <td>{{ formatDate(t.endDate) }}</td>
              <td>
                <span class="badge" :class="t.active ? 'badge-success' : 'badge-muted'">
                  {{ t.active ? 'Active' : 'Ended' }}
                </span>
              </td>
              <td>
                <div class="flex gap-1">
                  <button class="btn btn-ghost btn-sm" :id="`btn-edit-tournament-${t.idTournament}`" @click="openEdit(t)">Edit</button>
                  <button class="btn btn-danger btn-sm" :id="`btn-delete-tournament-${t.idTournament}`" @click="confirmDelete(t)">Delete</button>
                </div>
              </td>
            </tr>
            <tr v-if="tournaments.length === 0">
              <td colspan="7" class="text-center text-muted" style="padding:2rem;">No tournaments found.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Create/Edit Modal -->
      <Transition name="fade">
        <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
          <div class="modal-box">
            <div class="modal-header">
              <h3>{{ editingT ? 'Edit Tournament' : 'New Tournament' }}</h3>
              <button class="modal-close" @click="showModal = false">✕</button>
            </div>
            <div class="modal-body">
              <Transition name="slide-up">
                <div class="alert alert-error" v-if="modalError">⚠ {{ modalError }}</div>
              </Transition>
              <div class="form-group">
                <label class="form-label" for="t-name">Tournament Name</label>
                <input id="t-name" class="form-input" v-model="form.name" placeholder="Tournament name" />
              </div>
              <div class="form-group">
                <label class="form-label" for="t-category">Category ID</label>
                <input id="t-category" class="form-input" type="number" v-model.number="form.categoryId" placeholder="Category ID" />
              </div>
              <div class="grid-2">
                <div class="form-group">
                  <label class="form-label" for="t-start">Start Date</label>
                  <input id="t-start" class="form-input" type="date" v-model="form.startDate" />
                </div>
                <div class="form-group">
                  <label class="form-label" for="t-end">End Date</label>
                  <input id="t-end" class="form-input" type="date" v-model="form.endDate" />
                </div>
              </div>
              <div class="form-group">
                <label class="form-label" style="display:flex;align-items:center;gap:0.5rem;cursor:pointer;">
                  <input type="checkbox" v-model="form.active" id="t-active" />
                  <span>Active</span>
                </label>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showModal = false">Cancel</button>
              <button class="btn btn-gold" id="btn-save-tournament" @click="saveTournament" :disabled="saving">
                {{ saving ? 'Saving...' : 'Save' }}
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
              <h3>Delete Tournament</h3>
              <button class="modal-close" @click="showDeleteModal = false">✕</button>
            </div>
            <div class="modal-body">
              <p class="text-secondary">Delete <strong class="text-gold">{{ deletingT?.name }}</strong>? All associated data will be removed.</p>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showDeleteModal = false">Cancel</button>
              <button class="btn btn-danger" id="btn-confirm-delete-tournament" @click="doDelete" :disabled="saving">
                {{ saving ? 'Deleting...' : 'Delete' }}
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

const tournaments    = ref([])
const loading        = ref(true)
const showModal      = ref(false)
const showDeleteModal = ref(false)
const editingT       = ref(null)
const deletingT      = ref(null)
const saving         = ref(false)
const modalError     = ref('')

const form = reactive({ name: '', categoryId: null, startDate: '', endDate: '', active: true })

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

async function load() {
  try { const res = await getTournaments(); tournaments.value = res.data }
  catch { /* ignore */ } finally { loading.value = false }
}

function openCreate() {
  editingT.value = null
  Object.assign(form, { name: '', categoryId: null, startDate: '', endDate: '', active: true })
  modalError.value = ''
  showModal.value = true
}

function openEdit(t) {
  editingT.value = t
  Object.assign(form, { name: t.name, categoryId: t.idCategory, startDate: t.startDate, endDate: t.endDate, active: t.active })
  modalError.value = ''
  showModal.value = true
}

function confirmDelete(t) { deletingT.value = t; showDeleteModal.value = true }

async function saveTournament() {
  modalError.value = ''
  saving.value = true
  try {
    if (editingT.value) await updateTournament(editingT.value.idTournament, form)
    else await createTournament(form)
    showModal.value = false
    await load()
  } catch (e) {
    modalError.value = e.response?.data?.error || 'Failed to save tournament.'
  } finally { saving.value = false }
}

async function doDelete() {
  saving.value = true
  try { await deleteTournament(deletingT.value.idTournament); showDeleteModal.value = false; await load() }
  catch { /* ignore */ } finally { saving.value = false }
}

onMounted(load)
</script>
