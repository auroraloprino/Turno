<template>
  <div class="two-col" style="align-items:start">
    <div class="card" style="margin-bottom:0">
      <div class="card-title">Il mio cartellino</div>
      <div class="timb-big">
        <div class="timb-clock">{{ clock }}</div>
        <div class="timb-date">{{ date }}</div>
        <div class="timb-status-row">
          <div class="dot" :class="{ out: !inTurno }"></div>
          <span class="timb-stato">{{ inTurno ? 'In turno' : 'Fuori turno' }}</span>
          <span class="timb-since">{{ inTurno ? '· dalle ' + since : '' }}</span>
        </div>
        <span v-if="error" style="font-size:12px;color:var(--coral-dark)">{{ error }}</span>
        <button class="btn-timb" :class="{ uscita: inTurno }" :disabled="loadingTimb" @click="timbra">
          {{ loadingTimb ? '…' : inTurno ? 'Timbra uscita' : 'Timbra entrata' }}
        </button>
      </div>
    </div>

    <div class="card" style="margin-bottom:0">
      <div class="card-title">Storico recente</div>
      <div v-if="loadingStorico" class="empty-state">Caricamento…</div>
      <div v-else-if="history.length === 0" class="empty-state">Nessuna timbratura.</div>
      <div v-for="h in history" :key="h.id" class="hist-row">
        <span class="hist-label">{{ fmt(h.timestamp) }}</span>
        <span class="pill" :class="h.tipo === 'ENTRATA' ? 'p-in' : 'p-out'">{{ h.tipo }}</span>
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-title">Timbrature</div>
    <div v-if="loading" class="empty-state">Caricamento…</div>
    <div v-else-if="timbrature.length === 0" class="empty-state">Nessuna timbratura registrata.</div>
    <template v-else>
      <div class="timb-filters">
        <input v-model="search" type="text" placeholder="Cerca per nome o email…" />
      </div>
      <div v-for="t in filtered" :key="t.id" class="timb-row">
        <span class="timb-name">{{ t.userName }}</span>
        <span class="timb-time">{{ fmt(t.timestamp) }}</span>
        <span class="timb-status" :class="t.tipo === 'ENTRATA' ? 'ts-in' : 'ts-out'">{{ t.tipo }}</span>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { api } from '@/api'

interface TimbraturaResponse {
  id: number
  userId: number
  userName: string
  userEmail: string
  tipo: string
  timestamp: string
}

const clock = ref('')
const date = ref('')
const inTurno = ref(false)
const since = ref('')
const history = ref<TimbraturaResponse[]>([])
const error = ref('')
const loadingTimb = ref(false)
const loadingStorico = ref(false)
let ticker: ReturnType<typeof setInterval>

function tick() {
  const now = new Date()
  clock.value = now.toLocaleTimeString('it-IT')
  date.value = now.toLocaleDateString('it-IT', { weekday: 'long', day: 'numeric', month: 'long' })
}
tick()

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
  loadingTimb.value = true
  try {
    const t = await api.post<TimbraturaResponse>('/api/timbrature', {})
    history.value.unshift(t)
    inTurno.value = t.tipo === 'ENTRATA'
    since.value   = inTurno.value
      ? new Date(t.timestamp).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
      : ''
    timbrature.value = await api.get<TimbraturaResponse[]>('/api/timbrature')
  } finally {
    loadingTimb.value = false
  }
}

const timbrature = ref<TimbraturaResponse[]>([])
const loading = ref(false)
const search = ref('')

const filtered = computed(() => {
  const q = search.value.toLowerCase()
  return timbrature.value.filter(t =>
    t.userName.toLowerCase().includes(q) || t.userEmail.toLowerCase().includes(q)
  )
})

function fmt(ts: string) {
  return new Date(ts).toLocaleString('it-IT', {
    day: '2-digit', month: '2-digit', year: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

let timer: ReturnType<typeof setInterval>

onMounted(async () => {
  tick()
  ticker = setInterval(tick, 1000)
  fetchStorico()

  loading.value = true
  try {
    timbrature.value = await api.get<TimbraturaResponse[]>('/api/timbrature')
  } finally {
    loading.value = false
  }
  timer = setInterval(async () => {
    timbrature.value = await api.get<TimbraturaResponse[]>('/api/timbrature')
  }, 30000)
})

onUnmounted(() => {
  clearInterval(ticker)
  clearInterval(timer)
})
</script>

<style scoped>
.timb-filters {
  margin-bottom: 12px;
}

.timb-filters input {
  padding: 6px 10px;
  border: 0.5px solid var(--border-md);
  border-radius: var(--radius-md);
  background: var(--bg-tertiary);
  color: var(--text-primary);
  font-size: 12px;
  font-family: inherit;
  outline: none;
  width: 220px;
}

.timb-filters input:focus {
  border-color: var(--purple);
}

.empty-state {
  font-size: 13px;
  color: var(--text-tertiary);
  padding: 8px 0;
}
</style>
