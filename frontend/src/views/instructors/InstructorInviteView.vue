<template>
  <v-container max-width="720">
    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.push('/instructors')"
    >
      Back to Instructors
    </v-btn>

    <h1 class="text-h5 font-weight-bold mb-6">Invite Instructors</h1>

    <!-- Step 1: Email input -->
    <v-card variant="outlined" class="mb-4">
      <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Instructor Emails</v-card-title>
      <v-card-text class="pa-4">
        <v-form ref="formRef" @submit.prevent>
          <v-textarea
            v-model="rawEmails"
            label="Email Addresses"
            placeholder="john.doe@tcu.edu; f.smith@tcu.edu; tim.johnson@tcu.edu"
            hint="Separate multiple emails with semicolons ( ; )"
            persistent-hint
            variant="outlined"
            rows="3"
            :rules="emailsRules"
            :error-messages="parseErrors"
            @input="parseErrors = []"
          />
        </v-form>

        <!-- Parsed preview -->
        <div v-if="parsedEmails.length > 0" class="mt-3">
          <p class="text-body-2 text-medium-emphasis mb-2">
            {{ parsedEmails.length }} email{{ parsedEmails.length !== 1 ? 's' : '' }} detected:
          </p>
          <v-chip
            v-for="email in parsedEmails"
            :key="email"
            size="small"
            class="mr-1 mb-1"
          >
            {{ email }}
          </v-chip>
        </div>
      </v-card-text>
    </v-card>

    <!-- Step 2 (optional): Custom message -->
    <v-card variant="outlined" class="mb-6">
      <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Email Message</v-card-title>
      <v-card-text class="pa-4">
        <v-alert type="info" variant="tonal" density="compact" class="mb-3">
          A default invitation email will be sent. You may optionally customize the message below.
        </v-alert>

        <v-expand-transition>
          <div>
            <v-checkbox
              v-model="useCustomMessage"
              label="Customize email message"
              density="comfortable"
              hide-details
              class="mb-2"
            />
            <v-textarea
              v-if="useCustomMessage"
              v-model="customMessage"
              label="Custom Message"
              variant="outlined"
              rows="6"
              :placeholder="defaultMessagePreview"
            />
            <div v-else class="pa-3 bg-grey-lighten-4 rounded text-body-2" style="white-space: pre-wrap">{{ defaultMessagePreview }}</div>
          </div>
        </v-expand-transition>
      </v-card-text>
    </v-card>

    <!-- Actions -->
    <v-row>
      <v-col cols="auto">
        <v-btn variant="outlined" @click="router.push('/instructors')">Cancel</v-btn>
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" @click="openConfirm">Review & Send</v-btn>
      </v-col>
    </v-row>

    <!-- Confirm Dialog -->
    <v-dialog v-model="confirmDialog" max-width="520">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Confirm Invitations</v-card-title>
        <v-card-text class="pa-6 pt-2">
          <p class="text-body-2 mb-1 text-medium-emphasis">Sending to {{ parsedEmails.length }} instructor{{ parsedEmails.length !== 1 ? 's' : '' }}:</p>
          <v-chip
            v-for="email in parsedEmails"
            :key="email"
            size="small"
            class="mr-1 mb-2"
          >
            {{ email }}
          </v-chip>

          <v-divider class="my-3" />

          <p class="text-body-2 mb-1 text-medium-emphasis">Email message:</p>
          <div class="pa-3 bg-grey-lighten-4 rounded text-body-2" style="white-space: pre-wrap">
            {{ useCustomMessage && customMessage ? customMessage : defaultMessagePreview }}
          </div>

          <v-alert v-if="submitError" type="error" variant="tonal" density="compact" class="mt-3">
            {{ submitError }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-btn variant="outlined" @click="confirmDialog = false">Modify</v-btn>
          <v-spacer />
          <v-btn color="primary" :loading="submitting" @click="submit">Send Invitations</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Success Dialog -->
    <v-dialog v-model="successDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Invitations Sent</v-card-title>
        <v-card-text class="pa-6 pt-0">
          Successfully sent {{ sentCount }} invitation{{ sentCount !== 1 ? 's' : '' }}.
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-spacer />
          <v-btn color="primary" @click="router.push('/instructors')">Done</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { instructorsApi } from '@/api/instructors'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref()
const rawEmails = ref('')
const parseErrors = ref<string[]>([])
const useCustomMessage = ref(false)
const customMessage = ref('')
const confirmDialog = ref(false)
const successDialog = ref(false)
const submitting = ref(false)
const submitError = ref('')
const sentCount = ref(0)

const adminName = computed(() =>
  `${auth.user?.firstName ?? ''} ${auth.user?.lastName ?? ''}`.trim()
)
const adminEmail = computed(() => auth.user?.email ?? '')

const defaultMessagePreview = computed(() =>
  `Hello,\n\n${adminName.value} has invited you to join The Peer Evaluation Tool. To complete your registration, please use the link below:\n\n[Registration link — unique per recipient]\n\nIf you have any questions or need assistance, feel free to contact ${adminEmail.value} or our team directly.\n\nPlease note: This email is not monitored, so do not reply directly to this message.\n\nBest regards,\nPeer Evaluation Tool Team`
)

const parsedEmails = computed<string[]>(() => {
  if (!rawEmails.value.trim()) return []
  return rawEmails.value
    .split(';')
    .map(e => e.trim())
    .filter(e => e.length > 0)
})

const emailsRules = [
  (v: string) => !!v?.trim() || 'At least one email is required',
]

function validateEmails(): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  const errors: string[] = []

  if (parsedEmails.value.length === 0) {
    errors.push('At least one email is required.')
  }

  // Check for trailing semicolon (bad format per spec)
  const trimmed = rawEmails.value.trim()
  if (trimmed.endsWith(';')) {
    errors.push('Remove the trailing semicolon at the end.')
  }

  parsedEmails.value.forEach(email => {
    if (!emailRegex.test(email)) {
      errors.push(`"${email}" is not a valid email address.`)
    }
  })

  parseErrors.value = errors
  return errors.length === 0
}

async function openConfirm() {
  const { valid } = await formRef.value.validate()
  if (!valid || !validateEmails()) return
  submitError.value = ''
  confirmDialog.value = true
}

async function submit() {
  submitting.value = true
  submitError.value = ''
  try {
    const res = await instructorsApi.inviteInstructors({
      emails: parsedEmails.value,
      customMessage: useCustomMessage.value && customMessage.value ? customMessage.value : undefined,
    })
    confirmDialog.value = false
    sentCount.value = res.data.data.count
    successDialog.value = true
  } catch (err: any) {
    submitError.value = err.response?.data?.error || 'Failed to send invitations. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>
