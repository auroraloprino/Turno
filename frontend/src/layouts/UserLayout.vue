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
        <div style="display:flex;align-items:center;gap:12px">
          <div style="position:relative;cursor:pointer" @click="showNotifiche = !showNotifiche">
            <svg viewBox="0 0 16 16" fill="none" style="width:20px;height:20px">
              <path d="M8 2a4 4 0 00-4 4v3l-1 1v1h10v-1l-1-1V6a4 4 0 00-4-4z" stroke="#6C63D5" stroke-width="1.2"/>
              <path d="M6.5 13a1.5 1.5 0 003 0" stroke="#6C63D5" stroke-width="1.2"/>
            </svg>
            <span v-if="notifiche.lista.length" style="position:absolute;top:-4px;right:-4px;background:#e74c3c;color:#fff;border-radius:50%;font-size:10px;width:14px;height:14px;display:flex;align-items:center;justify-content:center">
              {{ notifiche.lista.length }}
            </span>
            <div v-if="showNotifiche" style="position:absolute;right:0;top:28px;background:#fff;border:1px solid #eee;border-radius:8px;min-width:240px;box-shadow:0 4px 12px rgba(0,0,0,.1);z-index:100">
              <div v-if="!notifiche.lista.length" style="padding:12px;color:#888;font-size:13px">Nessuna notifica</div>
              <div v-for="(n, i) in notifiche.lista" :key="i" style="padding:10px 12px;border-bottom:1px solid #f0f0f0;font-size:13px;display:flex;justify-content:space-between;align-items:center">
                <span>{{ n.messaggio }}</span>
                <span style="cursor:pointer;color:#aaa;margin-left:8px" @click.stop="notifiche.rimuovi(i)">✕</span>
              </div>
            </div>
          </div>
          <span class="badge">Utente</span>
        </div>
      </div>
      <div class="content">
        <RouterView />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificheStore } from '@/stores/notifiche'

const notifiche = useNotificheStore()
const showNotifiche = ref(false)
onMounted(() => notifiche.connetti())
onUnmounted(() => notifiche.disconnetti())

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
