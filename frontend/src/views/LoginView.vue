<template>
  <div class="login-wrap">
    <div class="login-box">
      <div>
        <div class="login-title">Turno</div>
        <div class="login-sub">Accedi al tuo account</div>
      </div>
      <div class="form-group">
        <label>Email</label>
        <input v-model="email" type="email" />
      </div>
      <div class="form-group">
        <label>Password</label>
        <input v-model="password" type="password" />
      </div>
      <button class="btn-primary" @click="login">Accedi</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const email    = ref('')
const password = ref('')
const router   = useRouter()

function login() {
  if (email.value.includes('admin')) {
    router.push('/admin')
  } else {
    router.push('/user')
  }
}
</script>
