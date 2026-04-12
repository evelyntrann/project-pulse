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

    <!-- Student placeholder (Micah / Angel to fill in) -->
    <v-alert
      v-if="auth.user?.role === 'STUDENT'"
      type="info"
      variant="tonal"
      class="mt-4"
    >
      Your dashboard features (Weekly Activity Reports, Peer Evaluations) are coming soon.
    </v-alert>
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
  {
    title: 'Sections',
    description: 'Create and manage course sections, configure active weeks, and invite students.',
    icon: 'mdi-school',
    color: 'primary',
    to: '/sections',
    roles: ['ADMIN'],
  },
  {
    title: 'Teams',
    description: 'Create senior design teams, assign students and instructors.',
    icon: 'mdi-account-group',
    color: 'secondary',
    to: '/teams',
    roles: ['ADMIN', 'INSTRUCTOR'],
  },
]

const quickLinks = computed(() =>
  allLinks.filter(l => l.roles.includes(auth.user?.role ?? ''))
)
</script>
