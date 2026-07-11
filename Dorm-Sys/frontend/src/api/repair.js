import request from '../utils/request'

export function getRepairs(submitterId, status) {
  return request({
    url: '/repairRequest/list',
    method: 'get',
    params: { submitterId, status }
  })
}

export function saveRepair(data) {
  return request({
    url: '/repairRequest/save',
    method: 'post',
    data
  })
}

export function deleteRepair(id) {
  return request({
    url: `/repairRequest/${id}`,
    method: 'delete'
  })
}
