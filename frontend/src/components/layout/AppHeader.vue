<template>
  <header class="app-header" role="banner">
    <!-- Logo -->
    <div class="header-logo">
      <RouterLink to="/" id="nav-logo" class="logo-link">
        <img :src="logoImg" alt="League of Arrow" class="logo-img" />
      </RouterLink>
    </div>

    <!-- Center Nav -->
    <nav class="header-nav" role="navigation" aria-label="Main navigation">
      <div v-for="item in guestNavItems" :key="item.to" class="nav-li">
        <RouterLink
          :to="item.to"
          :id="`nav-${item.id}`"
          class="nav-link"
          :class="{ 'nav-link--active': isActive(item.to) }"
        >{{ item.label }}</RouterLink>
      </div>
    </nav>

    <!-- Right: Login or User -->
    <div class="header-right">
      <!-- Guest: LoL-style INGRESAR button (SVG-rendered) -->
      <RouterLink
        v-if="!isLoggedIn"
        to="/login"
        id="btn-login"
        class="ingresar-btn"
      >
        <!-- SVG shape: concave left arc + angular right notch -->
        <svg class="ingresar-svg" viewBox="0 0 188 42" preserveAspectRatio="none" aria-hidden="true">
          <defs>
            <!-- Metallic body gradient -->
            <linearGradient id="ig-metal" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%"   stop-color="#1c2d3e"/>
              <stop offset="30%"  stop-color="#0e1e2d"/>
              <stop offset="60%"  stop-color="#0a1826"/>
              <stop offset="100%" stop-color="#1a2d3e"/>
            </linearGradient>
            <!-- Top bevel highlight -->
            <linearGradient id="ig-bevel" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%"   stop-color="rgba(255,255,255,0.12)"/>
              <stop offset="100%" stop-color="rgba(255,255,255,0)"/>
            </linearGradient>
            <!-- Cyan glow filter -->
            <filter id="ig-glow" x="-15%" y="-40%" width="130%" height="180%">
              <feGaussianBlur in="SourceGraphic" stdDeviation="2.5" result="blur"/>
              <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
            </filter>
          </defs>
          <!-- Metallic body -->
          <path d="M18 1 L168 1 L187 21 L168 41 L18 41 Q1 21 18 1Z"
                fill="url(#ig-metal)"/>
          <!-- Top-edge bevel highlight -->
          <path d="M18 1 L168 1 L180 12"
                fill="none" stroke="url(#ig-bevel)" stroke-width="1.5"/>
          <!-- Outer cyan border glow -->
          <path d="M18 1 L168 1 L187 21 L168 41 L18 41 Q1 21 18 1Z"
                fill="none" stroke="#0BC4E3" stroke-width="1.2"
                filter="url(#ig-glow)" opacity="0.95"/>
          <!-- Inner cyan hairline -->
          <path d="M19 3 L167 3 L184 21 L167 39 L19 39 Q3 21 19 3Z"
                fill="none" stroke="rgba(11,196,227,0.3)" stroke-width="0.7"/>
        </svg>
        <span class="ingresar-text">INGRESAR</span>
      </RouterLink>

      <!-- Auth: Avatar pill + dropdown -->
      <div v-else class="user-trigger" @click="userOpen = !userOpen" id="btn-user-menu">
        <span class="user-avatar" :class="isAdmin ? 'avatar-admin' : 'avatar-archer'">
          {{ userInitials }}
        </span>
        <span class="chevron">▾</span>

        <div class="dropdown-panel user-dropdown" v-show="userOpen">
          <div class="dp-header">
            <span class="dp-session-label">Sesión Activa</span>
            <span class="dp-session-name">{{ user?.email?.split('@')[0] }}</span>
          </div>
          <RouterLink to="/dashboard" class="dp-item" @click.stop="userOpen = false">
            <span class="material-icons dp-icon">dashboard</span> Resumen
          </RouterLink>

          <!-- Archer-only links -->
          <template v-if="!isAdmin">
            <RouterLink to="/profile" class="dp-item" @click.stop="userOpen = false">
              <span class="material-icons dp-icon">person</span> Mi información
            </RouterLink>
            <RouterLink to="/history" class="dp-item" @click.stop="userOpen = false">
              <span class="material-icons dp-icon">history</span> Mi historial
            </RouterLink>
          </template>

          <!-- Admin-only links -->
          <template v-if="isAdmin">
            <RouterLink to="/admin/archers" class="dp-item" @click.stop="userOpen = false" id="sidebar-manage-archers">
              <span class="material-icons dp-icon">group</span> Manage Archers
            </RouterLink>
            <RouterLink to="/admin/tournaments" class="dp-item" @click.stop="userOpen = false" id="sidebar-manage-events">
              <span class="material-icons dp-icon">emoji_events</span> Manage Events
            </RouterLink>
            <RouterLink to="/admin/scoring" class="dp-item" @click.stop="userOpen = false" id="sidebar-score-entry">
              <span class="material-icons dp-icon">sports_score</span> Score Registration
            </RouterLink>
            <RouterLink to="/admin/audit" class="dp-item" @click.stop="userOpen = false" id="sidebar-audit">
              <span class="material-icons dp-icon">policy</span> Audit Table
            </RouterLink>
          </template>

          <button class="dp-item dp-item--danger" @click.stop="handleLogout" id="btn-logout">
            <span class="material-icons dp-icon">logout</span> Cerrar sesión
          </button>
        </div>
      </div>
    </div>

    <div v-if="adminOpen || userOpen" class="dropdown-backdrop" @click="adminOpen = false; userOpen = false"></div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import logoImg from '@/components/assets/logo.png'

