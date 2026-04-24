<template>
  <v-app>
    <!-- ── Navigation Drawer ────────────────────────────────────────────── -->
    <v-navigation-drawer v-model="drawer" permanent width="240">

      <!-- App title -->
      <v-list-item
        prepend-icon="mdi-pulse"
        title="Project Pulse"
        subtitle="TCU Senior Design"
        class="py-4"
      />
      <v-divider />

      <!-- Nav items -->
      <v-list density="compact" nav class="mt-1">
        <template v-for="item in navItems" :key="item.title">

          <!-- Group with children (sub-menu) -->
          <v-list-group v-if="item.children" :value="item.title">
            <template #activator="{ props }">
              <v-list-item
                v-bind="props"
                :prepend-icon="item.icon"
                :title="item.title"
                rounded="lg"
              />
            </template>
            <v-list-item
              v-for="child in item.children"
              :key="child.to"
              :prepend-icon="child.icon"
              :title="child.title"
              :to="child.to"
              rounded="lg"
              class="pl-8"
            />
          </v-list-group>

          <!-- Simple item -->
          <v-list-item
            v-else
            :prepend-icon="item.icon"
            :title="item.title"
            :to="item.to"
            rounded="lg"
          />

        </template>
      </v-list>

      <!-- Bottom: user info + logout -->
      <template #append>
        <v-divider />
        <v-list density="compact" nav class="pa-2">
          <v-list-item
            prepend-icon="mdi-account-circle"
            :title="`${auth.user?.firstName ?? ''} ${auth.user?.lastName ?? ''}`"
            :subtitle="auth.user?.role"
          />
          <v-list-item
            prepend-icon="mdi-logout"
            title="Logout"
            rounded="lg"
            base-color="error"
            @click="handleLogout"
          />
        </v-list>
      </template>

    </v-navigation-drawer>

    <!-- ── Main Content ──────────────────────────────────────────────────── -->
    <v-main>
      <div class="pa-6">
        <router-view />
      </div>
    </v-main>

  </v-app>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const drawer = ref(true)

// ── Nav item types ───────────────────────────────────────────────────────────
interface ChildNavItem {
  title: string
  icon: string
  to: string
}
interface NavItem {
  title: string
  icon: string
  to?: string
  children?: ChildNavItem[]
}

// ── Role-based nav items ─────────────────────────────────────────────────────
const navItems = computed((): NavItem[] => {
  const role = auth.user?.role
  const items: NavItem[] = []

  // Home — visible to all roles
  items.push({ title: 'Home', icon: 'mdi-home', to: '/home' })

  if (role === 'ADMIN') {
    items.push({ title: 'Sections', icon: 'mdi-school', to: '/sections' })
    items.push({ title: 'Teams',    icon: 'mdi-account-group', to: '/teams' })
    items.push({ title: 'Rubrics',     icon: 'mdi-clipboard-list-outline', to: '/rubrics' })
    items.push({ title: 'Students',    icon: 'mdi-account-multiple',       to: '/students' })
    items.push({ title: 'Instructors', icon: 'mdi-account-tie',            to: '/instructors' })
  }

  if (role === 'INSTRUCTOR') {
    items.push({ title: 'Teams',    icon: 'mdi-account-group',    to: '/teams' })
    items.push({ title: 'Students', icon: 'mdi-account-multiple', to: '/students' })
    items.push({ title: 'Account',  icon: 'mdi-account-edit',     to: '/profile' })
  }

  if (role === 'STUDENT') {
    items.push({ title: 'Account', icon: 'mdi-account-edit', to: '/profile' })
    // Micah → UC-27–29: add WAR + Peer Eval when implemented
  }

  return items
})

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>
