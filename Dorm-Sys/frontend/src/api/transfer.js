import request from '../utils/request'

export function getTransfers(studentId, status) {
  return request({
    url: '/api/transferRequest/list',
    method: 'get',
    params: { studentId, status }
  })
}

export function saveTransfer(data) {
  return request({
    url: '/api/transferRequest/save',
    method: 'post',
    data
  })
}

export function deleteTransfer(id) {
  return request({
    url: `/api/transferRequest/${id}`,
    method: 'delete'
  })
}
