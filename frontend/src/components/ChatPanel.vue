<template>
  <div class="chat" :class="{ 'has-active': active }">
    <aside class="side">
      <div class="side-search">
        <input
          ref="searchRef"
          v-model="ricerca"
          type="search"
          placeholder="Cerca o avvia una conversazione"
          @keydown.esc="ricerca = ''"
        />
      </div>

      <div class="side-scroll">
        <div class="side-section">
          Gruppi
          <button class="side-add" title="Nuovo gruppo" @click="iniziaGruppo">
            +
          </button>
        </div>

        <div v-if="creandoGruppo" class="new-group">
          <input
            ref="nomeGruppoRef"
            v-model="nomeGruppo"
            maxlength="60"
            placeholder="Nome del gruppo"
            @keydown.esc="annullaGruppo"
          />
          <input
            v-model="cercaMembri"
            type="search"
            placeholder="Cerca persone da aggiungere"
          />
          <div class="pick-list">
            <label v-for="u in candidatiGruppo" :key="u.id" class="pick">
              <input v-model="membriGruppo" type="checkbox" :value="u.id" />
              <span class="av xs" :style="avatarStyle(u.id)">
                {{ initials(u.name) }}
              </span>
              <span class="conv-name">{{ u.name }}</span>
            </label>
            <div v-if="!candidatiGruppo.length" class="side-empty">
              Nessuna persona trovata.
            </div>
          </div>
          <div v-if="erroreGruppo" class="send-error">{{ erroreGruppo }}</div>
          <div class="new-group-actions">
            <span class="pick-count">{{ membriGruppo.length }} selezionati</span>
            <button class="btn-ghost" @click="annullaGruppo">Annulla</button>
            <button
              class="btn-primary"
              :disabled="!nomeGruppo.trim() || !membriGruppo.length"
              @click="confermaGruppo"
            >
              Crea
            </button>
          </div>
        </div>

        <template v-if="gruppi.length">
          <div
            v-for="c in gruppi"
            :key="c.id"
            class="conv"
            :class="{ sel: activeId === c.id, unread: c.nonLetti > 0 }"
            @click="open(c.id)"
          >
            <span class="conv-hash">#</span>
            <span class="conv-name">{{ c.nome }}</span>
            <span v-if="c.nonLetti" class="non-letti">{{ c.nonLetti }}</span>
            <button
              class="conv-close"
              title="Nascondi"
              @click.stop="chiudiConv(c.id)"
            >
              ×
            </button>
          </div>
        </template>
        <div v-else-if="!creandoGruppo && !ricerca" class="side-empty">
          Nessun gruppo. Crealo con +
        </div>

        <div class="side-section">
          Messaggi diretti
          <button
            class="side-add"
            title="Nuovo messaggio"
            @click="searchRef?.focus()"
          >
            +
          </button>
        </div>
        <div
          v-for="c in diretti"
          :key="c.id"
          class="conv"
          :class="{ sel: activeId === c.id, unread: c.nonLetti > 0 }"
          @click="open(c.id)"
        >
          <span class="av sm" :style="avatarStyle(otherId(c))">
            {{ initials(titolo(c)) }}
          </span>
          <span class="conv-name">{{ titolo(c) }}</span>
          <span v-if="c.nonLetti" class="non-letti">{{ c.nonLetti }}</span>
          <button
            class="conv-close"
            title="Nascondi"
            @click.stop="chiudiConv(c.id)"
          >
            ×
          </button>
        </div>
        <div v-if="!diretti.length && !ricerca" class="side-empty">
          Nessun messaggio diretto. Cerca una persona qui sopra.
        </div>

        <template v-if="ricerca && personeTrovate.length">
          <div class="side-section">Persone</div>
          <div
            v-for="u in personeTrovate"
            :key="u.id"
            class="conv"
            @click="avviaPrivata(u)"
          >
            <span class="av sm" :style="avatarStyle(u.id)">
              {{ initials(u.name) }}
            </span>
            <span class="conv-name">{{ u.name }}</span>
          </div>
        </template>
        <div
          v-if="
            ricerca &&
            !gruppi.length &&
            !diretti.length &&
            !personeTrovate.length
          "
          class="side-empty"
        >
          Nessun risultato.
        </div>
      </div>
    </aside>

    <section
      class="pannello"
      :class="{ dragging: trascinando }"
      @dragover.prevent="trascinando = !!active"
      @dragleave.self="trascinando = false"
      @drop.prevent="onDrop"
    >
      <template v-if="active">
        <header class="head">
          <button class="back" title="Indietro" @click="chiudiAttiva">‹</button>
          <span v-if="active.tipo === 'group'" class="head-hash">#</span>
          <span v-else class="av sm" :style="avatarStyle(otherId(active))">
            {{ initials(titolo(active)) }}
          </span>
          <span class="head-name">{{ titolo(active) }}</span>
          <span v-if="active.tipo === 'group'" class="head-sub">
            {{ active.partecipanti.length }} membri
          </span>
          <div class="head-tools">
            <div class="zoom" title="Zoom dei messaggi (anche Ctrl + rotella)">
              <button :disabled="zoom <= ZOOM_MIN" @click="cambiaZoom(-1)">−</button>
              <button class="zoom-val" @click="zoom = 1">
                {{ Math.round(zoom * 100) }}%
              </button>
              <button :disabled="zoom >= ZOOM_MAX" @click="cambiaZoom(1)">+</button>
            </div>
            <button
              v-if="active.tipo === 'group'"
              class="head-btn"
              :class="{ on: showMembers }"
              title="Mostra membri"
              @click="showMembers = !showMembers"
            >
              Membri
            </button>
          </div>
        </header>

        <div class="body">
          <div class="colonna">
          <div
            ref="scroller"
            class="scroller"
            :style="{ zoom }"
            @scroll="onScroll"
            @wheel="onWheel"
          >
            <div class="intro">
              <div
                class="intro-icon"
                :style="
                  active.tipo === 'group' ? {} : avatarStyle(otherId(active))
                "
              >
                {{ active.tipo === "group" ? "#" : initials(titolo(active)) }}
              </div>
              <div class="intro-title">
                {{
                  active.tipo === "group"
                    ? `Benvenuto in #${titolo(active)}`
                    : titolo(active)
                }}
              </div>
              <div class="intro-sub">
                {{
                  active.tipo === "group"
                    ? "Questo è l'inizio del gruppo."
                    : `Questo è l'inizio della tua conversazione con ${titolo(active)}.`
                }}
              </div>
            </div>

            <template v-for="row in rows" :key="row.key">
              <div v-if="row.kind === 'day'" class="day-divider">
                <span>{{ row.label }}</span>
              </div>

              <div
                v-else
                class="riga"
                :class="{ primo: row.head, editing: editingId === row.msg.id }"
              >
                <div class="gutter">
                  <span
                    v-if="row.head"
                    class="av"
                    :style="avatarStyle(row.msg.senderId)"
                  >
                    {{ initials(nomeDi(row.msg)) }}
                  </span>
                  <span v-else class="gutter-time">{{
                    fmtTime(row.msg.timestamp)
                  }}</span>
                </div>

                <div class="corpo">
                  <div v-if="row.head" class="meta">
                    <span
                      class="author"
                      :style="{ color: colorOf(row.msg.senderId) }"
                    >
                      {{ nomeDi(row.msg) }}
                    </span>
                    <span class="stamp">{{ fmtStamp(row.msg.timestamp) }}</span>
                  </div>

                  <template v-if="editingId === row.msg.id">
                    <textarea
                      ref="editRef"
                      v-model="editDraft"
                      class="edit-box"
                      rows="1"
                      @input="autoResize"
                      @keydown.enter.exact.prevent="saveEdit(row.msg.id)"
                      @keydown.esc="cancelEdit"
                    />
                    <div class="edit-hint">
                      esc per <a @click="cancelEdit">annullare</a> • invio per
                      <a @click="saveEdit(row.msg.id)">salvare</a>
                    </div>
                  </template>
                  <template v-else>
                    <div v-if="row.msg.testo" class="text">
                      <span class="md" v-html="renderMd(row.msg.testo)" />
                      <span v-if="row.msg.modificato" class="edited"
                        >(modificato)</span
                      >
                    </div>
                    <div
                      v-if="row.msg.immagineUrl && !srcImmagine(row.msg.immagineUrl)"
                      class="img img-attesa"
                    />
                    <img
                      v-else-if="row.msg.immagineUrl"
                      class="img"
                      :src="srcImmagine(row.msg.immagineUrl)"
                      alt="immagine"
                      @load="scrollBottom()"
                      @click="apriImmagine(row.msg.immagineUrl)"
                    />
                  </template>
                </div>

                <div
                  v-if="row.msg.senderId === myId && editingId !== row.msg.id"
                  class="actions"
                >
                  <button title="Modifica" @click="startEdit(row.msg)">
                    ✎
                  </button>
                  <button
                    title="Elimina"
                    class="danger"
                    @click="deleteMsg(row.msg.id)"
                  >
                    🗑
                  </button>
                </div>
              </div>
            </template>
          </div>

          <footer class="composer">
            <div v-if="sendError" class="send-error">{{ sendError }}</div>
            <div v-if="allegato" class="pending">
              <img :src="anteprima" alt="anteprima" />
              <span class="pending-name">{{ allegato.name }}</span>
              <button title="Togli l'immagine" @click="rimuoviAllegato">×</button>
            </div>
            <div class="composer-box">
              <button
                class="attach"
                title="Allega un'immagine (anche incolla o trascina)"
                @click="fileRef?.click()"
              >
                +
              </button>
              <textarea
                ref="inputRef"
                v-model="draft"
                rows="1"
                :placeholder="
                  active.tipo === 'group'
                    ? `Scrivi in #${titolo(active)}`
                    : `Scrivi a @${titolo(active)}`
                "
                @input="autoResize"
                @paste="onPaste"
                @keydown.enter.exact.prevent="send"
                @keydown.up="editLastIfEmpty"
              />
              <input
                ref="fileRef"
                type="file"
                accept="image/png,image/jpeg,image/gif,image/webp"
                hidden
                @change="onFileScelto"
              />
            </div>
          </footer>
  
          </div>

          <aside v-if="active.tipo === 'group' && showMembers" class="members">
            <div class="side-section">
              Membri — {{ active.partecipanti.length }}
            </div>
            <div v-for="id in active.partecipanti" :key="id" class="member">
              <span class="av sm" :style="avatarStyle(id)">
                {{ initials(nomeUtente(id)) }}
              </span>
              <span class="member-name">{{ nomeUtente(id) }}</span>
            </div>
          </aside>
        </div>
      </template>

      <div v-else class="placeholder">
        <div class="placeholder-icon">💬</div>
        <div>Seleziona una conversazione o cerca una persona per iniziare.</div>
      </div>
    </section>

    <Teleport to="body">
      <div
        v-if="lightbox"
        class="lightbox"
        @click.self="chiudiLightbox"
        @wheel.prevent="(e: WheelEvent) => lbZoom(e.deltaY < 0 ? 1 : -1)"
      >
        <div class="lb-tools">
          <button title="Riduci" @click="lbZoom(-1)">−</button>
          <button class="lb-val" title="Adatta allo schermo" @click="adattaLightbox">
            {{ Math.round(lbScala * 100) }}%
          </button>
          <button title="Ingrandisci" @click="lbZoom(1)">+</button>
          <button title="Apri in una nuova scheda" @click="apriInScheda">↗</button>
          <button title="Chiudi (Esc)" @click="chiudiLightbox">×</button>
        </div>
        <img
          :src="lightbox"
          alt="immagine"
          draggable="false"
          :style="{ width: lbNat.w * lbScala + 'px' }"
          @load="onLbLoad"
        />
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted, onUnmounted } from "vue";
import { marked } from "marked";
import DOMPurify from "dompurify";
import { useChatStore } from "@/stores/chat";
import { useAuthStore } from "@/stores/auth";
import type { Conversazione, Messaggio } from "@/stores/chat";
import { api } from "@/api";

