<template>
  <div class="dorm-container">
    <el-row :gutter="24" class="main-row">
      <!-- Left Column: Dorm Profile & Roommates -->
      <el-col :span="8">
        <el-card shadow="never" class="profile-card">
          <!-- Top House Info -->
          <div class="dorm-header">
            <div class="dorm-icon-box">
              <el-icon :size="40" color="var(--el-color-primary)"><component :is="Home" /></el-icon>
            </div>
            <h2 class="dorm-title">{{ myBuilding?.name || '未知' }} · {{ myRoom?.roomNumber || '未知' }}</h2>
            <div class="dorm-tags">
              <el-tag size="small" effect="plain">{{ myRoom?.floor || '-' }}层</el-tag>
              <el-tag size="small" effect="plain">{{ myRoom?.capacity || 4 }}人间</el-tag>
              <el-tag size="small" type="primary" effect="dark">{{ myBed?.bedNumber ? myBed.bedNumber.split('-')[1] + '号床位' : '未分配' }}</el-tag>
            </div>
          </div>

          <!-- Dorm Meta Details -->
          <div class="dorm-meta-list">
            <div class="meta-item">
              <span class="meta-label">入住日期</span>
              <span class="meta-value">{{ checkInDate }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">所属校区</span>
              <span class="meta-value">主校区 · 西区</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">责任宿管</span>
              <span class="meta-value">{{ myBuilding?.manager || '未知' }}</span>
            </div>
          </div>

          <!-- Roommates -->
          <div class="roommates-section">
            <div class="section-title">
              <span>我的室友 <span class="count">{{ allOccupants.length }}/{{ myRoom?.capacity || 4 }}</span></span>
              <el-button type="primary" class="group-chat-btn" plain @click="openGroupChat">
                <el-icon class="el-icon--left"><component :is="MessageCircle" /></el-icon>宿舍群聊
              </el-button>
            </div>
            <div class="roommate-list">
              <div 
                v-for="user in allOccupants" 
                :key="user.id" 
                class="roommate-item"
                :class="{ 'is-me': user.isMe }"
              >
                <img v-if="user.avatar" :src="user.avatar" class="avatar" />
                <div v-else class="avatar-placeholder">{{ user.name?.[0] || '?' }}</div>
                
                <div class="rm-info">
                  <div class="rm-name-row">
                    <span class="rm-name">{{ user.name }}</span>
                    <el-tag v-if="user.isMe" size="small" type="primary" effect="dark" class="me-tag">我</el-tag>
                  </div>
                  <div class="rm-sub">
                    <span>{{ user.bedNumber }}号床</span>
                    <span class="rm-status">外出</span>
                  </div>
                </div>
                
                <div class="rm-action">
                  <el-tooltip v-if="!user.isMe" content="发起私聊" placement="top">
                    <button type="button" class="message-btn" :aria-label="`与${user.name}私聊`" @click="openPrivateChat(user)">
                      <MessageCircle :size="19" aria-hidden="true" />
                    </button>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column: Layout & Facilities -->
      <el-col :span="16">
        <div class="right-stack">
        <!-- Bed Layout -->
        <el-card shadow="never" class="layout-card">
          <div class="card-header">宿舍床位布局</div>
          <div class="layout-wrapper">
            <div class="floor-plan">
              <div class="plan-border"></div>
              <div class="door-label">大门</div>
              <div class="balcony-label">阳台</div>
              
              <div class="beds-grid">
                <!-- 4 Beds Layout Mapping -->
                <div 
                  v-for="i in (myRoom?.capacity || 4)" 
                  :key="i"
                  class="bed-slot"
                  :class="{ 'is-mine': getOccupantForBed(i)?.isMe, 'is-occupied': getOccupantForBed(i) && !getOccupantForBed(i)?.isMe, 'is-empty': !getOccupantForBed(i) }"
                >
                  <div class="bed-box">
                    <div v-if="getOccupantForBed(i)?.isMe" class="my-bed-badge">我的床位</div>
                    <div class="bed-number">{{ myRoom?.roomNumber ? `${myRoom.roomNumber}-${i}` : `1-${i}` }}</div>
                  </div>
                  <div class="bed-owner">{{ getOccupantForBed(i)?.name || '空床' }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Bottom Row -->
        <el-row :gutter="24" style="margin-top: 24px">
          <!-- Facilities -->
          <el-col :span="14">
            <el-card shadow="never" class="facility-card">
              <div class="card-header">
                <span>设施运行状态</span>
                <el-button link type="primary" style="display:flex;align-items:center;gap:4px">
                  <el-icon><component :is="RefreshCw" /></el-icon>刷新
                </el-button>
              </div>
              <div class="facility-grid">
                <div class="fac-item">
                  <div class="fac-icon-box bg-blue"><el-icon><component :is="Lightbulb" /></el-icon></div>
                  <div class="fac-info">
                    <div class="fac-name">照明灯具</div>
                    <div class="fac-status">运行中</div>
                  </div>
                </div>
                <div class="fac-item">
                  <div class="fac-icon-box bg-blue"><el-icon><component :is="Fan" /></el-icon></div>
                  <div class="fac-info">
                    <div class="fac-name">空调设备</div>
                    <div class="fac-status">良好</div>
                  </div>
                </div>
                <div class="fac-item">
                  <div class="fac-icon-box bg-blue"><el-icon><component :is="Wifi" /></el-icon></div>
                  <div class="fac-info">
                    <div class="fac-name">宿舍网络</div>
                    <div class="fac-status">已连接</div>
                  </div>
                </div>
                <div class="fac-item">
                  <div class="fac-icon-box bg-yellow"><el-icon><component :is="Droplet" /></el-icon></div>
                  <div class="fac-info">
                    <div class="fac-name">直饮水</div>
                    <div class="fac-status text-yellow">滤芯待换</div>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <!-- Hygiene Metric -->
          <el-col :span="10">
            <el-card shadow="never" class="hygiene-card">
              <div class="card-header">卫生月度指标</div>
              <div class="hygiene-content">
                <div class="chart-box">
                  <el-progress type="dashboard" :percentage="95" :width="120" color="#3b82f6" :stroke-width="8">
                    <template #default="{ percentage }">
                      <div class="score-value">
                        <span class="num">{{ percentage }}</span>
                        <span class="unit">分</span>
                      </div>
                    </template>
                  </el-progress>
                  <div class="chart-label">本月平均得分</div>
                </div>
                <div class="hygiene-stats">
                  <div class="h-stat-row">
                    <span class="h-label">全楼排名</span>
                    <span class="h-val">第 12 名</span>
                  </div>
                  <el-divider border-style="dashed" />
                  <div class="h-stat-row">
                    <span class="h-label">上次检查</span>
                    <span class="h-val">98 (优秀)</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        </div>
      </el-col>
    </el-row>

    <!-- Chat Drawer -->
    <ChatDrawer
      v-model="chatVisible"
      :target-user="chatTarget"
      :room-id="myRoom?.id"
      :my-id="userStore.userInfo?.id"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { 
  Home, MessageCircle, RefreshCw, Lightbulb, Fan, Wifi, Droplet
} from '@lucide/vue'
import { getRooms, getBeds } from '../../api/room'
import { getUsers } from '../../api/user'
import { getBuildings } from '../../api/building'
import { useUserStore } from '../../store/user'
import ChatDrawer from '../../components/ChatDrawer.vue'
import request from '../../utils/request'

const userStore = useUserStore()

const chatVisible = ref(false)
const chatTarget = ref(null)

const openPrivateChat = (user) => {
  chatTarget.value = { id: user.id, name: user.name }
  chatVisible.value = true
}

const openGroupChat = () => {
  chatTarget.value = null
  chatVisible.value = true
}

const myRoom = ref(null)
const myBed = ref(null)
const myBuilding = ref(null)
const roommates = ref([])
const checkInDate = ref('-')

// Combine me + roommates into one list for rendering
const allOccupants = computed(() => {
  const list = []
  const me = userStore.userInfo
  if (me && myBed.value) {
    list.push({
      id: me.id,
      name: me.name,
      avatar: me.avatar,
      bedNumber: myBed.value.bedNumber,
      isMe: true
    })
  }
  list.push(...roommates.value)
  // Sort by bed number
  list.sort((a, b) => {
    const numA = parseInt(a.bedNumber.split('-')[1] || 0)
    const numB = parseInt(b.bedNumber.split('-')[1] || 0)
    return numA - numB
  })
  return list
})

const getOccupantForBed = (bedIndex) => {
  if (!myRoom.value) return null
  const targetBedSuffix = `-${bedIndex}`
  return allOccupants.value.find(u => u.bedNumber.endsWith(targetBedSuffix))
}

const fetchDormInfo = async () => {
  try {
    const [bedsRes, usersRes, roomsRes, buildingsRes, stayHistory] = await Promise.all([
      getBeds(), getUsers(), getRooms(), getBuildings(),
      request({ url: '/stayHistory/current', method: 'get' })
    ])
    if (stayHistory?.checkInDate) {
      checkInDate.value = new Date(stayHistory.checkInDate).toLocaleDateString('zh-CN')
    }
    const beds = Array.isArray(bedsRes) ? bedsRes : (bedsRes.data || [])
    const users = Array.isArray(usersRes) ? usersRes : (usersRes.data || [])
    const rooms = Array.isArray(roomsRes) ? roomsRes : (roomsRes.data || [])
    const buildings = Array.isArray(buildingsRes) ? buildingsRes : (buildingsRes.data || [])

    const me = userStore.userInfo || {}
    const myBedInfo = beds.find(b => b.studentId === me.id)
    if (!myBedInfo) return // No bed assigned
    
    myBed.value = myBedInfo
    const roomInfo = rooms.find(r => r.id === myBedInfo.roomId)
    if (roomInfo) {
      myRoom.value = roomInfo
      myBuilding.value = buildings.find(b => b.id === roomInfo.buildingId)
      
      // Find roommates
      const roomBeds = beds.filter(b => b.roomId === roomInfo.id && b.studentId && b.studentId !== me.id)
      const userMap = Object.fromEntries(users.map(u => [u.id, u]))
      
      roommates.value = roomBeds.map(b => {
        const u = userMap[b.studentId] || {}
        return {
          id: u.id,
          name: u.name || '未知',
          avatar: u.avatar,
          sno: u.username || '',
          bedNumber: b.bedNumber,
          isMe: false
        }
      })
    }
  } catch (e) {
    console.error('Failed to fetch dorm info', e)
  }
}

onMounted(() => fetchDormInfo())
</script>

<style scoped>
.dorm-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* Base Cards */
.profile-card, .layout-card, .facility-card, .hygiene-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04) !important;
  background: #fff;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
  color: #1e293b;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

/* Profile Card */
.profile-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.dorm-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0 30px;
}
.dorm-icon-box {
  width: 90px;
  height: 90px;
  background: #eef2ff;
  border-radius: 24px;
  display: grid;
  place-items: center;
  margin-bottom: 20px;
}
.dorm-title {
  margin: 0 0 16px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}
