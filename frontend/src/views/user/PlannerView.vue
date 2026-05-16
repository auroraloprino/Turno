<template>
  <div class="plan-nav">
    <button class="btn-week" @click="offset--">&#8592;</button>
    <span class="week-label">{{ label }}</span>
    <button class="btn-week" @click="offset++">&#8594;</button>
    <div class="view-toggle">
      <button :class="{ active: view === 'week' }"  @click="view = 'week'">Settimana</button>
      <button :class="{ active: view === 'month' }" @click="view = 'month'">Mese</button>
    </div>
  </div>

  <!-- Weekly -->
  <div v-if="view === 'week'" class="plan-grid-7">
    <div v-for="date in currentWeekDates" :key="toDateKey(date)" class="day-col">
      <div class="day-label">
        {{ DAYS[(date.getDay() + 6) % 7] }}
        <span class="day-num">{{ date.getDate() }}</span>
      </div>
      <div
        v-for="shift in store.getShifts(toDateKey(date))"
        :key="shift.id"
        class="day-slot"
        :class="{ me: shift.userId === ME_ID }"
      >
        <span class="slot-name">{{ shift.name }}</span>
        <span class="slot-time">{{ shift.start }}–{{ shift.end }}</span>
      </div>
    </div>
  </div>

  <!-- Monthly -->
  <div v-else class="month-grid">
    <div v-for="d in DAYS" :key="d" class="month-header">{{ d }}</div>
    <div v-for="_ in leadingBlanks" class="month-cell empty" />
    <div v-for="date in currentMonthDates" :key="toDateKey(date)" class="month-cell">
      <div class="month-day-num">{{ date.getDate() }}</div>
      <div
        v-for="shift in store.getShifts(toDateKey(date))"
        :key="shift.id"
        class="day-slot"
        :class="{ me: shift.userId === ME_ID }"
      >
        <span class="slot-name">{{ shift.name }}</span>
      </div>
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
import { usePlannerStore, DAYS, toDateKey, weekDates, monthDates, weekLabel, monthLabel } from '@/stores/planner'

const ME_ID = 1  // TODO: replace with auth store user id

const store  = usePlannerStore()
const view   = ref<'week' | 'month'>('week')
const offset = ref(0)

const currentWeekDates  = computed(() => weekDates(offset.value))
const currentMonthDates = computed(() => monthDates(offset.value))
const label             = computed(() => view.value === 'week' ? weekLabel(offset.value) : monthLabel(offset.value))
const leadingBlanks     = computed(() => (currentMonthDates.value[0].getDay() + 6) % 7)
</script>

<style scoped>
.plan-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.week-label {
  font-size: 13px;
  color: var(--text-primary);
  min-width: 140px;
  text-align: center;
  text-transform: capitalize;
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

.view-toggle {
  margin-left: auto;
  display: flex;
  border: 0.5px solid var(--border-md);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.view-toggle button {
  padding: 5px 14px;
  font-size: 12px;
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background 0.12s, color 0.12s;
}

.view-toggle button.active {
  background: var(--purple);
  color: white;
}

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

.day-num {
  font-weight: 400;
  color: var(--text-tertiary);
  margin-left: 4px;
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.month-header {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-tertiary);
  text-align: center;
  padding: 4px 0;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.month-cell {
  background: var(--bg-tertiary);
  border-radius: 6px;
  padding: 6px 6px 4px;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.month-cell.empty { background: transparent; }

.month-day-num {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 2px;
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
  background: var(--teal-light);
}

.slot-name { font-size: 11px; color: var(--text-primary); }
.day-slot.me .slot-name { color: #5ecfb0; }
.slot-time { font-size: 10px; color: var(--text-secondary); }
.day-slot.me .slot-time { color: #5ecfb0; opacity: 0.7; }

.plan-legend {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 12px;
  font-size: 11px;
  color: var(--text-secondary);
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  flex-shrink: 0;
}
</style>
