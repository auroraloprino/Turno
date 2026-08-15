import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, role: ['OWNER', 'ADMIN'] },
      children: [
        { path: '', redirect: 'dashboard' },
        { path: 'dashboard',   component: () => import('@/views/admin/DashboardView.vue') },
        { path: 'utenti',      component: () => import('@/views/admin/UtentiView.vue') },
        { path: 'planner',     component: () => import('@/views/admin/PlannerView.vue') },
        { path: 'timbrature',  component: () => import('@/views/admin/TimbratureView.vue') },
        { path: 'permessi',    component: () => import('@/views/admin/PermessiView.vue') },
        { path: 'chat',        component: () => import('@/views/admin/ChatView.vue') },
        { path: 'credenziali', component: () => import('@/views/admin/CredenzialiView.vue') },
      ],
    },
    {
      path: '/user',
      component: () => import('@/layouts/UserLayout.vue'),
      meta: { requiresAuth: true, role: ['USER'] },
      children: [
        { path: '', redirect: 'timbratura' },
        { path: 'timbratura', component: () => import('@/views/user/TimbraturaView.vue') },
        { path: 'planner',    component: () => import('@/views/user/PlannerView.vue') },
        { path: 'permessi',   component: () => import('@/views/user/PermessiView.vue') },
        { path: 'chat',       component: () => import('@/views/user/ChatView.vue') },
        { path: 'profilo',    component: () => import('@/views/user/ProfiloView.vue') },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.public && auth.isAuthenticated) {
    return auth.role === 'OWNER' || auth.role === 'ADMIN' ? '/admin' : '/user'
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return '/login'
  }

  if (to.meta.requiresAuth && to.meta.role && !(to.meta.role as string[]).includes(auth.role!)) {
    return auth.role === 'OWNER' || auth.role === 'ADMIN' ? '/admin' : '/user'
  }
})

export default router
