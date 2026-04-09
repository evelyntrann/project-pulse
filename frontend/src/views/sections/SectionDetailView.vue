<template>
  <v-container>
    <!-- Back button -->
    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.push('/sections')"
    >
      Back to Sections
    </v-btn>

    <!-- Loading -->
    <v-row v-if="loading" justify="center" class="mt-8">
      <v-progress-circular indeterminate color="primary" />
    </v-row>

    <!-- Error -->
    <v-alert v-else-if="error" type="error" variant="tonal">{{ error }}</v-alert>

    <template v-else-if="section">
      <!-- Section header -->
      <v-row align="center" class="mb-6">
        <v-col>
          <h1 class="text-h5 font-weight-bold">{{ section.name }}</h1>
          <p class="text-body-2 text-grey mt-1">
            {{ formatDate(section.startDate) }} — {{ formatDate(section.endDate) }}
          </p>
        </v-col>
        <v-col cols="auto">
          <v-btn
            color="primary"
            variant="tonal"
            prepend-icon="mdi-pencil"
            @click="router.push(`/sections/${section.id}/edit`)"
          >
            Edit
          </v-btn>
        </v-col>
      </v-row>

      <!-- Info cards row -->
      <v-row class="mb-6">
        <v-col cols="12" sm="4">
          <v-card variant="outlined" class="pa-4 text-center">
            <div class="text-h4 font-weight-bold text-primary">{{ section.teams.length }}</div>
            <div class="text-body-2 text-grey mt-1">Teams</div>
          </v-card>
        </v-col>
        <v-col cols="12" sm="4">
          <v-card variant="outlined" class="pa-4 text-center">
            <div class="text-h4 font-weight-bold text-primary">{{ totalMembers }}</div>
            <div class="text-body-2 text-grey mt-1">Students Assigned</div>
          </v-card>
        </v-col>
        <v-col cols="12" sm="4">
          <v-card variant="outlined" class="pa-4 text-center">
            <div class="text-h4 font-weight-bold text-primary">{{ section.unassignedStudents.length }}</div>
            <div class="text-body-2 text-grey mt-1">Unassigned Students</div>
          </v-card>
        </v-col>
      </v-row>

      <!-- Teams -->
      <h2 class="text-h6 font-weight-bold mb-3">Teams</h2>
      <v-row v-if="section.teams.length === 0" class="mb-6">
        <v-col>
          <v-card variant="outlined" class="pa-6 text-center">
            <v-icon size="40" color="grey-lighten-1">mdi-account-group-outline</v-icon>
            <p class="text-body-2 text-grey mt-2">No teams yet</p>
          </v-card>
        </v-col>
      </v-row>
      <v-row v-else class="mb-6">
        <v-col v-for="team in section.teams" :key="team.id" cols="12" md="6">
          <v-card variant="outlined">
            <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
              {{ team.name }}
            </v-card-title>
            <v-card-subtitle v-if="team.description" class="px-4">
              {{ team.description }}
            </v-card-subtitle>
            <v-card-text>
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
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <!-- Unassigned Students -->
      <h2 class="text-h6 font-weight-bold mb-3">Students Not Assigned to a Team</h2>
      <v-card variant="outlined" class="mb-6">
        <v-card-text v-if="section.unassignedStudents.length === 0" class="text-grey text-center py-4">
          None
        </v-card-text>
        <v-list v-else density="compact">
          <v-list-item
            v-for="s in section.unassignedStudents"
            :key="s.id"
            :title="`${s.firstName} ${s.lastName}`"
            :subtitle="s.email"
            prepend-icon="mdi-account"
          />
        </v-list>
      </v-card>

      <!-- Unassigned Instructors -->
      <h2 class="text-h6 font-weight-bold mb-3">Instructors Not Assigned to a Team</h2>
      <v-card variant="outlined">
        <v-card-text v-if="section.unassignedInstructors.length === 0" class="text-grey text-center py-4">
          None
        </v-card-text>
        <v-list v-else density="compact">
          <v-list-item
            v-for="i in section.unassignedInstructors"
            :key="i.id"
            :title="`${i.firstName} ${i.lastName}`"
            :subtitle="i.email"
            prepend-icon="mdi-account-tie"
          />
        </v-list>
      </v-card>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { sectionsApi, type SectionDetail } from '@/api/sections'

const router = useRouter()
const route = useRoute()

const section = ref<SectionDetail | null>(null)
const loading = ref(false)
const error = ref('')

const totalMembers = computed(() =>
  section.value?.teams.reduce((sum, t) => sum + t.members.length, 0) ?? 0
)

onMounted(async () => {
  loading.value = true
  try {
    const res = await sectionsApi.getSection(Number(route.params.id))
    section.value = res.data.data
  } catch {
    error.value = 'Section not found or failed to load'
  } finally {
    loading.value = false
  }
})

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric',
  })
}
</script>
