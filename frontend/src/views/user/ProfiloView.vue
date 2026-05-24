<template>
  <div class="card">
    <div class="card-title">Informazioni account</div>
    <div class="info-row">
      <span class="info-label">Nome</span>
      <span class="info-val">{{ user.name }}</span>
    </div>
    <div class="info-row">
      <span class="info-label">Email</span>
      <span class="info-val">{{ user.email }}</span>
    </div>
    <div class="info-row">
      <span class="info-label">Ruolo</span>
      <span class="info-val">{{ user.role }}</span>
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
      <button class="btn-send" @click="salva">Salva</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const user = ref({ name: '', email: '', role: '' })
const form = ref({ vecchia: '', nuova: '', conferma: '' })
const errore = ref('')

function salva() {
  errore.value = ''
  if (!form.value.vecchia || !form.value.nuova || !form.value.conferma) {
    errore.value = 'Compila tutti i campi.'
    return
  }
  if (form.value.nuova !== form.value.conferma) {
    errore.value = 'Le password non coincidono.'
    return
  }
  form.value = { vecchia: '', nuova: '', conferma: '' }
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
.info-val   { color: var(--text-primary); }

.errore {
  font-size: 12px;
  color: var(--coral-dark);
}
</style>
