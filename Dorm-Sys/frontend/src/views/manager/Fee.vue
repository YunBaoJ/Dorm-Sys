<template>
  <div class="workbench-page">
    <el-card shadow="never" class="page-card">
      <div class="page-heading">
        <div>
          <h2>水电计费中心</h2>
          <p>管理宿舍水电账单，发布新账单与登记缴费状态。</p>
        </div>
        <el-button type="primary" @click="openPublishDialog">发布新账单</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-select v-model="filterBuilding" placeholder="筛选楼栋" style="width: 150px" clearable @change="fetchBills">
          <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态筛选" style="width: 120px" clearable @change="fetchBills">
          <el-option label="待缴费" value="UNPAID" />
          <el-option label="已结清" value="PAID" />
        </el-select>
        <el-button :loading="loading" @click="fetchBills">
          刷新数据
        </el-button>
      </div>

      <el-table v-loading="loading" :data="filteredBills" style="width: 100%" empty-text="暂无账单数据">
        <el-table-column prop="roomName" label="宿舍号" min-width="120" />
        <el-table-column prop="month" label="账期" min-width="100" />
        <el-table-column prop="type" label="类型" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'WATER' ? 'primary' : 'warning'" effect="light">
              {{ scope.row.type === 'WATER' ? '水费' : '电费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" min-width="100">
          <template #default="scope">
            <strong>¥ {{ Number(scope.row.amount).toFixed(2) }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'UNPAID' ? 'danger' : 'success'" effect="plain">
              {{ scope.row.status === 'UNPAID' ? '待缴费' : '已缴费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'UNPAID'" 
              type="success" link 
              @click="markAsPaid(scope.row)"
            >
              登记已缴
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发布账单弹窗 -->
    <el-dialog v-model="dialogVisible" title="发布水电账单" width="450px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="目标楼栋" required>
          <el-select v-model="form.buildingId" placeholder="选择楼栋" @change="onBuildingChange" style="width: 100%">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标房间" required>
          <el-select v-model="form.roomId" placeholder="选择房间" style="width: 100%" :disabled="!form.buildingId">
            <el-option v-for="r in currentRooms" :key="r.id" :label="r.roomNumber" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型" required>
          <el-radio-group v-model="form.type">
            <el-radio label="WATER">水费</el-radio>
            <el-radio label="ELECTRICITY">电费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="账期" required>
          <el-date-picker
            v-model="form.month"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="金额(元)" required>
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBill" :loading="submitting">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFeeBills, saveFeeBill, deleteFeeBill } from '../../api/fee'
import { getBuildings } from '../../api/building'
import { getRooms } from '../../api/room'

const loading = ref(false)
const bills = ref([])
const buildings = ref([])
const allRooms = ref([]) // To map building id to rooms

const filterBuilding = ref(null)
const filterStatus = ref('')

const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({
  buildingId: null,
  roomId: null,
  type: 'WATER',
  month: '',
  amount: 0.00
})
const currentRooms = ref([])

const filteredBills = computed(() => {
  let res = bills.value
  if (filterBuilding.value) {
    // We need to filter by building. roomName typically looks like "至善楼 101"
    const bName = buildings.value.find(b => b.id === filterBuilding.value)?.name || ''
    res = res.filter(b => b.roomName && b.roomName.startsWith(bName))
  }
  return res
})

async function fetchBills() {
  loading.value = true
  try {
    const res = await getFeeBills(null, filterStatus.value || null)
    bills.value = res || []
  } catch (e) { console.error(e);
    ElMessage.error('获取账单失败')
  } finally {
    loading.value = false
  }
}

async function fetchBaseData() {
  try {
    const bs = await getBuildings()
    buildings.value = bs || []
  } catch (e) {
    console.error('Failed to load buildings')
  }
}

function openPublishDialog() {
  form.value = { buildingId: null, roomId: null, type: 'WATER', month: '', amount: 0 }
  currentRooms.value = []
  dialogVisible.value = true
}

async function onBuildingChange(val) {
  form.value.roomId = null
  if (!val) {
    currentRooms.value = []
    return
  }
  try {
    const rooms = await getRooms(val)
    currentRooms.value = rooms || []
  } catch (e) { console.error(e);
    ElMessage.error('获取房间失败')
  }
}

async function submitBill() {
  if (!form.value.roomId || !form.value.month || form.value.amount <= 0) {
    ElMessage.warning('请完整填写账单信息')
    return
  }
  submitting.value = true
  try {
    const payload = {
      roomId: form.value.roomId,
      type: form.value.type,
      month: form.value.month,
      amount: form.value.amount,
      status: 'UNPAID'
    }
    await saveFeeBill(payload)
    ElMessage.success('账单发布成功')
    dialogVisible.value = false
    fetchBills()
  } catch (e) { console.error(e);
    ElMessage.error('发布失败')
  } finally {
    submitting.value = false
  }
}

async function markAsPaid(row) {
  try {
    await ElMessageBox.confirm(`确认该账单(¥${row.amount})已完成线下缴费？`, '提示', { type: 'warning' })
    await saveFeeBill({ ...row, status: 'PAID' })
    ElMessage.success('登记成功')
    fetchBills()
  } catch (e) { console.error(e);
    // cancelled
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该账单记录？此操作不可撤销。', '警告', { type: 'danger' })
    await deleteFeeBill(row.id)
    ElMessage.success('删除成功')
    fetchBills()
  } catch (e) { console.error(e);
    // cancelled
  }
}

onMounted(() => {
  fetchBaseData()
  fetchBills()
})
</script>

<style scoped>
.workbench-page {
  max-width: 1200px;
  margin: 0 auto;
}
.page-card {
  margin-bottom: 24px;
}
.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-heading h2 {
  margin: 0 0 8px;
  color: var(--text);
  font-size: 22px;
}
.page-heading p {
  margin: 0;
  color: var(--sub);
  font-size: 14px;
}
.toolbar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}
</style>
