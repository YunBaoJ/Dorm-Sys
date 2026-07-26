<template>
  <div class="resources-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <el-card shadow="never" class="hero-card">
          <div class="hero-content">
            <div class="hero-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="Building2" /></el-icon>
              <div>
                <h2>楼栋资源管理</h2>
                <p>资产登记 · 房源动态配置</p>
              </div>
            </div>
            <el-button type="primary" @click="openAddDialog"><el-icon class="el-icon--left"><component :is="Plus" /></el-icon>新增宿舍楼栋</el-button>
          </div>
        </el-card>

        <!-- Building List Section -->
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <span class="list-title">楼宇列表</span>
            <div class="list-actions">
              <el-input v-model="search" placeholder="搜索楼栋名称" prefix-icon="Search" style="width: 240px" />
              <el-button icon="Refresh" circle style="margin-left: 12px" @click="loadBuildings" :loading="loading" />
            </div>
          </div>

          <div class="building-list">
            <div v-for="b in filteredBuildings" :key="b.id" class="building-item">
              <div class="building-icon" :class="b.type === '男生楼' ? 'bg-blue' : 'bg-orange'">
                <el-icon :size="32"><component :is="Building" /></el-icon>
                <el-tag size="small" :type="b.type === '男生楼' ? 'primary' : 'warning'" effect="plain" class="type-tag">{{ b.type }}</el-tag>
              </div>
              
              <div class="building-info">
                <div class="building-main">
                  <span class="building-name">{{ b.name }}</span>
                  <span class="building-no">#{{ b.id }}</span>
                  <el-tag size="small" type="info" effect="plain" round>{{ b.floors }} 层</el-tag>
                </div>
                
                <div class="building-manager">
                  <el-icon><component :is="UserRound" /></el-icon> 负责人：{{ b.manager }}
                </div>
                
                <div class="building-stats">
                  <div class="stat-col">
                    <span class="stat-label">总房间</span>
                    <span class="stat-val text-dark">{{ b.totalRooms }}</span>
                  </div>
                  <div class="stat-col">
                    <span class="stat-label">已占用</span>
                    <span class="stat-val text-blue">{{ b.occupiedRooms }}</span>
                  </div>
                  <div class="stat-col">
                    <span class="stat-label">空余</span>
                    <span class="stat-val text-orange">{{ b.freeRooms }}</span>
                  </div>
                </div>

                <div class="building-location">
                  <el-icon><component :is="MapPin" /></el-icon> {{ b.location }}
                </div>
              </div>

              <div class="building-actions">
                <div class="status-toggle">
                  <span class="status-label">运营中</span>
                  <el-switch v-model="b.active" :loading="b.statusSaving" @change="handleStatusChange(b)" />
                </div>
                <div class="btn-group">
                  <el-button type="primary" link @click="openEditDialog(b)"><el-icon class="el-icon--left"><component :is="Edit" /></el-icon>编辑</el-button>
                  <el-button type="danger" link @click="handleDelete(b.id)"><el-icon class="el-icon--left"><component :is="Delete" /></el-icon>删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="6">
        <!-- Asset Overview -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>资产概览</span></div>
          </template>
          <div class="asset-list">
            <div class="asset-item">
              <div class="asset-icon bg-light-blue"><el-icon><component :is="Building" /></el-icon></div>
              <div class="asset-info">
                <div class="asset-label">楼栋总数</div>
                <div class="asset-value">{{ assetOverview.buildingCount }} <span>栋</span></div>
              </div>
            </div>
            <div class="asset-item">
              <div class="asset-icon bg-light-orange"><el-icon><component :is="Home" /></el-icon></div>
              <div class="asset-info">
                <div class="asset-label">总床位数</div>
                <div class="asset-value">{{ assetOverview.totalBeds }} <span>张</span></div>
              </div>
            </div>
            <div class="asset-item">
              <div class="asset-icon bg-light-cyan"><el-icon><component :is="Clock" /></el-icon></div>
              <div class="asset-info">
                <div class="asset-label">平均入住率</div>
                <div class="asset-value">{{ assetOverview.occupancyRate }} <span>%</span></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Resource Allocation Rules -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>资源分配说明</span></div>
          </template>
          <div class="rules-list">
            <div class="rule-item">
              <div class="rule-number">1</div>
              <div class="rule-content">
                <div class="rule-title">性别隔离原则</div>
                <div class="rule-desc">宿舍楼栋必须明确性别限制，不可混合入住，系统将强制校验。</div>
              </div>
            </div>
            <div class="rule-item">
              <div class="rule-number">2</div>
              <div class="rule-content">
                <div class="rule-title">宿管员配置</div>
                <div class="rule-desc">每栋楼须指派至少一名专职宿管员，负责日常考勤与报修初审。</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Maintenance Tips -->
        <el-card shadow="never" class="side-card bg-light-blue tips-card">
          <template #header>
            <div class="card-header">
              <span>维护贴士</span>
              <el-icon><component :is="Info" /></el-icon>
            </div>
          </template>
          <ul class="tips-list">
            <li>寒暑假封楼前，请统一更新楼栋运营状态为"禁用"。</li>
            <li>新楼启用需先录入楼栋信息，再批量导入房间数据。</li>
            <li>楼层数修改将影响房间号的自动生成逻辑。</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>

    <!-- 楼栋表单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑楼栋' : '新增楼栋'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="楼栋名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：明德楼" />
        </el-form-item>
        <el-form-item label="楼栋类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option label="男生楼" value="男生楼" />
            <el-option label="女生楼" value="女生楼" />
          </el-select>
        </el-form-item>
        <el-form-item label="总楼层" prop="floors">
          <el-input-number v-model="form.floors" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="form.manager" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="地理位置" prop="location">
          <el-input v-model="form.location" placeholder="例如：东区A区" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { Building2, Building, Home, Clock, Info, Search, RefreshCw as Refresh, Plus, MapPin, UserRound, Edit, Delete } from '@lucide/vue'
