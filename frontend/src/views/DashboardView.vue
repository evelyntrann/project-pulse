<template>
  <v-container>
    <v-row>
      <v-col>
        <v-card class="pa-6">
          <v-card-title>Welcome, {{ auth.user?.firstName }}!</v-card-title>
          <v-card-subtitle>Role: {{ auth.user?.role }}</v-card-subtitle>
          <v-card-actions>
            <v-btn color="error" variant="outlined" @click="handleLogout">Logout</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>
