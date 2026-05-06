<template>
  <div class="page-wrapper admin-page">
    <div class="container" style="max-width:860px;">

      <div class="admin-page-header">
        <button class="btn-back" id="btn-back-scoring" @click="$router.push('/dashboard')">
          <span class="material-icons">arrow_back</span> Volver
        </button>
        <h1 class="page-title">
          <span class="material-icons page-title-icon">sports_score</span>
          Registro de Puntajes
        </h1>
        <p class="page-subtitle">Registra o edita los puntajes de rondas para arqueros en torneos.</p>
        <hr class="page-rule" />
      </div>

      <!-- TABS -->
      <div class="mode-tabs">
        <button class="mode-tab" :class="{ active: mode === 'register' }" id="tab-register" @click="mode = 'register'">
          <span class="material-icons">add_circle</span> Registrar Ronda
        </button>
        <button class="mode-tab" :class="{ active: mode === 'edit' }" id="tab-edit" @click="mode = 'edit'">
          <span class="material-icons">edit</span> Editar Puntajes
        </button>
      </div>

      <!-- ══ TAB 1: REGISTER ══ -->
      <div class="lol-card" v-show="mode === 'register'">
        <Transition name="slide-up">
          <div class="alert alert-error" v-if="error"><span class="material-icons">warning</span> {{ error }}</div>
        </Transition>
        <Transition name="slide-up">
          <div class="alert alert-success" v-if="success"><span class="material-icons">check_circle</span> {{ success }}</div>
        </Transition>
        <div v-if="loadingData" class="loading-center" style="padding:2rem 0;"><div class="spinner"></div></div>
        <template v-else>
          <div class="grid-2">
            <div class="form-group">
              <label class="form-label" for="scoring-tournament">Torneo</label>
              <select id="scoring-tournament" class="form-input" v-model.number="form.tournamentId">
                <option :value="null" disabled>Selecciona un torneo</option>
                <option v-for="t in activeTournaments" :key="t.tournamentId" :value="t.tournamentId">{{ t.name }}</option>
              </select>
              <span v-if="activeTournaments.length === 0" class="hint-text">No hay torneos activos.</span>
            </div>
            <div class="form-group">
              <label class="form-label" for="scoring-archer">Arquero</label>
              <select id="scoring-archer" class="form-input" v-model.number="form.archerId">
                <option :value="null" disabled>Selecciona un arquero</option>
                <option v-for="a in archers" :key="a.archerId" :value="a.archerId">{{ a.name }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label" for="scoring-round">Número de Ronda</label>
            <input id="scoring-round" class="form-input" type="number" v-model.number="form.roundNumber" placeholder="Ronda (ej. 1)" min="1" style="max-width:160px;" />
          </div>
          <div class="form-group">
            <label class="form-label">Puntajes de Flechas (0–10 por flecha)</label>
            <div class="arrow-inputs">
              <input v-for="(_, i) in arrowCount" :key="i" :id="`arrow-score-${i+1}`"
                class="form-input arrow-score-input" type="text" inputmode="numeric" maxlength="2"
                :placeholder="`#${i+1}`" :value="displayArrow(form.arrows[i])"
                @keydown="onArrowKeydown(i, $event)" @input="onArrowInput(i, $event.target.value)" />
            </div>
            <div class="arrow-controls mt-1">
              <button class="btn btn-ghost btn-sm" id="btn-add-arrow" @click="addArrow"><span class="material-icons" style="font-size:1rem;">add</span> Flecha</button>
              <button class="btn btn-ghost btn-sm" id="btn-remove-arrow" @click="removeArrow" :disabled="arrowCount <= 1"><span class="material-icons" style="font-size:1rem;">remove</span> Flecha</button>
            </div>
          </div>
          <div class="score-preview lol-card mt-2" v-if="hasScores">
            <div class="preview-row">
              <span class="preview-label">Total de Ronda</span>
              <span class="preview-value">{{ roundTotal }}</span>
            </div>
            <div class="arrow-bubbles">
              <span v-for="(s, i) in form.arrows" :key="i" class="arrow-bubble" :class="bubbleClass(s)">{{ displayArrow(s) || '?' }}</span>
            </div>
          </div>
          <div class="modal-footer" style="padding:1.5rem 0 0;">
            <button class="btn btn-ghost" @click="resetForm" id="btn-reset-scoring"><span class="material-icons btn-icon">restart_alt</span> Limpiar</button>
            <button class="btn btn-gold" @click="submitScore" :disabled="submitting" id="btn-submit-score">
              <span class="material-icons btn-icon">send</span>{{ submitting ? 'Registrando...' : 'Registrar Ronda' }}
            </button>
          </div>
        </template>
      </div>

      <!-- ══ TAB 2: EDIT ══ -->
      <div class="lol-card" v-show="mode === 'edit'">
        <Transition name="slide-up">
          <div class="alert alert-error" v-if="editError"><span class="material-icons">warning</span> {{ editError }}</div>
        </Transition>
        <Transition name="slide-up">
          <div class="alert alert-success" v-if="editSuccess"><span class="material-icons">check_circle</span> {{ editSuccess }}</div>
        </Transition>

        <div class="edit-search-bar">
          <div class="form-group" style="flex:1;">
            <label class="form-label" for="edit-tournament">Torneo</label>
            <select id="edit-tournament" class="form-input" v-model.number="editQuery.tournamentId">
              <option :value="null" disabled>Selecciona un torneo</option>
              <option v-for="t in allTournaments" :key="t.tournamentId" :value="t.tournamentId">
                {{ t.name }}{{ t.active ? ' ✓' : '' }}
              </option>
            </select>
          </div>
          <div class="form-group" style="flex:1;">
            <label class="form-label" for="edit-archer">Arquero</label>
            <select id="edit-archer" class="form-input" v-model.number="editQuery.archerId">
              <option :value="null" disabled>Selecciona un arquero</option>
              <option v-for="a in archers" :key="a.archerId" :value="a.archerId">{{ a.name }}</option>
            </select>
          </div>
          <div class="form-group" style="flex:0 0 auto; align-self:flex-end;">
            <button class="btn btn-gold" id="btn-search-rounds"
              :disabled="!editQuery.tournamentId || !editQuery.archerId || searchingRounds"
              @click="loadRounds">
              <span class="material-icons btn-icon">search</span>{{ searchingRounds ? 'Buscando...' : 'Buscar Rondas' }}
            </button>
          </div>
        </div>

        <div v-if="searchingRounds" class="loading-center" style="padding:2rem;"><div class="spinner"></div></div>

        <div v-else-if="editRounds.length === 0 && hasSearched" class="empty-edit">
          <span class="material-icons" style="font-size:2rem;opacity:0.3;">search_off</span>
          <p>No se encontraron rondas para este arquero en este torneo.</p>
        </div>

        <div v-else-if="editRounds.length > 0" class="rounds-list">
          <div v-for="round in editRounds" :key="round.roundId" class="round-block lol-card" :id="`round-block-${round.roundId}`">
            <div class="round-header" @click="toggleRound(round.roundId)">
              <div class="round-header-left">
                <span class="material-icons round-icon">arrow_right</span>
                <span class="round-label">Ronda {{ round.roundNumber }}</span>
                <span class="round-total-badge">Total: {{ roundSum(round) }} pts</span>
                <span v-if="hasChangesInRound(round)" class="round-unsaved-badge">● Sin guardar</span>
              </div>
              <span class="material-icons expand-icon" :class="{ rotated: openRounds.has(round.roundId) }">expand_more</span>
            </div>

            <Transition name="expand">
              <div v-if="openRounds.has(round.roundId)" class="arrow-editor">
                <div class="arrow-editor-grid">
                  <div v-for="arrow in round.arrows" :key="arrow.arrowId" class="arrow-edit-cell" :id="`arrow-cell-${arrow.arrowId}`">
                    <label class="arrow-edit-label">F{{ arrow.arrowNumber }}</label>
                    <input class="form-input arrow-score-input" :class="{ 'score-changed': changedArrows.has(arrow.arrowId) }"
                      type="number" min="0" max="10" v-model.number="arrow.score" @input="markChanged(arrow)" />
                    <span class="arrow-bubble-mini" :class="bubbleClass(arrow.score)">{{ arrow.score }}</span>
                  </div>
                </div>
                <div class="round-footer">
                  <span class="round-new-total">Nuevo total: <strong>{{ roundSum(round) }}</strong> pts</span>
                  <button class="btn btn-gold btn-sm" :id="`btn-save-round-${round.roundId}`"
                    :disabled="!hasChangesInRound(round) || savingRound === round.roundId"
                    @click="saveRound(round)">
                    <span class="material-icons btn-icon" style="font-size:1rem;">save</span>
                    {{ savingRound === round.roundId ? 'Guardando...' : 'Guardar Cambios' }}
                  </button>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import api from '@/api/axios'
import { getArchers } from '@/api/archers'
import { getTournaments } from '@/api/tournaments'

const mode        = ref('register')
const loadingData = ref(true)
const archers           = ref([])
const activeTournaments = ref([])
const allTournaments    = ref([])

// ── TAB 1 ─────────────────────────────────────────────────────────────
const error      = ref('')
const success    = ref('')
const submitting = ref(false)
const arrowCount = ref(6)
const form = reactive({ tournamentId: null, archerId: null, roundNumber: 1, arrows: Array(6).fill(null) })

const hasScores  = computed(() => form.arrows.some(a => a !== null))
const roundTotal = computed(() => form.arrows.reduce((sum, v) => sum + (typeof v === 'number' ? v : 0), 0))

function displayArrow(v) { return (v === null || v === undefined) ? '' : String(v) }

function onArrowInput(i, raw) {
  const val = raw.trim()
  if (val === '') { form.arrows[i] = null; return }
  if (val.toUpperCase() === 'M') { form.arrows[i] = 'M'; return }
  const num = parseInt(val, 10)
  if (!isNaN(num)) form.arrows[i] = Math.min(10, Math.max(0, num))
}
function onArrowKeydown(i, e) {
  const allowed = ['Backspace','Delete','ArrowLeft','ArrowRight','Tab','Enter']
  if (allowed.includes(e.key) || e.key === 'm' || e.key === 'M') return
  if (!/^\d$/.test(e.key)) { e.preventDefault(); return }
  const next = String(displayArrow(form.arrows[i])) + e.key
  if (parseInt(next, 10) > 10) e.preventDefault()
}
function addArrow()    { arrowCount.value++; form.arrows.push(null) }
function removeArrow() { if (arrowCount.value > 1) { arrowCount.value--; form.arrows.pop() } }
function bubbleClass(s) {
  if (s === null || s === undefined) return 'bubble-empty'
  if (s === 'M')  return 'bubble-miss'
  if (s === 10)   return 'bubble-perfect'
  if (s >= 7)     return 'bubble-high'
  if (s >= 4)     return 'bubble-mid'
  return 'bubble-low'
}
function resetForm() {
  Object.assign(form, { tournamentId: null, archerId: null, roundNumber: 1, arrows: Array(arrowCount.value).fill(null) })
  error.value = success.value = ''
}
async function submitScore() {
  error.value = success.value = ''
  if (!form.tournamentId) { error.value = 'Selecciona un torneo.'; return }
  if (!form.archerId)     { error.value = 'Selecciona un arquero.'; return }
  const scores = form.arrows.map(v => (v === 'M' || v === null) ? 0 : Number(v))
  submitting.value = true
  try {
    await api.post('/rounds', { tournamentId: form.tournamentId, archerId: form.archerId, roundNumber: form.roundNumber, scores })
    success.value = '¡Ronda registrada exitosamente!'
    resetForm()
  } catch (e) {
    error.value = e.response?.data?.error || e.response?.data?.message || 'Error al registrar la ronda.'
  } finally { submitting.value = false }
}

// ── TAB 2 ─────────────────────────────────────────────────────────────
const editError   = ref('')
const editSuccess = ref('')
const editQuery   = reactive({ tournamentId: null, archerId: null })
const editRounds  = ref([])
const openRounds  = ref(new Set())
const changedArrows   = ref(new Set())
const originalScores  = ref({})
const searchingRounds = ref(false)
const savingRound     = ref(null)
const hasSearched     = ref(false)

async function loadRounds() {
  editError.value = editSuccess.value = ''
  searchingRounds.value = true
  hasSearched.value = true
  openRounds.value = new Set()
  changedArrows.value = new Set()
  try {
    const res = await api.get('/rounds', { params: { tournamentId: editQuery.tournamentId, archerId: editQuery.archerId } })
    editRounds.value = res.data
    const orig = {}
    res.data.forEach(r => r.arrows.forEach(a => { orig[a.arrowId] = a.score }))
    originalScores.value = orig
  } catch (e) {
    editError.value = e.response?.data?.error || 'Error al cargar las rondas.'
    editRounds.value = []
  } finally { searchingRounds.value = false }
}

function toggleRound(roundId) {
  const s = new Set(openRounds.value)
  s.has(roundId) ? s.delete(roundId) : s.add(roundId)
  openRounds.value = s
}
function markChanged(arrow) {
  const s = new Set(changedArrows.value)
  arrow.score !== originalScores.value[arrow.arrowId] ? s.add(arrow.arrowId) : s.delete(arrow.arrowId)
  changedArrows.value = s
}
function hasChangesInRound(round) { return round.arrows.some(a => changedArrows.value.has(a.arrowId)) }
function roundSum(round) { return round.arrows.reduce((sum, a) => sum + (typeof a.score === 'number' ? a.score : 0), 0) }

async function saveRound(round) {
  editError.value = editSuccess.value = ''
  savingRound.value = round.roundId
  const changed = round.arrows.filter(a => changedArrows.value.has(a.arrowId))
  try {
    await Promise.all(changed.map(arrow => api.put(`/rounds/${round.roundId}/arrows/${arrow.arrowId}`, { score: arrow.score })))
    const orig = { ...originalScores.value }
    changed.forEach(a => { orig[a.arrowId] = a.score })
    originalScores.value = orig
    const s = new Set(changedArrows.value)
    changed.forEach(a => s.delete(a.arrowId))
    changedArrows.value = s
    editSuccess.value = `Ronda ${round.roundNumber} actualizada. El log de auditoría fue registrado automáticamente.`
    setTimeout(() => { editSuccess.value = '' }, 4000)
  } catch (e) {
    editError.value = e.response?.data?.error || 'Error al guardar los cambios.'
  } finally { savingRound.value = null }
}

onMounted(async () => {
  try {
    const res = await getTournaments()
    allTournaments.value = Array.isArray(res.data) ? res.data : []
    activeTournaments.value = allTournaments.value.filter(t => t.active === true || t.active === 'true')
  } catch (e) { console.error('[AdminScoring] tournaments error:', e.message) }
  try {
    const res = await getArchers()
    archers.value = res.data
  } catch (e) { console.error('[AdminScoring] archers error:', e.message) }
  loadingData.value = false
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');
.admin-page { padding: calc(var(--header-height, 70px) + 2rem) 0 4rem; min-height: 100vh; }
.admin-page-header { margin-bottom: 1.5rem; }
.btn-back { display:inline-flex;align-items:center;gap:0.3rem;background:none;border:none;color:var(--text-muted);cursor:pointer;font-family:'Cinzel',serif;font-size:0.7rem;text-transform:uppercase;letter-spacing:0.1em;padding:0;margin-bottom:1rem;transition:color 0.2s; }
.btn-back:hover { color: var(--lol-gold); }
.btn-back .material-icons { font-size: 1rem; }
.page-title { font-size:1.5rem;font-family:'Cinzel',serif;display:flex;align-items:center;gap:0.5rem;margin-bottom:0.3rem; }
.page-title-icon { font-size:1.3rem;color:var(--lol-gold); }
.page-subtitle { font-size:0.82rem;color:var(--text-muted);margin:0; }
.page-rule { margin:1rem 0 1.5rem; }
.hint-text { font-size:0.75rem;color:var(--lol-danger,#e84057);margin-top:0.3rem;display:block; }
.btn-icon { display:inline-flex;align-items:center;gap:0.3rem; }
/* Tabs */
.mode-tabs { display:flex;gap:0.5rem;margin-bottom:1.2rem; }
.mode-tab { display:flex;align-items:center;gap:0.4rem;padding:0.55rem 1.2rem;font-family:'Cinzel',serif;font-size:0.72rem;font-weight:700;text-transform:uppercase;letter-spacing:0.1em;background:transparent;border:1px solid var(--lol-border,rgba(200,155,60,0.2));color:var(--text-muted);cursor:pointer;transition:all 0.2s; }
.mode-tab .material-icons { font-size:1rem; }
.mode-tab:hover { border-color:var(--lol-gold-dark);color:var(--lol-gold-light); }
.mode-tab.active { background:rgba(200,155,60,0.1);border-color:var(--lol-gold);color:var(--lol-gold-bright); }
/* Arrow inputs */
.arrow-inputs { display:grid;grid-template-columns:repeat(auto-fill,minmax(72px,1fr));gap:0.5rem; }
.arrow-score-input { text-align:center; }
.arrow-controls { display:flex;gap:0.5rem; }
.score-preview { padding:1rem;border-color:var(--lol-gold-dark); }
.preview-row { display:flex;justify-content:space-between;align-items:center;margin-bottom:0.8rem; }
.preview-label { font-family:'Cinzel',serif;font-size:0.72rem;text-transform:uppercase;letter-spacing:0.1em;color:var(--text-muted); }
.preview-value { font-family:'Cinzel',serif;font-size:1.6rem;font-weight:700;color:var(--lol-gold-bright); }
.arrow-bubbles { display:flex;gap:0.4rem;flex-wrap:wrap; }
.arrow-bubble { width:36px;height:36px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-family:'Cinzel',serif;font-size:0.85rem;font-weight:700;border:1px solid; }
/* Edit tab */
.edit-search-bar { display:flex;gap:1rem;align-items:flex-start;flex-wrap:wrap;margin-bottom:1.5rem; }
.empty-edit { text-align:center;padding:3rem 1rem;color:var(--text-muted);display:flex;flex-direction:column;align-items:center;gap:0.5rem; }
.empty-edit p { font-size:0.88rem; }
.rounds-list { display:flex;flex-direction:column;gap:0.75rem; }
.round-block { border:1px solid var(--lol-border,rgba(200,155,60,0.15));border-radius:4px;overflow:hidden;padding:0; }
.round-header { display:flex;align-items:center;justify-content:space-between;padding:0.9rem 1.1rem;cursor:pointer;transition:background 0.2s; }
.round-header:hover { background:rgba(200,155,60,0.05); }
.round-header-left { display:flex;align-items:center;gap:0.6rem; }
.round-icon { font-size:1.1rem;color:var(--lol-gold-dark); }
.round-label { font-family:'Cinzel',serif;font-size:0.85rem;font-weight:700;color:var(--text-primary); }
.round-total-badge { font-size:0.72rem;font-family:'Cinzel',serif;color:var(--lol-gold);background:rgba(200,155,60,0.1);border:1px solid rgba(200,155,60,0.25);padding:0.15rem 0.5rem;border-radius:2px; }
.round-unsaved-badge { font-size:0.7rem;color:var(--lol-warning,#ff9800);font-family:'Cinzel',serif;animation:pulse 1.5s infinite; }
@keyframes pulse { 0%,100% { opacity:1 } 50% { opacity:0.4 } }
.expand-icon { color:var(--text-muted);transition:transform 0.25s;font-size:1.2rem; }
.expand-icon.rotated { transform:rotate(180deg); }
.arrow-editor { padding:1rem 1.1rem 1.1rem;border-top:1px solid var(--lol-border,rgba(200,155,60,0.15));background:rgba(0,0,0,0.15); }
.arrow-editor-grid { display:flex;flex-wrap:wrap;gap:0.75rem;margin-bottom:1rem; }
.arrow-edit-cell { display:flex;flex-direction:column;align-items:center;gap:0.3rem;width:64px; }
.arrow-edit-label { font-family:'Cinzel',serif;font-size:0.62rem;text-transform:uppercase;letter-spacing:0.1em;color:var(--text-muted); }
.arrow-edit-cell .form-input { width:64px;text-align:center;padding:0.4rem 0.3rem;transition:border-color 0.2s,box-shadow 0.2s; }
.score-changed { border-color:var(--lol-gold)!important;box-shadow:0 0 6px rgba(200,155,60,0.35)!important; }
.arrow-bubble-mini { width:28px;height:28px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-family:'Cinzel',serif;font-size:0.75rem;font-weight:700;border:1px solid; }
.round-footer { display:flex;align-items:center;justify-content:space-between;padding-top:0.75rem;border-top:1px solid rgba(200,155,60,0.1); }
.round-new-total { font-size:0.82rem;color:var(--text-muted);font-family:'Cinzel',serif; }
.round-new-total strong { color:var(--lol-gold-bright); }
/* Bubbles */
.bubble-empty   { border-color:var(--lol-border);color:var(--text-muted); }
.bubble-perfect { border-color:var(--lol-gold);color:var(--lol-gold-bright);background:rgba(200,155,60,0.1); }
.bubble-high    { border-color:var(--lol-success,#4caf50);color:var(--lol-success,#4caf50); }
.bubble-mid     { border-color:var(--lol-warning,#ff9800);color:var(--lol-warning,#ff9800); }
.bubble-low     { border-color:var(--lol-danger,#e84057);color:var(--lol-danger,#e84057); }
.bubble-miss    { border-color:#888;color:#888;font-style:italic; }
/* Transitions */
.expand-enter-active,.expand-leave-active { transition:all 0.25s ease;overflow:hidden; }
.expand-enter-from,.expand-leave-to { opacity:0;max-height:0; }
.expand-enter-to,.expand-leave-from { opacity:1;max-height:800px; }
.slide-up-enter-active,.slide-up-leave-active { transition:all 0.2s ease; }
.slide-up-enter-from,.slide-up-leave-to { opacity:0;transform:translateY(-8px); }
</style>
