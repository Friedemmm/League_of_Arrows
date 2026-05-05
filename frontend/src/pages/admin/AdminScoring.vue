<template>
  <div class="page-wrapper">
    <div class="container" style="max-width:800px;">
      <h1 class="section-title">Score Entry</h1>
      <p class="text-secondary mb-4" style="margin-top:-1rem;">
        Register round scores for an archer in an active tournament.
        Each arrow score must be between 0–10 (or M for miss).
      </p>

      <div class="lol-card">
        <Transition name="slide-up">
          <div class="alert alert-error" v-if="error">⚠ {{ error }}</div>
        </Transition>
        <Transition name="slide-up">
          <div class="alert alert-success" v-if="success">✓ {{ success }}</div>
        </Transition>

        <div class="grid-2">
          <div class="form-group">
            <label class="form-label" for="scoring-tournament">Tournament ID</label>
            <input id="scoring-tournament" class="form-input" type="number" v-model.number="form.tournamentId"
              placeholder="Enter tournament ID" />
          </div>
          <div class="form-group">
            <label class="form-label" for="scoring-archer">Archer ID</label>
            <input id="scoring-archer" class="form-input" type="number" v-model.number="form.archerId"
              placeholder="Enter archer ID" />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label" for="scoring-round">Round Number</label>
          <input id="scoring-round" class="form-input" type="number" v-model.number="form.roundNumber"
            placeholder="Round number (e.g. 1)" min="1" />
        </div>

        <div class="form-group">
          <label class="form-label">Arrow Scores (0–10 each, space-separated)</label>
          <div class="arrow-inputs">
            <input
              v-for="(_, i) in arrowCount"
              :key="i"
              :id="`arrow-score-${i+1}`"
              class="form-input arrow-score-input"
              type="number"
              min="0"
              max="10"
              v-model.number="form.arrows[i]"
              :placeholder="`Arrow ${i+1}`"
            />
          </div>
          <div class="arrow-controls mt-1">
            <button class="btn btn-ghost btn-sm" id="btn-add-arrow" @click="addArrow">+ Arrow</button>
            <button class="btn btn-ghost btn-sm" id="btn-remove-arrow" @click="removeArrow" :disabled="arrowCount <= 1">− Arrow</button>
          </div>
        </div>

        <!-- Score Preview -->
        <div class="score-preview lol-card mt-2" v-if="form.arrows.some(a => a !== null && a !== '')">
          <div class="preview-row">
            <span class="preview-label">Round Total</span>
            <span class="preview-value">{{ roundTotal }}</span>
          </div>
          <div class="arrow-bubbles">
            <span
              v-for="(s, i) in form.arrows"
              :key="i"
              class="arrow-bubble"
              :class="bubbleClass(s)"
            >{{ s !== null && s !== '' ? s : '?' }}</span>
          </div>
        </div>

        <div class="modal-footer" style="padding:1.5rem 0 0;">
          <button class="btn btn-ghost" @click="resetForm" id="btn-reset-scoring">Reset</button>
          <button class="btn btn-gold" @click="submitScore" :disabled="submitting" id="btn-submit-score">
            {{ submitting ? 'Submitting...' : 'Submit Round' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import api from '@/api/axios'

const error     = ref('')
const success   = ref('')
const submitting = ref(false)
const arrowCount = ref(6)

const form = reactive({
  tournamentId: null,
  archerId:     null,
  roundNumber:  1,
  arrows:       Array(6).fill(null),
})

const roundTotal = computed(() =>
  form.arrows.reduce((sum, v) => sum + (Number(v) || 0), 0)
)

function addArrow() {
  arrowCount.value++
  form.arrows.push(null)
}

function removeArrow() {
  if (arrowCount.value > 1) {
    arrowCount.value--
    form.arrows.pop()
  }
}

function bubbleClass(s) {
  if (s === null || s === '') return 'bubble-empty'
  if (s === 10) return 'bubble-perfect'
  if (s >= 7)   return 'bubble-high'
  if (s >= 4)   return 'bubble-mid'
  return 'bubble-low'
}

function resetForm() {
  Object.assign(form, { tournamentId: null, archerId: null, roundNumber: 1, arrows: Array(arrowCount.value).fill(null) })
  error.value = success.value = ''
}

async function submitScore() {
  error.value   = ''
  success.value = ''
  if (!form.tournamentId || !form.archerId) {
    error.value = 'Tournament ID and Archer ID are required.'
    return
  }
  submitting.value = true
  try {
    await api.post('/inscriptions/score', {
      tournamentId: form.tournamentId,
      archerId:     form.archerId,
      roundNumber:  form.roundNumber,
      arrows:       form.arrows.map(Number),
    })
    success.value = 'Round score submitted successfully!'
    resetForm()
  } catch (e) {
    error.value = e.response?.data?.error || 'Failed to submit score. Ensure the tournament is active.'
  } finally { submitting.value = false }
}
</script>

<style scoped>
.arrow-inputs {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  gap: 0.5rem;
}

.arrow-score-input { text-align: center; }

.arrow-controls { display: flex; gap: 0.5rem; }

.score-preview {
  padding: 1rem;
  border-color: var(--lol-gold-dark);
}

.preview-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.8rem;
}

.preview-label {
  font-family: 'Cinzel', serif;
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--text-muted);
}

.preview-value {
  font-family: 'Cinzel', serif;
  font-size: 1.6rem;
  font-weight: 700;
  color: var(--lol-gold-bright);
}

.arrow-bubbles {
  display: flex;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.arrow-bubble {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Cinzel', serif;
  font-size: 0.85rem;
  font-weight: 700;
  border: 1px solid;
}

.bubble-empty   { border-color: var(--lol-border); color: var(--text-muted); }
.bubble-perfect { border-color: var(--lol-gold); color: var(--lol-gold-bright); background: rgba(200,155,60,0.1); box-shadow: var(--glow-gold); }
.bubble-high    { border-color: var(--lol-success); color: var(--lol-success); }
.bubble-mid     { border-color: var(--lol-warning); color: var(--lol-warning); }
.bubble-low     { border-color: var(--lol-danger); color: var(--lol-danger); }
</style>
