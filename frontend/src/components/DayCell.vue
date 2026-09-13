<template>
  <div v-for="shift in store.getShifts(dateKey)" :key="shift.id" class="day-slot">
    <span class="slot-name">{{ shift.userName }}</span>
    <span class="slot-time">{{ shift.inizio }}–{{ shift.fine }}</span>
    <button class="slot-remove" @click="store.removeShift(shift.id)">×</button>
  </div>

  <button class="btn-add-slot" ref="anchorRef" @click="open">+ Aggiungi</button>

  <Teleport to="body">
    <div v-if="adding" class="pop-backdrop" @click.self="adding = false">
      <div class="pop" :style="popStyle">
        <div class="pop-title">Nuovo turno · {{ dateKey }}</div>

        <div style="position:relative">
          <input
            ref="searchRef"
            v-model="userSearch"
            class="pop-input"
            placeholder="Cerca per nome o email…"
            @input="showDrop = true"
            @focus="showDrop = true"
            @blur="hideDrop"
          />
          <div v-if="showDrop && filteredUsers.length" class="pop-drop">
            <div
              v-for="u in filteredUsers"
              :key="u.id"
              class="pop-drop-item"
              @mousedown.prevent="pickUser(u)"
            >
              <span class="pd-name">{{ u.name }}</span>
              <span class="pd-email">{{ u.email }}</span>
            </div>
          </div>
          <div v-if="showDrop && userSearch && !filteredUsers.length" class="pop-drop">
            <div class="pd-empty">Nessun utente trovato</div>
          </div>
        </div>

        <div class="pop-times">
          <div class="pop-field">
            <label>Inizio</label>
            <input type="time" v-model="form.inizio" class="pop-input" />
          </div>
          <div class="pop-field">
            <label>Fine</label>
            <input type="time" v-model="form.fine" class="pop-input" />
          </div>
        </div>

        <div class="pop-actions">
          <button class="btn-sm btn-ok" :disabled="!form.userId" @click="confirm">Aggiungi</button>
          <button class="btn-sm btn-no" @click="adding = false">Annulla</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { usePlannerStore } from '@/stores/planner'
import { api } from '@/api'

const props = defineProps<{ dateKey: string }>()

const store     = usePlannerStore()
const adding    = ref(false)
const anchorRef = ref<HTMLElement | null>(null)
const searchRef = ref<HTMLInputElement | null>(null)
const popStyle  = ref('')

const users      = ref<{ id: number; name: string; email: string }[]>([])
const userSearch = ref('')
const showDrop   = ref(false)
const form       = ref({ userId: 0, inizio: '09:00', fine: '17:00' })

const filteredUsers = computed(() => {
  const q = userSearch.value.toLowerCase().trim()
  if (!q) return users.value
  return users.value.filter(u =>
    u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q)
  )
})

function pickUser(u: { id: number; name: string; email: string }) {
  form.value.userId = u.id
  userSearch.value  = `${u.name} (${u.email})`
  showDrop.value    = false
}

function hideDrop() {
  setTimeout(() => { showDrop.value = false }, 150)
}

async function open() {
  if (users.value.length === 0)
    users.value = await api.get<{ id: number; name: string; email: string }[]>('/api/users')

  form.value    = { userId: 0, inizio: '09:00', fine: '17:00' }
  userSearch.value = ''
  showDrop.value   = false

  await nextTick()
  const rect = anchorRef.value?.getBoundingClientRect()
  if (rect) {
    const left = Math.min(rect.left, window.innerWidth - 300)
    const top  = rect.bottom + 6
    popStyle.value = `left:${left}px;top:${top}px`
  }

  adding.value = true
  nextTick(() => searchRef.value?.focus())
}

async function confirm() {
  if (!form.value.userId) return
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
  width: 100%;
}
.btn-add-slot:hover { color: var(--text-secondary); border-color: var(--purple); }

/* popover */
.pop-backdrop {
  position: fixed;
  inset: 0;
  z-index: 200;
}

.pop {
  position: fixed;
  width: 280px;
  background: var(--bg-secondary);
  border: 0.5px solid var(--border-md);
  border-radius: var(--radius-md);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  box-shadow: 0 8px 24px rgba(0,0,0,.4);
  z-index: 201;
}

.pop-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.07em;
}

.pop-input {
  width: 100%;
  padding: 7px 10px;
  border: 0.5px solid var(--border-md);
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--text-primary);
  background: var(--bg-tertiary);
  outline: none;
  font-family: inherit;
  box-sizing: border-box;
}
.pop-input:focus { border-color: var(--purple); }

.pop-drop {
  position: absolute;
  top: 100%;
  left: 0; right: 0;
  background: var(--bg-secondary);
  border: 0.5px solid var(--border-md);
  border-radius: var(--radius-md);
  z-index: 202;
  max-height: 180px;
  overflow-y: auto;
  box-shadow: 0 4px 16px rgba(0,0,0,.35);
  margin-top: 2px;
}
.pop-drop-item {
  display: flex;
  flex-direction: column;
  padding: 8px 10px;
  cursor: pointer;
  transition: background .1s;
}
.pop-drop-item:hover { background: var(--bg-tertiary); }
.pd-name  { font-size: 13px; color: var(--text-primary); font-weight: 500; }
.pd-email { font-size: 11px; color: var(--text-secondary); margin-top: 1px; }
.pd-empty { padding: 10px; font-size: 12px; color: var(--text-tertiary); }

.pop-times {
  display: flex;
  gap: 8px;
}
.pop-field {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.pop-field label {
  font-size: 11px;
  color: var(--text-secondary);
}

.pop-actions {
  display: flex;
  gap: 6px;
  justify-content: flex-end;
}
</style>
