<template>
  <div v-for="shift in store.getShifts(dateKey)" :key="shift.id" class="day-slot">
    <span class="slot-name">{{ shift.userName }}</span>
    <span class="slot-time">{{ shift.inizio }}–{{ shift.fine }}</span>
    <button class="slot-remove" @click="store.removeShift(shift.id)">×</button>
  </div>

  <div v-if="adding" class="add-form">
    <select v-model="form.userId">
      <option v-for="u in users" :key="u.id" :value="u.id">{{ u.name }}</option>
    </select>
    <input type="time" v-model="form.inizio" />
    <input type="time" v-model="form.fine" />
    <div class="add-form-actions">
      <button class="btn-sm btn-ok" @click="confirm">Ok</button>
      <button class="btn-sm btn-no" @click="adding = false">✕</button>
    </div>
  </div>

  <button v-else class="btn-add-slot" @click="open">+ Aggiungi</button>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { usePlannerStore } from '@/stores/planner'
import { api } from '@/api'

const props = defineProps<{ dateKey: string }>()

const store = usePlannerStore()
const adding = ref(false)
const users = ref<{ id: number; name: string }[]>([])
const form = ref({ userId: 0, inizio: '09:00', fine: '17:00' })

async function open() {
  if (users.value.length === 0)
    users.value = await api.get<{ id: number; name: string; role: string }[]>('/api/users')
  form.value = { userId: users.value[0]?.id ?? 0, inizio: '09:00', fine: '17:00' }
  adding.value = true
}

async function confirm() {
  await store.addShift(props.dateKey, form.value.userId, form.value.inizio, form.value.fine)
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
