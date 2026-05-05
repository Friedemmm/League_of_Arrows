<template>
  <div class="page-wrapper">
    <div class="container" style="max-width:700px;">
      <div class="flex-between mb-4">
        <h1 class="section-title" style="margin-bottom:0;">Categories</h1>
        <button class="btn btn-gold" id="btn-add-category" @click="openCreate">+ New Category</button>
      </div>

      <div v-if="loading" class="loading-center"><div class="spinner"></div></div>

      <div v-else class="lol-table-wrapper">
        <table class="lol-table" id="categories-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in categories" :key="c.idCategory" :id="`category-row-${c.idCategory}`">
              <td class="text-muted">#{{ c.idCategory }}</td>
              <td><span class="badge badge-blue">{{ c.name }}</span></td>
              <td>
                <div class="flex gap-1">
                  <button class="btn btn-ghost btn-sm" :id="`btn-edit-category-${c.idCategory}`" @click="openEdit(c)">Edit</button>
                  <button class="btn btn-danger btn-sm" :id="`btn-delete-category-${c.idCategory}`" @click="confirmDelete(c)">Delete</button>
                </div>
              </td>
            </tr>
            <tr v-if="categories.length === 0">
              <td colspan="3" class="text-center text-muted" style="padding:2rem;">No categories yet.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Create/Edit Modal -->
      <Transition name="fade">
        <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
          <div class="modal-box">
            <div class="modal-header">
              <h3>{{ editingCat ? 'Edit Category' : 'New Category' }}</h3>
              <button class="modal-close" @click="showModal = false">✕</button>
            </div>
            <div class="modal-body">
              <Transition name="slide-up">
                <div class="alert alert-error" v-if="modalError">⚠ {{ modalError }}</div>
              </Transition>
              <div class="form-group">
                <label class="form-label" for="cat-name">Category Name</label>
                <input id="cat-name" class="form-input" v-model="catName" placeholder="e.g. Recurve Open" />
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showModal = false">Cancel</button>
              <button class="btn btn-gold" id="btn-save-category" @click="saveCategory" :disabled="saving">
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
              <h3>Delete Category</h3>
              <button class="modal-close" @click="showDeleteModal = false">✕</button>
            </div>
            <div class="modal-body">
              <p class="text-secondary">Delete category <strong class="text-gold">{{ deletingCat?.name }}</strong>?
              Archers/tournaments in this category will be unlinked.</p>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showDeleteModal = false">Cancel</button>
              <button class="btn btn-danger" id="btn-confirm-delete-category" @click="doDelete" :disabled="saving">
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
import { ref, onMounted } from 'vue'
import api from '@/api/axios'

const categories     = ref([])
const loading        = ref(true)
const showModal      = ref(false)
const showDeleteModal = ref(false)
const editingCat     = ref(null)
const deletingCat    = ref(null)
const catName        = ref('')
const saving         = ref(false)
const modalError     = ref('')

async function load() {
  try { const res = await api.get('/categories'); categories.value = res.data }
  catch { /* ignore */ } finally { loading.value = false }
}

function openCreate() { editingCat.value = null; catName.value = ''; modalError.value = ''; showModal.value = true }
function openEdit(c)  { editingCat.value = c; catName.value = c.name; modalError.value = ''; showModal.value = true }
function confirmDelete(c) { deletingCat.value = c; showDeleteModal.value = true }

async function saveCategory() {
  modalError.value = ''
  saving.value = true
  try {
    if (editingCat.value) await api.put(`/categories/${editingCat.value.idCategory}`, { name: catName.value })
    else await api.post('/categories', { name: catName.value })
    showModal.value = false
    await load()
  } catch (e) {
    modalError.value = e.response?.data?.error || 'Failed to save category.'
  } finally { saving.value = false }
}

async function doDelete() {
  saving.value = true
  try {
    await api.delete(`/categories/${deletingCat.value.idCategory}`)
    showDeleteModal.value = false
    await load()
  } catch { /* ignore */ } finally { saving.value = false }
}

onMounted(load)
</script>
