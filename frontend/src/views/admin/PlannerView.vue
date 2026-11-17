<template>
  <div class="plan-nav">
    <button class="btn-week" @click="offset--">&#8592;</button>
    <span class="week-label">{{ label }}</span>
    <button class="btn-week" @click="offset++">&#8594;</button>
    <button v-if="offset !== 0" class="btn-today" @click="offset = 0">
      Oggi
    </button>

    <template v-if="view === 'week'">
      <input
        v-model="search"
        class="plan-input"
        type="search"
        placeholder="Cerca persona…"
      />
      <select v-model="teamFilter" class="plan-input">
        <option value="">Tutti i team</option>
        <option v-for="t in teams" :key="t" :value="t">{{ t }}</option>
        <option v-if="hasNoTeam" :value="NO_TEAM">{{ NO_TEAM }}</option>
      </select>
      <button
        class="btn-week"
        :disabled="copying"
        title="Copia i turni della settimana precedente in questa"
        @click="copiaSettimanaPrecedente"
      >
        {{ copying ? "Copia…" : "Copia sett. prec." }}
      </button>
    </template>

    <div class="view-toggle">
      <button :class="{ active: view === 'week' }" @click="setView('week')">
        Settimana
      </button>
      <button :class="{ active: view === 'month' }" @click="setView('month')">
        Mese
      </button>
    </div>
  </div>

  <div v-if="notice" class="notice" :class="notice.kind">
    {{ notice.text }}
    <button class="notice-close" @click="notice = null">×</button>
  </div>

  <div v-if="view === 'week'" class="roster-wrap">
    <table class="roster">
      <thead>
        <tr>
          <th class="col-name">
            {{ visibleUsers.length }}
            {{ visibleUsers.length === 1 ? "persona" : "persone" }}
          </th>
          <th
            v-for="(d, i) in days"
            :key="dayKeys[i]"
            :class="{ today: dayKeys[i] === todayKey }"
          >
            <div class="day-head">
              {{ DAYS[i] }} <span class="day-num">{{ d.getDate() }}</span>
            </div>
            <div class="coverage" title="Persone in turno">
              {{ coverage[i] }} in turno
            </div>
          </th>
        </tr>
      </thead>
      <tbody>
        <template v-for="g in groups" :key="g.team">
          <tr v-if="showTeamRows" class="team-row">
            <td :colspan="8">
              <span class="team-label"
                >{{ g.team }} · {{ g.users.length }}</span
              >
            </td>
          </tr>
          <tr v-for="u in g.users" :key="u.id">
            <td class="col-name">
              <div class="person-name">{{ u.name }}</div>
              <div class="person-hours">{{ weeklyHours(u.id) }}</div>
            </td>
            <td
              v-for="key in dayKeys"
              :key="key"
              class="cell"
              :class="{
                today: key === todayKey,
                'on-leave': leaveAt(u.id, key)?.fullDay,
              }"
              @click="openAdd($event, u, key)"
            >
              <div v-if="leaveAt(u.id, key)" class="leave-tag">
                {{ leaveAt(u.id, key)!.tipo }}
              </div>
              <div
                v-for="s in shiftsAt(u.id, key)"
                :key="s.id"
                class="chip"
                @click.stop
              >
                {{ hhmm(s.inizio) }}–{{ hhmm(s.fine) }}
                <button
                  class="chip-x"
                  title="Elimina turno"
                  @click.stop="rimuovi(s.id)"
                >
                  ×
                </button>
              </div>
              <span
                v-if="
                  !leaveAt(u.id, key)?.fullDay && !shiftsAt(u.id, key).length
                "
                class="cell-add"
                >+</span
              >
            </td>
          </tr>
        </template>
        <tr v-if="!visibleUsers.length">
          <td colspan="8" class="empty">
            {{ loading ? "Caricamento…" : "Nessuna persona trovata." }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <div v-else class="month-grid">
    <div v-for="d in DAYS" :key="d" class="month-header">{{ d }}</div>
    <div v-for="n in leadingBlanks" :key="'b' + n" class="month-cell empty" />
    <div
      v-for="date in currentMonthDates"
      :key="toDateKey(date)"
      class="month-cell"
      :class="[
        coverageClass(peopleOn(toDateKey(date))),
        { today: toDateKey(date) === todayKey },
      ]"
      title="Apri la settimana"
      @click="openWeekOf(date)"
    >
      <div class="month-day-num">{{ date.getDate() }}</div>
      <div v-if="peopleOn(toDateKey(date))" class="month-shift-count">
        {{ peopleOn(toDateKey(date)) }} in turno
      </div>
    </div>
  </div>

  <Teleport to="body">
    <div v-if="adding" class="pop-backdrop" @click.self="adding = null">
      <div
        class="pop"
        :style="{ left: adding.x + 'px', top: adding.y + 'px' }"
        @keydown.enter="salva"
        @keydown.esc="adding = null"
      >
        <div class="pop-title">
          {{ adding.userName }} ·
          {{
            fromDateKey(adding.dateKey).toLocaleDateString("it-IT", {
              weekday: "short",
              day: "numeric",
              month: "short",
            })
          }}
        </div>
        <div class="pop-times">
          <label>
            Inizio
            <input ref="firstInput" v-model="adding.inizio" type="time" />
          </label>
          <label>
            Fine
            <input v-model="adding.fine" type="time" />
          </label>
        </div>
        <div v-if="adding.error" class="pop-error">{{ adding.error }}</div>
        <div class="pop-actions">
          <button
            class="btn-sm btn-ok"
            :disabled="adding.saving"
            @click="salva"
          >
            Aggiungi
          </button>
          <button class="btn-sm btn-no" @click="adding = null">Annulla</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from "vue";
