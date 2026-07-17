<template>
  <div class="patrol-manager-container">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Bot" /></el-icon>
          <div>
            <h2>AI巡查记录</h2>
            <p>上传或自动记录楼道异常情况并跟进处理</p>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="openAddDialog">
            <el-icon class="el-icon--left"><component :is="Plus" /></el-icon>上报巡查异常
          </el-button>
          <el-button @click="fetchRecords" :loading="loading">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待处理" name="PENDING"></el-tab-pane>
        <el-tab-pane label="处理中" name="PROCESSING"></el-tab-pane>
        <el-tab-pane label="已解决" name="RESOLVED"></el-tab-pane>
      </el-tabs>

      <el-table :data="filteredRecords" style="width: 100%" v-loading="loading">
        <el-table-column prop="buildingName" label="楼栋" width="100" />
        <el-table-column prop="area" label="区域" width="120" />
        <el-table-column prop="issue" label="异常情况" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发现时间" width="180">
          <template #default="scope">{{ scope.row.createTime ? scope.row.createTime.replace('T', ' ') : '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'PENDING'" type="warning" link @click="updateStatus(scope.row, 'PROCESSING')">开始处理</el-button>
            <el-button v-if="scope.row.status === 'PROCESSING'" type="success" link @click="updateStatus(scope.row, 'RESOLVED')">标记已解决</el-button>
            <span v-if="scope.row.status === 'RESOLVED'" style="color: var(--el-color-info);">已完结</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="上报巡查异常" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="楼栋" required>
          <el-input v-model="form.buildingName" placeholder="例如：1号楼" />
        </el-form-item>
        <el-form-item label="区域" required>
          <el-input v-model="form.area" placeholder="例如：3层东侧走廊" />
        </el-form-item>
        <el-form-item label="异常描述" required>
          <el-input v-model="form.issue" type="textarea" :rows="3" placeholder="填写发现的问题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdd" :loading="submitting">确认上报</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ShieldCheck, Plus } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPatrols, savePatrol } from '../../api/patrol'

const records = ref([])
const activeTab = ref('PENDING')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({ buildingName: '', area: '', issue: '' })

const filteredRecords = computed(() => {
  return records.value.filter(r => r.status === activeTab.value)
})

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getPatrols()
    records.value = res || []
  } catch (e) { console.error(e);
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  form.value = { buildingName: '', area: '', issue: '' }
  dialogVisible.value = true
}

const submitAdd = async () => {
  if (!form.value.buildingName || !form.value.area || !form.value.issue) {
    ElMessage.warning('请填写必填信息')
    return
  }
  submitting.value = true
  try {
    await savePatrol({
      buildingName: form.value.buildingName,
      area: form.value.area,
      issue: form.value.issue,
      status: 'PENDING'
    })
    ElMessage.success('打卡成功')
    dialogVisible.value = false
    fetchRecords()
  } catch (e) { console.error(e);
    ElMessage.error('上报失败')
  } finally {
    submitting.value = false
  }
}

const updateStatus = async (row, newStatus) => {
  try {
    await ElMessageBox.confirm('确认该巡查问题已解决？', '提示', { type: 'warning' })
    await savePatrol({ id: row.id, status: newStatus })
    ElMessage.success('操作成功')
    fetchRecords()
  } catch (e) { console.error(e);
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.patrol-manager-container { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02)); border: none; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px; font-size: 22px; color: var(--text); }
.hero-text p { margin: 0; color: var(--sub); font-size: 14px; }
.list-card { border-radius: 12px; border: 1px solid var(--border); }
</style>
