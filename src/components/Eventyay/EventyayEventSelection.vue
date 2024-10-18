<script setup>
import { useLoadingStore } from '@/stores/loading'
import StandardButton from '@/components/Common/StandardButton.vue'
import { useRouter } from 'vue-router'

const loadingStore = useLoadingStore()
loadingStore.contentLoaded()

const router = useRouter()
function checkIn() {
  router.push({ name: 'eventyaycheckin' })
}
function leadscan() {
  router.push({ name: 'eventyayleedlogin' })
}
</script>

<template>
  <div
    class="-mt-16 grid h-screen w-full grid-cols-1 place-items-center items-center justify-center align-middle"
  >
    <div class="flex h-1/4 w-1/2 flex-col items-center justify-around">
      <StandardButton
        :text="'Lead Scanning'"
        class="btn-primary h-14 w-full justify-center"
        @click="leadscan()"
      />
      <p class="text-xl">OR</p>
      <StandardButton
        :text="'Check-In'"
        class="btn-primary h-14 w-full justify-center"
        @click="checkIn()"
      />
    </div>
  </div>
</template>
