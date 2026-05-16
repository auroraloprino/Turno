<template>
  <div v-for="shift in store.getShifts(dateKey)" :key="shift.id" class="day-slot">
    <span class="slot-name">{{ shift.name }}</span>
    <span class="slot-time">{{ shift.start }}–{{ shift.end }}</span>
    <button class="slot-remove" @click="store.removeShift(dateKey, shift.id)">×</button>
  </div>

  <div v-if="adding" class="add-form">
    <select v-model="form.userId">
      <option v-for="m in members" :key="m.id" :value="m.id">{{ m.name }}</option>
    </select>
    <template v-if="!compact">
      <input type="time" v-model="form.start" />
      <input type="time" v-model="form.end" />
    </template>
    <div class="add-form-actions">
      <button class="btn-sm btn-ok" @click="confirm">Ok</button>
      <button class="btn-sm btn-no" @click="adding = false">✕</button>
    </div>
  </div>

  <button v-else class="btn-add-slot" @click="open">+ Aggiungi</button>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { usePlannerStore, members } from '@/stores/planner'

const props = defineProps<{ dateKey: string; compact?: boolean }>()

const store  = usePlannerStore()
const adding = ref(false)
const form   = ref({ userId: 0, start: '09:00', end: '17:00' })

function open() {
  form.value = { userId: members[0]?.id ?? 0, start: '09:00', end: '17:00' }
  adding.value = true
}

function confirm() {
  store.addShift(props.dateKey, form.value.userId, form.value.start, form.value.end)
  adding.value = false
}
</script>

<style scoped>
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
  top: 3px; right: 4px;
  background: none; border: none;
  color: var(--text-tertiary);
  cursor: pointer; font-size: 12px; line-height: 1; padding: 0;
}
.slot-remove:hover { color: var(--coral-dark); }

.add-form { display: flex; flex-direction: column; gap: 4px; }

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
