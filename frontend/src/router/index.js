import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const routes = [
  // Public
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/Login.vue'),
    meta: { public: true },
  },

  // Public pages (no auth required)
  {
    path: '/',
    name: 'Home',
    component: () => import('../pages/Home.vue'),
    meta: { public: true },
  },
  {
    path: '/leaderboard',
    name: 'Leaderboard',
    component: () => import('../pages/Leaderboard.vue'),
    meta: { public: true },
  },
  {
    path: '/tournaments',
    name: 'Tournaments',
    component: () => import('../pages/Tournaments.vue'),
    meta: { public: true },
  },

  // Auth-required pages
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../pages/Dashboard.vue'),
  },
  {
    path: '/history',
    name: 'ArcherHistory',
    component: () => import('../pages/ArcherHistory.vue'),
  },
  {
    path: '/profile',
    name: 'ArcherProfile',
    component: () => import('../pages/ArcherProfile.vue'),
  },
  {
    path: '/trending',
    name: 'Trending',
    component: () => import('../pages/Trending.vue'),
  },

  // Admin (requires ADMIN role)
  {
    path: '/admin/archers',
    name: 'AdminArchers',
    component: () => import('../pages/admin/AdminArchers.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/admin/tournaments',
    name: 'AdminTournaments',
    component: () => import('../pages/admin/AdminTournaments.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/admin/scoring',
    name: 'AdminScoring',
    component: () => import('../pages/admin/AdminScoring.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/admin/ranking',
    name: 'AdminRanking',
    component: () => import('../pages/admin/AdminRanking.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/admin/audit',
    name: 'AdminAudit',
    component: () => import('../pages/admin/AdminAudit.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/admin/categories',
    name: 'AdminCategories',
    component: () => import('../pages/admin/AdminCategories.vue'),
    meta: { requiresAdmin: true },
  },

  // Catch-all
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() { return { top: 0 } },
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLoggedIn) return '/login'
  if (to.meta.requiresAdmin && !auth.isAdmin) return '/'
  if (to.path === '/login' && auth.isLoggedIn) return '/'
})

export default router
