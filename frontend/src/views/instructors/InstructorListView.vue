<template>
  <v-container max-width="800">
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">Instructors</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-link-variant" :loading="generating" @click="generateLink">
        Generate Invite Link
      </v-btn>
    </div>

    <v-alert v-if="generateError" type="error" variant="tonal" density="compact" class="mb-4">
      {{ generateError }}
    </v-alert>

    <v-card variant="outlined">
      <v-card-text class="pa-8 text-center text-medium-emphasis">
        Instructor search will be available here once implemented (UC-21).
      </v-card-text>
    </v-card>

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
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { instructorsApi } from '@/api/instructors'

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
</script>
