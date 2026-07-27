<template>
  <div class="workbench-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <div class="hero-section">
          <div class="hero-content">
            <div class="hero-text">
              <h2>您好，{{ userStore.userInfo?.name || '宿管' }} 老师</h2>
              <p>今天是 {{ todayDisplay }}，目前楼栋运行状态：<span class="text-blue">良好</span></p>
            </div>
            <div class="hero-actions">
              <WeatherWidget />
              <el-button type="primary" @click="loadData"><el-icon class="el-icon--left"><component :is="Refresh" /></el-icon>刷新数据</el-button>
            </div>
          </div>
        </div>

        <!-- 4 Stats Cards -->
        <el-row :gutter="20" class="stats-grid">
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card" @click="router.push('/dormmanager/checkin')" role="button" tabindex="0" @keydown.enter="router.push('/dormmanager/checkin')" @keydown.space.prevent="router.push('/dormmanager/checkin')">
              <div class="stat-body">
                <div class="stat-icon bg-blue"><el-icon><component :is="User" /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-label">在住学子</div>
                  <div class="stat-value">{{ residentCount }}</div>
                </div>
              </div>
              <div class="stat-footer" v-if="totalBedCount > 0">当前楼栋入住率{{ Math.round(residentCount / totalBedCount * 100) }}%</div>
              <div class="stat-footer" v-else>当前楼栋入住率--</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card" @click="router.push('/dormmanager/repair')" role="button" tabindex="0" @keydown.enter="router.push('/dormmanager/repair')" @keydown.space.prevent="router.push('/dormmanager/repair')">
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
            <el-card shadow="hover" class="stat-card" @click="router.push('/dormmanager/visitor')" role="button" tabindex="0" @keydown.enter="router.push('/dormmanager/visitor')" @keydown.space.prevent="router.push('/dormmanager/visitor')">
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
            <el-card shadow="hover" class="stat-card" @click="router.push('/dormmanager/late-return')" role="button" tabindex="0" @keydown.enter="router.push('/dormmanager/late-return')" @keydown.space.prevent="router.push('/dormmanager/late-return')">
              <div class="stat-body">
                <div class="stat-icon bg-red"><el-icon><component :is="Clock" /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-label">近日晚归</div>
                  <div class="stat-value text-red">{{ lateReturnCount }}</div>
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
              <div class="card-header-toolbar">
                <el-select v-model="selectedBuilding" placeholder="选择楼栋查看" class="building-select" size="small" clearable>
                  <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
                </el-select>
                <el-radio-group v-model="dynamicsView" size="small">
                  <el-radio-button label="room">房间状态</el-radio-button>
                  <el-radio-button label="hygiene">卫生评比</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <template v-if="!loading">
            <div v-if="dynamicsView === 'room'" class="rooms-grid">
              <div v-for="room in filteredRooms" :key="room.id" class="room-box" :class="{'is-warning': room.warning}" @click="openRoomDetail(room)" role="button" tabindex="0" @keydown.enter="openRoomDetail(room)" @keydown.space.prevent="openRoomDetail(room)">
                <div class="room-number">{{ room.buildingName }}-{{ room.number }}</div>
                <div class="room-dots">
                  <span v-for="i in room.capacity" :key="i" class="dot" :class="{ filled: i <= room.occupied }"></span>
                </div>
                <div v-if="room.warning" class="room-warning-icon">!</div>
              </div>
            </div>
            <div v-else class="hygiene-view">
              <el-empty description="暂无卫生评比数据" :image-size="60"></el-empty>
            </div>
          </template>
          <div v-else class="rooms-grid-skeleton">
            <div v-for="i in 12" :key="i" class="skeleton skeleton-text" style="height: 80px;"></div>
          </div>
        </el-card>

        <!-- Room Detail Dialog -->
        <el-dialog v-model="roomDialogVisible" :title="`${selectedRoom?.buildingName} - ${selectedRoom?.number} 房间床位布局`" width="700px" destroy-on-close>
          <div class="layout-wrapper" v-loading="roomDetailLoading">
            <div class="floor-plan">
              <div class="plan-border"></div>
              <div class="door-label">大门</div>
              <div class="balcony-label">阳台</div>
              
              <div class="beds-grid">
                <div 
                  v-for="i in (selectedRoom?.capacity || 4)" 
                  :key="i"
                  class="bed-slot"
                  :class="{ 'is-occupied': getOccupantForBed(i), 'is-empty': !getOccupantForBed(i) }"
                >
                  <div class="bed-box">
                    <div class="bed-number">{{ selectedRoom?.number ? `${selectedRoom.number}-${i}` : `1-${i}` }}</div>
                  </div>
                  <div class="bed-owner">{{ getOccupantForBed(i)?.studentName || '空床' }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-dialog>

        <!-- Bottom Two Lists -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="never" class="list-card">
              <template #header>
                <div class="card-header">
                  <span class="card-title">待处理报修</span>
                  <el-button type="primary" link @click="router.push('/dormmanager/repair')">全部</el-button>
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
                  <el-button type="primary" link @click="router.push('/dormmanager/visitor')">全部</el-button>
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
            <button type="button" class="tool-btn" @click="router.push('/dormmanager/checkin')" aria-label="入住办理">
              <el-icon :size="24" color="var(--el-color-primary)"><component :is="UserPlus" /></el-icon>
              <span>入住办理</span>
            </button>
            <button type="button" class="tool-btn" @click="router.push('/dormmanager/repair')" aria-label="报修派工">
              <el-icon :size="24" color="var(--el-color-primary)"><component :is="Settings" /></el-icon>
              <span>报修派工</span>
            </button>
            <button type="button" class="tool-btn" @click="router.push('/dormmanager/hygiene')" aria-label="卫生评分">
              <el-icon :size="24" color="var(--el-color-primary)"><component :is="Medal" /></el-icon>
              <span>卫生评分</span>
            </button>
            <button type="button" class="tool-btn" @click="router.push('/dormmanager/visitor')" aria-label="访客登记">
              <el-icon :size="24" color="var(--el-color-primary)"><component :is="UserCheck" /></el-icon>
              <span>访客登记</span>
            </button>
            <button type="button" class="tool-btn" @click="router.push('/dormmanager/late-return')" aria-label="晚归登记">
              <el-icon :size="24" color="var(--el-color-primary)"><component :is="Clock" /></el-icon>
              <span>晚归登记</span>
            </button>
          </div>
        </el-card>

        <!-- Management Memos -->
        <el-card shadow="never" class="side-card memos-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">备忘录</span>
              <el-button type="primary" link @click="router.push('/dormmanager/memos')">查看更多</el-button>
            </div>
          </template>
          <div class="memos-list">
            <div v-for="m in memos" :key="m.id" class="memo-item" @click="openMemoDialog(m)">
              <div class="memo-dot" :class="m.status === 'DONE' ? 'dot-green' : 'dot-blue'"></div>
              <div class="memo-content">
                <div class="memo-title">{{ m.title }}</div>
                <div class="memo-time">{{ m.status === 'DONE' ? '已办' : '待办' }}</div>
              </div>
            </div>
            <el-empty v-if="memos.length === 0" description="暂无备忘" :image-size="40"></el-empty>
            <el-button class="add-memo-btn" plain @click="openMemoDialog()">+ 快速备忘</el-button>
          </div>
        </el-card>

        <!-- Quick Memo Dialog -->
        <el-dialog v-model="memoDialogVisible" :title="memoEditId ? '编辑备忘：' + memoForm.title : '快速备忘'" width="420px" destroy-on-close>
          <el-form :model="memoForm" label-position="top">
            <el-form-item label="标题" required>
              <el-input v-model="memoForm.title" placeholder="输入标题" maxlength="50" />
            </el-form-item>
            <el-form-item label="内容">
              <el-input v-model="memoForm.description" type="textarea" :rows="4" placeholder="输入详情（可选）" maxlength="500" />
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="memoDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="saveMemo" :loading="memoSaving">保存</el-button>
            </span>
          </template>
        </el-dialog>

        <!-- System Notices -->
        <el-card shadow="never" class="side-card notices-card">
          <template #header>
            <div class="card-header"><span class="card-title">系统通知</span></div>
          </template>
          <div class="notice-list">
            <div v-for="n in notices" :key="n.id" class="notice-item" :class="{ unread: !n.read }" @click="openNoticeDialog(n)">
              <span class="notice-dot" :class="n.read ? 'dot-gray' : 'dot-primary'" aria-hidden="true"></span>
              <div class="notice-content-wrap">
                <span class="notice-title">{{ n.title }}</span>
                <span class="notice-desc">{{ n.description?.substring(0, 28) }}{{ n.description?.length > 28 ? '...' : '' }}</span>
              </div>
            </div>
            <el-empty v-if="notices.length === 0" description="暂无通知" :image-size="40"></el-empty>
          </div>
        </el-card>

        <!-- Notice Dialog -->
        <el-dialog v-model="noticeDialogVisible" :title="currentNotice?.title || '通知详情'" width="420px" destroy-on-close>
          <div class="notice-detail-body">
            <div class="notice-detail-time" v-if="currentNotice?.createTime">发布时间：{{ currentNotice.createTime }}</div>
            <div class="notice-detail-content">{{ currentNotice?.description || '暂无详细内容' }}</div>
          </div>
          <template #footer>
            <span class="dialog-footer">
              <el-button type="primary" @click="noticeDialogVisible = false">知道了</el-button>
            </span>
          </template>
        </el-dialog>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RefreshCw as Refresh, User, Settings, UserCheck, Clock, ChevronRight, UserPlus, Medal, Info, AlertCircle } from '@lucide/vue'
