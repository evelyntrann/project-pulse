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

    <h1 class="text-h5 font-weight-bold mb-6">Invite Students</h1>

    <!-- Step 1: Email input -->
    <v-card variant="outlined" class="mb-4">
      <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Student Emails</v-card-title>
      <v-card-text class="pa-4">
        <v-form ref="formRef" @submit.prevent>
          <p class="text-body-2 text-grey mb-3">
            Enter email addresses separated by semicolons. Spaces between emails are ignored.
          </p>
          <v-textarea
            v-model="rawEmails"
            label="Email addresses"
            variant="outlined"
            density="comfortable"
            rows="5"
            placeholder="john.doe@tcu.edu; f.smith@tcu.edu; tim.johnson@tcu.edu"
            :rules="emailsRules"
            :error-messages="parseError"
            @input="parseError = ''"
          />

          <!-- Parsed email chips -->
          <template v-if="parsedEmails.length > 0">
            <p class="text-caption text-grey font-weight-medium mb-2">
              {{ parsedEmails.length }} email{{ parsedEmails.length > 1 ? 's' : '' }} recognized:
            </p>
            <div class="d-flex flex-wrap gap-1 mb-2">
              <v-chip
                v-for="email in parsedEmails"
                :key="email"
                size="small"
                variant="tonal"
                color="primary"
                closable
                @click:close="removeEmail(email)"
              >
                {{ email }}
              </v-chip>
            </div>
          </template>
        </v-form>
      </v-card-text>
    </v-card>

    <!-- Step 2: Email message preview / customize -->
    <v-card variant="outlined" class="mb-6">
      <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0 d-flex align-center">
        Email Message
        <v-spacer />
        <v-btn size="small" variant="text" @click="customizing = !customizing">
          {{ customizing ? 'Use Default' : 'Customize' }}
        </v-btn>
      </v-card-title>
      <v-card-text class="pa-4">
        <v-textarea
          v-model="emailBody"
          variant="outlined"
          density="comfortable"
          rows="10"
          :readonly="!customizing"
          :bg-color="customizing ? undefined : 'grey-lighten-4'"
        />
        <p class="text-caption text-grey mt-1">
          <v-icon size="14">mdi-information-outline</v-icon>
          The subject line is always:
          <em>Welcome to The Peer Evaluation Tool - Complete Your Registration</em>
        </p>
      </v-card-text>
    </v-card>

    <!-- Actions -->
    <v-row>
      <v-col cols="auto">
        <v-btn variant="outlined" @click="router.push(`/sections/${route.params.id}`)">Cancel</v-btn>
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" :disabled="parsedEmails.length === 0" @click="openConfirm">
          Review & Send
        </v-btn>
      </v-col>
    </v-row>

    <!-- Confirm dialog -->
    <v-dialog v-model="confirmDialog" max-width="520">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Confirm Invitations</v-card-title>
        <v-card-text class="pa-6 pt-2">
          <v-list density="compact">
            <v-list-item
              prepend-icon="mdi-email-multiple-outline"
              :title="`${parsedEmails.length} invitation${parsedEmails.length > 1 ? 's' : ''} will be sent`"
            />
          </v-list>

          <v-divider class="my-3" />

          <p class="text-caption text-grey font-weight-medium mb-2">RECIPIENTS</p>
          <div class="d-flex flex-wrap gap-1 mb-4">
            <v-chip
              v-for="email in parsedEmails"
              :key="email"
              size="small"
              variant="tonal"
            >
              {{ email }}
            </v-chip>
          </div>

          <p class="text-caption text-grey font-weight-medium mb-2">EMAIL PREVIEW</p>
          <v-sheet
            rounded
            color="grey-lighten-4"
            class="pa-3 text-body-2"
            style="white-space: pre-wrap; font-family: monospace; font-size: 12px;"
          >{{ emailBody }}</v-sheet>

          <v-alert v-if="submitError" type="error" variant="tonal" density="compact" class="mt-3">
            {{ submitError }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-btn variant="outlined" @click="confirmDialog = false">Modify</v-btn>
          <v-spacer />
          <v-btn color="primary" :loading="submitting" @click="submit">Confirm & Send</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { invitationsApi } from '@/api/invitations'

const router = useRouter()
const route = useRoute()

const formRef = ref()
const rawEmails = ref('')
const parseError = ref('')
const customizing = ref(false)
const confirmDialog = ref(false)
const submitting = ref(false)
const submitError = ref('')

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const parsedEmails = computed<string[]>(() => {
  if (!rawEmails.value.trim()) return []
  return rawEmails.value
    .split(';')
    .map(e => e.trim())
    .filter(e => e.length > 0 && EMAIL_REGEX.test(e))
})

const defaultBody = computed(() =>
  `Hello,\n\n` +
  `You have been invited to join The Peer Evaluation Tool. ` +
  `To complete your registration, please use the link below:\n\n` +
  `[Registration link will be generated per recipient]\n\n` +
  `If you have any questions or need assistance, please contact our team directly.\n\n` +
  `Please note: This email is not monitored, so do not reply directly to this message.\n\n` +
  `Best regards,\nPeer Evaluation Tool Team`
)

const emailBody = ref(defaultBody.value)

// Reset body to default when user clicks "Use Default"
watch(customizing, (val) => {
  if (!val) emailBody.value = defaultBody.value
})

function removeEmail(email: string) {
  const remaining = parsedEmails.value.filter(e => e !== email)
  rawEmails.value = remaining.join('; ')
}

const emailsRules = [
  (v: string) => !!v.trim() || 'At least one email is required',
  (v: string) => {
    const parts = v.split(';').map(e => e.trim()).filter(e => e.length > 0)
    const invalid = parts.filter(e => !EMAIL_REGEX.test(e))
    return invalid.length === 0 || `Invalid email(s): ${invalid.join(', ')}`
  },
]

async function openConfirm() {
  const { valid } = await formRef.value.validate()
  if (!valid) return
  if (parsedEmails.value.length === 0) {
    parseError.value = 'No valid emails found. Check formatting.'
    return
  }
  submitError.value = ''
  confirmDialog.value = true
}

async function submit() {
  submitting.value = true
  submitError.value = ''
  try {
    await invitationsApi.inviteStudents({
      sectionId: Number(route.params.id),
      emails: parsedEmails.value,
      customMessage: customizing.value ? emailBody.value : undefined,
    })
    confirmDialog.value = false
    router.push(`/sections/${route.params.id}?saved=invited`)
  } catch (err: any) {
    const message = err.response?.data?.error || err.response?.data?.message
    submitError.value = message || 'Failed to send invitations. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>
