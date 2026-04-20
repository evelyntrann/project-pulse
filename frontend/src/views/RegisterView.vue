<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="5">

        <!-- Loading state while we validate the token (UC-25 Step 2) -->
        <div v-if="pageState === 'loading'" class="text-center pa-8">
          <v-progress-circular indeterminate color="primary" size="48" />
          <p class="mt-4 text-body-2 text-grey">Validating your invitation link…</p>
        </div>

        <!-- Invalid token — link doesn't exist in the system -->
        <v-card v-else-if="pageState === 'invalid'" class="pa-6" elevation="4">
          <v-card-title class="text-h6 text-center mb-2">Invalid Registration Link</v-card-title>
          <v-alert type="error" variant="tonal" class="mb-4">
            This registration link is invalid or does not exist.
            Please check your invitation email or contact your administrator.
          </v-alert>
        </v-card>

        <!-- Expired token — link timed out before the student used it -->
        <v-card v-else-if="pageState === 'expired'" class="pa-6" elevation="4">
          <v-card-title class="text-h6 text-center mb-2">Link Expired</v-card-title>
          <v-alert type="warning" variant="tonal" class="mb-4">
            This registration link has expired. Please contact your administrator to send a new invitation.
          </v-alert>
        </v-card>

        <!--
          UC-25 Extension 2a — Student has already set up the account.
          System alerts the student and redirects to login.
        -->
        <v-card v-else-if="pageState === 'alreadyUsed'" class="pa-6" elevation="4">
          <v-card-title class="text-h6 text-center mb-2">Account Already Created</v-card-title>
          <v-alert type="info" variant="tonal" class="mb-4">
            This invitation has already been used. Your account is set up — please log in.
          </v-alert>
          <v-btn color="primary" block :to="'/login'">Go to Login</v-btn>
        </v-card>

        <!--
          UC-25 Steps 2–6 — The main registration form.
          Shown once the token is validated and confirmed unused.
        -->
        <v-card v-else-if="pageState === 'form'" class="pa-6" elevation="4">
          <v-card-title class="text-h5 text-center mb-1">Create Your Account</v-card-title>

          <!-- Show which section the student is joining (context) -->
          <v-card-subtitle class="text-center mb-4">
            <span v-if="invitation!.sectionName">
              Joining section: <strong>{{ invitation!.sectionName }}</strong>
            </span>
            <span v-else>Project Pulse — Student Registration</span>
          </v-card-subtitle>

          <!--
            Step 5 — confirmation state: show the entered details and ask to confirm.
            Step 6 — student can go back to modify.
          -->
          <template v-if="confirming">
            <v-list lines="one" class="mb-4">
              <v-list-item title="First name" :subtitle="form.firstName" />
              <v-list-item title="Last name"  :subtitle="form.lastName" />
              <v-list-item title="Email"      :subtitle="invitation!.email" />
            </v-list>
            <v-alert v-if="submitError" type="error" variant="tonal" class="mb-4">
              {{ submitError }}
            </v-alert>
            <v-row>
              <!-- Step 6 — modify: go back to the form -->
              <v-col cols="6">
                <v-btn variant="outlined" block @click="confirming = false">Edit</v-btn>
              </v-col>
              <!-- Step 6 → 7 — confirm and submit -->
              <v-col cols="6">
                <v-btn color="primary" block :loading="submitting" @click="submitRegistration">
                  Confirm & Create Account
                </v-btn>
              </v-col>
            </v-row>
          </template>

          <!--
            Steps 2–4 — Input form: first name, last name, email (read-only), password.
            Extension 4a — validation errors shown inline by Vuetify rules.
          -->
          <template v-else>
            <v-form ref="formRef" @submit.prevent="goToConfirm">
              <v-text-field
                v-model="form.firstName"
                label="First name"
                variant="outlined"
                class="mb-2"
                :rules="[v => !!v || 'First name is required']"
                autofocus
              />
              <v-text-field
                v-model="form.lastName"
                label="Last name"
                variant="outlined"
                class="mb-2"
                :rules="[v => !!v || 'Last name is required']"
              />

              <!--
                Email comes from the invitation — shown read-only so the student
                knows which email their account will be tied to.
              -->
              <v-text-field
                :model-value="invitation!.email"
                label="Email (from invitation)"
                variant="outlined"
                class="mb-2"
                readonly
                hint="This is the email your account will be registered under"
                persistent-hint
              />

              <v-text-field
                v-model="form.password"
                label="Password"
                :type="showPassword ? 'text' : 'password'"
                variant="outlined"
                class="mb-2"
                :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                @click:append-inner="showPassword = !showPassword"
                :rules="[
                  v => !!v || 'Password is required',
                  v => v.length >= 8 || 'Password must be at least 8 characters'
                ]"
              />

              <!-- Confirm password — not in UC-25 explicitly but standard UX -->
              <v-text-field
                v-model="form.confirmPassword"
                label="Confirm password"
                :type="showPassword ? 'text' : 'password'"
                variant="outlined"
                class="mb-4"
                :rules="[
                  v => !!v || 'Please confirm your password',
                  v => v === form.password || 'Passwords do not match'
                ]"
              />

              <v-btn type="submit" color="primary" size="large" block>
                Review Details
              </v-btn>
            </v-form>
          </template>
        </v-card>

        <!-- UC-25 Step 7 — success: account created, redirect to login -->
        <v-card v-else-if="pageState === 'success'" class="pa-6 text-center" elevation="4">
          <v-icon color="success" size="64" class="mb-4">mdi-check-circle</v-icon>
          <v-card-title class="text-h6 mb-2">Account Created!</v-card-title>
          <v-card-text class="text-body-2 text-grey mb-4">
            Your account has been set up. You will be redirected to the login page shortly.
          </v-card-text>
          <!-- Step 8 — redirect to login -->
          <v-btn color="primary" :to="'/login'">Go to Login</v-btn>
        </v-card>

      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { registrationApi, type InvitationValidation } from '@/api/registration'

