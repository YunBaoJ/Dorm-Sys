<template>
  <div class="items-manager-container">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Package" /></el-icon>
          <div>
            <h2>物品出入登记</h2>
            <p>登记大件物品、维修工具和临时物资出入</p>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="openAddDialog">
            <el-icon class="el-icon--left"><component :is="Plus" /></el-icon>新增出入记录
          </el-button>
          <el-button @click="fetchItems" :loading="loading">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待确认" name="PENDING"></el-tab-pane>
        <el-tab-pane label="已放行" name="RELEASED"></el-tab-pane>
        <el-tab-pane label="已归还" name="RETURNED"></el-tab-pane>
      </el-tabs>

      <el-table :data="filteredItems" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="出入事由/物品名" width="200" />
        <el-table-column prop="owner" label="经办人/房间" width="120" />
        <el-table-column prop="description" label="详细说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="登记时间" width="180">
          <template #default="scope">{{ scope.row.createTime ? scope.row.createTime.replace('T', ' ') : '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'PENDING'" type="primary" link @click="updateStatus(scope.row, 'RELEASED')">放行</el-button>
            <el-button v-if="scope.row.status === 'RELEASED'" type="success" link @click="updateStatus(scope.row, 'RETURNED')">确认归还</el-button>
            <span v-if="scope.row.status === 'RETURNED'" style="color: var(--el-color-info);">已完结</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增出入记录" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="物品名称" required>
          <el-input v-model="form.title" placeholder="例如：维修梯借出" />
        </el-form-item>
        <el-form-item label="经办人/房间" required>
          <el-input v-model="form.owner" placeholder="例如：后勤维修 张三" />
        </el-form-item>
        <el-form-item label="详细说明" required>
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="填写物品数量、用途和归还要求" />
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
import { Package, Plus } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const items = ref([])
const activeTab = ref('PENDING')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({ title: '', owner: '', description: '' })

const filteredItems = computed(() => {
  return items.value.filter(i => i.status === activeTab.value)
})

const fetchItems = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/itemRecord/list', method: 'get' })
    items.value = res || []
  } catch (e) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  form.value = { title: '', owner: '', description: '' }
  dialogVisible.value = true
}

const submitAdd = async () => {
  if (!form.value.title || !form.value.owner) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submitting.value = true
  try {
    await request({
      url: '/itemRecord/add',
      method: 'post',
      data: {
        title: form.value.title,
        owner: form.value.owner,
        description: form.value.description,
        status: 'PENDING'
      }
    })
    ElMessage.success('登记成功')
    dialogVisible.value = false
    fetchItems()
  } catch (e) {
    ElMessage.error('登记失败')
  } finally {
    submitting.value = false
  }
}

const updateStatus = async (row, newStatus) => {
  const actionText = newStatus === 'RELEASED' ? '确认放行该物品？' : '确认物品已归还？'
  try {
    await ElMessageBox.confirm(actionText, '提示', { type: 'warning' })
    await request({
      url: '/itemRecord/update',
      method: 'post',
      data: { id: row.id, status: newStatus }
    })
    ElMessage.success('操作成功')
    fetchItems()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchItems()
})
</script>

<style scoped>
.items-manager-container { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02)); border: none; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px; font-size: 22px; color: var(--text); }
.hero-text p { margin: 0; color: var(--sub); font-size: 14px; }
.list-card { border-radius: 12px; border: 1px solid var(--border); }
</style>
