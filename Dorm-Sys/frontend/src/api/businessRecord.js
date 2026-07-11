import request from '../utils/request'

export const getBusinessRecords = (type, status) => {
  return request({
    url: '/businessRecord/list',
    method: 'get',
    params: { type, status }
  })
}

export const saveBusinessRecord = (data) => {
  return request({
    url: '/businessRecord/save',
    method: 'post',
    data
  })
}

export const deleteBusinessRecord = (id) => {
  return request({
    url: `/businessRecord/${id}`,
    method: 'delete'
  })
}
