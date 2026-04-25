<template>
  <v-container max-width="1200" class="py-8">
    <h1 class="text-h5 font-weight-bold mb-2">Team WAR Report</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      Weekly Activity Reports for all team members for a selected week.
    </p>

    <!-- Team + week selectors -->
    <v-card variant="outlined" class="mb-6">
      <v-card-text class="pa-4">
        <v-row dense>
          <!-- Team picker (instructor only) -->
          <v-col v-if="isInstructor" cols="12" sm="4">
            <v-select
              v-model="selectedTeamId"
              :items="teams"
              item-title="teamName"
              item-value="id"
              label="Team"
              variant="outlined"
              density="comfortable"
              :loading="loadingTeams"
              :disabled="loadingTeams"
              no-data-text="No teams found"
              @update:model-value="onTeamSelected"
              hide-details
            />
          </v-col>

          <!-- Student: show team name as read-only -->
          <v-col v-else cols="12" sm="4" class="d-flex align-center">
            <v-text-field
              :model-value="studentTeamName"
              label="Team"
              variant="outlined"
              density="comfortable"
              readonly
              :loading="loadingTeams"
              hide-details
            />
          </v-col>

          <v-col cols="12" sm="4">
            <v-select
              v-model="selectedWeek"
              :items="weekOptions"
              item-title="label"
              item-value="date"
              label="Select a week"
              variant="outlined"
              density="comfortable"
              :loading="loadingWeeks"
              :disabled="loadingWeeks || !selectedTeamId"
              no-data-text="No active weeks available"
              @update:model-value="onWeekSelected"
              hide-details
            />
          </v-col>

          <v-col cols="12" sm="4" class="d-flex align-center">
            <v-btn
              color="primary"
              variant="tonal"
              block
              :loading="loadingReport"
              :disabled="!selectedWeek"
              @click="loadReport"
            >
              Generate
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- Not on a team (student) -->
    <v-alert v-if="noTeamError" type="info" variant="tonal" class="mb-6">
      You have not been assigned to a team yet.
    </v-alert>

    <!-- Loading -->
    <div v-if="loadingReport" class="d-flex justify-center py-12">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <!-- No data for selected week -->
    <v-alert
      v-else-if="report && hasNoData"
      type="info"
      variant="tonal"
      class="mb-6"
    >
      No WARs have been submitted for this team this week yet.
    </v-alert>

    <!-- ── Report ──────────────────────────────────────────────────────────── -->
    <template v-else-if="report">

      <!-- Summary -->
      <div class="d-flex align-center gap-4 mb-5 flex-wrap">
        <div>
          <p class="text-subtitle-1 font-weight-medium">{{ report.teamName }}</p>
          <p class="text-caption text-medium-emphasis">
            Week of {{ formatDate(report.weekStartDate) }}
          </p>
        </div>
        <v-chip color="primary" variant="tonal" size="small">
          {{ report.students.length }} of {{ report.students.length + report.nonSubmitters.length }} submitted
        </v-chip>
      </div>

      <!-- WAR table -->
      <v-card variant="outlined" class="mb-5">
        <v-card-title class="text-body-1 font-weight-medium pa-4 pb-2">WAR Submissions</v-card-title>
        <div class="overflow-x-auto">
          <table class="report-table">
            <thead>
              <tr>
                <th>Student</th>
                <th>Activity Category</th>
                <th>Planned Activity</th>
                <th>Description</th>
                <th>Planned Hours</th>
                <th>Actual Hours</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="student in report.students" :key="student.studentId">
                <!-- Student has activities -->
                <template v-if="student.activities.length > 0">
                  <tr
                    v-for="(act, i) in student.activities"
                    :key="i"
                    :class="{ 'group-start': i === 0 }"
                  >
                    <td v-if="i === 0" :rowspan="student.activities.length" class="student-name-cell font-weight-medium">
                      {{ student.studentName }}
                    </td>
                    <td>{{ act.category }}</td>
                    <td>{{ act.plannedActivity }}</td>
                    <td class="description-cell">{{ act.description || '—' }}</td>
                    <td class="hours-cell">{{ act.plannedHours ?? '—' }}</td>
                    <td class="hours-cell">{{ act.actualHours ?? '—' }}</td>
                    <td>
                      <v-chip
                        v-if="act.status"
                        :color="statusColor(act.status!)"
                        variant="tonal"
                        size="x-small"
                      >
                        {{ formatStatus(act.status!) }}
                      </v-chip>
                      <span v-else class="text-medium-emphasis">—</span>
                    </td>
                  </tr>
                </template>

                <!-- Student submitted WAR but has no activities -->
                <tr v-else class="group-start">
                  <td class="student-name-cell font-weight-medium">{{ student.studentName }}</td>
                  <td colspan="6" class="text-medium-emphasis text-caption">No activities recorded</td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </v-card>

      <!-- Non-submitters -->
      <v-card variant="outlined">
        <v-card-title class="text-body-1 font-weight-medium pa-4 pb-2">
          Did Not Submit
          <v-chip
            size="x-small"
            class="ml-2"
            :color="report.nonSubmitters.length > 0 ? 'warning' : 'success'"
            variant="tonal"
          >
            {{ report.nonSubmitters.length }}
          </v-chip>
        </v-card-title>
        <v-card-text class="pa-4 pt-0">
          <p v-if="report.nonSubmitters.length === 0" class="text-body-2 text-medium-emphasis">
            All team members submitted their WAR this week.
          </p>
          <v-chip-group v-else column>
            <v-chip
              v-for="name in report.nonSubmitters"
              :key="name"
              color="warning"
              variant="tonal"
              size="small"
            >
              {{ name }}
            </v-chip>
          </v-chip-group>
        </v-card-text>
      </v-card>

    </template>

    <!-- Error snackbar -->
    <v-snackbar v-model="showError" color="error" :timeout="4000" location="top">
      {{ errorMessage }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { reportsApi, type InstructorTeam, type WARTeamReport } from '@/api/reports'

const auth = useAuthStore()
const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

// ── State ─────────────────────────────────────────────────────────────────────

const loadingTeams  = ref(false)
const loadingWeeks  = ref(false)
const loadingReport = ref(false)
const noTeamError   = ref(false)

const teams           = ref<InstructorTeam[]>([])
const studentTeamName = ref('')
const selectedTeamId  = ref<number | null>(null)
const availableWeeks  = ref<string[]>([])
const selectedWeek    = ref<string | null>(null)
const report          = ref<WARTeamReport | null>(null)

const showError    = ref(false)
const errorMessage = ref('')

// ── Computed ──────────────────────────────────────────────────────────────────

const weekOptions = computed(() =>
  availableWeeks.value.map(date => ({ date, label: formatDate(date) }))
)

const hasNoData = computed(() =>
  report.value !== null && report.value.students.length === 0
)

// ── Lifecycle ─────────────────────────────────────────────────────────────────

onMounted(async () => {
  loadingTeams.value = true
  try {
    if (isInstructor.value) {
      const res = await reportsApi.getInstructorTeams()
      teams.value = res.data.data
      const first = teams.value[0]
      if (first) {
        selectedTeamId.value = first.id
        await loadWeeks(first.id)
      }
    } else {
      const res = await reportsApi.getStudentTeam()
      const team = res.data.data
      selectedTeamId.value = team.id
      studentTeamName.value = team.teamName
      await loadWeeks(team.id)
    }
  } catch (err: any) {
    if (err.response?.status === 404) {
      noTeamError.value = true
    } else {
      showErrorSnack(err.response?.data?.error || 'Failed to load team information.')
    }
  } finally {
    loadingTeams.value = false
  }
})

// ── Handlers ──────────────────────────────────────────────────────────────────

async function onTeamSelected(teamId: number) {
  selectedWeek.value = null
  report.value = null
  availableWeeks.value = []
  await loadWeeks(teamId)
}

async function onWeekSelected() {
  await loadReport()
}

async function loadWeeks(teamId: number) {
  loadingWeeks.value = true
  try {
    const res = await reportsApi.getTeamAvailableWeeks(teamId)
    availableWeeks.value = res.data.data
    if (availableWeeks.value.length > 0) {
      selectedWeek.value = availableWeeks.value[availableWeeks.value.length - 1] ?? null
      await loadReport()
    }
  } catch (err: any) {
    showErrorSnack(err.response?.data?.error || 'Failed to load available weeks.')
  } finally {
    loadingWeeks.value = false
  }
}

async function loadReport() {
  if (!selectedTeamId.value || !selectedWeek.value) return
  loadingReport.value = true
  report.value = null
  try {
    const res = await reportsApi.getWARTeamReport(selectedTeamId.value, selectedWeek.value)
    report.value = res.data.data
  } catch (err: any) {
    showErrorSnack(err.response?.data?.error || 'Failed to load report.')
  } finally {
    loadingReport.value = false
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function formatDate(date: string): string {
  return new Date(date + 'T00:00:00').toLocaleDateString('en-US', {
    month: 'long', day: 'numeric', year: 'numeric',
  })
}

function formatStatus(status: string): string {
  const map: Record<string, string> = {
    DONE: 'Done',
    IN_PROGRESS: 'In Progress',
    UNDER_TESTING: 'Under Testing',
  }
  return map[status] ?? status
}

function statusColor(status: string): string {
  const map: Record<string, string> = {
    DONE: 'success',
    IN_PROGRESS: 'primary',
    UNDER_TESTING: 'warning',
  }
  return map[status] ?? 'default'
}

function showErrorSnack(msg: string) {
  errorMessage.value = msg
  showError.value = true
}
</script>

<style scoped>
.report-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.report-table th {
  text-align: left;
  padding: 10px 16px;
  font-weight: 600;
  border-bottom: 2px solid rgba(0, 0, 0, 0.12);
  white-space: nowrap;
}

.report-table td {
  padding: 8px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  vertical-align: top;
}

.report-table tr.group-start td {
  border-top: 2px solid rgba(0, 0, 0, 0.12);
}

.student-name-cell {
  white-space: nowrap;
}

.hours-cell {
  white-space: nowrap;
  text-align: right;
}

.description-cell {
  max-width: 280px;
  word-break: break-word;
}
</style>
