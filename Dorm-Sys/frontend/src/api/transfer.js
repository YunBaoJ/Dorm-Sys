import request from '../utils/request'

export function getTransfers(studentId, status) {
  return request({
    url: '/transferRequest/list',
    method: 'get',
    params: { studentId, status }
  })
}

export function saveTransfer(data) {
  return request({
    url: '/transferRequest/save',
    method: 'post',
    data
  })
}

export function deleteTransfer(id) {
  return request({
    url: `/transferRequest/${id}`,
    method: 'delete'
  })
}
