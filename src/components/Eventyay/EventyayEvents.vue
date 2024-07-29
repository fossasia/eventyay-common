<script setup>
import { useLoadingStore } from '@/stores/loading'
import { ref, onMounted, watchEffect } from 'vue'
import { useEventyayEventStore } from '@/stores/eventyayEvent'
import StandardButton from '@/components/Common/StandardButton.vue'
import { useRouter } from 'vue-router'
const loadingStore = useLoadingStore()

const apiToken = ref('')
const organiser = ref('')
const url = ref('')
const eventyayEventStore = useEventyayEventStore()
const selectedEvent = ref(null)
const router = useRouter()
const { events, loading, error, fetchEvents } = eventyayEventStore

watchEffect(() => {
  apiToken.value = localStorage.getItem('api_token')
  organiser.value = localStorage.getItem('organizer')
  url.value = localStorage.getItem('url')

  if (apiToken.value && organiser.value && url.value) {
    fetchEvents(url.value, apiToken.value, organiser.value)
    loadingStore.contentLoaded()
  }
})

const submitForm = () => {
  if (selectedEvent.value) {
    localStorage.setItem('selectedEvent', selectedEvent.value)
    router.push({ name: 'eventyaycheckin' })
    console.log('Selected event:', selectedEvent.value)
  } else {
    console.error('Please select an event.')
  }
}
</script>
<template>
  <div class="-mt-16 flex h-screen flex-col justify-center">
    <div v-if="loading">Loading events...</div>
    <div v-if="error" class="text-danger">{{ error }}</div>
    <form v-if="events.length" @submit.prevent="submitForm">
      <div v-for="event in events" :key="event.slug" class="mb-2">
        <label>
          <input type="radio" :value="event.slug" v-model="selectedEvent" />
          {{ event.name.en }}
        </label>
      </div>
      <div>
        <StandardButton
          :type="'submit'"
          :text="'Select Event'"
          class="btn-primary mt-6 w-full justify-center"
        />
      </div>
    </form>
    <div v-if="!loading && !events.length && !error">
      No events available
      <StandardButton
        :text="'Refresh'"
        class="btn-primary mt-6 w-full justify-center"
        @click="fetchEvents(url.value, apiToken.value, organiser.value)"
      />
    </div>
  </div>
</template>
