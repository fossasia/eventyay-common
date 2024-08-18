import { useApiStore } from '@/stores/api'
import { useCameraStore } from '@/stores/camera'
import { mande } from 'mande'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
export const useProcessDeviceStore = defineStore('processDevice', () => {
  const cameraStore = useCameraStore()
  const router = useRouter()
  const message = ref('')
  const showSuccess = ref(false)
  const showError = ref(false)
  const apiStore = useApiStore()
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
      if (qrData.handshake_version > 1) {
        message.value = 'Unsupported handshake version'
        showErrorMsg()
        return
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
      const port = import.meta.env.VITE_LOCAL_PORT || 3000
      if (url.includes('localhost')) {
        url = `${url}:${port}` // Add your desired port number here
      }
      const api = mande(url, { headers: { 'Content-Type': 'application/json' } })
      const response = await api.post('/api/v1/device/initialize', payload)
      if (response) {
        apiStore.newSession(true)
        const data = response
        localStorage.setItem('api_token', data.api_token)
        localStorage.setItem('organizer', data.organizer)
        localStorage.setItem('url', url)
        router.push({ name: 'eventyayselect' })
        showSuccessMsg()
      } else {
        showErrorMsg()
      }
    } catch (error) {
      showErrorMsg()
    }
  }

  return {
    response,
    $reset,
    authDevice
  }
})
