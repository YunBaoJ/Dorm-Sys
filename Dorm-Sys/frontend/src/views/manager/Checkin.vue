<template>
  <div class="checkin-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <el-card shadow="never" class="hero-card">
          <div class="hero-content">
            <div class="hero-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="User" /></el-icon>
              <div>
                <h2>入住管理</h2>
                <p>规范登记 · 动态流转</p>
              </div>
            </div>
            <el-button type="primary" @click="openCheckinDialog"><el-icon class="el-icon--left"><component :is="Plus" /></el-icon>办理新入住</el-button>
          </div>
        </el-card>

        <!-- Checkin Dialog -->
        <el-dialog v-model="checkinVisible" title="办理新入住" width="500px" destroy-on-close>
          <el-form :model="form" label-width="90px">
            <el-form-item label="选择学生" required>
              <el-select v-model="form.studentId" placeholder="请选择未入住学生" filterable style="width: 100%" @change="onStudentChange">
                <el-option v-for="u in unassignedStudents" :key="u.id" :label="`${u.name} (${u.gender}) - ${u.username}`" :value="u.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="目标楼栋" required>
              <el-select v-model="form.buildingId" placeholder="选择楼栋" @change="onBuildingChange" style="width: 100%" :disabled="!form.studentId">
                <el-option v-for="b in availableBuildings" :key="b.id" :label="b.name" :value="b.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="目标房间" required>
              <el-select v-model="form.roomId" placeholder="选择房间" @change="onRoomChange" style="width: 100%" :disabled="!form.buildingId">
                <el-option v-for="r in currentRooms" :key="r.id" :label="r.roomNumber" :value="r.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="空闲床位" required>
              <el-select v-model="form.bedId" placeholder="选择床位" style="width: 100%" :disabled="!form.roomId">
                <el-option v-for="b in currentBeds" :key="b.id" :label="`${b.bedNumber}号床`" :value="b.id" />
              </el-select>
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="checkinVisible = false">取消</el-button>
              <el-button type="primary" @click="submitCheckin" :loading="submitting">确认入住</el-button>
            </span>
          </template>
        </el-dialog>

        <!-- Resident List Section -->
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <span class="list-title">在住人员</span>
            <div class="list-actions">
              <el-input v-model="search" placeholder="姓名/学号/房间号" prefix-icon="Search" style="width: 240px" />
              <el-select v-model="statusFilter" style="width: 120px; margin-left: 12px">
                <el-option label="在住" value="active" />
                <el-option label="已退宿" value="inactive" />
              </el-select>
              <el-button icon="Refresh" circle style="margin-left: 12px" />
            </div>
          </div>

          <div class="resident-list" v-loading="loading">
            <div v-for="r in residents" :key="r.id" class="resident-item">
              <el-avatar :size="48" :src="r.avatar">{{ r.name?.[0] }}</el-avatar>
              
              <div class="resident-info">
                <div class="resident-main">
                  <span class="resident-name">{{ r.name }}</span>
                  <span class="resident-id">{{ r.id }}</span>
                  <el-tag size="small" type="primary" effect="plain" round>{{ r.status }}</el-tag>
                </div>
                <div class="resident-meta">
                  <span class="meta-item"><el-icon><component :is="Home" /></el-icon> {{ r.room }}</span>
                  <span class="meta-item"><el-icon><component :is="CalendarDays" /></el-icon> 入住: {{ r.date }}</span>
                </div>
              </div>

              <div class="resident-actions">
                <el-button type="danger" link @click="handleCheckout(r)">退宿办理</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="6">
        <!-- Data Dashboard -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>数据看板</span></div>
          </template>
          <div class="dashboard-list">
            <div class="dash-item">
              <div class="dash-icon bg-light-blue"><el-icon><component :is="User" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">系统内总学生</div>
                <div class="dash-value">{{ totalStudents }} <span>人</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-light-orange"><el-icon><component :is="Check" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">当前在住</div>
                <div class="dash-value">{{ currentOccupied }} <span>人</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-gray"><el-icon><component :is="X" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">未分配床位(空闲)</div>
                <div class="dash-value">{{ unassignedCount }} <span>人</span></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Building Occupation -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>楼栋占用情况</span></div>
          </template>
          <div class="occupation-list">
            <div class="occ-item" v-for="occ in buildingOccupations" :key="occ.id">
              <div class="occ-head">
                <span>{{ occ.name }}</span>
                <span class="text-blue">{{ occ.percentage }}%</span>
              </div>
              <el-progress :percentage="occ.percentage" :show-text="false" color="#3b82f6" />
            </div>
            <div v-if="buildingOccupations.length === 0" style="color:var(--sub); font-size:13px">暂无楼栋数据</div>
          </div>
        </el-card>

        <!-- Notes -->
        <el-card shadow="never" class="side-card bg-light-blue notes-card">
          <template #header>
            <div class="card-header">
              <span>办理须知</span>
              <el-icon><component :is="Info" /></el-icon>
            </div>
          </template>
          <ol class="tips-list">
            <li>入住前需核对学生身份并确认完成注册。</li>
            <li>床位分配遵循系统规划，严禁私自调换。</li>
            <li>退宿须实地检查设施及钥匙回收情况。</li>
          </ol>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { User, Plus, Search, RefreshCw as Refresh, Home, CalendarDays, Check, X, Info } from '@lucide/vue'
