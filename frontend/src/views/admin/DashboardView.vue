<template>
  <div class="stats-row">
    <div class="stat">
      <div class="stat-label">Utenti</div>
      <div class="stat-val">{{ utenti.length }}</div>
    </div>
    <div class="stat">
      <div class="stat-label">Timbrature oggi</div>
      <div class="stat-val">{{ timbrature.length }}</div>
    </div>
    <div class="stat">
      <div class="stat-label">Permessi in attesa</div>
      <div class="stat-val">{{ permessi.length }}</div>
    </div>
    <div class="stat">
      <div class="stat-label">Messaggi non letti</div>
      <div class="stat-val">0</div>
    </div>
  </div>

  <div class="two-col">
    <div class="card">
      <div class="card-title">Utenti</div>
      <div v-for="u in utenti" :key="u.nome" class="user-row">
        <div class="avatar" :class="u.av">{{ u.iniziali }}</div>
        <div class="user-info">
          <div class="user-name">{{ u.nome }}</div>
          <div class="user-role">{{ u.ruolo }}</div>
        </div>
        <span class="pill" :class="u.online ? 'pill-online' : 'pill-off'">
          {{ u.online ? 'online' : 'offline' }}
        </span>
      </div>
    </div>

    <div class="card">
      <div class="card-title">Timbrature in tempo reale</div>
      <div v-for="t in timbrature" :key="t.nome" class="timb-row">
        <span class="timb-name">{{ t.nome }}</span>
        <span class="timb-time">{{ t.ora }}</span>
        <span class="timb-status" :class="t.tipo === 'entrata' ? 'ts-in' : 'ts-out'">{{ t.tipo }}</span>
      </div>
    </div>
  </div>

  <div class="two-col">
    <div class="card">
      <div class="card-title">Permessi in attesa</div>
      <div v-for="p in permessi" :key="p.nome" class="perm-row">
        <div>
          <div class="perm-info">{{ p.nome }} — {{ p.tipo }}</div>
          <div class="perm-date">{{ p.data }}</div>
        </div>
        <div class="perm-actions">
          <button class="btn-sm btn-ok">Approva</button>
          <button class="btn-sm btn-no">Rifiuta</button>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-title">Planner settimana</div>
      <div class="planner-grid">
        <div v-for="(nomi, giorno) in planner" :key="giorno" class="day-col">
          <div class="day-label">{{ giorno }}</div>
          <div v-for="n in nomi" :key="n" class="day-slot">{{ n }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const utenti: { nome: string; ruolo: string; iniziali: string; online: boolean; av: string }[] = []
const timbrature: { nome: string; ora: string; tipo: string }[] = []
const permessi: { nome: string; tipo: string; data: string }[] = []
const planner: Record<string, string[]> = { Lun: [], Mar: [], Mer: [], Gio: [], Ven: [], Sab: [], Dom: [] }
</script>
