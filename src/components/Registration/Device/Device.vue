<script setup>
import QRCamera from '@/components/Common/QRCamera.vue'
import { useLoadingStore } from '@/stores/loading'

const loadingStore = useLoadingStore()
loadingStore.contentLoaded()
</script>

<template>
  <div
    class="-mt-16 grid h-screen w-full grid-cols-1 place-items-center items-center justify-center align-middle"
  >
    <QRCamera :qr-type="'device'" :scan-type="'Device Registration'" />
    <div>
      <input type="text" placeholder="Device Key" class="input" />
      <StandardButton :text="'Register Device'" class="btn-primary mt-6 w-full justify-center" />
    </div>
  </div>
</template>
