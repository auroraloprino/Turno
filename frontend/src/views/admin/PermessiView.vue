<template>
  <div class="two-col">
    <div class="card">
      <div class="card-title">In attesa</div>
      <div v-if="loadingAttesa" class="empty-state">Caricamento…</div>
      <div v-else-if="inAttesa.length === 0" class="empty-state">Nessuna richiesta in attesa.</div>
      <div v-for="p in inAttesa" :key="p.id" class="perm-row">
        <div>
          <div class="perm-info">{{ p.userName }} — {{ p.tipo }}</div>
          <div class="perm-date">{{ fmt(p.dal) }}{{ p.al !== p.dal ? ' – ' + fmt(p.al) : '' }}</div>
          <div v-if="p.note" class="perm-note">{{ p.note }}</div>
        </div>
        <div class="perm-actions">
          <button class="btn-sm btn-ok" @click="aggiorna(p.id, 'APPROVATO')">Approva</button>
          <button class="btn-sm btn-no" @click="aggiorna(p.id, 'RIFIUTATO')">Rifiuta</button>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-title">Tutte le richieste</div>
      <div v-if="loadingTutti" class="empty-state">Caricamento…</div>
      <div v-else-if="tutti.length === 0" class="empty-state">Nessuna richiesta.</div>
      <div v-for="p in tutti" :key="p.id" class="perm-row-past">
        <div>
          <span class="perm-tipo">{{ p.userName }} — {{ p.tipo }}</span>
          <span class="perm-d">· {{ fmt(p.dal) }}{{ p.al !== p.dal ? ' – ' + fmt(p.al) : '' }}</span>
        </div>
        <span class="pill" :class="statoClass(p.stato)">{{ p.stato }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api'

interface PermessoResponse {
  id: number
  userId: number
  userName: string
  tipo: string
  dal: string
  al: string
  note: string | null
  stato: string
}

const inAttesa = ref<PermessoResponse[]>([])
const tutti = ref<PermessoResponse[]>([])
const loadingAttesa = ref(false)
const loadingTutti = ref(false)

async function fetchAll() {
  loadingAttesa.value = true
  loadingTutti.value = true
  try {
    inAttesa.value = await api.get<PermessoResponse[]>('/api/permessi/in-attesa')
  } finally {
    loadingAttesa.value = false
  }
  try {
    tutti.value = await api.get<PermessoResponse[]>('/api/permessi')
  } finally {
    loadingTutti.value = false
  }
}

async function aggiorna(id: number, stato: string) {
  const updated = await api.patch<PermessoResponse>(`/api/permessi/${id}/stato?stato=${stato}`, {})
  inAttesa.value = inAttesa.value.filter(p => p.id !== id)
  const idx = tutti.value.findIndex(p => p.id === id)
  if (idx !== -1) tutti.value[idx] = updated
}

function fmt(d: string) {
  if (!d) return ''
  const [y, m, g] = d.split('-')
  return `${g}/${m}/${y}`
}

function statoClass(stato: string) {
  if (stato === 'APPROVATO') return 'p-ok'
  if (stato === 'RIFIUTATO') return 'p-no'
  return 'p-pend'
}

onMounted(fetchAll)
</script>

<style scoped>
.empty-state {
  font-size: 13px;
  color: var(--text-tertiary);
  padding: 8px 0;
}
.perm-note {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}
</style>
