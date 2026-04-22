<template>
  <v-container max-width="960">
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4 pl-0" @click="router.push('/instructors')">
      Back to Instructors
    </v-btn>

    <h1 class="text-h5 font-weight-bold mb-6">Assign Instructors to Teams</h1>

    <div v-if="loading" class="text-center pa-8">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <v-alert v-else-if="loadError" type="error" variant="tonal">{{ loadError }}</v-alert>

    <template v-else>
      <v-alert type="info" variant="tonal" density="compact" class="mb-4">
        Select an instructor for each team. Click <strong>Review & Save</strong> when done.
        Only teams with changes will be saved.
      </v-alert>

      <!-- Teams table -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Teams</v-card-title>
        <v-table>
          <thead>
            <tr>
              <th class="text-left">Section</th>
              <th class="text-left">Team</th>
              <th class="text-left">Current Instructor</th>
              <th class="text-left" style="min-width: 260px">Assign Instructor</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.team.id">
              <td class="text-medium-emphasis">{{ row.team.sectionName }}</td>
              <td class="font-weight-medium">{{ row.team.name }}</td>
              <td class="text-medium-emphasis">
                {{ row.team.instructors[0]
                   ? `${row.team.instructors[0].firstName} ${row.team.instructors[0].lastName}`
                   : '—' }}
              </td>
              <td>
                <v-select
                  v-model="row.selectedInstructorId"
                  :items="instructorItems"
                  item-title="label"
                  item-value="id"
                  variant="outlined"
                  density="compact"
                  clearable
                  placeholder="No instructor"
                  hide-details
                  class="my-1"
                />
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-card>

      <div class="d-flex justify-end gap-2">
        <v-btn variant="outlined" @click="router.push('/instructors')">Cancel</v-btn>
        <v-btn
          color="primary"
          :disabled="pendingChanges.length === 0"
          @click="confirmDialog = true"
        >
          Review & Save ({{ pendingChanges.length }} change{{ pendingChanges.length !== 1 ? 's' : '' }})
        </v-btn>
      </div>
    </template>

    <!-- Confirmation Dialog -->
    <v-dialog v-model="confirmDialog" max-width="560">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Confirm Assignments</v-card-title>
        <v-card-text class="pa-6 pt-0">
          <p class="text-body-2 text-medium-emphasis mb-3">
            The following instructor assignments will be saved:
          </p>
          <v-list density="compact" lines="two">
            <v-list-item
              v-for="change in pendingChanges"
              :key="change.teamId"
              :prepend-icon="change.instructorId ? 'mdi-account-tie' : 'mdi-account-off-outline'"
            >
              <template #title>{{ change.teamName }}</template>
              <template #subtitle>
                <span class="text-medium-emphasis">{{ change.sectionName }}</span>
                <span class="mx-1">→</span>
                <span :class="change.instructorId ? '' : 'text-error'">
                  {{ change.instructorId ? change.instructorName : 'Remove instructor' }}
                </span>
              </template>
            </v-list-item>
          </v-list>
          <v-alert v-if="saveError" type="error" variant="tonal" density="compact" class="mt-3">
            {{ saveError }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-btn variant="outlined" @click="confirmDialog = false">Cancel</v-btn>
          <v-spacer />
          <v-btn color="primary" :loading="saving" @click="saveChanges">Confirm</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { teamsApi, type TeamSummary } from '@/api/teams'
import { instructorsApi, type InstructorSummary } from '@/api/instructors'

const router = useRouter()

const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
const saveError = ref('')
const confirmDialog = ref(false)

interface Row {
  team: TeamSummary
  selectedInstructorId: number | null
}

const rows = ref<Row[]>([])
const instructors = ref<InstructorSummary[]>([])

const instructorItems = computed(() =>
  instructors.value.map(i => ({
    id: i.id,
    label: `${i.firstName} ${i.lastName}`,
  }))
)

const pendingChanges = computed(() =>
  rows.value
    .filter(row => {
      const currentId = row.team.instructors[0]?.id ?? null
      return row.selectedInstructorId !== currentId
    })
    .map(row => {
      const instructor = instructors.value.find(i => i.id === row.selectedInstructorId)
      return {
        teamId: row.team.id,
        teamName: row.team.name,
        sectionName: row.team.sectionName,
        instructorId: row.selectedInstructorId,
        instructorName: instructor ? `${instructor.firstName} ${instructor.lastName}` : null,
      }
    })
)

onMounted(async () => {
  try {
    const [teamsRes, instructorsRes] = await Promise.all([
      teamsApi.findTeams({}),
      instructorsApi.listInstructors(),
    ])
    instructors.value = instructorsRes.data.data
    rows.value = teamsRes.data.data
      .sort((a, b) => {
        const sc = a.sectionName.localeCompare(b.sectionName)
        return sc !== 0 ? sc : a.name.localeCompare(b.name)
      })
      .map(team => ({
        team,
        selectedInstructorId: team.instructors[0]?.id ?? null,
      }))
  } catch {
    loadError.value = 'Failed to load teams or instructors. Please try again.'
  } finally {
    loading.value = false
  }
})

async function saveChanges() {
  saving.value = true
  saveError.value = ''
  try {
    await Promise.all(
      pendingChanges.value.map(change =>
        change.instructorId
          ? teamsApi.assignInstructor(change.teamId, change.instructorId)
          : teamsApi.removeInstructor(change.teamId)
      )
    )
    confirmDialog.value = false
    router.push({ path: '/instructors', state: { successMessage: 'Instructor assignments saved.' } })
  } catch {
    saveError.value = 'Failed to save some assignments. Please try again.'
  } finally {
    saving.value = false
  }
}
</script>
