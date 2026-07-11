<template>
  <LocalFeaturePage
    storage-key="student-feedback"
    title="意见反馈"
    subtitle="提交宿舍服务建议、投诉和体验反馈"
    list-title="反馈记录"
    action-text="提交反馈"
    :icon="Pencil"
    :default-records="records"
    :statuses="['待受理', '处理中', '已回复']"
    :tips="tips"
    title-placeholder="例如：洗衣房设备建议"
    owner-placeholder="涉及位置"
    description-placeholder="请描述问题、影响范围和建议方案"
  />
</template>

<script setup>
import LocalFeaturePage from '../../components/LocalFeaturePage.vue'
import { Pencil } from '@lucide/vue'

const tips = ['反馈内容越具体，处理效率越高。', '涉及安全问题请同时通知宿管。', '处理完成后可保留记录用于复盘。']
const records = [
  { id: 1, title: '公共区域照明较暗', owner: '明德楼 1 层', description: '晚间楼梯口照明不足，建议增加巡检。', status: '待受理', time: '2026/3/27 08:30:00' }
]
</script>
