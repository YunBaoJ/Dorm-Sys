<template>
  <LocalFeaturePage
    storage-key="manager-ai-patrol"
    record-type="manager_ai_patrol"
    title="AI巡查"
    subtitle="记录智能巡查线索、异常风险和复核结果"
    list-title="巡查线索"
    action-text="新增巡查线索"
    :icon="Bot"
    :default-records="records"
    :statuses="['待复核', '处理中', '已关闭']"
    :tips="tips"
    title-placeholder="例如：疑似违规电器"
    owner-placeholder="房间 / 区域"
    description-placeholder="填写巡查发现、风险等级和处理建议"
  />
</template>

<script setup>
import LocalFeaturePage from '../../components/LocalFeaturePage.vue'
import { Bot } from '@lucide/vue'

const tips = ['AI 巡查线索需人工复核后处理。', '安全类线索优先级最高。', '关闭前应记录复核结论。']
const records = [
  { id: 1, title: '疑似大功率电器', owner: '至善楼 203', description: '夜间用电曲线异常，建议现场复核。', status: '待复核', time: '2026/3/27 06:30:00' }
]
</script>
