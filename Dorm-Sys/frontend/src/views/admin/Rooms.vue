<template>
  <div class="rooms-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <el-card shadow="never" class="hero-card">
          <div class="hero-content">
            <div class="hero-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="Home" /></el-icon>
              <div>
                <h2>宿舍房间配置</h2>
                <p>精细化分配 · 状态实时追踪</p>
              </div>
            </div>
            <div class="hero-actions">
              <el-button type="primary" @click="handleAdd"><el-icon class="el-icon--left"><component :is="Plus" /></el-icon>新增单间</el-button>
              <el-button plain type="warning"><el-icon class="el-icon--left"><component :is="Files" /></el-icon>批量创建</el-button>
            </div>
          </div>
        </el-card>

        <!-- Room List Section -->
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <div class="header-left">
              <span class="list-title">房间名录</span>
              <el-select v-model="buildingFilter" placeholder="所属楼栋" style="width: 140px; margin-left: 16px;" clearable @change="fetchRooms">
                <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
              </el-select>
              <el-input v-model="search" placeholder="房间号搜索" prefix-icon="Search" style="width: 180px; margin-left: 12px;" />
            </div>
            <div class="list-actions">
              <el-button circle @click="fetchRooms"><el-icon><component :is="Refresh" /></el-icon></el-button>
            </div>
          </div>

          <div class="room-list" v-loading="loading">
            <div v-for="room in filteredRooms" :key="room.id" class="room-item">
              <div class="room-badge bg-light-blue">
                <span class="room-no">{{ room.roomNumber }}</span>
                <span class="room-type">{{ room.capacity }}人间</span>
              </div>
              
              <div class="room-info">
                <div class="room-main">
                  <span class="room-bldg"><el-icon><component :is="Building" /></el-icon> {{ room.buildingName }}</span>
                  <span class="room-floor">{{ room.floor }}层</span>
                </div>
                
                <div class="progress-section">
                  <div class="progress-header">
                    <span class="progress-label">入住进度</span>
                    <span class="progress-val">{{ room.occupied }}/{{ room.capacity }}</span>
                  </div>
                  <el-progress 
                    :percentage="room.capacity ? (room.occupied / room.capacity) * 100 : 0" 
                    :stroke-width="8" 
                    :color="room.occupied === room.capacity ? '#f59e0b' : 'var(--line)'"
                    :show-text="false"
                  />
                </div>
                
                <div class="room-tags">
                  <el-tag v-if="room.occupied === room.capacity" size="small" type="warning" effect="plain">全部满员</el-tag>
                  <el-tag v-else size="small" type="info" effect="plain">未满员</el-tag>
                  <el-tag v-if="room.status === 'MAINTENANCE'" size="small" type="warning" effect="plain">维护中</el-tag>
                  <el-tag v-else size="small" type="primary" effect="plain">可用</el-tag>
                </div>
              </div>

              <div class="room-actions-col">
                <div class="action-buttons">
                  <el-button type="primary" link @click="handleEdit(room)"><el-icon class="el-icon--left"><component :is="Edit" /></el-icon>编辑</el-button>
                  <el-button type="primary" link @click="handleViewBeds(room)"><el-icon class="el-icon--left"><component :is="Eye" /></el-icon>床位</el-button>
                  <el-button type="danger" link @click="handleDelete(room.id)"><el-icon class="el-icon--left"><component :is="Trash2" /></el-icon>删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="6">
        <!-- Status Distribution -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>状态分布</span></div>
          </template>
          <div class="dashboard-list">
            <div class="dash-item">
              <div class="dash-icon bg-light-blue"><el-icon color="#3b82f6"><component :is="Check" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">空闲房间</div>
                <div class="dash-value">42 <span>间</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-light-orange"><el-icon color="#f59e0b"><component :is="AlertCircle" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">满员房间</div>
                <div class="dash-value">1,208 <span>间</span></div>
              </div>
            </div>
            <div class="dash-item">
              <div class="dash-icon bg-gray"><el-icon color="var(--sub)"><component :is="Settings" /></el-icon></div>
              <div class="dash-info">
                <div class="dash-label">维修中</div>
                <div class="dash-value">15 <span>间</span></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Allocation Guide -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>配额说明</span></div>
          </template>
          <div class="guide-list">
            <div class="guide-item">
              <div class="guide-num">4</div>
              <div class="guide-info">
                <div class="guide-title">四人间 (标配)</div>
                <div class="guide-desc">适合本科生，配套书桌、衣柜齐全。</div>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-num bg-indigo">6</div>
              <div class="guide-info">
                <div class="guide-title">六人间/八人间</div>
                <div class="guide-desc">主要用于公共区域或特殊公寓楼栋。</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Management Tips -->
        <el-card shadow="never" class="side-card bg-light-blue rules-card">
          <template #header>
            <div class="card-header">
              <span class="text-primary">房源管理建议</span>
              <el-icon><component :is="Info" /></el-icon>
            </div>
          </template>
          <ul class="tips-list">
            <li>批量创建时请核对起始房号与实际物理排布。</li>
            <li>房间状态设为"禁用"将导致其下所有床位不可选。</li>
            <li>维修中的房间不会出现在学生的调宿申请可选列表中。</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>

    <!-- Room Form Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="所属楼栋" required>
          <el-select v-model="form.buildingId" placeholder="请选择楼栋" style="width: 100%">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" required>
          <el-input v-model="form.roomNumber" placeholder="如：101" />
        </el-form-item>
        <el-form-item label="所在楼层" required>
          <el-input-number v-model="form.floor" :min="1" />
        </el-form-item>
        <el-form-item label="可容纳人数" required>
          <el-input-number v-model="form.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="房间状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常可用" value="NORMAL" />
            <el-option label="已满员" value="FULL" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Beds Drawer -->
    <el-drawer v-model="bedDrawerVisible" :title="`床位管理 - ${currentRoom?.buildingName} ${currentRoom?.roomNumber}`" size="500px">
      <div class="beds-container" v-loading="bedLoading">
        <el-table :data="beds" style="width: 100%" border>
          <el-table-column prop="bedNumber" label="床位编号" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'EMPTY' ? 'success' : row.status === 'OCCUPIED' ? 'danger' : 'warning'" size="small">
                {{ row.status === 'EMPTY' ? '空闲' : row.status === 'OCCUPIED' ? '已入住' : '损坏' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="当前住客">
            <template #default="{ row }">
              {{ row.studentName || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Home, Plus, Files, Search, RefreshCw as Refresh, Building, Edit, Eye, Trash2, Clock, Check, AlertCircle, Settings, Info } from '@lucide/vue'
import { getRooms, saveRoom, deleteRoom, getBeds } from '../../api/room'
import { getBuildings } from '../../api/building'
import { ElMessage, ElMessageBox } from 'element-plus'

const buildingFilter = ref('')
const search = ref('')
const rooms = ref([])
const buildings = ref([])
const loading = ref(false)

const filteredRooms = computed(() => {
  if (!search.value) return rooms.value
  const lowerSearch = search.value.toLowerCase()
  return rooms.value.filter(r => (r.roomNumber || '').toLowerCase().includes(lowerSearch))
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增房间')
const form = ref({
  id: null,
  buildingId: null,
  roomNumber: '',
  floor: 1,
  capacity: 4,
  status: 'NORMAL'
})

const bedDrawerVisible = ref(false)
const bedLoading = ref(false)
const beds = ref([])
const currentRoom = ref(null)

onMounted(async () => {
  await fetchBuildings()
  fetchRooms()
})

const fetchBuildings = async () => {
  try {
    const res = await getBuildings()
    buildings.value = res || []
  } catch (error) {
    console.error('Failed to fetch buildings', error)
  }
}

const fetchRooms = async () => {
  loading.value = true
  try {
    const res = await getRooms(buildingFilter.value || null)
    rooms.value = res || []
  } catch (error) { console.error(error);
    ElMessage.error('获取房间列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增房间'
  form.value = {
    id: null,
    buildingId: null,
    roomNumber: '',
    floor: 1,
    capacity: 4,
    status: 'NORMAL'
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑房间'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.buildingId || !form.value.roomNumber) {
    ElMessage.warning('请填写楼栋和房间号')
    return
  }
  try {
    await saveRoom(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchRooms()
  } catch (error) { console.error(error);
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该房间及其所有关联床位吗？', '警告', { type: 'warning' })
    await deleteRoom(id)
    ElMessage.success('删除成功')
    fetchRooms()
  } catch (error) { console.error(error);
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleViewBeds = async (room) => {
  currentRoom.value = room
  bedDrawerVisible.value = true
  bedLoading.value = true
  try {
    const res = await getBeds(room.id)
    beds.value = res || []
  } catch (error) { console.error(error);
    ElMessage.error('获取床位信息失败')
  } finally {
    bedLoading.value = false
  }
}
</script>

<style scoped>
.rooms-container {
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

.hero-actions {
  display: flex;
  gap: 12px;
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

.header-left {
  display: flex;
  align-items: center;
}

.list-title {
  font-size: 16px;
  font-weight: bold;
  color: var(--text);
}

.room-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.room-item {
  display: flex;
  align-items: center;
  padding: 24px;
  background-color: var(--bg);
  border-radius: 12px;
  transition: background-color 0.2s;
}

.room-item:hover {
  background-color: var(--line);
}

.room-badge {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #3b82f6;
}

.bg-light-blue { background: var(--primary-2); }

.room-no {
  font-size: 24px;
  font-weight: bold;
}

.room-type {
  font-size: 12px;
  opacity: 0.8;
}

.room-info {
  flex: 1;
  margin-left: 24px;
}

.room-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 14px;
  color: var(--text);
}

.room-bldg {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.room-floor {
  color: var(--sub);
}

.progress-section {
  max-width: 400px;
  margin-bottom: 16px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.progress-label {
  color: var(--sub);
}

.progress-val {
  font-weight: 500;
  color: var(--text);
}

.room-tags {
  display: flex;
  gap: 8px;
}

.room-actions-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 32px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.room-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--sub);
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
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
}

.bg-light-orange { background: var(--orange-2); }
.bg-gray { background: var(--line); }

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

.guide-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.guide-item {
  display: flex;
  gap: 16px;
}

.guide-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--primary-2);
  color: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.bg-indigo {
  background: #eef2ff;
  color: #6366f1;
}

.guide-info {
  flex: 1;
}

.guide-title {
  font-size: 14px;
  font-weight: bold;
  color: var(--text);
  margin-bottom: 4px;
}

.guide-desc {
  font-size: 13px;
  color: var(--sub);
  line-height: 1.5;
}

.rules-card {
  background-color: var(--primary-2);
  border: 1px solid var(--primary-2) !important;
}

.text-primary {
  color: #3b82f6;
}

.tips-list {
  margin: 0;
  padding-left: 16px;
  font-size: 13px;
  color: var(--text);
  line-height: 2;
}

.tips-list li::marker {
  color: #3b82f6;
}
</style>