interface Utente {
  id: number;
  name: string;
  email: string;
}

type Row =
  | { kind: "day"; key: string; label: string }
  | { kind: "msg"; key: string; msg: Messaggio; head: boolean };

// messaggi consecutivi della stessa persona entro 7 minuti vengono raggruppati (avatar e nome solo sul primo)
const GROUP_WINDOW_MS = 7 * 60 * 1000;

const store = useChatStore();
const auth = useAuthStore();
const myId = computed(() => auth.user?.id);

const activeId = ref<number | null>(null);
const active = computed(
  () => store.conversazioni.find((c) => c.id === activeId.value) ?? null,
);

const utenti = ref<Utente[]>([]);
const utentiById = computed(() => new Map(utenti.value.map((u) => [u.id, u])));

const ricerca = ref("");
const draft = ref("");
const sendError = ref("");
const showMembers = ref(true);
const editingId = ref<number | null>(null);
const editDraft = ref("");

const creandoGruppo = ref(false);
const nomeGruppo = ref("");
const cercaMembri = ref("");
const membriGruppo = ref<number[]>([]);
const erroreGruppo = ref("");
const nomeGruppoRef = ref<HTMLInputElement | null>(null);

const allegato = ref<File | null>(null);
const anteprima = ref("");
const trascinando = ref(false);
const fileRef = ref<HTMLInputElement | null>(null);
// url del backend -> url locale del blob, così ogni immagine si scarica una volta sola
const immagini = ref<Record<string, string>>({});
const inCaricamento = new Set<string>();

