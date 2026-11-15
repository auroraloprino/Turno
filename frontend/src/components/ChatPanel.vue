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
        <template v-if="gruppi.length">
          <div class="side-section">Gruppi</div>
          <div
            v-for="c in gruppi"
            :key="c.id"
            class="conv"
            :class="{ sel: activeId === c.id, unread: c.nonLetti > 0 }"
            @click="open(c.id)"
          >
            <span class="conv-hash">#</span>
            <span class="conv-name">{{ c.nome }}</span>
            <span v-if="c.nonLetti" class="badge">{{ c.nonLetti }}</span>
            <button
              class="conv-close"
              title="Nascondi"
              @click.stop="chiudiConv(c.id)"
            >
              ×
            </button>
          </div>
        </template>

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
          <span class="avatar sm" :style="avatarStyle(otherId(c))">
            {{ initials(titolo(c)) }}
          </span>
          <span class="conv-name">{{ titolo(c) }}</span>
          <span v-if="c.nonLetti" class="badge">{{ c.nonLetti }}</span>
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
            <span class="avatar sm" :style="avatarStyle(u.id)">
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

    <section class="main">
      <template v-if="active">
        <header class="head">
          <button class="back" title="Indietro" @click="chiudiAttiva">‹</button>
          <span v-if="active.tipo === 'group'" class="head-hash">#</span>
          <span v-else class="avatar sm" :style="avatarStyle(otherId(active))">
            {{ initials(titolo(active)) }}
          </span>
          <span class="head-name">{{ titolo(active) }}</span>
          <span v-if="active.tipo === 'group'" class="head-sub">
            {{ active.partecipanti.length }} membri
          </span>
          <button
            v-if="active.tipo === 'group'"
            class="head-btn"
            :class="{ on: showMembers }"
            title="Mostra membri"
            @click="showMembers = !showMembers"
          >
            Membri
          </button>
        </header>

        <div class="body">
          <div ref="scroller" class="scroller" @scroll="onScroll">
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
                class="msg"
                :class="{ head: row.head, editing: editingId === row.msg.id }"
              >
                <div class="gutter">
                  <span
                    v-if="row.head"
                    class="avatar"
                    :style="avatarStyle(row.msg.senderId)"
                  >
                    {{ initials(nomeDi(row.msg)) }}
                  </span>
                  <span v-else class="gutter-time">{{
                    fmtTime(row.msg.timestamp)
                  }}</span>
                </div>

                <div class="content">
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

          <aside v-if="active.tipo === 'group' && showMembers" class="members">
            <div class="side-section">
              Membri — {{ active.partecipanti.length }}
            </div>
            <div v-for="id in active.partecipanti" :key="id" class="member">
              <span class="avatar sm" :style="avatarStyle(id)">
                {{ initials(nomeUtente(id)) }}
              </span>
              <span class="member-name">{{ nomeUtente(id) }}</span>
            </div>
          </aside>
        </div>

        <footer class="composer">
          <div v-if="sendError" class="send-error">{{ sendError }}</div>
          <div class="composer-box">
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
              @keydown.enter.exact.prevent="send"
              @keydown.up="editLastIfEmpty"
            />
          </div>
          <div class="composer-hint">
            Invio per inviare · Shift+Invio per andare a capo · ↑ per modificare
            l'ultimo messaggio · supporta **grassetto**, *corsivo*, `codice`
          </div>
        </footer>
      </template>

      <div v-else class="placeholder">
        <div class="placeholder-icon">💬</div>
        <div>Seleziona una conversazione o cerca una persona per iniziare.</div>
      </div>
    </section>
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
  t = t.replace(/~~(.+?)~~/g, "<del>$1</del>");
  t = t.replace(/__(.+?)__/g, "<u>$1</u>");
  // i messaggi arrivano da altri utenti, quindi vanno sempre sanificati prima di finire nel v-html
  return DOMPurify.sanitize(marked.parse(t) as string, {
    ALLOWED_TAGS: [
      "p",
      "br",
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

async function send() {
  const testo = draft.value.trim();
  if (!testo || !active.value) return;
  sendError.value = "";
  draft.value = "";
  if (inputRef.value) inputRef.value.style.height = "auto";
  try {
    await store.invia(active.value.id, testo);
  } catch (e) {
    draft.value = testo;
    sendError.value = e instanceof Error ? e.message : "Invio non riuscito.";
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
  await store.carica();
  store.connetti();
  utenti.value = await api.get<Utente[]>("/api/users").catch(() => []);
});

// la connessione WebSocket la gestisce il layout, così i badge dei non letti restano attivi nelle altre pagine
onUnmounted(() => store.setActive(null));
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
  background: #191919;
  border-right: 0.5px solid var(--border);
}
.side-search {
  padding: 10px;
  border-bottom: 0.5px solid var(--border);
}
.side-search input {
  width: 100%;
  background: #111;
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
  padding: 14px 6px 6px;
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
  padding: 6px 8px;
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
.badge {
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

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}
.avatar.sm {
  width: 24px;
  height: 24px;
  font-size: 10px;
}

.main {
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
}
.head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-bottom: 0.5px solid var(--border);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
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
.head-btn {
  margin-left: auto;
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
.scroller {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  padding: 0 0 12px;
}

.intro {
  padding: 32px 16px 12px;
}
.intro-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--bg-tertiary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 600;
  margin-bottom: 10px;
}
.intro-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}
.intro-sub {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.day-divider {
  display: flex;
  align-items: center;
  margin: 18px 16px 6px;
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

.msg {
  position: relative;
  display: flex;
  gap: 14px;
  padding: 2px 16px;
}
.msg.head {
  margin-top: 14px;
}
.msg:hover,
.msg.editing {
  background: rgba(255, 255, 255, 0.025);
}
.gutter {
  width: 40px;
  flex-shrink: 0;
  display: flex;
  justify-content: center;
}
.msg.head .gutter {
  padding-top: 2px;
}
.gutter-time {
  font-size: 10px;
  color: var(--text-tertiary);
  line-height: 22px;
  visibility: hidden;
  font-variant-numeric: tabular-nums;
}
.msg:hover .gutter-time {
  visibility: visible;
}
.content {
  flex: 1;
  min-width: 0;
}
.meta {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.author {
  font-weight: 600;
  font-size: 14px;
}
.stamp {
  font-size: 11px;
  color: var(--text-tertiary);
}
.text {
  color: var(--text-primary);
  line-height: 1.45;
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
  margin-top: 4px;
}
.md :deep(code) {
  background: #111;
  border-radius: 4px;
  padding: 1px 4px;
  font-size: 12.5px;
}
.md :deep(pre) {
  background: #111;
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  z-index: 1;
}
.msg:hover .actions {
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
  width: 200px;
  flex-shrink: 0;
  border-left: 0.5px solid var(--border);
  background: #191919;
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
  padding: 0 16px 10px;
}
.composer-box {
  background: var(--bg-tertiary);
  border-radius: 8px;
  padding: 10px 14px;
}
.composer-box textarea {
  width: 100%;
  resize: none;
  background: none;
  border: none;
  color: var(--text-primary);
  font: inherit;
  line-height: 1.4;
  max-height: 200px;
}
.composer-box textarea:focus {
  outline: none;
}
.composer-box textarea::placeholder {
  color: var(--text-tertiary);
}
.composer-hint {
  font-size: 10px;
  color: var(--text-tertiary);
  margin-top: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  .chat:not(.has-active) .main {
    display: none;
  }
  .back {
    display: inline;
  }
}
</style>
