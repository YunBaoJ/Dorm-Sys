import request from '../utils/request'

export const getBuildingList = () => {
  return request.get('/building/list')
}

export const saveBuilding = (data) => {
  return request.post('/building/save', data)
}

export const deleteBuilding = (id) => {
  return request.delete(`/building/${id}`)
}

export const getRoomList = () => {
  return request.get('/room/list')
}

export const saveRoom = (data) => {
  return request.post('/room/save', data)
}

export const deleteRoom = (id) => {
  return request.delete(`/room/${id}`)
}
