import request from '../utils/request'

export function getItems() {
  return request({
    url: '/itemRecord/list',
    method: 'get'
  })
}

export function saveItem(data) {
  return request({
    url: '/itemRecord/save',
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
