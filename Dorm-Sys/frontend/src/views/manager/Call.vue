<template>
  <LocalFeaturePage
    storage-key="manager-smart-calls"
    record-type="manager_call"
    title="智能通话"
    subtitle="联系学生、维修人员和访客的通话任务"
    list-title="通话任务"
    action-text="新建通话任务"
    :icon="Video"
    :default-records="records"
    :statuses="['待呼叫', '已接通', '已完成']"
    :tips="tips"
    title-placeholder="例如：联系学生确认访客"
    owner-placeholder="联系人"
    description-placeholder="填写通话目的和处理结果"
  />
</template>

<script setup>
import LocalFeaturePage from '../../components/LocalFeaturePage.vue'
import { Video } from '@lucide/vue'

const tips = ['通话任务可用于跟踪未完成事项。', '维修和访客事项建议补充房间号。', '完成后及时更新状态。']
const records = [
  { id: 1, title: '确认访客离楼', owner: '张伟 / 明德楼 101', description: '联系学生确认访客预计离开时间。', status: '待呼叫', time: '2026/3/27 14:10:00' }
]
</script>
