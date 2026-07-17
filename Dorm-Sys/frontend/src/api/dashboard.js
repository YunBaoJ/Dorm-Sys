import request from '../utils/request'

export function getStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

export function getBuildingStats() {
  return request({
    url: '/dashboard/buildings',
    method: 'get'
  })
}

export function getAlerts() {
  return request({
    url: '/dashboard/alerts',
    method: 'get'
  })
}
