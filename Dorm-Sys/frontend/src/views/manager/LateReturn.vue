<template>
  <div class="late-manager-container">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Clock" /></el-icon>
          <div>
            <h2>晚归记录管理</h2>
            <p>登记并跟踪学生晚归情况</p>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="openAddDialog">
            <el-icon class="el-icon--left"><component :is="Plus" /></el-icon>新增晚归记录
          </el-button>
          <el-button @click="fetchRecords" :loading="loading">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待处理" name="PENDING"></el-tab-pane>
        <el-tab-pane label="已通报" name="NOTIFIED"></el-tab-pane>
        <el-tab-pane label="已归档" name="ARCHIVED"></el-tab-pane>
      </el-tabs>

      <el-table :data="filteredRecords" style="width: 100%" v-loading="loading">
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="roomNumber" label="宿舍号" width="120" />
        <el-table-column prop="reason" label="晚归原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="returnTime" label="归寝时间" width="180">
          <template #default="scope">{{ scope.row.returnTime ? scope.row.returnTime.replace('T', ' ') : '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'PENDING'" type="warning" link @click="updateStatus(scope.row, 'NOTIFIED')">通报批评</el-button>
            <el-button v-if="scope.row.status === 'PENDING' || scope.row.status === 'NOTIFIED'" type="primary" link @click="updateStatus(scope.row, 'ARCHIVED')">归档</el-button>
            <span v-if="scope.row.status === 'ARCHIVED'" style="color: var(--el-color-info);">已完结</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增晚归记录" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="学生姓名" required>
          <el-input v-model="form.studentName" placeholder="学生姓名" />
        </el-form-item>
        <el-form-item label="学号/ID">
          <el-input v-model="form.studentId" placeholder="选填" />
        </el-form-item>
        <el-form-item label="宿舍号" required>
          <el-input v-model="form.roomNumber" placeholder="例如：101" />
        </el-form-item>
        <el-form-item label="晚归原因" required>
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="填写学生晚归的原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdd" :loading="submitting">确认登记</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Clock, Plus } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const records = ref([])
const activeTab = ref('PENDING')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({ studentName: '', studentId: '', roomNumber: '', reason: '' })

const filteredRecords = computed(() => {
  return records.value.filter(r => r.status === activeTab.value)
})

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/lateReturnRecord/list', method: 'get' })
    records.value = res || []
  } catch (e) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  form.value = { studentName: '', studentId: '', roomNumber: '', reason: '' }
  dialogVisible.value = true
}

const submitAdd = async () => {
  if (!form.value.studentName || !form.value.roomNumber) {
    ElMessage.warning('请填写必填信息')
    return
  }
  submitting.value = true
  try {
    const now = new Date()
    // format as ISO without Z to match LocalDateTime roughly
    const returnTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, -1)
    
    await request({
      url: '/lateReturnRecord/add',
      method: 'post',
      data: {
        studentName: form.value.studentName,
        studentId: form.value.studentId ? parseInt(form.value.studentId) : null,
        roomNumber: form.value.roomNumber,
        reason: form.value.reason,
        status: 'PENDING',
        returnTime: returnTime
      }
    })
    ElMessage.success('登记成功')
    dialogVisible.value = false
    fetchRecords()
  } catch (e) {
    ElMessage.error('登记失败')
  } finally {
    submitting.value = false
  }
}

const updateStatus = async (row, newStatus) => {
  const actionText = newStatus === 'NOTIFIED' ? '确认通报该记录？' : '确认归档该记录？'
  try {
    await ElMessageBox.confirm(actionText, '提示', { type: 'warning' })
    await request({
      url: '/lateReturnRecord/update',
      method: 'post',
      data: { id: row.id, status: newStatus }
    })
    ElMessage.success('操作成功')
    fetchRecords()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.late-manager-container { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02)); border: none; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px; font-size: 22px; color: var(--text); }
.hero-text p { margin: 0; color: var(--sub); font-size: 14px; }
.list-card { border-radius: 12px; border: 1px solid var(--border); }
</style>
