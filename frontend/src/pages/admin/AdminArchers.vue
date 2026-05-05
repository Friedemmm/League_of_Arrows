<template>
  <div class="page-wrapper admin-page">
    <div class="container">

      <!-- Page header -->
      <div class="admin-page-header">
        <button class="btn-back" id="btn-back-archers" @click="$router.back()">
          <span class="material-icons">arrow_back</span> Back
        </button>
        <div class="header-row">
          <div>
            <h1 class="page-title">
              <span class="material-icons page-title-icon">group</span>
              Manage Archers
            </h1>
            <p class="page-subtitle">Create, edit and remove archer profiles.</p>
          </div>
          <button class="btn btn-gold" id="btn-add-archer" @click="openCreate">
            <span class="material-icons btn-icon">person_add</span> New Archer
          </button>
        </div>
        <hr class="page-rule" />
      </div>

      <!-- Search -->
      <div class="search-bar mb-3">
        <span class="material-icons search-icon">search</span>
        <input id="search-archers" class="form-input search-input" type="text"
          v-model="search" placeholder="Search by name..." />
      </div>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <div v-else class="lol-table-wrapper">
        <table class="lol-table" id="archers-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Category</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in filtered" :key="a.archerId" :id="`archer-row-${a.archerId}`">
              <td class="text-muted">#{{ a.archerId }}</td>
              <td class="fw-bold">{{ a.name }}</td>
              <td><span class="badge badge-blue">{{ categoryLabel(a) }}</span></td>
              <td>
                <div class="flex gap-1">
                  <button class="btn btn-ghost btn-sm icon-btn"
                    :id="`btn-edit-archer-${a.archerId}`" @click="openEdit(a)">
                    <span class="material-icons">edit</span>
                  </button>
                  <button class="btn btn-danger btn-sm icon-btn"
                    :id="`btn-delete-archer-${a.archerId}`" @click="confirmDelete(a)">
                    <span class="material-icons">delete</span>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!loading && filtered.length === 0">
              <td colspan="4" class="text-center text-muted" style="padding:2rem;">No archers found.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Create/Edit Modal -->
      <Transition name="fade">
        <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
          <div class="modal-box">
            <div class="modal-header">
              <h3>{{ editingArcher ? 'Edit Archer' : 'New Archer' }}</h3>
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
                <label class="form-label" for="archer-name">Name</label>
                <input id="archer-name" class="form-input" v-model="form.name" placeholder="Full name" />
              </div>
              <div class="form-group" v-if="!editingArcher">
                <label class="form-label" for="archer-email">Email</label>
                <input id="archer-email" class="form-input" type="email" v-model="form.email" placeholder="email@example.com" />
              </div>
              <div class="form-group" v-if="!editingArcher">
                <label class="form-label" for="archer-password">Password</label>
                <input id="archer-password" class="form-input" type="password" v-model="form.password" placeholder="••••••••" />
              </div>
              <div class="form-group">
                <label class="form-label" for="archer-category">Category</label>
                <select id="archer-category" class="form-input" v-model.number="form.categoryId">
                  <option :value="null" disabled>Select a category</option>
                  <option v-for="c in categories" :key="c.id_category ?? c.idCategory" :value="c.id_category ?? c.idCategory">{{ c.name }}</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showModal = false">Cancel</button>
              <button class="btn btn-gold" id="btn-save-archer" @click="saveArcher" :disabled="saving">
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
              <h3>Delete Archer</h3>
              <button class="modal-close" @click="showDeleteModal = false">
                <span class="material-icons">close</span>
              </button>
            </div>
            <div class="modal-body">
              <p class="text-secondary">Are you sure you want to delete
                <strong class="text-gold">{{ deletingArcher?.name }}</strong>? This cannot be undone.
              </p>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showDeleteModal = false">Cancel</button>
              <button class="btn btn-danger" id="btn-confirm-delete-archer" @click="doDelete" :disabled="saving">
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
import { ref, computed, onMounted, reactive } from 'vue'
import { getArchers, createArcher, updateArcher, deleteArcher } from '@/api/archers'
import api from '@/api/axios'

const archers         = ref([])
const categories      = ref([])
const loading         = ref(true)
const search          = ref('')
const showModal       = ref(false)
const showDeleteModal = ref(false)
const editingArcher   = ref(null)
const deletingArcher  = ref(null)
const saving          = ref(false)
const modalError      = ref('')

const form = reactive({ name: '', email: '', password: '', categoryId: null })

const filtered = computed(() => {
  if (!search.value) return archers.value
  const q = search.value.toLowerCase()
  return archers.value.filter(a => a.name?.toLowerCase().includes(q))
})

// Resolve category name: prefer the enriched field from backend JOIN,
// then look up from the categories list, then fall back to showing the ID
function categoryLabel(archer) {
  if (archer.categoryName) return archer.categoryName
  const cat = categories.value.find(c => (c.id_category ?? c.idCategory) === archer.categoryId)
  if (cat) return cat.name
  const fallback = { 1: 'Largo', 2: 'Recurvo', 3: 'Compuesto', 4: 'Tradicional' }
  return fallback[archer.categoryId] ?? (archer.categoryId ? `#${archer.categoryId}` : '—')
}

async function load() {
  loading.value = true
  // Load archers and categories independently so one failure doesn't block the other
  try {
    const res = await getArchers()
    archers.value = res.data
  } catch (e) {
    console.error('[AdminArchers] failed to load archers:', e.message)
  }
  try {
    const res = await api.get('/categories')
    categories.value = res.data   // [{idCategory, name}, ...]
  } catch (e) {
    console.warn('[AdminArchers] categories unavailable:', e.message)
  }
  loading.value = false
}

function openCreate() {
  editingArcher.value = null
  Object.assign(form, { name: '', email: '', password: '', categoryId: null })
  modalError.value = ''
  showModal.value = true
}

function openEdit(a) {
  editingArcher.value = a
  Object.assign(form, { name: a.name, email: a.email ?? '', password: '', categoryId: a.categoryId })
  modalError.value = ''
  showModal.value = true
}

function confirmDelete(a) { deletingArcher.value = a; showDeleteModal.value = true }

async function saveArcher() {
  modalError.value = ''
  saving.value = true
  try {
    if (editingArcher.value) await updateArcher(editingArcher.value.archerId, form)
    else await createArcher(form)
    showModal.value = false
    await load()
  } catch (e) {
    modalError.value = e.response?.data?.error || 'Failed to save archer.'
  } finally { saving.value = false }
}

async function doDelete() {
  saving.value = true
  try {
    await deleteArcher(deletingArcher.value.archerId)
    showDeleteModal.value = false
    await load()
  } catch { /* ignore */ } finally { saving.value = false }
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

.search-bar { display: flex; align-items: center; gap: 0.5rem; max-width: 340px; }
.search-icon { color: var(--text-muted); font-size: 1.1rem; }
.search-input { flex: 1; }

.icon-btn { display: inline-flex; align-items: center; padding: 0.3rem 0.5rem; }
.icon-btn .material-icons { font-size: 1rem; }
.btn-icon { display: inline-flex; align-items: center; gap: 0.3rem; }
.fw-bold { font-weight: 600; }
</style>
