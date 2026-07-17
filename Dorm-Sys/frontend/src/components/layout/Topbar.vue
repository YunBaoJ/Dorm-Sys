<template>
  <el-header class="topbar-container" height="60px">
    <div class="topbar-left">
      <el-icon class="toggle-icon" @click="toggleSidebar"><component :is="MenuIcon" /></el-icon>
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item>{{ groupName }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentPageName }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <div class="topbar-right">
      <el-icon class="action-icon" @click="refreshPage"><component :is="RefreshCw" :class="{'is-rotating': isRefreshing}" /></el-icon>
      <el-icon class="action-icon" @click="toggleTheme"><component :is="appStore.theme === 'dark' ? Sun : Moon" /></el-icon>
      
      <el-badge :hidden="true" class="bell-badge">
        <el-icon class="action-icon"><component :is="Bell" /></el-icon>
      </el-badge>
      
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-profile">
          <el-avatar :size="32" :src="roleInfo.avatarImg">{{ roleInfo.avatarText }}</el-avatar>
          <span class="user-name">{{ roleInfo.user }}</span>
          <el-icon><component :is="ChevronDown" /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { appStore } from '../../store/app'
import { useUserStore } from '../../store/user'
import { Menu as MenuIcon, Moon, Sun, RefreshCw, ChevronDown, Bell } from '@lucide/vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const roles = {
  admin: { brand: "宿舍管理 · 管理端", user: "系统管理员", avatarText: "管" },
  dormmanager: { brand: "宿舍管理 · 宿管端", user: "宿管", avatarText: "管" },
  student: { brand: "学生宿舍管理系统", user: "学生", avatarText: "学", avatarImg: "/images/avatar.jpg" },
}

const roleInfo = computed(() => {
  const base = roles[userStore.role] || roles.student
  return {
    ...base,
    user: userStore.userInfo?.name || base.user,
    avatarText: userStore.userInfo?.name ? userStore.userInfo.name.charAt(0) : base.avatarText
  }
})

const groupMap = { admin: "系统管理", dormmanager: "宿管服务", student: "学生服务" }
const groupName = computed(() => groupMap[userStore.role])

const currentPageName = computed(() => {
  const pathMap = {
    '/student/desk': '服务台',
    '/student/dorm': '我的宿舍',
    '/admin/overview': '管理概览',
    // add others as needed
  }
  return pathMap[route.path] || "当前页面"
})

function toggleSidebar() {
  appStore.sidebarCollapsed = !appStore.sidebarCollapsed
}

function toggleTheme() {
  appStore.theme = appStore.theme === 'dark' ? 'light' : 'dark'
}

const isRefreshing = ref(false)
function refreshPage() {
  isRefreshing.value = true
  setTimeout(() => {
    isRefreshing.value = false
    ElMessage.success('页面已刷新')
  }, 500)
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
    ElMessage.info('已退出登录')
  }
}
</script>

<style scoped>
.topbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--surface);
  border-bottom: 1px solid var(--line);
  padding: 0 20px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-icon {
  font-size: 20px;
  cursor: pointer;
  color: var(--sub);
  transition: color 0.2s;
}

.toggle-icon:hover {
  color: var(--el-color-primary);
}

.breadcrumb {
  font-size: 14px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-icon {
  font-size: 20px;
  cursor: pointer;
  color: var(--sub);
  transition: color 0.2s;
}

.action-icon:hover {
  color: var(--el-color-primary);
}

.bell-badge {
  line-height: 1;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-name {
  font-size: 14px;
  color: var(--text);
  font-weight: 500;
}

.is-rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
