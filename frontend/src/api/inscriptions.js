import api from './axios'

export const getInscriptions    = ()      => api.get('/inscriptions')
export const createInscription  = (data)  => api.post('/inscriptions', data)
export const deleteInscription  = (id)    => api.delete(`/inscriptions/${id}`)
