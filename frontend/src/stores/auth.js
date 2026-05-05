import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/axios'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('loa_token') || null)
  const user  = ref(JSON.parse(localStorage.getItem('loa_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin    = computed(() => user.value?.role === 'ADMIN')
  const isArcher   = computed(() => user.value?.role === 'ARQUERO')

  async function login(email, password) {
    const res = await api.post('/auth/login', { email, password })
    const { token: jwt, rol, userId } = res.data
    token.value = jwt
    user.value  = { email, role: rol, userId }
    localStorage.setItem('loa_token', jwt)
    localStorage.setItem('loa_user', JSON.stringify(user.value))
    return user.value
  }

  function logout() {
    token.value = null
    user.value  = null
    localStorage.removeItem('loa_token')
    localStorage.removeItem('loa_user')
  }

  return { token, user, isLoggedIn, isAdmin, isArcher, login, logout }
})
