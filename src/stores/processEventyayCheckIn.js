import { useCameraStore } from '@/stores/camera'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useProcessEventyayCheckInStore = defineStore('processEventyayCheckIn', () => {
  const cameraStore = useCameraStore()
  const message = ref('')
  const showSuccess = ref(false)
  const showError = ref(false)
  function $reset() {
    message.value = ''
    showSuccess.value = false
    showError.value = false
  }

  const response = computed(() => {
    let classType = ''
    if (showSuccess.value) {
      classType = 'text-success'
    }
    if (showError.value) {
      classType = 'text-danger'
    }
    return {
      message: message.value,
      class: classType
    }
  })

  function showErrorMsg() {
    showSuccess.value = false
    showError.value = true
  }

  function showSuccessMsg() {
    showSuccess.value = true
    showError.value = false
  }

  async function checkIn() {
    const qrData = JSON.parse(cameraStore.qrCodeValue)
    const event = localStorage.getItem('selectedEventName')

    if (qrData.event === event) {
      console.log(cameraStore.qrCodeValue)
      message.value = 'Check in successful'
      showSuccessMsg()
    } else {
      console.log(qrData.event)
      console.log(event)
      message.value = 'Invalid Event'
      showErrorMsg()
    }
  }

  return {
    response,
    checkIn,
    $reset
  }
})