const ZOOM_MIN = 0.7;
const ZOOM_MAX = 1.8;
const ZOOM_PASSO = 0.1;
const zoom = ref(leggiZoom());

function leggiZoom() {
  try {
    const v = Number(localStorage.getItem("chat_zoom"));
    return v >= ZOOM_MIN && v <= ZOOM_MAX ? v : 1;
  } catch {
    return 1;
  }
}

function cambiaZoom(direzione: number) {
  // arrotondo per non accumulare errori tipo 1.2000000000000002
  const nuovo = Math.round((zoom.value + direzione * ZOOM_PASSO) * 10) / 10;
  zoom.value = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, nuovo));
}

// Ctrl + rotella sopra i messaggi ingrandisce solo la chat invece di tutta la pagina
function onWheel(e: WheelEvent) {
  if (!e.ctrlKey) return;
  e.preventDefault();
  cambiaZoom(e.deltaY < 0 ? 1 : -1);
}

watch(zoom, (v) => {
  try {
    localStorage.setItem("chat_zoom", String(v));
  } catch {
    // senza localStorage lo zoom vale solo per singola sessione
  }
  scrollBottom();
});

const searchRef = ref<HTMLInputElement | null>(null);
const inputRef = ref<HTMLTextAreaElement | null>(null);
const editRef = ref<HTMLTextAreaElement[] | null>(null);
const scroller = ref<HTMLElement | null>(null);
let stickToBottom = true;

