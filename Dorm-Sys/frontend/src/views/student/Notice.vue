<template>
  <div class="notice-page">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="ScrollText" /></el-icon>
          <div>
            <h2>校园公告</h2>
            <p>查看管理员与宿管发布的最新通知</p>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="list-card" v-loading="loading">
      <div class="toolbar">
        <el-input v-model="searchQuery" placeholder="搜索公告标题..." clearable class="search-input">
          <template #prefix>
            <el-icon><component :is="Search" /></el-icon>
          </template>
        </el-input>
        <div class="toolbar-right">
          <el-radio-group v-model="filterTab" size="default">
            <el-radio-button value="all">全部（{{ totalCount }}）</el-radio-button>
            <el-radio-button value="unread">未读（{{ unreadCount }}）</el-radio-button>
            <el-radio-button value="read">已读（{{ readCount }}）</el-radio-button>
          </el-radio-group>
          <el-button @click="handleRefresh" :loading="loading">刷新</el-button>
        </div>
      </div>

      <el-empty v-if="paginatedNotices.length === 0" :description="emptyHint" />
      <div v-else class="notice-list">
        <div
          v-for="n in paginatedNotices"
          :key="n.id"
          class="notice-item"
          :class="{ unread: !n.read }"
          @click="openNoticeDialog(n)"
          role="button"
          tabindex="0"
          @keydown.enter="openNoticeDialog(n)"
          @keydown.space.prevent="openNoticeDialog(n)"
        >
          <span class="notice-dot" :class="n.read ? 'dot-gray' : 'dot-primary'" aria-hidden="true"></span>
          <div class="notice-content-wrap">
            <span class="notice-title">{{ n.title }}</span>
            <span class="notice-desc">{{ n.description?.substring(0, 80) }}{{ n.description?.length > 80 ? '...' : '' }}</span>
            <span class="notice-time">{{ n.displayTime }}</span>
          </div>
        </div>
      </div>
      <div v-if="totalPages > 1" class="pagination">
        <button class="page-btn" :disabled="currentPage <= 1" @click="currentPage--">
          <el-icon :size="16"><component :is="ChevronLeft" /></el-icon>
          <span>上一页</span>
        </button>

        <div class="page-numbers">
          <button
            v-for="p in displayPages"
            :key="typeof p === 'string' ? `e-${p}` : p"
            class="page-num"
            :class="{ active: p === currentPage, ellipsis: typeof p === 'string' }"
            :disabled="typeof p === 'string'"
            @click="typeof p !== 'string' && (currentPage = p)"
          >{{ p }}</button>
        </div>

        <button class="page-btn" :disabled="currentPage >= totalPages" @click="currentPage++">
          <span>下一页</span>
          <el-icon :size="16"><component :is="ChevronRight" /></el-icon>
        </button>
      </div>
    </el-card>

    <!-- Notice Detail Dialog -->
    <teleport to="body">
      <el-dialog v-model="noticeDialogVisible" :title="currentNotice?.title || '公告详情'" width="520px" destroy-on-close>
        <div class="notice-detail-body">
          <div class="notice-detail-time" v-if="currentNotice?.displayTime">发布时间：{{ currentNotice.displayTime }}</div>
          <div class="notice-detail-content">{{ currentNotice?.description || '暂无详细内容' }}</div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button type="primary" @click="noticeDialogVisible = false">知道了</el-button>
          </span>
        </template>
      </el-dialog>
    </teleport>
  </div>
</template>

<script setup>
import { computed, watch, onMounted, onUnmounted, ref } from 'vue'
import { ScrollText, Search, ChevronLeft, ChevronRight } from '@lucide/vue'
import { ElMessage } from 'element-plus'
import { getBusinessRecords } from '../../api/businessRecord'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const notices = ref([])
const loading = ref(false)

// Read tracking
const noticeDialogVisible = ref(false)
const currentNotice = ref({})
const seenNoticeIds = ref(new Set())

// Filter & pagination
const filterTab = ref('all')
const searchQuery = ref('')
const currentPage = ref(1)

function calcPageSize() {
  return Math.max(6, Math.floor((window.innerHeight - 330) / 85))
}
const pageSize = ref(calcPageSize())

// ---- Computed ----

const totalCount = computed(() => notices.value.length)
const unreadCount = computed(() => notices.value.filter(n => !n.read).length)
const readCount = computed(() => notices.value.filter(n => n.read).length)

const filteredNotices = computed(() => {
  let list = notices.value
  if (filterTab.value === 'unread') list = list.filter(n => !n.read)
  else if (filterTab.value === 'read') list = list.filter(n => n.read)
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(n =>
      n.title.toLowerCase().includes(q) || n.description.toLowerCase().includes(q)
    )
  }
  return list
})

const paginatedNotices = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredNotices.value.slice(start, start + pageSize.value)
})

const totalPages = computed(() => Math.ceil(filteredNotices.value.length / pageSize.value))

