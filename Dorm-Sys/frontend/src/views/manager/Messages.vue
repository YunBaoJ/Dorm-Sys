<template>
  <div class="messages-manager-container">
    <el-card shadow="never" class="hero-card">
      <div class="hero-content">
        <div class="hero-text">
          <el-icon :size="28" color="var(--el-color-primary)"><component :is="Mail" /></el-icon>
          <div>
            <h2>消息通知发布</h2>
            <p>发布全楼公告，所有学生将会在“通知公告”中看到</p>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="openAddDialog">
            <el-icon class="el-icon--left"><component :is="Plus" /></el-icon>发布新公告
          </el-button>
          <el-button @click="fetchMessages" :loading="loading">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-table :data="messages" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="公告标题" width="200" />
        <el-table-column prop="description" label="公告内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="scope">{{ scope.row.createTime ? scope.row.createTime.replace('T', ' ') : '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-popconfirm title="确定删除该公告吗？" @confirm="deleteMessage(scope.row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="发布新公告" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="公告标题" required>
          <el-input v-model="form.title" placeholder="例如：本周五停水通知" />
        </el-form-item>
        <el-form-item label="详细内容" required>
          <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请输入通知详情内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdd" :loading="submitting">确认发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Mail, Plus } from '@lucide/vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const messages = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({ title: '', description: '' })

const fetchMessages = async () => {
  loading.value = true
  try {
    // API in businessRecord.js
    const res = await request({ url: '/businessRecord/list', method: 'get', params: { type: 'manager_messages' } })
    messages.value = res || []
  } catch (e) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  form.value = { title: '', description: '' }
  dialogVisible.value = true
}

const submitAdd = async () => {
  if (!form.value.title || !form.value.description) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submitting.value = true
  try {
    await request({
      url: '/businessRecord/save',
      method: 'post',
      data: {
        type: 'manager_messages',
        title: form.value.title,
        description: form.value.description,
        status: '已发布',
        creatorId: userStore.userInfo?.id
      }
    })
    ElMessage.success('发布成功')
    dialogVisible.value = false
    fetchMessages()
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    submitting.value = false
  }
}

const deleteMessage = async (id) => {
  try {
    await request({
      url: `/businessRecord/${id}`,
      method: 'delete'
    })
    ElMessage.success('删除成功')
    fetchMessages()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped>
.messages-manager-container { max-width: 1200px; margin: 0 auto; }
.hero-card { margin-bottom: 24px; border-radius: 12px; background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.1), rgba(var(--el-color-primary-rgb), 0.02)); border: none; }
.hero-content { display: flex; justify-content: space-between; align-items: center; }
.hero-text { display: flex; align-items: center; gap: 16px; }
.hero-text h2 { margin: 0 0 4px; font-size: 22px; color: var(--text); }
.hero-text p { margin: 0; color: var(--sub); font-size: 14px; }
.list-card { border-radius: 12px; border: 1px solid var(--border); }
</style>