function nomeUtente(id: number) {
  if (id === myId.value) return auth.user?.name ?? "Tu";
  return utentiById.value.get(id)?.name ?? `Utente ${id}`;
}

function nomeDi(m: Messaggio) {
  return utentiById.value.get(m.senderId)?.name ?? m.senderName ?? "Utente";
}

function otherId(c: Conversazione) {
  return c.partecipanti.find((id) => id !== myId.value) ?? c.partecipanti[0];
}

// nelle chat private il nome salvato è quello visto da chi l'ha creata, quindi mostro sempre il nome dell'altro
function titolo(c: Conversazione) {
  if (c.tipo === "group") return c.nome;
  const other = otherId(c);
  return utentiById.value.get(other)?.name ?? c.nome;
}

function initials(name: string) {
  return name
    .split(" ")
    .filter(Boolean)
    .map((w) => w[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
}

const PALETTE = [
  "#6C63D5",
  "#2f9e8f",
  "#d9822b",
  "#c2577a",
  "#3d8bd9",
  "#7a9e3d",
];
const colorOf = (id: number) => PALETTE[Math.abs(id) % PALETTE.length];
const avatarStyle = (id: number) => ({ background: colorOf(id) });

function matches(text: string) {
  const q = ricerca.value.trim().toLowerCase();
  return !q || text.toLowerCase().includes(q);
}

const gruppi = computed(() =>
  store.conversazioni.filter((c) => c.tipo === "group" && matches(c.nome)),
);
const diretti = computed(() =>
  store.conversazioni.filter((c) => c.tipo !== "group" && matches(titolo(c))),
);

const personeTrovate = computed(() => {
  const q = ricerca.value.trim().toLowerCase();
  if (!q) return [];
  const withDm = new Set(
    store.conversazioni.filter((c) => c.tipo !== "group").map(otherId),
  );
  return utenti.value
    .filter((u) => u.id !== myId.value && !withDm.has(u.id))
    .filter(
      (u) =>
        u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q),
    )
    .slice(0, 8);
});

function dayLabel(d: Date) {
  const today = new Date();
  const yesterday = new Date();
  yesterday.setDate(today.getDate() - 1);
  if (d.toDateString() === today.toDateString()) return "Oggi";
  if (d.toDateString() === yesterday.toDateString()) return "Ieri";
  return d.toLocaleDateString("it-IT", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

const rows = computed<Row[]>(() => {
  const out: Row[] = [];
  let prev: Messaggio | null = null;
  for (const m of active.value?.messaggi ?? []) {
    if (m.eliminato) continue;
    const d = new Date(m.timestamp);
    const newDay =
      !prev || new Date(prev.timestamp).toDateString() !== d.toDateString();
    if (newDay) out.push({ kind: "day", key: `d${m.id}`, label: dayLabel(d) });
    const head =
      newDay ||
      !prev ||
      prev.senderId !== m.senderId ||
      d.getTime() - new Date(prev.timestamp).getTime() > GROUP_WINDOW_MS;
    out.push({ kind: "msg", key: `m${m.id}`, msg: m, head });
    prev = m;
  }
  return out;
});

function fmtTime(ts: string) {
  return new Date(ts).toLocaleTimeString("it-IT", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function fmtStamp(ts: string) {
  const d = new Date(ts);
  const label = dayLabel(d);
  const time = fmtTime(ts);
  if (label === "Oggi" || label === "Ieri") return `${label} alle ${time}`;
  return `${d.toLocaleDateString("it-IT")} ${time}`;
}

marked.setOptions({ breaks: true });

function renderMd(text: string): string {
  let t = text.replace(/^-#\s+(.+)$/gm, "<small>$1</small>");
  t = t.replace(/__(.+?)__/g, "<u>$1</u>");
  // i messaggi arrivano da altri utenti, quindi vanno sempre sanificati prima di finire nel v-html
  return DOMPurify.sanitize(marked.parse(t) as string, {
    ALLOWED_TAGS: [
      "p",
      "br",
      "h1",
      "h2",
      "h3",
      "strong",
      "em",
      "u",
      "del",
      "small",
      "code",
      "pre",
      "a",
      "ul",
      "ol",
      "li",
      "blockquote",
    ],
    ALLOWED_ATTR: ["href"],
  });
}

// gli hook di DOMPurify sono globali e questo codice gira a ogni mount, quindi prima li tolgo e poi li riaggiungo
DOMPurify.removeHooks("afterSanitizeAttributes");
DOMPurify.addHook("afterSanitizeAttributes", (node) => {
  if (node.tagName === "A") {
    node.setAttribute("target", "_blank");
    node.setAttribute("rel", "noopener noreferrer");
  }
});

function onScroll() {
  const el = scroller.value;
  if (!el) return;
  stickToBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 80;
}

function scrollBottom(force = false) {
  nextTick(() => {
    const el = scroller.value;
    if (el && (force || stickToBottom)) el.scrollTop = el.scrollHeight;
  });
}

watch(
  () => active.value?.messaggi.length,
  (n, old) => {
    const last = active.value?.messaggi.at(-1);
    // i propri messaggi si seguono sempre, quelli degli altri solo se si è già in fondo
    scrollBottom(
      !!last && last.senderId === myId.value && (n ?? 0) > (old ?? 0),
    );
  },
);

function autoResize(e: Event) {
  const el = e.target as HTMLTextAreaElement;
  el.style.height = "auto";
  el.style.height = Math.min(el.scrollHeight, 200) + "px";
}

async function open(id: number) {
  if (activeId.value === id) return;
  activeId.value = id;
  cancelEdit();
  sendError.value = "";
  ricerca.value = "";
  stickToBottom = true;
  await store.apri(id);
  scrollBottom(true);
  nextTick(() => inputRef.value?.focus());
}

function chiudiAttiva() {
  activeId.value = null;
  store.setActive(null);
}

function chiudiConv(id: number) {
  if (activeId.value === id) chiudiAttiva();
  store.rimuovi(id);
}

const inviando = ref(false);

async function send() {
  const testo = draft.value.trim();
  const file = allegato.value;
  // finché un invio è in corso gli Invio successivi non fanno niente, sennò l'immagine partiva più volte
  if (inviando.value || (!testo && !file) || !active.value) return;
  inviando.value = true;
  sendError.value = "";
  draft.value = "";
  if (inputRef.value) inputRef.value.style.height = "auto";
  // tolgo subito l'allegato dal composer, così non resta lì pronto a essere rimandato
  allegato.value = null;
  try {
    // con un'immagine in attesa il testo diventa la sua didascalia
    if (file) await store.inviaImmagine(active.value.id, file, testo);
    else await store.invia(active.value.id, testo);
    if (anteprima.value) URL.revokeObjectURL(anteprima.value);
    anteprima.value = "";
    if (fileRef.value) fileRef.value.value = "";
  } catch (e) {
    draft.value = testo;
    allegato.value = file;
    sendError.value = e instanceof Error ? e.message : "Invio non riuscito.";
  } finally {
    inviando.value = false;
  }
}

const MAX_IMMAGINE = 5 * 1024 * 1024;

function preparaAllegato(file: File | null | undefined) {
  if (!file) return;
  if (!file.type.startsWith("image/")) {
    sendError.value = "Si possono allegare solo immagini.";
    return;
  }
  if (file.size > MAX_IMMAGINE) {
    sendError.value = "L'immagine supera i 5 MB.";
    return;
  }
  rimuoviAllegato();
  sendError.value = "";
  allegato.value = file;
  anteprima.value = URL.createObjectURL(file);
  nextTick(() => inputRef.value?.focus());
}

function rimuoviAllegato() {
  if (anteprima.value) URL.revokeObjectURL(anteprima.value);
  allegato.value = null;
  anteprima.value = "";
  if (fileRef.value) fileRef.value.value = "";
}

function onFileScelto(e: Event) {
  preparaAllegato((e.target as HTMLInputElement).files?.[0]);
}

// screenshot incollati con Ctrl+V diventano subito un allegato
function onPaste(e: ClipboardEvent) {
  const file = [...(e.clipboardData?.files ?? [])].find((f) =>
    f.type.startsWith("image/"),
  );
  if (!file) return;
  e.preventDefault();
  preparaAllegato(file);
}

function onDrop(e: DragEvent) {
  trascinando.value = false;
  if (active.value) preparaAllegato(e.dataTransfer?.files?.[0]);
}

// il download vuole il token, quindi l'immagine passa da un blob e non da un src diretto
function srcImmagine(url: string) {
  if (!immagini.value[url] && !inCaricamento.has(url)) {
    inCaricamento.add(url);
    api
      .blob(url)
      .then((b) => (immagini.value[url] = URL.createObjectURL(b)))
      .finally(() => inCaricamento.delete(url));
  }
  return immagini.value[url] ?? "";
}

const lightbox = ref<string | null>(null);
const lbScala = ref(1);
const lbNat = ref({ w: 0, h: 0 });

function apriImmagine(url: string) {
  if (!immagini.value[url]) return;
  lbNat.value = { w: 0, h: 0 };
  lightbox.value = immagini.value[url];
}

function onLbLoad(e: Event) {
  const img = e.target as HTMLImageElement;
  if (lbNat.value.w) return;
  lbNat.value = { w: img.naturalWidth, h: img.naturalHeight };
  adattaLightbox();
}

// all'apertura l'immagine sta tutta nello schermo, senza ingrandire quelle piccole
function adattaLightbox() {
  const { w, h } = lbNat.value;
  if (!w || !h) return;
  lbScala.value = Math.min(1, (window.innerWidth * 0.9) / w, (window.innerHeight * 0.85) / h);
}

function lbZoom(direzione: number) {
  const nuova = lbScala.value * (direzione > 0 ? 1.25 : 0.8);
  lbScala.value = Math.min(8, Math.max(0.05, nuova));
}

function apriInScheda() {
  if (lightbox.value) window.open(lightbox.value, "_blank");
}

function chiudiLightbox() {
  lightbox.value = null;
}

function onTasto(e: KeyboardEvent) {
  if (e.key === "Escape" && lightbox.value) chiudiLightbox();
}

const candidatiGruppo = computed(() => {
  const q = cercaMembri.value.trim().toLowerCase();
  return utenti.value
    .filter((u) => u.id !== myId.value)
    .filter((u) => !q || u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q))
    .slice(0, 50);
});

function iniziaGruppo() {
  creandoGruppo.value = !creandoGruppo.value;
  erroreGruppo.value = "";
  if (creandoGruppo.value) nextTick(() => nomeGruppoRef.value?.focus());
}

function annullaGruppo() {
  creandoGruppo.value = false;
  nomeGruppo.value = "";
  cercaMembri.value = "";
  membriGruppo.value = [];
  erroreGruppo.value = "";
}

async function confermaGruppo() {
  erroreGruppo.value = "";
  try {
    const id = await store.creaGruppo(nomeGruppo.value.trim(), membriGruppo.value);
    annullaGruppo();
    await open(id);
  } catch (e) {
    erroreGruppo.value = e instanceof Error ? e.message : "Creazione non riuscita.";
  }
}

function startEdit(msg: Messaggio) {
  editingId.value = msg.id;
  editDraft.value = msg.testo;
  nextTick(() => {
    const el = editRef.value?.[0];
    if (!el) return;
    el.focus();
    el.style.height = Math.min(el.scrollHeight, 200) + "px";
  });
}

function cancelEdit() {
  editingId.value = null;
  editDraft.value = "";
  nextTick(() => inputRef.value?.focus());
}

async function saveEdit(msgId: number) {
  const testo = editDraft.value.trim();
  if (!testo) return;
  await store.modifica(msgId, testo);
  cancelEdit();
}

function editLastIfEmpty(e: KeyboardEvent) {
  if (draft.value || !active.value) return;
  const mine = active.value.messaggi.filter(
    (m) => m.senderId === myId.value && !m.eliminato,
  );
  const last = mine.at(-1);
  if (!last) return;
  e.preventDefault();
  startEdit(last);
}

async function deleteMsg(msgId: number) {
  if (!confirm("Eliminare questo messaggio?")) return;
  await store.elimina(msgId);
}

async function avviaPrivata(u: Utente) {
  const params = new URLSearchParams({
    conUserId: String(u.id),
    nomeUtente: u.name,
  });
  try {
    const conv = await api.post<Omit<Conversazione, "messaggi" | "nonLetti">>(
      `/api/chat/conversazioni/privata?${params}`,
      {},
    );
    store.dismissed.delete(conv.id);
    store.saveDismissed();
    if (!store.conversazioni.some((c) => c.id === conv.id))
      store.conversazioni.push({ ...conv, messaggi: [], nonLetti: 0 });
    ricerca.value = "";
    await open(conv.id);
  } catch (e) {
    sendError.value =
      e instanceof Error ? e.message : "Impossibile avviare la chat.";
  }
}

onMounted(async () => {
  window.addEventListener("keydown", onTasto);
  await store.carica();
  store.connetti();
  utenti.value = await api.get<Utente[]>("/api/users").catch(() => []);
});

// la connessione WebSocket la gestisce il layout, così i badge dei non letti restano attivi nelle altre pagine
onUnmounted(() => {
  window.removeEventListener("keydown", onTasto);
  store.setActive(null);
  rimuoviAllegato();
  Object.values(immagini.value).forEach((u) => URL.revokeObjectURL(u));
});
</script>

<style scoped>
.chat {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  height: 100%;
  min-height: 0;
  background: var(--bg-primary);
  border: 0.5px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  font-size: 14px;
}

.side {
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--bg-panel);
  border-right: 0.5px solid var(--border);
}
.side-search {
  padding: 10px;
  border-bottom: 0.5px solid var(--border);
}
.side-search input {
  width: 100%;
  background: var(--bg-input);
  border: none;
  border-radius: 6px;
  padding: 7px 10px;
  color: var(--text-primary);
  font-size: 12px;
}
.side-search input:focus {
  outline: 1px solid var(--purple);
}
.side-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 6px 8px 12px;
}
.side-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--text-tertiary);
  padding: 10px 6px 4px;
}
.side-add {
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
}
.side-add:hover {
  color: var(--text-primary);
}
.side-empty {
  font-size: 12px;
  color: var(--text-tertiary);
  padding: 6px;
  line-height: 1.4;
}
.conv {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  margin: 1px 0;
  border-radius: 6px;
  color: var(--text-secondary);
  cursor: pointer;
}
.conv:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}
.conv.sel {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}
.conv.unread {
  color: var(--text-primary);
  font-weight: 600;
}
.conv-hash {
  width: 24px;
  text-align: center;
  font-size: 18px;
  color: var(--text-tertiary);
}
.conv-name {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
}
.non-letti {
  background: var(--coral-dark);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  border-radius: 9px;
  padding: 1px 6px;
}
.conv-close {
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  font-size: 15px;
  visibility: hidden;
}
.conv:hover .conv-close {
  visibility: visible;
}
.conv-close:hover {
  color: var(--text-primary);
}

