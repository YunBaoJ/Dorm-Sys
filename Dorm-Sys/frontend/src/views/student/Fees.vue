<template>
  <div class="fees-container">
    <el-row :gutter="24">
      <!-- Left Column -->
      <el-col :span="18">
        <!-- Hero Header -->
        <el-card shadow="never" class="hero-card">
          <div class="hero-content">
            <div class="hero-text">
              <el-icon :size="28" color="var(--el-color-primary)"><component :is="Wallet" /></el-icon>
              <div>
                <h2>费用查询</h2>
                <p>透明账单 · 智慧生活</p>
              </div>
            </div>
            <el-button type="primary"><el-icon class="el-icon--left"><component :is="Plus" /></el-icon>账户充值</el-button>
          </div>
        </el-card>

        <!-- Balance Cards -->
        <el-row :gutter="20" class="balance-cards">
          <el-col :span="12">
            <div class="bal-card bal-blue">
              <div class="bal-content">
                <div class="bal-label">当前余额 (元)</div>
                <div class="bal-value">0.00</div>
                <div class="bal-sub">余额以财务系统为准</div>
              </div>
              <el-icon class="bg-icon"><component :is="WalletCards" /></el-icon>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="bal-card bal-orange">
              <div class="bal-content">
                <div class="bal-label">待缴费用 (元)</div>
                <div class="bal-value">120.80</div>
                <div class="bal-sub">共 2 笔未缴清</div>
              </div>
              <el-icon class="bg-icon"><component :is="AlertCircle" /></el-icon>
            </div>
          </el-col>
        </el-row>

        <!-- Energy Chart -->
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">能耗支出趋势</span>
              <el-radio-group v-model="chartType" size="small">
                <el-radio-button label="water">水费</el-radio-button>
                <el-radio-button label="elec">电费</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-wrapper">
            <!-- Simulated Chart Area -->
            <div class="sim-chart">
              <div class="y-axis">
                <span>100</span>
                <span>75</span>
                <span>50</span>
                <span>25</span>
                <span>0</span>
              </div>
              <div class="chart-content">
                <div class="chart-line"></div>
                <div class="x-axis">
                  <span>10月</span>
                  <span>11月</span>
                  <span>12月</span>
                  <span>1月</span>
                  <span>2月</span>
                  <span>3月</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Bill Details -->
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">费用明细</span>
              <el-radio-group v-model="filterType" size="small">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="unpaid">待缴</el-radio-button>
                <el-radio-button label="paid">已缴</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-table :data="bills" style="width: 100%">
            <el-table-column prop="period" label="账期" width="120" />
            <el-table-column label="类型" width="120">
              <template #default="scope">
                <div class="type-cell">
                  <el-icon :color="scope.row.type === '电费' ? '#f59e0b' : '#3b82f6'">
                    <component :is="scope.row.type === '电费' ? 'Zap' : 'Droplets'" />
                  </el-icon>
                  {{ scope.row.type }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="金额">
              <template #default="scope">
                <strong>¥ {{ scope.row.amount.toFixed(2) }}</strong>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.status === '待缴费' ? 'warning' : 'success'" effect="plain" round>
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="时间" />
            <el-table-column label="操作" width="100" align="right">
              <template #default="scope">
                <el-button v-if="scope.row.status === '待缴费'" type="primary" link>去缴纳</el-button>
                <span v-else class="text-gray">已结清</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="6">
        <!-- Guidelines -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>缴费指引</span></div>
          </template>
          <div class="guide-list">
            <div class="guide-item">
              <div class="guide-num">1</div>
              <div class="guide-info">
                <div class="guide-title">确认账单</div>
                <div class="guide-desc">每月 5 号生成上月水电费账单，请核对能耗数据。</div>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-num">2</div>
              <div class="guide-info">
                <div class="guide-title">账户充值</div>
                <div class="guide-desc">点击“账户充值”，支持微信/支付宝快捷支付。</div>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-num">3</div>
              <div class="guide-info">
                <div class="guide-title">一键缴费</div>
                <div class="guide-desc">在明细列表中选择待缴项，点击“去缴纳”即可。</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- FAQ -->
        <el-card shadow="never" class="side-card">
          <template #header>
            <div class="card-header"><span>常见问题</span></div>
          </template>
          <div class="faq-list">
            <div class="faq-item">
              <span>余额不足会停电吗？</span>
              <el-icon><component :is="ChevronRight" /></el-icon>
            </div>
            <div class="faq-item">
              <span>如何查询水表度数？</span>
              <el-icon><component :is="ChevronRight" /></el-icon>
            </div>
            <div class="faq-item">
              <span>缴费成功但没来电？</span>
              <el-icon><component :is="ChevronRight" /></el-icon>
            </div>
          </div>
        </el-card>

        <!-- Pricing -->
        <el-card shadow="never" class="side-card pricing-card">
          <template #header>
            <div class="card-header">
              <span>校园价格标准</span>
              <el-icon><component :is="Info" /></el-icon>
            </div>
          </template>
          <div class="pricing-list">
            <div class="price-row">
              <span>电费标准</span>
              <span class="price-val">0.56 元/度</span>
            </div>
            <div class="price-row">
              <span>水费标准</span>
              <span class="price-val">3.20 元/吨</span>
            </div>
            <div class="price-row">
              <span>热水标准</span>
              <span class="price-val">0.15 元/升</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Wallet, Plus, WalletCards, AlertCircle, Zap, Droplets, ChevronRight, Info } from '@lucide/vue'

