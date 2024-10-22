<script setup>
import { watch, ref } from 'vue'
import QRCamera from '@/components/Common/QRCamera.vue'
import StandardButton from '@/components/Common/StandardButton.vue'
import { useLoadingStore } from '@/stores/loading'
import { useLeadScanStore } from '@/stores/leadscan'
import { storeToRefs } from 'pinia'

const loadingStore = useLoadingStore()
const leadScanStore = useLeadScanStore()

const { message, showSuccess, showError } = storeToRefs(leadScanStore)
const countdown = ref(5)
const timerInstance = ref(null)  // To store the timer reference
const timeoutInstance = ref(null)  // To store the setTimeout reference
const notes = ref('')  // For notes input

loadingStore.contentLoaded()

function startCountdown() {
  countdown.value = 5
  // Store the interval reference
  timerInstance.value = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timerInstance.value)
    }
  }, 1000)
}

function stopTimer() {
  if (timerInstance.value) {
    clearInterval(timerInstance.value)
  }
  if (timeoutInstance.value) {
    clearTimeout(timeoutInstance.value)
  }
}

function handleNotesInput() {
  stopTimer()
  countdown.value = '...'  // Replace the countdown with dots when typing
}

function handleSave() {
  // Add your save logic here
  console.log('Saving notes:', notes.value)
  leadScanStore.$reset()
}

function handleCancel() {
  leadScanStore.$reset()
}

// Show popup for success or when we have attendee info (409 case)
watch([showSuccess, showError], ([newSuccess, newError]) => {
  if (newSuccess || (newError && message.value.attendee)) {
    showPopup()
  }
})

function showPopup() {
  notes.value = ''  // Reset notes
  startCountdown()
  // Store the timeout reference
  timeoutInstance.value = setTimeout(() => {
    leadScanStore.$reset()
  }, 5000)
}
</script>

<template>
  <div class="flex flex-col h-screen w-full justify-center items-center">
    <QRCamera qr-type="eventyaylead" scan-type="Lead-Scan" />

    <!-- Attendee Info Popup Modal -->
    <div
      v-if="(showSuccess || showError) && message.attendee"
      class="fixed inset-0 flex items-center justify-center"
    >
      <div class="rounded w-1/4 bg-white p-5 shadow-lg relative">
        <!-- Countdown display -->
        <div class="absolute top-2 right-2 w-8 h-8 flex items-center justify-center 
                    rounded-full bg-gray-200 text-gray-600 font-medium">
          {{ countdown }}
        </div>
        
        <h2 
            :class="showError ? 'text-danger mb-2 text-xl' : 'text-success mb-2 text-xl'"
        >
          {{ message.message }}
        </h2>
        <div>
          <p><b>Name:</b> {{ message.attendee.name || 'No name provided' }}</p>
          <p><b>Email:</b> {{ message.attendee.email || 'No email provided' }}</p>
          <input
              v-model="notes"
              type="text"
              class="w-full mt-2 p-2 border border-gray-300 rounded"
              placeholder="Take Notes"
              @focus="handleNotesInput"
              @input="handleNotesInput"
          />
          <div class="flex flex-row justify-around" >
            <StandardButton
              type="submit"
              text="Save"
              @click="handleSave"
              class="btn-primary w-1/4 mt-6 justify-center"
            />
            <StandardButton
              type="submit"
              text="Cancel"
              @click="handleCancel"
              class="btn-secondary w-1/4 mt-6 justify-center"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
