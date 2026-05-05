<template>
  <div class="page-wrapper">
    <div class="container">
      <div class="flex-between mb-4">
        <h1 class="section-title" style="margin-bottom:0;">Manage Archers</h1>
        <button class="btn btn-gold" id="btn-add-archer" @click="openCreate">+ New Archer</button>
      </div>

      <!-- Search -->
      <div class="form-group mb-3" style="max-width:320px;">
        <input id="search-archers" class="form-input" type="text" v-model="search" placeholder="Search by name or email..." />
      </div>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <div v-else class="lol-table-wrapper">
        <table class="lol-table" id="archers-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Category</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in filtered" :key="a.idArcher" :id="`archer-row-${a.idArcher}`">
              <td class="text-muted">#{{ a.idArcher }}</td>
              <td>{{ a.name }}</td>
              <td class="text-secondary">{{ a.email }}</td>
              <td><span class="badge badge-blue">{{ a.categoryName || '—' }}</span></td>
              <td>
                <div class="flex gap-1">
                  <button class="btn btn-ghost btn-sm" :id="`btn-edit-archer-${a.idArcher}`" @click="openEdit(a)">Edit</button>
                  <button class="btn btn-danger btn-sm" :id="`btn-delete-archer-${a.idArcher}`" @click="confirmDelete(a)">Delete</button>
                </div>
              </td>
            </tr>
            <tr v-if="filtered.length === 0">
              <td colspan="5" class="text-center text-muted" style="padding:2rem;">No archers found.</td>
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
              <button class="modal-close" @click="showModal = false">✕</button>
            </div>
            <div class="modal-body">
              <Transition name="slide-up">
                <div class="alert alert-error" v-if="modalError">⚠ {{ modalError }}</div>
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
                <label class="form-label" for="archer-category">Category ID</label>
                <input id="archer-category" class="form-input" type="number" v-model.number="form.categoryId" placeholder="Category ID" />
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
              <button class="modal-close" @click="showDeleteModal = false">✕</button>
            </div>
            <div class="modal-body">
              <p class="text-secondary">Are you sure you want to delete <strong class="text-gold">{{ deletingArcher?.name }}</strong>? This cannot be undone.</p>
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

const archers       = ref([])
const loading       = ref(true)
const search        = ref('')
const showModal     = ref(false)
const showDeleteModal = ref(false)
const editingArcher = ref(null)
const deletingArcher = ref(null)
const saving        = ref(false)
const modalError    = ref('')

const form = reactive({ name: '', email: '', password: '', categoryId: null })

const filtered = computed(() => {
  if (!search.value) return archers.value
  const q = search.value.toLowerCase()
  return archers.value.filter(a =>
    a.name?.toLowerCase().includes(q) || a.email?.toLowerCase().includes(q)
  )
})

async function load() {
  try {
    const res = await getArchers()
    archers.value = res.data
  } catch { /* ignore */ } finally { loading.value = false }
}

function openCreate() {
  editingArcher.value = null
  Object.assign(form, { name: '', email: '', password: '', categoryId: null })
  modalError.value = ''
  showModal.value = true
}

function openEdit(a) {
  editingArcher.value = a
  Object.assign(form, { name: a.name, email: a.email, password: '', categoryId: a.categoryId })
  modalError.value = ''
  showModal.value = true
}

function confirmDelete(a) {
  deletingArcher.value = a
  showDeleteModal.value = true
}

async function saveArcher() {
  modalError.value = ''
  saving.value = true
  try {
    if (editingArcher.value) {
      await updateArcher(editingArcher.value.idArcher, form)
    } else {
      await createArcher(form)
    }
    showModal.value = false
    await load()
  } catch (e) {
    modalError.value = e.response?.data?.error || 'Failed to save archer.'
  } finally { saving.value = false }
}

async function doDelete() {
  saving.value = true
  try {
    await deleteArcher(deletingArcher.value.idArcher)
    showDeleteModal.value = false
    await load()
  } catch { /* ignore */ } finally { saving.value = false }
}

onMounted(load)
</script>
