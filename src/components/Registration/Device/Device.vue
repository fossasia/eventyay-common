<script setup>
import QRCamera from '@/components/Common/QRCamera.vue'
import { useLoadingStore } from '@/stores/loading'
import StandardButton from '@/components/Common/StandardButton.vue'
import { useProcessDeviceStore } from '@/stores/processDevice'
import { ref } from 'vue'
const loadingStore = useLoadingStore()
loadingStore.contentLoaded()

const processDeviceStore = useProcessDeviceStore()
const showError = ref(false)
async function registerDevice() {
  const auth_token = document.getElementById('auth_token').value
  const payload = { handshake_version: 1, url: 'http://localhost', token: auth_token }
  try {
    const resp = await processDeviceStore.authDevice(JSON.stringify(payload))
  } catch (error) {
    showError.value = true
    console.error('Error registering device:', error)
  }
}
</script>

<template>
  <div
    class="-mt-16 grid h-screen w-full grid-cols-1 place-items-center items-center justify-center align-middle"
  >
    <QRCamera :qr-type="'device'" :scan-type="'Device Registration'" />
    <div v-if="showError">
      <p class="text-sm text-danger">Oops! something went wrong</p>
    </div>
  </div>
</template>
