import request from '../utils/request'

export const getUserList = () => {
  return request.get('/user/list')
}

export const getUsers = getUserList

export const saveUser = (data) => {
  return request.post('/user/save', data)
}

export const deleteUser = (id) => {
  return request.delete(`/user/${id}`)
}
