import { useCameraStore } from '@/stores/camera'
import { mande } from 'mande'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLeadScanStore = defineStore('processLeadScan', () => {
  const cameraStore = useCameraStore()
  const message = ref('')
  const showSuccess = ref(false)
  const showError = ref(false)

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

    const apiToken = localStorage.getItem('api_token')
    const url = localStorage.getItem('url')
    const key = localStorage.getItem('exhikey')
    const slug = localStorage.getItem('selectedEventSlug')

    // Prepare the POST request body
    const requestBody = {
      lead: qrData.lead,
      scanned: 'null',
      scan_type: 'lead',
      device_name: 'Test'
    }

    try {
      const headers = {
        Authorization: `Device ${apiToken}`,
        Accept: 'application/json',
        Exhibitor: key
      }

      const api = mande(`${url}/api/v1/event/admin/${slug}/exhibitors/lead/create`, {
        headers: headers
      })
      const response = await api.post(requestBody)
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
