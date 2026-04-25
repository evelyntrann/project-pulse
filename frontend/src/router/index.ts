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

    {
      path: '/join/:token',
      name: 'join-section',
      component: () => import('@/views/JoinSectionView.vue'),
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

        // ── Rubrics (ADMIN) ───────────────────────────────────────────────
        {
          path: 'rubrics',
          name: 'rubrics',
          component: () => import('@/views/rubrics/RubricListView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'rubrics/new',
          name: 'rubrics-new',
          component: () => import('@/views/rubrics/RubricCreateView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'rubrics/:id',
          name: 'rubrics-detail',
          component: () => import('@/views/rubrics/RubricDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },

        // ── Students (ADMIN + INSTRUCTOR) ────────────────────────────────
        {
          path: 'students',
          name: 'students',
          component: () => import('@/views/students/StudentListView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'INSTRUCTOR'] },
        },
        {
          path: 'students/:id',
          name: 'students-detail',
          component: () => import('@/views/students/StudentDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'INSTRUCTOR'] },
        },

        // ── Instructors (ADMIN) ───────────────────────────────────────────
        {
          path: 'instructors',
          name: 'instructors',
          component: () => import('@/views/instructors/InstructorListView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'instructors/invite',
          name: 'instructors-invite',
          component: () => import('@/views/instructors/InstructorInviteView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'instructors/assign',
          name: 'instructors-assign',
          component: () => import('@/views/instructors/InstructorAssignView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
        {
          path: 'instructors/:id',
          name: 'instructors-detail',
          component: () => import('@/views/instructors/InstructorDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },

        // ── Profile (STUDENT + INSTRUCTOR) ───────────────────────────────
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/profile/ProfileEditView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT', 'INSTRUCTOR'] },
        },

        // ── WAR (STUDENT) ────────────────────────────────────────────────
        {
          path: 'war',
          name: 'war',
          component: () => import('@/views/war/WARView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'] },
        },

        // ── Peer Evaluation (STUDENT) ─────────────────────────────────────
        {
          path: 'peer-evaluations',
          name: 'peer-evaluations',
          component: () => import('@/views/peer-evaluations/PeerEvalSubmitView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'] },
        },
        {
          path: 'peer-evaluations/my-report',
          name: 'peer-evaluations-my-report',
          component: () => import('@/views/peer-evaluations/PeerEvalMyReportView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'] },
        },

        // ── Reports (INSTRUCTOR) ──────────────────────────────────────────
        {
          path: 'reports/peer-eval-section',
          name: 'reports-peer-eval-section',
          component: () => import('@/views/reports/PeerEvalSectionReportView.vue'),
          meta: { requiresAuth: true, roles: ['INSTRUCTOR'] },
        },

        // ── WAR Team Report (INSTRUCTOR + STUDENT) ────────────────────────
        {
          path: 'reports/war-team',
          name: 'reports-war-team',
          component: () => import('@/views/reports/WARTeamReportView.vue'),
          meta: { requiresAuth: true, roles: ['INSTRUCTOR', 'STUDENT'] },
        },

        // ── Placeholder slots for remaining UCs ───────────────────────────
        // ── Peer Eval Student Report (INSTRUCTOR) — UC-33 ─────────────────
        {
          path: 'reports/peer-eval-student/:studentId',
          name: 'reports-peer-eval-student',
          component: () => import('@/views/reports/PeerEvalStudentReportView.vue'),
          meta: { requiresAuth: true, roles: ['INSTRUCTOR'] },
        },

        // Micah  → UC-34: /reports/war-student
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
