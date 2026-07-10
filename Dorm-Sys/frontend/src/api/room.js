import request from '../utils/request'

export function getRooms(buildingId) {
  return request({
    url: '/api/room/list',
    method: 'get',
    params: { buildingId }
  })
}

export function saveRoom(data) {
  return request({
    url: '/api/room/save',
    method: 'post',
    data
  })
}

export function deleteRoom(id) {
  return request({
    url: `/api/room/${id}`,
    method: 'delete'
  })
}

export function getBeds(roomId) {
  return request({
    url: '/api/bed/list',
    method: 'get',
    params: { roomId }
  })
}

export function saveBed(data) {
  return request({
    url: '/api/bed/save',
    method: 'post',
    data
  })
}

export function deleteBed(id) {
  return request({
    url: `/api/bed/${id}`,
    method: 'delete'
  })
}
