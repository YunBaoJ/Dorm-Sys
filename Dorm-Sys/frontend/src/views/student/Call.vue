<template>
  <LocalFeaturePage
    storage-key="student-smart-calls"
    record-type="student_call"
    title="智能通话"
    subtitle="预约宿舍事务通话并查看处理进度"
    list-title="我的通话预约"
    action-text="新建通话预约"
    :icon="Video"
    :statuses="['待呼叫', '已接通', '已结束']"
    :tips="tips"
    :manage-records="false"
    title-placeholder="例如：联系宿管确认维修时间"
    owner-placeholder="联系人或房间"
    description-placeholder="说明通话目的和希望处理的事项"
  />
</template>

<script setup>
import LocalFeaturePage from '../../components/LocalFeaturePage.vue'
import { Video } from '@lucide/vue'

const tips = [
  '预约提交后由宿管处理，学生端不能自行改变处理状态。',
  '紧急事项请直接联系宿管或值班电话。',
  '通话记录仅显示当前登录学生提交的内容。'
]
</script>
