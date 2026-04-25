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
      Peer Evaluation Report
      <span v-if="report" class="font-weight-regular">— {{ report.studentName }}</span>
    </h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      Evaluations received by this student, including evaluator identities and private comments.
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

    <!-- No evals in selected range -->
    <v-alert
      v-else-if="report && hasNoData"
      type="info"
      variant="tonal"
      class="mb-6"
    >
      No peer evaluations were submitted for this student during the selected period.
    </v-alert>

    <!-- ── Report table ────────────────────────────────────────────────────── -->
    <template v-else-if="report">

      <div class="d-flex align-center gap-4 mb-5 flex-wrap">
        <div>
          <p class="text-subtitle-1 font-weight-medium">{{ report.studentName }}</p>
          <p class="text-caption text-medium-emphasis">
            {{ weeksWithEvals }} of {{ report.weeks.length }} week(s) have evaluations
          </p>
        </div>
        <v-chip color="primary" variant="tonal" size="small">
          Max grade: {{ formatScore(report.maxGrade) }} pts
        </v-chip>
      </div>

      <v-card variant="outlined">
        <v-card-title class="text-body-1 font-weight-medium pa-4 pb-2">
          Peer Evaluation Results
        </v-card-title>
        <div class="overflow-x-auto">
          <table class="report-table">
            <thead>
              <tr>
                <th>Week</th>
                <th>Grade</th>
                <th>Commented By</th>
                <th>Public Comments</th>
                <th>Private Comments</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="week in report.weeks" :key="week.weekStartDate">

                <template v-if="week.evaluations.length > 0">
                  <tr
                    v-for="(ev, i) in week.evaluations"
                    :key="ev.evaluatorName"
                    :class="{ 'group-start': i === 0 }"
                  >
                    <td v-if="i === 0" :rowspan="week.evaluations.length" class="week-cell">
                      {{ formatWeekRange(week.weekStartDate) }}
                    </td>
                    <td v-if="i === 0" :rowspan="week.evaluations.length" class="grade-cell">
                      {{ formatScore(week.grade) }} / {{ formatScore(report.maxGrade) }}
                    </td>
                    <td class="name-cell">{{ ev.evaluatorName }}</td>
                    <td class="comment-cell">{{ ev.publicComment || '—' }}</td>
                    <td class="comment-cell">{{ ev.privateComment || '—' }}</td>
                  </tr>
                </template>

                <tr v-else class="group-start">
                  <td class="week-cell">{{ formatWeekRange(week.weekStartDate) }}</td>
                  <td class="text-medium-emphasis">—</td>
                  <td colspan="3" class="text-medium-emphasis text-caption">
                    No evaluations received this week
                  </td>
                </tr>

              </template>
            </tbody>
          </table>
        </div>
      </v-card>

    </template>

    <v-snackbar v-model="showError" color="error" :timeout="4000" location="top">
      {{ errorMessage }}
    </v-snackbar>

  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reportsApi, type PeerEvalStudentReport } from '@/api/reports'

const route  = useRoute()
const router = useRouter()

const studentId = Number(route.params.studentId)

const loadingWeeks  = ref(false)
const loadingReport = ref(false)
const noDataError   = ref(false)

const availableWeeks = ref<string[]>([])
const startWeek      = ref<string | null>(null)
const endWeek        = ref<string | null>(null)
const report         = ref<PeerEvalStudentReport | null>(null)

const showError    = ref(false)
const errorMessage = ref('')

const weekOptions = computed(() =>
  availableWeeks.value.map(date => ({ date, label: formatWeekRange(date) }))
)

const endWeekOptions = computed(() =>
  weekOptions.value.filter(o => !startWeek.value || o.date >= startWeek.value)
)

const hasNoData = computed(() =>
  report.value !== null && report.value.weeks.every(w => w.evaluations.length === 0)
)

const weeksWithEvals = computed(() =>
  report.value?.weeks.filter(w => w.evaluations.length > 0).length ?? 0
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
    const res = await reportsApi.getPeerEvalStudentReport(studentId, startWeek.value, endWeek.value)
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

function formatScore(n: number): string {
  return parseFloat(n.toFixed(2)).toString()
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
.report-table tr.group-start td {
  border-top: 2px solid rgba(0, 0, 0, 0.12);
}
.week-cell  { white-space: nowrap; font-weight: 500; }
.grade-cell { white-space: nowrap; font-weight: 600; }
.name-cell  { white-space: nowrap; }
.comment-cell { max-width: 260px; word-break: break-word; }
</style>
