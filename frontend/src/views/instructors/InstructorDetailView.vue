<template>
  <v-container max-width="760">
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4 pl-0" @click="router.push('/instructors')">
      Back to Instructors
    </v-btn>

    <div v-if="loading" class="text-center pa-8">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <v-alert v-else-if="error" type="error" variant="tonal">{{ error }}</v-alert>

    <template v-else-if="instructor">
      <!-- Header -->
      <div class="d-flex align-center mb-6">
        <div>
          <h1 class="text-h5 font-weight-bold">
            {{ instructor.firstName }} {{ instructor.lastName }}
          </h1>
          <v-chip
            :color="instructor.active ? 'success' : 'default'"
            size="small"
            variant="tonal"
            class="mt-1"
          >
            {{ instructor.active ? 'Active' : 'Deactivated' }}
          </v-chip>
        </div>
      </div>

      <!-- Profile -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Profile</v-card-title>
        <v-card-text class="pa-4 pt-2">
          <v-list density="compact" lines="two">
            <v-list-item prepend-icon="mdi-account"       title="First Name" :subtitle="instructor.firstName" />
            <v-list-item prepend-icon="mdi-account"       title="Last Name"  :subtitle="instructor.lastName" />
            <v-list-item prepend-icon="mdi-email-outline" title="Email"      :subtitle="instructor.email" />
          </v-list>
        </v-card-text>
      </v-card>

      <!-- Supervised Teams -->
      <v-card variant="outlined">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Supervised Teams</v-card-title>

        <v-card-text
          v-if="instructor.supervisedTeams.length === 0"
          class="pa-4 pt-2 text-medium-emphasis text-body-2"
        >
          Not currently assigned to any teams.
        </v-card-text>

        <template v-else>
          <div
            v-for="section in instructor.supervisedTeams"
            :key="section.sectionId"
            class="pa-4 pt-3"
            :class="{ 'border-t': true }"
          >
            <div class="text-body-2 font-weight-medium text-medium-emphasis mb-2">
              <v-icon size="16" class="mr-1">mdi-school-outline</v-icon>
              {{ section.sectionName }}
            </div>
            <div class="d-flex flex-wrap gap-2">
              <v-chip
                v-for="team in section.teams"
                :key="team.id"
                size="small"
                variant="tonal"
                color="primary"
                class="cursor-pointer"
                @click="router.push(`/teams/${team.id}`)"
              >
                {{ team.name }}
              </v-chip>
            </div>
          </div>
        </template>
      </v-card>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { instructorsApi, type InstructorDetail } from '@/api/instructors'

const router = useRouter()
const route = useRoute()

const instructor = ref<InstructorDetail | null>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const res = await instructorsApi.getInstructor(Number(route.params.id))
    instructor.value = res.data.data
  } catch {
    error.value = 'Instructor not found.'
  } finally {
    loading.value = false
  }
})
</script>