.av {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}
.av.sm {
  width: 24px;
  height: 24px;
  font-size: 10px;
}

.pannello {
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
}
.head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  border-bottom: 0.5px solid var(--border);
  box-shadow: 0 1px 2px var(--shadow);
}
.back {
  display: none;
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  padding: 0 4px 0 0;
}
.head-hash {
  font-size: 22px;
  color: var(--text-tertiary);
}
.head-name {
  font-weight: 600;
  color: var(--text-primary);
}
.head-sub {
  font-size: 12px;
  color: var(--text-tertiary);
  padding-left: 10px;
  border-left: 0.5px solid var(--border-md);
}
.head-tools {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.zoom {
  display: flex;
  border: 0.5px solid var(--border-md);
  border-radius: 6px;
  overflow: hidden;
}
.zoom button {
  background: none;
  border: none;
  color: var(--text-secondary);
  padding: 2px 8px;
  font-size: 12px;
  cursor: pointer;
}
.zoom button:hover:not(:disabled) {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}
.zoom button:disabled {
  opacity: 0.35;
  cursor: default;
}
.zoom-val {
  min-width: 44px;
  font-variant-numeric: tabular-nums;
}
.head-btn {
  background: none;
  border: 0.5px solid var(--border-md);
  color: var(--text-secondary);
  border-radius: 6px;
  padding: 3px 10px;
  font-size: 12px;
  cursor: pointer;
}
.head-btn.on {
  color: var(--text-primary);
  background: var(--bg-tertiary);
}

.body {
  flex: 1;
  min-height: 0;
  display: flex;
}
.colonna {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.scroller {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  padding: 0 0 6px;
}

.intro {
  padding: 10px 14px 2px;
}
.intro-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--bg-tertiary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 6px;
}
.intro-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}
.intro-sub {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.day-divider {
  display: flex;
  align-items: center;
  margin: 8px 14px 2px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-tertiary);
}
.day-divider::before,
.day-divider::after {
  content: "";
  flex: 1;
  height: 0.5px;
  background: var(--border-md);
}
.day-divider span {
  padding: 0 8px;
}

