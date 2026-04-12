<template>
  <v-container>
    <!-- Back button -->
    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.push('/teams')"
    >
      Back to Teams
    </v-btn>

    <!-- Loading -->
    <v-row v-if="loading" justify="center" class="mt-8">
      <v-progress-circular indeterminate color="primary" />
    </v-row>

    <!-- Error -->
    <v-alert v-else-if="error" type="error" variant="tonal">{{ error }}</v-alert>

    <template v-else-if="team">
      <!-- Header -->
      <v-row align="center" class="mb-6">
        <v-col>
          <div class="d-flex align-center gap-2 mb-1">
            <h1 class="text-h5 font-weight-bold">{{ team.name }}</h1>
            <v-chip size="small" variant="tonal" color="primary">{{ team.section.name }}</v-chip>
          </div>
          <p v-if="team.description" class="text-body-2 text-grey">{{ team.description }}</p>
        </v-col>
        <v-col cols="auto">
          <v-btn
            v-if="team.websiteUrl"
            variant="tonal"
            prepend-icon="mdi-open-in-new"
            :href="team.websiteUrl"
            target="_blank"
            class="mr-2"
          >
            Website
          </v-btn>
          <v-btn
            v-if="authStore.user?.role === 'ADMIN'"
            color="primary"
            variant="tonal"
            prepend-icon="mdi-pencil"
            class="mr-2"
            @click="router.push(`/teams/${team.id}/edit`)"
          >
            Edit
          </v-btn>
          <v-btn
            v-if="authStore.user?.role === 'ADMIN'"
            color="error"
            variant="tonal"
            prepend-icon="mdi-delete"
            @click="deleteDialog = true"
          >
            Delete
          </v-btn>
        </v-col>
      </v-row>

      <!-- Stats -->
      <v-row class="mb-6">
        <v-col cols="12" sm="4">
          <v-card variant="outlined" class="pa-4 text-center">
            <div class="text-h4 font-weight-bold text-primary">{{ team.members.length }}</div>
            <div class="text-body-2 text-grey mt-1">Students</div>
          </v-card>
        </v-col>
        <v-col cols="12" sm="4">
          <v-card variant="outlined" class="pa-4 text-center">
            <div class="text-h4 font-weight-bold text-primary">{{ team.instructors.length }}</div>
            <div class="text-body-2 text-grey mt-1">Instructors</div>
          </v-card>
        </v-col>
      </v-row>

      <!-- Students -->
      <h2 class="text-h6 font-weight-bold mb-3">Students</h2>
      <v-card variant="outlined" class="mb-6">
        <v-card-text v-if="team.members.length === 0" class="text-grey text-center py-4">
          No students assigned yet
        </v-card-text>
        <v-list v-else density="compact">
          <v-list-item
            v-for="m in team.members"
            :key="m.id"
            :title="`${m.firstName} ${m.lastName}`"
            :subtitle="m.email"
            prepend-icon="mdi-account"
          >
            <template v-if="authStore.user?.role === 'ADMIN'" #append>
              <v-btn
                icon="mdi-account-remove"
                variant="text"
                color="error"
                size="small"
                :loading="removingId === m.id"
                @click="promptRemove(m)"
              />
            </template>
          </v-list-item>
        </v-list>
      </v-card>

      <!-- Instructors -->
      <h2 class="text-h6 font-weight-bold mb-3">Instructors</h2>
      <v-card variant="outlined">
        <v-card-text v-if="team.instructors.length === 0" class="text-grey text-center py-4">
          No instructors assigned yet
        </v-card-text>
        <v-list v-else density="compact">
          <v-list-item
            v-for="i in team.instructors"
            :key="i.id"
            :title="`${i.firstName} ${i.lastName}`"
            :subtitle="i.email"
            prepend-icon="mdi-account-tie"
          />
        </v-list>
      </v-card>
    </template>

    <!-- Confirm delete team dialog -->
    <v-dialog v-model="deleteDialog" max-width="480" persistent>
      <v-card>
        <v-card-title class="text-h6">Delete team?</v-card-title>
        <v-card-text>
          <p class="mb-3">
            Permanently delete <strong>{{ team?.name }}</strong>? This cannot be undone.
          </p>
          <v-alert type="warning" variant="tonal" density="compact">
            <ul class="pl-4">
              <li>All students and instructors will be removed from this team</li>
              <li>All associated WARs and peer evaluations will be deleted</li>
              <li>Students and instructors will be notified by email</li>
            </ul>
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" variant="tonal" :loading="deleting" @click="confirmDelete">
            Delete permanently
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Confirm remove dialog -->
    <v-dialog v-model="confirmDialog" max-width="420" persistent>
      <v-card>
        <v-card-title class="text-h6">Remove student?</v-card-title>
        <v-card-text v-if="studentToRemove">
          Remove <strong>{{ studentToRemove.firstName }} {{ studentToRemove.lastName }}</strong>
          from <strong>{{ team?.name }}</strong>? They will be notified by email.
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="cancelRemove">Cancel</v-btn>
          <v-btn color="error" variant="tonal" :loading="removingId !== null" @click="confirmRemove">
            Remove
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbars -->
    <v-snackbar v-model="savedSnackbar" color="success" timeout="3000" location="bottom">
      Team updated successfully.
    </v-snackbar>
    <v-snackbar v-model="removedSnackbar" color="success" timeout="3000" location="bottom">
      Student removed successfully.
    </v-snackbar>
    <v-snackbar v-model="removeErrorSnackbar" color="error" timeout="4000" location="bottom">
      {{ removeError }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { teamsApi, type TeamDetail, type TeamMember } from '@/api/teams'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const team = ref<TeamDetail | null>(null)
const loading = ref(false)
const error = ref('')
const savedSnackbar = ref(false)
const removedSnackbar = ref(false)
const removeErrorSnackbar = ref(false)
const removeError = ref('')

const confirmDialog = ref(false)
const studentToRemove = ref<TeamMember | null>(null)
const removingId = ref<number | null>(null)

const deleteDialog = ref(false)
const deleting = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await teamsApi.getTeam(Number(route.params.id))
    team.value = res.data.data
  } catch {
    error.value = 'Team not found or failed to load'
  } finally {
    loading.value = false
  }
  if (route.query.saved === '1') {
    savedSnackbar.value = true
  }
})

async function confirmDelete() {
  if (!team.value) return
  deleting.value = true
  try {
    await teamsApi.deleteTeam(team.value.id)
    router.push('/teams?deleted=1')
  } catch {
    deleteDialog.value = false
    removeError.value = 'Failed to delete team. Please try again.'
    removeErrorSnackbar.value = true
  } finally {
    deleting.value = false
  }
}

function promptRemove(member: TeamMember) {
  studentToRemove.value = member
  confirmDialog.value = true
}

function cancelRemove() {
  confirmDialog.value = false
  studentToRemove.value = null
}

async function confirmRemove() {
  if (!team.value || !studentToRemove.value) return
  removingId.value = studentToRemove.value.id
  try {
    const res = await teamsApi.removeStudent(team.value.id, studentToRemove.value.id)
    team.value = res.data.data
    confirmDialog.value = false
    studentToRemove.value = null
    removedSnackbar.value = true
  } catch {
    removeError.value = 'Failed to remove student. Please try again.'
    removeErrorSnackbar.value = true
  } finally {
    removingId.value = null
  }
}
</script>
