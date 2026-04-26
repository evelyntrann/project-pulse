<template>
  <v-container>
    <!-- Greeting -->
    <div class="mb-6">
      <h1 class="text-h5 font-weight-bold">
        Welcome back, {{ auth.user?.firstName }}!
      </h1>
      <p class="text-body-2 text-grey mt-1">{{ roleLabel }}</p>
    </div>

    <!-- Quick-action cards -->
    <v-row v-if="quickLinks.length">
      <v-col
        v-for="card in quickLinks"
        :key="card.to"
        cols="12"
        sm="6"
        md="4"
      >
        <v-card
          :to="card.to"
          variant="outlined"
          hover
          class="pa-2"
        >
          <v-card-item>
            <template #prepend>
              <v-icon :color="card.color" size="36">{{ card.icon }}</v-icon>
            </template>
            <v-card-title class="text-body-1 font-weight-bold">{{ card.title }}</v-card-title>
          </v-card-item>
          <v-card-text class="text-body-2 text-grey pt-0">
            {{ card.description }}
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const roleLabel = computed(() => {
  const labels: Record<string, string> = {
    ADMIN:      'Administrator — manage sections, teams, and users.',
    INSTRUCTOR: 'Instructor — view and manage your assigned teams.',
    STUDENT:    'Student — submit weekly reports and peer evaluations.',
  }
  return labels[auth.user?.role ?? ''] ?? ''
})

interface QuickLink {
  title: string
  description: string
  icon: string
  color: string
  to: string
  roles: string[]
}

const allLinks: QuickLink[] = [
  // ADMIN
  {
    title: 'Sections',
    description: 'Create and manage course sections, configure active weeks, and invite students.',
    icon: 'mdi-school',
    color: 'primary',
    to: '/sections',
    roles: ['ADMIN'],
  },
  {
    title: 'Rubrics',
    description: 'Create and manage peer evaluation rubrics used across sections.',
    icon: 'mdi-clipboard-list-outline',
    color: 'deep-purple',
    to: '/rubrics',
    roles: ['ADMIN'],
  },
  {
    title: 'Instructors',
    description: 'Invite instructors and assign them to senior design teams.',
    icon: 'mdi-account-tie',
    color: 'teal',
    to: '/instructors',
    roles: ['ADMIN'],
  },
  {
    title: 'Students',
    description: 'View all enrolled students and their section and team assignments.',
    icon: 'mdi-account-school',
    color: 'orange',
    to: '/students',
    roles: ['ADMIN', 'INSTRUCTOR'],
  },
  {
    title: 'Teams',
    description: 'Create senior design teams and assign students and instructors.',
    icon: 'mdi-account-group',
    color: 'secondary',
    to: '/teams',
    roles: ['ADMIN', 'INSTRUCTOR'],
  },

  // INSTRUCTOR
  {
    title: 'WAR Team Report',
    description: "View your team's weekly activity report across all active weeks.",
    icon: 'mdi-chart-line',
    color: 'primary',
    to: '/reports/war-team',
    roles: ['INSTRUCTOR'],
  },
  {
    title: 'Peer Eval Report',
    description: 'View aggregated peer evaluation results for your section.',
    icon: 'mdi-file-chart-outline',
    color: 'teal',
    to: '/reports/peer-eval-section',
    roles: ['INSTRUCTOR'],
  },

  // STUDENT
  {
    title: 'Weekly Activity Report',
    description: 'Submit your weekly hours, accomplishments, and planned tasks.',
    icon: 'mdi-clipboard-text-outline',
    color: 'primary',
    to: '/war',
    roles: ['STUDENT'],
  },
  {
    title: 'Peer Evaluations',
    description: 'Submit your weekly evaluations for each teammate.',
    icon: 'mdi-account-star-outline',
    color: 'secondary',
    to: '/peer-evaluations',
    roles: ['STUDENT'],
  },
  {
    title: 'My Peer Eval Report',
    description: 'View the peer evaluation feedback you have received from your team.',
    icon: 'mdi-chart-bar',
    color: 'teal',
    to: '/peer-evaluations/my-report',
    roles: ['STUDENT'],
  },
  {
    title: 'Team WAR Report',
    description: "View your team's weekly activity report across all active weeks.",
    icon: 'mdi-chart-line',
    color: 'orange',
    to: '/reports/war-team',
    roles: ['STUDENT'],
  },
]

const quickLinks = computed(() =>
  allLinks.filter(l => l.roles.includes(auth.user?.role ?? ''))
)
</script>
