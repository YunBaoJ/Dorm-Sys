<template>
  <div>
    <!-- Hero Section -->
    <div class="hero">
      <div class="hero-title">
        <div class="hero-icon">
          <el-icon :size="28"><component :is="WalletCards" /></el-icon>
        </div>
        <div>
          <h1>费用查询</h1>
          <p class="subtitle">透明账单 · 智慧生活</p>
        </div>
      </div>
      <button class="primary-btn" @click="ElMessage.info('充值功能开发中')">
        <el-icon :size="18"><component :is="Plus" /></el-icon> 账户充值
      </button>
    </div>

    <div class="grid">
      <!-- Left Stack -->
      <div class="left-stack">
        <!-- Wallets -->
        <div class="wallets">
          <div class="wallet">
            <span style="opacity:.8">当前余额 (元)</span>
            <strong>128.50</strong>
            <p style="font-size:12px;opacity:.7;margin-top:8px">余额以财务系统为准</p>
          </div>
          <div class="wallet warn">
            <span style="opacity:.8">待缴费用 (元)</span>
            <strong>{{ unpaidTotal.toFixed(2) }}</strong>
            <p style="font-size:12px;opacity:.7;margin-top:8px">共 {{ unpaidBills.length }} 笔未缴清</p>
          </div>
        </div>

        <!-- Chart Placeholder -->
        <div class="card">
          <div class="card-head">
            <h2>能耗支出趋势</h2>
            <div>
              <button class="ghost-btn">水费</button>
              <button class="ghost-btn active-filter" style="margin-left:8px;">电费</button>
            </div>
          </div>
          <div class="card-body">
            <div style="display:flex;align-items:flex-end;gap:12px;height:140px;padding:16px 0">
              <div v-for="(h, i) in [25, 32, 28, 35, 30, 28]" :key="i" style="flex:1;display:flex;flex-direction:column;align-items:center;gap:4px">
                <span style="font-size:11px;color:var(--sub)">{{h}}°</span>
                <div :style="`width:100%;height:${h * 3}px;background:linear-gradient(180deg,var(--primary),var(--primary-2));border-radius:4px 4px 0 0`"></div>
                <span style="font-size:11px;color:var(--sub)">{{ ["10月","11月","12月","1月","2月","3月"][i] }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Fees Table -->
        <div class="card">
          <div class="card-head">
            <h2>费用明细</h2>
            <div>
              <button class="ghost-btn active-filter">全部</button>
              <button class="ghost-btn" style="margin-left:4px;">待缴</button>
              <button class="ghost-btn" style="margin-left:4px;">已缴</button>
            </div>
          </div>
          <div class="card-body table-wrap" style="padding:0; overflow-x: auto;">
            <table style="width: 100%; border-collapse: collapse; text-align: left;">
              <thead style="background: var(--muted); color: var(--sub); font-size: 13px;">
                <tr>
                  <th style="padding: 12px 20px; font-weight: 600;">账期</th>
                  <th style="padding: 12px 20px; font-weight: 600;">类型</th>
                  <th style="padding: 12px 20px; font-weight: 600;">金额</th>
                  <th style="padding: 12px 20px; font-weight: 600;">状态</th>
                  <th style="padding: 12px 20px; font-weight: 600;">时间</th>
                  <th style="padding: 12px 20px; font-weight: 600;">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="b in bills" :key="b.id" style="border-bottom: 1px solid var(--line); font-size: 14px;">
                  <td style="padding: 16px 20px;">{{ b.period }}</td>
                  <td style="padding: 16px 20px;">{{ b.type }}</td>
                  <td style="padding: 16px 20px;">¥ {{ b.amount.toFixed(2) }}</td>
                  <td style="padding: 16px 20px;">
                    <span class="tag" :class="b.status === 'unpaid' ? 'warn' : 'ok'">
                      {{ b.status === 'unpaid' ? '待缴费' : '已缴费' }}
                    </span>
                  </td>
                  <td style="padding: 16px 20px;">{{ b.payTime || "--" }}</td>
                  <td style="padding: 16px 20px;">
                    <a v-if="b.status === 'unpaid'" class="mini-link" style="cursor: pointer;" @click="ElMessage.success('前往支付...')">去缴纳</a>
                    <span v-else class="muted">已结清</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Right Stack -->
      <div class="right-stack">
        <!-- Guidelines -->
        <div class="card">
          <div class="card-head"><h2>缴费指引</h2></div>
          <div class="card-body list">
            <div style="display:flex; gap:16px;">
              <div class="tile-icon" style="flex-shrink:0"><el-icon :size="20"><component :is="BadgeCheck" /></el-icon></div>
              <div>
                <strong style="display:block;margin-bottom:4px">确认账单</strong>
                <span style="color:var(--sub);font-size:14px">每月 5 号生成上月水电费账单，请核对能耗数据。</span>
              </div>
            </div>
            <div style="display:flex; gap:16px;">
              <div class="tile-icon" style="flex-shrink:0"><el-icon :size="20"><component :is="Wallet" /></el-icon></div>
              <div>
                <strong style="display:block;margin-bottom:4px">账户充值</strong>
                <span style="color:var(--sub);font-size:14px">支持微信/支付宝快捷支付。</span>
              </div>
            </div>
            <div style="display:flex; gap:16px;">
              <div class="tile-icon" style="flex-shrink:0"><el-icon :size="20"><component :is="MousePointerClick" /></el-icon></div>
              <div>
                <strong style="display:block;margin-bottom:4px">一键缴费</strong>
                <span style="color:var(--sub);font-size:14px">在明细表中选择待缴项，点击去缴纳即可。</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Prices -->
        <div class="card">
          <div class="card-head">
            <h2>校园价格标准</h2>
            <el-icon :size="16" color="var(--sub)"><component :is="Info" /></el-icon>
          </div>
          <div class="card-body list">
            <div style="display:flex;justify-content:space-between;padding:12px;background:var(--muted);border-radius:6px;font-size:14px">
              <span>电费标准</span>
              <strong style="color:var(--primary)">0.56 元/度</strong>
            </div>
            <div style="display:flex;justify-content:space-between;padding:12px;background:var(--muted);border-radius:6px;font-size:14px">
              <span>水费标准</span>
              <strong style="color:var(--primary)">3.20 元/吨</strong>
            </div>
            <div style="display:flex;justify-content:space-between;padding:12px;background:var(--muted);border-radius:6px;font-size:14px">
              <span>热水标准</span>
              <strong style="color:var(--primary)">0.15 元/升</strong>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { WalletCards, Plus, BadgeCheck, Wallet, MousePointerClick, Info } from '@lucide/vue'
import { ElMessage } from 'element-plus'

const bills = [
  { id: 1, period: "2025-12", type: "电费", amount: 92.30, status: "unpaid", payTime: null },
  { id: 2, period: "2025-12", type: "水费", amount: 28.50, status: "unpaid", payTime: null },
  { id: 3, period: "2025-11", type: "电费", amount: 85.50, status: "paid", payTime: "2025-12-01 10:23:11" },
  { id: 4, period: "2025-11", type: "水费", amount: 25.00, status: "paid", payTime: "2025-12-01 10:23:45" },
  { id: 5, period: "2025-10", type: "电费", amount: 78.20, status: "paid", payTime: "2025-11-02 09:12:00" },
]

const unpaidBills = computed(() => bills.filter(b => b.status === "unpaid"))
const unpaidTotal = computed(() => unpaidBills.value.reduce((sum, b) => sum + b.amount, 0))
</script>

<style scoped>
/* Prototype Wallet Specific Styles */
.wallets {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.wallet {
  padding: 24px;
  border-radius: var(--radius-lg);
  color: #fff;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  box-shadow: 0 10px 20px rgba(59, 130, 246, 0.25);
}

.wallet.warn {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  box-shadow: 0 10px 20px rgba(245, 158, 11, 0.25);
}

.wallet strong {
  display: block;
  font-size: 32px;
  line-height: 1.2;
  margin-top: 4px;
  font-variant-numeric: tabular-nums;
}

.table-wrap table tbody tr:hover {
  background: var(--muted);
}
</style>
