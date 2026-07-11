import request from '../utils/request'

export function getRooms(buildingId) {
  return request({
    url: '/room/list',
    method: 'get',
    params: { buildingId }
  })
}

export function saveRoom(data) {
  return request({
    url: '/room/save',
    method: 'post',
    data
  })
}

export function deleteRoom(id) {
  return request({
    url: `/room/${id}`,
    method: 'delete'
  })
}

export function getBeds(roomId) {
  return request({
    url: '/bed/list',
    method: 'get',
    params: { roomId }
  })
}

export function saveBed(data) {
  return request({
    url: '/bed/save',
    method: 'post',
    data
  })
}

export function deleteBed(id) {
  return request({
    url: `/bed/${id}`,
    method: 'delete'
  })
}
