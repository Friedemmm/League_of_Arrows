import api from './axios'

export const getArchers       = ()        => api.get('/archers')
export const getArcherById    = (id)      => api.get(`/archers/${id}`)
export const createArcher     = (data)    => api.post('/archers', data)
export const updateArcher     = (id, data)=> api.put(`/archers/${id}`, data)
export const deleteArcher     = (id)      => api.delete(`/archers/${id}`)
export const getMyHistory     = ()        => api.get('/archers/me/history')
export const getTopMonth      = ()        => api.get('/archers/top-month')
export const getLeaderboard   = ()        => api.get('/archers/leaderboard')