const route  = useRoute()
const router = useRouter()

// ── Page state machine ────────────────────────────────────────────────────────
// UC-25 has multiple distinct system states — a state string is cleaner than
// a pile of booleans.
type PageState = 'loading' | 'invalid' | 'expired' | 'alreadyUsed' | 'form' | 'success'
const pageState  = ref<PageState>('loading')
const invitation = ref<InvitationValidation | null>(null)

// ── Form state ────────────────────────────────────────────────────────────────
const formRef    = ref()
const confirming = ref(false)   // true = Step 5 (review), false = Steps 2-4 (input)
const submitting = ref(false)
const submitError = ref('')
const showPassword = ref(false)

const form = ref({
  firstName: '',
  lastName: '',
  password: '',
  confirmPassword: '',
})

// ── UC-25 Step 2: validate the token on page load ─────────────────────────────
onMounted(async () => {
  const token = route.query.token as string | undefined

  if (!token) {
    pageState.value = 'invalid'
    return
  }

  try {
    const res = await registrationApi.validateToken(token)
    invitation.value = res.data.data

    if (invitation.value.alreadyUsed) {
      // Extension 2a — already registered
      pageState.value = 'alreadyUsed'
    } else if (invitation.value.expired) {
      pageState.value = 'expired'
    } else {
      // Token is valid and unused — show the form (Step 2)
      pageState.value = 'form'
    }
  } catch {
    // Token not found in the system
    pageState.value = 'invalid'
  }
})

// ── Step 4 → Step 5: validate inputs then show confirmation screen ─────────────
async function goToConfirm() {
  const { valid } = await formRef.value.validate()
  if (!valid) return   // Extension 4a — Vuetify shows inline errors
  confirming.value = true
}

// ── Step 6 → Step 7: confirmed, submit to backend ─────────────────────────────
async function submitRegistration() {
  submitting.value = true
  submitError.value = ''

  try {
    const token = route.query.token as string
    await registrationApi.registerStudent({
      token,
      firstName: form.value.firstName.trim(),
      lastName:  form.value.lastName.trim(),
      password:  form.value.password,
    })

    // Step 7 — success
    pageState.value = 'success'

    // Step 8 — redirect to login after a short delay so the student can read the success message
    setTimeout(() => router.push('/login'), 3000)

  } catch (err: any) {
    // Extension 2a edge case — caught on submit (token could have been used in another tab)
    const msg: string = err.response?.data?.message ?? 'Registration failed. Please try again.'
    submitError.value = msg

    if (msg.toLowerCase().includes('already been used') || msg.toLowerCase().includes('already exists')) {
      confirming.value = false
      pageState.value = 'alreadyUsed'
    }
  } finally {
    submitting.value = false
  }
}
</script>
