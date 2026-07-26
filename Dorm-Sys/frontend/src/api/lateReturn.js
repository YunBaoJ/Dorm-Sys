import request from '../utils/request'

export function getLateReturns() {
  return request({
    url: '/lateReturnRecord/list',
    method: 'get'
  })
}

export function saveLateReturn(data) {
  return request({
    url: '/lateReturnRecord/save',
    method: 'post',
    data
  })
}

export function deleteLateReturn(id) {
  return request({
    url: `/lateReturnRecord/${id}`,
    method: 'delete'
  })
}
