<template>
  <v-container max-width="480" class="py-12">
    <!-- Loading token info -->
    <div v-if="loadingInfo" class="text-center pa-8">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <!-- Invalid / expired token -->
    <v-alert v-else-if="tokenError" type="error" variant="tonal">
      {{ tokenError }}
    </v-alert>

    <!-- Registration form -->
    <template v-else>
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
          <v-form ref="formRef" @submit.prevent="submit">
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

            <v-alert v-if="submitError" type="error" variant="tonal" density="compact" class="mb-4">
              {{ submitError }}
            </v-alert>

            <v-btn
              type="submit"
              color="primary"
              block
              :loading="submitting"
            >
              Create Account & Join
            </v-btn>
          </v-form>
        </v-card-text>
      </v-card>

      <p class="text-caption text-center text-medium-emphasis mt-4">
        Already have an account?
        <a href="/login" class="text-primary">Sign in</a>
      </p>
    </template>

    <!-- Success dialog -->
    <v-dialog v-model="successDialog" max-width="400" persistent>
      <v-card>
        <v-card-text class="pa-6 text-center">
          <v-icon color="success" size="48" class="mb-3">mdi-check-circle-outline</v-icon>
          <h2 class="text-h6 font-weight-bold mb-2">You're in!</h2>
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

const router = useRouter()
const route = useRoute()

const loadingInfo = ref(true)
const tokenError = ref('')
const role = ref('')
const sectionName = ref('')

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
    tokenError.value = err.response?.data?.error
      || err.response?.data?.message
      || 'This invitation link is invalid or has expired.'
  } finally {
    loadingInfo.value = false
  }
})

async function submit() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  submitting.value = true
  submitError.value = ''
  try {
    await invitationsApi.registerViaToken(route.params.token as string, {
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      password: form.password,
    })
    successDialog.value = true
  } catch (err: any) {
    submitError.value = err.response?.data?.error
      || err.response?.data?.message
      || 'Registration failed. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>
