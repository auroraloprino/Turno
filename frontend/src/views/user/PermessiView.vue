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

      <div v-if="form.tipo === 'Malattia'" class="fg">
        <label>Codice certificato INPS</label>
        <input type="text" v-model="form.codiceCertificato" />
      </div>

      <div v-if="form.tipo === 'Malattia'" class="fg">
        <label>Certificato medico</label>
        <div class="file-row">
          <button class="btn-file" @click="fileInput?.click()">
            Scegli file
          </button>
          <span class="file-name">{{
            form.certificato || "Nessun file selezionato"
          }}</span>
        </div>
        <input
          ref="fileInput"
          type="file"
          accept=".pdf,.jpg,.jpeg,.png"
          style="display: none"
          @change="onFile"
        />
      </div>

      <div v-if="form.tipo !== 'Malattia'" class="fg">
        <label>Note (opzionale)</label>
        <textarea v-model="form.note"></textarea>
      </div>

      <span v-if="errore" class="errore">{{ errore }}</span>
      <span v-if="inviata" class="ok">Richiesta inviata.</span>
      <button class="btn-send" :disabled="invio" @click="invia">
        {{ invio ? "Invio…" : "Invia richiesta" }}
      </button>
    </div>

    <div class="past-title">Richieste precedenti</div>
    <div v-if="richieste.length === 0" class="file-name">
      Nessuna richiesta.
    </div>
    <div v-for="p in richieste" :key="p.id" class="perm-row-past">
      <div>
        <span class="perm-tipo">{{ p.tipo }}</span>
        <span class="perm-d"
          >· {{ fmt(p.dal) }}{{ p.al !== p.dal ? " – " + fmt(p.al) : "" }}</span
        >
        <a
          v-if="p.certificatoUrl"
          class="cert-link"
          href="#"
          @click.prevent="apriCertificato(p.certificatoUrl)"
          >📎 Certificato</a
        >
      </div>
      <span class="pill" :class="statoClass(p.stato)">{{
        etichetta(p.stato)
      }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { api } from "@/api";

interface Permesso {
  id: number;
  tipo: string;
  dal: string;
  al: string;
  certificatoUrl: string | null;
  stato: string;
}

const vuoto = () => ({
  tipo: "Ferie",
  dal: "",
  al: "",
  note: "",
  certificato: "",
  codiceCertificato: "",
  durata: "Giornaliero",
  oraInizio: "",
  oraFine: "",
});
const form = ref(vuoto());
const richieste = ref<Permesso[]>([]);
const errore = ref("");
const inviata = ref(false);
const invio = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const file = ref<File | null>(null);

const oraMode = computed(
  () => form.value.tipo === "Permesso" && form.value.durata === "Orario",
);

async function caricaRichieste() {
  richieste.value = await api.get<Permesso[]>("/api/permessi/me");
}

function onFile(e: Event) {
  file.value = (e.target as HTMLInputElement).files?.[0] ?? null;
  form.value.certificato = file.value?.name ?? "";
}

async function invia() {
  errore.value = "";
  inviata.value = false;
  if (!form.value.dal) {
    errore.value = "Inserisci la data.";
    return;
  }
  if (!oraMode.value && !form.value.al) {
    errore.value = "Inserisci la data di fine.";
    return;
  }
  if (oraMode.value && (!form.value.oraInizio || !form.value.oraFine)) {
    errore.value = "Inserisci orario inizio e fine.";
    return;
  }
  invio.value = true;
  try {
    const malattia = form.value.tipo === "Malattia";
    // prima carico il file, poi allego al permesso l'indirizzo che mi restituisce permessi-service
    const certificatoUrl =
      malattia && file.value
        ? (
            await api.upload<{ certificatoUrl: string }>(
              "/api/permessi/certificato",
              file.value,
            )
          ).certificatoUrl
        : null;
    const note = malattia
      ? form.value.codiceCertificato
        ? `Codice certificato INPS ${form.value.codiceCertificato}`
        : null
      : form.value.note || null;
    await api.post("/api/permessi", {
      tipo: form.value.tipo,
      dal: form.value.dal,
      al: oraMode.value ? form.value.dal : form.value.al,
      oraInizio: oraMode.value ? form.value.oraInizio : null,
      oraFine: oraMode.value ? form.value.oraFine : null,
      note,
      certificatoUrl,
    });
    form.value = vuoto();
    file.value = null;
    if (fileInput.value) fileInput.value.value = "";
    inviata.value = true;
    await caricaRichieste();
  } catch (e) {
    errore.value = e instanceof Error ? e.message : "Invio non riuscito.";
  } finally {
    invio.value = false;
  }
}

async function apriCertificato(url: string) {
  window.open(URL.createObjectURL(await api.blob(url)), "_blank");
}

function fmt(d: string) {
  if (!d) return "";
  const [y, m, g] = d.split("-");
  return `${g}/${m}/${y}`;
}

function etichetta(stato: string) {
  if (stato === "APPROVATO") return "approvata";
  if (stato === "RIFIUTATO") return "rifiutata";
  return "in attesa";
}

function statoClass(stato: string) {
  if (stato === "APPROVATO") return "p-ok";
  if (stato === "RIFIUTATO") return "p-no";
  return "p-pend";
}

onMounted(caricaRichieste);
</script>

<style scoped>
.file-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-file {
  padding: 6px 14px;
  font-size: 12px;
  background: var(--bg-tertiary);
  border: 0.5px solid var(--border-md);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.12s;
}
.btn-file:hover {
  opacity: 0.7;
}

.file-name {
  font-size: 11px;
  color: var(--text-secondary);
}

.errore {
  font-size: 12px;
  color: var(--coral-dark);
}

.ok {
  font-size: 12px;
  color: var(--teal-dark, #0f6e56);
}

.cert-link {
  margin-left: 8px;
  font-size: 11px;
}
</style>
