import { useCameraStore } from '@/stores/camera'
import { useEventyayApi } from '@/stores/eventyayapi'

import { mande } from 'mande'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLeadScanStore = defineStore('processLeadScan', () => {
  const cameraStore = useCameraStore()
  const message = ref('')
  const showSuccess = ref(false)
  const showError = ref(false)
  const processApi = useEventyayApi()
  const { apitoken, url, organizer, eventSlug, exikey } = processApi

  function $reset() {
    message.value = ''
    showSuccess.value = false
    showError.value = false
  }

  function showErrorMsg(msg) {
    message.value = msg
    showSuccess.value = false
    showError.value = true
  }

  function showSuccessMsg(msg) {
    message.value = msg
    showSuccess.value = true
    showError.value = false
  }

  async function scanLead() {
    const qrData = JSON.parse(cameraStore.qrCodeValue)
    console.log(qrData)
    console.log(apiToken, url, organizer, eventSlug, exikey)
    // Prepare the POST request body
    const requestBody = {
      lead: qrData.lead,
      scanned: 'null',
      scan_type: 'lead',
      device_name: 'Test'
    }

    try {
      const headers = {
        Authorization: `Device ${apitoken}`,
        Accept: 'application/json',
        Exhibitor: exikey
      }

      const api = mande(`${url}/api/v1/event/${organizer}/${eventSlug}/exhibitors/lead/create`, {
        headers: headers
      })
      const response = await api.post(requestBody)
      console.log(response)

      showSuccessMsg('Lead scanned successfully!')
    } catch (err) {
      showErrorMsg('Check-in failed: ' + err.message)
    }
  }

  return {
    message,
    showSuccess,
    showError,
    scanLead,
    $reset
  }
})
