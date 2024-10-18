import { mande } from 'mande'
import { defineStore } from 'pinia'

export const useleedauth = defineStore('leedauth', () => {
  async function leedlogin(payload) {
    try {
      const url = localStorage.getItem('url')
      const organizer = localStorage.getItem('organizer')
      const slug = localStorage.getItem('selectedEventSlug')
      const apiToken = localStorage.getItem('api_token')
      const headers = {
        Authorization: `Device ${apiToken}`,
        Accept: 'application/json'
      }
      const api = mande(url, { headers: headers })
      const response = await api.post(`/api/v1/event/${organizer}/${slug}/exhibitors/auth`, payload)
      if (response.success) {
        if (localStorage.getItem('exhikey')) {
          localStorage.removeItem('exhikey')
        }
        localStorage.setItem('exhikey', payload.key)
      }

      return response
    } catch (error) {
      console.log('error')
    }
  }

  return { leedlogin }
})
