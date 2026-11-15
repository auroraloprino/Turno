<template>
  <div class="dash-grid">
    <RouterLink
      v-for="t in riquadri"
      :key="t.titolo"
      :to="t.to"
      class="dash-tile"
      :class="[t.colore, { evidenza: t.evidenza }]"
    >
      <div class="dash-top">
        <span class="dash-icon" v-html="t.icona" />
        <span class="dash-arrow">→</span>
      </div>
      <div class="dash-title">{{ t.titolo }}</div>
      <div class="dash-num">
        <span v-if="caricato">{{ t.valore }}</span>
        <span v-else class="dash-skeleton" />
      </div>
      <div class="dash-sub">{{ t.sottotitolo }}</div>
    </RouterLink>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import { api } from "@/api";
import { toDateKey, weekDates } from "@/stores/planner";

const route = useRoute();
// la stessa dashboard serve owner e admin, cambia solo il prefisso delle pagine
const base = computed(() => (route.path.startsWith("/owner") ? "/owner" : "/admin"));

const utenti = ref(0);
const timbratureOggi = ref(0);
const permessiAttesa = ref(0);
const turniSettimana = ref(0);
const caricato = ref(false);

const ICONE = {
  utenti: `<svg viewBox="0 0 16 16" fill="none"><circle cx="6" cy="5" r="3" stroke="currentColor" stroke-width="1.2"/><path d="M1 14c0-3 2-5 5-5s5 2 5 5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/><path d="M11 3.5a2.5 2.5 0 0 1 0 4.5M13 13.5c0-2-.8-3.4-2-4.2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>`,
  timbrature: `<svg viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.2"/><path d="M8 5v3l2 2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>`,
  permessi: `<svg viewBox="0 0 16 16" fill="none"><rect x="2" y="2" width="12" height="12" rx="1.5" stroke="currentColor" stroke-width="1.2"/><path d="M5 8h6M8 5v6" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>`,
  turni: `<svg viewBox="0 0 16 16" fill="none"><rect x="2" y="3" width="12" height="11" rx="1.5" stroke="currentColor" stroke-width="1.2"/><path d="M2 6.5h12M5.5 1.5v3M10.5 1.5v3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>`,
};

const riquadri = computed(() => [
  {
    titolo: "Utenti",
    to: base.value === "/owner" ? "/owner/utenti" : "/admin/membri",
    valore: utenti.value,
    sottotitolo: "persone registrate",
    icona: ICONE.utenti,
    colore: "c-purple",
    evidenza: false,
  },
  {
    titolo: "Timbrature",
    to: `${base.value}/timbrature`,
    valore: timbratureOggi.value,
    sottotitolo: "registrate oggi",
    icona: ICONE.timbrature,
    colore: "c-teal",
    evidenza: false,
  },
  {
    titolo: "Permessi",
    to: `${base.value}/permessi`,
    valore: permessiAttesa.value,
    sottotitolo: permessiAttesa.value ? "da approvare" : "nessuna richiesta in attesa",
    icona: ICONE.permessi,
    colore: "c-amber",
    evidenza: permessiAttesa.value > 0,
  },
  {
    titolo: "Turni",
    to: `${base.value}/planner`,
    valore: turniSettimana.value,
    sottotitolo: "in programma questa settimana",
    icona: ICONE.turni,
    colore: "c-coral",
    evidenza: false,
  },
]);

let timer: ReturnType<typeof setInterval>;

async function carica() {
  const oggi = toDateKey(new Date());
  const settimana = weekDates(0);
  const [u, t, p, turni] = await Promise.all([
    api.get<unknown[]>("/api/users"),
    api.get<{ timestamp: string }[]>("/api/timbrature"),
    api.get<unknown[]>("/api/permessi/in-attesa"),
    api.get<unknown[]>(`/api/turni?dal=${toDateKey(settimana[0])}&al=${toDateKey(settimana[6])}`),
  ]);
  utenti.value = u.length;
  timbratureOggi.value = t.filter((x) => x.timestamp.startsWith(oggi)).length;
  permessiAttesa.value = p.length;
  turniSettimana.value = turni.length;
  caricato.value = true;
}

onMounted(() => {
  carica();
  // i numeri si aggiornano da soli, utile se la dashboard resta aperta
  timer = setInterval(carica, 30000);
});
onUnmounted(() => clearInterval(timer));
</script>

<style scoped>
.dash-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  max-width: 1100px;
}

.dash-tile {
  --accento: var(--purple-dark);
  --fondo: var(--purple-light);
  display: flex;
  flex-direction: column;
  min-height: 190px;
  padding: 22px 24px;
  background: var(--bg-secondary);
  border: 0.5px solid var(--border);
  border-radius: var(--radius-lg);
  color: var(--text-primary);
  text-decoration: none;
  transition: transform 0.12s, border-color 0.12s, background 0.12s;
}
.dash-tile:hover {
  transform: translateY(-2px);
  border-color: var(--accento);
  background: var(--bg-tertiary);
}
.dash-tile:focus-visible {
  outline: 2px solid var(--accento);
  outline-offset: 2px;
}
.dash-tile.evidenza {
  border-color: var(--accento);
}

.c-purple {
  --accento: var(--purple-dark);
  --fondo: var(--purple-light);
}
.c-teal {
  --accento: var(--teal-dark);
  --fondo: var(--teal-light);
}
.c-amber {
  --accento: var(--amber-dark);
  --fondo: var(--amber-light);
}
.c-coral {
  --accento: var(--coral-dark);
  --fondo: var(--coral-light);
}

.dash-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.dash-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--fondo);
  color: var(--accento);
}
.dash-icon :deep(svg) {
  width: 22px;
  height: 22px;
}
.dash-arrow {
  font-size: 20px;
  color: var(--text-tertiary);
  transition: transform 0.12s, color 0.12s;
}
.dash-tile:hover .dash-arrow {
  transform: translateX(4px);
  color: var(--accento);
}

.dash-title {
  margin-top: 18px;
  font-size: 15px;
  font-weight: 600;
}
.dash-num {
  margin-top: auto;
  padding-top: 10px;
  font-size: 38px;
  font-weight: 700;
  line-height: 1;
  color: var(--accento);
  font-variant-numeric: tabular-nums;
}
.dash-skeleton {
  display: inline-block;
  width: 56px;
  height: 34px;
  border-radius: 6px;
  background: var(--bg-tertiary);
}
.dash-sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 700px) {
  .dash-grid {
    grid-template-columns: minmax(0, 1fr);
  }
  .dash-tile {
    min-height: 150px;
  }
}
</style>
