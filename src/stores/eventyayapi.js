import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useEventyayApi = defineStore(
  'processApi',
  () => {
    const apitoken = ref('')
    const url = ref('')
    const organizer = ref('')
    const eventSlug = ref('')
    const exikey = ref('')
    const selectedRole = ref('')
    const exhiname = ref('')
    const boothname = ref('')
    const boothid = ref('')

    function $reset() {
      apitoken.value = ''
      url.value = ''
      organizer.value = ''
      eventSlug.value = ''
      exikey.value = ''
      selectedRole.value = ''
      exhiname.value = ''
      boothname.value = ''
      boothid.value = ''
    }

    function setApiCred(newToken, newUrl, newOrg) {
      apitoken.value = newToken
      url.value = newUrl
      organizer.value = newOrg
    }

    function setEventSlug(slug) {
      eventSlug.value = slug
    }

    function setExhibitor(key, name, booth, bid) {
      exikey.value = key
      exhiname.value = name
      boothname.value = booth
      boothid.value = bid
    }

    function setRole(role) {
      selectedRole.value = role
    }

    return {
      $reset,
      setApiCred,
      setEventSlug,
      setExhibitor,
      selectedRole,
      setRole,
      apitoken,
      url,
      organizer,
      eventSlug,
      exikey,
      exhiname,
      boothname,
      boothid
    }
  },
  {
    persist: true
  }
)
