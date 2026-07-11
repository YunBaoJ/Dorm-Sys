<template>
  <LocalFeaturePage
    storage-key="student-smart-calls"
    title="智能通话"
    subtitle="宿舍事务语音联系与通话记录"
    list-title="通话记录"
    action-text="新建通话预约"
    :icon="Video"
    :default-records="records"
    :statuses="['待呼叫', '已接通', '已结束']"
    :tips="tips"
    title-placeholder="例如：联系宿管确认维修时间"
    owner-placeholder="联系人或房间"
    description-placeholder="说明通话目的和期望处理事项"
  />
</template>

<script setup>
import LocalFeaturePage from '../../components/LocalFeaturePage.vue'
import { Video } from '@lucide/vue'

const tips = ['通话预约会保存在本地记录中，便于后续追踪。', '紧急事项仍建议直接联系宿管或值班电话。', '通话结束后可将状态更新为已结束。']
const records = [
  { id: 1, title: '确认水龙头维修时间', owner: '明德楼 101', description: '询问维修人员预计到达时间。', status: '待呼叫', time: '2026/3/27 09:20:00' }
]
</script>
