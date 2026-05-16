<template>
  <div class="week-nav">
    <button class="btn-week" @click="weekOffset--">&#8592;</button>
    <span class="week-label">{{ weekLabel }}</span>
    <button class="btn-week" @click="weekOffset++">&#8594;</button>
  </div>

  <div class="plan-grid-7">
    <div v-for="day in DAYS" :key="day" class="day-col">
      <div class="day-label">{{ day }}</div>

      <template v-if="store.getShifts(weekKey, day).length">
        <div
          v-for="shift in store.getShifts(weekKey, day)"
          :key="shift.id"
          class="day-slot"
          :class="{ me: shift.userId === ME_ID }"
        >
          <span class="slot-name">{{ shift.name }}</span>
          <span class="slot-time">{{ shift.start }}–{{ shift.end }}</span>
        </div>
      </template>
      <div v-else class="day-empty">—</div>
    </div>
  </div>

  <div class="plan-legend">
    <div style="display:flex;align-items:center;gap:6px;">
      <div class="legend-dot" style="background:#1D9E75;"></div>
      <span>Il mio turno</span>
    </div>
    <div style="display:flex;align-items:center;gap:6px;">
      <div class="legend-dot" style="background:var(--purple);"></div>
      <span>Colleghi</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { usePlannerStore, DAYS } from '@/stores/planner'

const ME_ID = 1  // TODO: replace with auth store user id

const store = usePlannerStore()
const weekOffset = ref(0)

function getWeekKey(offset: number): string {
  const d = new Date()
  d.setDate(d.getDate() - d.getDay() + 1 + offset * 7)
  const jan1 = new Date(d.getFullYear(), 0, 1)
  const week = Math.ceil(((d.getTime() - jan1.getTime()) / 86400000 + jan1.getDay() + 1) / 7)
  return `${d.getFullYear()}-W${String(week).padStart(2, '0')}`
}

const weekKey = computed(() => getWeekKey(weekOffset.value))
const weekLabel = computed(() => {
  const d = new Date()
  d.setDate(d.getDate() - d.getDay() + 1 + weekOffset.value * 7)
  const from = d.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit' })
  d.setDate(d.getDate() + 6)
  const to = d.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit' })
  return `${from} – ${to}`
})
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
}

.day-slot.me {
  border-left-color: #1D9E75;
  color: #5ecfb0;
  background: var(--teal-light);
}

.slot-name { font-size: 11px; color: inherit; }
.slot-time { font-size: 10px; color: var(--text-secondary); }
.day-slot.me .slot-time { color: #5ecfb0; opacity: 0.7; }

.day-empty {
  font-size: 11px;
  color: var(--text-tertiary);
  text-align: center;
  padding: 4px 0;
}
</style>
