import request from '../utils/request'

export const getHygieneRecords = (roomId) => {
  return request({
    url: '/hygieneRecord/list',
    method: 'get',
    params: { roomId }
  })
}

export const saveHygieneRecord = (data) => {
  return request({
    url: '/hygieneRecord/save',
    method: 'post',
    data
  })
}

export const deleteHygieneRecord = (id) => {
  return request({
    url: `/hygieneRecord/${id}`,
    method: 'delete'
  })
}
