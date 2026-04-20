<template>
  <v-container max-width="800">
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">Instructors</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-email-plus-outline" @click="router.push('/instructors/invite')">
        Invite Instructors
      </v-btn>
    </div>

    <v-card variant="outlined">
      <v-card-text class="pa-8 text-center text-medium-emphasis">
        Instructor search will be available here once implemented (UC-21).
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>
