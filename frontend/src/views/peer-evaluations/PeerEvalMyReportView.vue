<template>
  <v-container max-width="860" class="py-8">
    <h1 class="text-h5 font-weight-bold mb-2">My Peer Evaluation Report</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      Scores and comments received from your teammates. Evaluator identities are not shown.
    </p>

    <!-- Week selector -->
    <v-card variant="outlined" class="mb-6">
      <v-card-text class="pa-4">
        <v-select
          v-model="selectedWeek"
          :items="weekOptions"
          item-title="label"
          item-value="date"
          label="Select a week"
          variant="outlined"
          density="comfortable"
          :loading="loadingWeeks"
          :disabled="loadingWeeks"
          no-data-text="No active weeks available for your section"
          @update:model-value="onWeekSelected"
          hide-details
        />
      </v-card-text>
    </v-card>

    <!-- Alerts -->
    <v-alert v-if="noSectionError" type="info" variant="tonal" class="mb-6">
      You are not enrolled in a section yet.
    </v-alert>

    <!-- Loading report -->
    <div v-if="loadingReport" class="d-flex justify-center py-12">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <!-- No data for selected week -->
    <v-alert
      v-else-if="report && report.evaluatorCount === 0"
      type="info"
      variant="tonal"
      class="mb-6"
    >
      No peer evaluations have been submitted for you this week yet.
      Check back after your teammates have submitted their evaluations.
    </v-alert>

    <!-- ── Report ──────────────────────────────────────────────────────────── -->
    <template v-else-if="report && report.evaluatorCount > 0">

      <!-- Header row: name + evaluator count + grade -->
      <div class="d-flex align-center justify-space-between flex-wrap gap-3 mb-5">
        <div>
          <p class="text-subtitle-1 font-weight-medium">{{ report.evaluateeName }}</p>
          <p class="text-caption text-medium-emphasis">
            Evaluated by {{ report.evaluatorCount }}
            {{ report.evaluatorCount === 1 ? 'teammate' : 'teammates' }}
            (self-evaluation included)
          </p>
        </div>
        <v-card variant="tonal" color="primary" class="pa-4 text-center" min-width="140">
          <div class="text-h5 font-weight-bold">{{ formatGrade(report.grade, report.maxGrade) }}</div>
          <div class="text-caption mt-1">Overall Grade</div>
        </v-card>
      </div>

      <!-- Criterion scores table -->
      <v-card variant="outlined" class="mb-5">
        <v-card-title class="text-body-1 font-weight-medium pa-4 pb-2">
          Rubric Scores
        </v-card-title>

        <v-table density="comfortable">
          <thead>
            <tr>
              <th class="text-left">Criterion</th>
              <th class="text-right">Your Average</th>
              <th class="text-right">Max</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="ca in report.criterionAverages" :key="ca.criterionId">
              <td>{{ ca.criterionName }}</td>
              <td class="text-right font-weight-medium">{{ formatScore(ca.averageScore) }}</td>
              <td class="text-right text-medium-emphasis">{{ formatScore(ca.maxScore) }}</td>
            </tr>
          </tbody>
        </v-table>
      </v-card>

      <!-- Public comments -->
      <v-card variant="outlined">
        <v-card-title class="text-body-1 font-weight-medium pa-4 pb-2">
          Public Comments
        </v-card-title>
        <v-card-text class="pa-4 pt-0">
          <p
            v-if="report.publicComments.length === 0"
            class="text-body-2 text-medium-emphasis"
          >
            No public comments were left for you this week.
          </p>
          <v-list v-else density="compact" lines="one">
            <v-list-item
              v-for="(comment, i) in report.publicComments"
              :key="i"
              :title="comment"
              prepend-icon="mdi-comment-outline"
            />
          </v-list>
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
import { peerEvaluationsApi, type PeerEvalReportResponse } from '@/api/peerEvaluations'

// ── State ─────────────────────────────────────────────────────────────────────

const loadingWeeks  = ref(false)
const loadingReport = ref(false)
const noSectionError = ref(false)

const availableWeeks = ref<string[]>([])
const selectedWeek   = ref<string | null>(null)
const report         = ref<PeerEvalReportResponse | null>(null)

const showError    = ref(false)
const errorMessage = ref('')

// ── Computed ──────────────────────────────────────────────────────────────────

const weekOptions = computed(() =>
  availableWeeks.value.map(date => ({ date, label: formatDate(date) }))
)

// ── Lifecycle ─────────────────────────────────────────────────────────────────

onMounted(async () => {
  loadingWeeks.value = true
  try {
    const res = await peerEvaluationsApi.getAvailableWeeks()
    availableWeeks.value = res.data.data

    // Default to the most recent week (last element — list is sorted ascending).
    if (availableWeeks.value.length > 0) {
      const latest = availableWeeks.value[availableWeeks.value.length - 1]
      selectedWeek.value = latest
      await loadReport(latest)
    }
  } catch (err: any) {
    const msg = err.response?.data?.error || err.response?.data?.message || ''
    if (msg.includes('not enrolled')) {
      noSectionError.value = true
    } else {
      showErrorSnack(msg || 'Failed to load available weeks.')
    }
  } finally {
    loadingWeeks.value = false
  }
})

// ── Handlers ──────────────────────────────────────────────────────────────────

async function onWeekSelected(date: string) {
  if (!date) return
  await loadReport(date)
}

async function loadReport(weekStartDate: string) {
  loadingReport.value = true
  report.value = null
  try {
    const res = await peerEvaluationsApi.getMyReport(weekStartDate)
    report.value = res.data.data
  } catch (err: any) {
    showErrorSnack(
      err.response?.data?.error || err.response?.data?.message || 'Failed to load report.'
    )
  } finally {
    loadingReport.value = false
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

// Strips trailing zeros: 8.50 → "8.5", 10.00 → "10"
function formatScore(n: number): string {
  return parseFloat(n.toFixed(2)).toString()
}

// Displays grade as "X / Y", both stripped of trailing zeros.
function formatGrade(grade: number, maxGrade: number): string {
  if (maxGrade === 0) return '—'
  return `${formatScore(grade)} / ${formatScore(maxGrade)}`
}

function formatDate(date: string) {
  return new Date(date + 'T00:00:00').toLocaleDateString('en-US', {
    month: 'long', day: 'numeric', year: 'numeric',
  })
}

function showErrorSnack(msg: string) {
  errorMessage.value = msg
  showError.value = true
}
</script>
