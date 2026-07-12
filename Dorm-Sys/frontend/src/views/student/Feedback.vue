<template>
  <LocalFeaturePage
    storage-key="student-feedback"
    record-type="student_feedback"
    title="意见反馈"
    subtitle="提交宿舍服务建议、投诉和体验反馈"
    list-title="我的反馈记录"
    action-text="提交反馈"
    :icon="Pencil"
    :statuses="['待受理', '处理中', '已回复']"
    :tips="tips"
    :manage-records="false"
    title-placeholder="例如：公共区域照明问题"
    owner-placeholder="涉及位置"
    description-placeholder="请描述问题、影响范围和建议方案"
  />
</template>

<script setup>
import LocalFeaturePage from '../../components/LocalFeaturePage.vue'
import { Pencil } from '@lucide/vue'

const tips = [
  '反馈内容越具体，处理效率越高。',
  '涉及安全问题时请同时通知宿管。',
  '学生只能查看自己的反馈，处理状态由宿管更新。'
]
</script>
