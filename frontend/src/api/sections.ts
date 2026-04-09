import api from './axios'

export interface SectionSummary {
  id: number
  name: string
  startDate: string
  endDate: string
  teamNames: string[]
}

export const sectionsApi = {
  findSections: (name?: string) =>
    api.get<{ success: boolean; data: SectionSummary[] }>('/sections', {
      params: name ? { name } : {},
    }),
}
