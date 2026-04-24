<template>
  <v-container max-width="480" class="py-12">

    <!-- Loading token info -->
    <div v-if="loadingInfo" class="text-center pa-8">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <!-- Invalid / expired / already-used token -->
    <v-card v-else-if="tokenError" variant="outlined" class="pa-6 text-center">
      <v-icon color="error" size="48" class="mb-3">mdi-link-off</v-icon>
      <h2 class="text-h6 font-weight-bold mb-2">Link Unavailable</h2>
      <p class="text-body-2 text-medium-emphasis mb-4">{{ tokenError }}</p>
      <v-btn color="primary" variant="tonal" :to="'/login'">Go to Login</v-btn>
    </v-card>

    <!-- Step 1: Entry form -->
    <template v-else-if="step === 'form'">
      <h1 class="text-h5 font-weight-bold mb-1">
        {{ role === 'INSTRUCTOR' ? 'Create Instructor Account' : `Join ${sectionName}` }}
      </h1>
      <p class="text-body-2 text-medium-emphasis mb-6">
        {{ role === 'INSTRUCTOR'
          ? 'Create your account to get access as an instructor.'
          : 'Create your account to join this Senior Design section.' }}
      </p>

      <v-card variant="outlined">
        <v-card-text class="pa-6">
          <v-form ref="formRef" @submit.prevent="goToReview">
            <v-text-field
              v-model="form.firstName"
              label="First Name"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[required]"
            />
            <v-text-field
              v-model="form.lastName"
              label="Last Name"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[required]"
            />
            <v-text-field
              v-model="form.email"
              label="Email"
              type="email"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[required, validEmail]"
            />
            <v-text-field
              v-model="form.password"
              label="Password"
              type="password"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[required, minLength]"
            />
            <v-text-field
              v-model="form.confirmPassword"
              label="Confirm Password"
              type="password"
              variant="outlined"
              density="comfortable"
              class="mb-4"
              :rules="[required, passwordsMatch]"
            />

            <v-btn type="submit" color="primary" block>
              Review & Confirm
            </v-btn>
          </v-form>
        </v-card-text>
      </v-card>

      <p class="text-caption text-center text-medium-emphasis mt-4">
        Already have an account?
        <a href="/login" class="text-primary">Sign in</a>
      </p>
    </template>

    <!-- Step 2: Review & confirm -->
    <template v-else-if="step === 'review'">
      <h1 class="text-h5 font-weight-bold mb-1">Confirm Your Details</h1>
      <p class="text-body-2 text-medium-emphasis mb-6">
        Review your information before creating your account.
      </p>

      <v-card variant="outlined" class="mb-4">
        <v-list lines="two" density="compact">
          <v-list-item title="First Name" :subtitle="form.firstName" />
          <v-divider />
          <v-list-item title="Last Name" :subtitle="form.lastName" />
          <v-divider />
          <v-list-item title="Email" :subtitle="form.email" />
          <v-divider />
          <v-list-item title="Section" :subtitle="role === 'INSTRUCTOR' ? 'Instructor account' : sectionName" />
        </v-list>
      </v-card>

      <v-alert v-if="submitError" type="error" variant="tonal" density="compact" class="mb-4">
        {{ submitError }}
      </v-alert>

      <div class="d-flex gap-3">
        <v-btn variant="outlined" @click="step = 'form'" :disabled="submitting" class="flex-grow-1">
          Modify Details
        </v-btn>
        <v-btn color="primary" @click="submit" :loading="submitting" class="flex-grow-1">
          Create Account
        </v-btn>
      </div>
    </template>

    <!-- Success dialog -->
    <v-dialog v-model="successDialog" max-width="400" persistent>
      <v-card>
        <v-card-text class="pa-6 text-center">
          <v-icon color="success" size="48" class="mb-3">mdi-check-circle-outline</v-icon>
          <h2 class="text-h6 font-weight-bold mb-2">Account Created!</h2>
          <p class="text-body-2 text-medium-emphasis">
            <template v-if="role === 'INSTRUCTOR'">
              Your instructor account has been created successfully.
            </template>
            <template v-else>
              Your account has been created and you've been added to <strong>{{ sectionName }}</strong>.
            </template>
          </p>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-btn color="primary" block @click="router.push('/login')">Go to Login</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { invitationsApi } from '@/api/invitations'
import { studentsApi } from '@/api/students'

const router = useRouter()
const route = useRoute()

const loadingInfo = ref(true)
const tokenError = ref('')
const role = ref('')
const sectionName = ref('')

const step = ref<'form' | 'review'>('form')
const formRef = ref()
const submitting = ref(false)
const submitError = ref('')
const successDialog = ref(false)

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const required = (v: string) => !!v || 'This field is required'
const validEmail = (v: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) || 'Invalid email address'
const minLength = (v: string) => v.length >= 8 || 'Password must be at least 8 characters'
const passwordsMatch = (v: string) => v === form.password || 'Passwords do not match'

onMounted(async () => {
  try {
    const res = await invitationsApi.getInvitationInfo(route.params.token as string)
    role.value = res.data.data.role
    sectionName.value = res.data.data.sectionName ?? ''
  } catch (err: any) {
    const msg = err.response?.data?.error || err.response?.data?.message || ''
    if (msg.toLowerCase().includes('already been used')) {
      tokenError.value = 'This invitation link has already been used. Please sign in to your account.'
    } else if (msg.toLowerCase().includes('expired')) {
      tokenError.value = 'This invitation link has expired. Contact your admin for a new one.'
    } else {
      tokenError.value = 'This invitation link is invalid or has expired.'
    }
  } finally {
    loadingInfo.value = false
  }
})

async function goToReview() {
  const { valid } = await formRef.value.validate()
  if (!valid) return
  submitError.value = ''
  step.value = 'review'
}

async function submit() {
  submitting.value = true
  submitError.value = ''
  try {
    const token = route.params.token as string
    if (role.value === 'STUDENT') {
      await studentsApi.register({
        token,
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
      })
    } else {
      await invitationsApi.registerViaToken(token, {
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
      })
    }
    successDialog.value = true
  } catch (err: any) {
    const msg = err.response?.data?.error || err.response?.data?.message || 'Registration failed. Please try again.'
    submitError.value = msg
    step.value = 'form'
  } finally {
    submitting.value = false
  }
}
</script>
