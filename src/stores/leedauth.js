import { useEventyayApi } from '@/stores/eventyayapi'
import { mande } from 'mande'
import { defineStore } from 'pinia'

export const useleedauth = defineStore('leedauth', () => {
  const processApi = useEventyayApi()
  const { apitoken, url, organizer, eventSlug } = processApi

  async function leedlogin(payload) {
    try {
      const headers = {
        Authorization: `Device ${apitoken}`,
        Accept: 'application/json'
      }
      const api = mande(url, { headers: headers })
      const response = await api.post(
        `/api/v1/event/${organizer}/${eventSlug}/exhibitors/auth`,
        payload
      )
      if (response.success) {
        processApi.setExhibitorKey(payload.key)
      }

      return response
    } catch (error) {
      console.log('error')
    }
  }

  return { leedlogin }
})
