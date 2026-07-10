<template>
  <div class="workbench-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <div class="hero-section">
          <div class="hero-content">
            <div class="hero-text">
              <h2>您好，张宿管 老师</h2>
              <p>今天是 2026年3月27日星期五，目前楼栋运行状态：<span class="text-blue">良好</span></p>
            </div>
            <div class="hero-actions">
              <el-button type="primary" @click="loadData"><el-icon class="el-icon--left"><component :is="Refresh" /></el-icon>刷新数据</el-button>
            </div>
          </div>
        </div>

        <!-- 4 Stats Cards -->
        <el-row :gutter="20" class="stats-grid">
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card" @click="router.push('/dormmanager/checkin')">
              <div class="stat-body">
                <div class="stat-icon bg-blue"><el-icon><component :is="User" /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-label">在住学子</div>
                  <div class="stat-value">{{ residentCount }}</div>
                </div>
              </div>
              <div class="stat-footer">当前楼栋入住率--</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card" @click="router.push('/dormmanager/repair')">
              <div class="stat-body">
                <div class="stat-icon bg-orange"><el-icon><component :is="Settings" /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-label">待办报修</div>
                  <div class="stat-value">{{ pendingRepairCount }}</div>
                </div>
              </div>
              <div class="stat-footer stat-action">
                <span>去处理</span><el-icon><component :is="ChevronRight" /></el-icon>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card" @click="router.push('/dormmanager/visitor')">
              <div class="stat-body">
                <div class="stat-icon bg-blue"><el-icon><component :is="UserCheck" /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-label">今日访客</div>
                  <div class="stat-value">{{ todayVisitorCount }}</div>
                </div>
              </div>
              <div class="stat-footer">预计还有 -- 人到达</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-body">
                <div class="stat-icon bg-red"><el-icon><component :is="Clock" /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-label">昨日晚归</div>
                  <div class="stat-value text-red">0</div>
                </div>
              </div>
              <div class="stat-footer stat-action">
                <span>详情</span><el-icon><component :is="ChevronRight" /></el-icon>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- Building Dynamics -->
        <el-card shadow="never" class="dynamics-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">楼栋业务动态</span>
              <el-radio-group v-model="dynamicsView" size="small">
                <el-radio-button label="room">房间状态</el-radio-button>
                <el-radio-button label="hygiene">卫生评比</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="rooms-grid">
            <div v-for="room in rooms" :key="room.number" class="room-box" :class="{'is-warning': room.warning}">
              <div class="room-number">{{ room.number }}</div>
              <div class="room-dots">
                <span class="dot filled"></span>
                <span class="dot filled"></span>
                <span class="dot filled"></span>
                <span class="dot"></span>
              </div>
              <div v-if="room.warning" class="room-warning-icon">!</div>
            </div>
          </div>
        </el-card>

        <!-- Bottom Two Lists -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="never" class="list-card">
              <template #header>
                <div class="card-header">
                  <span class="card-title">待处理报修</span>
                  <el-button type="primary" link>全部</el-button>
                </div>
              </template>
              <div class="mini-list">
                <el-empty v-if="pendingRepairs.length === 0" description="暂无待办" :image-size="60"></el-empty>
                <div v-for="r in pendingRepairs" :key="r.id" class="mini-item">
                  <span>{{ r.type || '报修' }}</span>
                  <span class="mini-date">{{ r.createTime?.substring(0, 10) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" class="list-card">
              <template #header>
                <div class="card-header">
                  <span class="card-title">访客预约</span>
                  <el-button type="primary" link>全部</el-button>
                </div>
              </template>
              <el-empty description="今日无预约" :image-size="80"></el-empty>
            </el-card>
          </el-col>
        </el-row>
      </el-col>

      <!-- Right Column -->
      <el-col :span="6">
        <!-- Quick Tools -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span class="card-title">快捷工具</span></div>
          </template>
          <div class="tools-grid">
            <div class="tool-btn" @click="router.push('/dormmanager/checkin')">
              <el-icon :size="24" color="#3b82f6"><component :is="UserPlus" /></el-icon>
              <span>入住办理</span>
            </div>
            <div class="tool-btn" @click="router.push('/dormmanager/repair')">
              <el-icon :size="24" color="#3b82f6"><component :is="Settings" /></el-icon>
              <span>报修派工</span>
            </div>
            <div class="tool-btn">
              <el-icon :size="24" color="#3b82f6"><component :is="Medal" /></el-icon>
              <span>卫生评分</span>
            </div>
            <div class="tool-btn" @click="router.push('/dormmanager/visitor')">
              <el-icon :size="24" color="#3b82f6"><component :is="UserCheck" /></el-icon>
              <span>访客登记</span>
            </div>
            <div class="tool-btn">
              <el-icon :size="24" color="#3b82f6"><component :is="MessageCircle" /></el-icon>
              <span>AI 巡查报告</span>
            </div>
            <div class="tool-btn">
              <el-icon :size="24" color="#3b82f6"><component :is="Clock" /></el-icon>
              <span>晚归登记</span>
            </div>
          </div>
        </el-card>

        <!-- Management Memos -->
        <el-card shadow="never" class="side-card memos-card">
          <template #header>
            <div class="card-header"><span class="card-title">管理备忘</span></div>
          </template>
          <div class="memos-list">
            <div class="memo-item">
              <div class="memo-dot dot-orange"></div>
              <div class="memo-content">
                <div class="memo-title">周四卫生大检查</div>
                <div class="memo-time">14:00 - 16:00</div>
              </div>
            </div>
            <div class="memo-item">
              <div class="memo-dot dot-blue"></div>
              <div class="memo-content">
                <div class="memo-title">毕业生离宿手续办理</div>
                <div class="memo-time">全天</div>
              </div>
            </div>
            <div class="memo-item">
              <div class="memo-dot dot-blue"></div>
              <div class="memo-content">
                <div class="memo-title">消防器材例行抽检</div>
                <div class="memo-time">10:00</div>
              </div>
            </div>
            <el-button class="add-memo-btn" plain>+ 添加备忘</el-button>
          </div>
        </el-card>

        <!-- System Notices -->
        <el-card shadow="never" class="side-card notices-card">
          <template #header>
            <div class="card-header"><span class="card-title">系统通知</span></div>
          </template>
          <div class="notice-list">
            <div class="notice-item">
              <el-icon color="#3b82f6"><component :is="Info" /></el-icon>
              <span>系统预计于周六凌晨 2:00 进行维护更新</span>
            </div>
            <div class="notice-item">
              <el-icon color="#f59e0b"><component :is="AlertCircle" /></el-icon>
              <span>发现 3 号楼有 2 间寝室电费余额不足</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, RefreshCw as Refresh, User, Settings, UserCheck, Clock, ChevronRight, UserPlus, Medal, MessageCircle, Info, AlertCircle } from '@lucide/vue'
import { getBeds, getRooms } from '../../api/room'
import { getRepairs } from '../../api/repair'
import { getVisitorRecords } from '../../api/visitor'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const dynamicsView = ref('room')

const residentCount = ref(0)
const pendingRepairCount = ref(0)
const todayVisitorCount = ref(0)
const rooms = ref([])
const pendingRepairs = ref([])

const loadData = async () => {
  try {
    const [beds, repairList, visitors, roomList] = await Promise.all([
      getBeds(), getRepairs(), getVisitorRecords(), getRooms()
    ])
    
    residentCount.value = beds.filter(b => b.status === 'OCCUPIED').length
    
    const pendings = repairList.filter(r => r.status === 'PENDING')
    pendingRepairCount.value = pendings.length
    pendingRepairs.value = pendings.slice(0, 3) // preview top 3
    
    const today = new Date().toDateString()
    todayVisitorCount.value = visitors.filter(v => new Date(v.createTime).toDateString() === today).length
    
    rooms.value = roomList.map(r => ({
      number: r.roomNumber,
      warning: false // Simplified: no warning logic for now
    }))
  } catch (e) {
    console.error('Failed to load workbench data', e)
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.workbench-container {
  max-width: 1400px;
  margin: 0 auto;
}

.hero-section {
  margin-bottom: 24px;
}

.hero-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-text h2 {
  font-size: 24px;
  color: var(--text);
  margin: 0 0 8px 0;
}

.hero-text p {
  color: var(--sub);
  font-size: 14px;
  margin: 0;
}

.text-blue {
  color: #3b82f6;
  font-weight: 500;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.stats-grid {
  margin-bottom: 24px;
}

.stat-card {
  margin-bottom: 20px;
  cursor: pointer;
  border-radius: 12px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card :deep(.el-card__body) {
  padding: 0;
}

.stat-body {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
}

.bg-blue { background: var(--primary-2); color: #3b82f6; }
.bg-orange { background: var(--orange-2); color: #f97316; }
.bg-red { background: var(--danger-2); color: var(--danger); }
.text-red { color: #ef4444 !important; }

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--sub);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--text);
}

.stat-footer {
  border-top: 1px solid var(--line);
  padding: 12px 24px;
  font-size: 13px;
  color: var(--sub);
}

.stat-action {
  color: var(--el-color-primary);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dynamics-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: var(--text);
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 16px;
  padding: 16px 0;
}

.room-box {
  background: var(--bg);
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  position: relative;
}

.room-box.is-warning {
  background: var(--danger-2);
  border-color: var(--danger);
}

.room-number {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.room-dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--line);
}

.dot.filled {
  background-color: #3b82f6;
}

.room-warning-icon {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 16px;
  height: 16px;
  background-color: #ef4444;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: bold;
}

.list-card {
  min-height: 200px;
}

.mini-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mini-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--bg);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.mini-date {
  color: var(--sub);
}

.side-card {
  margin-bottom: 24px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.tool-btn {
  background: var(--bg);
  border-radius: 12px;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.tool-btn:hover {
  background: var(--line);
}

.tool-btn span {
  font-size: 13px;
  color: var(--text-secondary);
}

.memos-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.memo-item {
  display: flex;
  gap: 12px;
  background: var(--bg);
  padding: 12px;
  border-radius: 8px;
}

.memo-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
}

.dot-orange { background-color: #f59e0b; }
.dot-blue { background-color: #3b82f6; }

.memo-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  margin-bottom: 4px;
}

.memo-time {
  font-size: 12px;
  color: var(--sub);
}

.add-memo-btn {
  width: 100%;
  margin-top: 8px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}
</style>
