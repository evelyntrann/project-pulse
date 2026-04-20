<template>
  <v-container max-width="760">
    <!-- Back button -->
    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.push('/students')"
    >
      Back to Students
    </v-btn>

    <div v-if="loading" class="text-center pa-8">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <v-alert v-else-if="error" type="error" variant="tonal">{{ error }}</v-alert>

    <template v-else-if="student">
      <!-- Header -->
      <h1 class="text-h5 font-weight-bold mb-6">
        {{ student.firstName }} {{ student.lastName }}
      </h1>

      <!-- Basic info -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Profile</v-card-title>
        <v-card-text class="pa-4 pt-2">
          <v-list density="compact" lines="two">
            <v-list-item
              prepend-icon="mdi-account"
              title="First Name"
              :subtitle="student.firstName"
            />
            <v-list-item
              prepend-icon="mdi-account"
              title="Last Name"
              :subtitle="student.lastName"
            />
            <v-list-item
              prepend-icon="mdi-email-outline"
              title="Email"
              :subtitle="student.email"
            />
            <v-list-item
              prepend-icon="mdi-school-outline"
              title="Section"
              :subtitle="student.sectionName ?? '—'"
            />
            <v-list-item
              prepend-icon="mdi-account-group-outline"
              title="Team"
              :subtitle="student.teamName ?? '—'"
            />
          </v-list>
        </v-card-text>
      </v-card>

      <!-- WARs — placeholder until Micah implements UC-27 -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">
          Weekly Activity Reports (WARs)
        </v-card-title>
        <v-card-text class="pa-4 pt-2 text-medium-emphasis text-body-2">
          WAR history will appear here once implemented.
        </v-card-text>
      </v-card>

      <!-- Peer Evaluations — placeholder until Micah implements UC-28/29 -->
      <v-card variant="outlined">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">
          Peer Evaluations
        </v-card-title>
        <v-card-text class="pa-4 pt-2 text-medium-emphasis text-body-2">
          Peer evaluation history will appear here once implemented.
        </v-card-text>
      </v-card>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { studentsApi, type StudentDetail } from '@/api/students'

const router = useRouter()
const route = useRoute()

const student = ref<StudentDetail | null>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const res = await studentsApi.getStudent(Number(route.params.id))
    student.value = res.data.data
  } catch {
    error.value = 'Student not found.'
  } finally {
    loading.value = false
  }
})
</script>
