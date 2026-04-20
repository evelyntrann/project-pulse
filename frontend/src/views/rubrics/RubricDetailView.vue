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

    <div v-if="loading" class="text-center pa-8">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <v-alert v-else-if="error" type="error" variant="tonal">{{ error }}</v-alert>

    <template v-else-if="rubric">
      <div class="d-flex align-center mb-6">
        <h1 class="text-h5 font-weight-bold">{{ rubric.name }}</h1>
      </div>

      <v-card variant="outlined">
        <v-card-title class="text-body-1 font-weight-bold pa-4 pb-0">
          Criteria ({{ rubric.criteria.length }})
        </v-card-title>
        <v-card-text class="pa-4 pt-2">
          <v-table>
            <thead>
              <tr>
                <th class="text-left" style="width: 48px">#</th>
                <th class="text-left">Name</th>
                <th class="text-left">Description</th>
                <th class="text-right">Max Score</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in rubric.criteria" :key="c.id">
                <td class="text-medium-emphasis">{{ c.orderIndex }}</td>
                <td class="font-weight-medium">{{ c.name }}</td>
                <td class="text-medium-emphasis">{{ c.description }}</td>
                <td class="text-right">{{ c.maxScore }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { rubricsApi, type RubricSummary } from '@/api/rubrics'

const router = useRouter()
const route = useRoute()

const rubric = ref<RubricSummary | null>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const res = await rubricsApi.getRubric(Number(route.params.id))
    rubric.value = res.data.data
  } catch {
    error.value = 'Rubric not found.'
  } finally {
    loading.value = false
  }
})
</script>
