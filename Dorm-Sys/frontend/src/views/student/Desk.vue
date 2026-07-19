<template>
  <div>
    <!-- Hero Section -->
    <div class="hero" style="padding: 24px 28px; margin-bottom: 24px;">
      <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
        <div style="display: flex; align-items: center; gap: 18px;">
          <!-- Empty avatar for placeholder, style kept from prototype -->
          <div style="width: 56px; height: 56px; border-radius: 16px; background: linear-gradient(145deg, var(--primary), #5ba6ff); color: white; display: grid; place-items: center; font-size: 24px; box-shadow: 0 6px 16px rgba(47, 140, 255, .35); font-weight: bold;">
            张
          </div>
          <div>
            <h1 style="margin: 0; font-size: 22px;">早安，{{ userStore.userInfo?.name || '同学' }} 👋</h1>
            <p style="margin: 4px 0 0; color: var(--sub); font-size: 14px;">开启高效的一天吧！</p>
          </div>
        </div>
        <WeatherWidget />
      </div>
    </div>

    <div class="grid" style="gap: 28px;">
      <!-- Left Stack -->
      <div class="left-stack" style="gap: 28px;">
        <!-- Banner -->
        <div class="card" style="background: linear-gradient(135deg, var(--primary-2) 0%, var(--surface) 100%); border: 1px solid var(--primary-2); padding: 24px 28px; margin-bottom: 0; transition: transform .25s ease, box-shadow .25s ease; cursor: pointer;"
             @mouseover="hoverBanner = true" @mouseleave="hoverBanner = false"
             :style="hoverBanner ? 'transform: translateY(-2px); box-shadow: 0 8px 24px rgba(59, 130, 246, .12);' : 'box-shadow: var(--shadow);'">
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <div style="display: flex; align-items: center; gap: 16px;">
              <div style="width: 52px; height: 52px; display: grid; place-items: center; background: linear-gradient(135deg, var(--primary), #60a5fa); color: #fff; border-radius: 14px; box-shadow: 0 6px 16px rgba(59, 130, 246, .3); transition: transform .2s ease;"
                   :style="hoverBanner ? 'transform: scale(1.05)' : ''">
                <el-icon :size="24"><component :is="Home" /></el-icon>
              </div>
              <div>
                <h2 style="margin: 0; font-size: 18px; font-weight: 700; color: var(--text); letter-spacing: -.3px;">文明寝室 · 共同维护</h2>
                <p style="margin: 4px 0 0; font-size: 13px; color: var(--sub); line-height: 1.5;">本周将进行月度卫生大检查，请保持寝室整洁</p>
              </div>
            </div>
            <button class="ghost-btn" style="white-space: nowrap;">查看详情 →</button>
          </div>
        </div>

        <!-- Service Cards -->
        <div class="service-cards">
          <div class="card" style="padding: 20px; display: flex; align-items: center; justify-content: space-between;">
            <div style="display: flex; align-items: center; gap: 14px;">
              <div style="width: 46px; height: 46px; border-radius: 12px; background: var(--primary-2); color: var(--primary); display: grid; place-items: center;">
                <el-icon :size="22"><component :is="Home" /></el-icon>
              </div>
              <div>
                <div style="color: var(--sub); font-size: 13px; margin-bottom: 2px;">我的宿舍</div>
                <div style="font-weight: 700; font-size: 17px;">{{ currentDormLabel }}</div>
              </div>
            </div>
            <button class="ghost-btn" @click="router.push('/student/dorm')">查看详情</button>
          </div>
          
          <div class="card" style="padding: 20px; display: flex; align-items: center; justify-content: space-between;">
            <div style="display: flex; align-items: center; gap: 14px;">
              <div style="width: 46px; height: 46px; border-radius: 12px; background: var(--orange-2); color: var(--orange); display: grid; place-items: center;">
                <el-icon :size="22"><component :is="Wallet" /></el-icon>
              </div>
              <div>
                <div style="color: var(--sub); font-size: 13px; margin-bottom: 2px;">待缴费金额</div>
                <div style="font-weight: 700; font-size: 17px;">¥ {{ dash.unpaidAmount || '0.00' }}</div>
              </div>
            </div>
            <button class="ghost-btn" @click="router.push('/student/fees')">立即充值</button>
          </div>

          <div class="card" style="padding: 20px; display: flex; align-items: center; justify-content: space-between;">
            <div style="display: flex; align-items: center; gap: 14px;">
              <div style="width: 46px; height: 46px; border-radius: 12px; background: var(--danger-2); color: var(--danger); display: grid; place-items: center;">
                <el-icon :size="22"><component :is="Settings" /></el-icon>
              </div>
              <div>
                <div style="color: var(--sub); font-size: 13px; margin-bottom: 2px;">报修申请</div>
                <div style="font-weight: 700; font-size: 17px;">{{ dash.pendingRepairs || 0 }} 件待处理</div>
              </div>
            </div>
            <button class="ghost-btn" @click="router.push('/student/repair')">申请报修</button>
          </div>

          <div class="card" style="padding: 20px; display: flex; align-items: center; justify-content: space-between;">
            <div style="display: flex; align-items: center; gap: 14px;">
              <div style="width: 46px; height: 46px; border-radius: 12px; background: var(--teal-2); color: var(--teal); display: grid; place-items: center;">
                <el-icon :size="22"><component :is="Medal" /></el-icon>
              </div>
              <div>
                <div style="color: var(--sub); font-size: 13px; margin-bottom: 2px;">卫生评分</div>
                <div style="font-weight: 700; font-size: 17px;">{{ dash.hygieneScore || '--' }} 分</div>
              </div>
            </div>
            <button class="ghost-btn" @click="router.push('/student/dorm')">查看详情</button>
          </div>
        </div>

        <!-- Todo List -->
        <div class="card">
          <div class="card-head">
            <h2>待办事项</h2>
            <span class="tag info" style="border-radius: 12px; padding: 2px 8px;" v-if="dash.todoList?.length">{{ dash.todoList?.length }}</span>
          </div>
          <div class="card-body list">
            <div class="row" style="min-height: 60px;" v-for="t in dash.todoList" :key="t.id">
              <span class="status-dot" :style="`width: 8px; height: 8px; border-radius: 50%; background: ${t.statusColor};`"></span>
              <div class="row-main">
                <div class="row-title">{{ t.title }}</div>
                <div class="row-meta">
                  <span>{{ t.meta1 }}</span>
                  <span :style="`color: ${t.statusColor};`">{{ t.meta2 }}</span>
                </div>
              </div>
              <button class="ghost-btn" @click="router.push(t.route)">{{ t.actionLabel }}</button>
            </div>
            <div v-if="!dash.todoList?.length" style="padding: 24px; text-align: center; color: var(--sub); font-size: 13px;">
              暂无待办事项
            </div>
          </div>
        </div>

        <!-- Electricity fee chart -->
        <div class="card">
          <div class="card-head">
            <h2>电费趋势</h2>
            <span style="font-size: 13px; color: var(--sub);">近 12 个月</span>
          </div>
          <div class="card-body">
            <div style="display: flex; align-items: flex-end; gap: 8px; height: 120px; padding: 16px 0;">
              <div v-for="(amount, i) in (dash.electricityFeeHistory || [])" :key="i"
                   style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px;">
                <span style="font-size: 11px; color: var(--sub);">{{ Number(amount).toFixed(0) }}</span>
                <div :style="`width: 100%; height: ${electricityBarHeight(amount)}px; background: var(--primary); border-radius: 4px 4px 0 0; transition: height .6s ease;`"></div>
                <span style="font-size: 11px; color: var(--sub);">{{ dash.electricityFeeMonths?.[i] }}</span>
              </div>
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 8px; padding-top: 8px; border-top: 1px solid var(--line);">
              <span style="font-size: 13px; color: var(--sub);">数据库账单合计</span>
              <span style="font-size: 13px; font-weight: 600; color: var(--primary);">¥ {{ Number(dash.totalElectricityFee || 0).toFixed(2) }}</span>
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
            <a class="mini-link" style="cursor: pointer;">更多</a>
          </div>
          <div class="card-body list">
            <div v-for="n in notices" :key="n.id" class="notice" style="display: flex; flex-direction: column; align-items: flex-start; background: var(--surface); border: 1px solid var(--line); color: var(--text);">
              <div style="display: flex; align-items: center; gap: 8px; width: 100%;">
                <span style="font-size: 14px; font-weight: 600; flex: 1;" class="text-truncate">{{ n.title }}</span>
                <span class="tag" :class="n.pinned ? 'warn' : 'gray'" style="font-size: 10px; padding: 0 6px; min-height: 20px;">
                  {{ n.pinned ? '置顶' : '通知' }}
                </span>
              </div>
              <div style="font-size: 12px; color: var(--sub); margin-top: 6px;">{{ n.date }} 发布</div>
            </div>
          </div>
        </div>

        <!-- Roommates -->
        <div class="card">
          <div class="card-head">
            <h2>我的室友</h2>
          </div>
          <div class="card-body list">
            <div v-for="(rm, idx) in roommates" :key="idx" class="row" style="border: 1px solid var(--line); min-height: 64px;">
              <div style="width: 40px; height: 40px; border-radius: 50%; background: var(--muted-2); display: grid; place-items: center; font-weight: bold; color: var(--text-secondary);">
                {{ rm.name[0] }}
              </div>
              <div class="row-main">
                <div class="row-title">{{ rm.name }}</div>
                <div class="row-meta"><span>{{ rm.bedNumber }}号床</span></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="card">
          <div class="card-head">
            <h2>快捷服务</h2>
          </div>
          <div class="card-body quick-grid">
            <button @click="router.push('/student/visitor')">
              <el-icon :size="24"><component :is="UserRoundCheck" /></el-icon>
              <span>访客预约</span>
            </button>
            <button @click="router.push('/student/transfer')">
              <el-icon :size="24"><component :is="Repeat2" /></el-icon>
              <span>调宿申请</span>
            </button>
            <button @click="router.push('/student/ai')">
              <el-icon :size="24"><component :is="MessageCircle" /></el-icon>
              <span>自助问答</span>
            </button>
            <button @click="router.push('/student/feedback')">
              <el-icon :size="24"><component :is="Pencil" /></el-icon>
              <span>意见反馈</span>
            </button>
            <button @click="router.push('/student/notice')">
              <el-icon :size="24"><component :is="Newspaper" /></el-icon>
              <span>校园公告</span>
            </button>
            <button @click="router.push('/student/fees')">
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
import { ref, onMounted } from 'vue'
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
.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
