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
      <button class="btn-timb" :class="{ uscita: inTurno }" @click="toggle">
        {{ inTurno ? 'Timbra uscita' : 'Timbra entrata' }}
      </button>
    </div>
  </div>

  <div class="card">
    <div class="card-title">Storico recente</div>
    <div v-for="(h, i) in history" :key="i" class="hist-row">
      <span class="hist-label">{{ h.label }}</span>
      <span class="hist-val">{{ h.time }}</span>
      <span class="pill" :class="h.tipo === 'entrata' ? 'p-in' : 'p-out'">{{ h.tipo }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const clock = ref('')
const date  = ref('')
const inTurno = ref(false)
const since   = ref('')
const history = ref<{ label: string; time: string; tipo: string }[]>([])

let interval: ReturnType<typeof setInterval>

function nowTime() {
  const d = new Date()
  return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
}

function tick() {
  const now = new Date()
  clock.value = now.toLocaleTimeString('it-IT')
  date.value  = now.toLocaleDateString('it-IT', { weekday: 'long', day: 'numeric', month: 'long' })
}

function toggle() {
  const time = nowTime()
  if (inTurno.value) {
    history.value.unshift({ label: 'Uscita', time, tipo: 'uscita' })
    inTurno.value = false
    since.value   = ''
  } else {
    history.value.unshift({ label: 'Entrata', time, tipo: 'entrata' })
    inTurno.value = true
    since.value   = time
  }
}

onMounted(() => {
  tick()
  interval = setInterval(tick, 1000)
})

onUnmounted(() => clearInterval(interval))
</script>