import { api } from "@/api";
import {
  usePlannerStore,
  DAYS,
  toDateKey,
  weekDates,
  monthDates,
  weekLabel,
  monthLabel,
  weekOffsetOf,
  fromDateKey,
  hhmm,
} from "@/stores/planner";
import type { Shift, NewShift } from "@/stores/planner";

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  team: string | null;
}

interface Permesso {
  userId: number;
  tipo: string;
  dal: string;
  al: string;
  oraInizio: string | null;
  stato: string;
}

const NO_TEAM = "Senza team";

const store = usePlannerStore();
const view = ref<"week" | "month">("week");
const offset = ref(0);
const loading = ref(false);

const users = ref<User[]>([]);
const permessi = ref<Permesso[]>([]);
const search = ref("");
const teamFilter = ref("");
const copying = ref(false);
const notice = ref<{ kind: "ok" | "err"; text: string } | null>(null);

const todayKey = toDateKey(new Date());

const days = computed(() => weekDates(offset.value));
const dayKeys = computed(() => days.value.map(toDateKey));
const currentMonthDates = computed(() => monthDates(offset.value));
const leadingBlanks = computed(
  () => (currentMonthDates.value[0].getDay() + 6) % 7,
);
const label = computed(() =>
  view.value === "week" ? weekLabel(offset.value) : monthLabel(offset.value),
);

function setView(v: "week" | "month") {
  if (v === view.value) return;
  // passando da settimana a mese (e viceversa) resto più o meno sullo stesso periodo
  const anchor =
    view.value === "week" ? days.value[0] : currentMonthDates.value[0];
  if (v === "week") {
    offset.value = weekOffsetOf(anchor);
  } else {
    const now = new Date();
    offset.value =
      (anchor.getFullYear() - now.getFullYear()) * 12 +
      anchor.getMonth() -
      now.getMonth();
  }
  view.value = v;
}

function openWeekOf(date: Date) {
  view.value = "week";
  offset.value = weekOffsetOf(date);
}

// l'owner gestisce l'attività e non fa turni
const schedulable = computed(() =>
  users.value.filter((u) => u.role !== "OWNER"),
);

const teams = computed(() =>
  [
    ...new Set(
      schedulable.value.map((u) => u.team).filter(Boolean) as string[],
    ),
  ].sort((a, b) => a.localeCompare(b, "it")),
);
const hasNoTeam = computed(() => schedulable.value.some((u) => !u.team));
const showTeamRows = computed(() => teams.value.length > 0);

const visibleUsers = computed(() => {
  const q = search.value.trim().toLowerCase();
  return schedulable.value.filter((u) => {
    if (teamFilter.value) {
      const t = u.team ?? NO_TEAM;
      if (t !== teamFilter.value) return false;
    }
    return (
      !q ||
      u.name.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q)
    );
  });
});

