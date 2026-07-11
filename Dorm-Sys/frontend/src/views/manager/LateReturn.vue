<template>
  <LocalFeaturePage
    storage-key="manager-late-return"
    record-type="manager_late_return"
    title="晚归登记"
    subtitle="记录学生晚归情况与后续跟进"
    list-title="晚归记录"
    action-text="登记晚归"
    :icon="Clock"
    :default-records="records"
    :statuses="['待核实', '已联系', '已归档']"
    :tips="tips"
    title-placeholder="例如：张伟晚归"
    owner-placeholder="学生姓名 / 宿舍"
    description-placeholder="填写晚归时间、原因和联系情况"
  />
</template>

<script setup>
import LocalFeaturePage from '../../components/LocalFeaturePage.vue'
import { Clock } from '@lucide/vue'

const tips = ['超过门禁时间需记录原因。', '连续晚归建议通知辅导员。', '异常未归需及时升级处理。']
const records = [
  { id: 1, title: '李明晚归', owner: '明德楼 101', description: '23:48 返回，原因：实验室项目收尾。', status: '待核实', time: '2026/3/26 23:50:00' }
]
</script>
