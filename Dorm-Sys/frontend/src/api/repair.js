import request from '../utils/request'

export function getRepairs(submitterId, status) {
  return request({
    url: '/api/repairRequest/list',
    method: 'get',
    params: { submitterId, status }
  })
}

export function saveRepair(data) {
  return request({
    url: '/api/repairRequest/save',
    method: 'post',
    data
  })
}

export function deleteRepair(id) {
  return request({
    url: `/api/repairRequest/${id}`,
    method: 'delete'
  })
}