.dorm-tags {
  display: flex;
  gap: 12px;
}

.dorm-meta-list {
  margin: 0 24px 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.meta-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}
.meta-label {
  color: #64748b;
}
.meta-value {
  font-weight: 600;
  color: #1e293b;
}

/* Roommates Section */
.roommates-section {
  margin: 0 24px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1e293b;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-title .count {
  color: #94a3b8;
  font-weight: 400;
  margin-left: 6px;
}
.group-chat-btn {
  min-height: 36px;
  border-radius: 8px;
  font-weight: 600;
}
.roommate-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.roommate-item {
  display: flex;
  align-items: center;
  min-width: 0;
  padding: 12px 16px;
  border: 1px solid #e4ecfc;
  border-radius: 8px;
  background: #f8faff;
}
.roommate-item.is-me {
  background: #f4f8ff;
  border-color: #93b4f9;
}
.avatar, .avatar-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 14px;
}
.avatar-placeholder {
  background: #e2e8f0;
  color: #64748b;
  display: grid;
  place-items: center;
  font-weight: 600;
  font-size: 18px;
}
.rm-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.rm-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.rm-name {
  font-weight: 600;
  color: #0f172a;
  font-size: 15px;
}
.me-tag {
  height: 22px;
  padding: 0 8px;
  border-radius: 6px;
}
.rm-sub {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #64748b;
}
.rm-status {
  background: #e2e8f0;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  color: #475569;
}
.rm-action {
  display: flex;
  flex: 0 0 44px;
  justify-content: flex-end;
  margin-left: 10px;
}
.message-btn {
  width: 44px;
  height: 44px;
  padding: 0;
  display: grid;
  place-items: center;
  border: 1px solid #d7e3f8;
  border-radius: 8px;
  background: #fff;
  color: #2563eb;
  cursor: pointer;
  transition: background-color 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}
