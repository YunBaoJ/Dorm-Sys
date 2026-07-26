<template>
  <div class="admin-page">
    <el-card shadow="never" class="page-header">
      <div><h2>公告管理</h2><p>发布面向全体学生的校园住宿通知</p></div>
      <el-button type="primary" @click="openForm()">新建公告</el-button>
    </el-card>
    <el-card shadow="never"><el-table :data="notices" v-loading="loading">
      <el-table-column prop="title" label="公告标题" min-width="220" />
      <el-table-column prop="status" label="状态" width="110"><template #default="{ row }"><el-tag :type="row.status === '已发布' ? 'success' : 'info'">{{ row.status }}</el-tag></template></el-table-column>
      <el-table-column label="发布时间" width="180"><template #default="{ row }">{{ formatTime(row.eventTime) }}</template></el-table-column>
      <el-table-column label="操作" width="190"><template #default="{ row }"><el-button link type="primary" @click="openForm(row)">编辑</el-button><el-button link type="primary" @click="toggleStatus(row)">{{ row.status === '已发布' ? '撤回' : '发布' }}</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
    </el-table></el-card>
    <el-dialog v-model="visible" :title="form.id ? '编辑公告' : '新建公告'" width="560px">
      <el-form label-width="80px"><el-form-item label="标题"><el-input v-model="form.title" maxlength="100" show-word-limit /></el-form-item><el-form-item label="内容"><el-input v-model="form.description" type="textarea" :rows="6" /></el-form-item><el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio value="草稿">草稿</el-radio><el-radio value="已发布">立即发布</el-radio></el-radio-group></el-form-item></el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBusinessRecords, saveBusinessRecord, deleteBusinessRecord } from '../../api/businessRecord'
const notices = ref([]), loading = ref(false), visible = ref(false)
const form = ref({ id: null, title: '', description: '', status: '草稿' })
const load = async () => { loading.value = true; try { notices.value = await getBusinessRecords('admin_notice') || [] } finally { loading.value = false } }
const openForm = row => { form.value = row ? { ...row } : { id: null, title: '', description: '', status: '草稿' }; visible.value = true }
const save = async () => { if (!form.value.title?.trim() || !form.value.description?.trim()) return ElMessage.warning('请填写公告标题和内容'); await saveBusinessRecord({ ...form.value, type: 'admin_notice' }); ElMessage.success('保存成功'); visible.value = false; load() }
const toggleStatus = row => { form.value = { ...row, status: row.status === '已发布' ? '草稿' : '已发布' }; save() }
const remove = async row => { await ElMessageBox.confirm(`确定删除“${row.title}”吗？`, '删除公告', { type: 'warning' }); await deleteBusinessRecord(row.id); ElMessage.success('已删除'); load() }
const formatTime = value => value ? String(value).replace('T', ' ') : '-'
onMounted(load)
</script>
<style scoped>.admin-page{max-width:1400px;margin:0 auto}.page-header{margin-bottom:20px}.page-header :deep(.el-card__body){display:flex;justify-content:space-between;align-items:center}.page-header h2{margin:0 0 6px}.page-header p{margin:0;color:var(--sub)}</style>
