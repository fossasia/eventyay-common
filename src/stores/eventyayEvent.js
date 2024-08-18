import { mande } from 'mande'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useEventyayEventStore = defineStore('eventyayEvent', () => {
  const events = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchEvents(url, apiToken, organizer) {
    loading.value = true
    error.value = null

    try {
      const api = mande(url, { headers: { Authorization: `Device ${apiToken}` } })
      const response = await api.get(`/api/v1/organizers/${organizer}/events/`)
      events.value = response.results
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  return {
    events,
    loading,
    error,
    fetchEvents
  }
})
