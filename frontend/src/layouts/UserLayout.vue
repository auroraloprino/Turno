<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="logo">
        <span class="logo-name">Turno</span>
        <small>Area personale</small>
      </div>

      <div class="nav-sec">Le mie aree</div>

      <RouterLink class="nav-item" to="/user/timbratura">
        <svg class="ni" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="6" stroke="#6C63D5" stroke-width="1.3"/><path d="M8 5v3l2 2" stroke="#6C63D5" stroke-width="1.3" stroke-linecap="round"/></svg>
        Timbratura
      </RouterLink>
      <RouterLink class="nav-item" to="/user/planner">
        <svg class="ni" viewBox="0 0 16 16" fill="none"><rect x="2" y="3" width="12" height="10" rx="1.5" stroke="#888" stroke-width="1.2"/><path d="M5 7h6M5 10h4" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Planner
      </RouterLink>
      <RouterLink class="nav-item" to="/user/permessi">
        <svg class="ni" viewBox="0 0 16 16" fill="none"><rect x="2" y="2" width="12" height="12" rx="1.5" stroke="#888" stroke-width="1.2"/><path d="M5 8h6M8 5v6" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Permessi
      </RouterLink>
      <RouterLink class="nav-item" to="/user/chat">
        <svg class="ni" viewBox="0 0 16 16" fill="none"><path d="M2 4h12v7a2 2 0 01-2 2H4a2 2 0 01-2-2V4z" stroke="#888" stroke-width="1.2"/><path d="M2 4l6 5 6-5" stroke="#888" stroke-width="1.2"/></svg>
        Chat
      </RouterLink>
      <RouterLink class="nav-item" to="/user/profilo">
        <svg class="ni" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="6" r="3" stroke="#888" stroke-width="1.2"/><path d="M2 14c0-3 2.5-5 6-5s6 2 6 5" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Profilo
      </RouterLink>

      <div class="user-chip">
        <div class="av">{{ initials }}</div>
        <div>
          <div class="chip-name">{{ auth.user?.name }}</div>
          <div class="chip-role">{{ auth.user?.email }}</div>
        </div>
      </div>
      <button class="nav-item" style="margin-top:auto;color:var(--coral-dark)" @click="logout">Esci</button>
    </aside>

    <div class="main">
      <div class="topbar">
        <h1>{{ pageTitle }}</h1>
        <span class="badge">Utente</span>
      </div>
      <div class="content">
        <RouterView />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route  = useRoute()
const router = useRouter()
const auth   = useAuthStore()

const initials = computed(() =>
  auth.user?.name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() ?? '?'
)

function logout() {
  auth.logout()
  router.push('/login')
}

const pageTitle = computed(() => {
  const map: Record<string, string> = {
    '/user/timbratura': 'Timbratura',
    '/user/planner':    'Planner',
    '/user/permessi':   'Permessi',
    '/user/profilo':    'Profilo',
    '/user/chat':       'Chat',
  }
  return map[route.path] ?? 'Area personale'
})
</script>
