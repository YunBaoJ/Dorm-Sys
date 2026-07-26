import request from '../utils/request'

export const getAdminReport = () => request.get('/admin/report/summary')
