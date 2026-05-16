<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <span class="logo-name">Turno</span>
        <small>Pannello Admin</small>
      </div>

      <div class="nav-section">Generale</div>
      <RouterLink class="nav-item" to="/admin/dashboard">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><rect x="1" y="1" width="6" height="6" rx="1.5" fill="#534AB7"/><rect x="9" y="1" width="6" height="6" rx="1.5" fill="#534AB7" opacity="0.4"/><rect x="1" y="9" width="6" height="6" rx="1.5" fill="#534AB7" opacity="0.4"/><rect x="9" y="9" width="6" height="6" rx="1.5" fill="#534AB7" opacity="0.4"/></svg>
        Dashboard
      </RouterLink>
      <RouterLink class="nav-item" to="/admin/utenti">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><circle cx="6" cy="5" r="3" stroke="#888" stroke-width="1.2"/><path d="M1 14c0-3 2-5 5-5s5 2 5 5" stroke="#888" stroke-width="1.2" stroke-linecap="round"/><circle cx="12" cy="5" r="2" stroke="#888" stroke-width="1.2"/><path d="M14 14c0-2-1-3.5-2-4" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Utenti
      </RouterLink>

      <div class="nav-section">Funzioni</div>
      <RouterLink class="nav-item" to="/admin/planner">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><rect x="2" y="3" width="12" height="10" rx="1.5" stroke="#888" stroke-width="1.2"/><path d="M5 7h6M5 10h4" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Planner
      </RouterLink>
      <RouterLink class="nav-item" to="/admin/timbrature">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="6" stroke="#888" stroke-width="1.2"/><path d="M8 5v3l2 2" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Timbrature
      </RouterLink>
      <RouterLink class="nav-item" to="/admin/permessi">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><rect x="2" y="2" width="12" height="12" rx="1.5" stroke="#888" stroke-width="1.2"/><path d="M5 8h6M8 5v6" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Permessi
      </RouterLink>
      <RouterLink class="nav-item" to="/admin/chat">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><path d="M2 4h12v7a2 2 0 01-2 2H4a2 2 0 01-2-2V4z" stroke="#888" stroke-width="1.2"/><path d="M2 4l6 5 6-5" stroke="#888" stroke-width="1.2"/></svg>
        Chat
      </RouterLink>

      <div class="nav-section">Accesso</div>
      <RouterLink class="nav-item" to="/admin/credenziali">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><rect x="4" y="7" width="8" height="6" rx="1" stroke="#888" stroke-width="1.2"/><path d="M5.5 7V5a2.5 2.5 0 015 0v2" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Credenziali
      </RouterLink>
    </aside>

    <div class="main">
      <div class="topbar">
        <h1>{{ pageTitle }}</h1>
        <span class="badge">Admin</span>
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
    '/admin/dashboard':   'Dashboard',
    '/admin/utenti':      'Utenti',
    '/admin/planner':     'Planner',
    '/admin/timbrature':  'Timbrature',
    '/admin/permessi':    'Permessi',
    '/admin/chat':        'Chat',
    '/admin/credenziali': 'Credenziali',
  }
  return map[route.path] ?? 'Admin'
})
</script>
