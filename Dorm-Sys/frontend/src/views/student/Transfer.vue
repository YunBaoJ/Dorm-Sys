<template>
  <div class="transfer-container">
    <!-- Hero Header -->
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Repeat2" /></el-icon>
          <div>
            <h2>调宿申请</h2>
            <p>提交申请 · 等待审批</p>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="24">
      <!-- Left: Form -->
      <el-col :span="10">
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-header"><span>提交调宿申请</span></div>
          </template>
          <el-form :model="form" label-width="80px">
            <el-form-item label="调宿原因" required>
              <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请详细描述调宿原因" />
            </el-form-item>
            <el-form-item label="目标房间">
              <el-select v-model="form.targetRoomId" placeholder="可选，也可由宿管分配" style="width: 100%" clearable>
                <el-option v-for="r in availableRooms" :key="r.id" :label="`${r.buildingName} ${r.roomNumber}`" :value="r.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- Right: My Requests -->
      <el-col :span="14">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header">
              <span>我的调宿记录</span>
              <el-button size="small" @click="fetchMyTransfers">刷新</el-button>
            </div>
          </template>
          <div v-loading="loading">
            <el-empty v-if="myTransfers.length === 0" description="暂无调宿记录" />
            <div v-for="t in myTransfers" :key="t.id" class="transfer-item">
              <div class="transfer-info">
                <div class="transfer-main">
                  <el-tag size="small" :type="statusTypeMap[t.status]" effect="plain" round>{{ statusMap[t.status] }}</el-tag>
                </div>
                <div class="transfer-route">
                  <span class="route-from">{{ t.currentBedName || '当前床位' }}</span>
                  <span class="route-arrow">→</span>
                  <span class="route-to">{{ t.targetRoomName || '由宿管分配' }}</span>
                </div>
                <div class="transfer-reason">原因：{{ t.reason }}</div>
                <div class="transfer-time">{{ t.createTime ? t.createTime.replace('T', ' ').substring(0, 16) : '' }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Repeat2 } from '@lucide/vue'
import { getTransfers, saveTransfer } from '../../api/transfer'
import { getRooms } from '../../api/room'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const userStore = useUserStore()
const form = ref({ reason: '', targetRoomId: null })
const myTransfers = ref([])
const availableRooms = ref([])
const myCurrentBedId = ref(null)
const loading = ref(false)
const submitting = ref(false)

const statusMap = { 'PENDING': '待审批', 'APPROVED': '已通过', 'REJECTED': '已驳回' }
const statusTypeMap = { 'PENDING': 'warning', 'APPROVED': 'success', 'REJECTED': 'danger' }

const fetchMyTransfers = async () => {
  loading.value = true
  try {
    const res = await getTransfers(userStore.userInfo?.id)
    myTransfers.value = res || []
  } catch (e) { console.error(e);
    ElMessage.error('获取调宿记录失败')
  } finally {
    loading.value = false
  }
}

const fetchRoomsAndBeds = async () => {
  try {
    const [roomsRes, summary] = await Promise.all([
      getRooms(),
      request({ url: '/dashboard/dorm', method: 'get' })
    ])
    availableRooms.value = (roomsRes || []).filter(r => r.status !== 'FULL')
    myCurrentBedId.value = summary?.myBed?.id || null
  } catch (e) {
    console.error('Failed to fetch rooms/beds', e)
  }
}

onMounted(() => {
  fetchMyTransfers()
  fetchRoomsAndBeds()
})

const handleSubmit = async () => {
  if (!form.value.reason) {
    ElMessage.warning('请填写调宿原因')
    return
  }
  if (!myCurrentBedId.value) {
    ElMessage.warning('未找到您当前的床位信息，无法提交')
    return
  }
  submitting.value = true
  try {
    await saveTransfer({
      studentId: userStore.userInfo?.id,
      currentBedId: myCurrentBedId.value,
      targetRoomId: form.value.targetRoomId || null,
      reason: form.value.reason,
      status: 'PENDING'
    })
    ElMessage.success('调宿申请提交成功！')
    form.value = { reason: '', targetRoomId: null }
    fetchMyTransfers()
  } catch (e) { console.error(e);
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.transfer-container { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px 0; font-size: 20px; color: var(--text); }
.hero-text p { margin: 0; font-size: 14px; color: var(--sub); }
.form-card, .list-card { border-radius: 12px; min-height: 400px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.transfer-item {
  padding: 16px; background: var(--bg); border-radius: 10px; margin-bottom: 12px;
}
.transfer-main { margin-bottom: 8px; }
.transfer-route { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; font-size: 14px; }
.route-from { color: var(--sub); }
.route-arrow { color: var(--el-color-primary); font-weight: bold; }
.route-to { color: var(--text); font-weight: 600; }
.transfer-reason { font-size: 14px; color: var(--sub); margin-bottom: 4px; }
.transfer-time { font-size: 12px; color: var(--sub); }
</style>
