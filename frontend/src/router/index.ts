import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'

// Extend RouteMeta to type our custom meta fields
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    guestOnly?: boolean
    roles?: string[]   // list of roles allowed; omit to allow any authenticated user
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/home' },

    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true },
    },

    // ── Authenticated shell ─────────────────────────────────────────────────
    {
      path: '/',
      component: () => import('@/components/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [

        // Home — all authenticated roles
        {
          path: 'home',
          name: 'home',
          component: () => import('@/views/DashboardView.vue'),
          meta: { requiresAuth: true },
        },

        // ── Sections (ADMIN) ──────────────────────────────────────────────
        {
          path: 'sections',
          name: 'sections',
          component: () => import('@/views/sections/SectionListView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'sections/new',
          name: 'sections-new',
          component: () => import('@/views/sections/SectionCreateView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'sections/:id',
          name: 'sections-detail',
          component: () => import('@/views/sections/SectionDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'sections/:id/edit',
          name: 'sections-edit',
          component: () => import('@/views/sections/SectionEditView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'sections/:id/active-weeks',
          name: 'sections-active-weeks',
          component: () => import('@/views/sections/SectionActiveWeeksView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'sections/:id/invite-students',
          name: 'sections-invite-students',
          component: () => import('@/views/sections/SectionInviteStudentsView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'sections/:id/assign-students',
          name: 'sections-assign-students',
          component: () => import('@/views/sections/SectionAssignStudentsView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'sections/:id/instructors',
          name: 'sections-instructors',
          component: () => import('@/views/sections/SectionInstructorsView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },

        // ── Teams (ADMIN + INSTRUCTOR) ────────────────────────────────────
        {
          path: 'teams',
          name: 'teams',
          component: () => import('@/views/teams/TeamListView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'INSTRUCTOR'] },
        },
        {
          path: 'teams/new',
          name: 'teams-new',
          component: () => import('@/views/teams/TeamCreateView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'teams/:id',
          name: 'teams-detail',
          component: () => import('@/views/teams/TeamDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'INSTRUCTOR'] },
        },
        {
          path: 'teams/:id/edit',
          name: 'teams-edit',
          component: () => import('@/views/teams/TeamEditView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },

        // ── Placeholder slots for teammates ──────────────────────────────
        // Angel  → UC-15–24: /students, /rubrics, /criteria
        // Micah  → UC-25–34: /activities, /evaluations, /reports
      ],
    },

    // ── Error pages ─────────────────────────────────────────────────────────
    {
      path: '/forbidden',
      name: 'forbidden',
      component: () => import('@/views/ForbiddenView.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/ForbiddenView.vue'),
    },
  ],
})

// ── Global navigation guard ────────────────────────────────────────────────
router.beforeEach(async (to) => {
  const auth = useAuthStore()

  // Redirect unauthenticated users to login
  if (to.meta.requiresAuth && !auth.isLoggedIn) return '/login'

  // Rehydrate user from token on hard refresh
  if (auth.isLoggedIn && !auth.user) {
    try {
      await auth.fetchMe()
    } catch {
      auth.logout()
      return '/login'
    }
  }

  // Redirect already-logged-in users away from guest-only pages
  if (to.meta.guestOnly && auth.isLoggedIn) return '/home'

  // Role-based access check
  const roles = to.meta.roles
  if (roles && auth.user && !roles.includes(auth.user.role)) return '/forbidden'
})

export default router
