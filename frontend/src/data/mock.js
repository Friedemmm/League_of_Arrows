/**
 * Mock data matching the database seeder.sql.
 * Used as fallback when the backend is not running.
 */

export const mockCategories = [
  { idCategory: 1, name: 'Largo' },
  { idCategory: 2, name: 'Recurvo' },
  { idCategory: 3, name: 'Compuesto' },
  { idCategory: 4, name: 'Tradicional' },
]

export const mockArchers = [
  { idArcher: 1, name: 'Ashe',    email: 'ashe@gmail.com',    categoryName: 'Largo',       categoryId: 1 },
  { idArcher: 2, name: 'Varus',   email: 'varus@gmail.com',   categoryName: 'Largo',       categoryId: 1 },
  { idArcher: 3, name: 'Kindred', email: 'kindred@gmail.com', categoryName: 'Largo',       categoryId: 1 },
  { idArcher: 4, name: 'Vayne',   email: 'vayne@gmail.com',   categoryName: 'Recurvo',     categoryId: 2 },
  { idArcher: 5, name: 'Quinn',   email: 'quinn@gmail.com',   categoryName: 'Compuesto',   categoryId: 3 },
  { idArcher: 6, name: 'Twitch',  email: 'twitch@gmail.com',  categoryName: 'Tradicional', categoryId: 4 },
]

export const mockTournaments = [
  {
    idTournament: 1,
    name: 'Campeonato Mundial de Tiro 2026',
    categoryName: 'Largo',
    idCategory: 1,
    startDate: new Date(Date.now() - 5 * 86400000).toISOString().split('T')[0],
    endDate:   new Date(Date.now() + 2 * 86400000).toISOString().split('T')[0],
    active: true,
  },
  {
    idTournament: 2,
    name: 'MSI 2025',
    categoryName: 'Largo',
    idCategory: 1,
    startDate: '2025-10-20',
    endDate:   '2025-11-05',
    active: false,
  },
  {
    idTournament: 3,
    name: 'Copa Valinor',
    categoryName: 'Recurvo',
    idCategory: 2,
    startDate: new Date(Date.now() + 5 * 86400000).toISOString().split('T')[0],
    endDate:   new Date(Date.now() + 12 * 86400000).toISOString().split('T')[0],
    active: true,
  },
]

export const mockTopArchers = [
  { archerId: 1, archerName: 'Ashe',    totalScore: 680, avgPointsPerArrow: 9.12, categoryName: 'Largo' },
  { archerId: 2, archerName: 'Varus',   totalScore: 675, avgPointsPerArrow: 9.08, categoryName: 'Largo' },
  { archerId: 3, archerName: 'Kindred', totalScore: 670, avgPointsPerArrow: 9.05, categoryName: 'Largo' },
  { archerId: 4, archerName: 'Vayne',   totalScore: 662, avgPointsPerArrow: 8.91, categoryName: 'Recurvo' },
  { archerId: 5, archerName: 'Quinn',   totalScore: 655, avgPointsPerArrow: 8.78, categoryName: 'Compuesto' },
  { archerId: 6, archerName: 'Twitch',  totalScore: 640, avgPointsPerArrow: 8.54, categoryName: 'Tradicional' },
]

export const mockRankings = [
  { idRanking: 1, idTournament: 2, idArcher: 1, archerName: 'Ashe',    totalScore: 680, position: 1 },
  { idRanking: 2, idTournament: 2, idArcher: 2, archerName: 'Varus',   totalScore: 675, position: 2 },
  { idRanking: 3, idTournament: 2, idArcher: 3, archerName: 'Kindred', totalScore: 670, position: 3 },
]

export const mockHistory = [
  { tournamentName: 'MSI 2025', score: 680, position: 1, endDate: '2025-11-05', categoryName: 'Largo' },
  { tournamentName: 'Campeonato Mundial de Tiro 2026', score: 58, position: null, endDate: null, categoryName: 'Largo' },
]

export const mockAuditLogs = [
  { idAudit: 1, idArcher: 1, idTournament: 1, oldScore: 57, newScore: 58, modifiedBy: 1, modifiedAt: new Date().toISOString() },
]
