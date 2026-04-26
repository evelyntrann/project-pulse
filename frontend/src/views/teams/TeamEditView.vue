<template>
  <v-container max-width="680">
    <!-- Back button -->
    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.push(`/teams/${route.params.id}`)"
    >
      Back to Team
    </v-btn>

    <!-- Loading -->
    <v-row v-if="loading" justify="center" class="mt-8">
      <v-progress-circular indeterminate color="primary" />
    </v-row>

    <!-- Load error -->
    <v-alert v-else-if="loadError" type="error" variant="tonal">{{ loadError }}</v-alert>

    <template v-else>
      <h1 class="text-h5 font-weight-bold mb-6">Edit Team</h1>

      <v-card variant="outlined" class="mb-6">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Team Details</v-card-title>
        <v-card-text class="pa-4">
          <v-form ref="formRef" @submit.prevent>
            <v-text-field
              v-model="form.name"
              label="Team Name"
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
              variant="outlined"
              density="comfortable"
              rows="3"
              class="mb-3"
            />
            <v-text-field
              v-model="form.websiteUrl"
              label="Website URL"
              variant="outlined"
              density="comfortable"
              placeholder="https://"
            />
          </v-form>
        </v-card-text>
      </v-card>

      <!-- Actions -->
      <v-row>
        <v-col cols="auto">
          <v-btn variant="outlined" @click="router.push(`/teams/${route.params.id}`)">Cancel</v-btn>
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
            <v-list-item title="Team Name" :subtitle="form.name" prepend-icon="mdi-account-group-outline" />
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
import { teamsApi, type TeamUpdateRequest } from '@/api/teams'

const router = useRouter()
const route = useRoute()

const formRef = ref()
const loading = ref(false)
const loadError = ref('')
const confirmDialog = ref(false)
const submitting = ref(false)
const submitError = ref('')
const duplicateError = ref('')

const form = reactive<TeamUpdateRequest>({
  name: '',
  description: '',
  websiteUrl: '',
})

const nameRules = [
  (v: string) => !!v || 'Team name is required',
]

onMounted(async () => {
  loading.value = true
  try {
    const res = await teamsApi.getTeam(Number(route.params.id))
    const t = res.data.data
    form.name = t.name
    form.description = t.description ?? ''
    form.websiteUrl = t.websiteUrl ?? ''
  } catch {
    loadError.value = 'Failed to load team details'
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
    await teamsApi.updateTeam(Number(route.params.id), { ...form })
    confirmDialog.value = false
    router.push(`/teams/${route.params.id}?saved=1`)
  } catch (err: any) {
    const status = err.response?.status
    const message = err.response?.data?.error || err.response?.data?.message
    if (status === 409) {
      duplicateError.value = message || 'A team with this name already exists'
      confirmDialog.value = false
    } else {
      submitError.value = message || 'Failed to save changes. Please try again.'
    }
  } finally {
    submitting.value = false
  }
}
</script>
