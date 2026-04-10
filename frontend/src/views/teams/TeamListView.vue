<template>
  <v-container>
    <!-- Header -->
    <v-row class="mb-4" align="center">
      <v-col>
        <h1 class="text-h5 font-weight-bold">Senior Design Teams</h1>
      </v-col>
      <v-col v-if="authStore.user?.role === 'ADMIN'" cols="auto">
        <v-btn color="primary" prepend-icon="mdi-plus" @click="router.push('/teams/new')">
          New Team
        </v-btn>
      </v-col>
    </v-row>

    <!-- Search filters -->
    <v-row class="mb-4">
      <v-col cols="12" sm="4">
        <v-text-field
          v-model="searchName"
          label="Team Name"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="comfortable"
          clearable
          hide-details
          @keyup.enter="search"
          @click:clear="onClearName"
        />
      </v-col>
      <v-col cols="12" sm="3">
        <v-select
          v-model="searchSectionId"
          :items="sections"
          item-title="name"
          item-value="id"
          label="Section"
          prepend-inner-icon="mdi-filter-outline"
          variant="outlined"
          density="comfortable"
          clearable
          hide-details
          @update:model-value="search"
        />
      </v-col>
      <v-col cols="12" sm="3">
        <v-text-field
          label="Instructor"
          prepend-inner-icon="mdi-account-tie"
          variant="outlined"
          density="comfortable"
          hide-details
          disabled
          hint="Available once instructors are assigned"
        />
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" variant="tonal" height="40" @click="search">Search</v-btn>
      </v-col>
    </v-row>

    <!-- Loading -->
    <v-row v-if="loading" justify="center" class="mt-8">
      <v-progress-circular indeterminate color="primary" />
    </v-row>

    <!-- Error -->
    <v-alert v-else-if="error" type="error" variant="tonal" class="mb-4">{{ error }}</v-alert>

    <!-- Empty state -->
    <v-row v-else-if="teams.length === 0 && searched" justify="center" class="mt-8">
      <v-col class="text-center">
        <v-icon size="64" color="grey-lighten-1">mdi-account-group-outline</v-icon>
        <p class="text-h6 text-grey mt-4">No teams found</p>
        <p class="text-body-2 text-grey mb-6">Try different search criteria or create a new team</p>
        <v-btn
          v-if="authStore.user?.role === 'ADMIN'"
          color="primary"
          prepend-icon="mdi-plus"
          @click="router.push('/teams/new')"
        >
          Create Team
        </v-btn>
      </v-col>
    </v-row>

    <!-- Results -->
    <v-row v-else-if="teams.length > 0">
      <v-col
        v-for="team in teams"
        :key="team.id"
        cols="12"
        md="6"
      >
        <v-card variant="outlined" class="h-100">
          <v-card-title class="pt-4 px-4 text-body-1 font-weight-bold d-flex align-center">
            {{ team.name }}
            <v-spacer />
            <v-chip size="x-small" variant="tonal" color="primary">{{ team.sectionName }}</v-chip>
          </v-card-title>
          <v-card-subtitle v-if="team.description" class="px-4 pb-0">
            {{ team.description }}
          </v-card-subtitle>
          <v-card-text class="px-4">
            <!-- Members -->
            <p class="text-caption text-grey font-weight-medium mb-1">STUDENTS</p>
            <p v-if="team.members.length === 0" class="text-body-2 text-grey">None assigned</p>
            <v-chip
              v-for="m in team.members"
              :key="m.id"
              size="small"
              class="mr-1 mb-1"
              variant="tonal"
            >
              {{ m.firstName }} {{ m.lastName }}
            </v-chip>

            <!-- Instructors -->
            <p class="text-caption text-grey font-weight-medium mt-3 mb-1">INSTRUCTORS</p>
            <p v-if="team.instructors.length === 0" class="text-body-2 text-grey">None assigned</p>
            <v-chip
              v-for="i in team.instructors"
              :key="i.id"
              size="small"
              class="mr-1 mb-1"
              color="secondary"
              variant="tonal"
            >
              {{ i.firstName }} {{ i.lastName }}
            </v-chip>
          </v-card-text>
          <v-card-actions class="px-4 pb-3">
            <v-btn
              v-if="team.websiteUrl"
              size="small"
              variant="text"
              :href="team.websiteUrl"
              target="_blank"
              prepend-icon="mdi-open-in-new"
            >
              Website
            </v-btn>
            <v-spacer />
            <v-btn
              size="small"
              variant="text"
              append-icon="mdi-chevron-right"
              @click="router.push(`/teams/${team.id}`)"
            >
              View
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Initial state -->
    <v-row v-else justify="center" class="mt-8">
      <v-col class="text-center">
        <v-icon size="64" color="grey-lighten-1">mdi-magnify</v-icon>
        <p class="text-body-1 text-grey mt-4">Enter search criteria above or leave blank to view all</p>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { teamsApi, type TeamSummary } from '@/api/teams'
import { sectionsApi, type SectionSummary } from '@/api/sections'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const searchName = ref('')
const searchSectionId = ref<number | null>(null)
const sections = ref<SectionSummary[]>([])
const teams = ref<TeamSummary[]>([])
const loading = ref(false)
const error = ref('')
const searched = ref(false)

onMounted(async () => {
  const res = await sectionsApi.findSections()
  sections.value = res.data.data
  search()
})

async function search() {
  searched.value = true
  loading.value = true
  error.value = ''
  try {
    const params: { name?: string; sectionId?: number } = {}
    if (searchName.value) params.name = searchName.value
    if (searchSectionId.value) params.sectionId = searchSectionId.value
    const res = await teamsApi.findTeams(params)
    teams.value = res.data.data
  } catch {
    error.value = 'Failed to load teams'
  } finally {
    loading.value = false
  }
}

function onClearName() {
  searchName.value = ''
  search()
}
</script>
