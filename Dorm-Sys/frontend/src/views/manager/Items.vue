<template>
  <LocalFeaturePage
    storage-key="manager-items"
    record-type="manager_items"
    title="物品出入"
    subtitle="登记大件物品、维修工具和临时物资出入"
    list-title="出入记录"
    action-text="新增出入记录"
    :icon="Package"
    :default-records="records"
    :statuses="['待确认', '已放行', '已归还']"
    :tips="tips"
    title-placeholder="例如：维修梯借出"
    owner-placeholder="经办人 / 房间"
    description-placeholder="填写物品名称、数量、用途和归还要求"
  />
</template>

<script setup>
import LocalFeaturePage from '../../components/LocalFeaturePage.vue'
import { Package } from '@lucide/vue'

const tips = ['大件物品出楼需登记经办人。', '维修工具需明确归还时间。', '贵重物品建议拍照留档。']
const records = [
  { id: 1, title: '维修梯借出', owner: '后勤维修', description: '用于 102 室窗户检修，预计当日归还。', status: '已放行', time: '2026/3/27 10:00:00' }
]
</script>
