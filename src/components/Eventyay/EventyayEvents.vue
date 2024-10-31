<script setup>
import { useLoadingStore } from '@/stores/loading'
import { useEventyayApi } from '@/stores/eventyayapi'
import { useEventyayEventStore } from '@/stores/eventyayEvent'

import { ref, onMounted, watchEffect } from 'vue'
import StandardButton from '@/components/Common/StandardButton.vue'
import { useRouter } from 'vue-router'

const loadingStore = useLoadingStore()
const router = useRouter()

const selectedEvent = ref(null)
const eventyayEventStore = useEventyayEventStore()
const { events, error } = eventyayEventStore
const processApi = useEventyayApi()
const { apitoken, url, organizer, selectedRole } = processApi

loadingStore.contentLoaded()
eventyayEventStore.fetchEvents(url, apitoken, organizer)

const submitForm = () => {
  if (selectedEvent.value) {
    const selectedEventData = events.find((event) => event.slug === selectedEvent.value)
    if (selectedEventData) {
      console.log('Selected Event:', selectedEventData)
      console.log('Selected Role:', selectedRole)
      processApi.setEventSlug(selectedEventData.slug)
      if (selectedRole === 'Exhibitor') router.push({ name: 'eventyayleedlogin' })
      if (selectedRole === 'CheckIn' || selectedRole === 'Badge Station')
        router.push({ name: 'eventyaycheckin' })
    }
  } else {
    console.error('Please select an event.')
  }
}
</script>
<template>
  <div class="-mt-16 flex h-screen flex-col justify-center">
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
          type="submit"
          text="Select Event"
          class="btn-primary mt-6 w-full justify-center"
        />
      </div>
    </form>
    <div v-if="!events.length && !error">
      No events available
      <StandardButton
        text="Refresh"
        class="btn-primary mt-6 w-1/2 justify-center"
        @click="fetchEvents(url.value, apiToken.value, organiser.value)"
      />
    </div>
  </div>
</template>
