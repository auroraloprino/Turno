<template>
  <!-- Nav bar -->
  <div class="plan-nav">
    <button class="btn-week" @click="offset--">&#8592;</button>
    <span class="week-label">{{ label }}</span>
    <button class="btn-week" @click="offset++">&#8594;</button>
    <div class="view-toggle">
      <button :class="{ active: view === 'week' }"  @click="view = 'week'">Settimana</button>
      <button :class="{ active: view === 'month' }" @click="view = 'month'">Mese</button>
    </div>
  </div>

  <!-- Weekly view -->
  <div v-if="view === 'week'" class="plan-grid-7">
    <div v-for="date in currentWeekDates" :key="toDateKey(date)" class="day-col">
      <div class="day-label">
        {{ DAYS[(date.getDay() + 6) % 7] }}
        <span class="day-num">{{ date.getDate() }}</span>
      </div>
      <DayCell :dateKey="toDateKey(date)" />
    </div>
  </div>

  <!-- Monthly view -->
  <div v-else class="month-grid">
    <div v-for="d in DAYS" :key="d" class="month-header">{{ d }}</div>
    <div v-for="_ in leadingBlanks" class="month-cell empty" />
    <div
      v-for="date in currentMonthDates"
      :key="toDateKey(date)"
      class="month-cell"
      :class="{ clickable: store.getShifts(toDateKey(date)).length > 0 }"
      @click="openModal(date)"
    >
      <div class="month-day-num">{{ date.getDate() }}</div>
      <div v-if="store.getShifts(toDateKey(date)).length" class="month-shift-count">
        {{ store.getShifts(toDateKey(date)).length }} turni
      </div>
    </div>
  </div>

  <!-- Day modal -->
  <Teleport to="body">
    <div v-if="modalDate" class="modal-backdrop" @click.self="modalDate = null">
      <div class="modal">
        <div class="modal-header">
          <span>{{ modalDate.toLocaleDateString('it-IT', { weekday: 'long', day: '2-digit', month: '2-digit', year: '2-digit' }) }}</span>
          <button class="modal-close" @click="modalDate = null">×</button>
        </div>
        <div v-for="(group, start) in groupedShifts" :key="start" class="modal-group">
          <div class="modal-group-label">{{ start }}</div>
          <div v-for="shift in group" :key="shift.id" class="modal-row">
            <span class="modal-name">{{ shift.name }}</span>
            <span class="modal-time">{{ shift.start }}–{{ shift.end }}</span>
          </div>
        </div>
        <div v-if="!Object.keys(groupedShifts).length" class="modal-empty">Nessun turno</div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { usePlannerStore, DAYS, toDateKey, weekDates, monthDates, weekLabel, monthLabel } from '@/stores/planner'
import type { Shift } from '@/stores/planner'
import DayCell from '@/components/DayCell.vue'

const store   = usePlannerStore()
const view    = ref<'week' | 'month'>('week')
const offset  = ref(0)

const currentWeekDates  = computed(() => weekDates(offset.value))
const currentMonthDates = computed(() => monthDates(offset.value))
const label             = computed(() => view.value === 'week' ? weekLabel(offset.value) : monthLabel(offset.value))
const leadingBlanks     = computed(() => (currentMonthDates.value[0].getDay() + 6) % 7)

const modalDate = ref<Date | null>(null)

function openModal(date: Date) {
  modalDate.value = date
}

const groupedShifts = computed(() => {
  if (!modalDate.value) return {}
  const shifts = [...store.getShifts(toDateKey(modalDate.value))]
    .sort((a, b) => a.start.localeCompare(b.start) || a.end.localeCompare(b.end))
  return shifts.reduce<Record<string, Shift[]>>((acc, s) => {
    ;(acc[s.start] ??= []).push(s)
    return acc
  }, {})
})
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

/* ─── Weekly ─── */
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

/* ─── Monthly ─── */
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

.month-cell.clickable { cursor: pointer; }
.month-cell.clickable:hover { background: #3a3a3a; }

.month-shift-count {
  font-size: 10px;
  color: var(--purple-dark);
  background: var(--purple-light);
  border-radius: 4px;
  padding: 2px 6px;
  width: fit-content;
}

/* ─── Modal ─── */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: var(--bg-secondary);
  border: 0.5px solid var(--border-md);
  border-radius: var(--radius-lg);
  padding: 20px;
  min-width: 320px;
  max-width: 480px;
  max-height: 70vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  text-transform: capitalize;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 18px;
  cursor: pointer;
  line-height: 1;
  padding: 0;
}
.modal-close:hover { color: var(--text-primary); }

.modal-group { display: flex; flex-direction: column; gap: 6px; }

.modal-group-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.modal-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 7px 10px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  border-left: 2px solid var(--purple);
}

.modal-name { font-size: 13px; color: var(--text-primary); }
.modal-time { font-size: 11px; color: var(--text-secondary); }

.modal-empty { font-size: 13px; color: var(--text-tertiary); text-align: center; padding: 10px 0; }
</style>
