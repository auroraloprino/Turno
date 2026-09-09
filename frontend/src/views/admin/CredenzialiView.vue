<template>
  <div class="two-col">
    <div v-if="isOwner" class="card">
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
        <span v-if="createError" class="errore">{{ createError }}</span>
        <span v-if="createOk" class="ok">Account creato.</span>
        <button class="btn-send" :disabled="createLoading" @click="crea">
          {{ createLoading ? 'Creazione…' : 'Crea account' }}
        </button>
      </div>
    </div>

    <div class="card">
      <div class="card-title">Cambia password</div>
      <div class="perm-form">
        <div class="fg">
          <label>Password attuale</label>
          <input v-model="pw.current" type="password" />
        </div>
        <div class="fg">
          <label>Nuova password</label>
          <input v-model="pw.next" type="password" />
        </div>
        <div class="fg">
          <label>Conferma nuova password</label>
          <input v-model="pw.confirm" type="password" />
        </div>
        <span v-if="pwError" class="errore">{{ pwError }}</span>
        <span v-if="pwOk" class="ok">Password aggiornata.</span>
        <button class="btn-send" :disabled="pwLoading" @click="cambia">
          {{ pwLoading ? 'Salvataggio…' : 'Aggiorna password' }}
        </button>
      </div>
    </div>

    <div v-if="isOwner || auth.role === 'ADMIN'" class="card">
      <div class="card-title">{{ isOwner ? 'Account esistenti' : 'Utenti' }}</div>
      <div v-if="loadingList" class="empty-state">Caricamento…</div>
      <div v-else-if="utenti.length === 0" class="empty-state">Nessun account.</div>
      <div v-for="u in utenti" :key="u.id" class="user-row">
        <div class="avatar">{{ initials(u.name) }}</div>
        <div class="user-info">
          <div class="user-name">{{ u.name }}</div>
          <div class="user-role">{{ u.email }}</div>
        </div>
        <span class="pill pill-online">{{ u.role }}</span>
        <button
          v-if="canDelete(u)"
          class="btn-sm btn-no"
          style="margin-left:8px"
          @click="elimina(u.id)"
        >Elimina</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { api } from '@/api'
import { useAuthStore } from '@/stores/auth'

interface UserResponse { id: number; name: string; email: string; role: string }

const auth = useAuthStore()
const isOwner = computed(() => auth.role === 'OWNER')

const form = ref({ name: '', email: '', password: '', role: 'USER' })
const createError = ref('')
const createOk = ref(false)
const createLoading = ref(false)
const loadingList = ref(false)
const utenti = ref<UserResponse[]>([])

const pw = ref({ current: '', next: '', confirm: '' })
const pwError = ref('')
const pwOk = ref(false)
const pwLoading = ref(false)

async function fetchUtenti() {
  loadingList.value = true
  try { utenti.value = await api.get<UserResponse[]>('/api/users') }
  finally { loadingList.value = false }
}

async function crea() {
  createError.value = ''
  createOk.value = false
  if (!form.value.name || !form.value.email || !form.value.password) {
    createError.value = 'Compila tutti i campi.'
    return
  }
  createLoading.value = true
  try {
    const created = await api.post<UserResponse>('/api/users', form.value)
    utenti.value.push(created)
    form.value = { name: '', email: '', password: '', role: 'USER' }
    createOk.value = true
  } catch (e: unknown) {
    createError.value = e instanceof Error ? e.message : 'Errore durante la creazione.'
  } finally {
    createLoading.value = false
  }
}

async function cambia() {
  pwError.value = ''
  pwOk.value = false
  if (!pw.value.current || !pw.value.next || !pw.value.confirm) {
    pwError.value = 'Compila tutti i campi.'
    return
  }
  if (pw.value.next !== pw.value.confirm) {
    pwError.value = 'Le password non coincidono.'
    return
  }
  pwLoading.value = true
  try {
    await api.patch(`/api/users/${auth.user!.id}/password`, {
      currentPassword: pw.value.current,
      newPassword: pw.value.next,
    })
    pw.value = { current: '', next: '', confirm: '' }
    pwOk.value = true
  } catch {
    pwError.value = 'Password attuale errata.'
  } finally {
    pwLoading.value = false
  }
}

function initials(name: string) {
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
}

function canDelete(u: UserResponse) {
  if (u.id === auth.user?.id) return false
  if (auth.role === 'OWNER') return u.role !== 'OWNER'
  if (auth.role === 'ADMIN') return u.role === 'USER'
  return false
}

async function elimina(id: number) {
  if (!confirm('Eliminare questo account?')) return
  await api.delete(`/api/users/${id}`)
  utenti.value = utenti.value.filter(u => u.id !== id)
}

onMounted(() => { if (isOwner.value || auth.role === 'ADMIN') fetchUtenti() })
</script>

<style scoped>
.empty-state { font-size: 13px; color: var(--text-tertiary); padding: 12px 0; }
.errore { font-size: 12px; color: var(--coral-dark); }
.ok { font-size: 12px; color: #27ae60; }
</style>
