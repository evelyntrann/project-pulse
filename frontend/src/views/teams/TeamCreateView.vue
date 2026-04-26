<template>
  <v-container max-width="680">
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4 pl-0" @click="router.push('/teams')">
      Back to Teams
    </v-btn>

    <h1 class="text-h5 font-weight-bold mb-6">Create Senior Design Team</h1>

    <v-card variant="outlined" class="mb-4">
      <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Team Details</v-card-title>
      <v-card-text class="pa-4">
        <v-form ref="formRef" @submit.prevent>
          <v-text-field
            v-model="form.name"
            label="Team Name"
            placeholder="e.g. Peer Evaluation Tool Team"
            variant="outlined"
            density="comfortable"
            :rules="nameRules"
            :error-messages="duplicateError"
            class="mb-3"
            @input="duplicateError = ''"
          />
          <v-textarea
            v-model="form.description"
            label="Description"
            placeholder="Brief description of what this team is building"
            variant="outlined"
            density="comfortable"
            rows="3"
            class="mb-3"
          />
          <v-text-field
            v-model="form.websiteUrl"
            label="Website URL"
            placeholder="https://github.com/..."
            variant="outlined"
            density="comfortable"
            :rules="urlRules"
          />
        </v-form>
      </v-card-text>
    </v-card>

    <v-card variant="outlined" class="mb-6">
      <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Section</v-card-title>
      <v-card-text class="pa-4">
        <v-select
          v-model="form.sectionId"
          :items="sections"
          item-title="name"
          item-value="id"
          label="Senior Design Section"
          variant="outlined"
          density="comfortable"
          :rules="[(v) => !!v || 'Section is required']"
        />
      </v-card-text>
    </v-card>

    <v-row>
      <v-col cols="auto">
        <v-btn variant="outlined" @click="router.push('/teams')">Cancel</v-btn>
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" @click="openConfirm">Review & Create</v-btn>
      </v-col>
    </v-row>

    <!-- Confirm Dialog -->
    <v-dialog v-model="confirmDialog" max-width="480">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Confirm New Team</v-card-title>
        <v-card-text class="pa-6 pt-2">
          <v-list density="compact" lines="two">
            <v-list-item title="Team Name" :subtitle="form.name" prepend-icon="mdi-account-group" />
            <v-list-item
              title="Description"
              :subtitle="form.description || '—'"
              prepend-icon="mdi-text"
            />
            <v-list-item
              title="Website URL"
              :subtitle="form.websiteUrl || '—'"
              prepend-icon="mdi-web"
            />
            <v-list-item
              title="Section"
              :subtitle="selectedSectionName"
              prepend-icon="mdi-school"
            />
          </v-list>
          <v-alert v-if="submitError" type="error" variant="tonal" density="compact" class="mt-3">
            {{ submitError }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-btn variant="outlined" @click="confirmDialog = false">Modify</v-btn>
          <v-spacer />
          <v-btn color="primary" :loading="submitting" @click="submit">Confirm & Create</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { teamsApi, type TeamCreateRequest } from '@/api/teams'
import { sectionsApi, type SectionSummary } from '@/api/sections'

const router = useRouter()

const formRef = ref()
const confirmDialog = ref(false)
const submitting = ref(false)
const submitError = ref('')
const duplicateError = ref('')
const sections = ref<SectionSummary[]>([])

const form = reactive<TeamCreateRequest>({
  name: '',
  description: '',
  websiteUrl: '',
  sectionId: 0,
})

const selectedSectionName = computed(
  () => sections.value.find(s => s.id === form.sectionId)?.name ?? '—'
)

const nameRules = [
  (v: string) => !!v || 'Team name is required',
]

const urlRules = [
  (v: string) => !v || /^https?:\/\/.+/.test(v) || 'Must be a valid URL starting with http:// or https://',
]

onMounted(async () => {
  const res = await sectionsApi.findSections()
  sections.value = res.data.data
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
    const res = await teamsApi.createTeam({ ...form })
    confirmDialog.value = false
    router.push(`/teams/${res.data.data.id}`)
  } catch (err: any) {
    const status = err.response?.status
    const message = err.response?.data?.error || err.response?.data?.message
    if (status === 409) {
      duplicateError.value = message || 'A team with this name already exists'
      confirmDialog.value = false
    } else {
      submitError.value = message || 'Failed to create team. Please try again.'
    }
  } finally {
    submitting.value = false
  }
}
</script>
