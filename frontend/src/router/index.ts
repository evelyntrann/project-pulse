import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/sections',
    },
    {
      path: '/login',
      component: LoginView,
      meta: { guestOnly: true },
    },
    // Authenticated routes — wrapped in AppLayout
    {
      path: '/',
      component: () => import('@/components/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'sections',
          component: () => import('@/views/sections/SectionListView.vue'),
          meta: { requiresAuth: true, role: 'ADMIN' },
        },
        {
          path: 'sections/new',
          component: () => import('@/views/sections/SectionCreateView.vue'),
          meta: { requiresAuth: true, role: 'ADMIN' },
        },
        {
          path: 'sections/:id/edit',
          component: () => import('@/views/sections/SectionEditView.vue'),
          meta: { requiresAuth: true, role: 'ADMIN' },
        },
        {
          path: 'sections/:id/active-weeks',
          component: () => import('@/views/sections/SectionActiveWeeksView.vue'),
          meta: { requiresAuth: true, role: 'ADMIN' },
        },
        {
          path: 'sections/:id/invite-students',
          component: () => import('@/views/sections/SectionInviteStudentsView.vue'),
          meta: { requiresAuth: true, role: 'ADMIN' },
        },
        {
          path: 'sections/:id',
          component: () => import('@/views/sections/SectionDetailView.vue'),
          meta: { requiresAuth: true, role: 'ADMIN' },
        },
        {
          path: 'teams',
          component: () => import('@/views/teams/TeamListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'teams/new',
          component: () => import('@/views/teams/TeamCreateView.vue'),
          meta: { requiresAuth: true, role: 'ADMIN' },
        },
        {
          path: 'teams/:id/edit',
          component: () => import('@/views/teams/TeamEditView.vue'),
          meta: { requiresAuth: true, role: 'ADMIN' },
        },
        {
          path: 'teams/:id',
          component: () => import('@/views/teams/TeamDetailView.vue'),
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: '/forbidden',
      component: () => import('@/views/ForbiddenView.vue'),
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return '/login'
  }

  // Token exists but user not loaded yet (e.g. page refresh) — fetch it first
  if (auth.isLoggedIn && !auth.user) {
    await auth.fetchMe()
  }

  if (to.meta.guestOnly && auth.isLoggedIn) {
    return '/sections'
  }

  if (to.meta.role && auth.user?.role !== to.meta.role) {
    return '/forbidden'
  }
})

export default router
