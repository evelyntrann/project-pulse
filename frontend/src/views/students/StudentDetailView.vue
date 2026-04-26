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
      <div class="d-flex align-center mb-6">
        <h1 class="text-h5 font-weight-bold">
          {{ student.firstName }} {{ student.lastName }}
        </h1>
        <v-spacer />
        <v-btn
          v-if="isAdmin"
          color="error"
          variant="outlined"
          prepend-icon="mdi-delete-outline"
          @click="deleteDialog = true"
        >
          Delete Student
        </v-btn>
      </div>

      <!-- Basic info -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Profile</v-card-title>
        <v-card-text class="pa-4 pt-2">
          <v-list density="compact" lines="two">
            <v-list-item prepend-icon="mdi-account"        title="First Name" :subtitle="student.firstName" />
            <v-list-item prepend-icon="mdi-account"        title="Last Name"  :subtitle="student.lastName" />
            <v-list-item prepend-icon="mdi-email-outline"  title="Email"      :subtitle="student.email" />
            <v-list-item prepend-icon="mdi-school-outline" title="Section"    :subtitle="student.sectionName ?? '—'" />
            <v-list-item prepend-icon="mdi-account-group-outline" title="Team" :subtitle="student.teamName ?? '—'" />
          </v-list>
        </v-card-text>
      </v-card>

      <!-- WARs — UC-34 entry point for instructors -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Weekly Activity Reports (WARs)</v-card-title>
        <v-card-text class="pa-4 pt-2">
          <p v-if="!isInstructor" class="text-body-2 text-medium-emphasis">
            WAR history is available to instructors.
          </p>
          <v-btn
            v-else
            color="primary"
            variant="tonal"
            prepend-icon="mdi-file-chart-outline"
            :to="`/reports/war-student/${student!.id}`"
          >
            Generate WAR Report
          </v-btn>
        </v-card-text>
      </v-card>

      <!-- Peer Evaluations — UC-33 entry point for instructors -->
      <v-card variant="outlined">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Peer Evaluations</v-card-title>
        <v-card-text class="pa-4 pt-2">
          <p v-if="!isInstructor" class="text-body-2 text-medium-emphasis">
            Peer evaluation history is available to instructors.
          </p>
          <v-btn
            v-else
            color="primary"
            variant="tonal"
            prepend-icon="mdi-chart-bar"
            :to="`/reports/peer-eval-student/${student!.id}`"
          >
            Generate Peer Eval Report
          </v-btn>
        </v-card-text>
      </v-card>
    </template>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="480">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Delete Student?</v-card-title>
        <v-card-text class="pa-6 pt-0">
          <p class="mb-3">
            You are about to permanently delete
            <strong>{{ student?.firstName }} {{ student?.lastName }}</strong>.
            This action cannot be undone.
          </p>
          <v-alert type="warning" variant="tonal" density="compact">
            Any WARs and peer evaluations submitted by this student will also be permanently deleted.
          </v-alert>
          <v-alert v-if="deleteError" type="error" variant="tonal" density="compact" class="mt-3">
            {{ deleteError }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-btn variant="outlined" @click="deleteDialog = false">Cancel</v-btn>
          <v-spacer />
          <v-btn color="error" :loading="deleting" @click="confirmDelete">Delete Permanently</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { studentsApi, type StudentDetail } from '@/api/students'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const isAdmin      = computed(() => auth.user?.role === 'ADMIN')
const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

const student = ref<StudentDetail | null>(null)
const loading = ref(true)
const error = ref('')

const deleteDialog = ref(false)
const deleting = ref(false)
const deleteError = ref('')

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

async function confirmDelete() {
  deleting.value = true
  deleteError.value = ''
  try {
    const name = `${student.value?.firstName} ${student.value?.lastName}`
    await studentsApi.deleteStudent(Number(route.params.id))
    deleteDialog.value = false
    router.push({ path: '/students', state: { successMessage: `${name} has been deleted.` } })
  } catch (err: any) {
    deleteError.value = err.response?.data?.error || 'Failed to delete student. Please try again.'
  } finally {
    deleting.value = false
  }
}
</script>
