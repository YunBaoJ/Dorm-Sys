<template>
  <div class="memos-page">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="ClipboardList" /></el-icon>
          <div>
            <h2>备忘录</h2>
            <p>管理待办备忘事项，支持搜索和状态筛选</p>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="openAddDialog">
            <el-icon class="el-icon--left"><component :is="Plus" /></el-icon>添加备忘录
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="list-card">
      <div class="toolbar">
        <el-input v-model="searchText" placeholder="搜索备忘录标题..." clearable class="search-input" :prefix-icon="SearchIcon">
          <template #prefix>
            <el-icon><component :is="Search" /></el-icon>
          </template>
        </el-input>
        <el-radio-group v-model="statusFilter" size="default">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="PENDING">待办</el-radio-button>
          <el-radio-button value="DONE">已办</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="filteredMemos" style="width: 100%" v-loading="loading" empty-text="暂无备忘录">
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="scope">
            <span class="memo-title-cell">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'DONE' ? 'success' : 'warning'" effect="plain" size="small" round>
              {{ scope.row.status === 'DONE' ? '已办' : '待办' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">{{ scope.row.createTime?.replace('T', ' ').substring(0, 16) || '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="toggleStatus(scope.row)">
              {{ scope.row.status === 'DONE' ? '重新待办' : '设为已办' }}
            </el-button>
            <el-button type="primary" link size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除该备忘？" @confirm="deleteMemo(scope.row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑备忘录' : '添加备忘录'" width="520px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="输入备忘录标题" maxlength="50" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="输入备忘详情，支持换行..." maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveMemo" :loading="saving">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ClipboardList, Plus, Search } from '@lucide/vue'
import { ElMessage } from 'element-plus'
import { getBusinessRecords, saveBusinessRecord, deleteBusinessRecord } from '../../api/businessRecord'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const memos = ref([])
const loading = ref(false)
const searchText = ref('')
const statusFilter = ref('')
const dialogVisible = ref(false)
const editId = ref(null)
const form = ref({ title: '', description: '' })
const saving = ref(false)

const SearchIcon = Search

const filteredMemos = computed(() => {
  let list = memos.value
  if (statusFilter.value) {
    list = list.filter(m => m.status === statusFilter.value)
  }
  if (searchText.value.trim()) {
    const q = searchText.value.trim().toLowerCase()
    list = list.filter(m => m.title?.toLowerCase().includes(q))
  }
  return list
})

const fetchMemos = async () => {
  loading.value = true
  try {
    const records = await getBusinessRecords('manager_memos')
    memos.value = (records || []).map(r => ({
      id: r.id,
      title: r.title || '备忘',
      description: r.description || '',
      status: r.status === 'DONE' ? 'DONE' : 'PENDING',
      createTime: r.createTime
    }))
  } catch (e) { console.error(e)
    ElMessage.error('获取备忘录失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  editId.value = null
  form.value = { title: '', description: '' }
  dialogVisible.value = true
}

const openEditDialog = (memo) => {
  editId.value = memo.id
  form.value = { title: memo.title, description: memo.description || '' }
  dialogVisible.value = true
}

const saveMemo = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  saving.value = true
  try {
    await saveBusinessRecord({
      id: editId.value || undefined,
      type: 'manager_memos',
      title: form.value.title.trim(),
      description: form.value.description.trim(),
      status: 'PENDING',
      creatorId: userStore.userInfo?.id
    })
    ElMessage.success(editId.value ? '已更新' : '已添加')
    dialogVisible.value = false
    fetchMemos()
  } catch (e) { console.error(e)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (memo) => {
  const newStatus = memo.status === 'DONE' ? 'PENDING' : 'DONE'
  const label = newStatus === 'DONE' ? '已办' : '待办'
  try {
    await saveBusinessRecord({
      id: memo.id,
      type: 'manager_memos',
      title: memo.title,
      description: memo.description,
      status: newStatus,
      creatorId: userStore.userInfo?.id
    })
    ElMessage.success(`已标记为${label}`)
    fetchMemos()
  } catch (e) { console.error(e)
    ElMessage.error('操作失败')
  }
}

const deleteMemo = async (id) => {
  try {
    await deleteBusinessRecord(id)
    ElMessage.success('已删除')
    fetchMemos()
  } catch (e) { console.error(e)
    ElMessage.error('删除失败')
  }
}

onMounted(fetchMemos)
</script>

<style scoped>
.memos-page { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02)); border: none; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px; font-size: 22px; color: var(--text); }
.hero-text p { margin: 0; color: var(--sub); font-size: 14px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; flex-wrap: wrap; }
.search-input { width: 280px; }
.memo-title-cell { font-weight: 600; color: var(--text); }
.list-card { border-radius: 12px; border: 1px solid var(--border); }
</style>