const route  = useRoute()
const router = useRouter()
const auth   = useAuthStore()

const user       = computed(() => auth.user)
const isLoggedIn = computed(() => auth.isLoggedIn)
const isAdmin    = computed(() => auth.isAdmin)

const adminOpen = ref(false)
const userOpen  = ref(false)

const userInitials = computed(() => {
  const email = user.value?.email || ''
  return email.split('@')[0].slice(0, 2).toUpperCase()
})

const guestNavItems = [
  { to: '/',            label: 'Inicio',   id: 'inicio' },
  { to: '/leaderboard', label: 'Rankings',  id: 'rankings' },
  { to: '/tournaments', label: 'Eventos',   id: 'eventos' },
]

const navItems = [
  { to: '/',            label: 'Inicio',    id: 'inicio' },
  { to: '/leaderboard', label: 'Rankings',  id: 'rankings' },
  { to: '/tournaments', label: 'Eventos',   id: 'eventos' },
]

const adminItems = [
  { to: '/admin/archers',     label: 'Arqueros',    icon: '🏹', id: 'arqueros' },
  { to: '/admin/tournaments', label: 'Torneos',     icon: '⚔',  id: 'torneos' },
  { to: '/admin/scoring',     label: 'Puntajes',    icon: '◎',  id: 'puntajes' },
  { to: '/admin/ranking',     label: 'Rankings',    icon: '◆',  id: 'rankings' },
  { to: '/admin/categories',  label: 'Categorías',  icon: '⊞',  id: 'categorias' },
  { to: '/admin/audit',       label: 'Auditoría',   icon: '📋', id: 'auditoria' },
]

const isAdminRoute = computed(() => route.path.startsWith('/admin'))

function isActive(to) {
  if (to === '/') return route.path === '/'
  return route.path.startsWith(to)
}

function handleLogout() {
  auth.logout()
  userOpen.value = false
  router.push('/')
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');
/* ═══════════════ Header shell ═══════════════ */
.app-header {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-height);
  background: #010A13;
  border-bottom: 2px solid #1E2328;
  display: flex;
  align-items: center;
  padding: 0 2rem;
  gap: 2rem;
  z-index: 100;
}

/* ═══════════════ Logo ═══════════════ */
.header-logo { 
  flex-shrink: 1;
  width: 30px; 
}

.header-logo img, 
.header-logo svg {
  max-width: 250px;
  width: 500px;
  height: auto;
}

.logo-link {
  display: block;
  text-decoration: none;
  transition: opacity 0.2s;
}

.logo-link:hover { opacity: 0.85; }

.logo-img {
  height: 52px; 
  width: auto;
  display: block;
}

/* ═══════════════ Nav ═══════════════ */
.header-nav {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: center;
  gap: 0;
  list-style: none;
}

.nav-li {
  position: relative;
}

