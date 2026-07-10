import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import router from '../router'

// 1. 创建 Axios 实例 (中央邮局)
const service = axios.create({
  baseURL: '/api', // 后端接口基础路径
  timeout: 5000 // 请求超时时间 (5秒)
})

// 2. 请求拦截器 (给每次请求贴上 Token 邮票)
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    // 如果本地有 token，就自动带在 Header 里
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 3. 响应拦截器 (统一错误处理中心)
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果后端直接返回数据（没有 code 包装）
    if (res.code === undefined) {
      return res;
    }
    
    // 如果有 code 包装且不为 200
    if (res.code !== 200) {
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res.data
    }
  },
  (error) => {
    const { response } = error
    let message = '请求失败'
    if (response && response.status) {
      switch (response.status) {
        case 401:
          message = '登录状态过期，请重新登录'
          // 自动跳转回登录页
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          message = '您没有权限访问此接口'
          break
        case 404:
          message = '请求的接口不存在'
          break
        case 500:
          message = '后端服务器异常，请联系管理员'
          break
        default:
          message = `网络异常 (${response.status})`
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时，请检查网络'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
