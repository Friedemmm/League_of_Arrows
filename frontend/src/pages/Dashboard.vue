<template>
  <div class="page-wrapper dashboard-page">
    <div class="container">

      <!-- ══ Welcome Banner ══ -->
      <div class="welcome-banner lol-card mb-4">
        <div class="welcome-left">
          <span class="welcome-tag">{{ auth.isAdmin ? 'Administrador' : 'Arquero' }}</span>
          <h1 class="welcome-title">
            Bienvenido,&nbsp;<span class="text-gold">{{ displayName }}</span>
          </h1>
          <p class="welcome-sub">
            {{ auth.isAdmin
              ? 'Gestiona torneos, arqueros y rankings desde tu panel de administrador.'
              : 'Sigue tu rendimiento en los torneos y escala en el leaderboard.' }}
          </p>
        </div>
        <div class="welcome-crest" aria-hidden="true">
          <svg width="80" height="80" viewBox="0 0 32 32" fill="none">
            <polygon points="16,2 20,12 30,12 22,18 25,29 16,23 7,29 10,18 2,12 12,12"
              fill="none" stroke="#c89b3c" stroke-width="1.5"/>
            <circle cx="16" cy="16" r="4" fill="#c89b3c" opacity="0.7"/>
          </svg>
        </div>
      </div>

      <!-- ══ ADMIN: Navigation Cards ══ -->
      <template v-if="auth.isAdmin">
        <div class="nav-grid" id="admin-nav-grid">
          <RouterLink
            v-for="link in adminLinks"
            :key="link.to"
            :to="link.to"
            :id="`admin-card-${link.id}`"
            class="nav-card lol-card"
          >
            <span class="nav-card-icon material-icons" :class="link.iconClass">{{ link.icon }}</span>
            <div class="nav-card-body">
              <span class="nav-card-title">{{ link.label }}</span>
              <span class="nav-card-desc">{{ link.desc }}</span>
            </div>
            <span class="nav-card-arrow material-icons">arrow_forward</span>
          </RouterLink>
        </div>
      </template>

      <!-- ══ ARCHER: Stats + Navigation ══ -->
      <template v-else>

        <!-- Summary cards (4 cards, centered) -->
        <div class="stat-grid stat-grid--centered">
          <div class="stat-card">
            <div class="stat-value">{{ archerStats.tournaments }}</div>
            <div class="stat-label">Torneos Jugados</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ archerStats.totalScore }}</div>
            <div class="stat-label">Puntos Totales</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ archerStats.bestPosition }}</div>
            <div class="stat-label">Mejor Posición</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ archerStats.avgScore }}</div>
            <div class="stat-label">Prom. Puntaje</div>
          </div>
        </div>

        <!-- Two action buttons below cards -->
        <div class="archer-nav-row">
          <RouterLink to="/profile" class="archer-nav-btn lol-card" id="dashboard-btn-info">
            <span class="material-icons anb-icon">person</span>
            <div class="anb-body">
              <span class="anb-title">Mi Información</span>
              <span class="anb-desc">Ver tu perfil y datos de cuenta.</span>
            </div>
            <span class="material-icons anb-arrow">arrow_forward</span>
          </RouterLink>

          <RouterLink to="/history" class="archer-nav-btn lol-card" id="dashboard-btn-history">
            <span class="material-icons anb-icon">history</span>
            <div class="anb-body">
              <span class="anb-title">Mi Historial</span>
              <span class="anb-desc">Revisa tu historial de rendimiento en torneos.</span>
            </div>
            <span class="material-icons anb-arrow">arrow_forward</span>
          </RouterLink>
        </div>

      </template>

    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMyHistory } from '@/api/archers'

const auth = useAuthStore()

const displayName = computed(() => auth.user?.email?.split('@')[0] ?? 'User')

// ── Admin navigation links ────────────────────────────────────────────
const adminLinks = [
  {
    to:        '/admin/archers',
    id:        'archers',
    icon:      'group',
    iconClass: 'icon-cyan',
    label:     'Gestionar Arqueros',
    desc:      'Crea, edita y elimina perfiles de arqueros.',
  },
  {
    to:        '/admin/tournaments',
    id:        'events',
    icon:      'emoji_events',
    iconClass: 'icon-gold',
    label:     'Gestionar Eventos',
    desc:      'Crea y administra torneos activos.',
  },
  {
    to:        '/admin/scoring',
    id:        'scoring',
    icon:      'sports_score',
    iconClass: 'icon-green',
    label:     'Registro de Puntajes',
    desc:      'Registra puntajes de rondas para arqueros.',
  },
  {
    to:        '/admin/audit',
    id:        'audit',
    icon:      'policy',
    iconClass: 'icon-red',
    label:     'Auditoría',
    desc:      'Ver registro de modificaciones de puntajes.',
  },
]

// ── Archer stats ──────────────────────────────────────────────────────
const archerStats = reactive({ tournaments: '—', totalScore: '—', bestPosition: '—', avgScore: '—' })

