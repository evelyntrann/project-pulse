<template>
  <v-container>
    <!-- Back -->
    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.push(`/sections/${sectionId}`)"
    >
      Back to Section
    </v-btn>

    <h1 class="text-h5 font-weight-bold mb-1">Manage Instructors</h1>
    <p class="text-body-2 text-grey mb-6">Section ID: {{ sectionId }}</p>

    <!-- Loading -->
    <v-row v-if="loading" justify="center" class="mt-8">
      <v-progress-circular indeterminate color="primary" />
    </v-row>

    <template v-else>
      <!-- ── Current instructors ─────────────────────────────────────── -->
      <h2 class="text-h6 font-weight-bold mb-3">Current Instructors</h2>
      <v-card variant="outlined" class="mb-6">
        <v-card-text
          v-if="instructors.length === 0"
          class="text-grey text-center py-6"
        >
          No instructors assigned to this section yet.
        </v-card-text>
        <v-list v-else density="compact">
          <v-list-item
            v-for="inst in instructors"
            :key="inst.id"
            :title="`${inst.firstName} ${inst.lastName}`"
            :subtitle="inst.email"
            prepend-icon="mdi-account-tie"
          >
            <template #append>
              <v-btn
                icon="mdi-delete"
                variant="text"
                color="error"
                size="small"
                :loading="removingId === inst.id"
                @click="handleRemove(inst)"
              />
            </template>
          </v-list-item>
        </v-list>
      </v-card>

      <!-- ── Assign by user ID ───────────────────────────────────────── -->
      <h2 class="text-h6 font-weight-bold mb-3">Assign by User ID</h2>
      <v-card variant="outlined" class="mb-6 pa-4">
        <v-form ref="assignFormRef" @submit.prevent="handleAssign">
          <v-row align="center">
            <v-col cols="12" sm="6" md="4">
              <v-text-field
                v-model="assignId"
                label="Instructor User ID"
                type="number"
                variant="outlined"
                density="compact"
                hide-details="auto"
                :rules="[v => !!v || 'Required']"
              />
            </v-col>
            <v-col cols="auto">
              <v-btn
                type="submit"
                color="primary"
                :loading="assigning"
              >
                Assign
              </v-btn>
            </v-col>
          </v-row>
        </v-form>
      </v-card>

      <!-- ── Invite or add by email ─────────────────────────────────── -->
      <h2 class="text-h6 font-weight-bold mb-3">Invite or Add by Email</h2>
      <v-card variant="outlined" class="mb-6 pa-4">
        <p class="text-body-2 text-grey mb-3">
          Existing instructors will be added directly. New emails will receive an invitation.
        </p>
        <v-form ref="inviteFormRef" @submit.prevent="handleInviteOrAdd">
          <v-textarea
            v-model="emailInput"
            label="Email addresses (one per line or semicolon-separated)"
            variant="outlined"
            density="compact"
            rows="3"
            hide-details="auto"
            class="mb-3"
          />

          <!-- Parsed email chips -->
          <div v-if="parsedEmails.length" class="mb-3">
            <v-chip
              v-for="email in parsedEmails"
              :key="email"
              size="small"
              closable
              class="mr-1 mb-1"
              @click:close="removeEmail(email)"
            >
              {{ email }}
            </v-chip>
          </div>

          <v-btn
            type="submit"
            color="primary"
            :loading="inviting"
            :disabled="parsedEmails.length === 0"
          >
            Invite / Add
          </v-btn>
        </v-form>

        <!-- Result summary -->
        <v-alert
          v-if="inviteResult"
          type="success"
          variant="tonal"
          class="mt-4"
        >
          <div v-if="inviteResult.added.length">
            <strong>Added:</strong> {{ inviteResult.added.join(', ') }}
          </div>
          <div v-if="inviteResult.invited.length">
            <strong>Invited:</strong> {{ inviteResult.invited.join(', ') }}
          </div>
          <div v-if="inviteResult.alreadyExists.length">
            <strong>Already in section:</strong> {{ inviteResult.alreadyExists.join(', ') }}
          </div>
        </v-alert>
      </v-card>
    </template>

    <!-- Remove confirm dialog -->
    <v-dialog v-model="confirmRemove" max-width="400">
      <v-card>
        <v-card-title class="text-h6 pa-4">Remove Instructor?</v-card-title>
        <v-card-text class="pb-2">
          Remove <strong>{{ removingInstructor?.firstName }} {{ removingInstructor?.lastName }}</strong>
          from this section?
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer />
          <v-btn variant="text" @click="confirmRemove = false">Cancel</v-btn>
          <v-btn
            color="error"
            variant="tonal"
            :loading="removingId === removingInstructor?.id"
            @click="confirmHandleRemove"
          >
            Remove
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="3000" location="bottom">
      {{ snackbarMessage }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { sectionsApi, type UserDto, type InviteOrAddResult } from '@/api/sections'

const router = useRouter()
const route = useRoute()

const sectionId = computed(() => Number(route.params.id))

const instructors = ref<UserDto[]>([])
const loading = ref(false)

const assignFormRef = ref()
const assignId = ref('')
const assigning = ref(false)

const inviteFormRef = ref()
const emailInput = ref('')
const inviting = ref(false)
const inviteResult = ref<InviteOrAddResult | null>(null)

const confirmRemove = ref(false)
const removingInstructor = ref<UserDto | null>(null)
const removingId = ref<number | null>(null)

const snackbar = ref(false)
const snackbarMessage = ref('')
const snackbarColor = ref<'success' | 'error'>('success')

// Parse textarea into unique valid emails
const parsedEmails = computed(() => {
  return [...new Set(
    emailInput.value
      .split(/[;\n,]+/)
      .map(e => e.trim())
      .filter(e => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e))
  )]
})