const displayPages = computed(() => {
  const total = totalPages.value
  const cur = currentPage.value
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }
  const pages = []
  if (cur <= 4) {
    for (let i = 1; i <= 5; i++) pages.push(i)
    pages.push('...')
    pages.push(total)
  } else if (cur >= total - 3) {
    pages.push(1)
    pages.push('...')
    for (let i = total - 4; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)
    pages.push('...')
    for (let i = cur - 1; i <= cur + 1; i++) pages.push(i)
    pages.push('...')
    pages.push(total)
  }
  return pages
})

const emptyHint = computed(() => {
  if (searchQuery.value.trim()) return '未找到匹配的公告'
  if (filterTab.value === 'unread') return '恭喜，全部已读！'
  if (filterTab.value === 'read') return '还没有已读的公告'
  return '暂无公告'
})

// ---- Handlers ----

function loadSeenNotices() {
  try {
    const key = `student-notices-read:${userStore.userInfo?.id || 'guest'}`
    const stored = JSON.parse(localStorage.getItem(key) || '[]')
    seenNoticeIds.value = new Set(Array.isArray(stored) ? stored : [])
  } catch {
    seenNoticeIds.value = new Set()
  }
}

function saveSeenNotices() {
  const key = `student-notices-read:${userStore.userInfo?.id || 'guest'}`
  localStorage.setItem(key, JSON.stringify([...seenNoticeIds.value]))
}

const openNoticeDialog = (notice) => {
  currentNotice.value = notice
  if (!seenNoticeIds.value.has(notice.id)) {
    const next = new Set(seenNoticeIds.value)
    next.add(notice.id)
    seenNoticeIds.value = next
    saveSeenNotices()
    const idx = notices.value.findIndex(n => n.id === notice.id)
    if (idx !== -1) notices.value[idx].read = true
  }
  noticeDialogVisible.value = true
}

const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').substring(0, 16)
}

async function fetchNotices() {
  loadSeenNotices()
  loading.value = true
  try {
    const [adminNotices, managerMessages] = await Promise.all([
      getBusinessRecords('admin_notice', '已发布'),
      getBusinessRecords('manager_messages', '已发布')
    ])
    notices.value = [...(adminNotices || []), ...(managerMessages || [])]
      .map(r => ({
        id: r.id,
        title: r.title || '',
        description: r.description || '',
        displayTime: formatTime(r.eventTime || r.createTime),
        read: seenNoticeIds.value.has(r.id)
      }))
      .sort((a, b) => {
        if (a.read !== b.read) return a.read ? 1 : -1
        return 0
      })
  } catch (error) {
    console.error(error)
    ElMessage.error('获取公告失败')
  } finally {
    loading.value = false
  }
}

function handleRefresh() {
  currentPage.value = 1
  fetchNotices()
}

// ---- Watchers ----

watch([filterTab, searchQuery], () => { currentPage.value = 1 })

function onResize() {
  pageSize.value = calcPageSize()
}

// ---- Lifecycle ----

onMounted(() => {
  fetchNotices()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.notice-page {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-card {
  margin-bottom: 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02));
  border: none;
}

.hero-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-text {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-text h2 {
  margin: 0 0 4px;
  font-size: 22px;
  color: var(--text);
}

.hero-text p {
  margin: 0;
  color: var(--sub);
  font-size: 14px;
}

.list-card {
  border-radius: 12px;
  border: 1px solid var(--border);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 280px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 10px;
  background: var(--bg);
  cursor: pointer;
  transition: background 0.2s;
}

.notice-item:hover {
  background: var(--el-color-primary-light-9);
}

.notice-item.unread {
  background: var(--primary-2);
}

.notice-dot {
  flex: 0 0 auto;
  width: 8px;
  height: 8px;
  margin-top: 7px;
  border-radius: 50%;
}

.dot-gray { background: var(--line); }
.dot-primary { background: var(--el-color-primary); }

.notice-content-wrap {
  display: flex;
  flex-direction: column;
  gap: 5px;
  min-width: 0;
  flex: 1;
}

.notice-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  line-height: 1.4;
}

.notice-item.unread .notice-title {
  color: var(--el-color-primary);
}

.notice-desc {
  font-size: 13px;
  color: var(--sub);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notice-time {
  font-size: 12px;
  color: var(--muted);
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding-top: 20px;
  flex-wrap: wrap;
}

.page-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg);
  color: var(--sub);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  background: var(--primary-1);
}

.page-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 3px;
}

.page-num {
  min-width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: var(--sub);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.page-num:hover:not(:disabled) {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.page-num.active {
  background: var(--el-color-primary);
  color: #fff;
  font-weight: 600;
  border-color: var(--el-color-primary);
}

.page-num.ellipsis {
  cursor: default;
  color: var(--muted);
  letter-spacing: 1px;
}

/* Dialog */
.notice-detail-body {
  padding: 0 4px;
}

.notice-detail-time {
  font-size: 13px;
  color: var(--sub);
  margin-bottom: 16px;
}

.notice-detail-content {
  font-size: 15px;
  color: var(--text);
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
