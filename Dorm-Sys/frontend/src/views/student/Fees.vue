<template>
  <div class="fees-page">
    <el-card shadow="never" class="page-card">
      <div class="page-heading">
        <div><h2>费用查询</h2><p>查看当前宿舍的水电费账单与缴费状态。</p></div>
        <el-button :loading="loading" @click="fetchFees"><RefreshCw :size="16" />刷新</el-button>
      </div>
    </el-card>

    <div class="summary-grid">
      <div class="summary-item"><span>待缴账单</span><strong>{{ unpaidBills.length }}</strong></div>
      <div class="summary-item"><span>待缴金额</span><strong>¥ {{ unpaidTotal.toFixed(2) }}</strong></div>
      <div class="summary-item"><span>账单总数</span><strong>{{ bills.length }}</strong></div>
    </div>

    <el-alert
      title="线上充值与支付尚未开放"
      description="本页面目前提供账单查询。需要缴费时，请携带学生证前往宿舍服务中心办理。"
      type="info"
      :closable="false"
      show-icon
      class="payment-notice"
    />

    <el-card shadow="never" class="bill-card">
      <template #header><strong>宿舍账单</strong></template>
      <el-table v-loading="loading" :data="bills" empty-text="暂无账单" style="width: 100%">
        <el-table-column prop="period" label="账期" min-width="120" />
        <el-table-column prop="type" label="费用类型" min-width="110" />
        <el-table-column label="金额" min-width="100">
          <template #default="scope"><strong>¥ {{ Number(scope.row.amount || 0).toFixed(2) }}</strong></template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'unpaid' ? 'warning' : 'success'" effect="plain">
              {{ scope.row.status === 'unpaid' ? '待缴费' : '已缴费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="缴费时间" min-width="160">
          <template #default="scope">{{ scope.row.payTime || '--' }}</template>
        </el-table-column>
        <el-table-column label="办理方式" width="150" align="right">
          <template #default="scope">
            <span v-if="scope.row.status === 'paid'" class="settled">已结清</span>
            <el-button v-else type="primary" link @click="showOfflineGuide">查看线下指引</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshCw } from '@lucide/vue'
import { getFeeBills } from '../../api/fee'
import { getBeds } from '../../api/room'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const bills = ref([])
const loading = ref(false)

const unpaidBills = computed(() => bills.value.filter((bill) => bill.status === 'unpaid'))
const unpaidTotal = computed(() => unpaidBills.value.reduce((sum, bill) => sum + Number(bill.amount || 0), 0))

const fetchFees = async () => {
  loading.value = true
  try {
    const beds = await getBeds()
    const myBed = (beds || []).find((bed) => bed.studentId === userStore.userInfo?.id)
    if (!myBed) {
      bills.value = []
      ElMessage.warning('尚未查询到您的住宿信息')
      return
    }
    const result = await getFeeBills(myBed.roomId)
    bills.value = (result || []).map((bill) => ({
      ...bill,
      period: bill.month,
      type: bill.type === 'WATER' ? '水费' : bill.type === 'ELECTRICITY' ? '电费' : bill.type,
      status: bill.status === 'UNPAID' ? 'unpaid' : 'paid',
      payTime: bill.status === 'PAID' ? bill.updateTime?.replace('T', ' ')?.slice(0, 16) : null
    }))
  } catch (error) {
    ElMessage.error('获取账单失败')
  } finally {
    loading.value = false
  }
}

const showOfflineGuide = () => {
  ElMessage.info('请前往宿舍服务中心缴费，线上支付功能尚未开放。')
}

onMounted(fetchFees)
</script>

<style scoped>
.fees-page { max-width: 1200px; margin: 0 auto; }
.page-card, .payment-notice { margin-bottom: 20px; }
.page-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.page-heading h2 { margin: 0 0 4px; color: var(--text); font-size: 20px; }
.page-heading p { margin: 0; color: var(--sub); font-size: 14px; }
.page-heading :deep(.el-button > span) { gap: 6px; }
.summary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-bottom: 20px; }
.summary-item { display: grid; gap: 8px; border: 1px solid var(--line); border-radius: 8px; padding: 18px 20px; background: var(--surface); }
.summary-item span { color: var(--sub); font-size: 13px; }
.summary-item strong { color: var(--text); font-size: 24px; }
.settled { color: var(--sub); font-size: 13px; }
@media (max-width: 700px) {
  .summary-grid { grid-template-columns: 1fr; }
  .page-heading { align-items: flex-start; }
}
</style>
