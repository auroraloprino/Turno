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

      <div v-if="form.tipo === 'Malattia'" class="fg">
        <label>Codice certificato INPS</label>
        <input type="text" v-model="form.codiceCertificato" />
      </div>

      <div v-if="form.tipo === 'Malattia'" class="fg">
        <label>Certificato medico</label>
        <div class="file-row">
          <button class="btn-file" @click="fileInput?.click()">Scegli file</button>
          <span class="file-name">{{ form.certificato || 'Nessun file selezionato' }}</span>
        </div>
        <input ref="fileInput" type="file" accept=".pdf,.jpg,.jpeg,.png" style="display:none" @change="onFile" />
      </div>

      <div v-if="form.tipo !== 'Malattia'" class="fg">
        <label>Note (opzionale)</label>
        <textarea v-model="form.note"></textarea>
      </div>

      <span v-if="errore" class="errore">{{ errore }}</span>
      <button class="btn-send" @click="invia">Invia richiesta</button>
    </div>

    <div class="past-title">Richieste precedenti</div>
    <div v-for="(p, i) in richieste" :key="i" class="perm-row-past">
      <div>
        <span class="perm-tipo">{{ p.tipo }}</span>
        <span class="perm-d">· {{ fmt(p.dal) }}{{ p.al !== p.dal ? ' – ' + fmt(p.al) : '' }}</span>
      </div>
      <span class="pill" :class="statoClass(p.stato)">{{ p.stato }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const form = ref({ tipo: 'Ferie', dal: '', al: '', note: '', certificato: '', codiceCertificato: '', durata: 'Giornaliero', oraInizio: '', oraFine: '' })
const richieste = ref<{ tipo: string; dal: string; al: string; stato: string }[]>([])
const errore = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

const oraMode = computed(() => form.value.tipo === 'Permesso' && form.value.durata === 'Orario')

function onFile(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  form.value.certificato = file?.name ?? ''
}

function invia() {
  errore.value = ''
  if (!form.value.dal) { errore.value = 'Inserisci la data.'; return }
  if (!oraMode.value && !form.value.al) { errore.value = 'Inserisci la data di fine.'; return }
  if (oraMode.value && (!form.value.oraInizio || !form.value.oraFine)) { errore.value = 'Inserisci orario inizio e fine.'; return }
  richieste.value.unshift({ tipo: form.value.tipo, dal: form.value.dal, al: oraMode.value ? form.value.dal : form.value.al, stato: 'in attesa' })
  form.value = { tipo: 'Ferie', dal: '', al: '', note: '', certificato: '', codiceCertificato: '', durata: 'Giornaliero', oraInizio: '', oraFine: '' }
}

function fmt(d: string) {
  if (!d) return ''
  const [y, m, g] = d.split('-')
  return `${g}/${m}/${y}`
}

function statoClass(stato: string) {
  if (stato === 'approvata') return 'p-ok'
  if (stato === 'rifiutata') return 'p-no'
  return 'p-pend'
}
</script>

<style scoped>
.file-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-file {
  padding: 6px 14px;
  font-size: 12px;
  background: var(--bg-tertiary);
  border: 0.5px solid var(--border-md);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.12s;
}
.btn-file:hover { opacity: 0.7; }

.file-name {
  font-size: 11px;
  color: var(--text-secondary);
}

.errore {
  font-size: 12px;
  color: var(--coral-dark);
}
</style>