const groups = computed(() => {
  const map = new Map<string, User[]>();
  for (const u of visibleUsers.value) {
    const t = u.team ?? NO_TEAM;
    if (!map.has(t)) map.set(t, []);
    map.get(t)!.push(u);
  }
  return [...map.entries()]
    .sort(([a], [b]) =>
      a === NO_TEAM ? 1 : b === NO_TEAM ? -1 : a.localeCompare(b, "it"),
    )
    .map(([team, list]) => ({
      team,
      users: list.sort((a, b) => a.name.localeCompare(b.name, "it")),
    }));
});

const cellKey = (userId: number, dateKey: string) => `${userId}|${dateKey}`;

const shiftsByCell = computed(() => {
  const map = new Map<string, Shift[]>();
  for (const s of store.shifts) {
    const k = cellKey(s.userId, s.data);
    if (!map.has(k)) map.set(k, []);
    map.get(k)!.push(s);
  }
  for (const list of map.values())
    list.sort((a, b) => a.inizio.localeCompare(b.inizio));
  return map;
});

function shiftsAt(userId: number, dateKey: string): Shift[] {
  return shiftsByCell.value.get(cellKey(userId, dateKey)) ?? [];
}

// un permesso a ore (con oraInizio) non blocca l'intera giornata
const leaveByCell = computed(() => {
  const map = new Map<string, { tipo: string; fullDay: boolean }>();
  const visible = view.value === "week" ? dayKeys.value : [];
  for (const p of permessi.value) {
    if (p.stato !== "APPROVATO") continue;
    for (const key of visible) {
      if (key >= p.dal && key <= (p.al ?? p.dal)) {
        map.set(cellKey(p.userId, key), {
          tipo: p.tipo,
          fullDay: !p.oraInizio,
        });
      }
    }
  }
  return map;
});

function leaveAt(userId: number, dateKey: string) {
  return leaveByCell.value.get(cellKey(userId, dateKey));
}

const visibleIds = computed(() => new Set(visibleUsers.value.map((u) => u.id)));

// conta le persone e non i turni, chi ha due turni nello stesso giorno vale uno
const coverage = computed(() =>
  dayKeys.value.map(
    (key) =>
      new Set(
        store.shifts
          .filter((s) => s.data === key && visibleIds.value.has(s.userId))
          .map((s) => s.userId),
      ).size,
  ),
);

function minutes(t: string) {
  const [h, m] = t.split(":").map(Number);
  return h * 60 + m;
}

function weeklyHours(userId: number) {
  const total = dayKeys.value
    .flatMap((k) => shiftsAt(userId, k))
    .reduce((sum, s) => sum + minutes(s.fine) - minutes(s.inizio), 0);
  if (!total) return "";
  const h = Math.floor(total / 60);
  const m = total % 60;
  return m ? `${h}h ${m}m` : `${h}h`;
}

const peopleByDay = computed(() => {
  const map = new Map<string, Set<number>>();
  for (const s of store.shifts) {
    if (!map.has(s.data)) map.set(s.data, new Set());
    map.get(s.data)!.add(s.userId);
  }
  return map;
});
const peopleOn = (dateKey: string) => peopleByDay.value.get(dateKey)?.size ?? 0;

function coverageClass(n: number) {
  const total = schedulable.value.length || 1;
  if (!n) return "cov-0";
  const ratio = n / total;
  return ratio < 0.25 ? "cov-1" : ratio < 0.5 ? "cov-2" : "cov-3";
}

const lastTimes = { inizio: "09:00", fine: "17:00" };
const adding = ref<{
  userId: number;
  userName: string;
  dateKey: string;
  inizio: string;
  fine: string;
  x: number;
  y: number;
  error: string;
  saving: boolean;
} | null>(null);
const firstInput = ref<HTMLInputElement | null>(null);

function openAdd(e: MouseEvent, u: User, dateKey: string) {
  if (leaveAt(u.id, dateKey)?.fullDay) return;
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
  const popW = 240;
  const popH = 170;
  adding.value = {
    userId: u.id,
    userName: u.name,
    dateKey,
    ...lastTimes,
    x: Math.max(8, Math.min(rect.left, window.innerWidth - popW - 8)),
    y:
      rect.bottom + popH > window.innerHeight
        ? Math.max(8, rect.top - popH - 4)
        : rect.bottom + 4,
    error: "",
    saving: false,
  };
  nextTick(() => firstInput.value?.focus());
}