import { getBuildingList, saveBuilding, deleteBuilding } from '../../api/building'
import { getRooms } from '../../api/room'
import { ElMessage, ElMessageBox } from 'element-plus'

const search = ref('')

const buildings = ref([])
const rooms = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const filteredBuildings = computed(() => {
  if (!search.value) return buildings.value
  const lowerSearch = search.value.toLowerCase()
  return buildings.value.filter(b => b.name.toLowerCase().includes(lowerSearch))
})

const assetOverview = computed(() => {
  const totalBeds = rooms.value.reduce((sum, room) => sum + (Number(room.capacity) || 0), 0)
  const occupiedBeds = rooms.value.reduce((sum, room) => sum + (Number(room.occupied) || 0), 0)
  return {
    buildingCount: buildings.value.length,
    totalBeds,
    occupancyRate: totalBeds ? Math.round((occupiedBeds / totalBeds) * 1000) / 10 : 0
  }
})

const form = reactive({
  id: null,
  name: '',
  type: '男生楼',
  floors: 6,
  manager: '',
  location: ''
})

const rules = {
  name: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }],
  manager: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

const loadBuildings = async () => {
  loading.value = true
  try {
    const [buildingData, roomData] = await Promise.all([getBuildingList(), getRooms()])
    buildings.value = buildingData || []
    rooms.value = roomData || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.type = '男生楼'
  form.floors = 6
  form.manager = ''
  form.location = ''
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.type = row.type
  form.floors = row.floors
  form.manager = row.manager
  form.location = row.location
  dialogVisible.value = true
}

const handleStatusChange = async (building) => {
  const nextActive = building.active
  building.statusSaving = true
  try {
    const { statusSaving, ...payload } = building
    await saveBuilding(payload)
    ElMessage.success(nextActive ? '楼栋已启用' : '楼栋已停用')
  } catch (error) {
    building.active = !nextActive
    console.error(error)
  } finally {
    building.statusSaving = false
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await saveBuilding(form)
        ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
        dialogVisible.value = false
        loadBuildings()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该楼栋吗？这将影响关联的房间数据！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteBuilding(id)
      ElMessage.success('删除成功')
      loadBuildings()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadBuildings()
})
</script>

<style scoped>
.resources-container {
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

.building-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.building-item {
  display: flex;
  padding: 24px;
  background-color: var(--bg);
  border-radius: 12px;
  transition: background-color 0.2s;
}

.building-item:hover {
  background-color: var(--line);
}

.building-icon {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}

.type-tag {
  position: absolute;
  bottom: -10px;
  font-weight: bold;
}

.bg-blue { background: var(--primary-2); color: var(--primary); border: 1px solid var(--primary-2); }
.bg-orange { background: var(--orange-2); color: var(--orange); border: 1px solid var(--orange-2); }

.building-info {
  flex: 1;
  margin-left: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.building-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.building-name {
  font-size: 18px;
  font-weight: bold;
  color: var(--text);
}

.building-no {
  font-size: 14px;
  color: var(--sub);
}

.building-manager {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--sub);
  margin-bottom: 16px;
}

.building-stats {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
}

.stat-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--sub);
}

.stat-val {
  font-size: 16px;
  font-weight: bold;
}

.text-dark { color: var(--text); }
.text-blue { color: var(--primary); }
.text-orange { color: var(--warn); }

.building-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--sub);
}

.building-actions {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
}

.status-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-label {
  font-size: 13px;
  color: var(--sub);
}

.btn-group {
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

.asset-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.asset-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg);
  border-radius: 12px;
}

.asset-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
}

.bg-light-blue { background: var(--primary-2); color: var(--primary); }
.bg-light-orange { background: var(--orange-2); color: var(--orange); }
.bg-light-cyan { background: var(--info-2); color: var(--info); }

.asset-info {
  flex: 1;
}

.asset-label {
  font-size: 13px;
  color: var(--sub);
  margin-bottom: 4px;
}

.asset-value {
  font-size: 20px;
  font-weight: bold;
  color: var(--text);
}

.asset-value span {
  font-size: 13px;
  font-weight: normal;
  color: var(--sub);
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rule-item {
  display: flex;
  gap: 12px;
}

.rule-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--primary-2);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.rule-content {
  flex: 1;
}

.rule-title {
  font-size: 14px;
  font-weight: bold;
  color: var(--text);
  margin-bottom: 4px;
}

.rule-desc {
  font-size: 13px;
  color: var(--sub);
  line-height: 1.5;
}

.tips-card {
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
  color: var(--primary);
}
</style>
