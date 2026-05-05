<template>
  <div class="page-wrapper">
    <div class="container">
      <!-- Welcome Banner -->
      <div class="welcome-banner lol-card mb-4">
        <div class="welcome-left">
          <span class="welcome-tag">{{ auth.isAdmin ? 'Administrator' : 'Archer' }}</span>
          <h1 class="welcome-title">Welcome back,<br>
            <span class="text-gold">{{ auth.user?.email?.split('@')[0] }}</span>
          </h1>
          <p class="text-secondary" style="font-size:0.88rem;">
            {{ auth.isAdmin
              ? 'Manage tournaments, archers, and rankings from your admin panel.'
              : 'Track your performance across tournaments and climb the leaderboard.' }}
          </p>
        </div>
        <div class="welcome-crest">
          <svg width="80" height="80" viewBox="0 0 32 32" fill="none">
            <polygon points="16,2 20,12 30,12 22,18 25,29 16,23 7,29 10,18 2,12 12,12"
              fill="none" stroke="#c89b3c" stroke-width="1.5"/>
            <circle cx="16" cy="16" r="4" fill="#c89b3c" opacity="0.7"/>
          </svg>
        </div>
      </div>

      <!-- Quick Stats (Archer) -->
      <div class="stat-grid" v-if="!auth.isAdmin">
        <div class="stat-card">
          <div class="stat-value">{{ stats.tournaments }}</div>
          <div class="stat-label">Tournaments Entered</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalScore }}</div>
          <div class="stat-label">Total Points</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.bestPosition }}</div>
          <div class="stat-label">Best Finish</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.avgScore }}</div>
          <div class="stat-label">Avg. Score</div>
        </div>
      </div>

      <!-- Quick Links -->
      <h2 class="section-title">Quick Access</h2>
      <div class="quick-grid">
        <RouterLink
          v-for="link in quickLinks"
          :key="link.to"
          :to="link.to"
          :id="`dashboard-link-${link.label.toLowerCase().replace(/\s/g,'-')}`"
          class="quick-card lol-card"
        >
          <span class="quick-icon">{{ link.icon }}</span>
          <span class="quick-label">{{ link.label }}</span>
          <span class="quick-arrow">→</span>
        </RouterLink>
      </div>

      <!-- Admin Quick Links -->
      <template v-if="auth.isAdmin">
        <h2 class="section-title mt-4">Admin Controls</h2>
        <div class="quick-grid">
          <RouterLink
            v-for="link in adminLinks"
            :key="link.to"
            :to="link.to"
            :id="`admin-link-${link.label.toLowerCase().replace(/\s/g,'-')}`"
            class="quick-card lol-card"
          >
            <span class="quick-icon">{{ link.icon }}</span>
            <span class="quick-label">{{ link.label }}</span>
            <span class="quick-arrow">→</span>
          </RouterLink>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMyHistory } from '@/api/archers'

const auth = useAuthStore()

const stats = reactive({ tournaments: '—', totalScore: '—', bestPosition: '—', avgScore: '—' })

const quickLinks = [
  { to: '/tournaments', icon: '⚔', label: 'Tournaments' },
  { to: '/leaderboard', icon: '◆', label: 'Leaderboard' },
  { to: '/trending',    icon: '▲', label: 'Trending' },
  { to: '/history',     icon: '◉', label: 'My History' },
  { to: '/profile',     icon: '◎', label: 'My Profile' },
]

const adminLinks = [
  { to: '/admin/archers',     icon: '🏹', label: 'Manage Archers' },
  { to: '/admin/tournaments', icon: '⚔',  label: 'Manage Tournaments' },
  { to: '/admin/scoring',     icon: '◎',  label: 'Score Entry' },
  { to: '/admin/ranking',     icon: '◆',  label: 'Rankings' },
  { to: '/admin/audit',       icon: '📋', label: 'Audit Log' },
  { to: '/admin/categories',  icon: '⊞',  label: 'Categories' },
]

onMounted(async () => {
  if (!auth.isAdmin) {
    try {
      const res = await getMyHistory()
      const history = res.data
      stats.tournaments = history.length
      const scores = history.map(h => h.score || 0)
      stats.totalScore  = scores.reduce((a, b) => a + b, 0)
      stats.avgScore    = history.length ? (stats.totalScore / history.length).toFixed(1) : '—'
      const positions   = history.map(h => h.position).filter(Boolean)
      stats.bestPosition = positions.length ? Math.min(...positions) : '—'
    } catch { /* not critical */ }
  }
})
</script>

<style scoped>
.welcome-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 2rem;
}

.welcome-tag {
  display: inline-block;
  font-family: 'Cinzel', serif;
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  color: var(--lol-gold);
  border: 1px solid rgba(200,155,60,0.3);
  padding: 0.2rem 0.6rem;
  border-radius: 2px;
  margin-bottom: 0.6rem;
}

.welcome-title {
  font-size: 2rem;
  line-height: 1.2;
  margin-bottom: 0.5rem;
}

.welcome-crest {
  opacity: 0.3;
  flex-shrink: 0;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 0.75rem;
}

.quick-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem 1rem;
  text-align: center;
  text-decoration: none;
  transition: all var(--transition);
}

.quick-card:hover {
  border-color: var(--lol-gold);
  transform: translateY(-3px);
  box-shadow: var(--glow-gold);
}

.quick-icon {
  font-size: 1.5rem;
}

.quick-label {
  font-family: 'Cinzel', serif;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-primary);
}

.quick-arrow {
  font-size: 0.85rem;
  color: var(--lol-gold);
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.quick-card:hover .quick-arrow {
  opacity: 1;
}
</style>
