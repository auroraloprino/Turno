<template>
  <div class="card" style="margin-bottom:0;display:flex;flex-direction:column;height:500px">
    <div class="card-title" style="display:flex;align-items:center;justify-content:space-between">
      Chat
      <button class="btn-sm btn-ok" @click="showNuova = !showNuova">+ Nuova</button>
    </div>

    <div v-if="showNuova" style="padding:12px;border-bottom:1px solid #f0f0f0;position:relative">
      <input
        v-model="ricerca"
        class="chat-textarea"
        style="width:100%;margin-bottom:0"
        placeholder="Cerca per nome o email…"
        @focus="showDropdown = true"
        @blur="hideDropdown"
        @input="showDropdown = true"
      />
      <div v-if="showDropdown && utentiFiltrati.length > 0" class="user-dropdown">
        <div
          v-for="u in utentiFiltrati"
          :key="u.id"
          class="user-dropdown-item"
          @mousedown.prevent="selezionaUtente(u)"
        >
          <span class="ud-name">{{ u.name }}</span>
          <span class="ud-email">{{ u.email }}</span>
        </div>
      </div>
      <div v-if="showDropdown && ricerca && utentiFiltrati.length === 0" class="user-dropdown">
        <div class="ud-empty">Nessun utente trovato</div>
      </div>
    </div>

    <div class="chat-wrap" style="flex:1;min-height:0">
      <div class="chat-contacts">
        <div v-if="store.conversazioni.length === 0" class="empty-state">Nessuna conversazione.</div>
        <div
          v-for="conv in store.conversazioni"
          :key="conv.id"
          class="contact-row"
          :class="{ sel: activeId === conv.id }"
          @click="toggleConv(conv.id)"
        >
          <div class="av" :class="conv.tipo === 'group' ? 'admin' : ''">
            {{ conv.tipo === 'group' ? 'GRP' : initials(conv.nome) }}
          </div>
          <div class="c-info">
            <div class="c-name">{{ conv.nome }}</div>
            <div class="c-prev">{{ lastPreview(conv) }}</div>
          </div>
          <div v-if="conv.nonLetti > 0" class="c-badge">{{ conv.nonLetti }}</div>
          <button class="conv-close" @click.stop="chiudiConv(conv.id)" title="Chiudi">×</button>
        </div>
      </div>

      <div class="chat-main">
        <template v-if="active">
          <div class="chat-header">
            <div class="av" :class="active.tipo === 'group' ? 'admin' : ''">
              {{ active.tipo === 'group' ? 'GRP' : initials(active.nome) }}
            </div>
            <span class="chat-header-name" style="flex:1">{{ active.nome }}</span>
          </div>

          <div class="msgs" ref="msgsBox">
            <div
              v-for="msg in active.messaggi"
              :key="msg.id"
              class="msg"
              :class="msg.senderId === auth.user?.id ? 'me' : 'them'"
            >
              <div v-if="active.tipo === 'group' && msg.senderId !== auth.user?.id" class="msg-sender">
                {{ msg.senderName }}
              </div>

              <!-- attachment -->
                <template v-if="msg.allegatoUrl">
                  <img v-if="isImage(msg.allegatoNome)" :src="msg.allegatoUrl" class="msg-img" />
                  <a v-else :href="msg.allegatoUrl" target="_blank" class="msg-file">📎 {{ msg.allegatoNome }}</a>
                </template>

                <!-- edit mode -->
                <template v-if="editingId === msg.id">
                  <textarea
                    v-model="editDraft"
                    class="edit-input"
                    rows="2"
                    @keydown.enter.exact.prevent="saveEdit(msg.id)"
                    @keydown.escape="cancelEdit"
                    ref="editInputRef"
                  />
                  <div style="display:flex;gap:4px;margin-top:4px">
                    <button class="btn-sm btn-ok" style="font-size:11px;padding:2px 8px" @click="saveEdit(msg.id)">Salva</button>
                    <button class="btn-sm" style="font-size:11px;padding:2px 8px" @click="cancelEdit">Annulla</button>
                  </div>
                </template>

                <template v-else>
                  <!-- markdown body -->
                  <div
                    v-if="msg.testo && msg.testo !== msg.allegatoNome"
                    class="msg-md"
                    v-html="renderMd(msg.testo)"
                  />
                  <div class="msg-actions" v-if="msg.senderId === auth.user?.id">
                    <button class="action-btn" @click="startEdit(msg)" title="Modifica">✏️</button>
                    <button class="action-btn" @click="deleteMsg(msg.id)" title="Elimina">🗑️</button>
                  </div>
                  <span v-if="msg.modificato" class="msg-edited">(modificato)</span>
                </template>

              <div class="msg-time">{{ fmtTime(msg.timestamp) }}</div>
            </div>
          </div>

          <div class="chat-input-area">
            <span v-if="uploadError" class="upload-err">{{ uploadError }}</span>
            <div class="chat-input-row">
              <label class="attach-btn" title="Allega file">
                +
                <input type="file" style="display:none" @change="uploadFile" accept="image/*,.pdf,.doc,.docx,.xls,.xlsx" />
              </label>
              <textarea
                v-model="draft"
                class="chat-textarea"
                placeholder="Scrivi un messaggio… (markdown supportato)"
                rows="1"
                @keydown.enter.exact.prevent="send"
                @input="autoResize"
                ref="textareaRef"
              />
              <button class="btn-msg" @click="send">Invia</button>
            </div>
          </div>
        </template>
        <div v-else class="empty-state" style="padding:24px">Seleziona una conversazione.</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { marked } from 'marked'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import type { Messaggio } from '@/stores/chat'