import { getRooms } from '../../api/room'
import { getBuildings } from '../../api/building'
import { getRepairs } from '../../api/repair'
import { getVisitorRecords } from '../../api/visitor'
import { getBusinessRecords, saveBusinessRecord } from '../../api/businessRecord'
import { useUserStore } from '../../store/user'
import request from '../../utils/request'
import WeatherWidget from '../../components/WeatherWidget.vue'

const router = useRouter()
const userStore = useUserStore()
const dynamicsView = ref('room')

const residentCount = ref(0)
const pendingRepairCount = ref(0)
const todayVisitorCount = ref(0)
const lateReturnCount = ref(0)
const totalBedCount = ref(0)
const loading = ref(false)
const rooms = ref([])
const buildings = ref([])
const selectedBuilding = ref('')
const pendingRepairs = ref([])
const memos = ref([])
const notices = ref([])

// Memo CRUD
const memoDialogVisible = ref(false)
const memoEditId = ref(null)
const memoForm = ref({ title: '', description: '' })
const memoSaving = ref(false)

// Notice read tracking
const noticeDialogVisible = ref(false)
const currentNotice = ref({})
const seenNoticeIds = ref(new Set())

function loadSeenNotices() {
  try {
    const key = `dorm-notices-read:${userStore.userInfo?.id || 'guest'}`
    const stored = JSON.parse(localStorage.getItem(key) || '[]')
    seenNoticeIds.value = new Set(Array.isArray(stored) ? stored : [])
  } catch {
    seenNoticeIds.value = new Set()
  }
}

