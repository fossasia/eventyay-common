import { mande } from 'mande'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useEventyayEventStore = defineStore('eventyayEvent', () => {
  const events = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchEvents(url, apiToken, organizer) {
    loading.value = false
    error.value = null

    try {
      const api = mande(url, { headers: { Authorization: `Device ${apiToken}` } })
      console.log(`/api/v1/organizers/${organizer}/events/`)
      const response = await api.get(`/api/v1/organizers/${organizer}/events/`)
      console.log('Hello', response)
      events.value = response.results
      loading.value = true
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
