<template>
  <v-container max-width="1100" class="py-8">
    <h1 class="text-h5 font-weight-bold mb-2">Section Peer Evaluation Report</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      Scores and comments received by each student this week, including evaluator identities.
    </p>

    <!-- Section + week selectors -->
    <v-card variant="outlined" class="mb-6">
      <v-card-text class="pa-4">
        <v-row dense>
          <v-col cols="12" sm="5">
            <v-select
              v-model="selectedSectionId"
              :items="sections"
              item-title="name"
              item-value="id"
              label="Section"
              variant="outlined"
              density="comfortable"
              :loading="loadingSections"
              :disabled="loadingSections"
              no-data-text="No sections found"
              @update:model-value="onSectionSelected"
              hide-details
            />
          </v-col>
          <v-col cols="12" sm="5">
            <v-select
              v-model="selectedWeek"
              :items="weekOptions"
              item-title="label"
              item-value="date"
              label="Select a week"
              variant="outlined"
              density="comfortable"
              :loading="loadingWeeks"
              :disabled="loadingWeeks || !selectedSectionId"
              no-data-text="No active weeks available"
              @update:model-value="onWeekSelected"
              hide-details
            />
          </v-col>
          <v-col cols="12" sm="2" class="d-flex align-center">
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

    <!-- No section enrolled -->
    <v-alert v-if="noSectionError" type="info" variant="tonal" class="mb-6">
      You are not enrolled in any section yet.
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
      No peer evaluations have been submitted for this section this week yet.
    </v-alert>

    <!-- ── Report ──────────────────────────────────────────────────────────── -->
    <template v-else-if="report">

      <!-- Summary row -->
      <div class="d-flex align-center gap-4 mb-5 flex-wrap">
        <div>
          <p class="text-subtitle-1 font-weight-medium">{{ report.sectionName }}</p>
          <p class="text-caption text-medium-emphasis">
            Week of {{ formatDate(report.weekStartDate) }} &mdash;
            {{ report.students.length }} students
          </p>
        </div>
        <v-chip color="primary" variant="tonal" size="small">
          Max grade: {{ formatScore(report.maxGrade) }} pts
        </v-chip>
      </div>

      <!-- Main report table -->
      <v-card variant="outlined" class="mb-5">
        <v-card-title class="text-body-1 font-weight-medium pa-4 pb-2">
          Peer Evaluation Results
        </v-card-title>

        <div class="overflow-x-auto">
          <table class="report-table">
            <thead>
              <tr>
                <th>Student</th>
                <th>Grade</th>
                <th>Evaluated By</th>
                <th>Public Comment</th>
                <th>Private Comment</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="student in report.students" :key="student.studentId">
                <!-- Student has evaluations -->
                <template v-if="student.evaluations.length > 0">
                  <tr
                    v-for="(ev, i) in student.evaluations"
                    :key="ev.evaluatorName"
                    :class="{ 'group-start': i === 0 }"
                  >
                    <td v-if="i === 0" :rowspan="student.evaluations.length" class="student-name-cell">
                      <div class="font-weight-medium">{{ student.studentName }}</div>
                      <div class="text-caption text-medium-emphasis">
                        {{ formatScore(student.grade) }} / {{ formatScore(report.maxGrade) }}
                      </div>
                    </td>
                    <td v-if="i === 0" :rowspan="student.evaluations.length" class="grade-cell">
                      {{ formatScore(student.grade) }} / {{ formatScore(report.maxGrade) }}
                    </td>
                    <td>{{ ev.evaluatorName }}</td>
                    <td class="comment-cell">{{ ev.publicComment || '—' }}</td>
                    <td class="comment-cell">{{ ev.privateComment || '—' }}</td>
                  </tr>
                </template>
                <!-- No evaluations yet -->
                <tr v-else class="group-start">
                  <td class="student-name-cell font-weight-medium">{{ student.studentName }}</td>
                  <td class="text-medium-emphasis">—</td>
                  <td colspan="3" class="text-medium-emphasis text-caption">
                    No evaluations received yet
                  </td>
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
          <v-chip size="x-small" class="ml-2" :color="report.nonSubmitters.length > 0 ? 'warning' : 'success'" variant="tonal">
            {{ report.nonSubmitters.length }}
          </v-chip>
        </v-card-title>
        <v-card-text class="pa-4 pt-0">
          <p v-if="report.nonSubmitters.length === 0" class="text-body-2 text-medium-emphasis">
            All students submitted their peer evaluations this week.
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
import { reportsApi, type PeerEvalSectionReport, type InstructorSection } from '@/api/reports'

