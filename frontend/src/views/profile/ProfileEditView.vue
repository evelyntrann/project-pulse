<template>
  <v-container max-width="560" class="py-8">
    <h1 class="text-h5 font-weight-bold mb-6">Edit Account</h1>

    <!-- Step 1: Edit form -->
    <template v-if="step === 'form'">
      <v-card variant="outlined">
        <v-card-text class="pa-6">
          <v-form ref="formRef" @submit.prevent="goToReview">

            <p class="text-caption text-medium-emphasis mb-4">
              Leave a field blank to keep its current value.
            </p>

            <v-text-field
              v-model="form.firstName"
              :label="`First Name (current: ${auth.user?.firstName})`"
              variant="outlined"
              density="comfortable"
              class="mb-3"
            />
            <v-text-field
              v-model="form.lastName"
              :label="`Last Name (current: ${auth.user?.lastName})`"
              variant="outlined"
              density="comfortable"
              class="mb-3"
            />
            <v-text-field
              v-model="form.email"
              :label="`Email (current: ${auth.user?.email})`"
              type="email"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[optionalEmail]"
            />
            <v-divider class="mb-4" />
            <p class="text-caption text-medium-emphasis mb-3">
              Leave password blank to keep your current password.
            </p>
            <v-text-field
              v-model="form.password"
              label="New Password"
              type="password"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[optionalMinLength]"
            />
            <v-text-field
              v-model="form.confirmPassword"
              label="Confirm New Password"
              type="password"
              variant="outlined"
              density="comfortable"
              class="mb-4"
              :rules="[optionalPasswordsMatch]"
            />

            <div class="d-flex gap-3">
              <v-btn variant="outlined" :to="'/home'" class="flex-grow-1">Cancel</v-btn>
              <v-btn type="submit" color="primary" class="flex-grow-1">Review Changes</v-btn>
            </div>

          </v-form>
        </v-card-text>
      </v-card>
    </template>

    <!-- Step 2: Review & confirm -->
    <template v-else-if="step === 'review'">
      <h2 class="text-subtitle-1 font-weight-medium mb-4">Confirm your changes</h2>

      <v-card variant="outlined" class="mb-4">
        <v-list lines="two" density="compact">
          <v-list-item
            title="First Name"
            :subtitle="changedFields.firstName
              ? `${auth.user?.firstName} → ${form.firstName}`
              : `${auth.user?.firstName} (unchanged)`"
          />
          <v-divider />
          <v-list-item
            title="Last Name"
            :subtitle="changedFields.lastName
              ? `${auth.user?.lastName} → ${form.lastName}`
              : `${auth.user?.lastName} (unchanged)`"
          />
          <v-divider />
          <v-list-item
            title="Email"
            :subtitle="changedFields.email
              ? `${auth.user?.email} → ${form.email}`
              : `${auth.user?.email} (unchanged)`"
          />
          <v-divider />
          <v-list-item
            title="Password"
            :subtitle="changedFields.password ? 'Will be updated' : 'Unchanged'"
          />
        </v-list>
      </v-card>

      <v-alert v-if="submitError" type="error" variant="tonal" density="compact" class="mb-4">
        {{ submitError }}
      </v-alert>

      <div class="d-flex gap-3">
        <v-btn variant="outlined" @click="step = 'form'" :disabled="submitting" class="flex-grow-1">
          Modify
        </v-btn>
        <v-btn color="primary" @click="submit" :loading="submitting" class="flex-grow-1">
          Save Changes
        </v-btn>
      </div>
    </template>

    <!-- Success -->
    <v-snackbar v-model="showSuccess" color="success" :timeout="3000" location="top">
      Account updated successfully.
    </v-snackbar>

  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usersApi } from '@/api/users'

const auth = useAuthStore()

const step = ref<'form' | 'review'>('form')
const formRef = ref()
const submitting = ref(false)
const submitError = ref('')
const showSuccess = ref(false)

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const changedFields = computed(() => ({
  firstName: form.firstName.trim() !== '',
  lastName:  form.lastName.trim() !== '',
  email:     form.email.trim() !== '',
  password:  form.password.trim() !== '',
}))

const optionalEmail = (v: string) =>
  !v || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) || 'Invalid email address'

const optionalMinLength = (v: string) =>
  !v || v.length >= 8 || 'Password must be at least 8 characters'

const optionalPasswordsMatch = (v: string) =>
  !form.password || v === form.password || 'Passwords do not match'

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
    await usersApi.updateProfile(auth.user!.id, {
      firstName: form.firstName.trim() || undefined,
      lastName:  form.lastName.trim()  || undefined,
      email:     form.email.trim()     || undefined,
      password:  form.password.trim()  || undefined,
    })
    await auth.fetchMe()
    showSuccess.value = true
    step.value = 'form'
    form.firstName = ''
    form.lastName = ''
    form.email = ''
    form.password = ''
    form.confirmPassword = ''
  } catch (err: any) {
    submitError.value = err.response?.data?.error
      || err.response?.data?.message
      || 'Failed to update account. Please try again.'
    step.value = 'form'
  } finally {
    submitting.value = false
  }
}
</script>
