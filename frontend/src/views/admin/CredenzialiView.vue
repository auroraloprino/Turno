<template>
  <div class="two-col">
    <div class="card">
      <div class="card-title">Nuovo account</div>
      <div class="perm-form">
        <div class="fg">
          <label>Nome</label>
          <input v-model="form.name" type="text" />
        </div>
        <div class="fg">
          <label>Email</label>
          <input v-model="form.email" type="email" />
        </div>
        <div class="fg">
          <label>Password iniziale</label>
          <input v-model="form.password" type="password" />
        </div>
        <div class="fg">
          <label>Ruolo</label>
          <select v-model="form.role">
            <option value="ADMIN">Admin</option>
            <option value="USER">Utente</option>
          </select>
        </div>
        <span v-if="error" class="errore">{{ error }}</span>
        <button class="btn-send" :disabled="loading" @click="crea">
          {{ loading ? 'Creazione…' : 'Crea account' }}
        </button>
      </div>
    </div>

    <div class="card">
      <div class="card-title">Account esistenti</div>
      <div v-if="loadingList" class="empty-state">Caricamento…</div>
      <div v-else-if="utenti.length === 0" class="empty-state">Nessun account.</div>
      <div v-for="u in utenti" :key="u.id" class="user-row">
        <div class="avatar">{{ initials(u.name) }}</div>
        <div class="user-info">
          <div class="user-name">{{ u.name }}</div>
          <div class="user-role">{{ u.email }}</div>
        </div>
        <span class="pill pill-online">{{ u.role }}</span>
      </div>
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

const form = ref({ name: '', email: '', password: '', role: 'USER' })
const error = ref('')
const loading = ref(false)
const loadingList = ref(false)
const utenti = ref<UserResponse[]>([])

async function fetchUtenti() {
  loadingList.value = true
  try {
    utenti.value = await api.get<UserResponse[]>('/api/users')
  } finally {
    loadingList.value = false
  }
}

async function crea() {
  error.value = ''
  if (!form.value.name || !form.value.email || !form.value.password) {
    error.value = 'Compila tutti i campi.'
    return
  }
  loading.value = true
  try {
    const created = await api.post<UserResponse>('/api/users', form.value)
    utenti.value.push(created)
    form.value = { name: '', email: '', password: '', role: 'USER' }
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Errore durante la creazione.'
  } finally {
    loading.value = false
  }
}

function initials(name: string) {
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
}

onMounted(fetchUtenti)
</script>

<style scoped>
.empty-state {
  font-size: 13px;
  color: var(--text-tertiary);
  padding: 12px 0;
}

.errore {
  font-size: 12px;
  color: var(--coral-dark);
}
</style>
