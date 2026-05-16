<template>
  <div class="week-nav">
    <button class="btn-week" @click="prevWeek">&#8592;</button>
    <span class="week-label">{{ weekLabel }}</span>
    <button class="btn-week" @click="nextWeek">&#8594;</button>
  </div>

  <div class="plan-grid-7">
    <div v-for="day in DAYS" :key="day" class="day-col">
      <div class="day-label">{{ day }}</div>

      <div v-for="shift in store.getShifts(weekKey, day)" :key="shift.id" class="day-slot">
        <span class="slot-name">{{ shift.name }}</span>
        <span class="slot-time">{{ shift.start }}–{{ shift.end }}</span>
        <button class="slot-remove" @click="store.removeShift(weekKey, day, shift.id)">×</button>
      </div>

      <div v-if="adding[day]" class="add-form">
        <select v-model="form[day].userId">
          <option v-for="m in members" :key="m.id" :value="m.id">{{ m.name }}</option>
        </select>
        <input type="time" v-model="form[day].start" />
        <input type="time" v-model="form[day].end" />
        <div class="add-form-actions">
          <button class="btn-sm btn-ok" @click="confirm(day)">Ok</button>
          <button class="btn-sm btn-no" @click="adding[day] = false">✕</button>
        </div>
      </div>

      <button v-else class="btn-add-slot" @click="open(day)">+ Aggiungi</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { usePlannerStore, DAYS, members } from '@/stores/planner'
import type { DayKey } from '@/stores/planner'

const store = usePlannerStore()

const weekOffset = ref(0)

function getWeekKey(offset: number): string {
  const d = new Date()
  d.setDate(d.getDate() - d.getDay() + 1 + offset * 7)
  const jan1 = new Date(d.getFullYear(), 0, 1)
  const week = Math.ceil(((d.getTime() - jan1.getTime()) / 86400000 + jan1.getDay() + 1) / 7)
  return `${d.getFullYear()}-W${String(week).padStart(2, '0')}`
}

function getWeekDates(offset: number): { from: string; to: string } {
  const d = new Date()
  d.setDate(d.getDate() - d.getDay() + 1 + offset * 7)
  const from = d.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit' })
  d.setDate(d.getDate() + 6)
  const to = d.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit' })
  return { from, to }
}

const weekKey  = computed(() => getWeekKey(weekOffset.value))
const weekLabel = computed(() => {
  const { from, to } = getWeekDates(weekOffset.value)
  return `${from} – ${to}`
})

function prevWeek() { weekOffset.value-- }
function nextWeek() { weekOffset.value++ }

const adding = ref<Partial<Record<DayKey, boolean>>>({})
const form   = ref<Record<string, { userId: number; start: string; end: string }>>(
  Object.fromEntries(DAYS.map(d => [d, { userId: 0, start: '09:00', end: '17:00' }]))
)

function open(day: DayKey) {
  form.value[day] = { userId: members[0]?.id ?? 0, start: '09:00', end: '17:00' }
  adding.value[day] = true
}

function confirm(day: DayKey) {
  const f = form.value[day]
  if (f.start && f.end) {
    store.addShift(weekKey.value, day, f.userId, f.start, f.end)
  }
  adding.value[day] = false
}
</script>

<style scoped>
.week-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.week-label {
  font-size: 13px;
  color: var(--text-primary);
  min-width: 120px;
  text-align: center;
}

.btn-week {
  background: var(--bg-tertiary);
  border: 0.5px solid var(--border-md);
  color: var(--text-primary);
  border-radius: var(--radius-md);
  padding: 4px 10px;
  cursor: pointer;
  font-size: 13px;
}
.btn-week:hover { opacity: 0.7; }

.plan-grid-7 {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.day-col {
  background: var(--bg-tertiary);
  border-radius: 6px;
  padding: 8px 7px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.day-slot {
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
  border-radius: 4px;
  padding: 5px 7px;
  border-left: 2px solid var(--purple);
  position: relative;
}

.slot-name { font-size: 11px; color: var(--text-primary); }
.slot-time { font-size: 10px; color: var(--text-secondary); }

.slot-remove {
  position: absolute;
  top: 3px;
  right: 4px;
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  font-size: 12px;
  line-height: 1;
  padding: 0;
}
.slot-remove:hover { color: var(--coral-dark); }

.add-form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.add-form select,
.add-form input {
  padding: 5px 7px;
  border: 0.5px solid var(--border-md);
  border-radius: 4px;
  font-size: 11px;
  color: var(--text-primary);
  background: var(--bg-secondary);
  outline: none;
  font-family: inherit;
}
.add-form select:focus,
.add-form input:focus { border-color: var(--purple); }
.add-form select option { background: var(--bg-secondary); }

.add-form-actions { display: flex; gap: 4px; }

.btn-add-slot {
  font-size: 10px;
  color: var(--text-tertiary);
  background: none;
  border: 0.5px dashed var(--border-md);
  border-radius: 4px;
  padding: 5px;
  cursor: pointer;
  text-align: center;
  transition: color 0.12s, border-color 0.12s;
}
.btn-add-slot:hover { color: var(--text-secondary); border-color: var(--purple); }
</style>
