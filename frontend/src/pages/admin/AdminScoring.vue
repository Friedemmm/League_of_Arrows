<template>
  <div class="page-wrapper admin-page">
    <div class="container" style="max-width:820px;">

      <!-- Page header -->
      <div class="admin-page-header">
        <button class="btn-back" id="btn-back-scoring" @click="$router.back()">
          <span class="material-icons">arrow_back</span> Back
        </button>
        <h1 class="page-title">
          <span class="material-icons page-title-icon">sports_score</span>
          Score Registration
        </h1>
        <p class="page-subtitle">Register round scores for an archer in an active tournament.</p>
        <hr class="page-rule" />
      </div>

      <div class="lol-card">
        <Transition name="slide-up">
          <div class="alert alert-error" v-if="error">
            <span class="material-icons">warning</span> {{ error }}
          </div>
        </Transition>
        <Transition name="slide-up">
          <div class="alert alert-success" v-if="success">
            <span class="material-icons">check_circle</span> {{ success }}
          </div>
        </Transition>

        <div v-if="loadingData" class="loading-center" style="padding:2rem 0;">
          <div class="spinner"></div>
        </div>

        <template v-else>
          <div class="grid-2">
            <!-- Tournament dropdown — only active ones -->
            <div class="form-group">
              <label class="form-label" for="scoring-tournament">Tournament</label>
              <select id="scoring-tournament" class="form-input" v-model.number="form.tournamentId">
                <option :value="null" disabled>Select a tournament</option>
                <option v-for="t in activeTournaments" :key="t.tournamentId" :value="t.tournamentId">
                  {{ t.name }}
                </option>
              </select>
              <span v-if="activeTournaments.length === 0" class="hint-text">No active tournaments found.</span>
            </div>

            <!-- Archer dropdown -->
            <div class="form-group">
              <label class="form-label" for="scoring-archer">Archer</label>
              <select id="scoring-archer" class="form-input" v-model.number="form.archerId">
                <option :value="null" disabled>Select an archer</option>
                <option v-for="a in archers" :key="a.archerId" :value="a.archerId">{{ a.name }}</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label" for="scoring-round">Round Number</label>
            <input id="scoring-round" class="form-input" type="number"
              v-model.number="form.roundNumber" placeholder="Round (e.g. 1)" min="1" style="max-width:160px;" />
          </div>

          <div class="form-group">
            <label class="form-label">Arrow Scores (0–10 each, or M for miss)</label>
            <div class="arrow-inputs">
              <input
                v-for="(_, i) in arrowCount"
                :key="i"
                :id="`arrow-score-${i+1}`"
                class="form-input arrow-score-input"
                type="text"
                inputmode="numeric"
                maxlength="2"
                :placeholder="`#${i+1}`"
                :value="displayArrow(form.arrows[i])"
                @keydown="onArrowKeydown(i, $event)"
                @input="onArrowInput(i, $event.target.value)"
              />
            </div>
            <div class="arrow-controls mt-1">
              <button class="btn btn-ghost btn-sm" id="btn-add-arrow" @click="addArrow">
                <span class="material-icons" style="font-size:1rem;">add</span> Arrow
              </button>
              <button class="btn btn-ghost btn-sm" id="btn-remove-arrow" @click="removeArrow" :disabled="arrowCount <= 1">
                <span class="material-icons" style="font-size:1rem;">remove</span> Arrow
              </button>
            </div>
          </div>

          <!-- Score Preview -->
          <div class="score-preview lol-card mt-2" v-if="hasScores">
            <div class="preview-row">
              <span class="preview-label">Round Total</span>
              <span class="preview-value">{{ roundTotal }}</span>
            </div>
            <div class="arrow-bubbles">
              <span v-for="(s, i) in form.arrows" :key="i"
                class="arrow-bubble" :class="bubbleClass(s)">
                {{ displayArrow(s) || '?' }}
              </span>
            </div>
          </div>

          <div class="modal-footer" style="padding:1.5rem 0 0;">
            <button class="btn btn-ghost" @click="resetForm" id="btn-reset-scoring">
              <span class="material-icons btn-icon">restart_alt</span> Reset
            </button>
            <button class="btn btn-gold" @click="submitScore" :disabled="submitting" id="btn-submit-score">
              <span class="material-icons btn-icon">send</span>
              {{ submitting ? 'Submitting...' : 'Submit Round' }}
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import api from '@/api/axios'
import { getArchers } from '@/api/archers'
import { getTournaments } from '@/api/tournaments'

const error      = ref('')
const success    = ref('')
const submitting = ref(false)
const loadingData = ref(true)
const arrowCount = ref(6)

const archers           = ref([])
const activeTournaments = ref([])

const form = reactive({
  tournamentId: null,
  archerId:     null,
  roundNumber:  1,
  arrows:       Array(6).fill(null),
})