onMounted(async () => {
  if (!auth.isAdmin) {
    try {
      const res     = await getMyHistory()
      const history = res.data
      archerStats.tournaments = history.length
      const scores = history.map(h => h.totalScore || 0)
      archerStats.totalScore  = scores.reduce((a, b) => a + b, 0)
      archerStats.avgScore    = history.length
        ? (archerStats.totalScore / history.length).toFixed(1) : '—'
      const positions = history.map(h => h.position).filter(p => p != null && p > 0)
      archerStats.bestPosition = positions.length ? Math.min(...positions) : '—'
    } catch { /* not critical */ }
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

/* ── Page ───────────────────────────────────────────────────── */
.dashboard-page {
  padding: calc(var(--header-height) + 2rem) 0 4rem;
  min-height: 100vh;
  /* Fog effect — layered over the aurora background */
  background:
    radial-gradient(ellipse 80% 50% at 50% 0%,   rgba(1,10,19,0.55) 0%, transparent 70%),
    radial-gradient(ellipse 60% 40% at 20% 100%,  rgba(1,10,19,0.45) 0%, transparent 70%),
    radial-gradient(ellipse 60% 40% at 80% 100%,  rgba(1,10,19,0.40) 0%, transparent 70%);
}

/* ── Welcome Banner ─────────────────────────────────────────── */
.welcome-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 2rem;
  gap: 1rem;
}

.welcome-left { flex: 1; min-width: 0; }

.welcome-tag {
  display: inline-block;
  font-family: 'Cinzel', serif;
  font-size: 0.6rem;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  color: var(--lol-gold);
  border: 1px solid rgba(200,155,60,0.3);
  padding: 0.18rem 0.55rem;
  border-radius: 2px;
  margin-bottom: 0.5rem;
}

.welcome-title {
  font-size: clamp(1.4rem, 3vw, 2rem);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.15;
  margin-bottom: 0.4rem;
}

.welcome-sub { font-size: 0.84rem; color: var(--text-muted); margin: 0; }
.welcome-crest { opacity: 0.3; flex-shrink: 0; }

/* ── Admin Nav Grid ─────────────────────────────────────────── */
.nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

@media (max-width: 1024px) { .nav-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 540px)  { .nav-grid { grid-template-columns: 1fr; } }

/* ── Nav Card ───────────────────────────────────────────────── */
.nav-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.4rem 1.2rem;
  text-decoration: none;
  transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
}

.nav-card:hover {
  border-color: rgba(200,155,60,0.45);
  transform: translateY(-3px);
  box-shadow: 0 6px 24px rgba(0,0,0,0.35), 0 0 16px rgba(200,155,60,0.08);
}

.nav-card-icon {
  font-size: 1.4rem;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
}

.icon-cyan  { background: rgba(11,196,227,0.12);  color: #0bc4e3; }
.icon-gold  { background: rgba(200,155,60,0.12);  color: #c89b3c; }
.icon-green { background: rgba(160,200,120,0.12); color: #a0c878; }
.icon-red   { background: rgba(232,64,87,0.12);   color: #e84057; }

.nav-card-body {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  flex: 1;
  min-width: 0;
}

.nav-card-title {
  font-family: 'Cinzel', serif;
  font-size: 0.78rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-primary);
}

.nav-card-desc {
  font-size: 0.72rem;
  color: var(--text-muted);
  line-height: 1.35;
}

.nav-card-arrow {
  font-size: 1.1rem;
  color: var(--lol-gold);
  opacity: 0;
  transition: opacity 0.2s, transform 0.2s;
  flex-shrink: 0;
}

.nav-card:hover .nav-card-arrow {
  opacity: 1;
  transform: translateX(3px);
}

/* ── Archer Stat Grid (3 cards, centered) ───────────────────── */
.stat-grid--centered {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.stat-grid--centered .stat-card {
  flex: 0 1 200px;
  min-width: 160px;
}

/* ── Archer Nav Row (2 buttons below cards) ─────────────────── */
.archer-nav-row {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
  justify-content: center;
  flex-wrap: wrap;
}

.archer-nav-btn {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.3rem 1.4rem;
  text-decoration: none;
  flex: 0 1 320px;
  min-width: 240px;
  transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
}

.archer-nav-btn:hover {
  border-color: rgba(200,155,60,0.5);
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.3), 0 0 14px rgba(200,155,60,0.1);
}

.anb-icon {
  font-size: 1.5rem;
  color: var(--lol-gold);
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(200,155,60,0.1);
  border-radius: 8px;
}

.anb-body {
  display: flex;
  flex-direction: column;
  gap: 0.18rem;
  flex: 1;
  min-width: 0;
}

.anb-title {
  font-family: 'Cinzel', serif;
  font-size: 0.82rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-primary);
}

.anb-desc {
  font-size: 0.72rem;
  color: var(--text-muted);
}

.anb-arrow {
  font-size: 1.1rem;
  color: var(--lol-gold);
  opacity: 0;
  flex-shrink: 0;
  transition: opacity 0.2s, transform 0.2s;
}

.archer-nav-btn:hover .anb-arrow {
  opacity: 1;
  transform: translateX(3px);
}
</style>
