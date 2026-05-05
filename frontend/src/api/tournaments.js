import api from './axios'

export const getTournaments    = ()         => api.get('/tournaments')
export const getTournamentById = (id)       => api.get(`/tournaments/${id}`)
export const createTournament  = (data)     => api.post('/tournaments', data)
export const updateTournament  = (id, data) => api.put(`/tournaments/${id}`, data)
export const deleteTournament  = (id)       => api.delete(`/tournaments/${id}`)
export const getPodium         = (id)       => api.get(`/tournaments/${id}/podium`)