// 'M' stored as string, numbers stored as numbers, null = empty
const hasScores  = computed(() => form.arrows.some(a => a !== null))
const roundTotal = computed(() =>
  form.arrows.reduce((sum, v) => sum + (typeof v === 'number' ? v : 0), 0)
)

function displayArrow(v) {
  if (v === null || v === undefined) return ''
  return String(v)
}

function onArrowInput(i, raw) {
  const val = raw.trim()
  if (val === '') { form.arrows[i] = null; return }
  if (val.toUpperCase() === 'M') { form.arrows[i] = 'M'; return }
  const num = parseInt(val, 10)
  if (!isNaN(num)) form.arrows[i] = Math.min(10, Math.max(0, num))
}

// Block any keystroke that would result in a value > 10
function onArrowKeydown(i, e) {
  const allowed = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab', 'Enter']
  if (allowed.includes(e.key)) return
  // Always allow 'M' / 'm'
  if (e.key === 'm' || e.key === 'M') return
  // Block non-digit keys (except the above)
  if (!/^\d$/.test(e.key)) { e.preventDefault(); return }
  // Simulate what the value would be after this key
  const current = String(displayArrow(form.arrows[i]))
  const next = current === '0' ? e.key : current + e.key
  const num = parseInt(next, 10)
  if (num > 10) e.preventDefault()
}

function addArrow()    { arrowCount.value++; form.arrows.push(null) }
function removeArrow() { if (arrowCount.value > 1) { arrowCount.value--; form.arrows.pop() } }

function bubbleClass(s) {
  if (s === null) return 'bubble-empty'
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
  error.value   = ''
  success.value = ''
  if (!form.tournamentId) { error.value = 'Please select a tournament.'; return }
  if (!form.archerId)     { error.value = 'Please select an archer.'; return }

  const scores = form.arrows.map(v => (v === 'M' || v === null) ? 0 : Number(v))

  submitting.value = true
  try {
    // POST /api/rounds  →  RoundRequestDTO {tournamentId, archerId, roundNumber, scores}
    await api.post('/rounds', {
      tournamentId: form.tournamentId,
      archerId:     form.archerId,
      roundNumber:  form.roundNumber,
      scores,
    })
    success.value = 'Round score submitted successfully!'
    resetForm()
  } catch (e) {
    error.value = e.response?.data?.error || e.response?.data?.message
      || 'Failed to submit. Ensure the tournament is active and the archer is registered.'
  } finally { submitting.value = false }
}

onMounted(async () => {
  // Load independently so one failure doesn't block the other
  try {
    const res = await getTournaments()
    // Show ALL tournaments in the dropdown (active field from API = boolean)
    activeTournaments.value = res.data.filter(t => t.active === true || t.active === 'true')
  } catch (e) {
    console.error('[AdminScoring] tournaments error:', e.message)
  }
  try {
    const res = await getArchers()
    archers.value = res.data
  } catch (e) {
    console.error('[AdminScoring] archers error:', e.message)
  }
  loadingData.value = false
})
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

.page-title {
  font-size: 1.5rem; font-family: 'Cinzel', serif;
  display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.3rem;
}
.page-title-icon { font-size: 1.3rem; color: var(--lol-gold); }
.page-subtitle { font-size: 0.82rem; color: var(--text-muted); margin: 0; }
.page-rule { margin: 1rem 0 1.5rem; }

.hint-text { font-size: 0.75rem; color: var(--lol-danger, #e84057); margin-top: 0.3rem; display: block; }

.btn-icon { display: inline-flex; align-items: center; gap: 0.3rem; }

.arrow-inputs {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
  gap: 0.5rem;
}
.arrow-score-input { text-align: center; }
.arrow-controls { display: flex; gap: 0.5rem; }

.score-preview { padding: 1rem; border-color: var(--lol-gold-dark); }
.preview-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.8rem; }
.preview-label {
  font-family: 'Cinzel', serif; font-size: 0.72rem; text-transform: uppercase;
  letter-spacing: 0.1em; color: var(--text-muted);
}
.preview-value { font-family: 'Cinzel', serif; font-size: 1.6rem; font-weight: 700; color: var(--lol-gold-bright); }

.arrow-bubbles { display: flex; gap: 0.4rem; flex-wrap: wrap; }
.arrow-bubble {
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-family: 'Cinzel', serif; font-size: 0.85rem; font-weight: 700; border: 1px solid;
}
.bubble-empty   { border-color: var(--lol-border); color: var(--text-muted); }
.bubble-perfect { border-color: var(--lol-gold); color: var(--lol-gold-bright); background: rgba(200,155,60,0.1); box-shadow: var(--glow-gold); }
.bubble-high    { border-color: var(--lol-success, #4caf50); color: var(--lol-success, #4caf50); }
.bubble-mid     { border-color: var(--lol-warning, #ff9800); color: var(--lol-warning, #ff9800); }
.bubble-low     { border-color: var(--lol-danger, #e84057); color: var(--lol-danger, #e84057); }
.bubble-miss    { border-color: #888; color: #888; font-style: italic; }
</style>
