<template>
  <el-aside :width="appStore.sidebarCollapsed ? '64px' : '240px'" class="sidebar-container">
    <div class="sidebar-logo">
      <div class="logo-icon">
        <el-icon :size="24" color="#fff"><component :is="Building2" /></el-icon>
      </div>
      <transition name="fade">
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">学生宿舍管理系统</span>
      </transition>
    </div>

    <el-menu
      :default-active="route.path"
      class="sidebar-menu"
      :collapse="appStore.sidebarCollapsed"
      :collapse-transition="false"
      router
    >
      <template v-for="item in navItems" :key="item.path">
        <!-- If there are submenus -->
        <el-sub-menu v-if="item.children" :index="item.path">
          <template #title>
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </template>
          <el-menu-item v-for="sub in item.children" :key="sub.path" :index="sub.path">
            {{ sub.label }}
          </el-menu-item>
        </el-sub-menu>

        <!-- No submenus -->
        <el-menu-item v-else :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title><span>{{ item.label }}</span></template>
        </el-menu-item>
      </template>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { appStore } from '../../store/app'
import { useUserStore } from '../../store/user'
import { Gauge, UserRound, Building, ClipboardList, UserPlus, Settings, UserCheck, Home, WalletCards, Newspaper, UserRoundCheck, Repeat2, MessageCircle, SlidersHorizontal, Building2, FileCheck } from '@lucide/vue'

const route = useRoute()
const userStore = useUserStore()

const navMap = {
  admin: [
    { path: '/admin/users/list', label: '用户管理', icon: UserRound },
    { path: '/admin/resources', label: '宿舍资源', icon: Building, children: [
      { path: '/admin/resources/buildings', label: '楼栋管理' },
      { path: '/admin/resources/rooms', label: '房间管理' }
    ]}
  ],
  dormmanager: [
    { path: '/dormmanager/workbench', label: '工作台', icon: ClipboardList },
    { path: '/dormmanager/checkin', label: '入住管理', icon: UserPlus },
    { path: '/dormmanager/repair', label: '报修处理', icon: Settings },
    { path: '/dormmanager/visitor', label: '访客登记', icon: UserCheck },
    { path: '/dormmanager/hygiene', label: '卫生检查', icon: FileCheck },
    { path: '/dormmanager/transfer', label: '调宿审批', icon: Repeat2 },
  ],
  student: [
    { path: '/student/dorm', label: '我的宿舍', icon: Home },
    { path: '/student/repair', label: '报修申请', icon: Settings },
    { path: '/student/fees', label: '费用查询', icon: WalletCards },
    { path: '/student/visitor', label: '访客预约', icon: UserRoundCheck },
    { path: '/student/transfer', label: '调宿申请', icon: Repeat2 },
  ]
}

const navItems = computed(() => navMap[userStore.role] || navMap.student)
</script>

<style scoped>
.sidebar-container {
  background-color: var(--surface);
  border-right: 1px solid var(--line);
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid var(--line);
  overflow: hidden;
  white-space: nowrap;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background-color: var(--el-color-primary);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  margin-left: 12px;
  font-weight: 600;
  font-size: 16px;
  color: var(--text);
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background-color: transparent;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: var(--primary-2);
  color: var(--primary);
  border-right: 3px solid var(--primary);
  font-weight: bold;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
