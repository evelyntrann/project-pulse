<template>
  <v-container max-width="720">
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

    <v-alert v-else-if="loadError" type="error" variant="tonal">{{ loadError }}</v-alert>

    <template v-else>
      <v-row align="center" class="mb-2">
        <v-col>
          <h1 class="text-h5 font-weight-bold">Set Up Active Weeks</h1>
          <p class="text-body-2 text-grey mt-1">{{ sectionName }} &nbsp;·&nbsp; {{ weeks.length }} weeks total</p>
        </v-col>
      </v-row>

      <v-alert type="info" variant="tonal" density="compact" class="mb-4">
        Toggle off weeks when students do <strong>not</strong> need to submit WARs or peer evaluations (e.g. holidays, breaks).
      </v-alert>

      <!-- Week list -->
      <v-card variant="outlined" class="mb-6">
        <v-list density="compact">
          <template v-for="(week, index) in weeks" :key="week.weekStartDate">
            <v-divider v-if="index > 0" />
            <v-list-item :class="{ 'bg-grey-lighten-4': !week.isActive }">
              <template #prepend>
                <v-switch
                  v-model="week.isActive"
                  color="primary"
                  hide-details
                  density="compact"
                  class="mr-2"
                />
              </template>
              <v-list-item-title :class="week.isActive ? '' : 'text-grey text-decoration-line-through'">
                Week {{ index + 1 }} &nbsp;·&nbsp; {{ formatDate(week.weekStartDate) }}
              </v-list-item-title>
              <template #append>
                <v-chip
                  :color="week.isActive ? 'success' : 'default'"
                  size="x-small"
                  variant="tonal"
                >
                  {{ week.isActive ? 'Active' : 'Inactive' }}
                </v-chip>
              </template>
            </v-list-item>
          </template>
        </v-list>
      </v-card>

      <!-- Summary -->
      <v-row class="mb-6">
        <v-col cols="6" sm="3">
          <v-card variant="tonal" color="success" class="pa-3 text-center">
            <div class="text-h6 font-weight-bold">{{ activeCount }}</div>
            <div class="text-caption">Active Weeks</div>
          </v-card>
        </v-col>
        <v-col cols="6" sm="3">
          <v-card variant="tonal" color="default" class="pa-3 text-center">
            <div class="text-h6 font-weight-bold">{{ weeks.length - activeCount }}</div>
            <div class="text-caption">Inactive Weeks</div>
          </v-card>
        </v-col>
      </v-row>

      <!-- Actions -->
      <v-row>
        <v-col cols="auto">
          <v-btn variant="outlined" @click="router.push(`/sections/${route.params.id}`)">Cancel</v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn color="primary" @click="confirmDialog = true">Review & Save</v-btn>
        </v-col>
      </v-row>
    </template>

    <!-- Confirm Dialog -->
    <v-dialog v-model="confirmDialog" max-width="440">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Confirm Active Weeks</v-card-title>
        <v-card-text class="pa-6 pt-2">
          <p class="text-body-2 mb-4">
            <strong>{{ activeCount }}</strong> active weeks and
            <strong>{{ weeks.length - activeCount }}</strong> inactive weeks will be saved for <strong>{{ sectionName }}</strong>.
          </p>
          <v-alert v-if="submitError" type="error" variant="tonal" density="compact">
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
import { sectionsApi, type ActiveWeek } from '@/api/sections'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const loadError = ref('')
const confirmDialog = ref(false)
const submitting = ref(false)
const submitError = ref('')
const sectionName = ref('')
const weeks = ref<ActiveWeek[]>([])

const activeCount = computed(() => weeks.value.filter(w => w.isActive).length)

onMounted(async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)

    // Load section to get name + date range for generating weeks
    const sectionRes = await sectionsApi.getSection(id)
    const section = sectionRes.data.data
    sectionName.value = section.name

    // Generate all Mondays between start and end date
    const generated = generateMondays(section.startDate, section.endDate)

    // Load saved active weeks and overlay onto generated list
    const savedRes = await sectionsApi.getActiveWeeks(id)
    const savedMap = new Map(savedRes.data.data.map(w => [w.weekStartDate, w.isActive]))

    weeks.value = generated.map(date => ({
      weekStartDate: date,
      isActive: savedMap.has(date) ? savedMap.get(date)! : true,
    }))
  } catch {
    loadError.value = 'Failed to load section data'
  } finally {
    loading.value = false
  }
})

function generateMondays(startDate: string, endDate: string): string[] {
  const mondays: string[] = []
  const end = new Date(endDate)

  // Find first Monday on or after startDate
  const current = new Date(startDate)
  const day = current.getDay()
  if (day !== 1) {
    current.setDate(current.getDate() + ((8 - day) % 7))
  }

  while (current <= end) {
    mondays.push(current.toISOString().slice(0, 10))
    current.setDate(current.getDate() + 7)
  }
  return mondays
}

async function submit() {
  submitting.value = true
  submitError.value = ''
  try {
    await sectionsApi.setActiveWeeks(Number(route.params.id), weeks.value)
    confirmDialog.value = false
    router.push(`/sections/${route.params.id}?saved=weeks`)
  } catch {
    submitError.value = 'Failed to save active weeks. Please try again.'
  } finally {
    submitting.value = false
  }
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
  })
}
</script>
