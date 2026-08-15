<template>
  <div class="card">
    <div class="card-title">Timbrature</div>
    <div v-if="loading" class="empty-state">Caricamento…</div>
    <div v-else-if="timbrature.length === 0" class="empty-state">Nessuna timbratura registrata.</div>
    <template v-else>
      <div class="timb-filters">
        <input v-model="search" type="text" placeholder="Cerca per nome…" />
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
import { ref, computed, onMounted } from 'vue'
import { api } from '@/api'

interface TimbraturaResponse {
  id: number
  userId: number
  userName: string
  tipo: string
  timestamp: string
}

const timbrature = ref<TimbraturaResponse[]>([])
const loading    = ref(false)
const search     = ref('')

const filtered = computed(() =>
  timbrature.value.filter(t =>
    t.userName.toLowerCase().includes(search.value.toLowerCase())
  )
)

function fmt(ts: string) {
  return new Date(ts).toLocaleString('it-IT', {
    day: '2-digit', month: '2-digit', year: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(async () => {
  loading.value = true
  try {
    timbrature.value = await api.get<TimbraturaResponse[]>('/api/timbrature')
  } finally {
    loading.value = false
  }
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
