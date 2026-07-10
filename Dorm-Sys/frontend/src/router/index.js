import { createRouter, createWebHistory } from 'vue-router'

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
  { path: '/student/settings', component: () => import('../views/student/Settings.vue') },
  
  // Dorm Manager Routes
  { path: '/dormmanager/workbench', component: () => import('../views/manager/Workbench.vue') },
  { path: '/dormmanager/checkin', component: () => import('../views/manager/Checkin.vue') },
  { path: '/dormmanager/repair', component: () => import('../views/manager/Repair.vue') },
  { path: '/dormmanager/visitor', component: () => import('../views/manager/Visitor.vue') },
  { path: '/dormmanager/hygiene', component: () => import('../views/manager/Hygiene.vue') },
  { path: '/dormmanager/transfer', component: () => import('../views/manager/Transfer.vue') },

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

export default router