import { getBuildings } from '../../api/building'
import { getRooms } from '../../api/room'
import request from '../../utils/request'
import { getUnassignedStudents } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const search = ref('')
const statusFilter = ref('active')
const residents = ref([])
const loading = ref(false)

const checkinVisible = ref(false)
const submitting = ref(false)
const form = ref({ studentId: null, buildingId: null, roomId: null, bedId: null })

const buildings = ref([])
const allRooms = ref([])
const allBeds = ref([])
const unassignedStudents = ref([])
const currentRooms = ref([])
const currentBeds = ref([])

const totalStudents = ref(0)
const currentOccupied = ref(0)
const unassignedCount = ref(0)

const fetchResidents = async () => {
  loading.value = true
  try {
    const me = userStore.userInfo || {}
    const managerGender = (me.role === 'dormmanager' && me.gender) ? me.gender : null

    const [bedsRes, roomsRes, buildingsRes, usersRes, unassignedRes] = await Promise.all([
      request({ url: '/bed/list', method: 'get' }),
      getRooms(), 
      getBuildings(),
      request({ url: '/user/list', method: 'get' }),
      getUnassignedStudents(managerGender)
    ])
    const beds = bedsRes || []
    const rooms = roomsRes || []
    allBeds.value = beds
    allRooms.value = rooms
    buildings.value = buildingsRes || []
    const users = usersRes || []
    unassignedStudents.value = unassignedRes.data || []
    
    const roomMap = Object.fromEntries(rooms.map(r => [r.id, r]))
    const buildingMap = Object.fromEntries(buildings.value.map(b => [b.id, b.name]))
    
    const occupiedBeds = beds.filter(b => b.studentId && b.status === 'OCCUPIED')
    
    const studentUsers = users.filter(u => u.role === 'student' && (!managerGender || u.gender === managerGender))
    const validStudentIds = new Set(studentUsers.map(u => u.id))
    
    const myOccupiedBeds = occupiedBeds.filter(b => validStudentIds.has(b.studentId))
    
    totalStudents.value = studentUsers.length
    currentOccupied.value = myOccupiedBeds.length
    unassignedCount.value = unassignedStudents.value.length
    
    residents.value = myOccupiedBeds.map(bed => {
      const room = roomMap[bed.roomId] || {}
      const bName = buildingMap[room.buildingId] || ''
      return {
        id: bed.studentId || '未知',
        name: bed.studentName || '未知',
        room: `${bName} · ${room.roomNumber || '?'} · ${bed.bedNumber}`,
        date: bed.createTime ? bed.createTime.substring(5, 10) : '09/01',
        status: '在住',
        avatar: '',
        bedId: bed.id
      }
    })
  } catch (e) { console.error(e);
    ElMessage.error('获取住宿信息失败')
  } finally {
    loading.value = false
  }
}

const buildingOccupations = computed(() => {
  if (!buildings.value.length || !allRooms.value.length || !allBeds.value.length) return []
  return buildings.value.map(b => {
    const bRooms = allRooms.value.filter(r => r.buildingId === b.id).map(r => r.id)
    const bBeds = allBeds.value.filter(bed => bRooms.includes(bed.roomId))
    const total = bBeds.length
    const occupied = bBeds.filter(bed => bed.studentId && bed.status === 'OCCUPIED').length
    const percentage = total === 0 ? 0 : Math.round((occupied / total) * 100)
    return {
      id: b.id,
      name: b.name,
      percentage
    }
  })
})

onMounted(() => fetchResidents())

const openCheckinDialog = () => {
  form.value = { studentId: null, buildingId: null, roomId: null, bedId: null }
  currentRooms.value = []
  currentBeds.value = []
  checkinVisible.value = true
}

