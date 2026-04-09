<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="6" class="text-center">
        <v-icon size="80" color="error">mdi-lock</v-icon>
        <h1 class="text-h4 mt-4">Access Denied</h1>
        <p class="text-body-1 mt-2">You do not have permission to view this page.</p>
        <v-btn class="mt-4" color="primary" @click="router.back()">Go Back</v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>
