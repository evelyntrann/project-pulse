<template>
  <v-container max-width="1100" class="py-8">

    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.back()"
    >
      Back to Student
    </v-btn>

    <h1 class="text-h5 font-weight-bold mb-1">
      WAR Report
      <span v-if="report" class="font-weight-regular">— {{ report.studentName }}</span>
    </h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      Weekly Activity Reports submitted by this student during the selected period.
    </p>

    <!-- Week range selectors -->
    <v-card variant="outlined" class="mb-6">
      <v-card-text class="pa-4">
        <v-row dense>
          <v-col cols="12" sm="4">
            <v-select
              v-model="startWeek"
              :items="weekOptions"
              item-title="label"
              item-value="date"
              label="From week"
              variant="outlined"
              density="comfortable"
              :loading="loadingWeeks"
              :disabled="loadingWeeks || weekOptions.length === 0"
              no-data-text="No active weeks"
              hide-details
            />
          </v-col>
          <v-col cols="12" sm="4">
            <v-select
              v-model="endWeek"
              :items="endWeekOptions"
              item-title="label"
              item-value="date"
              label="To week"
              variant="outlined"
              density="comfortable"
              :loading="loadingWeeks"
              :disabled="loadingWeeks || weekOptions.length === 0"
              no-data-text="No active weeks"
              hide-details
            />
          </v-col>
          <v-col cols="12" sm="4" class="d-flex align-center">
            <v-btn
              color="primary"
              variant="tonal"
              block
              :loading="loadingReport"
              :disabled="!startWeek || !endWeek"
              @click="loadReport"
            >
              Generate
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- Not enrolled in section -->
    <v-alert v-if="noDataError" type="info" variant="tonal" class="mb-6">
      This student is not enrolled in a section with active weeks.
    </v-alert>

    <!-- Loading -->
    <div v-if="loadingReport" class="d-flex justify-center py-12">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <!-- No WARs at all in range -->
    <v-alert
      v-else-if="report && hasNoData"
      type="info"
      variant="tonal"
      class="mb-6"
    >
      No WARs were submitted by this student during the selected period.
    </v-alert>

    <!-- ── Report ──────────────────────────────────────────────────────────── -->
    <template v-else-if="report">

      <div class="d-flex align-center gap-4 mb-5 flex-wrap">
        <div>
          <p class="text-subtitle-1 font-weight-medium">{{ report.studentName }}</p>
          <p class="text-caption text-medium-emphasis">
            {{ submittedCount }} of {{ report.weeks.length }} week(s) submitted
          </p>
        </div>
      </div>

      <!-- One card per week -->
      <div v-for="week in report.weeks" :key="week.weekStartDate" class="mb-5">
        <v-card variant="outlined">

          <!-- Week header -->
          <v-card-title class="text-body-1 font-weight-medium pa-4 pb-2 d-flex align-center gap-3">
            Active week: {{ formatWeekRange(week.weekStartDate) }}
            <v-chip
              :color="week.submitted ? 'success' : 'warning'"
              variant="tonal"
              size="x-small"
            >
              {{ week.submitted ? 'Submitted' : 'Not submitted' }}
            </v-chip>
          </v-card-title>

          <!-- No WAR this week -->
          <v-card-text v-if="!week.submitted" class="pa-4 pt-1 text-body-2 text-medium-emphasis">
            No WAR submitted this week.
          </v-card-text>

          <!-- Activities table -->
          <div v-else-if="week.activities.length === 0" class="pa-4 pt-1 text-body-2 text-medium-emphasis">
            WAR submitted but no activities recorded.
          </div>

          <div v-else class="overflow-x-auto">
            <table class="report-table">
              <thead>
                <tr>
                  <th>Activity Category</th>
                  <th>Planned Activity</th>
                  <th>Description</th>
                  <th>Planned Hours</th>
                  <th>Actual Hours</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(act, i) in week.activities" :key="i">
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
              </tbody>
            </table>
          </div>

        </v-card>
      </div>

    </template>

    <v-snackbar v-model="showError" color="error" :timeout="4000" location="top">
      {{ errorMessage }}
    </v-snackbar>

  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reportsApi, type WARStudentReport } from '@/api/reports'

const route  = useRoute()
const router = useRouter()

const studentId = Number(route.params.studentId)

const loadingWeeks  = ref(false)
const loadingReport = ref(false)
const noDataError   = ref(false)

const availableWeeks = ref<string[]>([])
const startWeek      = ref<string | null>(null)
const endWeek        = ref<string | null>(null)
const report         = ref<WARStudentReport | null>(null)

const showError    = ref(false)
const errorMessage = ref('')

const weekOptions = computed(() =>
  availableWeeks.value.map(date => ({ date, label: formatWeekRange(date) }))
)

const endWeekOptions = computed(() =>
  weekOptions.value.filter(o => !startWeek.value || o.date >= startWeek.value)
)

const hasNoData = computed(() =>
  report.value !== null && report.value.weeks.every(w => !w.submitted)
)

const submittedCount = computed(() =>
  report.value?.weeks.filter(w => w.submitted).length ?? 0
)

onMounted(async () => {
  loadingWeeks.value = true
  try {
    const res = await reportsApi.getStudentAvailableWeeks(studentId)
    availableWeeks.value = res.data.data
    if (availableWeeks.value.length > 0) {
      startWeek.value = availableWeeks.value[0] ?? null
      endWeek.value   = availableWeeks.value[availableWeeks.value.length - 1] ?? null
      await loadReport()
    } else {
      noDataError.value = true
    }
  } catch (err: any) {
    if (err.response?.status === 404) {
      noDataError.value = true
    } else {
      showErrorSnack(err.response?.data?.error || 'Failed to load available weeks.')
    }
  } finally {
    loadingWeeks.value = false
  }
})

async function loadReport() {
  if (!startWeek.value || !endWeek.value) return
  if (endWeek.value < startWeek.value) {
    showErrorSnack('End week must be on or after start week.')
    return
  }
  loadingReport.value = true
  report.value = null
  try {
    const res = await reportsApi.getWARStudentReport(studentId, startWeek.value, endWeek.value)
    report.value = res.data.data
  } catch (err: any) {
    showErrorSnack(err.response?.data?.error || 'Failed to load report.')
  } finally {
    loadingReport.value = false
  }
}

function formatWeekRange(weekStartDate: string): string {
  const start = new Date(weekStartDate + 'T00:00:00')
  const end   = new Date(start)
  end.setDate(end.getDate() + 6)
  const fmt = (d: Date) =>
    `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}-${d.getFullYear()}`
  return `${fmt(start)} - ${fmt(end)}`
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
  showError.value    = true
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
.hours-cell      { white-space: nowrap; text-align: right; }
.description-cell { max-width: 280px; word-break: break-word; }
</style>
