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
        <v-spacer />
        <v-btn
          v-if="instructor.active"
          color="error"
          variant="outlined"
          prepend-icon="mdi-account-off-outline"
          @click="openDeactivateDialog"
        >
          Deactivate
        </v-btn>
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

      <!-- Deactivated banner -->
      <v-alert v-if="!instructor.active" type="warning" variant="tonal" density="compact" class="mb-4">
        This instructor has been deactivated and no longer has access to the system.
      </v-alert>

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

    <!-- Deactivate Dialog -->
    <v-dialog v-model="deactivateDialog" max-width="500" persistent>
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Deactivate Instructor?</v-card-title>
        <v-card-text class="pa-6 pt-0">
          <p class="mb-3">
            You are about to deactivate
            <strong>{{ instructor?.firstName }} {{ instructor?.lastName }}</strong>.
          </p>
          <v-alert type="warning" variant="tonal" density="compact" class="mb-4">
            This instructor will no longer have access to the system. Their information and team
            assignments will be kept and the account can be reactivated in the future.
          </v-alert>
          <v-textarea
            v-model="deactivateReason"
            label="Reason for deactivation"
            variant="outlined"
            density="comfortable"
            rows="3"
            counter
            :rules="[v => !!v.trim() || 'Reason is required']"
            :error-messages="reasonError"
          />
          <v-alert v-if="deactivateError" type="error" variant="tonal" density="compact" class="mt-3">
            {{ deactivateError }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-btn variant="outlined" @click="closeDeactivateDialog">Cancel</v-btn>
          <v-spacer />
          <v-btn color="error" :loading="deactivating" @click="confirmDeactivate">
            Deactivate
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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

// ── Deactivate ────────────────────────────────────────────────────────────────
const deactivateDialog = ref(false)
const deactivateReason = ref('')
const reasonError = ref('')
const deactivateError = ref('')
const deactivating = ref(false)

function openDeactivateDialog() {
  deactivateReason.value = ''
  reasonError.value = ''
  deactivateError.value = ''
  deactivateDialog.value = true
}

function closeDeactivateDialog() {
  deactivateDialog.value = false
}

async function confirmDeactivate() {
  if (!deactivateReason.value.trim()) {
    reasonError.value = 'Reason is required'
    return
  }
  reasonError.value = ''
  deactivating.value = true
  deactivateError.value = ''
  try {
    await instructorsApi.deactivateInstructor(Number(route.params.id), deactivateReason.value.trim())
    instructor.value = { ...instructor.value!, active: false }
    deactivateDialog.value = false
  } catch (err: any) {
    deactivateError.value = err.response?.data?.error || 'Failed to deactivate. Please try again.'
  } finally {
    deactivating.value = false
  }
}
</script>