.riga {
  position: relative;
  display: flex;
  gap: 12px;
  padding: 1px 16px;
}
.riga.primo {
  margin-top: 12px;
}
.riga:hover,
.riga.editing {
  background: var(--hover);
}
.gutter {
  width: 30px;
  flex-shrink: 0;
  display: flex;
  justify-content: center;
}
.riga.primo .gutter {
  padding-top: 1px;
}
.gutter-time {
  font-size: 10px;
  color: var(--text-tertiary);
  line-height: 20px;
  visibility: hidden;
  font-variant-numeric: tabular-nums;
}
.riga:hover .gutter-time {
  visibility: visible;
}
.corpo {
  flex: 1;
  min-width: 0;
}
.meta {
  display: flex;
  align-items: baseline;
  gap: 8px;
  line-height: 1.3;
  margin-bottom: 1px;
}
.author {
  font-weight: 600;
  font-size: 13px;
}
.stamp {
  font-size: 11px;
  color: var(--text-tertiary);
}
.text {
  color: var(--text-primary);
  font-size: 13px;
  line-height: 1.5;
  word-wrap: break-word;
}
.edited {
  font-size: 10px;
  color: var(--text-tertiary);
  margin-left: 4px;
}
.md :deep(p) {
  display: inline;
}
.md :deep(p + p)::before {
  content: "";
  display: block;
  margin-top: 2px;
}
.md :deep(code) {
  background: var(--code-bg);
  border-radius: 4px;
  padding: 1px 4px;
  font-size: 12.5px;
}
.md :deep(pre) {
  background: var(--code-bg);
  border: 0.5px solid var(--border);
  border-radius: 6px;
  padding: 8px 10px;
  margin: 4px 0;
  overflow-x: auto;
}
.md :deep(pre code) {
  background: none;
  padding: 0;
}
.md :deep(blockquote) {
  border-left: 3px solid var(--border-md);
  padding-left: 8px;
  color: var(--text-secondary);
}
.md :deep(a) {
  color: var(--purple-dark);
}
.md :deep(small) {
  color: var(--text-tertiary);
}
.md :deep(ul),
.md :deep(ol) {
  padding-left: 20px;
}

