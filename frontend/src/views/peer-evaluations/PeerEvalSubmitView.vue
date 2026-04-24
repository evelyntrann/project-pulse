<template>
  <v-container max-width="900" class="py-8">
    <h1 class="text-h5 font-weight-bold mb-6">Peer Evaluation</h1>

    <!-- Week selector -->
    <v-card variant="outlined" class="mb-6">
      <v-card-text class="pa-4">
        <v-select
          v-model="selectedWeek"
          :items="weekOptions"
          item-title="label"
          item-value="date"
          label="Select a week"
          variant="outlined"
          density="comfortable"
          :loading="loadingWeeks"
          :disabled="loadingWeeks"
          no-data-text="No active weeks available for your section"
          @update:model-value="onWeekSelected"
          hide-details
        />
      </v-card-text>
    </v-card>

    <!-- Not enrolled / not in team -->
    <v-alert v-if="noSectionError" type="info" variant="tonal" class="mb-6">
      You are not enrolled in a section yet. Ask your admin for an invite link.
    </v-alert>
    <v-alert v-if="noTeamError" type="info" variant="tonal" class="mb-6">
      You are not assigned to a team yet. Contact your instructor.
    </v-alert>

    <!-- Loading context -->
    <div v-if="loadingContext" class="d-flex justify-center py-12">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <!-- ── Step: Form ──────────────────────────────────────────────────────── -->
    <template v-if="context && step === 'form'">

      <v-alert v-if="isEditing" type="info" variant="tonal" density="compact" class="mb-5">
        You have already submitted evaluations for this week. Your changes will update the submission.
      </v-alert>

      <v-form ref="formRef">
        <v-card
          v-for="evalForm in sortedEvalForms"
          :key="evalForm.evaluateeId"
          variant="outlined"
          class="mb-5"
        >
          <v-card-title class="pa-5 pb-3 d-flex align-center gap-2">
            {{ getMemberName(evalForm.evaluateeId) }}
            <v-chip
              v-if="isCurrentUser(evalForm.evaluateeId)"
              color="primary"
              size="small"
              variant="tonal"
            >
              Self-Evaluation
            </v-chip>
          </v-card-title>

          <v-card-text class="pa-5 pt-0">
            <!-- Score inputs — one per rubric criterion -->
            <div
              v-if="context.criteria.length > 0"
              class="d-flex gap-4 flex-wrap mb-4"
            >
              <v-text-field
                v-for="criterion in context.criteria"
                :key="criterion.id"
                v-model="evalForm.scores[criterion.id]"
                :label="`${criterion.name} (1–${criterion.maxScore})`"
                :hint="criterion.description"
                persistent-hint
                type="number"
                :min="1"
                :max="criterion.maxScore"
                variant="outlined"
                density="comfortable"
                style="min-width: 160px; flex: 1 1 160px; max-width: 240px"
                :rules="scoreRules(criterion.maxScore)"
              />
            </div>

            <v-textarea
              v-model="evalForm.publicComment"
              label="Public Comment (optional)"
              hint="This comment will be shared with the student being evaluated."
              persistent-hint
              variant="outlined"
              density="comfortable"
              rows="2"
              class="mb-4"
            />

            <v-textarea
              v-model="evalForm.privateComment"
              label="Private Comment (optional)"
              hint="This comment is only visible to the instructor."
              persistent-hint
              variant="outlined"
              density="comfortable"
              rows="2"
            />
          </v-card-text>
        </v-card>
      </v-form>

      <div class="d-flex justify-end mt-2">
        <v-btn color="primary" size="large" @click="goToReview">
          Review Submission
        </v-btn>
      </div>
    </template>

    <!-- ── Step: Review ────────────────────────────────────────────────────── -->
    <template v-if="context && step === 'review'">
      <p class="text-subtitle-1 font-weight-medium mb-4">
        Review your evaluations before submitting.
      </p>

      <v-card
        v-for="evalForm in sortedEvalForms"
        :key="evalForm.evaluateeId"
        variant="outlined"
        class="mb-4"
      >
        <v-card-title class="pa-5 pb-2 d-flex align-center gap-2">
          {{ getMemberName(evalForm.evaluateeId) }}
          <v-chip
            v-if="isCurrentUser(evalForm.evaluateeId)"
            color="primary"
            size="small"
            variant="tonal"
          >
            Self
          </v-chip>
        </v-card-title>

        <v-card-text class="pa-5 pt-0">
          <v-list density="compact" variant="outlined" rounded="lg">
            <template v-for="(criterion, ci) in context.criteria" :key="criterion.id">
              <v-list-item
                :title="criterion.name"
                :subtitle="`${evalForm.scores[criterion.id]} / ${criterion.maxScore}`"
              />
              <v-divider v-if="ci < context.criteria.length - 1" />
            </template>

            <v-divider v-if="context.criteria.length > 0" />

            <v-list-item
              title="Public Comment"
              :subtitle="evalForm.publicComment?.trim() || '—'"
            />
            <v-divider />
            <v-list-item
              title="Private Comment (instructor only)"
              :subtitle="evalForm.privateComment?.trim() || '—'"
            />
          </v-list>
        </v-card-text>
      </v-card>

      <v-alert v-if="submitError" type="error" variant="tonal" density="compact" class="mb-4">
        {{ submitError }}
      </v-alert>

      <div class="d-flex gap-3 justify-end mt-2">
        <v-btn variant="outlined" :disabled="submitting" @click="step = 'form'">
          Modify
        </v-btn>
        <v-btn color="primary" size="large" :loading="submitting" @click="submit">
          {{ isEditing ? 'Update Submission' : 'Submit Evaluations' }}
        </v-btn>
      </div>
    </template>

    <!-- Global snackbars -->
    <v-snackbar v-model="showError" color="error" :timeout="4000" location="top">
      {{ errorMessage }}
    </v-snackbar>
    <v-snackbar v-model="showSuccess" color="success" :timeout="3000" location="top">
      Peer evaluations {{ isEditing ? 'updated' : 'submitted' }} successfully.
    </v-snackbar>

  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { peerEvaluationsApi, type PeerEvalContext } from '@/api/peerEvaluations'

