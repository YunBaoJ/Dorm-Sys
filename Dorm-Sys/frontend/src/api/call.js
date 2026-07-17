import request from '../utils/request'

export function getCalls(studentId) {
  return request({
    url: '/callRecord/list',
    method: 'get',
    params: { studentId }
  })
}

export function saveCall(data) {
  return request({
    url: data.id ? '/callRecord/update' : '/callRecord/add',
    method: 'post',
    data
  })
}

export function deleteCall(id) {
  return request({
    url: `/callRecord/${id}`,
    method: 'delete'
  })
}
