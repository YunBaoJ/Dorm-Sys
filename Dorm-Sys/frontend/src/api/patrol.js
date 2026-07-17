import request from '../utils/request'

export function getPatrols() {
  return request({
    url: '/patrolRecord/list',
    method: 'get'
  })
}

export function savePatrol(data) {
  return request({
    url: data.id ? '/patrolRecord/update' : '/patrolRecord/add',
    method: 'post',
    data
  })
}

export function deletePatrol(id) {
  return request({
    url: `/patrolRecord/${id}`,
    method: 'delete'
  })
}
