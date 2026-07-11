<template>
  <LocalFeaturePage
    storage-key="manager-profile-tasks"
    record-type="manager_profile"
    title="个人中心"
    subtitle="个人待办、值班备忘和工作记录"
    list-title="个人事项"
    action-text="新增个人事项"
    :icon="User"
    :default-records="records"
    :statuses="['待办', '进行中', '已完成']"
    :tips="tips"
    title-placeholder="例如：整理本周卫生检查表"
    owner-placeholder="关联对象"
    description-placeholder="填写个人备忘或工作说明"
  />
</template>

<script setup>
import LocalFeaturePage from '../../components/LocalFeaturePage.vue'
import { User } from '@lucide/vue'

const tips = ['个人事项只保存在本机浏览器。', '适合记录值班备忘和临时工作。', '正式业务数据仍应在对应功能页登记。']
const records = [
  { id: 1, title: '整理本周卫生检查表', owner: '张宿管', description: '汇总低于 80 分宿舍并准备复查。', status: '待办', time: '2026/3/27 11:00:00' }
]
</script>