function saveSeenNotices() {
  const key = `dorm-notices-read:${userStore.userInfo?.id || 'guest'}`
  localStorage.setItem(key, JSON.stringify([...seenNoticeIds.value]))
}

const openNoticeDialog = (notice) => {
  try {
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
  } catch (e) {
    console.error('openNoticeDialog error', e)
  }
}

const openMemoDialog = (memo) => {
  if (memo) {
    memoEditId.value = memo.id
    memoForm.value = { title: memo.title, description: memo.description || '' }
  } else {
    memoEditId.value = null
    memoForm.value = { title: '', description: '' }
  }
  memoDialogVisible.value = true
}

const saveMemo = async () => {
  if (!memoForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  memoSaving.value = true
  try {
    await saveBusinessRecord({
      id: memoEditId.value || undefined,
      type: 'manager_memos',
      title: memoForm.value.title.trim(),
      description: memoForm.value.description.trim(),
      status: 'PENDING',
      creatorId: userStore.userInfo?.id
    })
    ElMessage.success(memoEditId.value ? '已更新' : '已添加')
    memoDialogVisible.value = false
    loadMemos()
  } catch (e) { console.error(e)
    ElMessage.error('操作失败')
  } finally {
    memoSaving.value = false
  }
}

const loadMemos = async () => {
  try {
    const memoRecords = await getBusinessRecords('manager_memos')
    memos.value = (memoRecords || [])
      .map(r => ({
        id: r.id,
        title: r.title || '备忘',
        description: r.description || '',
        status: r.status === 'DONE' ? 'DONE' : 'PENDING',
        createTime: r.createTime
      }))
      .sort((a, b) => a.status === 'PENDING' && b.status === 'DONE' ? -1 : a.status === 'DONE' && b.status === 'PENDING' ? 1 : 0)
      .slice(0, 3)
  } catch (e) { console.error(e) }
}

const roomDialogVisible = ref(false)
const selectedRoom = ref(null)
const roomDetailBeds = ref([])
const roomDetailLoading = ref(false)

const weekNames = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
const todayDisplay = computed(() => {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth() + 1
  const d = now.getDate()
  const w = weekNames[now.getDay()]
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  return `${y}年${m}月${d}日${w} ${h}:${min}`
})

const filteredRooms = computed(() => {
  if (!selectedBuilding.value) return rooms.value
  return rooms.value.filter(r => r.buildingId === selectedBuilding.value)
})

const openRoomDetail = async (room) => {
  selectedRoom.value = room
  roomDialogVisible.value = true
  roomDetailLoading.value = true
  try {
    const beds = await request({ url: `/bed/list?roomId=${room.id}`, method: 'get' })
    roomDetailBeds.value = (beds || [])
  } catch (e) { console.error(e);
    ElMessage.error('获取房间详情失败')
  } finally {
    roomDetailLoading.value = false
  }
}

const getOccupantForBed = (index) => {
  const suffix = `-${index}`
  return roomDetailBeds.value.find(b => b.bedNumber && b.bedNumber.endsWith(suffix) && b.studentId)
}

const loadData = async () => {
  loadSeenNotices()
  loading.value = true
  try {
    const [repairList, visitors, buildingList, buildingStatsRes, noticeRecords] = await Promise.all([
      getRepairs(), getVisitorRecords(), getBuildings(), request({ url: '/dashboard/buildings', method: 'get' }),
      getBusinessRecords('admin_notice')
    ])
    
    notices.value = (noticeRecords || [])
      .map(r => ({
        id: r.id,
        title: r.title || '',
        description: r.description || '',
        createTime: r.createTime ? r.createTime.replace('T', ' ').substring(0, 16) : '',
        read: seenNoticeIds.value.has(r.id)
      }))
      .sort((a, b) => (a.read ? 1 : 0) - (b.read ? 1 : 0))
      .slice(0, 3)
    
    loadMemos()
    
    buildings.value = buildingList || []
    
    // Calculate resident count from building stats
    if (buildingStatsRes) {
      residentCount.value = buildingStatsRes.reduce((acc, curr) => acc + (curr.occupiedBeds || 0), 0)
      totalBedCount.value = buildingStatsRes.reduce((acc, curr) => acc + (curr.totalBeds || 0), 0)
    }
    
    const pendings = repairList.filter(r => r.status === 'PENDING')
    pendingRepairCount.value = pendings.length
    pendingRepairs.value = pendings.slice(0, 3) // preview top 3
    
    const today = new Date().toDateString()
    todayVisitorCount.value = visitors.filter(v => new Date(v.createTime).toDateString() === today).length
    
    // Fetch late returns
    try {
      const lateRes = await request({ url: '/lateReturnRecord/list', method: 'get' })
      lateReturnCount.value = lateRes ? lateRes.filter(r => r.status === 'PENDING').length : 0
    } catch (err) {
      console.error('Failed to fetch late returns', err)
    }
    
    // Initial rooms load for the first building
    if (buildings.value.length > 0) {
      selectedBuilding.value = buildings.value[0].id
      await fetchRoomsForBuilding(selectedBuilding.value)
    }
  } catch (e) {
    console.error('Failed to load workbench data', e)
  } finally {
    loading.value = false
  }
}

const fetchRoomsForBuilding = async (bId) => {
  if (!bId) {
    rooms.value = []
    return
  }
  try {
    const roomList = await getRooms(bId)
    rooms.value = (roomList || []).map(r => ({
      id: r.id,
      number: r.roomNumber,
      buildingId: r.buildingId,
      buildingName: r.buildingName,
      capacity: r.capacity || 4,
      occupied: r.occupied || 0,
      warning: false
    }))
  } catch(e) {
    console.error('Failed to load rooms for building', e)
  }
}

watch(selectedBuilding, (newVal) => {
  fetchRoomsForBuilding(newVal)
})

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
  color: var(--primary);
  font-weight: 500;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 16px;
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

.bg-blue { background: var(--primary-2); color: var(--primary); }
.bg-orange { background: var(--orange-2); color: var(--orange); }
.bg-red { background: var(--danger-2); color: var(--danger); }
.text-red { color: var(--danger) !important; }

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

.card-header-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.building-select {
  width: 150px;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 12px;
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
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.room-box:hover {
  border-color: var(--primary);
  box-shadow: 0 2px 8px var(--primary-2);
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
  background-color: var(--primary);
}

.room-warning-icon {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 16px;
  height: 16px;
  background-color: var(--danger);
  color: var(--surface);
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
  border: 0;
  border-radius: 12px;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font: inherit;
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
  max-height: 360px;
  overflow-y: auto;
  min-height: 0;
}

.memo-item {
  display: flex;
  gap: 12px;
  background: var(--bg);
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.memo-item:hover {
  background: var(--el-color-primary-light-9);
}

.memo-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
}

.dot-orange { background-color: var(--warn); }
.dot-blue { background-color: var(--primary); }
.dot-green { background-color: var(--success); }

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
  max-height: 300px;
  overflow-y: auto;
  min-height: 0;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  border-radius: 8px;
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
  width: 7px;
  height: 7px;
  margin-top: 6px;
  border-radius: 50%;
}

.dot-gray { background: var(--line); }
.dot-primary { background: var(--el-color-primary); }

.notice-content-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.notice-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  line-height: 1.4;
}

