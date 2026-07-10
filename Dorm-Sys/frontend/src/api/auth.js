import request from '../utils/request'

export const loginApi = (username, password, role) => {
  // 真实调用后端 Spring Boot 接口
  return request.post('/auth/login', { username, password, role })
}
