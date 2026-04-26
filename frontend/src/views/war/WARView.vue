<template>
  <v-container max-width="1100" class="py-8">
    <h1 class="text-h5 font-weight-bold mb-6">Weekly Activity Report</h1>

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

    <!-- No section enrolled message -->
    <v-alert v-if="noSectionError" type="info" variant="tonal" class="mb-6">
      You are not enrolled in a section yet. Ask your admin for an invite link.
    </v-alert>

    <!-- WAR panel -->
    <template v-if="currentWAR">

      <!-- Status bar -->
      <div class="d-flex align-center justify-space-between mb-4">
        <div class="d-flex align-center gap-3">
          <span class="text-subtitle-1 font-weight-medium">
            Week of {{ formatDate(currentWAR.weekStartDate) }}
          </span>
          <v-chip
            :color="currentWAR.status === 'SUBMITTED' ? 'success' : 'warning'"
            size="small"
            variant="tonal"
          >
            {{ currentWAR.status }}
          </v-chip>
        </div>
        <div class="d-flex gap-2">
          <v-btn
            v-if="currentWAR.status === 'DRAFT'"
            color="primary"
            variant="outlined"
            prepend-icon="mdi-plus"
            @click="openAddDialog"
          >
            Add Activity
          </v-btn>
          <v-btn
            v-if="currentWAR.status === 'DRAFT'"
            color="success"
            :disabled="currentWAR.activities.length === 0"
            @click="confirmSubmitDialog = true"
          >
            Submit WAR
          </v-btn>
        </div>
      </div>

      <!-- Activities table -->
      <v-card variant="outlined">
        <v-data-table
          :headers="tableHeaders"
          :items="currentWAR.activities"
          :items-per-page="25"
          no-data-text="No activities yet. Add your first activity above."
        >
          <template #item.plannedHours="{ item }">
            {{ item.plannedHours }} hrs
          </template>
          <template #item.actualHours="{ item }">
            {{ item.actualHours != null ? `${item.actualHours} hrs` : '—' }}
          </template>
          <template #item.status="{ item }">
            <v-chip :color="statusColor(item.status)" size="small" variant="tonal">
              {{ formatStatus(item.status) }}
            </v-chip>
          </template>
          <template #item.actions="{ item }">
            <div v-if="currentWAR!.status === 'DRAFT'" class="d-flex gap-1">
              <v-btn icon size="small" variant="text" color="primary" @click="openEditDialog(item)">
                <v-icon>mdi-pencil</v-icon>
              </v-btn>
              <v-btn icon size="small" variant="text" color="error" @click="openDeleteDialog(item)">
                <v-icon>mdi-delete</v-icon>
              </v-btn>
            </div>
          </template>
        </v-data-table>
      </v-card>

      <p v-if="currentWAR.submittedAt" class="text-caption text-medium-emphasis mt-3">
        Submitted on {{ formatDateTime(currentWAR.submittedAt) }}
      </p>
    </template>

    <!-- Loading WAR -->
    <div v-else-if="loadingWAR" class="d-flex justify-center py-12">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <!-- ── Add / Edit Activity Dialog ─────────────────────────────────────── -->
    <v-dialog v-model="activityDialog" max-width="600" persistent>
      <v-card>
        <v-card-title class="pa-5 pb-0">
          {{ editingActivity ? 'Edit Activity' : 'Add Activity' }}
        </v-card-title>

        <!-- Step 1: Form -->
        <v-card-text v-if="dialogStep === 'form'" class="pa-5">
          <v-form ref="activityFormRef" @submit.prevent="goToActivityReview">

            <v-select
              v-model="activityForm.category"
              :items="CATEGORIES"
              label="Category"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[v => !!v || 'Category is required']"
            />

            <v-text-field
              v-model="activityForm.plannedActivity"
              label="Planned Activity"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[v => !!v?.trim() || 'Planned activity is required']"
            />

            <v-textarea
              v-model="activityForm.description"
              label="Description (optional)"
              variant="outlined"
              density="comfortable"
              rows="2"
              class="mb-3"
              hide-details
            />

            <div class="d-flex gap-3 mb-3">
              <v-text-field
                v-model="activityForm.plannedHours"
                label="Planned Hours"
                type="number"
                min="0"
                step="0.5"
                variant="outlined"
                density="comfortable"
                :rules="[v => (v !== '' && v !== null && Number(v) >= 0) || 'Required, must be ≥ 0']"
              />
              <v-text-field
                v-model="activityForm.actualHours"
                label="Actual Hours (optional)"
                type="number"
                min="0"
                step="0.5"
                variant="outlined"
                density="comfortable"
                hide-details
              />
            </div>

            <v-select
              v-model="activityForm.status"
              :items="STATUS_OPTIONS"
              item-title="label"
              item-value="value"
              label="Status"
              variant="outlined"
              density="comfortable"
              hide-details
            />

          </v-form>
        </v-card-text>

        <!-- Step 2: Review -->
        <v-card-text v-else class="pa-5">
          <p class="text-subtitle-2 font-weight-medium mb-3">Confirm activity details</p>
          <v-list lines="two" density="compact" variant="outlined" rounded="lg">
            <v-list-item title="Category" :subtitle="activityForm.category" />
            <v-divider />
            <v-list-item title="Planned Activity" :subtitle="activityForm.plannedActivity" />
            <v-divider />
            <v-list-item
              title="Description"
              :subtitle="activityForm.description?.trim() || '—'"
            />
            <v-divider />
            <v-list-item
              title="Hours"
              :subtitle="`Planned: ${activityForm.plannedHours} hrs  |  Actual: ${activityForm.actualHours || '—'} ${activityForm.actualHours ? 'hrs' : ''}`"
            />
            <v-divider />
            <v-list-item
              title="Status"
              :subtitle="formatStatus(activityForm.status)"
            />
          </v-list>

          <v-alert v-if="activityError" type="error" variant="tonal" density="compact" class="mt-4">
            {{ activityError }}
          </v-alert>
        </v-card-text>

        <v-card-actions class="px-5 pb-5 gap-2">
          <v-btn
            v-if="dialogStep === 'form'"
            variant="outlined"
            @click="closeActivityDialog"
          >
            Cancel
          </v-btn>
          <v-btn
            v-if="dialogStep === 'form'"
            color="primary"
            class="ml-auto"
            @click="goToActivityReview"
          >
            Review
          </v-btn>

          <v-btn
            v-if="dialogStep === 'review'"
            variant="outlined"
            :disabled="savingActivity"
            @click="dialogStep = 'form'"
          >
            Back
          </v-btn>
          <v-btn
            v-if="dialogStep === 'review'"
            color="primary"
            class="ml-auto"
            :loading="savingActivity"
            @click="saveActivity"
          >
            {{ editingActivity ? 'Save Changes' : 'Add Activity' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- ── Delete Confirm Dialog ────────────────────────────────────────────── -->
    <v-dialog v-model="deleteDialog" max-width="420">
      <v-card>
        <v-card-title class="pa-5 pb-2">Delete Activity</v-card-title>
        <v-card-text class="px-5">
          Are you sure you want to delete
          <strong>{{ deletingActivity?.plannedActivity }}</strong>?
          This cannot be undone.
        </v-card-text>
        <v-card-actions class="px-5 pb-5 gap-2">
          <v-btn variant="outlined" @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" class="ml-auto" :loading="deletingLoading" @click="deleteActivity">
            Delete
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- ── Submit Confirm Dialog ────────────────────────────────────────────── -->
    <v-dialog v-model="confirmSubmitDialog" max-width="420">
      <v-card>
        <v-card-title class="pa-5 pb-2">Submit WAR</v-card-title>
        <v-card-text class="px-5">
          Submit your WAR for the week of
          <strong>{{ currentWAR ? formatDate(currentWAR.weekStartDate) : '' }}</strong>?
          You will not be able to edit it after submission.
        </v-card-text>
        <v-card-actions class="px-5 pb-5 gap-2">
          <v-btn variant="outlined" @click="confirmSubmitDialog = false">Cancel</v-btn>
          <v-btn color="success" class="ml-auto" :loading="submittingWAR" @click="submitWAR">
            Submit
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Global error snackbar -->
    <v-snackbar v-model="showError" color="error" :timeout="4000" location="top">
      {{ errorMessage }}
    </v-snackbar>

    <!-- Success snackbar -->
    <v-snackbar v-model="showSuccess" color="success" :timeout="3000" location="top">
      {{ successMessage }}
    </v-snackbar>

  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { warsApi, type WARDetail, type ActivityResponse } from '@/api/wars'

// ── Constants ────────────────────────────────────────────────────────────────

const CATEGORIES = ['Design', 'Development', 'Testing', 'Documentation', 'Meeting', 'Research', 'Other']

const STATUS_OPTIONS = [
  { label: 'In Progress', value: 'IN_PROGRESS' },
  { label: 'Under Testing', value: 'UNDER_TESTING' },
  { label: 'Done', value: 'DONE' },
]

const tableHeaders = [
  { title: 'Category', key: 'category', width: '130px' },
  { title: 'Planned Activity', key: 'plannedActivity' },
  { title: 'Planned Hrs', key: 'plannedHours', width: '120px' },
  { title: 'Actual Hrs', key: 'actualHours', width: '110px' },
  { title: 'Status', key: 'status', width: '140px' },
  { title: '', key: 'actions', sortable: false, width: '80px' },
]

// ── State ────────────────────────────────────────────────────────────────────

const loadingWeeks = ref(false)
const loadingWAR = ref(false)
const noSectionError = ref(false)

const availableWeeks = ref<string[]>([])
const existingWARs = ref<{ id: number; weekStartDate: string; status: string; submittedAt: string | null }[]>([])
const selectedWeek = ref<string | null>(null)
const currentWAR = ref<WARDetail | null>(null)

// Activity dialog
const activityDialog = ref(false)
const dialogStep = ref<'form' | 'review'>('form')
const activityFormRef = ref()
const savingActivity = ref(false)
const activityError = ref('')
const editingActivity = ref<ActivityResponse | null>(null)

const activityForm = reactive({
  category: '',
  plannedActivity: '',
  description: '',
  plannedHours: '' as string | number,
  actualHours: '' as string | number,
  status: 'IN_PROGRESS',
})

// Delete dialog
const deleteDialog = ref(false)
const deletingActivity = ref<ActivityResponse | null>(null)
const deletingLoading = ref(false)

// Submit dialog
const confirmSubmitDialog = ref(false)
const submittingWAR = ref(false)

// Snackbars
const showError = ref(false)
const errorMessage = ref('')
const showSuccess = ref(false)
const successMessage = ref('')

// ── Computed ─────────────────────────────────────────────────────────────────

const weekOptions = computed(() => {
  return availableWeeks.value.map(date => {
    const existing = existingWARs.value.find(w => w.weekStartDate === date)
    return {
      date,
      label: existing
        ? `${formatDate(date)} — ${existing.status}`
        : formatDate(date),
    }
  })
})

// ── Lifecycle ────────────────────────────────────────────────────────────────

onMounted(async () => {
  loadingWeeks.value = true
  noSectionError.value = false
  try {
    const [weeksRes, warsRes] = await Promise.all([
      warsApi.getAvailableWeeks(),
      warsApi.getWARs(),
    ])
    availableWeeks.value = weeksRes.data.data
    existingWARs.value = warsRes.data.data
  } catch (err: any) {
    const msg = err.response?.data?.error || err.response?.data?.message || ''
    if (msg.includes('not enrolled')) {
      noSectionError.value = true
    } else {
      showErrorMessage(msg || 'Failed to load available weeks.')
    }
  } finally {
    loadingWeeks.value = false
  }
})

// ── Handlers ─────────────────────────────────────────────────────────────────

async function onWeekSelected(date: string) {
  if (!date) return
  loadingWAR.value = true
  currentWAR.value = null
  try {
    const res = await warsApi.createWAR(date)
    currentWAR.value = res.data.data
    // Refresh the WAR list so the week label updates
    const warsRes = await warsApi.getWARs()
    existingWARs.value = warsRes.data.data
  } catch (err: any) {
    const msg = err.response?.data?.error || err.response?.data?.message || ''
    showErrorMessage(msg || 'Failed to load WAR for this week.')
    selectedWeek.value = null
  } finally {
    loadingWAR.value = false
  }
}

// ── Activity dialog ───────────────────────────────────────────────────────────

function openAddDialog() {
  editingActivity.value = null
  activityForm.category = ''
  activityForm.plannedActivity = ''
  activityForm.description = ''
  activityForm.plannedHours = ''
  activityForm.actualHours = ''
  activityForm.status = 'IN_PROGRESS'
  activityError.value = ''
  dialogStep.value = 'form'
  activityDialog.value = true
}

function openEditDialog(activity: ActivityResponse) {
  editingActivity.value = activity
  activityForm.category = activity.category
  activityForm.plannedActivity = activity.plannedActivity
  activityForm.description = activity.description ?? ''
  activityForm.plannedHours = activity.plannedHours
  activityForm.actualHours = activity.actualHours ?? ''
  activityForm.status = activity.status ?? 'IN_PROGRESS'
  activityError.value = ''
  dialogStep.value = 'form'
  activityDialog.value = true
}

function closeActivityDialog() {
  activityDialog.value = false
  editingActivity.value = null
}

async function goToActivityReview() {
  const { valid } = await activityFormRef.value.validate()
  if (!valid) return
  activityError.value = ''
  dialogStep.value = 'review'
}

async function saveActivity() {
  savingActivity.value = true
  activityError.value = ''
  try {
    const payload = {
      category: activityForm.category,
      plannedActivity: activityForm.plannedActivity,
      description: activityForm.description?.trim() || null,
      plannedHours: Number(activityForm.plannedHours),
      actualHours: activityForm.actualHours !== '' && activityForm.actualHours !== null
        ? Number(activityForm.actualHours)
        : null,
      status: activityForm.status || 'IN_PROGRESS',
    }
    let res
    if (editingActivity.value) {
      res = await warsApi.updateActivity(currentWAR.value!.id, editingActivity.value.id, payload)
      successMessage.value = 'Activity updated.'
    } else {
      res = await warsApi.addActivity(currentWAR.value!.id, payload)
      successMessage.value = 'Activity added.'
    }
    currentWAR.value = res.data.data
    activityDialog.value = false
    showSuccess.value = true
  } catch (err: any) {
    activityError.value = err.response?.data?.error
      || err.response?.data?.message
      || 'Failed to save activity.'
    dialogStep.value = 'form'
  } finally {
    savingActivity.value = false
  }
}

// ── Delete ────────────────────────────────────────────────────────────────────

function openDeleteDialog(activity: ActivityResponse) {
  deletingActivity.value = activity
  deleteDialog.value = true
}

async function deleteActivity() {
  if (!deletingActivity.value || !currentWAR.value) return
  deletingLoading.value = true
  try {
    const res = await warsApi.deleteActivity(currentWAR.value.id, deletingActivity.value.id)
    currentWAR.value = res.data.data
    deleteDialog.value = false
    successMessage.value = 'Activity deleted.'
    showSuccess.value = true
  } catch (err: any) {
    showErrorMessage(err.response?.data?.error || 'Failed to delete activity.')
    deleteDialog.value = false
  } finally {
    deletingLoading.value = false
  }
}

// ── Submit ────────────────────────────────────────────────────────────────────

async function submitWAR() {
  if (!currentWAR.value) return
  submittingWAR.value = true
  try {
    const res = await warsApi.submitWAR(currentWAR.value.id)
    currentWAR.value = res.data.data
    // Update the list so the week label shows SUBMITTED
    const warsRes = await warsApi.getWARs()
    existingWARs.value = warsRes.data.data
    confirmSubmitDialog.value = false
    successMessage.value = 'WAR submitted successfully.'
    showSuccess.value = true
  } catch (err: any) {
    showErrorMessage(err.response?.data?.error || 'Failed to submit WAR.')
    confirmSubmitDialog.value = false
  } finally {
    submittingWAR.value = false
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function formatDate(date: string) {
  return new Date(date + 'T00:00:00').toLocaleDateString('en-US', {
    month: 'long', day: 'numeric', year: 'numeric',
  })
}

function formatDateTime(dt: string) {
  return new Date(dt).toLocaleString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
    hour: 'numeric', minute: '2-digit',
  })
}

function formatStatus(status: string | null) {
  switch (status) {
    case 'IN_PROGRESS': return 'In Progress'
    case 'UNDER_TESTING': return 'Under Testing'
    case 'DONE': return 'Done'
    default: return status ?? '—'
  }
}

function statusColor(status: string | null) {
  switch (status) {
    case 'IN_PROGRESS': return 'info'
    case 'UNDER_TESTING': return 'warning'
    case 'DONE': return 'success'
    default: return 'default'
  }
}

function showErrorMessage(msg: string) {
  errorMessage.value = msg
  showError.value = true
}
</script>
