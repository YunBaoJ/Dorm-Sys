<template>
  <div class="feature-page">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-title">
          <el-icon :size="28" color="var(--el-color-primary)">
            <component :is="icon" />
          </el-icon>
          <div>
            <h2>{{ title }}</h2>
            <p>{{ subtitle }}</p>
          </div>
        </div>
        <el-button type="primary" @click="openCreate">
          <el-icon class="el-icon--left"><component :is="Plus" /></el-icon>
          {{ actionText }}
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="24">
      <el-col :span="16">
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <div>
              <span class="list-title">{{ listTitle }}</span>
              <el-tag size="small" effect="plain" style="margin-left: 8px">{{ records.length }}</el-tag>
            </div>
            <div class="list-actions">
              <el-input v-model="keyword" clearable placeholder="搜索记录" style="width: 220px" />
              <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 140px">
                <el-option v-for="status in statuses" :key="status" :label="status" :value="status" />
              </el-select>
            </div>
          </div>

          <el-empty v-if="filteredRecords.length === 0" description="暂无记录" />
          <div v-else class="record-list">
            <div v-for="record in filteredRecords" :key="record.id" class="record-item">
              <div class="record-icon">
                <el-icon><component :is="icon" /></el-icon>
              </div>
              <div class="record-main">
                <div class="record-head">
                  <strong>{{ record.title }}</strong>
                  <el-tag size="small" :type="statusType(record.status)" effect="plain">{{ record.status }}</el-tag>
                </div>
                <div class="record-desc">{{ record.description }}</div>
                <div class="record-meta">
                  <span>{{ record.owner }}</span>
                  <span>{{ record.time }}</span>
                </div>
              </div>
              <div class="record-actions">
                <el-button type="primary" link @click="advance(record)">{{ nextAction(record.status) }}</el-button>
                <el-button type="danger" link @click="remove(record.id)">删除</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="side-card">
          <template #header>
            <span>流程提示</span>
          </template>
          <ol class="tips-list">
            <li v-for="tip in tips" :key="tip">{{ tip }}</li>
          </ol>
        </el-card>

        <el-card shadow="never" class="side-card">
          <template #header>
            <span>状态统计</span>
          </template>
          <div class="status-list">
            <div v-for="status in statuses" :key="status" class="status-row">
              <span>{{ status }}</span>
              <strong>{{ countByStatus(status) }}</strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="actionText" width="520px">
      <el-form :model="form" label-width="88px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" :placeholder="titlePlaceholder" />
        </el-form-item>
        <el-form-item label="对象">
          <el-input v-model="form.owner" :placeholder="ownerPlaceholder" />
        </el-form-item>
        <el-form-item label="说明" required>
          <el-input v-model="form.description" type="textarea" :rows="4" :placeholder="descriptionPlaceholder" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="status in statuses" :key="status" :label="status" :value="status" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@lucide/vue'

const props = defineProps({
  storageKey: { type: String, required: true },
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  listTitle: { type: String, default: '记录列表' },
  actionText: { type: String, default: '新增记录' },
  icon: { type: [Object, Function], required: true },
  defaultRecords: { type: Array, default: () => [] },
  statuses: { type: Array, default: () => ['待处理', '处理中', '已完成'] },
  tips: { type: Array, default: () => [] },
  titlePlaceholder: { type: String, default: '请输入标题' },
  ownerPlaceholder: { type: String, default: '请输入姓名、房间或对象' },
  descriptionPlaceholder: { type: String, default: '请输入详细说明' }
})

const records = ref([])
const keyword = ref('')
const statusFilter = ref('')
const dialogVisible = ref(false)
const form = reactive({ title: '', owner: '', description: '', status: props.statuses[0] })

onMounted(() => {
  const stored = localStorage.getItem(props.storageKey)
  records.value = stored ? JSON.parse(stored) : props.defaultRecords
  persist()
})

const filteredRecords = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  return records.value.filter((record) => {
    const matchesKeyword = !key || [record.title, record.owner, record.description].some((value) => String(value || '').toLowerCase().includes(key))
    const matchesStatus = !statusFilter.value || record.status === statusFilter.value
    return matchesKeyword && matchesStatus
  })
})

const persist = () => {
  localStorage.setItem(props.storageKey, JSON.stringify(records.value))
}

const openCreate = () => {
  Object.assign(form, { title: '', owner: '', description: '', status: props.statuses[0] })
  dialogVisible.value = true
}

const submit = () => {
  if (!form.title || !form.description) {
    ElMessage.warning('请填写标题和说明')
    return
  }
  records.value.unshift({
    id: Date.now(),
    title: form.title,
    owner: form.owner || '未指定',
    description: form.description,
    status: form.status,
    time: new Date().toLocaleString()
  })
  persist()
  dialogVisible.value = false
  ElMessage.success('保存成功')
}

const remove = async (id) => {
  await ElMessageBox.confirm('确定删除这条记录吗？', '提示', { type: 'warning' })
  records.value = records.value.filter((record) => record.id !== id)
  persist()
}

const advance = (record) => {
  const index = props.statuses.indexOf(record.status)
  record.status = props.statuses[Math.min(index + 1, props.statuses.length - 1)]
  persist()
}

const nextAction = (status) => {
  const index = props.statuses.indexOf(status)
  return index >= props.statuses.length - 1 ? '已完成' : `转为${props.statuses[index + 1]}`
}

const countByStatus = (status) => records.value.filter((record) => record.status === status).length

const statusType = (status) => {
  const index = props.statuses.indexOf(status)
  if (index === 0) return 'warning'
  if (index === props.statuses.length - 1) return 'success'
  return 'primary'
}
</script>

<style scoped>
.feature-page {
  max-width: 1400px;
  margin: 0 auto;
}

.hero-card,
.list-card,
.side-card {
  border-radius: 12px;
}

.hero-card {
  margin-bottom: 24px;
}

.hero-content,
.hero-title,
.list-header,
.record-head,
.record-meta,
.record-actions,
.status-row {
  display: flex;
  align-items: center;
}

.hero-content,
.list-header,
.record-head,
.status-row {
  justify-content: space-between;
}

.hero-title {
  gap: 16px;
}

.hero-title h2 {
  margin: 0 0 4px;
  color: var(--text);
  font-size: 20px;
}

.hero-title p {
  margin: 0;
  color: var(--sub);
  font-size: 14px;
}

.list-header {
  padding-bottom: 16px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--line);
}

.list-title {
  font-weight: 700;
  color: var(--text);
}

.list-actions {
  display: flex;
  gap: 12px;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.record-item {
  display: flex;
  gap: 16px;
  padding: 18px;
  border-radius: 12px;
  background: var(--bg);
}

.record-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  flex: none;
  background: var(--primary-2);
  color: var(--primary);
}

.record-main {
  flex: 1;
}

.record-head {
  margin-bottom: 8px;
}

.record-desc {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 10px;
}

.record-meta {
  gap: 18px;
  color: var(--sub);
  font-size: 13px;
}

.record-actions {
  flex-direction: column;
  align-items: flex-end;
}

.side-card {
  margin-bottom: 24px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: var(--text-secondary);
  line-height: 2;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-row {
  padding: 14px 16px;
  border-radius: 10px;
  background: var(--bg);
}
</style>