.nav-link {
  display: block;
  padding: 0 1.2rem;
  line-height: var(--header-height);
  font-family: 'Cinzel', serif;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: #A09B8C;
  text-decoration: none;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;        /* NO transform in transition */
  white-space: nowrap;
  user-select: none;
}

/* Gold underline — loading sweep from left */
.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 3px;
  background: linear-gradient(to right, #C89B3C, #C8AA6E);
  transform-origin: left center;  /* sweep starts at left edge */
  transition: width 0.3s ease-out;
}

.nav-link:hover {
  color: #F0E6D2;
  /* NO transform: scale — text stays still */
}

.nav-link:hover::after {
  width: 100%;
}

/* Active: full gold underline */
.nav-link--active {
  color: #F0E6D2;
}

.nav-link--active::after {
  width: 100%;
}

/* Chevron for dropdowns */
.chevron {
  font-size: 0.6rem;
  margin-left: 3px;
  transition: transform 0.2s;
  display: inline-block;
}

.chevron.open { transform: rotate(180deg); }

/* ═══════════════ Dropdowns ═══════════════ */
.admin-trigger,
.user-trigger {
  position: relative;
  cursor: pointer;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 0.5rem;
  height: var(--header-height);
}

.dropdown-panel {
  position: absolute;
  top: calc(100% + 1px);
  left: 50%;
  transform: translateX(-50%);
  min-width: 210px;
  background: #010A13;
  border: 1px solid #1E2328;
  border-top: none;
  z-index: 200;
  overflow: hidden;
}

.user-dropdown {
  left: auto;
  right: 0;
  transform: none;
}

.dp-header {
  padding: 0.6rem 1rem;
  font-family: 'Cinzel', serif;
  font-size: 0.6rem;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #C89B3C;
  border-bottom: 1px solid #1E2328;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.dp-session-label {
  font-size: 0.55rem;
  color: #5C5B57;
  text-transform: uppercase;
  letter-spacing: 1.5px;
}

.dp-session-name {
  font-family: 'Cinzel', serif;
  font-size: 0.85rem;
  font-weight: 700;
  color: #F0E6D2;
  text-transform: none;
  letter-spacing: 0;
}

.dp-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.65rem 1rem;
  font-family: 'Cinzel', serif;
  font-size: 0.78rem;
  letter-spacing: 1px;
  color: #A09B8C;
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
  border: none;
  background: none;
  width: 100%;
  cursor: pointer;
  text-align: left;
  border-bottom: 1px solid #1E2328;
}

.dp-item:last-child { border-bottom: none; }

.dp-item:hover {
  background: rgba(200,155,60,0.06);
  color: #C89B3C;
}

.dp-item--danger { color: #e84057; }
.dp-item--danger:hover { color: #ff5a6e; background: rgba(232,64,87,0.06); }

.dp-icon { font-size: 18px; line-height: 1; flex-shrink: 0; vertical-align: middle; }

.dropdown-backdrop {
  position: fixed;
  inset: 0;
  z-index: 99;
}

/* ═══════════════ Right section ═══════════════ */
.header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: auto;
}

/* ── LoL-style INGRESAR button (SVG-rendered) ───────────── */
.ingresar-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 148px;
  height: 34px;
  text-decoration: none;
  cursor: pointer;
  flex-shrink: 0;
  transition: filter 0.3s ease, transform 0.2s ease;
}

.ingresar-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: visible;
}

.ingresar-text {
  position: relative;
  z-index: 1;
  font-family: 'Inter', sans-serif;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #C8E8F0;
  text-shadow: 0 0 8px rgba(11,196,227,0.6);
  margin-left: 4px; /* optical offset for concave left */
}

.ingresar-btn:hover {
  filter: brightness(1.18)
          drop-shadow(0 0 6px rgba(11,196,227,0.8))
          drop-shadow(0 0 18px rgba(11,196,227,0.35));
  transform: translateY(-1px);
}

.ingresar-btn:active {
  transform: scale(0.97) translateY(0);
}

/* ═══════════════ User avatar ═══════════════ */
.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  font-family: 'Cinzel', serif;
  font-size: 0.65rem;
  font-weight: 800;
  color: #010A13;
}

.avatar-admin  { background: #C89B3C; }
.avatar-archer { background: #0596aa; }

.user-trigger .chevron {
  color: #5C5B57;
  font-size: 0.6rem;
}
</style>