onMounted(loadInstructors)

async function loadInstructors() {
  loading.value = true
  try {
    const res = await sectionsApi.getInstructors(sectionId.value)
    instructors.value = res.data.data
  } catch {
    showSnackbar('Failed to load instructors.', 'error')
  } finally {
    loading.value = false
  }
}

async function handleAssign() {
  const { valid } = await assignFormRef.value.validate()
  if (!valid) return
  assigning.value = true
  try {
    await sectionsApi.assignInstructor(sectionId.value, Number(assignId.value))
    assignId.value = ''
    assignFormRef.value.reset()
    await loadInstructors()
    showSnackbar('Instructor assigned successfully.', 'success')
  } catch (e: any) {
    const msg = e.response?.data?.error ?? 'Failed to assign instructor.'
    showSnackbar(msg, 'error')
  } finally {
    assigning.value = false
  }
}

function handleRemove(instructor: UserDto) {
  removingInstructor.value = instructor
  confirmRemove.value = true
}

async function confirmHandleRemove() {
  if (!removingInstructor.value) return
  removingId.value = removingInstructor.value.id
  try {
    await sectionsApi.removeInstructor(sectionId.value, removingInstructor.value.id)
    confirmRemove.value = false
    await loadInstructors()
    showSnackbar('Instructor removed.', 'success')
  } catch (e: any) {
    const msg = e.response?.data?.error ?? 'Failed to remove instructor.'
    showSnackbar(msg, 'error')
  } finally {
    removingId.value = null
    removingInstructor.value = null
  }
}

async function handleInviteOrAdd() {
  if (parsedEmails.value.length === 0) return
  inviting.value = true
  inviteResult.value = null
  try {
    const res = await sectionsApi.inviteOrAddInstructors(sectionId.value, parsedEmails.value)
    inviteResult.value = res.data.data
    emailInput.value = ''
    await loadInstructors()
    showSnackbar('Instructor invite/add completed.', 'success')
  } catch (e: any) {
    const msg = e.response?.data?.error ?? 'Failed to invite or add instructors.'
    showSnackbar(msg, 'error')
  } finally {
    inviting.value = false
  }
}

function removeEmail(email: string) {
  const lines = emailInput.value.split(/[;\n,]+/).map(e => e.trim()).filter(Boolean)
  emailInput.value = lines.filter(e => e !== email).join('\n')
}

function showSnackbar(msg: string, color: 'success' | 'error') {
  snackbarMessage.value = msg
  snackbarColor.value = color
  snackbar.value = true
}
</script>
