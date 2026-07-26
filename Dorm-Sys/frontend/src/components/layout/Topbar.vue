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
      <button class="action-button" type="button" title="刷新页面" :disabled="isRefreshing" @click="refreshPage">
        <RefreshCw :class="{ 'is-rotating': isRefreshing }" />
      </button>
      <el-icon class="action-icon" @click="toggleTheme"><component :is="appStore.theme === 'dark' ? Sun : Moon" /></el-icon>

      <el-popover
        v-model:visible="notificationVisible"
        placement="bottom-end"
        :width="360"
        trigger="click"
        popper-class="notification-popover"
      >
        <template #reference>
          <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="bell-badge">
            <button class="action-button" type="button" title="消息通知" aria-label="消息通知">
              <Bell />
            </button>
          </el-badge>
        </template>

        <div class="notification-panel">
          <div class="notification-header">
            <div>
              <strong>消息通知</strong>
              <span v-if="unreadCount">{{ unreadCount }} 条未读</span>
            </div>
            <button v-if="unreadCount" type="button" @click="markAllRead">全部已读</button>
          </div>

          <div v-if="loadingNotifications && notifications.length === 0" class="notification-empty">正在获取消息...</div>
          <div v-else-if="notifications.length === 0" class="notification-empty">暂无新消息</div>
          <div v-else class="notification-list">
            <button
              v-for="item in notifications"
              :key="item.key"
              class="notification-item"
              :class="{ unread: !isRead(item) }"
              type="button"
              @click="openNotification(item)"
            >
              <span class="notification-dot" aria-hidden="true"></span>
              <span class="notification-copy">
                <span class="notification-title">
                  <span class="notification-kind">{{ item.kindLabel }}</span>
                  {{ item.title }}
                </span>
                <span class="notification-content">{{ item.content }}</span>
                <span class="notification-time">{{ formatNotificationTime(item.createTime) }}</span>
              </span>
            </button>
          </div>
        </div>
      </el-popover>
      
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
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { appStore } from '../../store/app'
import { useUserStore } from '../../store/user'
import { Menu as MenuIcon, Moon, Sun, RefreshCw, ChevronDown, Bell } from '@lucide/vue'
import { ElLoading, ElMessage } from 'element-plus'
import request from '../../utils/request'

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

const pageNameMap = {
  '/student/desk': '服务台',
  '/student/dorm': '我的宿舍',
  '/student/repair': '报修申请',
  '/student/fees': '缴费管理',
  '/student/notice': '通知公告',
  '/student/visitor': '访客预约',
  '/student/transfer': '调宿申请',
  '/student/ai': 'AI 助手',
  '/student/call': '通话预约',
  '/student/feedback': '意见反馈',
  '/student/settings': '个人设置',
  '/dormmanager/workbench': '工作台',
  '/dormmanager/checkin': '住宿管理',
  '/dormmanager/fee': '费用管理',
  '/dormmanager/repair': '报修管理',
  '/dormmanager/visitor': '访客管理',
  '/dormmanager/hygiene': '卫生检查',
  '/dormmanager/transfer': '调宿管理',
  '/dormmanager/late-return': '晚归管理',
  '/dormmanager/items': '物品管理',
  '/dormmanager/messages': '消息通知',
  '/dormmanager/call': '来电管理',
  '/dormmanager/feedback': '意见反馈',
  '/dormmanager/profile': '个人信息',
  '/admin/overview': '管理概览',
  '/admin/users/list': '用户管理',
  '/admin/users/roles': '角色管理',
  '/admin/resources/buildings': '楼栋管理',
  '/admin/resources/rooms': '房间管理',
  '/admin/repairs/list': '报修管理',
  '/admin/notices': '通知管理',
  '/admin/operation-logs': '操作日志',
  '/admin/reports': '数据报表',
}

const currentPageName = computed(() => {
  const exact = pageNameMap[route.path]
  if (exact) return exact
  for (const [prefix, name] of Object.entries(pageNameMap)) {
    if (route.path.startsWith(prefix + '/') || route.path.startsWith(prefix + '?')) {
      return name
    }
  }
  return '当前页面'
})

function toggleSidebar() {
  appStore.sidebarCollapsed = !appStore.sidebarCollapsed
}

function toggleTheme() {
  appStore.theme = appStore.theme === 'dark' ? 'light' : 'dark'
}

const isRefreshing = ref(false)
function refreshPage() {
  if (isRefreshing.value) return
  isRefreshing.value = true
  ElLoading.service({
    lock: true,
    text: '正在刷新页面...',
    background: 'rgba(241, 245, 249, 0.72)'
  })
  setTimeout(() => {
    window.location.reload()
  }, 450)
}

const notificationVisible = ref(false)
const loadingNotifications = ref(false)
const notifications = ref([])
const seenNotificationKeys = ref(new Set())
let notificationTimer = null

const notificationStorageKey = computed(() => {
  const accountId = userStore.userInfo?.id || userStore.userInfo?.username || 'guest'
  return `dorm-notifications-read:${accountId}`
})

const unreadCount = computed(() => notifications.value.filter(item => !isRead(item)).length)

