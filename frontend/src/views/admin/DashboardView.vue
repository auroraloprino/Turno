<template>
  <div class="stats-row">
    <div class="stat">
      <div class="stat-label">Utenti</div>
      <div class="stat-val">{{ utenti.length }}</div>
    </div>
    <div class="stat">
      <div class="stat-label">Timbrature oggi</div>
      <div class="stat-val">{{ timbratureOggi.length }}</div>
    </div>
    <div class="stat">
      <div class="stat-label">Permessi in attesa</div>
      <div class="stat-val">{{ permessiAttesa.length }}</div>
    </div>
    <div class="stat">
      <div class="stat-label">Turni questa settimana</div>
      <div class="stat-val">{{ turniSettimana.length }}</div>
    </div>
  </div>

  <div class="two-col">
    <div class="card">
      <div class="card-title">Utenti</div>
      <div v-if="utenti.length === 0" class="empty-state">Nessun utente.</div>
      <div v-for="u in utenti" :key="u.id" class="user-row">
        <div class="avatar">{{ initials(u.name) }}</div>
        <div class="user-info">
          <div class="user-name">{{ u.name }}</div>
          <div class="user-role">{{ u.email }}</div>
        </div>
        <span class="pill pill-online">{{ u.role }}</span>
      </div>
    </div>

    <div class="card">
      <div class="card-title">Timbrature oggi</div>
      <div v-if="timbratureOggi.length === 0" class="empty-state">Nessuna timbratura oggi.</div>
      <div v-for="t in timbratureOggi" :key="t.id" class="timb-row">
        <span class="timb-name">{{ t.userName }}</span>
        <span class="timb-time">{{ fmtTime(t.timestamp) }}</span>
        <span class="timb-status" :class="t.tipo === 'ENTRATA' ? 'ts-in' : 'ts-out'">{{ t.tipo }}</span>
      </div>
    </div>
  </div>

  <div class="two-col">
    <div class="card">
      <div class="card-title">Permessi in attesa</div>
      <div v-if="permessiAttesa.length === 0" class="empty-state">Nessuna richiesta in attesa.</div>
      <div v-for="p in permessiAttesa" :key="p.id" class="perm-row">
        <div>
          <div class="perm-info">{{ p.userName }} — {{ p.tipo }}</div>
          <div class="perm-date">{{ fmt(p.dal) }}</div>
        </div>
        <div class="perm-actions">
          <button class="btn-sm btn-ok" @click="aggiorna(p.id, 'APPROVATO')">Approva</button>
          <button class="btn-sm btn-no" @click="aggiorna(p.id, 'RIFIUTATO')">Rifiuta</button>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-title">Turni questa settimana</div>
      <div v-if="turniSettimana.length === 0" class="empty-state">Nessun turno.</div>
      <div v-for="t in turniSettimana" :key="t.id" class="timb-row">
        <span class="timb-name">{{ t.userName }}</span>
        <span class="timb-time">{{ fmt(t.data) }}</span>
        <span class="timb-status ts-in">{{ t.inizio }}–{{ t.fine }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/api'
import { toDateKey, weekDates } from '@/stores/planner'

interface UserResponse { id: number; name: string; email: string; role: string }
interface TimbraturaResponse { id: number; userName: string; tipo: string; timestamp: string }
interface PermessoResponse { id: number; userName: string; tipo: string; dal: string; stato: string }
interface TurnoResponse { id: number; userName: string; data: string; inizio: string; fine: string }

const utenti = ref<UserResponse[]>([])
const timbratureOggi = ref<TimbraturaResponse[]>([])
const permessiAttesa = ref<PermessoResponse[]>([])
const turniSettimana = ref<TurnoResponse[]>([])

function initials(name: string) {
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
}

function fmt(d: string) {
  if (!d) return ''
  const [y, m, g] = d.split('-')
  return `${g}/${m}/${y}`
}

function fmtTime(ts: string) {
  return new Date(ts).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
}

async function aggiorna(id: number, stato: string) {
  await api.patch(`/api/permessi/${id}/stato?stato=${stato}`, {})
  permessiAttesa.value = permessiAttesa.value.filter(p => p.id !== id)
}

onMounted(async () => {
  const oggi = toDateKey(new Date())
  const dates = weekDates(0)
  const dal = toDateKey(dates[0])
  const al = toDateKey(dates[6])

  const [u, t, p, turni] = await Promise.all([
    api.get<UserResponse[]>('/api/users'),
    api.get<TimbraturaResponse[]>('/api/timbrature'),
    api.get<PermessoResponse[]>('/api/permessi/in-attesa'),
    api.get<TurnoResponse[]>(`/api/turni?dal=${dal}&al=${al}`),
  ])

  utenti.value = u
  timbratureOggi.value = t.filter(x => x.timestamp.startsWith(oggi))
  permessiAttesa.value = p
  turniSettimana.value = turni
})
</script>

<style scoped>
.empty-state {
  font-size: 13px;
  color: var(--text-tertiary);
  padding: 8px 0;
}
</style>
