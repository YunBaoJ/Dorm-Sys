<template>
  <LocalFeaturePage
    storage-key="manager-messages"
    title="消息通知"
    subtitle="面向楼栋、房间或学生发布管理通知"
    list-title="通知记录"
    action-text="发布通知"
    :icon="Mail"
    :default-records="records"
    :statuses="['草稿', '已发布', '已撤回']"
    :tips="tips"
    title-placeholder="例如：周四卫生检查提醒"
    owner-placeholder="通知范围"
    description-placeholder="填写通知正文"
  />
</template>

<script setup>
import LocalFeaturePage from '../../components/LocalFeaturePage.vue'
import { Mail } from '@lucide/vue'

const tips = ['重要通知建议提前一天发布。', '通知范围可写楼栋、楼层或具体房间。', '撤回通知仍会保留操作记录。']
const records = [
  { id: 1, title: '周四卫生检查提醒', owner: '明德楼全体', description: '请各宿舍保持地面、桌面和床铺整洁。', status: '已发布', time: '2026/3/27 09:00:00' }
]
</script>
