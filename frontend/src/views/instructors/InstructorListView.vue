<template>
  <v-container max-width="900">
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">Find Instructors</h1>
      <v-spacer />
      <v-btn variant="outlined" class="mr-2" prepend-icon="mdi-account-group" @click="router.push('/instructors/assign')">
        Assign to Teams
      </v-btn>
      <v-btn color="primary" prepend-icon="mdi-link-variant" :loading="generating" @click="generateLink">
        Generate Invite Link
      </v-btn>
    </div>

    <!-- Search Form -->
    <v-card variant="outlined" class="mb-4">
      <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Search Criteria</v-card-title>
      <v-card-text class="pa-4">
        <v-row dense>
          <v-col cols="12" sm="4">
            <v-text-field
              v-model="filters.firstName"
              label="First Name"
              variant="outlined"
              density="comfortable"
              clearable
              @keyup.enter="search"
            />
          </v-col>
          <v-col cols="12" sm="4">
            <v-text-field
              v-model="filters.lastName"
              label="Last Name"
              variant="outlined"
              density="comfortable"
              clearable
              @keyup.enter="search"
            />
          </v-col>
          <v-col cols="12" sm="4">
            <v-text-field
              v-model="filters.teamName"
              label="Team Name"
              variant="outlined"
              density="comfortable"
              clearable
              @keyup.enter="search"
            />
          </v-col>
          <v-col cols="12" sm="4">
            <v-select
              v-model="filters.status"
              label="Status"
              :items="statusOptions"
              item-title="label"
              item-value="value"
              variant="outlined"
              density="comfortable"
              clearable
            />
          </v-col>
        </v-row>

        <v-row class="mt-1">
          <v-col cols="auto">
            <v-btn color="primary" prepend-icon="mdi-magnify" :loading="loading" @click="search">
              Search
            </v-btn>
          </v-col>
          <v-col cols="auto">
            <v-btn variant="outlined" @click="clearAndSearch">Clear</v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- Error -->
    <v-alert v-if="error" type="error" variant="tonal" density="compact" class="mb-4">
      {{ error }}
    </v-alert>

    <!-- Results -->
    <template v-if="searched">

      <!-- Empty state -->
      <v-alert v-if="results.length === 0" type="info" variant="tonal" class="mb-4">
        <div class="font-weight-medium mb-1">No matching instructors found.</div>
        <div class="text-body-2">
          Try adjusting your search criteria, or
          <v-btn variant="text" size="small" density="compact" class="px-1" :loading="generating" @click="generateLink">
            generate an invite link
          </v-btn>
          to invite a new instructor.
        </div>
      </v-alert>

      <!-- Results table -->
      <v-card v-else variant="outlined">
        <v-card-title class="text-body-2 text-medium-emphasis pa-4 pb-0">
          {{ results.length }} instructor{{ results.length !== 1 ? 's' : '' }} found
        </v-card-title>
        <v-table>
          <thead>
            <tr>
              <th class="text-left">First Name</th>
              <th class="text-left">Last Name</th>
              <th class="text-left">Email</th>
              <th class="text-left">Teams</th>
              <th class="text-left">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="inst in results"
              :key="inst.id"
              class="cursor-pointer"
              @click="router.push(`/instructors/${inst.id}`)"
            >
              <td>{{ inst.firstName }}</td>
              <td>{{ inst.lastName }}</td>
              <td class="text-medium-emphasis">{{ inst.email }}</td>
              <td class="text-medium-emphasis">{{ inst.teamNames || '—' }}</td>
              <td>
                <v-chip :color="inst.active ? 'success' : 'default'" size="small" variant="tonal">
                  {{ inst.active ? 'Active' : 'Deactivated' }}
                </v-chip>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-card>

    </template>

    <!-- Shareable Link Dialog -->
    <v-dialog v-model="linkDialog" max-width="540">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Instructor Invite Link</v-card-title>
        <v-card-text class="pa-6 pt-0">
          <p class="text-body-2 text-medium-emphasis mb-4">
            Share this link with the instructor. It expires on
            <strong>{{ expiresFormatted }}</strong>.
          </p>
          <v-text-field
            :model-value="generatedLink"
            readonly
            variant="outlined"
            density="comfortable"
            append-inner-icon="mdi-content-copy"
            @click:append-inner="copyLink"
          />
          <v-alert v-if="copied" type="success" variant="tonal" density="compact" class="mt-2">
            Link copied to clipboard!
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-spacer />
          <v-btn variant="outlined" @click="linkDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Success snackbar -->
    <v-snackbar v-model="snackbar" color="success" timeout="3000" location="bottom">
      {{ snackbarMessage }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { instructorsApi, type InstructorSearchResult } from '@/api/instructors'

const router = useRouter()

// ── Search ───────────────────────────────────────────────────────────────────
const statusOptions = [
  { label: 'Active',      value: true  },
  { label: 'Deactivated', value: false },
]

const filters = reactive({
  firstName: '',
  lastName: '',
  teamName: '',
  status: null as boolean | null,
})

const results = ref<InstructorSearchResult[]>([])
const loading = ref(false)
const error = ref('')
const searched = ref(false)

async function search() {
  loading.value = true
  error.value = ''
  try {
    const res = await instructorsApi.searchInstructors({
      firstName: filters.firstName || undefined,
      lastName:  filters.lastName  || undefined,
      teamName:  filters.teamName  || undefined,
      active:    filters.status    ?? undefined,
    })
    results.value = res.data.data
    searched.value = true
  } catch {
    error.value = 'Failed to search instructors. Please try again.'
  } finally {
    loading.value = false
  }
}

function clearAndSearch() {
  filters.firstName = ''
  filters.lastName  = ''
  filters.teamName  = ''
  filters.status    = null
  search()
}

// ── Invite link ───────────────────────────────────────────────────────────────
const generating = ref(false)
const generateError = ref('')
const linkDialog = ref(false)
const generatedLink = ref('')
const expiresAt = ref('')
const copied = ref(false)

const expiresFormatted = computed(() => {
  if (!expiresAt.value) return ''
  return new Date(expiresAt.value).toLocaleString()
})

async function generateLink() {
  generating.value = true
  generateError.value = ''
  try {
    const res = await instructorsApi.generateInstructorLink()
    generatedLink.value = res.data.data.shareableLink
    expiresAt.value = res.data.data.expiresAt
    copied.value = false
    linkDialog.value = true
  } catch {
    generateError.value = 'Failed to generate invite link. Please try again.'
  } finally {
    generating.value = false
  }
}

async function copyLink() {
  await navigator.clipboard.writeText(generatedLink.value)
  copied.value = true
}

// ── Snackbar (after assign redirect) ─────────────────────────────────────────
const snackbar = ref(false)
const snackbarMessage = ref('')

onMounted(() => {
  const msg = (history.state as any)?.successMessage
  if (msg) {
    snackbarMessage.value = msg
    snackbar.value = true
  }
})
</script>
