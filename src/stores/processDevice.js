import { useCameraStore } from '@/stores/camera'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useProcessDeviceStore = defineStore('processDevice', () => {
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

  async function authDevice(val) {
    try {
      const qrData = JSON.parse(val)
      console.log(qrData)
      if (qrData.handshake_version > 1) {
        message.value = 'Unsupported handshake version'
        showErrorMsg()
        return
      } else {
        console.log('Handshake version is 1')
      }

      const payload = {
        token: qrData.token,
        hardware_brand: 'Test',
        hardware_model: 'test',
        os_name: 'Test',
        os_version: '2.3.6',
        software_brand: 'check-in',
        software_version: 'x.x'
      }
      let url = qrData.url
      if (url.includes('localhost')) {
        url = `${url}:8000` // Add your desired port number here
      }
      console.log(url)
      const api = mande(url, { headers: { 'Content-Type': 'application/json' } })
      const response = await api.post('/api/v1/device/initialize', payload)
      console.log(response)
      if (response) {
        const data = response
        console.log(data.api_token)
        showSuccessMsg()
      } else {
        console.log('Something happend')
        showErrorMsg()
      }
    } catch (error) {
      console.log('Error in catch')
      showErrorMsg()
    }
  }

  return {
    response,
    $reset,
    authDevice
  }
})