const chartType = ref('elec')
const filterType = ref('all')

const bills = [
  { period: '2025-12', type: '电费', amount: 92.30, status: '待缴费', time: '--' },
  { period: '2025-12', type: '水费', amount: 28.50, status: '待缴费', time: '--' },
  { period: '2025-11', type: '电费', amount: 85.50, status: '已缴费', time: '2025/12/05 10:00' },
  { period: '2025-11', type: '水费', amount: 25.00, status: '已缴费', time: '2025/12/05 10:05' },
]
</script>

<style scoped>
.fees-container {
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
  color: #1f2937;
}

.hero-text p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.balance-cards {
  margin-bottom: 24px;
}

.bal-card {
  position: relative;
  border-radius: 12px;
  padding: 32px;
  overflow: hidden;
  color: white;
}

.bal-blue {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
}

.bal-orange {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
}

.bal-content {
  position: relative;
  z-index: 2;
}

.bal-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.bal-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
  line-height: 1;
}

.bal-sub {
  font-size: 13px;
  opacity: 0.8;
}

.bg-icon {
  position: absolute;
  right: -20px;
  bottom: -20px;
  font-size: 140px;
  opacity: 0.15;
  transform: rotate(-15deg);
}

.chart-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.chart-wrapper {
  height: 300px;
  padding: 20px 0;
}

.sim-chart {
  display: flex;
  height: 100%;
}

.y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #94a3b8;
  font-size: 12px;
  padding-right: 16px;
  padding-bottom: 30px;
}

.chart-content {
  flex: 1;
  position: relative;
  border-left: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
}

.x-axis {
  position: absolute;
  bottom: -30px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  color: #94a3b8;
  font-size: 12px;
}

.chart-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  top: 0;
  /* Placeholder for actual chart */
  background: linear-gradient(to top, rgba(59, 130, 246, 0.1) 0%, transparent 100%);
}

.table-card {
  margin-bottom: 24px;
}

.type-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.text-gray {
  color: #94a3b8;
  font-size: 13px;
}

.side-card {
  margin-bottom: 24px;
}

.guide-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.guide-item {
  display: flex;
  gap: 16px;
}

.guide-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #eff6ff;
  color: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.guide-info {
  flex: 1;
}

.guide-title {
  font-size: 14px;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 4px;
}

.guide-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.faq-list {
  display: flex;
  flex-direction: column;
}

.faq-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
  font-size: 14px;
  cursor: pointer;
}

.faq-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.faq-item:hover {
  color: #3b82f6;
}

.pricing-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #334155;
}

.price-val {
  color: #3b82f6;
  font-weight: 500;
}
</style>