const availableBuildings = computed(() => {
  if (!form.value.studentId) return []
  const student = unassignedStudents.value.find(s => s.id === form.value.studentId)
  if (student && student.gender) {
    if (student.gender === '男') {
      return buildings.value.filter(b => (b.type || '').includes('男'))
    } else if (student.gender === '女') {
      return buildings.value.filter(b => (b.type || '').includes('女'))
    }
  }
  return buildings.value
})

const onStudentChange = () => {
  form.value.buildingId = null
  form.value.roomId = null
  form.value.bedId = null
  currentRooms.value = []
  currentBeds.value = []
}

const onBuildingChange = async (val) => {
  form.value.roomId = null
  form.value.bedId = null
  currentBeds.value = []
  if (!val) {
    currentRooms.value = []
    return
  }
  try {
    const rooms = await getRooms(val)
    currentRooms.value = rooms || []
  } catch (e) { console.error(e);
    ElMessage.error('获取房间失败')
  }
}

const onRoomChange = async (val) => {
  form.value.bedId = null
  if (!val) {
    currentBeds.value = []
    return
  }
  try {
    const beds = await request({ url: `/bed/list?roomId=${val}`, method: 'get' })
    currentBeds.value = (beds || []).filter(b => b.status === 'EMPTY' || !b.studentId)
  } catch (e) { console.error(e);
    ElMessage.error('获取床位失败')
  }
}

const submitCheckin = async () => {
  if (!form.value.studentId || !form.value.bedId) {
    ElMessage.warning('请完整选择学生和床位')
    return
  }
  submitting.value = true
  try {
    await request({ 
      url: '/bed/save', 
      method: 'post', 
      data: { id: form.value.bedId, studentId: form.value.studentId, status: 'OCCUPIED' } 
    })
    ElMessage.success('办理入住成功')
    checkinVisible.value = false
    fetchResidents()
  } catch (e) { console.error(e);
    ElMessage.error('入住办理失败')
  } finally {
    submitting.value = false
  }
}

const handleCheckout = async (r) => {
  try {
    await ElMessageBox.confirm(`确定要为 ${r.name} 办理退宿吗？`, '退宿确认', { type: 'warning' })
    await request({ url: '/bed/save', method: 'post', data: { id: r.bedId, status: 'EMPTY', studentId: null } })
    ElMessage.success('退宿办理成功')
    fetchResidents()
  } catch (e) { console.error(e);
    if (e !== 'cancel') ElMessage.error('退宿操作失败')
  }
}
</script>

<style scoped>
.checkin-container {
  max-width: 1400px;
  margin: 0 auto;
}

.hero-card {
  margin-bottom: 24px;
  border-radius: 12px;
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
  margin: 0 0 4px 0;
  font-size: 20px;
  color: var(--text);
}

.hero-text p {
  margin: 0;
  font-size: 14px;
  color: var(--sub);
}

.list-card {
  min-height: 600px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.list-title {
  font-size: 16px;
  font-weight: bold;
  color: var(--text);
}

.list-actions {
  display: flex;
  align-items: center;
}

.resident-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.resident-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: var(--bg);
  border-radius: 12px;
  transition: background-color 0.2s;
}

.resident-item:hover {
  background-color: var(--line);
}

.resident-info {
  flex: 1;
  margin-left: 16px;
}

.resident-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.resident-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}

.resident-id {
  font-size: 14px;
  color: var(--sub);
}

.resident-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 13px;
  color: var(--sub);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.resident-actions {
  display: flex;
  gap: 16px;
}

.side-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.dashboard-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dash-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg);
  border-radius: 12px;
}

.dash-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
}

.bg-light-blue { background: var(--primary-2); color: #3b82f6; }
.bg-light-orange { background: var(--orange-2); color: #f97316; }
.bg-gray { background: var(--line); color: var(--sub); }

.dash-info {
  flex: 1;
}

.dash-label {
  font-size: 13px;
  color: var(--sub);
  margin-bottom: 4px;
}

.dash-value {
  font-size: 20px;
  font-weight: bold;
  color: var(--text);
}

.dash-value span {
  font-size: 13px;
  font-weight: normal;
  color: var(--sub);
}

.occupation-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.occ-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.occ-head {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: var(--text-secondary);
}

.text-blue { color: #3b82f6; font-weight: bold; }

.notes-card {
  background-color: var(--primary-2);
  border: 1px solid var(--primary-2) !important;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: var(--text);
  line-height: 2;
}

.tips-list li::marker {
  color: #3b82f6;
}
</style>
