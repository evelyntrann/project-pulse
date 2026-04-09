<template>
  <v-app>
    <!-- Navigation drawer -->
    <v-navigation-drawer v-model="drawer" permanent>
      <v-list-item
        title="Project Pulse"
        subtitle="TCU Senior Design"
        class="py-4"
      />
      <v-divider />
      <v-list density="compact" nav>
        <v-list-item
          v-for="item in navItems"
          :key="item.to"
          :prepend-icon="item.icon"
          :title="item.title"
          :to="item.to"
          rounded="lg"
        />
      </v-list>
      <template #append>
        <v-divider />
        <v-list density="compact" nav class="pa-2">
          <v-list-item
            prepend-icon="mdi-account-circle"
            :title="`${auth.user?.firstName} ${auth.user?.lastName}`"
            :subtitle="auth.user?.role"
          />
          <v-list-item
            prepend-icon="mdi-logout"
            title="Logout"
            rounded="lg"
            @click="handleLogout"
          />
        </v-list>
      </template>
    </v-navigation-drawer>

    <!-- Main content -->
    <v-main>
      <div class="pa-6">
        <router-view />
      </div>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const drawer = ref(true)

const navItems = [
  { title: 'Sections', icon: 'mdi-view-list', to: '/sections' },
  { title: 'Teams', icon: 'mdi-account-group', to: '/teams' },
]

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>
