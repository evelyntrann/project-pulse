import { defineStore } from 'pinia'
import { ref } from 'vue'
import { sectionsApi, type SectionSummary } from '@/api/sections'

export const useSectionsStore = defineStore('sections', () => {
  const sections = ref<SectionSummary[]>([])
  const loading = ref(false)
  const error = ref('')

  async function findSections(name?: string) {
    loading.value = true
    error.value = ''
    try {
      const res = await sectionsApi.findSections(name)
      sections.value = res.data.data
    } catch {
      error.value = 'Failed to load sections'
    } finally {
      loading.value = false
    }
  }

  return { sections, loading, error, findSections }
})