const auth = useAuthStore()

// ── State ─────────────────────────────────────────────────────────────────────

const loadingWeeks   = ref(false)
const loadingContext = ref(false)
const noSectionError = ref(false)
const noTeamError    = ref(false)

const availableWeeks  = ref<string[]>([])
const submittedWeeks  = ref<string[]>([])
const selectedWeek    = ref<string | null>(null)
const context         = ref<PeerEvalContext | null>(null)

const step     = ref<'form' | 'review'>('form')
const formRef  = ref()
const submitting   = ref(false)
const submitError  = ref('')
const showError    = ref(false)
const errorMessage = ref('')
const showSuccess  = ref(false)

// One entry per team member. scores is a map of criterionId → input value string.
interface EvalForm {
  evaluateeId: number
  scores: Record<number, string>
  publicComment: string
  privateComment: string
}
const evalForms = ref<EvalForm[]>([])

// ── Computed ──────────────────────────────────────────────────────────────────

const weekOptions = computed(() =>
  availableWeeks.value.map(date => ({
    date,
    label: submittedWeeks.value.includes(date)
      ? `${formatDate(date)} — Already Submitted`
      : formatDate(date),
  }))
)

// True when the student is editing a previously submitted week.
const isEditing = computed(() =>
  !!selectedWeek.value && submittedWeeks.value.includes(selectedWeek.value)
)

// Self first, then alphabetical by last name.
const sortedEvalForms = computed(() => {
  const self = evalForms.value.find(f => f.evaluateeId === auth.user?.id)
  const others = evalForms.value.filter(f => f.evaluateeId !== auth.user?.id)
  return self ? [self, ...others] : others
})

// ── Lifecycle ─────────────────────────────────────────────────────────────────

