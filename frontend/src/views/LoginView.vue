<template>
  <div class="login-wrap">
    <div class="login-box">
      <div>
        <div class="login-title">Turno</div>
        <div class="login-sub">Accedi al tuo account</div>
      </div>
      <div class="form-group">
        <label>Email</label>
        <input v-model="email" type="email" autocomplete="username" />
      </div>
      <div class="form-group">
        <label>Password</label>
        <input v-model="password" type="password" autocomplete="current-password" @keydown.enter="login" />
      </div>
      <span v-if="error" class="login-error">{{ error }}</span>
      <button class="btn-primary" :disabled="loading" @click="login">
        {{ loading ? 'Accesso…' : 'Accedi' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const email    = ref('')
const password = ref('')
const error    = ref('')
const loading  = ref(false)
const router   = useRouter()
const auth     = useAuthStore()

async function login() {
  error.value = ''
  if (!email.value || !password.value) {
    error.value = 'Inserisci email e password.'
    return
  }
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    router.push(auth.role === 'OWNER' || auth.role === 'ADMIN' ? '/admin' : '/user')
  } catch {
    error.value = 'Credenziali non valide.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-error {
  font-size: 12px;
  color: var(--coral-dark);
}
</style>
