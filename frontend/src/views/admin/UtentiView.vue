<template>
  <div class="card">
    <div class="card-title">Utenti</div>
    <div v-if="loading" class="empty-state">Caricamento…</div>
    <div v-else-if="utenti.length === 0" class="empty-state">Nessun utente.</div>
    <div v-for="u in utenti" :key="u.id" class="user-row">
      <div class="avatar">{{ initials(u.name) }}</div>
      <div class="user-info">
        <div class="user-name">{{ u.name }}</div>
        <div class="user-role">{{ u.email }}</div>
      </div>
      <span class="pill pill-online">{{ u.role }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api'

interface UserResponse {
  id: number
  name: string
  email: string
  role: string
}

const utenti = ref<UserResponse[]>([])
const loading = ref(false)

function initials(name: string) {
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
}

onMounted(async () => {
  loading.value = true
  try {
    utenti.value = await api.get<UserResponse[]>('/api/users')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.empty-state {
  font-size: 13px;
  color: var(--text-tertiary);
  padding: 8px 0;
}
</style>