.actions {
  position: absolute;
  top: -14px;
  right: 16px;
  display: none;
  background: var(--bg-secondary);
  border: 0.5px solid var(--border-md);
  border-radius: 6px;
  box-shadow: 0 2px 8px var(--shadow);
  overflow: hidden;
  z-index: 1;
}
.riga:hover .actions {
  display: flex;
}
.actions button {
  background: none;
  border: none;
  color: var(--text-secondary);
  padding: 4px 8px;
  cursor: pointer;
  font-size: 13px;
}
.actions button:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}
.actions button.danger:hover {
  color: var(--coral-dark);
}

.edit-box {
  width: 100%;
  resize: none;
  background: var(--bg-tertiary);
  border: none;
  border-radius: 6px;
  color: var(--text-primary);
  padding: 8px 10px;
  font: inherit;
  margin-top: 2px;
}
.edit-box:focus {
  outline: none;
}
.edit-hint {
  font-size: 11px;
  color: var(--text-tertiary);
  margin: 4px 0 2px;
}
.edit-hint a {
  color: var(--purple-dark);
  cursor: pointer;
}

.members {
  width: 180px;
  flex-shrink: 0;
  border-left: 0.5px solid var(--border);
  background: var(--bg-panel);
  overflow-y: auto;
  padding: 0 8px;
}
.member {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 6px;
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}
.member-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.composer {
  padding: 0 14px 8px;
}
.composer-box {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: var(--bg-tertiary);
  border-radius: 8px;
  padding: 7px 10px;
}
.composer-box textarea {
  width: 100%;
  resize: none;
  background: none;
  border: none;
  color: var(--text-primary);
  font: inherit;
  font-size: 13px;
  line-height: 1.4;
  max-height: 200px;
}
.composer-box textarea:focus {
  outline: none;
}
.composer-box textarea::placeholder {
  color: var(--text-tertiary);
}
.send-error {
  font-size: 12px;
  color: var(--coral-dark);
  margin-bottom: 6px;
}

.placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--text-tertiary);
  font-size: 13px;
  text-align: center;
  padding: 24px;
}
.placeholder-icon {
  font-size: 36px;
  opacity: 0.6;
}

.av.xs {
  width: 20px;
  height: 20px;
  font-size: 9px;
}

.md :deep(h1),
.md :deep(h2),
.md :deep(h3) {
  display: block;
  font-weight: 700;
  line-height: 1.25;
  margin: 4px 0 2px;
  color: var(--text-primary);
}
.md :deep(h1) {
  font-size: 20px;
}
.md :deep(h2) {
  font-size: 17px;
}
.md :deep(h3) {
  font-size: 15px;
}
.md :deep(del) {
  text-decoration: line-through;
  opacity: 0.75;
}

.img {
  display: block;
  max-width: min(360px, 100%);
  max-height: 280px;
  min-height: 40px;
  margin: 3px 0 2px;
  border-radius: 6px;
  cursor: zoom-in;
  object-fit: contain;
  background: var(--bg-tertiary);
}

.img-attesa {
  width: 240px;
  height: 160px;
  cursor: default;
}
.pannello.dragging {
  outline: 2px dashed var(--purple);
  outline-offset: -6px;
}

.pending {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  padding: 4px;
  background: var(--bg-tertiary);
  border-radius: 6px;
  width: fit-content;
  max-width: 100%;
}
.pending img {
  height: 48px;
  border-radius: 4px;
}
.pending-name {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pending button,
.attach {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
}
.attach {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--bg-secondary);
  font-size: 16px;
}
.pending button:hover,
.attach:hover {
  color: var(--text-primary);
}

.new-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  margin: 2px 0 6px;
  background: var(--bg-secondary);
  border-radius: 8px;
}
.new-group input[type="search"],
.new-group input:not([type]) {
  width: 100%;
  background: var(--bg-input);
  border: none;
  border-radius: 6px;
  padding: 6px 8px;
  color: var(--text-primary);
  font-size: 12px;
}
.new-group input:focus {
  outline: 1px solid var(--purple);
}
.pick-list {
  max-height: 180px;
  overflow-y: auto;
}
.pick {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 4px;
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
}
.pick:hover {
  background: var(--bg-tertiary);
}
.new-group-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}
.pick-count {
  flex: 1;
  font-size: 11px;
  color: var(--text-tertiary);
}
.btn-ghost,
.btn-primary {
  border: none;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 12px;
  cursor: pointer;
}
.btn-ghost {
  background: none;
  color: var(--text-secondary);
}
.btn-primary {
  background: var(--purple);
  color: #fff;
}
.btn-primary:disabled {
  opacity: 0.4;
  cursor: default;
}

.lightbox {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  overflow: auto;
  background: var(--overlay);
}
.lightbox img {
  display: block;
  margin: auto;
  max-width: none;
  user-select: none;
}
.lb-tools {
  position: fixed;
  top: 14px;
  right: 14px;
  z-index: 1001;
  display: flex;
  gap: 2px;
  padding: 4px;
  background: var(--toolbar);
  border: 0.5px solid var(--border-md);
  border-radius: 8px;
}
.lb-tools button {
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 15px;
  min-width: 30px;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
}
.lb-tools button:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}
.lb-val {
  font-size: 12px !important;
  min-width: 52px !important;
  font-variant-numeric: tabular-nums;
}

@media (max-width: 1100px) {
  .members {
    display: none;
  }
  .head-btn {
    display: none;
  }
}
@media (max-width: 760px) {
  .chat {
    grid-template-columns: minmax(0, 1fr);
  }
  .chat.has-active .side {
    display: none;
  }
  .chat:not(.has-active) .pannello {
    display: none;
  }
  .back {
    display: inline;
  }
}
</style>
