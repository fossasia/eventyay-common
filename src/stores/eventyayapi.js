import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useEventyayApi = defineStore('processApi', () => {

  const apitoken = ref('')
  const url = ref('')
  const organizer = ref('')
  const eventSlug = ref('')
  const exikey = ref('')
  const selectedRole = ref('')

  function setApiCred(newToken, newUrl, newOrg) {
    apitoken.value = newToken
    url.value = newUrl
    organizer.value = newOrg
  }

  function setEventSlug(slug) {
    eventSlug.value = slug
  }

  function setExhibitorKey(key) {
    exikey.value = key
  }

  function setRole(role) {
    selectedRole.value = role
  }

  return {
    setApiCred,
    setEventSlug,
    setExhibitorKey,
    selectedRole,
    setRole,
    apitoken,
    url,
    organizer,
    eventSlug,
    exikey
  }
})