async function salva() {
  const a = adding.value;
  if (!a || a.saving) return;
  if (!a.inizio || !a.fine || a.fine <= a.inizio) {
    a.error = "La fine deve essere dopo l'inizio.";
    return;
  }
  a.saving = true;
  try {
    await store.addShift(a.dateKey, a.userId, a.inizio, a.fine);
    lastTimes.inizio = a.inizio;
    lastTimes.fine = a.fine;
    adding.value = null;
  } catch (e) {
    a.error = e instanceof Error ? e.message : "Errore nel salvataggio.";
    a.saving = false;
  }
}

async function rimuovi(id: number) {
  try {
    await store.removeShift(id);
  } catch (e) {
    notice.value = {
      kind: "err",
      text: e instanceof Error ? e.message : "Errore.",
    };
  }
}

async function copiaSettimanaPrecedente() {
  const prev = weekDates(offset.value - 1);
  copying.value = true;
  notice.value = null;
  try {
    const prevShifts = await api.get<Shift[]>(
      `/api/turni?dal=${toDateKey(prev[0])}&al=${toDateKey(prev[6])}`,
    );
    const stillHere = new Set(schedulable.value.map((u) => u.id));
    let skippedLeave = 0;
    const list: NewShift[] = [];
    for (const s of prevShifts) {
      if (!stillHere.has(s.userId)) continue;
      const d = fromDateKey(s.data);
      d.setDate(d.getDate() + 7);
      const data = toDateKey(d);
      // non copio turni su giorni in cui la persona è in permesso
      if (leaveAt(s.userId, data)?.fullDay) {
        skippedLeave++;
        continue;
      }
      list.push({
        userId: s.userId,
        data,
        inizio: hhmm(s.inizio),
        fine: hhmm(s.fine),
      });
    }
    if (!list.length) {
      notice.value = {
        kind: "err",
        text: "Nessun turno da copiare dalla settimana precedente.",
      };
      return;
    }
    const res = await store.addMany(list);
    const skipped = res.saltati + skippedLeave;
    notice.value = {
      kind: "ok",
      text:
        `Copiati ${res.creati.length} turni.` +
        (skipped
          ? ` ${skipped} saltati (già presenti, sovrapposti o in permesso).`
          : ""),
    };
  } catch (e) {
    notice.value = {
      kind: "err",
      text: e instanceof Error ? e.message : "Errore nella copia.",
    };
  } finally {
    copying.value = false;
  }
}

function range(): [string, string] {
  const dates = view.value === "week" ? days.value : currentMonthDates.value;
  return [toDateKey(dates[0]), toDateKey(dates[dates.length - 1])];
}

async function reload() {
  loading.value = true;
  try {
    const [dal, al] = range();
    await store.carica(dal, al);
  } finally {
    loading.value = false;
  }
}

watch([offset, view], reload);

onMounted(async () => {
  loading.value = true;
  try {
    [users.value, permessi.value] = await Promise.all([
      api.get<User[]>("/api/users"),
      api.get<Permesso[]>("/api/permessi").catch(() => [] as Permesso[]),
    ]);
  } finally {
    loading.value = false;
  }
  await reload();
});
</script>

<style scoped>
.plan-nav {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.week-label {
  font-size: 13px;
  color: var(--text-primary);
  min-width: 140px;
  text-align: center;
  text-transform: capitalize;
}

.btn-week,
.btn-today {
  background: var(--bg-tertiary);
  border: 0.5px solid var(--border-md);
  color: var(--text-primary);
  border-radius: var(--radius-md);
  padding: 4px 10px;
  cursor: pointer;
  font-size: 12px;
}
.btn-week:hover,
.btn-today:hover {
  opacity: 0.75;
}
.btn-week:disabled {
  opacity: 0.5;
  cursor: default;
}

.plan-input {
  background: var(--bg-secondary);
  border: 0.5px solid var(--border-md);
  color: var(--text-primary);
  border-radius: var(--radius-md);
  padding: 5px 10px;
  font-size: 12px;
  min-width: 0;
}
input.plan-input {
  width: 170px;
}

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
}
.view-toggle button.active {
  background: var(--purple);
  color: white;
}

.notice {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  margin-bottom: 10px;
}
.notice.ok {
  background: var(--green-light);
  color: var(--green-dark);
}
.notice.err {
  background: var(--coral-light);
  color: var(--coral-dark);
}
.notice-close {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  font-size: 14px;
}

.roster-wrap {
  overflow: auto;
  max-height: calc(100vh - 170px);
  border: 0.5px solid var(--border);
  border-radius: var(--radius-md);
}

.roster {
  width: 100%;
  min-width: 860px;
  border-collapse: separate;
  border-spacing: 0;
  table-layout: fixed;
  font-size: 12px;
}