.message-btn:hover {
  color: #fff;
  border-color: #2563eb;
  background: #2563eb;
}
.message-btn:focus-visible {
  outline: 3px solid rgba(37, 99, 235, 0.24);
  outline-offset: 2px;
}

@media (max-width: 720px) {
  .dorm-container { padding: 16px; }
  .roommates-section { margin: 0; }
  .section-title { align-items: flex-start; gap: 12px; }
  .group-chat-btn { flex-shrink: 0; }
  .roommate-item { padding: 12px; }
}

/* Right Stack */
.right-stack {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Bed Layout Card */
.layout-wrapper {
  padding: 40px 0;
  display: flex;
  justify-content: center;
}
.floor-plan {
  position: relative;
  width: 100%;
  max-width: 600px;
  height: 300px;
}
.plan-border {
  position: absolute;
  top: 0; left: 24px; right: 24px; bottom: 0;
  border: 4px solid #cbd5e1;
  border-radius: 12px;
}
.plan-border::before {
  content: '';
  position: absolute;
  top: -4px; left: 10%; width: 60px; height: 8px;
  background: #fff;
}
.plan-border::after {
  content: '';
  position: absolute;
  bottom: -4px; right: 10%; width: 80px; height: 8px;
  background: #fff;
}
.door-label {
  position: absolute;
  top: -12px;
  left: calc(24px + 10% + 15px);
  background: #fff;
  padding: 0 12px;
  color: #94a3b8;
  font-weight: 500;
  font-size: 14px;
}
.balcony-label {
  position: absolute;
  bottom: -12px;
  right: calc(24px + 10% + 15px);
  background: #fff;
  padding: 0 12px;
  color: #94a3b8;
  font-weight: 500;
  font-size: 14px;
}
.beds-grid {
  position: absolute;
  top: 50px; left: 60px; right: 60px; bottom: 50px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 50px 80px;
  place-items: center;
}
.bed-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.bed-box {
  width: 100px;
  height: 56px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  display: grid;
  place-items: center;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
  position: relative;
  transition: all 0.3s ease;
}
.bed-number {
  font-weight: 600;
  color: #64748b;
  font-size: 16px;
}
.bed-owner {
  color: #475569;
  font-size: 15px;
  font-weight: 500;
  transition: color 0.3s ease;
}

/* My bed - blue accent */
.bed-slot.is-mine .bed-box {
  border-color: #3b82f6;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15), 0 4px 12px rgba(59, 130, 246, 0.12);
}
.bed-slot.is-mine .bed-number {
  color: #1d4ed8;
  font-weight: 700;
}
.bed-slot.is-mine .bed-owner {
  color: #2563eb;
  font-weight: 700;
}
.my-bed-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  background: #3b82f6;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 8px;
  white-space: nowrap;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
}

