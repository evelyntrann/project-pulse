<template>
  <v-container>
    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.push(`/sections/${route.params.id}`)"
    >
      Back to Section
    </v-btn>

    <v-row v-if="loading" justify="center" class="mt-8">
      <v-progress-circular indeterminate color="primary" />
    </v-row>
    <v-alert v-else-if="loadError" type="error" variant="tonal">{{ loadError }}</v-alert>

    <template v-else>
      <v-row align="center" class="mb-6">
        <v-col>
          <h1 class="text-h5 font-weight-bold">Assign Students to Teams</h1>
          <p class="text-body-2 text-grey mt-1">
            Drag students from the pool to a team, or use the Add button on each team card.
          </p>
        </v-col>
        <v-col cols="auto">
          <v-btn variant="outlined" class="mr-2" @click="router.push(`/sections/${route.params.id}`)">Cancel</v-btn>
          <v-btn color="primary" @click="openConfirm">Review & Save</v-btn>
        </v-col>
      </v-row>

      <v-row>
        <!-- Teams panel -->
        <v-col cols="12" md="8">
          <h2 class="text-subtitle-1 font-weight-bold mb-3">Teams</h2>
          <v-row>
            <v-col v-for="team in localTeams" :key="team.id" cols="12" sm="6">
              <v-card variant="outlined" class="pa-3">
                <div class="d-flex align-center justify-space-between mb-2">
                  <span class="text-body-1 font-weight-bold">{{ team.name }}</span>
                  <v-menu>
                    <template #activator="{ props }">
                      <v-btn
                        v-bind="props"
                        size="small"
                        variant="tonal"
                        prepend-icon="mdi-plus"
                        :disabled="unassignedStudents.length === 0"
                      >
                        Add
                      </v-btn>
                    </template>
                    <v-list density="compact" max-height="240" style="overflow-y:auto">
                      <v-list-item
                        v-for="student in unassignedStudents"
                        :key="student.id"
                        :title="`${student.firstName} ${student.lastName}`"
                        :subtitle="student.email"
                        @click="addToTeam(team.id, student.id)"
                      />
                    </v-list>
                  </v-menu>
                </div>

                <div v-if="team.assignedStudents.length === 0" class="text-caption text-grey py-2">
                  No students assigned
                </div>
                <v-chip
                  v-for="s in team.assignedStudents"
                  :key="s.id"
                  size="small"
                  variant="tonal"
                  color="primary"
                  closable
                  class="mr-1 mb-1"
                  @click:close="removeFromTeam(team.id, s.id)"
                >
                  {{ s.firstName }} {{ s.lastName }}
                </v-chip>
              </v-card>
            </v-col>
          </v-row>
        </v-col>

        <!-- Unassigned pool -->
        <v-col cols="12" md="4">
          <h2 class="text-subtitle-1 font-weight-bold mb-3">
            Unassigned Students
            <v-chip size="x-small" class="ml-1">{{ unassignedStudents.length }}</v-chip>
          </h2>
          <v-card variant="outlined" class="pa-3" min-height="200">
            <div v-if="unassignedStudents.length === 0" class="text-caption text-grey text-center py-4">
              All students have been assigned
            </div>
            <v-list v-else density="compact" lines="two">
              <v-list-item
                v-for="s in unassignedStudents"
                :key="s.id"
                :title="`${s.firstName} ${s.lastName}`"
                :subtitle="s.email"
                prepend-icon="mdi-account"
              />
            </v-list>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <!-- Confirm dialog -->
    <v-dialog v-model="confirmDialog" max-width="560">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Confirm Team Assignments</v-card-title>
        <v-card-text class="pa-6 pt-2" style="max-height:400px; overflow-y:auto">
          <div v-for="team in localTeams" :key="team.id" class="mb-4">
            <p class="text-body-2 font-weight-bold mb-1">{{ team.name }}</p>
            <div v-if="team.assignedStudents.length === 0" class="text-caption text-grey">No students assigned</div>
            <div v-else class="d-flex flex-wrap gap-1">
              <v-chip
                v-for="s in team.assignedStudents"
                :key="s.id"
                size="small"
                variant="tonal"
              >
                {{ s.firstName }} {{ s.lastName }}
              </v-chip>
            </div>
          </div>
          <v-divider v-if="unassignedStudents.length > 0" class="my-3" />
          <div v-if="unassignedStudents.length > 0">
            <p class="text-body-2 font-weight-bold mb-1 text-warning">Unassigned Students</p>
            <div class="d-flex flex-wrap gap-1">
              <v-chip v-for="s in unassignedStudents" :key="s.id" size="small" variant="tonal" color="warning">
                {{ s.firstName }} {{ s.lastName }}
              </v-chip>
            </div>
          </div>
          <v-alert v-if="submitError" type="error" variant="tonal" density="compact" class="mt-3">
            {{ submitError }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-btn variant="outlined" @click="confirmDialog = false">Modify</v-btn>
          <v-spacer />
          <v-btn color="primary" :loading="submitting" @click="submit">Confirm & Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { sectionsApi, type EnrolledStudent } from '@/api/sections'
import { teamsApi } from '@/api/teams'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const loadError = ref('')
const confirmDialog = ref(false)
const submitting = ref(false)
const submitError = ref('')

interface LocalTeam {
  id: number
  name: string
  assignedStudents: EnrolledStudent[]
}

const localTeams = ref<LocalTeam[]>([])
const allStudents = ref<EnrolledStudent[]>([])

// Students not yet assigned to any team in localTeams
const unassignedStudents = computed<EnrolledStudent[]>(() => {
  const assignedIds = new Set(
    localTeams.value.flatMap(t => t.assignedStudents.map(s => s.id))
  )
  return allStudents.value.filter(s => !assignedIds.has(s.id))
})

onMounted(async () => {
  loading.value = true
  try {
    const sectionId = Number(route.params.id)
    const [sectionRes, studentsRes] = await Promise.all([
      sectionsApi.getSection(sectionId),
      sectionsApi.getEnrolledStudents(sectionId),
    ])

    const section = sectionRes.data.data
    allStudents.value = studentsRes.data.data

    // Seed localTeams with existing assignments
    localTeams.value = section.teams.map(t => ({
      id: t.id,
      name: t.name,
      assignedStudents: t.members.map(m => ({
        id: m.id,
        firstName: m.firstName,
        lastName: m.lastName,
        email: m.email,
      })),
    }))

    // Ensure enrolled students not in section.teams are available
    const knownIds = new Set(allStudents.value.map(s => s.id))
    section.teams.forEach(t =>
      t.members.forEach(m => {
        if (!knownIds.has(m.id)) {
          allStudents.value.push({
            id: m.id,
            firstName: m.firstName,
            lastName: m.lastName,
            email: m.email,
          })
        }
      })
    )
  } catch {
    loadError.value = 'Failed to load section data'
  } finally {
    loading.value = false
  }
})

function addToTeam(teamId: number, studentId: number) {
  const student = allStudents.value.find(s => s.id === studentId)
  if (!student) return
  const team = localTeams.value.find(t => t.id === teamId)
  if (!team) return
  // Remove from any current team first
  localTeams.value.forEach(t => {
    t.assignedStudents = t.assignedStudents.filter(s => s.id !== studentId)
  })
  team.assignedStudents.push(student)
}

function removeFromTeam(teamId: number, studentId: number) {
  const team = localTeams.value.find(t => t.id === teamId)
  if (!team) return
  team.assignedStudents = team.assignedStudents.filter(s => s.id !== studentId)
}

function openConfirm() {
  submitError.value = ''
  confirmDialog.value = true
}

async function submit() {
  submitting.value = true
  submitError.value = ''
  try {
    // Send assignments for each team that has students
    await Promise.all(
      localTeams.value
        .filter(t => t.assignedStudents.length > 0)
        .map(t => teamsApi.assignStudents(t.id, t.assignedStudents.map(s => s.id)))
    )
    confirmDialog.value = false
    router.push(`/sections/${route.params.id}?saved=assigned`)
  } catch (err: any) {
    const message = err.response?.data?.error || err.response?.data?.message
    submitError.value = message || 'Failed to save assignments. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>
