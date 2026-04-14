<template>
  <v-container max-width="760">
    <!-- Back button -->
    <v-btn
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4 pl-0"
      @click="router.push('/rubrics')"
    >
      Back to Rubrics
    </v-btn>

    <h1 class="text-h5 font-weight-bold mb-6">Create New Rubric</h1>

    <!-- Rubric Name -->
    <v-card variant="outlined" class="mb-4">
      <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">Rubric Details</v-card-title>
      <v-card-text class="pa-4">
        <v-form ref="nameFormRef" @submit.prevent>
          <v-text-field
            v-model="rubricName"
            label="Rubric Name"
            placeholder="e.g. Peer Eval Rubric v1"
            variant="outlined"
            density="comfortable"
            :rules="nameRules"
            :error-messages="duplicateError"
            @input="duplicateError = ''"
          />
        </v-form>
      </v-card-text>
    </v-card>

    <!-- Criteria -->
    <v-card variant="outlined" class="mb-4">
      <v-card-title class="text-body-1 font-weight-bold pa-4 pb-2">Criteria</v-card-title>
      <v-card-text class="pa-4 pt-0">
        <v-form ref="criteriaFormRef" @submit.prevent>
          <div
            v-for="(criterion, index) in criteria"
            :key="index"
            class="mb-4"
          >
            <div class="d-flex align-center mb-2">
              <span class="text-body-2 font-weight-medium text-medium-emphasis">
                Criterion {{ index + 1 }}
              </span>
              <v-spacer />
              <v-btn
                v-if="criteria.length > 1"
                icon="mdi-close"
                size="x-small"
                variant="text"
                color="error"
                @click="removeCriterion(index)"
              />
            </div>

            <v-row dense>
              <v-col cols="12" sm="8">
                <v-text-field
                  v-model="criterion.name"
                  label="Criterion Name"
                  placeholder="e.g. Quality of work"
                  variant="outlined"
                  density="comfortable"
                  :rules="requiredRule"
                />
              </v-col>
              <v-col cols="12" sm="4">
                <v-text-field
                  v-model.number="criterion.maxScore"
                  label="Max Score"
                  type="number"
                  min="0.01"
                  step="0.01"
                  variant="outlined"
                  density="comfortable"
                  :rules="maxScoreRules"
                />
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="criterion.description"
                  label="Description"
                  placeholder="e.g. How do you rate the quality of this teammate's work?"
                  variant="outlined"
                  density="comfortable"
                  :rules="requiredRule"
                />
              </v-col>
            </v-row>

            <v-divider v-if="index < criteria.length - 1" class="mt-2" />
          </div>
        </v-form>

        <v-btn
          variant="tonal"
          prepend-icon="mdi-plus"
          size="small"
          class="mt-2"
          @click="addCriterion"
        >
          Add Criterion
        </v-btn>
      </v-card-text>
    </v-card>

    <!-- Actions -->
    <v-row class="mt-2">
      <v-col cols="auto">
        <v-btn variant="outlined" @click="router.push('/rubrics')">Cancel</v-btn>
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" @click="openConfirm">Review & Create</v-btn>
      </v-col>
    </v-row>

    <!-- Confirm Dialog -->
    <v-dialog v-model="confirmDialog" max-width="560">
      <v-card>
        <v-card-title class="text-h6 pa-6 pb-2">Confirm New Rubric</v-card-title>
        <v-card-text class="pa-6 pt-2">
          <p class="text-body-2 text-medium-emphasis mb-1">Rubric Name</p>
          <p class="text-body-1 font-weight-medium mb-4">{{ rubricName }}</p>

          <p class="text-body-2 text-medium-emphasis mb-2">Criteria ({{ criteria.length }})</p>
          <v-table density="compact">
            <thead>
              <tr>
                <th class="text-left">#</th>
                <th class="text-left">Name</th>
                <th class="text-left">Description</th>
                <th class="text-right">Max Score</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(c, i) in criteria" :key="i">
                <td>{{ i + 1 }}</td>
                <td>{{ c.name }}</td>
                <td class="text-medium-emphasis">{{ c.description }}</td>
                <td class="text-right">{{ c.maxScore }}</td>
              </tr>
            </tbody>
          </v-table>

          <v-alert v-if="submitError" type="error" variant="tonal" density="compact" class="mt-4">
            {{ submitError }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-btn variant="outlined" @click="confirmDialog = false">Modify</v-btn>
          <v-spacer />
          <v-btn color="primary" :loading="submitting" @click="submit">Confirm & Create</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { rubricsApi } from '@/api/rubrics'

const router = useRouter()

const nameFormRef = ref()
const criteriaFormRef = ref()
const confirmDialog = ref(false)
const submitting = ref(false)
const submitError = ref('')
const duplicateError = ref('')

const rubricName = ref('')

interface CriterionDraft {
  name: string
  description: string
  maxScore: number | null
}

const criteria = ref<CriterionDraft[]>([
  { name: '', description: '', maxScore: null },
])

const requiredRule = [(v: string) => !!v?.trim() || 'Required']

const nameRules = [
  (v: string) => !!v?.trim() || 'Rubric name is required',
]

const maxScoreRules = [
  (v: number | null) => v !== null && v !== undefined || 'Max score is required',
  (v: number | null) => (v !== null && v > 0) || 'Max score must be a positive number',
]

function addCriterion() {
  criteria.value.push({ name: '', description: '', maxScore: null })
}

function removeCriterion(index: number) {
  criteria.value.splice(index, 1)
}

async function openConfirm() {
  const [nameResult, criteriaResult] = await Promise.all([
    nameFormRef.value.validate(),
    criteriaFormRef.value.validate(),
  ])
  if (!nameResult.valid || !criteriaResult.valid) return
  submitError.value = ''
  confirmDialog.value = true
}

async function submit() {
  submitting.value = true
  submitError.value = ''
  try {
    const payload = {
      name: rubricName.value.trim(),
      criteria: criteria.value.map((c, i) => ({
        name: c.name.trim(),
        description: c.description.trim(),
        maxScore: c.maxScore as number,
        orderIndex: i + 1,
      })),
    }
    const res = await rubricsApi.createRubric(payload)
    confirmDialog.value = false
    router.push(`/rubrics/${res.data.data.id}`)
  } catch (err: any) {
    const status = err.response?.status
    const message = err.response?.data?.error || err.response?.data?.message
    if (status === 409) {
      duplicateError.value = message || 'A rubric with this name already exists'
      confirmDialog.value = false
    } else {
      submitError.value = message || 'Failed to create rubric. Please try again.'
    }
  } finally {
    submitting.value = false
  }
}
</script>
