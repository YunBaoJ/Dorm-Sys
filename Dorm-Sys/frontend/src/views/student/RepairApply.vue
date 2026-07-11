<template>
  <div class="repair-apply-container">
    <!-- Hero Header -->
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Settings" /></el-icon>
          <div>
            <h2>报修申请</h2>
            <p>提交故障 · 跟踪进度</p>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="24">
      <!-- Left: Form -->
      <el-col :span="10">
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-header"><span>提交新工单</span></div>
          </template>
          <el-form :model="form" label-width="80px">
            <el-form-item label="故障类型" required>
              <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
                <el-option label="水管" value="水管" />
                <el-option label="电器" value="电器" />
                <el-option label="门窗" value="门窗" />
                <el-option label="网络" value="网络" />
              </el-select>
            </el-form-item>
            <el-form-item label="问题描述" required>
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请描述具体的故障情况" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">提交报修</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- Right: My Requests -->
      <el-col :span="14">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header">
              <span>我的报修记录</span>
              <el-button size="small" @click="fetchMyRepairs">刷新</el-button>
            </div>
          </template>
          <div v-loading="loading">
            <el-empty v-if="myRepairs.length === 0" description="暂无报修记录" />
            <div v-for="r in myRepairs" :key="r.id" class="repair-item">
              <div class="repair-badge">{{ iconMap[r.type] || '修' }}</div>
              <div class="repair-info">
                <div class="repair-main">
                  <span class="repair-type">{{ r.type }}</span>
                  <el-tag size="small" :type="statusTypeMap[r.status]" effect="plain" round>{{ statusMap[r.status] }}</el-tag>
                </div>
                <div class="repair-desc">{{ r.description }}</div>
                <div class="repair-time">{{ r.createTime ? r.createTime.replace('T', ' ').substring(0, 16) : '' }}</div>
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
import { Settings } from '@lucide/vue'
import { getRepairs, saveRepair } from '../../api/repair'
import { getBeds } from '../../api/room'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const form = ref({ type: '', description: '' })
const myRepairs = ref([])
const currentRoomId = ref(null)
const loading = ref(false)
const submitting = ref(false)

const statusMap = { 'PENDING': '待处理', 'PROCESSING': '处理中', 'COMPLETED': '已完成' }
const statusTypeMap = { 'PENDING': 'warning', 'PROCESSING': 'primary', 'COMPLETED': 'success' }
const iconMap = { '水管': '水', '电器': '电', '门窗': '门', '网络': '网' }

const fetchMyRepairs = async () => {
  loading.value = true
  try {
    const res = await getRepairs(userStore.userInfo?.id)
    myRepairs.value = res || []
  } catch (e) {
    ElMessage.error('获取报修记录失败')
  } finally {
    loading.value = false
  }
}

const fetchCurrentRoom = async () => {
  const beds = await getBeds()
  const currentBed = (beds || []).find((bed) => bed.studentId === userStore.userInfo?.id)
  currentRoomId.value = currentBed?.roomId || null
}

onMounted(async () => {
  await fetchCurrentRoom()
  fetchMyRepairs()
})

const handleSubmit = async () => {
  if (!form.value.type || !form.value.description) {
    ElMessage.warning('请填写故障类型和描述')
    return
  }
  submitting.value = true
  try {
    await saveRepair({
      submitterId: userStore.userInfo?.id,
      roomId: currentRoomId.value,
      type: form.value.type,
      description: form.value.description,
      status: 'PENDING'
    })
    ElMessage.success('报修提交成功！')
    form.value = { type: '', description: '' }
    fetchMyRepairs()
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.repair-apply-container {
  max-width: 1200px;
  margin: 0 auto;
}
.hero-card { margin-bottom: 24px; border-radius: 12px; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px 0; font-size: 20px; color: var(--text); }
.hero-text p { margin: 0; font-size: 14px; color: var(--sub); }
.form-card, .list-card { border-radius: 12px; min-height: 400px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.repair-item {
  display: flex; align-items: flex-start; gap: 16px;
  padding: 16px; background: var(--bg); border-radius: 10px; margin-bottom: 12px;
}
.repair-badge {
  width: 44px; height: 44px; border-radius: 12px; background: #3b82f6;
  color: white; font-weight: bold; font-size: 18px;
  display: flex; justify-content: center; align-items: center; flex-shrink: 0;
}
.repair-info { flex: 1; }
.repair-main { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.repair-type { font-weight: 600; color: var(--text); }
.repair-desc { font-size: 14px; color: var(--sub); margin-bottom: 4px; }
.repair-time { font-size: 12px; color: var(--sub); }
</style>