import { api } from '@/api'

const store = useChatStore()
const auth  = useAuthStore()

const activeId    = ref<number | null>(null)
const draft       = ref('')
const msgsBox     = ref<HTMLElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const showNuova   = ref(false)
const nuovaUserId = ref<number | null>(null)
const utentiDisponibili = ref<{ id: number; name: string; email: string }[]>([])
const uploadError = ref('')
const ricerca = ref('')
const showDropdown = ref(false)


const utentiFiltrati = computed(() => {
  const q = ricerca.value.toLowerCase().trim()
  if (!q) return utentiDisponibili.value
  return utentiDisponibili.value.filter(u =>
    u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q)
  )
})

const editingId    = ref<number | null>(null)
const editDraft    = ref('')
const editInputRef = ref<HTMLTextAreaElement | null>(null)

const active = computed(() => store.conversazioni.find(c => c.id === activeId.value) ?? null)

// configure marked: safe inline rendering
marked.setOptions({ breaks: true })

function renderMd(text: string): string {
  // -# small text
  let t = text.replace(/^-#\s+(.+)$/gm, '<small>$1</small>')
  // ~~strikethrough~~
  t = t.replace(/~~(.+?)~~/g, '<del>$1</del>')
  // __underline__
  t = t.replace(/__(.+?)__/g, '<u>$1</u>')
  return marked.parse(t) as string
}

function lastPreview(conv: typeof store.conversazioni[0]) {
  const last = conv.messaggi.at(-1)
  if (!last) return ''
  return last.allegatoNome ? `📎 ${last.allegatoNome}` : last.testo
}

function initials(name: string) {
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
}

function fmtTime(ts: string) {
  return new Date(ts).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
}

function isImage(nome: string | null) {
  if (!nome) return false
  return /\.(png|jpe?g|gif|webp|svg)$/i.test(nome)
}

function autoResize(e: Event) {
  const el = e.target as HTMLTextAreaElement
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

async function open(id: number) {
  activeId.value = id
  cancelEdit()
  await store.apri(id)
  scrollBottom()
}

async function send() {
  const testo = draft.value.trim()
  if (!testo || !active.value) return
  draft.value = ''
  if (textareaRef.value) textareaRef.value.style.height = 'auto'
  await store.invia(active.value.id, testo)
}

function startEdit(msg: Messaggio) {
  editingId.value = msg.id
  editDraft.value = msg.testo
  nextTick(() => editInputRef.value?.focus())
}

function cancelEdit() {
  editingId.value = null
  editDraft.value = ''
}

async function saveEdit(msgId: number) {
  const testo = editDraft.value.trim()
  if (!testo) return
  await store.modifica(msgId, testo)
  cancelEdit()
}

async function deleteMsg(msgId: number) {
  await store.elimina(msgId)
}

async function uploadFile(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file || !active.value) return
  uploadError.value = ''
  const form = new FormData()
  form.append('file', file)
  try {
    const res = await fetch(`/api/chat/conversazioni/${active.value.id}/messaggi/allegato`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${auth.token}` },
      body: form,
    })
    if (!res.ok) {
      uploadError.value = 'Upload fallito.'
    } else {
      const msg = await res.json()
      const conv = store.conversazioni.find(c => c.id === active.value!.id)
      if (conv && !conv.messaggi.some(m => m.id === msg.id)) conv.messaggi.push(msg)
    }
  } catch {
    uploadError.value = 'Errore di rete.'
  } finally {
    ;(e.target as HTMLInputElement).value = ''
  }
}

function toggleConv(id: number) {
  if (activeId.value === id) activeId.value = null
  else open(id)
}

function chiudiConv(id: number) {
  if (activeId.value === id) activeId.value = null
  store.rimuovi(id)
}

function selezionaUtente(u: { id: number; name: string; email: string }) {
  nuovaUserId.value = u.id
  ricerca.value = `${u.name} (${u.email})`
  showDropdown.value = false
  avviaPrivata()
}

function hideDropdown() {
  setTimeout(() => { showDropdown.value = false }, 150)
}

async function avviaPrivata() {
  if (!nuovaUserId.value) return
  const res = await fetch(`/api/chat/conversazioni/privata?conUserId=${nuovaUserId.value}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${auth.token}` },
  })
  if (!res.ok) return
  const conv = await res.json()
  // add to list, un-dismissing if previously closed
  store.dismissed.delete(conv.id)
  store.saveDismissed()
  const exists = store.conversazioni.find(c => c.id === conv.id)
  if (!exists) store.conversazioni.push({ ...conv, messaggi: [], nonLetti: 0 })
  showNuova.value = false
  nuovaUserId.value = null
  ricerca.value = ''
  open(conv.id)
}

function scrollBottom() {
  nextTick(() => {
    if (msgsBox.value) msgsBox.value.scrollTop = msgsBox.value.scrollHeight
  })
}

watch(() => active.value?.messaggi.length, scrollBottom)

onMounted(async () => {
  await store.carica()
  store.connetti()
  const users = await api.get<{ id: number; name: string; email: string }[]>('/api/users').catch(() => [])
  utentiDisponibili.value = users.filter(u => u.id !== auth.user?.id)
})

onUnmounted(() => store.disconnetti())
</script>

<style scoped>
.empty-state { font-size: 13px; color: var(--text-tertiary); }
.msg-sender  { font-size: 10px; color: var(--purple-dark); margin-bottom: 2px; }
.msg-edited  { font-size: 10px; opacity: .5; margin-left: 4px; }
.msg-img     { max-width: 200px; border-radius: 6px; display: block; margin-bottom: 4px; }
.msg-file    { font-size: 12px; color: var(--purple); }

.msg-md {
  font-size: 13px;
  line-height: 1.5;
}
.msg-md :deep(p)        { margin: 0 0 4px; }
.msg-md :deep(p:last-child) { margin-bottom: 0; }
.msg-md :deep(code)     { background: rgba(0,0,0,.08); border-radius: 3px; padding: 1px 4px; font-size: 12px; }
.msg-md :deep(pre)      { background: rgba(0,0,0,.08); border-radius: 6px; padding: 8px; overflow-x: auto; }
.msg-md :deep(pre code) { background: none; padding: 0; }
.msg-md :deep(ul), .msg-md :deep(ol) { margin: 2px 0; padding-left: 18px; }
.msg-md :deep(strong)   { font-weight: 600; }
.msg-md :deep(em)       { font-style: italic; }
.msg-md :deep(a)        { color: var(--purple); }
.msg-md :deep(small)    { font-size: 10px; opacity: .6; }
.msg-md :deep(del)      { opacity: .6; }
.msg-md :deep(u)        { text-decoration: underline; }

.msg-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity .15s;
  margin-top: 2px;
}
.msg:hover .msg-actions { opacity: 1; }

.action-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 11px;
  padding: 0 2px;
  line-height: 1;
}

.edit-input {
  width: 100%;
  border: 1px solid var(--purple);
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 13px;
  outline: none;
  resize: none;
  font-family: inherit;
}

/* input area */
.chat-input-area { border-top: 1px solid #f0f0f0; padding: 8px; }
.upload-err      { font-size: 11px; color: var(--coral-dark); display: block; margin-bottom: 4px; }

.chat-input-row {
  display: flex;
  align-items: flex-end;
  gap: 6px;
}

.attach-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--purple);
  color: #fff;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  flex-shrink: 0;
  user-select: none;
  transition: background .15s;
}
.attach-btn:hover { background: var(--purple-dark); }

.chat-textarea {
  flex: 1;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 13px;
  font-family: inherit;
  resize: none;
  outline: none;
  line-height: 1.4;
  overflow-y: hidden;
  transition: border-color .15s;
}
.chat-textarea:focus { border-color: var(--purple); }

.conv-close {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-tertiary);
  line-height: 1;
  padding: 0 2px;
  opacity: 0;
  transition: opacity .15s, color .15s;
  flex-shrink: 0;
}
.contact-row:hover .conv-close { opacity: 1; }
.conv-close:hover { color: var(--coral-dark); }

.user-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--bg-secondary);
  border: 0.5px solid var(--border-md);
  border-radius: var(--radius-md);
  z-index: 100;
  max-height: 200px;
  overflow-y: auto;
  box-shadow: 0 4px 16px rgba(0,0,0,.3);
}
.user-dropdown-item {
  display: flex;
  flex-direction: column;
  padding: 8px 12px;
  cursor: pointer;
  transition: background .12s;
}
.user-dropdown-item:hover { background: var(--bg-tertiary); }
.ud-name  { font-size: 13px; color: var(--text-primary); font-weight: 500; }
.ud-email { font-size: 11px; color: var(--text-secondary); margin-top: 1px; }
.ud-empty { padding: 10px 12px; font-size: 12px; color: var(--text-tertiary); }
</style>
