<template>
  <div class="card">
    <div class="card-title">Nuova richiesta permesso</div>
    <div class="perm-form">
      <div class="form-row">
        <div class="fg">
          <label>Tipo di richiesta</label>
          <select v-model="form.tipo">
            <option>Ferie</option>
            <option>Permesso</option>
            <option>Malattia</option>
            <option>Altro</option>
          </select>
        </div>
        <div v-if="form.tipo === 'Permesso'" class="fg">
          <label>Durata</label>
          <select v-model="form.durata">
            <option>Giornaliero</option>
            <option>Orario</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="fg">
          <label>{{ oraMode ? "Giorno" : "Dal" }}</label>
          <input type="date" v-model="form.dal" />
        </div>
        <template v-if="!oraMode">
          <div class="fg">
            <label>Al</label>
            <input type="date" v-model="form.al" />
          </div>
        </template>
        <template v-if="oraMode">
          <div class="fg">
            <label>Ora inizio</label>
            <input type="time" v-model="form.oraInizio" />
          </div>
          <div class="fg">
            <label>Ora fine</label>
            <input type="time" v-model="form.oraFine" />
          </div>
        </template>
      </div>
      <div class="fg">
        <label>Note (opzionale)</label>
        <textarea v-model="form.note"></textarea>
      </div>
      <span v-if="error" class="errore">{{ error }}</span>
      <button class="btn-send" :disabled="loading" @click="invia">
        {{ loading ? "Invio…" : "Invia richiesta" }}
      </button>
    </div>

    <div class="past-title">Richieste precedenti</div>
    <div v-if="loadingList" class="empty-state">Caricamento…</div>
    <div v-else-if="richieste.length === 0" class="empty-state">
      Nessuna richiesta.
    </div>

    <template v-for="p in richieste" :key="p.id">
      <!-- edit mode -->
      <div v-if="editingId === p.id" class="perm-row-past perm-edit">
        <div style="flex: 1; display: flex; flex-direction: column; gap: 6px">
          <div class="form-row">
            <div class="fg">
              <label>Tipo</label>
              <select v-model="editForm.tipo">
                <option>Ferie</option>
                <option>Permesso</option>
                <option>Malattia</option>
                <option>Altro</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="fg">
              <label>Dal</label><input type="date" v-model="editForm.dal" />
            </div>
            <div class="fg">
              <label>Al</label><input type="date" v-model="editForm.al" />
            </div>
          </div>
          <div class="fg">
            <label>Note</label><textarea v-model="editForm.note"></textarea>
          </div>
          <div style="display: flex; gap: 6px; margin-top: 4px">
            <button class="btn-sm btn-ok" @click="salvaEdit(p.id)">
              Salva
            </button>
            <button class="btn-sm" @click="editingId = null">Annulla</button>
          </div>
        </div>
      </div>

      <!-- normal row -->
      <div v-else class="perm-row-past">
        <div style="flex: 1">
          <span class="perm-tipo">{{ p.tipo }}</span>
          <span class="perm-d"
            >· {{ fmt(p.dal)
            }}{{ p.al !== p.dal ? " – " + fmt(p.al) : "" }}</span
          >
          <div v-if="p.note" class="perm-note">{{ p.note }}</div>
        </div>
        <div style="display: flex; align-items: center; gap: 8px">
          <button
            v-if="p.stato === 'IN_ATTESA'"
            class="btn-sm"
            @click="startEdit(p)"
          >
            Modifica
          </button>
          <span class="pill" :class="statoClass(p.stato)">{{ p.stato }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { api } from "@/api";

interface PermessoResponse {
  id: number;
  tipo: string;
  dal: string;
  al: string;
  oraInizio: string | null;
  oraFine: string | null;
  note: string | null;
  certificatoUrl: string | null;
  stato: string;
}

const form = ref({
  tipo: "Ferie",
  dal: "",
  al: "",
  note: "",
  durata: "Giornaliero",
  oraInizio: "",
  oraFine: "",
});
const richieste = ref<PermessoResponse[]>([]);
const error = ref("");
const loading = ref(false);
const loadingList = ref(false);

const editingId = ref<number | null>(null);
const editForm = ref({ tipo: "", dal: "", al: "", note: "" });

function startEdit(p: PermessoResponse) {
  editingId.value = p.id;
  editForm.value = { tipo: p.tipo, dal: p.dal, al: p.al, note: p.note ?? "" };
}

async function salvaEdit(id: number) {
  const body = {
    tipo: editForm.value.tipo,
    dal: editForm.value.dal,
    al: editForm.value.al,
    oraInizio: null,
    oraFine: null,
    note: editForm.value.note || null,
    certificatoUrl: null,
  };
  const updated = await api.patch<PermessoResponse>(
    `/api/permessi/${id}`,
    body,
  );
  const idx = richieste.value.findIndex((p) => p.id === id);
  if (idx !== -1) richieste.value[idx] = updated;
  editingId.value = null;
}

const oraMode = computed(
  () => form.value.tipo === "Permesso" && form.value.durata === "Orario",
);

async function fetchMiei() {
  loadingList.value = true;
  try {
    richieste.value = await api.get<PermessoResponse[]>("/api/permessi/me");
  } finally {
    loadingList.value = false;
  }
}

async function invia() {
  error.value = "";
  if (!form.value.dal) {
    error.value = "Inserisci la data.";
    return;
  }
  if (!oraMode.value && !form.value.al) {
    error.value = "Inserisci la data di fine.";
    return;
  }
  if (oraMode.value && (!form.value.oraInizio || !form.value.oraFine)) {
    error.value = "Inserisci orario inizio e fine.";
    return;
  }
  loading.value = true;
  try {
    const body = {
      tipo: form.value.tipo,
      dal: form.value.dal,
      al: oraMode.value ? form.value.dal : form.value.al,
      oraInizio: oraMode.value ? form.value.oraInizio : null,
      oraFine: oraMode.value ? form.value.oraFine : null,
      note: form.value.note || null,
      certificatoUrl: null,
    };
    const created = await api.post<PermessoResponse>("/api/permessi", body);
    richieste.value.unshift(created);
    form.value = {
      tipo: "Ferie",
      dal: "",
      al: "",
      note: "",
      durata: "Giornaliero",
      oraInizio: "",
      oraFine: "",
    };
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : "Errore durante l'invio.";
  } finally {
    loading.value = false;
  }
}

function fmt(d: string) {
  if (!d) return "";
  const [y, m, g] = d.split("-");
  return `${g}/${m}/${y}`;
}

function statoClass(stato: string) {
  if (stato === "APPROVATO") return "p-ok";
  if (stato === "RIFIUTATO") return "p-no";
  return "p-pend";
}

onMounted(fetchMiei);
</script>

<style scoped>
.empty-state {
  font-size: 13px;
  color: var(--text-tertiary);
  padding: 8px 0;
}
.errore {
  font-size: 12px;
  color: var(--coral-dark);
}
.perm-note {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}
.perm-edit {
  flex-direction: column;
  align-items: stretch;
}
</style>
