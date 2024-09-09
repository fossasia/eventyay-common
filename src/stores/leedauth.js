import { mande } from 'mande'
import { defineStore } from 'pinia'

export const useleedauth = defineStore('leedauth', () => {
  async function leedlogin(payload) {
    try {
      console.log('leedlogin')
      const url = localStorage.getItem('url')
      const organizer = localStorage.getItem('organizer')
      const slug = localStorage.getItem('selectedEventSlug')
      const apiToken = localStorage.getItem('api_token')
      console.log(url, organizer, slug, apiToken, payload)
      const headers = {
        Authorization: `Device ${apiToken}`,
        Accept: 'application/json'
      }
      const api = mande(url, { headers: headers })
      const response = await api.post(`/api/v1/event/${organizer}/${slug}/exhibitors/auth`, payload)
      console.log('here', response)
      return response
    } catch (error) {
      console.log(error)
      console.log('error')
      return 'wt'
    }
  }

  return { leedlogin }
})
