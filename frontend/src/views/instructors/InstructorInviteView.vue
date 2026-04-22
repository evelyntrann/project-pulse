<template>
  <v-container max-width="600">
    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.push('/instructors')"
    >
      Back to Instructors
    </v-btn>

    <h1 class="text-h5 font-weight-bold mb-6">Invite Instructors</h1>

    <v-card variant="outlined">
      <v-card-text class="pa-6">
        <p class="text-body-2 text-medium-emphasis mb-6">
          Generate a shareable invite link for instructors. Anyone with the link can
          register as an instructor. Links expire after 7 days.
        </p>

        <v-btn
          color="primary"
          prepend-icon="mdi-link-variant"
          :loading="generating"
          @click="generate"
        >
          Generate Invite Link
        </v-btn>

        <v-alert v-if="error" type="error" variant="tonal" density="compact" class="mt-4">
          {{ error }}
        </v-alert>

        <!-- Generated link result -->
        <template v-if="inviteLink">
          <v-divider class="my-6" />

          <p class="text-body-2 font-weight-medium mb-2">Shareable Link</p>
          <v-text-field
            :model-value="inviteLink"
            variant="outlined"
            density="comfortable"
            readonly
            hide-details
            class="mb-2"
          >
            <template #append-inner>
              <v-btn
                :icon="copied ? 'mdi-check' : 'mdi-content-copy'"
                variant="text"
                density="compact"
                :color="copied ? 'success' : undefined"
                @click="copyLink"
              />
            </template>
          </v-text-field>

          <p class="text-caption text-medium-emphasis">
            Expires {{ formatExpiry(expiresAt) }}. Share this link via email, Slack, or any channel.
          </p>
        </template>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { instructorsApi } from '@/api/instructors'

const router = useRouter()

const generating = ref(false)
const error = ref('')
const inviteLink = ref('')
const expiresAt = ref('')
const copied = ref(false)

async function generate() {
  generating.value = true
  error.value = ''
  inviteLink.value = ''
  try {
    const res = await instructorsApi.generateInstructorLink()
    inviteLink.value = res.data.data.shareableLink
    expiresAt.value = res.data.data.expiresAt
  } catch (err: any) {
    error.value = err.response?.data?.error || 'Failed to generate link. Please try again.'
  } finally {
    generating.value = false
  }
}

async function copyLink() {
  await navigator.clipboard.writeText(inviteLink.value)
  copied.value = true
  setTimeout(() => (copied.value = false), 2000)
}

function formatExpiry(iso: string) {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>