function loadSeenNotifications() {
  try {
    const stored = JSON.parse(localStorage.getItem(notificationStorageKey.value) || '[]')
    seenNotificationKeys.value = new Set(Array.isArray(stored) ? stored : [])
  } catch {
    seenNotificationKeys.value = new Set()
  }
}

function saveSeenNotifications() {
  localStorage.setItem(notificationStorageKey.value, JSON.stringify([...seenNotificationKeys.value].slice(-300)))
}

function isRead(item) {
  return seenNotificationKeys.value.has(item.key)
}

function markRead(item) {
  if (isRead(item)) return
  const next = new Set(seenNotificationKeys.value)
  next.add(item.key)
  seenNotificationKeys.value = next
  saveSeenNotifications()
}

function markAllRead() {
  const next = new Set(seenNotificationKeys.value)
  notifications.value.forEach(item => next.add(item.key))
  seenNotificationKeys.value = next
  saveSeenNotifications()
}

async function fetchNotifications() {
  if (userStore.role !== 'student' || !userStore.token) {
    notifications.value = []
    return
  }

  loadingNotifications.value = true
  try {
    const [noticeRecords, chatMessages] = await Promise.all([
      request({
        url: '/businessRecord/list',
        method: 'get',
        params: { type: 'manager_messages', status: '已发布' }
      }),
      request({ url: '/chat/notifications', method: 'get' })
    ])

    const notices = (noticeRecords || []).map(item => ({
      key: `notice:${item.id}`,
      kindLabel: '公告',
      title: item.title || '宿管发布了新公告',
      content: item.description || '点击查看公告详情',
      createTime: item.createTime,
      route: '/student/notice'
    }))
    const chats = (chatMessages || []).map(item => ({
      key: `chat:${item.id}`,
      kindLabel: item.type === 'GROUP' ? '群聊' : '私聊',
      title: `${item.senderName || '室友'}发来新消息`,
      content: item.content,
      createTime: item.createTime,
      route: '/student/dorm'
    }))

    notifications.value = [...notices, ...chats]
      .sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))
      .slice(0, 30)
  } catch (error) {
    console.error('Failed to fetch topbar notifications', error)
  } finally {
    loadingNotifications.value = false
  }
}

function openNotification(item) {
  markRead(item)
  notificationVisible.value = false
  router.push(item.route)
}

function formatNotificationTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const now = new Date()
  const diffMinutes = Math.floor((now - date) / 60000)
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes} 分钟前`
  if (diffMinutes < 1440) return `${Math.floor(diffMinutes / 60)} 小时前`
  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function restartNotificationPolling() {
  if (notificationTimer) clearInterval(notificationTimer)
  notificationTimer = null
  loadSeenNotifications()
  notifications.value = []
  if (userStore.role === 'student' && userStore.token) {
    fetchNotifications()
    notificationTimer = setInterval(fetchNotifications, 8000)
  }
}

watch(() => [userStore.role, userStore.userInfo?.id, userStore.token], restartNotificationPolling)

onMounted(restartNotificationPolling)
onBeforeUnmount(() => {
  if (notificationTimer) clearInterval(notificationTimer)
})

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

.action-button {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--sub);
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}

.action-button svg {
  width: 20px;
  height: 20px;
}

.action-button:hover {
  background: var(--primary-2);
  color: var(--el-color-primary);
}

.action-button:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}

.action-button:disabled {
  cursor: wait;
}

.action-icon:hover {
  color: var(--el-color-primary);
}

.bell-badge {
  line-height: 1;
}

.notification-panel {
  margin: -4px;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--line);
  padding: 12px 14px;
}

.notification-header > div {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.notification-header strong {
  color: var(--text);
  font-size: 15px;
}

.notification-header span,
.notification-time {
  color: var(--sub);
  font-size: 12px;
}

.notification-header button {
  border: 0;
  background: transparent;
  color: var(--el-color-primary);
  cursor: pointer;
  font-size: 12px;
}

.notification-list {
  max-height: 360px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: 10px;
  width: 100%;
  border: 0;
  border-bottom: 1px solid var(--line);
  padding: 13px 14px;
  background: transparent;
  color: inherit;
  cursor: pointer;
  text-align: left;
  transition: background-color 0.18s;
}

.notification-item:last-child {
  border-bottom: 0;
}

.notification-item:hover,
.notification-item.unread {
  background: var(--primary-2);
}

.notification-dot {
  flex: 0 0 auto;
  width: 7px;
  height: 7px;
  margin-top: 7px;
  border-radius: 50%;
  background: transparent;
}

.notification-item.unread .notification-dot {
  background: var(--el-color-primary);
}

.notification-copy {
  display: grid;
  min-width: 0;
  gap: 5px;
}

.notification-title {
  overflow: hidden;
  color: var(--text);
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-kind {
  display: inline-block;
  margin-right: 5px;
  border-radius: 4px;
  padding: 2px 5px;
  background: rgba(59, 130, 246, 0.12);
  color: var(--el-color-primary);
  font-size: 11px;
}

.notification-content {
  overflow: hidden;
  color: var(--sub);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-empty {
  padding: 36px 16px;
  color: var(--sub);
  font-size: 13px;
  text-align: center;
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