.roster th,
.roster td {
  border-bottom: 0.5px solid var(--border);
  border-right: 0.5px solid var(--border);
}

.roster thead th {
  position: sticky;
  top: 0;
  z-index: 2;
  background: var(--bg-secondary);
  padding: 8px 6px;
  font-weight: 500;
  color: var(--text-secondary);
  text-align: left;
}
.roster thead th.today {
  color: var(--purple-dark);
}

.col-name {
  width: 170px;
  position: sticky;
  left: 0;
  z-index: 1;
  background: var(--bg-primary);
  padding: 6px 10px;
}
.roster thead th.col-name {
  z-index: 3;
  font-size: 11px;
  color: var(--text-tertiary);
}

.day-head {
  font-size: 12px;
  color: inherit;
}
.day-num {
  color: var(--text-primary);
  font-weight: 600;
  margin-left: 2px;
}
.coverage {
  font-size: 10px;
  color: var(--text-tertiary);
  margin-top: 2px;
  font-weight: 400;
}

.person-name {
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.person-hours {
  font-size: 10px;
  color: var(--text-tertiary);
  min-height: 12px;
}

.team-row td {
  background: var(--bg-tertiary);
  padding: 4px 10px;
}
.team-label {
  position: sticky;
  left: 10px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-secondary);
}

.cell {
  padding: 4px;
  vertical-align: top;
  cursor: pointer;
  height: 40px;
}
.cell:hover {
  background: var(--bg-secondary);
}
.cell.today {
  background: rgba(108, 99, 213, 0.06);
}
.cell.on-leave {
  cursor: default;
  background: repeating-linear-gradient(
    135deg,
    transparent 0 6px,
    var(--hover-strong) 6px 12px
  );
}

.cell-add {
  display: block;
  text-align: center;
  color: var(--text-tertiary);
  opacity: 0;
  line-height: 30px;
}
.cell:hover .cell-add {
  opacity: 1;
}

.chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2px;
  background: var(--purple-light);
  color: var(--purple-dark);
  border-radius: 5px;
  padding: 3px 4px 3px 6px;
  margin-bottom: 3px;
  cursor: default;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}
.chip-x {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  opacity: 0;
  font-size: 13px;
  line-height: 1;
  padding: 0 2px;
}
.chip:hover .chip-x {
  opacity: 0.8;
}

.leave-tag {
  font-size: 10px;
  color: var(--amber-dark);
  background: var(--amber-light);
  border-radius: 4px;
  padding: 2px 5px;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty {
  padding: 20px;
  text-align: center;
  color: var(--text-tertiary);
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 6px;
}
.month-header {
  font-size: 11px;
  color: var(--text-tertiary);
  text-align: center;
  padding-bottom: 4px;
}
.month-cell {
  min-height: 74px;
  border: 0.5px solid var(--border);
  border-radius: var(--radius-md);
  padding: 8px;
  cursor: pointer;
  background: var(--bg-secondary);
}
.month-cell.empty {
  background: transparent;
  border: none;
  cursor: default;
}
.month-cell:hover:not(.empty) {
  border-color: var(--purple);
}
.month-cell.today .month-day-num {
  color: var(--purple-dark);
}
.month-cell.cov-1 {
  background: var(--oggi);
}
.month-cell.cov-2 {
  background: var(--oggi-forte);
}
.month-cell.cov-3 {
  background: var(--purple-light);
}
.month-day-num {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}
.month-shift-count {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 6px;
}

.pop-backdrop {
  position: fixed;
  inset: 0;
  z-index: 50;
}
.pop {
  position: fixed;
  width: 240px;
  background: var(--bg-secondary);
  border: 0.5px solid var(--border-md);
  border-radius: var(--radius-md);
  padding: 12px;
  box-shadow: 0 8px 24px var(--shadow);
}
.pop-title {
  font-size: 12px;
  color: var(--text-primary);
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pop-times {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.pop-times label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 11px;
  color: var(--text-tertiary);
}
.pop-times input {
  background: var(--bg-tertiary);
  border: 0.5px solid var(--border-md);
  color: var(--text-primary);
  border-radius: 6px;
  padding: 5px 6px;
  font-size: 12px;
  color-scheme: dark;
}
.pop-error {
  font-size: 11px;
  color: var(--coral-dark);
  margin-top: 8px;
}
.pop-actions {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}
</style>
