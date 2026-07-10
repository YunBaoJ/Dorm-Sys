import request from '../utils/request'

export const loginApi = (username, password, role) => {
  // 当后端真实存在时，请取消下面这行注释，并删除模拟代码
  // return request.post('/auth/login', { username, password, role })

  // ------ 这是模拟后端的临时代码开始 ------
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (username && password) {
        // 模拟登录成功，返回假 Token 和信息
        resolve({
          token: 'mock-jwt-token-1234567890',
          role: role,
          user: {
            id: 1,
            name: username,
            avatar: ''
          }
        })
      } else {
        // 模拟登录失败
        reject(new Error('账号或密码不能为空'))
      }
    }, 1000)
  })
  // ------ 模拟结束 ------
}
