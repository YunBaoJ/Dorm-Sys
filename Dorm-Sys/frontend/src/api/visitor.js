import request from '../utils/request'

export const getVisitorRecords = (studentId) => {
  return request({
    url: '/visitorRecord/list',
    method: 'get',
    params: { studentId }
  })
}

export const saveVisitorRecord = (data) => {
  return request({
    url: '/visitorRecord/save',
    method: 'post',
    data
  })
}

export const deleteVisitorRecord = (id) => {
  return request({
    url: `/visitorRecord/${id}`,
    method: 'delete'
  })
}
