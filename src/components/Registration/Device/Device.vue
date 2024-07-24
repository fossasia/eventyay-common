<script setup>
import QRCamera from '@/components/Common/QRCamera.vue'
import { useLoadingStore } from '@/stores/loading'
import StandardButton from '@/components/Common/StandardButton.vue'
import { useProcessDeviceStore } from '@/stores/processDevice'
const loadingStore = useLoadingStore()
loadingStore.contentLoaded()

const processDeviceStore = useProcessDeviceStore()

async function registerDevice() {
  const auth_token = document.getElementById('auth_token').value
  const payload = { handshake_version: 1, url: 'http://localhost', token: auth_token }
  await processDeviceStore.authDevice(JSON.stringify(payload))
}
</script>

<template>
  <div
    class="-mt-16 grid h-screen w-full grid-cols-1 place-items-center items-center justify-center align-middle"
  >
    <QRCamera :qr-type="'device'" :scan-type="'Device Registration'" />
    <div>
      <input type="text" id="auth_token" placeholder="Device Key" class="input" />
      <StandardButton
        :text="'Register Device'"
        class="btn-primary mt-6 w-full justify-center"
        @click="registerDevice()"
      />
    </div>
  </div>
</template>
