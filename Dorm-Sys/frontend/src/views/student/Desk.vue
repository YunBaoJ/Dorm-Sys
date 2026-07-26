<template>
  <div class="desk-container">
    <!-- Hero Section -->
    <div class="hero dash-hero">
      <div class="hero-inner">
        <div class="hero-left">
          <!-- Empty avatar for placeholder, style kept from prototype -->
          <div class="hero-avatar">
            {{ (userStore.userInfo?.name || '同')[0] }}
          </div>
          <div>
            <h1 class="hero-greeting">{{ greeting }}，{{ userStore.userInfo?.name || '同学' }} 👋</h1>
            <p class="hero-time">{{ currentTime }}</p>
          </div>
        </div>
        <div class="hero-actions">
          <WeatherWidget />
        </div>
      </div>
    </div>

    <div class="grid">
      <!-- Left Stack -->
      <div class="left-stack">
        <!-- Banner -->
        <div class="card dash-banner"
             @mouseover="hoverBanner = true" @mouseleave="hoverBanner = false"
             :style="hoverBanner ? 'transform: translateY(-2px); box-shadow: 0 8px 24px rgba(59, 130, 246, .12);' : 'box-shadow: var(--shadow);'">
          <div class="banner-inner">
            <div class="banner-left">
              <div class="banner-icon"
                   :style="hoverBanner ? 'transform: scale(1.05)' : ''">
                <el-icon :size="24"><component :is="Home" /></el-icon>
              </div>
              <div>
                <h2 class="banner-title">文明寝室 · 共同维护</h2>
                <p class="banner-desc">本周将进行月度卫生大检查，请保持寝室整洁</p>
              </div>
            </div>
            <button class="ghost-btn" @click="router.push('/student/notice')">查看详情 →</button>
          </div>
        </div>

        <!-- Service Cards -->
        <div class="service-cards">
          <div class="card svc-card">
            <div class="svc-left">
              <div class="svc-icon svc-icon-primary">
                <el-icon :size="22"><component :is="Home" /></el-icon>
              </div>
              <div>
                <div class="svc-label">我的宿舍</div>
                <div class="svc-value">{{ currentDormLabel }}</div>
              </div>
            </div>
            <button class="ghost-btn" @click="router.push('/student/dorm')">查看详情</button>
          </div>
          
          <div class="card svc-card">
            <div class="svc-left">
              <div class="svc-icon svc-icon-orange">
                <el-icon :size="22"><component :is="Wallet" /></el-icon>
              </div>
              <div>
                <div class="svc-label">待缴费金额</div>
                <div class="svc-value">¥ {{ dash.unpaidAmount || '0.00' }}</div>
              </div>
            </div>
            <button class="ghost-btn" @click="router.push('/student/fees')">立即充值</button>
          </div>

          <div class="card svc-card">
            <div class="svc-left">
              <div class="svc-icon svc-icon-danger">
                <el-icon :size="22"><component :is="Settings" /></el-icon>
              </div>
              <div>
                <div class="svc-label">报修申请</div>
                <div class="svc-value">{{ dash.pendingRepairs || 0 }} 件待处理</div>
              </div>
            </div>
            <button class="ghost-btn" @click="router.push('/student/repair')">申请报修</button>
          </div>

          <div class="card svc-card">
            <div class="svc-left">
              <div class="svc-icon svc-icon-teal">
                <el-icon :size="22"><component :is="Medal" /></el-icon>
              </div>
              <div>
                <div class="svc-label">卫生评分</div>
                <div class="svc-value">{{ dash.hygieneScore || '--' }} 分</div>
              </div>
            </div>
            <button class="ghost-btn" @click="router.push('/student/dorm')">查看详情</button>
          </div>
        </div>

        <!-- Todo List -->
        <div class="card">
          <div class="card-head">
            <h2>待办事项</h2>
            <span class="tag info todo-badge" v-if="dash.todoList?.length">{{ dash.todoList?.length }}</span>
          </div>
          <div class="card-body list">
            <div v-if="!dash.electricityFeeHistory && !dash.todoList" class="skeleton-list">
              <div class="skeleton skeleton-text" style="width: 100%; height: 60px;"></div>
              <div class="skeleton skeleton-text" style="width: 80%; height: 60px;"></div>
            </div>
            <div class="row todo-row" v-for="t in dash.todoList" :key="t.id">
              <span class="status-dot" :style="`background: ${t.statusColor};`"></span>
              <div class="row-main">
                <div class="row-title">{{ t.title }}</div>
                <div class="row-meta">
                  <span>{{ t.meta1 }}</span>
                  <span :style="`color: ${t.statusColor};`">{{ t.meta2 }}</span>
                </div>
              </div>
              <button class="ghost-btn" @click="router.push(t.route)">{{ t.actionLabel }}</button>
            </div>
            <div v-if="dash.todoList && dash.todoList.length === 0" class="empty-tip">
              暂无待办事项
            </div>
          </div>
        </div>

        <!-- Electricity fee chart -->
        <div class="card">
          <div class="card-head">
            <h2>电费趋势</h2>
            <span class="chart-head-label">近 12 个月</span>
          </div>
          <div class="card-body">
            <div v-if="!dash.electricityFeeHistory" class="skeleton-bars">
              <div v-for="i in 6" :key="i" class="skeleton skeleton-bar-item"
                   :style="{ height: (30 + Math.random() * 60) + 'px', animationDelay: (i * 0.08) + 's' }"></div>
            </div>
            <div v-else class="chart-bars">
              <div v-for="(amount, i) in dash.electricityFeeHistory" :key="i" class="chart-bar-col">
                <span class="chart-bar-val">{{ Number(amount).toFixed(0) }}</span>
                <div class="chart-bar-fill" :style="`height: ${electricityBarHeight(amount)}px;`"></div>
                <span class="chart-bar-lbl">{{ dash.electricityFeeMonths?.[i] }}</span>
              </div>
            </div>
            <div class="chart-footer">
              <span class="chart-footer-label">数据库账单合计</span>
              <span class="chart-footer-total">¥ {{ Number(dash.totalElectricityFee || 0).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Stack -->
      <div class="right-stack">
        <!-- Notices -->
        <div class="card">
          <div class="card-head">
            <h2>校园公告</h2>
            <a class="mini-link" @click="router.push('/student/notice')">更多</a>
          </div>
          <div class="card-body list">
            <div v-for="n in notices" :key="n.id" class="notice-item-card">
              <div class="notice-head">
                <span class="notice-title text-truncate">{{ n.title }}</span>
                <span class="tag tag-sm" :class="n.pinned ? 'warn' : 'gray'">
                  {{ n.pinned ? '置顶' : '通知' }}
                </span>
              </div>
              <div class="notice-date">{{ n.date }} 发布</div>
            </div>
            <el-empty v-if="notices.length === 0" description="暂无公告" :image-size="40"></el-empty>
          </div>
        </div>

        <!-- Roommates -->
        <div class="card">
          <div class="card-head">
            <h2>我的室友</h2>
          </div>
          <div class="card-body list">
            <div v-for="(rm, idx) in roommates" :key="idx" class="row roomie-row">
              <div class="roomie-avatar">
                {{ rm.name[0] }}
              </div>
              <div class="row-main">
                <div class="row-title">{{ rm.name }}</div>
                <div class="row-meta"><span>{{ rm.bedNumber }}号床</span></div>
              </div>
            </div>
            <el-empty v-if="roommates.length === 0" description="暂无室友信息" :image-size="40"></el-empty>
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="card">
          <div class="card-head">
            <h2>快捷服务</h2>
          </div>
          <div class="card-body quick-grid">
            <button @click="router.push('/student/visitor')" aria-label="访客预约">
              <el-icon :size="24"><component :is="UserRoundCheck" /></el-icon>
              <span>访客预约</span>
            </button>
            <button @click="router.push('/student/transfer')" aria-label="调宿申请">
              <el-icon :size="24"><component :is="Repeat2" /></el-icon>
              <span>调宿申请</span>
            </button>
            <button @click="router.push('/student/ai')" aria-label="自助问答">
              <el-icon :size="24"><component :is="MessageCircle" /></el-icon>
              <span>自助问答</span>
            </button>
            <button @click="router.push('/student/feedback')" aria-label="意见反馈">
              <el-icon :size="24"><component :is="Pencil" /></el-icon>
              <span>意见反馈</span>
            </button>
            <button @click="router.push('/student/notice')" aria-label="校园公告">
              <el-icon :size="24"><component :is="Newspaper" /></el-icon>
              <span>校园公告</span>
            </button>
            <button @click="router.push('/student/fees')" aria-label="费用查询">
              <el-icon :size="24"><component :is="WalletCards" /></el-icon>
              <span>费用查询</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBusinessRecords } from '../../api/businessRecord'
import request from '../../utils/request'
import { useUserStore } from '../../store/user'
import { 
  Home, Wallet, Settings, Medal,
  UserRoundCheck, Repeat2, MessageCircle, Pencil, Newspaper, WalletCards
} from '@lucide/vue'
import WeatherWidget from '../../components/WeatherWidget.vue'

const router = useRouter()
const userStore = useUserStore()

const weekNames = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentTime = computed(() => {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth() + 1
  const d = now.getDate()
  const w = weekNames[now.getDay()]
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  return `${y}年${m}月${d}日${w} ${h}:${min}`
})

const hoverBanner = ref(false)
const notices = ref([])
const currentDormLabel = ref('加载中')
const roommates = ref([])
const dash = ref({})

const electricityBarHeight = (amount) => {
  const values = dash.value.electricityFeeHistory || []
  const max = Math.max(...values.map(Number), 1)
  return Math.max(4, Math.round(Number(amount || 0) / max * 72))
}

const fetchDashboard = async () => {
  try {
    const res = await request({ url: '/dashboard/student', method: 'get' })
    if (res) {
      dash.value = res
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchDormSummary = async () => {
  try {
    const summary = await request({ url: '/dashboard/dorm', method: 'get' })
    if (!summary?.myBed) {
      currentDormLabel.value = '未分配宿舍'
      roommates.value = []
      return
    }
    currentDormLabel.value = `${summary.building?.name || ''} ${summary.room?.roomNumber || ''}`.trim()
    roommates.value = summary.roommates || []
  } catch (error) { console.error(error);
    currentDormLabel.value = '获取失败'
    roommates.value = []
  }
}

const fetchNotices = async () => {
  const records = await getBusinessRecords('manager_messages', '已发布')
  notices.value = (records || []).slice(0, 3).map((record, index) => ({
    id: record.id,
    title: record.title,
    date: (record.createTime || '').replace('T', ' ').slice(0, 10),
    pinned: index === 0
  }))
}

onMounted(() => {
  fetchDormSummary()
  fetchNotices()
  fetchDashboard()
})
</script>

<style scoped>
.grid {
  gap: 28px;
}

.left-stack {
  gap: 28px;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* === Hero Section === */
.dash-hero {
  padding: 24px 28px;
  margin-bottom: 24px;
}

.hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.hero-avatar {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(145deg, var(--primary), #5ba6ff);
  color: white;
  display: grid;
  place-items: center;
  font-size: 24px;
  font-weight: bold;
  box-shadow: 0 6px 16px rgba(47, 140, 255, .35);
  flex-shrink: 0;
}

.hero-greeting {
  margin: 0;
  font-size: 22px;
}

.hero-time {
  margin: 4px 0 0;
  color: var(--sub);
  font-size: 14px;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* === Banner === */
.dash-banner {
  background: linear-gradient(135deg, var(--primary-2) 0%, var(--surface) 100%);
  border: 1px solid var(--primary-2);
  padding: 24px 28px;
  margin-bottom: 0;
  cursor: pointer;
}

.banner-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.banner-icon {
  width: 52px;
  height: 52px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, var(--primary), #60a5fa);
  color: #fff;
  border-radius: 14px;
  box-shadow: 0 6px 16px rgba(59, 130, 246, .3);
  transition: transform .2s ease;
  flex-shrink: 0;
}

.banner-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: -.3px;
}

.banner-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--sub);
  line-height: 1.5;
}

/* === Service Cards === */
.svc-card {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.svc-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.svc-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.svc-icon-primary {
  background: var(--primary-2);
  color: var(--primary);
}

.svc-icon-orange {
  background: var(--orange-2);
  color: var(--orange);
}

.svc-icon-danger {
  background: var(--danger-2);
  color: var(--danger);
}

.svc-icon-teal {
  background: var(--teal-2);
  color: var(--teal);
}

.svc-label {
  color: var(--sub);
  font-size: 13px;
  margin-bottom: 2px;
}

.svc-value {
  font-weight: 700;
  font-size: 17px;
}

/* === Todo === */
.todo-badge {
  border-radius: 12px;
  padding: 2px 8px;
}

.todo-row {
  min-height: 60px;
}

.todo-row .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* === Notices === */
.notice-item-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  background: var(--surface);
  border: 1px solid var(--line);
  color: var(--text);
  padding: 14px 18px;
  border-radius: var(--radius);
}

.notice-head {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.notice-title {
  font-size: 14px;
  font-weight: 600;
  flex: 1;
}

.notice-date {
  font-size: 12px;
  color: var(--sub);
  margin-top: 6px;
}

.tag-sm {
  font-size: 10px;
  padding: 0 6px;
  min-height: 20px;
  display: inline-flex;
  align-items: center;
}

/* === Roommates === */
.roomie-row {
  border: 1px solid var(--line);
  min-height: 64px;
}

.roomie-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--muted-2);
  display: grid;
  place-items: center;
  font-weight: bold;
  color: var(--text-secondary);
  flex-shrink: 0;
}

/* === Electricity Chart === */
.chart-head-label {
  font-size: 13px;
  color: var(--sub);
}

.skeleton-bars {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 120px;
  padding: 16px 0;
}

.skeleton-bar-item {
  flex: 1;
  border-radius: 4px 4px 0 0;
  min-height: 20px;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 120px;
  padding: 16px 0;
}

.chart-bar-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.chart-bar-val {
  font-size: 11px;
  color: var(--sub);
}

.chart-bar-fill {
  width: 100%;
  background: var(--primary);
  border-radius: 4px 4px 0 0;
  transition: height .6s ease;
}

.chart-bar-lbl {
  font-size: 11px;
  color: var(--sub);
}

.chart-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--line);
}

.chart-footer-label {
  font-size: 13px;
  color: var(--sub);
}

.chart-footer-total {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary);
}

/* === Shared === */
.empty-tip {
  padding: 24px;
  text-align: center;
  color: var(--sub);
  font-size: 13px;
}

/* === Responsive === */
@media (max-width: 1024px) {
  .hero-inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .banner-inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .service-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 16px 20px !important;
  }

  .hero-greeting {
    font-size: 18px;
  }

  .hero-avatar {
    width: 44px;
    height: 44px;
    font-size: 18px;
  }

  .quick-grid {
    grid-template-columns: repeat(3, 1fr) !important;
  }

  .quick-grid button {
    padding: 12px 0 !important;
  }
}

.desk-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
