import request from '../utils/request'

export function getItems() {
  return request({
    url: '/itemRecord/list',
    method: 'get'
  })
}

export function saveItem(data) {
  return request({
    url: data.id ? '/itemRecord/update' : '/itemRecord/add',
    method: 'post',
    data
  })
}

export function deleteItem(id) {
  return request({
    url: `/itemRecord/${id}`,
    method: 'delete'
  })
}