/* Occupied by roommate */
.bed-slot.is-occupied .bed-box {
  border-color: #94a3b8;
  background: #f1f5f9;
}
.bed-slot.is-occupied .bed-number {
  color: #475569;
}

/* Empty bed */
.bed-slot.is-empty .bed-box {
  border: 2px dashed #e2e8f0;
  background: #fff;
}
.bed-slot.is-empty .bed-number {
  color: #cbd5e1;
}
.bed-slot.is-empty .bed-owner {
  color: #cbd5e1;
  font-weight: 400;
}

/* Facility Grid */
.facility-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.fac-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
  gap: 16px;
}
.fac-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 22px;
}
.bg-blue {
  background: #eef2ff;
  color: #3b82f6;
}
.bg-yellow {
  background: #fef3c7;
  color: #d97706;
}
.text-yellow {
  color: #d97706 !important;
}
.fac-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
}
.fac-status {
  font-size: 14px;
  color: #64748b;
}

/* Hygiene Card */
.hygiene-content {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 10px 0;
}
.chart-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.score-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
}
.score-value .num {
  font-size: 40px;
  font-weight: 800;
  color: #1e293b;
}
.score-value .unit {
  font-size: 16px;
  color: #64748b;
  margin-left: 4px;
}
.chart-label {
  font-size: 14px;
  color: #94a3b8;
  margin-top: -12px;
}
.hygiene-stats {
  flex: 1;
  margin-left: 32px;
  display: flex;
  flex-direction: column;
}
.h-stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}
.h-label {
  color: #64748b;
  font-size: 15px;
}
.h-val {
  color: #1e293b;
  font-weight: 600;
  font-size: 15px;
}
</style>