onMounted(async () => {
  loadingWeeks.value = true
  noSectionError.value = false
  try {
    const [weeksRes, submittedRes] = await Promise.all([
      peerEvaluationsApi.getAvailableWeeks(),
      peerEvaluationsApi.getMySubmittedWeeks(),
    ])
    availableWeeks.value  = weeksRes.data.data
    submittedWeeks.value  = submittedRes.data.data
  } catch (err: any) {
    const msg = err.response?.data?.error || err.response?.data?.message || ''
    if (msg.includes('not enrolled')) {
      noSectionError.value = true
    } else {
      showErrorSnack(msg || 'Failed to load available weeks.')
    }
  } finally {
    loadingWeeks.value = false
  }
})

// ── Handlers ──────────────────────────────────────────────────────────────────

async function onWeekSelected(date: string) {
  if (!date) return
  loadingContext.value = true
  context.value = null
  evalForms.value = []
  noTeamError.value = false
  step.value = 'form'
  try {
    const res = await peerEvaluationsApi.getContext(date)
    context.value = res.data.data
    initEvalForms(res.data.data)
  } catch (err: any) {
    const msg = err.response?.data?.error || err.response?.data?.message || ''
    if (msg.includes('not assigned to a team')) {
      noTeamError.value = true
    } else {
      showErrorSnack(msg || 'Failed to load peer evaluation form.')
    }
    selectedWeek.value = null
  } finally {
    loadingContext.value = false
  }
}

// Build the reactive form array from the context.
// Pre-fills from existingEvaluations when editing a prior submission.
function initEvalForms(ctx: PeerEvalContext) {
  evalForms.value = ctx.teamMembers.map(member => {
    const existing = ctx.existingEvaluations.find(e => e.evaluateeId === member.id)
    const scores: Record<number, string> = {}
    for (const criterion of ctx.criteria) {
      const existingScore = existing?.scores.find(s => s.criterionId === criterion.id)
      scores[criterion.id] = existingScore != null ? String(existingScore.score) : ''
    }
    return {
      evaluateeId:    member.id,
      scores,
      publicComment:  existing?.publicComment  ?? '',
      privateComment: existing?.privateComment ?? '',
    }
  })
}

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
    const payload = {
      weekStartDate: selectedWeek.value!,
      evaluations: evalForms.value.map(f => ({
        evaluateeId: f.evaluateeId,
        scores: context.value!.criteria.map(c => ({
          criterionId: c.id,
          score: Number(f.scores[c.id]),
        })),
        publicComment:  f.publicComment.trim()  || null,
        privateComment: f.privateComment.trim() || null,
      })),
    }
    await peerEvaluationsApi.submit(payload)
    // Mark this week as submitted so the dropdown label updates.
    if (selectedWeek.value && !submittedWeeks.value.includes(selectedWeek.value)) {
      submittedWeeks.value = [...submittedWeeks.value, selectedWeek.value]
    }
    showSuccess.value = true
    step.value = 'form'
  } catch (err: any) {
    submitError.value = err.response?.data?.error
      || err.response?.data?.message
      || 'Failed to submit evaluations.'
  } finally {
    submitting.value = false
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function getMemberName(evaluateeId: number) {
  const m = context.value?.teamMembers.find(m => m.id === evaluateeId)
  return m ? `${m.firstName} ${m.lastName}` : 'Unknown'
}

function isCurrentUser(id: number) {
  return id === auth.user?.id
}

const scoreRules = (max: number) => [
  (v: string) => (v !== '' && v !== null && v !== undefined) || 'Score is required',
  (v: string) => Number.isInteger(Number(v)) || 'Score must be a whole number',
  (v: string) => Number(v) >= 1 || 'Minimum score is 1',
  (v: string) => Number(v) <= max || `Maximum score is ${max}`,
]

function formatDate(date: string) {
  return new Date(date + 'T00:00:00').toLocaleDateString('en-US', {
    month: 'long', day: 'numeric', year: 'numeric',
  })
}

function showErrorSnack(msg: string) {
  errorMessage.value = msg
  showError.value = true
}
</script>
