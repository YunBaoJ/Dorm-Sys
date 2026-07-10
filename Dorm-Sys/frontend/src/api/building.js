import request from '../utils/request'

export const getBuildings = () => {
  return request.get('/building/list').then(data => ({ data }))
}

export const getBuildingList = getBuildings

export const saveBuilding = (data) => {
  return request.post('/building/save', data)
}

export const deleteBuilding = (id) => {
  return request.delete(`/building/${id}`)
}
