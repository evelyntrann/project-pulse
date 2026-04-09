<template>
  <v-container>
    <!-- Header -->
    <v-row class="mb-4" align="center">
      <v-col>
        <h1 class="text-h5 font-weight-bold">Senior Design Sections</h1>
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" prepend-icon="mdi-plus" @click="router.push('/sections/new')">
          New Section
        </v-btn>
      </v-col>
    </v-row>

    <!-- Search bar -->
    <v-row class="mb-4">
      <v-col cols="12" md="5">
        <v-text-field
          v-model="searchName"
          label="Search by section name"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="comfortable"
          clearable
          hide-details
          @keyup.enter="search"
          @click:clear="onClear"
        />
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" variant="tonal" height="40" @click="search">
          Search
        </v-btn>
      </v-col>
    </v-row>

    <!-- Loading -->
    <v-row v-if="store.loading" justify="center" class="mt-8">
      <v-progress-circular indeterminate color="primary" />
    </v-row>

    <!-- Error -->
    <v-alert v-else-if="store.error" type="error" variant="tonal" class="mb-4">
      {{ store.error }}
    </v-alert>

    <!-- Empty state -->
    <v-row v-else-if="store.sections.length === 0 && searched" justify="center" class="mt-8">
      <v-col class="text-center">
        <v-icon size="64" color="grey-lighten-1">mdi-folder-open-outline</v-icon>
        <p class="text-h6 text-grey mt-4">No sections found</p>
        <p class="text-body-2 text-grey mb-6">Try a different name or create a new section</p>
        <v-btn color="primary" prepend-icon="mdi-plus" @click="router.push('/sections/new')">
          Create Section
        </v-btn>
      </v-col>
    </v-row>

    <!-- Results table -->
    <v-card v-else-if="store.sections.length > 0" variant="outlined">
      <v-table>
        <thead>
          <tr>
            <th>Section Name</th>
            <th>Start Date</th>
            <th>End Date</th>
            <th>Teams</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="section in store.sections" :key="section.id">
            <td class="font-weight-medium">{{ section.name }}</td>
            <td>{{ formatDate(section.startDate) }}</td>
            <td>{{ formatDate(section.endDate) }}</td>
            <td>
              <span v-if="section.teamNames.length === 0" class="text-grey">No teams</span>
              <v-chip
                v-for="team in section.teamNames"
                :key="team"
                size="small"
                class="mr-1"
                variant="tonal"
                color="primary"
              >
                {{ team }}
              </v-chip>
            </td>
            <td>
              <v-btn
                variant="text"
                size="small"
                icon="mdi-chevron-right"
                @click="router.push(`/sections/${section.id}`)"
              />
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>

    <!-- Initial state (before first search) -->
    <v-row v-else justify="center" class="mt-8">
      <v-col class="text-center">
        <v-icon size="64" color="grey-lighten-1">mdi-magnify</v-icon>
        <p class="text-body-1 text-grey mt-4">Enter a section name above to search, or leave blank to view all</p>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useSectionsStore } from '@/stores/sections'

const router = useRouter()
const store = useSectionsStore()

const searchName = ref('')
const searched = ref(false)

onMounted(() => search())

function search() {
  searched.value = true
  store.findSections(searchName.value || undefined)
}

function onClear() {
  searchName.value = ''
  search()
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
  })
}
</script>
