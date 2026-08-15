<template>
  <div class="card">
    <div class="card-title">Nuova richiesta permesso</div>
    <div class="perm-form">
      <div class="form-row">
        <div class="fg">
          <label>Tipo di richiesta</label>
          <select v-model="form.tipo">
            <option>Ferie</option>
            <option>Permesso</option>
            <option>Malattia</option>
            <option>Altro</option>
          </select>
        </div>
        <div v-if="form.tipo === 'Permesso'" class="fg">
          <label>Durata</label>
          <select v-model="form.durata">
            <option>Giornaliero</option>
            <option>Orario</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="fg">
          <label>{{ oraMode ? 'Giorno' : 'Dal' }}</label>
          <input type="date" v-model="form.dal" />
        </div>
        <template v-if="!oraMode">
          <div class="fg">
            <label>Al</label>
            <input type="date" v-model="form.al" />
          </div>
        </template>
        <template v-if="oraMode">
          <div class="fg">
            <label>Ora inizio</label>
            <input type="time" v-model="form.oraInizio" />
          </div>
          <div class="fg">
            <label>Ora fine</label>
            <input type="time" v-model="form.oraFine" />
          </div>
        </template>
      </div>
      <div class="fg">
        <label>Note (opzionale)</label>
        <textarea v-model="form.note"></textarea>
      </div>
      <span v-if="error" class="errore">{{ error }}</span>
      <button class="btn-send" :disabled="loading" @click="invia">
        {{ loading ? 'Invio…' : 'Invia richiesta' }}
      </button>
    </div>

    <div class="past-title">Richieste precedenti</div>
    <div v-if="loadingList" class="empty-state">Caricamento…</div>
    <div v-else-if="richieste.length === 0" class="empty-state">Nessuna richiesta.</div>
    <div v-for="p in richieste" :key="p.id" class="perm-row-past">
      <div>
        <span class="perm-tipo">{{ p.tipo }}</span>
        <span class="perm-d">· {{ fmt(p.dal) }}{{ p.al !== p.dal ? ' – ' + fmt(p.al) : '' }}</span>
      </div>
      <span class="pill" :class="statoClass(p.stato)">{{ p.stato }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { api } from '@/api'

interface PermessoResponse {
  id: number
  tipo: string
  dal: string
  al: string
  oraInizio: string | null
  oraFine: string | null
  note: string | null
  stato: string
}

const form = ref({ tipo: 'Ferie', dal: '', al: '', note: '', durata: 'Giornaliero', oraInizio: '', oraFine: '' })
const richieste = ref<PermessoResponse[]>([])
const error = ref('')
const loading = ref(false)
const loadingList = ref(false)

const oraMode = computed(() => form.value.tipo === 'Permesso' && form.value.durata === 'Orario')

async function fetchMiei() {
  loadingList.value = true
  try {
    richieste.value = await api.get<PermessoResponse[]>('/api/permessi/me')
  } finally {
    loadingList.value = false
  }
}

async function invia() {
  error.value = ''
  if (!form.value.dal) { error.value = 'Inserisci la data.'; return }
  if (!oraMode.value && !form.value.al) { error.value = 'Inserisci la data di fine.'; return }
  if (oraMode.value && (!form.value.oraInizio || !form.value.oraFine)) {
    error.value = 'Inserisci orario inizio e fine.'
    return
  }
  loading.value = true
  try {
    const body = {
      tipo: form.value.tipo,
      dal: form.value.dal,
      al: oraMode.value ? form.value.dal : form.value.al,
      oraInizio: oraMode.value ? form.value.oraInizio : null,
      oraFine: oraMode.value ? form.value.oraFine : null,
      note: form.value.note || null,
    }
    const created = await api.post<PermessoResponse>('/api/permessi', body)
    richieste.value.unshift(created)
    form.value = { tipo: 'Ferie', dal: '', al: '', note: '', durata: 'Giornaliero', oraInizio: '', oraFine: '' }
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Errore durante l\'invio.'
  } finally {
    loading.value = false
  }
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

onMounted(fetchMiei)
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
