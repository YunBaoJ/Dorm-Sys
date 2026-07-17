<template>
  <div class="call-manager-container">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Video" /></el-icon>
          <div>
            <h2>智能通话管理</h2>
            <p>处理学生的通话预约</p>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="fetchCalls" :loading="loading">
            <el-icon class="el-icon--left"><component :is="RefreshCw" /></el-icon>刷新列表
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待接通" name="PENDING"></el-tab-pane>
        <el-tab-pane label="已接通" name="ACCEPTED"></el-tab-pane>
        <el-tab-pane label="已结束" name="FINISHED"></el-tab-pane>
      </el-tabs>

      <el-table :data="filteredCalls" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="topic" label="通话事由" min-width="200" />
        <el-table-column prop="targetPerson" label="指定联系人" width="120" />
        <el-table-column prop="studentId" label="发起学生ID" width="120" />
        <el-table-column prop="createTime" label="预约时间" width="180">
          <template #default="scope">{{ scope.row.createTime ? scope.row.createTime.replace('T', ' ') : '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'PENDING'" type="primary" link @click="updateStatus(scope.row, 'ACCEPTED')">接通</el-button>
            <el-button v-if="scope.row.status === 'ACCEPTED'" type="success" link @click="updateStatus(scope.row, 'FINISHED')">结束通话</el-button>
            <span v-if="scope.row.status === 'FINISHED'" style="color: var(--el-color-info);">已完结</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Video, RefreshCw } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCalls, saveCall } from '../../api/call'

const calls = ref([])
const activeTab = ref('PENDING')
const loading = ref(false)

const filteredCalls = computed(() => {
  return calls.value.filter(c => c.status === activeTab.value)
})

const fetchCalls = async () => {
  loading.value = true
  try {
    const res = await getCalls()
    calls.value = res || []
  } catch (e) { console.error(e);
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const updateStatus = async (row, newStatus) => {
  const actionText = newStatus === 'ACCEPTED' ? '确认接通该通话？' : '确认结束该通话？'
  try {
    await ElMessageBox.confirm(actionText, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    
    await saveCall({
      id: row.id,
      status: newStatus
    })
    ElMessage.success('操作成功')
    fetchCalls()
  } catch (e) { console.error(e);
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchCalls()
})
</script>

<style scoped>
.call-manager-container { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02)); border: none; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px; font-size: 22px; color: var(--text); }
.hero-text p { margin: 0; color: var(--sub); font-size: 14px; }
.list-card { border-radius: 12px; border: 1px solid var(--border); }
</style>
