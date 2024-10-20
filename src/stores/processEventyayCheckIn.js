import { useCameraStore } from '@/stores/camera'
import { useEventyayApi } from '@/stores/eventyayapi'

import { mande } from 'mande'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useProcessEventyayCheckInStore = defineStore('processEventyayCheckIn', () => {
  const cameraStore = useCameraStore()
  const message = ref('')
  const showSuccess = ref(false)
  const showError = ref(false)
  const processApi = useEventyayApi()
  const { apitoken, url, organizer, eventSlug } = processApi

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

  // Function to generate a random nonce
  function generateNonce(length = 32) {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let result = ''
    for (let i = 0; i < length; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    return result
  }

  async function getlist() {
    const api = mande(url, { headers: { Authorization: `Device ${apitoken}` } })

    // Fetch the check-in lists
    const response = await api.get(
      `/api/v1/organizers/${organizer}/events/${eventSlug}/checkinlists/`
    )

    // Extract all IDs from the results
    const listIds = response.results.map((list) => list.id.toString())
    return listIds
  }

  async function checkIn() {
    const qrData = JSON.parse(cameraStore.qrCodeValue)

    // Instead of calling getlist(), let's use a hardcoded value for now
    const checkInList = ['2']

    // Generate a random nonce
    const nonce = generateNonce()

    // Prepare the POST request body
    const requestBody = {
      secret: qrData.ticket,
      source_type: 'barcode',
      lists: checkInList,
      force: false,
      ignore_unpaid: false,
      nonce: nonce,
      datetime: null,
      questions_supported: false
    }

    try {
      const response = await fetch(`${url}/api/v1/organizers/${organizer}/checkinrpc/redeem/`, {
        method: 'POST',
        headers: {
          Authorization: `Device ${apitoken}`,
          Accept: 'application/json',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const data = await response.json()

      if (data.status === 'ok') {
        const pdfDownload = data.position.downloads.find((download) => download.output === 'pdf')
        if (pdfDownload) {
          const printWindow = window.open(pdfDownload.url, '_blank')
          printWindow.onload = function () {
            printWindow.print()
          }
        } else {
          console.warn('PDF download URL not found in the response')
        }

        showSuccessMsg(`Check-in successful for ${data.position.attendee_name}`)
      } else {
        showErrorMsg('Check-in failed: Unexpected response')
      }
    } catch (error) {
      console.error('Fetch error:', error)
      showErrorMsg('Check-in failed: ' + error.message)
    }
  }

  return {
    message,
    showSuccess,
    showError,
    checkIn,
    $reset
  }
})
