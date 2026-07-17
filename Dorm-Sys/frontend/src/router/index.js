import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  { path: '/login', component: () => import('../views/Login.vue') },
  // Student Routes
  { path: '/student/desk', component: () => import('../views/student/Desk.vue') },
  { path: '/student/dorm', component: () => import('../views/student/Dorm.vue') },
  { path: '/student/repair', component: () => import('../views/student/RepairApply.vue') },
  { path: '/student/fees', component: () => import('../views/student/Fees.vue') },
  { path: '/student/notice', component: () => import('../views/student/Notice.vue') },
  { path: '/student/visitor', component: () => import('../views/student/Visitor.vue') },
  { path: '/student/transfer', component: () => import('../views/student/Transfer.vue') },
  { path: '/student/ai', component: () => import('../views/student/AI.vue') },
  { path: '/student/call', component: () => import('../views/student/Call.vue') },
  { path: '/student/feedback', component: () => import('../views/student/Feedback.vue') },
  { path: '/student/settings', component: () => import('../views/student/Settings.vue') },
  
  // Dorm Manager Routes
  { path: '/dormmanager/workbench', component: () => import('../views/manager/Workbench.vue') },
  { path: '/dormmanager/checkin', component: () => import('../views/manager/Checkin.vue') },
  { path: '/dormmanager/fee', component: () => import('../views/manager/Fee.vue') },
  { path: '/dormmanager/repair', component: () => import('../views/manager/Repair.vue') },
  { path: '/dormmanager/visitor', component: () => import('../views/manager/Visitor.vue') },
  { path: '/dormmanager/hygiene', component: () => import('../views/manager/Hygiene.vue') },
  { path: '/dormmanager/transfer', component: () => import('../views/manager/Transfer.vue') },
  { path: '/dormmanager/late-return', component: () => import('../views/manager/LateReturn.vue') },
  { path: '/dormmanager/items', component: () => import('../views/manager/Items.vue') },
  { path: '/dormmanager/messages', component: () => import('../views/manager/Messages.vue') },
  { path: '/dormmanager/patrol', component: () => import('../views/manager/Patrol.vue') },
  { path: '/dormmanager/call', component: () => import('../views/manager/Call.vue') },
  { path: '/dormmanager/feedback', component: () => import('../views/manager/Feedback.vue') },
  { path: '/dormmanager/profile', component: () => import('../views/manager/Profile.vue') },

  // Admin Routes
  { path: '/admin/overview', component: () => import('../views/admin/Overview.vue') },
  { path: '/admin/users/list', component: () => import('../views/admin/Users.vue') },
  { path: '/admin/users/roles', component: () => import('../views/admin/Users.vue') },
  { path: '/admin/resources/buildings', component: () => import('../views/admin/Resources.vue') },
  { path: '/admin/resources/rooms', component: () => import('../views/admin/Rooms.vue') },
  { path: '/admin/repairs/list', component: () => import('../views/admin/Repairs.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const homeByRole = {
  student: '/student/desk',
  dormmanager: '/dormmanager/workbench',
  admin: '/admin/overview'
}

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.path === '/login') {
    return userStore.token ? homeByRole[userStore.role] || '/student/desk' : true
  }
  if (!userStore.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const targetRole = to.path.split('/')[1]
  if (['student', 'dormmanager', 'admin'].includes(targetRole) && targetRole !== userStore.role) {
    return homeByRole[userStore.role] || '/student/desk'
  }
  return true
})

export default router
