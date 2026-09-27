<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <span class="logo-name">Turno</span>
        <small>Pannello Owner</small>
      </div>

      <div class="nav-section">Generale</div>
      <RouterLink class="nav-item" to="/owner/dashboard">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><rect x="1" y="1" width="6" height="6" rx="1.5" fill="#534AB7"/><rect x="9" y="1" width="6" height="6" rx="1.5" fill="#534AB7" opacity="0.4"/><rect x="1" y="9" width="6" height="6" rx="1.5" fill="#534AB7" opacity="0.4"/><rect x="9" y="9" width="6" height="6" rx="1.5" fill="#534AB7" opacity="0.4"/></svg>
        Dashboard
      </RouterLink>
      <RouterLink class="nav-item" to="/owner/utenti">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><circle cx="6" cy="5" r="3" stroke="#888" stroke-width="1.2"/><path d="M1 14c0-3 2-5 5-5s5 2 5 5" stroke="#888" stroke-width="1.2" stroke-linecap="round"/><circle cx="12" cy="5" r="2" stroke="#888" stroke-width="1.2"/><path d="M14 14c0-2-1-3.5-2-4" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Utenti
      </RouterLink>

      <div class="nav-section">Funzioni</div>
      <RouterLink class="nav-item" to="/owner/planner">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><rect x="2" y="3" width="12" height="10" rx="1.5" stroke="#888" stroke-width="1.2"/><path d="M5 7h6M5 10h4" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Planner
      </RouterLink>
      <RouterLink class="nav-item" to="/owner/timbrature">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="6" stroke="#888" stroke-width="1.2"/><path d="M8 5v3l2 2" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Timbrature
      </RouterLink>
      <RouterLink class="nav-item" to="/owner/permessi">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><rect x="2" y="2" width="12" height="12" rx="1.5" stroke="#888" stroke-width="1.2"/><path d="M5 8h6M8 5v6" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Permessi
      </RouterLink>
      <RouterLink class="nav-item" to="/owner/chat">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><path d="M2 4h12v7a2 2 0 01-2 2H4a2 2 0 01-2-2V4z" stroke="#888" stroke-width="1.2"/><path d="M2 4l6 5 6-5" stroke="#888" stroke-width="1.2"/></svg>
        Chat
        <span v-if="chat.totalNonLetti" class="nav-badge">{{ chat.totalNonLetti }}</span>
      </RouterLink>

      <div class="nav-section">Accesso</div>
      <RouterLink class="nav-item" to="/owner/credenziali">
        <svg class="nav-icon" viewBox="0 0 16 16" fill="none"><rect x="4" y="7" width="8" height="6" rx="1" stroke="#888" stroke-width="1.2"/><path d="M5.5 7V5a2.5 2.5 0 015 0v2" stroke="#888" stroke-width="1.2" stroke-linecap="round"/></svg>
        Credenziali
      </RouterLink>
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
            <span v-if="chat.totalNonLetti + notifiche.lista.length" style="position:absolute;top:-4px;right:-4px;background:#e74c3c;color:#fff;border-radius:50%;font-size:10px;width:14px;height:14px;display:flex;align-items:center;justify-content:center">
              {{ chat.totalNonLetti + notifiche.lista.length }}
            </span>
            <div v-if="showNotifiche" style="position:absolute;right:0;top:28px;background:var(--bg-secondary);border:0.5px solid var(--border-md);border-radius:8px;min-width:260px;box-shadow:0 4px 12px rgba(0,0,0,.4);z-index:100">
              <div v-if="chat.totalNonLetti" style="padding:8px 12px;border-bottom:0.5px solid var(--border);font-size:12px;font-weight:600;color:var(--purple)">Chat ({{ chat.totalNonLetti }} non letti)</div>
              <div v-for="conv in chat.conversazioni.filter(c => c.nonLetti > 0)" :key="conv.id" style="padding:8px 12px;border-bottom:0.5px solid var(--border);font-size:13px;display:flex;justify-content:space-between;align-items:center;color:var(--text-primary)">
                <span>{{ conv.nome }}</span>
                <span style="background:#e74c3c;color:#fff;border-radius:50%;font-size:10px;width:18px;height:18px;display:flex;align-items:center;justify-content:center">{{ conv.nonLetti }}</span>
              </div>
              <div v-if="notifiche.lista.length" style="padding:8px 12px;border-bottom:0.5px solid var(--border);font-size:12px;font-weight:600;color:var(--text-secondary)">Notifiche</div>
              <div v-for="(n, i) in notifiche.lista" :key="i" style="padding:10px 12px;border-bottom:0.5px solid var(--border);font-size:13px;display:flex;justify-content:space-between;align-items:center;color:var(--text-primary)">
                <span>{{ n.messaggio }}</span>
                <span style="cursor:pointer;color:var(--text-tertiary);margin-left:8px" @click.stop="notifiche.rimuovi(i)">&#10005;</span>
              </div>
              <div v-if="!chat.totalNonLetti && !notifiche.lista.length" style="padding:12px;color:var(--text-secondary);font-size:13px">Nessuna notifica</div>
            </div>
          </div>
          <span class="badge">Owner</span>
          <button style="background:none;border:none;color:var(--coral-dark);cursor:pointer;font-size:12px;padding:0" @click="logout">Esci</button>
        </div>
      <div class="content">
        <RouterView />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'

const chat = useChatStore()
onMounted(async () => { await chat.carica(); chat.connetti() })

const route  = useRoute()
const router = useRouter()
const auth   = useAuthStore()

function logout() {
  auth.logout()
  router.push('/login')
}

const pageTitle = computed(() => {
  const map: Record<string, string> = {
    '/owner/dashboard':  'Dashboard',
    '/owner/utenti':     'Utenti',
    '/owner/planner':    'Planner',
    '/owner/timbrature': 'Timbrature',
    '/owner/permessi':   'Permessi',
    '/owner/chat':       'Chat',
    '/owner/credenziali':'Credenziali',
  }
  return map[route.path] ?? 'Owner'
})
</script>