.notice-item.unread .notice-title {
  color: var(--el-color-primary);
}

.notice-desc {
  font-size: 12px;
  color: var(--sub);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

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

/* Bed Layout Styles */
.layout-wrapper {
  padding: 20px 0 40px;
  display: flex;
  justify-content: center;
}
.floor-plan {
  position: relative;
  width: 100%;
  max-width: 500px;
  height: 260px;
}
.plan-border {
  position: absolute;
  top: 0; left: 24px; right: 24px; bottom: 0;
  border: 4px solid var(--line);
  border-radius: 12px;
}
.plan-border::before {
  content: '';
  position: absolute;
  top: -4px; left: 10%; width: 60px; height: 8px;
  background: var(--surface);
}
.plan-border::after {
  content: '';
  position: absolute;
  bottom: -4px; right: 10%; width: 80px; height: 8px;
  background: var(--surface);
}
.door-label {
  position: absolute;
  top: -12px;
  left: calc(24px + 10% + 15px);
  background: var(--surface);
  padding: 0 12px;
  color: var(--sub);
  font-weight: 500;
  font-size: 14px;
}
.balcony-label {
  position: absolute;
  bottom: -12px;
  right: calc(24px + 10% + 15px);
  background: var(--surface);
  padding: 0 12px;
  color: var(--sub);
  font-weight: 500;
  font-size: 14px;
}
.beds-grid {
  position: absolute;
  top: 40px; left: 50px; right: 50px; bottom: 40px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 30px 60px;
  place-items: center;
}
.bed-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.bed-box {
  width: 80px;
  height: 48px;
  border: 2px solid var(--line);
  border-radius: 8px;
  background: var(--bg);
  display: grid;
  place-items: center;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
  transition: all 0.3s ease;
}
.bed-number {
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 14px;
}
.bed-owner {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}
.bed-slot.is-occupied .bed-box {
  border-color: var(--sub);
  background: var(--muted);
}
.bed-slot.is-occupied .bed-number {
  color: var(--text-secondary);
}
.bed-slot.is-empty .bed-box {
  border: 2px dashed var(--line);
  background: var(--surface);
}
.bed-slot.is-empty .bed-number,
.bed-slot.is-empty .bed-owner {
  color: var(--sub);
}

.rooms-grid-skeleton {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 12px;
  padding: 16px 0;
}

.rooms-grid-skeleton .skeleton {
  border-radius: 8px;
}

/* === Responsive === */
@media (max-width: 1024px) {
  .workbench-container :deep(.el-col) {
    flex: 0 0 100% !important;
    max-width: 100% !important;
  }

  .hero-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .tools-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .stats-grid :deep(.el-col) {
    flex: 0 0 100% !important;
    max-width: 100% !important;
  }

  .tools-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .rooms-grid {
    grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  }

  .beds-grid {
    gap: 20px 30px !important;
  }
}

.hygiene-view {
  padding: 32px 0;
  display: flex;
  justify-content: center;
}
</style>
