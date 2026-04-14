<template>
  <v-container max-width="900">
    <h1 class="text-h5 font-weight-bold mb-6">Find Students</h1>

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
              v-model="filters.email"
              label="Email"
              variant="outlined"
              density="comfortable"
              clearable
              @keyup.enter="search"
            />
          </v-col>
          <v-col cols="12" sm="6">
            <v-text-field
              v-model="filters.sectionName"
              label="Section Name"
              variant="outlined"
              density="comfortable"
              clearable
              @keyup.enter="search"
            />
          </v-col>
          <v-col cols="12" sm="6">
            <v-text-field
              v-model="filters.teamName"
              label="Team Name"
              variant="outlined"
              density="comfortable"
              clearable
              @keyup.enter="search"
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

      <!-- Empty state — extension 4a -->
      <v-alert
        v-if="results.length === 0"
        type="info"
        variant="tonal"
        class="mb-4"
      >
        <div class="font-weight-medium mb-1">No matching students found.</div>
        <div class="text-body-2">
          Try adjusting your search criteria, or
          <v-btn
            v-if="isAdmin"
            variant="text"
            size="small"
            density="compact"
            class="px-1"
            @click="router.push('/sections')"
          >
            invite students to a section
          </v-btn>
          <span v-else>contact an admin to invite students.</span>
        </div>
      </v-alert>

      <!-- Results table -->
      <v-card v-else variant="outlined">
        <v-card-title class="text-body-2 text-medium-emphasis pa-4 pb-0">
          {{ results.length }} student{{ results.length !== 1 ? 's' : '' }} found
        </v-card-title>
        <v-table>
          <thead>
            <tr>
              <th class="text-left">Section</th>
              <th class="text-left">First Name</th>
              <th class="text-left">Last Name</th>
              <th class="text-left">Team</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="student in results" :key="student.id">
              <td class="text-medium-emphasis">{{ student.sectionName ?? '—' }}</td>
              <td>{{ student.firstName }}</td>
              <td>{{ student.lastName }}</td>
              <td class="text-medium-emphasis">{{ student.teamName ?? '—' }}</td>
            </tr>
          </tbody>
        </v-table>
      </v-card>

    </template>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { studentsApi, type StudentSearchResult } from '@/api/students'

const router = useRouter()
const auth = useAuthStore()

const isAdmin = computed(() => auth.user?.role === 'ADMIN')

const filters = reactive({
  firstName: '',
  lastName: '',
  email: '',
  sectionName: '',
  teamName: '',
})

const results = ref<StudentSearchResult[]>([])
const loading = ref(false)
const error = ref('')
const searched = ref(false)

async function search() {
  loading.value = true
  error.value = ''
  try {
    const params = {
      firstName:   filters.firstName   || undefined,
      lastName:    filters.lastName    || undefined,
      email:       filters.email       || undefined,
      sectionName: filters.sectionName || undefined,
      teamName:    filters.teamName    || undefined,
    }
    const res = await studentsApi.searchStudents(params)
    results.value = res.data.data
    searched.value = true
  } catch {
    error.value = 'Failed to search students. Please try again.'
  } finally {
    loading.value = false
  }
}

function clearAndSearch() {
  filters.firstName = ''
  filters.lastName = ''
  filters.email = ''
  filters.sectionName = ''
  filters.teamName = ''
  search()
}
</script>
