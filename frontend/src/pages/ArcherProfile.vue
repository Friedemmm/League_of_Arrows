<template>
  <div class="page-wrapper profile-page">
    <div class="container" style="max-width:700px;">

      <div class="page-header">
        <button class="btn-back" id="btn-back-profile" @click="$router.push('/dashboard')">
          <span class="material-icons">arrow_back</span> Volver
        </button>
        <h1 class="page-title">
          <span class="material-icons page-title-icon">person</span>
          Perfil de Arquero
        </h1>
        <hr class="page-rule" />
      </div>

      <div class="profile-card lol-card">
        <div class="profile-avatar">
          <svg width="64" height="64" viewBox="0 0 32 32" fill="none">
            <polygon points="16,2 20,12 30,12 22,18 25,29 16,23 7,29 10,18 2,12 12,12"
              fill="none" stroke="#c89b3c" stroke-width="1.5"/>
            <circle cx="16" cy="16" r="4" fill="#c89b3c" opacity="0.8"/>
          </svg>
        </div>
        <div class="profile-info">
          <h2 class="profile-name">{{ auth.user?.email?.split('@')[0] }}</h2>
          <p class="profile-email">{{ auth.user?.email }}</p>
          <div class="profile-badges">
            <span class="badge" :class="auth.isAdmin ? 'badge-gold' : 'badge-blue'">
              {{ auth.isAdmin ? 'Administrador' : 'Arquero' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Account Details -->
      <div class="lol-card mt-2">
        <h3 class="detail-section-title">Datos de Cuenta</h3>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">Correo</span>
            <span class="detail-value">{{ auth.user?.email }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Rol</span>
            <span class="detail-value">{{ auth.isAdmin ? 'Administrador' : 'Arquero' }}</span>
          </div>
        </div>
      </div>

      <!-- Navigation shortcuts -->
      <div class="profile-actions mt-3">
        <RouterLink to="/history"  class="btn btn-outline" id="profile-btn-history">Ver Historial</RouterLink>
        <RouterLink to="/leaderboard" class="btn btn-outline" id="profile-btn-leaderboard">Leaderboard</RouterLink>
        <button class="btn btn-danger" id="profile-btn-logout" @click="handleLogout">Cerrar Sesión</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth   = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

.profile-page {
  padding: calc(var(--header-height, 70px) + 2rem) 0 4rem;
  min-height: 100vh;
}

.page-header { margin-bottom: 1.5rem; }

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
  display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0;
}
.page-title-icon { font-size: 1.3rem; color: var(--lol-gold); }
.page-rule { margin: 0.8rem 0 1.5rem; }
.profile-card {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 2rem;
}

.profile-avatar {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  background: rgba(200,155,60,0.08);
  border: 1px solid rgba(200,155,60,0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-name {
  font-size: 1.4rem;
  color: var(--lol-gold-light);
  margin-bottom: 0.2rem;
}

.profile-email {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-bottom: 0.6rem;
}

.profile-badges { display: flex; gap: 0.5rem; }

.detail-section-title {
  font-family: 'Cinzel', serif;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--lol-gold);
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--lol-border);
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 0.8rem;
  border-bottom: 1px solid rgba(30,58,95,0.3);
}

.detail-item:last-child { border-bottom: none; padding-bottom: 0; }

.detail-label {
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-muted);
  font-family: 'Cinzel', serif;
}

.detail-value {
  font-size: 0.88rem;
  color: var(--text-primary);
}

.profile-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}
</style>
