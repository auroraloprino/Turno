import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: () => import('@/views/LoginView.vue') },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      children: [
        { path: '', redirect: 'dashboard' },
        { path: 'dashboard',   component: () => import('@/views/admin/DashboardView.vue') },
        { path: 'utenti',      component: () => import('@/views/admin/UtentiView.vue') },
        { path: 'planner',     component: () => import('@/views/admin/PlannerView.vue') },
        { path: 'timbrature',  component: () => import('@/views/admin/TimbratureView.vue') },
        { path: 'permessi',    component: () => import('@/views/admin/PermessiView.vue') },
        { path: 'chat',        component: () => import('@/views/admin/ChatView.vue') },
        { path: 'credenziali', component: () => import('@/views/admin/CredenzialiView.vue') },
      ]
    },
    {
      path: '/user',
      component: () => import('@/layouts/UserLayout.vue'),
      children: [
        { path: '', redirect: 'timbratura' },
        { path: 'timbratura', component: () => import('@/views/user/TimbraturaView.vue') },
        { path: 'planner',    component: () => import('@/views/user/PlannerView.vue') },
        { path: 'permessi',   component: () => import('@/views/user/PermessiView.vue') },
        { path: 'chat',       component: () => import('@/views/user/ChatView.vue') },
      ]
    }
  ]
})

export default router
