<script setup>
import { useLoadingStore } from '@/stores/loading'
import StandardButton from '@/components/Common/StandardButton.vue'
import { useRouter } from 'vue-router'

const loadingStore = useLoadingStore()
loadingStore.contentLoaded()

const router = useRouter()
function checkIn() {
  router.push({ name: 'eventyayevents' })
}
</script>

<template>
  <div
    class="-mt-16 grid h-screen w-full grid-cols-1 place-items-center items-center justify-center align-middle"
  >
    <StandardButton :text="'Lead Scanning'" class="btn-primary mt-6 w-full justify-center" />
    <p class="text-xl">OR</p>
    <StandardButton
      :text="'Check-In'"
      class="btn-primary mt-6 w-full justify-center"
      @click="checkIn()"
    />
  </div>
</template>
