<template>
  <div class="users-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <el-card shadow="never" class="hero-card">
          <div class="hero-content">
            <div class="hero-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="User" /></el-icon>
              <div>
                <h2>用户账户管理</h2>
                <p>权限分配 · 账号审计</p>
              </div>
            </div>
            <el-button type="primary" @click="handleAdd"><el-icon class="el-icon--left"><component :is="Plus" /></el-icon>新增用户账户</el-button>
          </div>
        </el-card>

        <!-- List Section -->
        <el-card shadow="never" class="list-card">
          <div class="list-header">
            <span class="list-title">全量账户</span>
            <div class="list-actions">
              <el-input v-model="search" placeholder="搜索用户名/昵称" prefix-icon="Search" style="width: 240px" />
              <el-select v-model="roleFilter" placeholder="所有角色" style="width: 120px; margin-left: 12px">
                <el-option label="所有角色" value="" />
                <el-option label="学生" value="student" />
                <el-option label="宿管" value="dormmanager" />
                <el-option label="管理员" value="admin" />
              </el-select>
              <el-button icon="Refresh" circle style="margin-left: 12px" @click="fetchUsers" />
            </div>
          </div>

          <div class="user-list" v-loading="loading">
            <div v-for="user in users" :key="user.id" class="user-item">
              <el-avatar :size="48" :src="user.avatar">{{ user.name?.[0] }}</el-avatar>
              
              <div class="user-info">
                <div class="user-main">
                  <span class="user-name">{{ user.name }}</span>
                  <span class="user-account">@{{ user.username }}</span>
                  <el-tag size="small" :type="user.role === 'student' ? 'primary' : 'warning'" effect="plain" round>{{ user.role }}</el-tag>
                </div>
                <div class="user-meta">
                  <span class="meta-item"><el-icon><component :is="CreditCard" /></el-icon> {{ user.id }}</span>
                  <span class="meta-item"><el-icon><component :is="MapPin" /></el-icon> {{ user.className || '无班级信息' }}</span>
                </div>
                <div class="user-meta">
                  <span class="meta-item"><el-icon><component :is="Mail" /></el-icon> {{ user.email || '未绑定邮箱' }}</span>
                  <span class="meta-item"><el-icon><component :is="Phone" /></el-icon> {{ user.phone || '未绑定手机' }}</span>
                </div>
              </div>

              <div class="user-actions">
                <div class="status-toggle">
                  <span class="status-label">{{ user.enabled ? '已启用' : '已停用' }}</span>
                  <el-switch v-model="user.enabled" @change="saveUser(user)" />
                </div>
                <div class="btn-group">
                  <el-button type="primary" link @click="handleEdit(user)">编辑</el-button>
                  <el-button type="danger" link @click="handleDelete(user.id)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
          
          <div class="pagination-wrapper">
             <el-pagination background layout="prev, pager, next" :total="50" />
          </div>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="6">
        <!-- Account Distribution -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>账号分布</span></div>
          </template>
          <div class="stat-list">
            <div class="stat-item">
              <div class="stat-icon bg-blue"><el-icon><component :is="User" /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">学生账号</div>
                <div class="stat-value">10 <span>个</span></div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon bg-orange"><el-icon><component :is="Users" /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">宿管账号</div>
                <div class="stat-value">0 <span>个</span></div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon bg-gray"><el-icon><component :is="Lock" /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">已禁用账号</div>
                <div class="stat-value">0 <span>个</span></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Management Assistant -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>管理助手</span></div>
          </template>
          <div class="assist-list">
            <div class="assist-item">
              <el-icon color="#3b82f6" :size="18" class="assist-icon"><component :is="UploadCloud" /></el-icon>
              <div>
                <div class="assist-title">批量导入</div>
                <div class="assist-desc">支持通过 Excel 模板批量创建学生和宿管账号。</div>
              </div>
            </div>
            <div class="assist-item">
              <el-icon color="#f59e0b" :size="18" class="assist-icon"><component :is="ShieldAlert" /></el-icon>
              <div>
                <div class="assist-title">安全审计</div>
                <div class="assist-desc">定期清理超过 1 年未登录的非活跃学生账号。</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Security Tips -->
        <el-card shadow="never" class="side-card bg-light-blue">
          <template #header>
            <div class="card-header">
              <span>安全提示</span>
              <el-icon><component :is="Lock" /></el-icon>
            </div>
          </template>
          <ol class="tips-list">
            <li>请确保系统管理员账号开启双重认证。</li>
            <li>宿管人员离职时应立即冻结其管理账号。</li>
            <li>初始密码建议要求用户在首次登录时修改。</li>
          </ol>
        </el-card>
      </el-col>
    </el-row>

    <!-- User Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号" required>
          <el-input v-model="form.username" placeholder="请输入学号/工号作为账号" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="宿管" value="dormmanager" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" v-if="form.role === 'student'">
          <el-input v-model="form.className" placeholder="例如：计科2201" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="可选" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="可选" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Plus, Search, RefreshCw as Refresh, CreditCard, MapPin, Mail, Phone, Users, Lock, UploadCloud, ShieldAlert } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, saveUser, deleteUser } from '../../api/user'

const search = ref('')
const roleFilter = ref('')
const users = ref([])
const loading = ref(false)

// Dialog states
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const form = ref({
  id: null,
  name: '',
  username: '',
  role: 'student',
  className: '',
  email: '',
  phone: '',
  enabled: true,
  avatar: ''
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList()
    users.value = res
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUsers()
})

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  form.value = {
    id: null,
    name: '',
    username: '',
    role: 'student',
    className: '',
    email: '',
    phone: '',
    enabled: true,
    avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${Math.random()}`
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.username) {
    ElMessage.warning('请填写必填字段')
    return
  }
  try {
    await saveUser(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？删除后无法恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUser(id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.users-container {
  max-width: 1400px;
  margin: 0 auto;
}

.hero-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.hero-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-text {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-text h2 {
  margin: 0 0 4px 0;
  font-size: 20px;
  color: var(--text);
}

.hero-text p {
  margin: 0;
  font-size: 14px;
  color: var(--sub);
}

.list-card {
  min-height: 500px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.list-title {
  font-size: 16px;
  font-weight: bold;
  color: var(--text);
}

.list-actions {
  display: flex;
  align-items: center;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: var(--bg);
  border-radius: 12px;
  transition: background-color 0.2s;
}

.user-item:hover {
  background-color: var(--line);
}

.user-info {
  flex: 1;
  margin-left: 16px;
}

.user-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}

.user-account {
  font-size: 14px;
  color: var(--sub);
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 13px;
  color: var(--sub);
  margin-bottom: 4px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.status-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-label {
  font-size: 13px;
  color: var(--sub);
}

.btn-group {
  display: flex;
  gap: 12px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.side-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.stat-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg);
  border-radius: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
}

.bg-blue { background: var(--primary-2); color: #3b82f6; }
.bg-orange { background: var(--orange-2); color: #f97316; }
.bg-gray { background: var(--line); color: var(--sub); }

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--sub);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: var(--text);
}

.stat-value span {
  font-size: 13px;
  font-weight: normal;
  color: var(--sub);
}

.assist-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.assist-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.assist-icon {
  margin-top: 2px;
}

.assist-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  margin-bottom: 4px;
}

.assist-desc {
  font-size: 13px;
  color: var(--sub);
  line-height: 1.5;
}

.bg-light-blue {
  background-color: var(--primary-2);
  border: 1px solid var(--primary-2) !important;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #3b82f6;
  line-height: 1.8;
}
</style>
