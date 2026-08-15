<template>
  <div class="card">
    <div class="card-title">Il mio cartellino</div>
    <div class="timb-big">
      <div class="timb-clock">{{ clock }}</div>
      <div class="timb-date">{{ date }}</div>
      <div class="timb-status-row">
        <div class="dot" :class="{ out: !inTurno }"></div>
        <span class="timb-stato">{{ inTurno ? 'In turno' : 'Fuori turno' }}</span>
        <span class="timb-since">{{ inTurno ? '· dalle ' + since : '' }}</span>
      </div>
      <span v-if="error" class="errore">{{ error }}</span>
      <button class="btn-timb" :class="{ uscita: inTurno }" :disabled="loading" @click="timbra">
        {{ loading ? '…' : inTurno ? 'Timbra uscita' : 'Timbra entrata' }}
      </button>
    </div>
  </div>

  <div class="card">
    <div class="card-title">Storico recente</div>
    <div v-if="loadingStorico" class="empty-state">Caricamento…</div>
    <div v-else-if="history.length === 0" class="empty-state">Nessuna timbratura.</div>
    <div v-for="h in history" :key="h.id" class="hist-row">
      <span class="hist-label">{{ fmt(h.timestamp) }}</span>
      <span class="pill" :class="h.tipo === 'ENTRATA' ? 'p-in' : 'p-out'">{{ h.tipo }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { api } from '@/api'

interface TimbraturaResponse {
  id: number
  userId: number
  userName: string
  tipo: string
  timestamp: string
}

const clock = ref('')
const date  = ref('')
const inTurno = ref(false)
const since   = ref('')
const history = ref<TimbraturaResponse[]>([])
const error   = ref('')
const loading = ref(false)
const loadingStorico = ref(false)

let interval: ReturnType<typeof setInterval>

function tick() {
  const now = new Date()
  clock.value = now.toLocaleTimeString('it-IT')
  date.value  = now.toLocaleDateString('it-IT', { weekday: 'long', day: 'numeric', month: 'long' })
}

function fmt(ts: string) {
  return new Date(ts).toLocaleString('it-IT', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function fetchStorico() {
  loadingStorico.value = true
  try {
    history.value = await api.get<TimbraturaResponse[]>('/api/timbrature/me')
    const ultima = history.value[0]
    if (ultima?.tipo === 'ENTRATA') {
      inTurno.value = true
      since.value   = new Date(ultima.timestamp).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
    }
  } finally {
    loadingStorico.value = false
  }
}

async function timbra() {
  error.value = ''
  loading.value = true
  try {
    const t = await api.post<TimbraturaResponse>('/api/timbrature', {})
    history.value.unshift(t)
    inTurno.value = t.tipo === 'ENTRATA'
    since.value   = inTurno.value
      ? new Date(t.timestamp).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
      : ''
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Errore.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  tick()
  interval = setInterval(tick, 1000)
  fetchStorico()
})

onUnmounted(() => clearInterval(interval))
</script>

<style scoped>
.empty-state {
  font-size: 13px;
  color: var(--text-tertiary);
  padding: 8px 0;
}
.errore {
  font-size: 12px;
  color: var(--coral-dark);
}
</style>
