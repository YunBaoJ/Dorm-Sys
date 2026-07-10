import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const role = ref('student') // 'student', 'dormmanager', 'admin'
  const userInfo = ref({})

  const setToken = (newToken) => {
    token.value = newToken
  }

  const setRole = (newRole) => {
    role.value = newRole
  }

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  const logout = () => {
    token.value = ''
    role.value = 'student'
    userInfo.value = {}
  }

  return { token, role, userInfo, setToken, setRole, setUserInfo, logout }
}, {
  persist: true // Enable pinia-plugin-persistedstate to save to localStorage automatically
})