// ── State ─────────────────────────────────────────────────────────────────────

const loadingSections = ref(false)
const loadingWeeks    = ref(false)
const loadingReport   = ref(false)
const noSectionError  = ref(false)

const sections          = ref<InstructorSection[]>([])
const selectedSectionId = ref<number | null>(null)
const availableWeeks    = ref<string[]>([])
const selectedWeek      = ref<string | null>(null)
const report            = ref<PeerEvalSectionReport | null>(null)

const showError    = ref(false)
const errorMessage = ref('')

// ── Computed ──────────────────────────────────────────────────────────────────

const weekOptions = computed(() =>
  availableWeeks.value.map(date => ({ date, label: formatDate(date) }))
)

const hasNoData = computed(() =>
  report.value !== null &&
  report.value.students.every(s => s.evaluations.length === 0)
)

// ── Lifecycle ─────────────────────────────────────────────────────────────────

onMounted(async () => {
  loadingSections.value = true
  try {
    const res = await reportsApi.getInstructorSections()
    sections.value = res.data.data
    if (sections.value.length === 0) {
      noSectionError.value = true
      return
    }
    const first = sections.value[0]!
    selectedSectionId.value = first.id
    await loadWeeks(first.id)
  } catch (err: any) {
    showErrorSnack(err.response?.data?.error || 'Failed to load sections.')
  } finally {
    loadingSections.value = false
  }
})

// ── Handlers ──────────────────────────────────────────────────────────────────

async function onSectionSelected(sectionId: number) {
  selectedWeek.value = null
  report.value = null
  availableWeeks.value = []
  await loadWeeks(sectionId)
}

async function onWeekSelected(date: string) {
  if (!date || !selectedSectionId.value) return
  await loadReport()
}

async function loadWeeks(sectionId: number) {
  loadingWeeks.value = true
  try {
    const res = await reportsApi.getSectionAvailableWeeks(sectionId)
    availableWeeks.value = res.data.data
    if (availableWeeks.value.length > 0) {
      const latest = availableWeeks.value[availableWeeks.value.length - 1]!
      selectedWeek.value = latest
      await loadReport()
    }
  } catch (err: any) {
    showErrorSnack(err.response?.data?.error || 'Failed to load available weeks.')
  } finally {
    loadingWeeks.value = false
  }
}

async function loadReport() {
  if (!selectedSectionId.value || !selectedWeek.value) return
  loadingReport.value = true
  report.value = null
  try {
    const res = await reportsApi.getPeerEvalSectionReport(selectedSectionId.value, selectedWeek.value)
    report.value = res.data.data
  } catch (err: any) {
    showErrorSnack(err.response?.data?.error || 'Failed to load report.')
  } finally {
    loadingReport.value = false
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function formatScore(n: number): string {
  return parseFloat(n.toFixed(2)).toString()
}

function formatDate(date: string): string {
  return new Date(date + 'T00:00:00').toLocaleDateString('en-US', {
    month: 'long', day: 'numeric', year: 'numeric',
  })
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

.grade-cell {
  white-space: nowrap;
  font-weight: 600;
}

.comment-cell {
  max-width: 260px;
  word-break: break-word;
}
</style>
