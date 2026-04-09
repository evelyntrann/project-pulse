<template>
  <v-container max-width="680">
    <!-- Back button -->
    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.push(`/sections/${route.params.id}`)"
    >
      Back to Section
    </v-btn>

    <!-- Loading -->
    <v-row v-if="loading" justify="center" class="mt-8">
      <v-progress-circular indeterminate color="primary" />
    </v-row>

    <!-- Load error -->
    <v-alert v-else-if="loadError" type="error" variant="tonal">{{ loadError }}</v-alert>

    <template v-else>
      <h1 class="text-h5 font-weight-bold mb-6">Edit Section</h1>

      <!-- Section Details -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Section Details</v-card-title>
        <v-card-text class="pa-4">
          <v-form ref="formRef" @submit.prevent>
            <v-text-field
              v-model="form.name"
              label="Section Name"
              variant="outlined"
              density="comfortable"
              :rules="nameRules"
              :error-messages="duplicateError"
              class="mb-3"
              @input="duplicateError = ''"
            />
            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="form.startDate"
                  label="Start Date"
                  type="date"
                  variant="outlined"
                  density="comfortable"
                  :rules="requiredRule"
                />
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="form.endDate"
                  label="End Date"
                  type="date"
                  variant="outlined"
                  density="comfortable"
                  :rules="endDateRules"
                />
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
      </v-card>

      <!-- Rubric -->
      <v-card variant="outlined" class="mb-6">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Peer Evaluation Rubric</v-card-title>
        <v-card-text class="pa-4">
          <!-- TODO: Replace with rubric selector once Angel builds UC-1 rubric package -->
          <v-text-field
            v-model.number="form.rubricId"
            label="Rubric ID"
            type="number"
            variant="outlined"
            density="comfortable"
            :rules="rubricRules"
            hint="Rubric selection UI coming soon — enter rubric ID for now"
            persistent-hint
          />
        </v-card-text>
      </v-card>

      <!-- Actions -->
      <v-row>
        <v-col cols="auto">
          <v-btn variant="outlined" @click="router.push(`/sections/${route.params.id}`)">Cancel</v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn color="primary" @click="openConfirm">Review & Save</v-btn>
        </v-col>
      </v-row>
    </template>

    <!-- Confirm Dialog -->
    <v-dialog v-model="confirmDialog" max-width="480">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Confirm Changes</v-card-title>
        <v-card-text class="pa-6 pt-2">
          <v-list density="compact" lines="two">
            <v-list-item title="Section Name" :subtitle="form.name" prepend-icon="mdi-label-outline" />
            <v-list-item title="Start Date" :subtitle="formatDate(form.startDate)" prepend-icon="mdi-calendar-start" />
            <v-list-item title="End Date" :subtitle="formatDate(form.endDate)" prepend-icon="mdi-calendar-end" />
            <v-list-item title="Rubric ID" :subtitle="String(form.rubricId)" prepend-icon="mdi-clipboard-list-outline" />
          </v-list>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { sectionsApi, type SectionCreateRequest } from '@/api/sections'

const router = useRouter()
const route = useRoute()

const formRef = ref()
const loading = ref(false)
const loadError = ref('')
const confirmDialog = ref(false)
const submitting = ref(false)
const submitError = ref('')
const duplicateError = ref('')

const form = reactive<SectionCreateRequest>({
  name: '',
  startDate: '',
  endDate: '',
  rubricId: 0,
})

const requiredRule = [(v: string) => !!v || 'Required']

const nameRules = [
  (v: string) => !!v || 'Section name is required',
  (v: string) => v.length <= 20 || 'Max 20 characters',
]

const endDateRules = [
  (v: string) => !!v || 'End date is required',
  (v: string) => !form.startDate || v >= form.startDate || 'End date must be after start date',
]

const rubricRules = [
  (v: number) => !!v || 'Rubric ID is required',
  (v: number) => v > 0 || 'Rubric ID must be a positive number',
]

onMounted(async () => {
  loading.value = true
  try {
    const res = await sectionsApi.getSection(Number(route.params.id))
    const s = res.data.data
    form.name = s.name
    form.startDate = s.startDate
    form.endDate = s.endDate
    form.rubricId = s.rubricId ?? 0
  } catch {
    loadError.value = 'Failed to load section details'
  } finally {
    loading.value = false
  }
})

async function openConfirm() {
  const { valid } = await formRef.value.validate()
  if (!valid) return
  submitError.value = ''
  confirmDialog.value = true
}

async function submit() {
  submitting.value = true
  submitError.value = ''
  try {
    await sectionsApi.updateSection(Number(route.params.id), { ...form })
    confirmDialog.value = false
    router.push(`/sections/${route.params.id}`)
  } catch (err: any) {
    const status = err.response?.status
    const message = err.response?.data?.error || err.response?.data?.message
    if (status === 409) {
      duplicateError.value = message || 'A section with this name already exists'
      confirmDialog.value = false
    } else {
      submitError.value = message || 'Failed to save changes. Please try again.'
    }
  } finally {
    submitting.value = false
  }
}

function formatDate(date: string) {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric',
  })
}
</script>
