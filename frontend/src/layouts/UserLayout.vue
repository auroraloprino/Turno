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
        <div class="av">MR</div>
        <div>
          <div class="chip-name">Mario Rossi</div>
          <div class="chip-role">L1</div>
        </div>
      </div>
    </aside>

    <div class="main">
      <div class="topbar">
        <h1>{{ pageTitle }}</h1>
        <span class="badge">L1</span>
      </div>
      <div class="content">
        <RouterView />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
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
