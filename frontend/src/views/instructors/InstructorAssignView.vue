<template>
  <v-container max-width="1000">
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
        Add instructors to teams using the dropdown. Remove existing ones with the × on each chip.
        Every team must keep at least one instructor. Click <strong>Review & Save</strong> when done.
      </v-alert>

      <v-card variant="outlined" class="mb-4">
        <v-table>
          <thead>
            <tr>
              <th class="text-left" style="width:160px">Section</th>
              <th class="text-left" style="width:180px">Team</th>
              <th class="text-left">Instructors</th>
              <th class="text-left" style="min-width:240px">Add Instructor</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.teamId">
              <td class="text-medium-emphasis text-body-2">{{ row.sectionName }}</td>
              <td class="font-weight-medium">{{ row.teamName }}</td>
              <td>
                <div class="d-flex flex-wrap gap-1 py-2">
                  <v-chip
                    v-for="inst in row.effectiveInstructors"
                    :key="inst.id"
                    size="small"
                    :closable="row.effectiveInstructors.length > 1"
                    color="primary"
                    variant="tonal"
                    @click:close="removeFromRow(row, inst.id)"
                  >
                    {{ inst.firstName }} {{ inst.lastName }}
                  </v-chip>
                  <span v-if="row.effectiveInstructors.length === 0" class="text-medium-emphasis text-body-2">—</span>
                </div>
              </td>
              <td>
                <v-select
                  v-model="row.addingId"
                  :items="availableToAdd(row)"
                  item-title="label"
                  item-value="id"
                  variant="outlined"
                  density="compact"
                  clearable
                  placeholder="Select instructor…"
                  hide-details
                  class="my-1"
                  @update:model-value="addToRow(row, $event)"
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
    <v-dialog v-model="confirmDialog" max-width="580">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Confirm Assignments</v-card-title>
        <v-card-text class="pa-6 pt-0">
          <p class="text-body-2 text-medium-emphasis mb-3">The following changes will be saved:</p>
          <v-list density="compact" lines="two">
            <template v-for="change in pendingChanges" :key="`${change.teamId}-${change.type}-${change.instructorId}`">
              <v-list-item
                :prepend-icon="change.type === 'add' ? 'mdi-account-plus-outline' : 'mdi-account-minus-outline'"
                :base-color="change.type === 'add' ? 'success' : 'error'"
              >
                <template #title>{{ change.teamName }}</template>
                <template #subtitle>
                  <span class="text-medium-emphasis">{{ change.sectionName }}</span>
                  <span class="mx-1">—</span>
                  <span :class="change.type === 'add' ? 'text-success' : 'text-error'">
                    {{ change.type === 'add' ? 'Add' : 'Remove' }} {{ change.instructorName }}
                  </span>
                </template>
              </v-list-item>
            </template>
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
import { teamsApi, type TeamSummary, type TeamMember } from '@/api/teams'
import { instructorsApi, type InstructorSummary } from '@/api/instructors'

const router = useRouter()

const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
const saveError = ref('')
const confirmDialog = ref(false)

interface InstructorItem {
  id: number
  firstName: string
  lastName: string
}

interface Row {
  teamId: number
  teamName: string
  sectionName: string
  originalInstructors: InstructorItem[]  // as loaded from server
  adds: Set<number>                       // instructor IDs to add
  removes: Set<number>                    // instructor IDs to remove
  addingId: number | null                 // v-select bound value (cleared after add)
  effectiveInstructors: InstructorItem[]  // computed field updated after each change
}

const rows = ref<Row[]>([])
const instructors = ref<InstructorSummary[]>([])

function buildEffective(row: Row): InstructorItem[] {
  const base = row.originalInstructors.filter(i => !row.removes.has(i.id))
  const added = instructors.value
    .filter(i => row.adds.has(i.id))
    .map(i => ({ id: i.id, firstName: i.firstName, lastName: i.lastName }))
  return [...base, ...added]
}

function availableToAdd(row: Row): { id: number; label: string }[] {
  const alreadyIn = new Set(row.effectiveInstructors.map(i => i.id))
  return instructors.value
    .filter(i => !alreadyIn.has(i.id))
    .map(i => ({ id: i.id, label: `${i.firstName} ${i.lastName}` }))
}

function addToRow(row: Row, instructorId: number | null) {
  if (!instructorId) return
  row.removes.delete(instructorId)
  row.adds.add(instructorId)
  row.effectiveInstructors = buildEffective(row)
  row.addingId = null
}

function removeFromRow(row: Row, instructorId: number) {
  if (row.effectiveInstructors.length <= 1) return  // guard: must keep 1
  if (row.originalInstructors.some(i => i.id === instructorId)) {
    row.removes.add(instructorId)
    row.adds.delete(instructorId)
  } else {
    row.adds.delete(instructorId)
  }
  row.effectiveInstructors = buildEffective(row)
}

interface PendingChange {
  teamId: number
  teamName: string
  sectionName: string
  type: 'add' | 'remove'
  instructorId: number
  instructorName: string
}

const pendingChanges = computed((): PendingChange[] => {
  const changes: PendingChange[] = []
  for (const row of rows.value) {
    for (const id of row.adds) {
      const inst = instructors.value.find(i => i.id === id)
      if (inst) changes.push({ teamId: row.teamId, teamName: row.teamName, sectionName: row.sectionName, type: 'add', instructorId: id, instructorName: `${inst.firstName} ${inst.lastName}` })
    }
    for (const id of row.removes) {
      const inst = instructors.value.find(i => i.id === id) ?? row.originalInstructors.find(i => i.id === id)
      if (inst) changes.push({ teamId: row.teamId, teamName: row.teamName, sectionName: row.sectionName, type: 'remove', instructorId: id, instructorName: `${inst.firstName} ${inst.lastName}` })
    }
  }
  return changes
})

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
      .map(team => {
        const orig = team.instructors.map((i: TeamMember) => ({ id: i.id, firstName: i.firstName, lastName: i.lastName }))
        return {
          teamId: team.id,
          teamName: team.name,
          sectionName: team.sectionName,
          originalInstructors: orig,
          adds: new Set<number>(),
          removes: new Set<number>(),
          addingId: null,
          effectiveInstructors: [...orig],
        }
      })
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
        change.type === 'add'
          ? teamsApi.assignInstructor(change.teamId, change.instructorId)
          : teamsApi.removeInstructor(change.teamId, change.instructorId)
      )
    )
    confirmDialog.value = false
    router.push({ path: '/instructors', state: { successMessage: 'Instructor assignments saved.' } })
  } catch (err: any) {
    saveError.value = err.response?.data?.error || 'Failed to save some assignments. Please try again.'
  } finally {
    saving.value = false
  }
}
</script>
