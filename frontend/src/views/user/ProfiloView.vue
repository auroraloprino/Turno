<template>
  <div class="card">
    <div class="card-title">Informazioni account</div>
    <div class="info-row">
      <span class="info-label">Nome</span>
      <span class="info-val">{{ auth.user?.name }}</span>
    </div>
    <div class="info-row">
      <span class="info-label">Email</span>
      <span class="info-val">{{ auth.user?.email }}</span>
    </div>
    <div class="info-row">
      <span class="info-label">Ruolo</span>
      <span class="info-val">{{ auth.user?.role }}</span>
    </div>
  </div>

  <div class="card">
    <div class="card-title">Cambia password</div>
    <div class="perm-form">
      <div class="fg">
        <label>Vecchia password</label>
        <input type="password" v-model="form.vecchia" />
      </div>
      <div class="fg">
        <label>Nuova password</label>
        <input type="password" v-model="form.nuova" />
      </div>
      <div class="fg">
        <label>Conferma nuova password</label>
        <input type="password" v-model="form.conferma" />
      </div>
      <span v-if="errore" class="errore">{{ errore }}</span>
      <span v-if="successo" class="successo">Password aggiornata.</span>
      <button class="btn-send" :disabled="loading" @click="salva">
        {{ loading ? 'Salvataggio…' : 'Salva' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { api } from '@/api'

const auth = useAuthStore()
const form = ref({ vecchia: '', nuova: '', conferma: '' })
const errore = ref('')
const successo = ref(false)
const loading = ref(false)

async function salva() {
  errore.value = ''
  successo.value = false
  if (!form.value.vecchia || !form.value.nuova || !form.value.conferma) {
    errore.value = 'Compila tutti i campi.'
    return
  }
  if (form.value.nuova !== form.value.conferma) {
    errore.value = 'Le password non coincidono.'
    return
  }
  loading.value = true
  try {
    await api.patch(`/api/users/${auth.user?.id}/password`, {
      currentPassword: form.value.vecchia,
      newPassword: form.value.nuova,
    })
    form.value = { vecchia: '', nuova: '', conferma: '' }
    successo.value = true
  } catch (e: unknown) {
    errore.value = e instanceof Error ? e.message : 'Errore durante il salvataggio.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.info-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 0.5px solid var(--border);
  font-size: 13px;
}
.info-row:last-child { border-bottom: none; }
.info-label { color: var(--text-secondary); width: 80px; flex-shrink: 0; }
.info-val { color: var(--text-primary); }
.errore { font-size: 12px; color: var(--coral-dark); }
.successo { font-size: 12px; color: #1D9E75; }
</style>
