import request from '../utils/request'

export const getFeeBills = (roomId, status) => {
  return request({
    url: '/feeBill/list',
    method: 'get',
    params: { roomId, status }
  })
}

export const saveFeeBill = (data) => {
  return request({
    url: '/feeBill/save',
    method: 'post',
    data
  })
}

export const deleteFeeBill = (id) => {
  return request({
    url: `/feeBill/${id}`,
    method: 'delete'
  })
}
