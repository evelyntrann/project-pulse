<template>
  <v-container max-width="800">
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">Rubrics</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-plus" @click="router.push('/rubrics/new')">
        Create Rubric
      </v-btn>
    </div>

    <v-alert v-if="error" type="error" variant="tonal" density="compact" class="mb-4">
      {{ error }}
    </v-alert>

    <v-card variant="outlined">
      <v-list v-if="rubrics.length > 0" lines="two">
        <v-list-item
          v-for="rubric in rubrics"
          :key="rubric.id"
          :title="rubric.name"
          :subtitle="`${rubric.criteria.length} criteria`"
          prepend-icon="mdi-clipboard-list-outline"
          append-icon="mdi-chevron-right"
          :to="`/rubrics/${rubric.id}`"
        />
      </v-list>

      <div v-else-if="!loading" class="pa-8 text-center text-medium-emphasis">
        No rubrics yet. Create one to get started.
      </div>

      <div v-if="loading" class="pa-8 text-center">
        <v-progress-circular indeterminate color="primary" />
      </div>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { rubricsApi, type RubricSummary } from '@/api/rubrics'

const router = useRouter()

const rubrics = ref<RubricSummary[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const res = await rubricsApi.listRubrics()
    rubrics.value = res.data.data
  } catch {
    error.value = 'Failed to load rubrics.'
  } finally {
    loading.value = false
  }
})
</script>
