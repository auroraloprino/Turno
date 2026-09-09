<template>
  <div class="card" style="margin-bottom:0;">
    <div class="card-title" style="display:flex;align-items:center;justify-content:space-between">
      Chat
      <button class="btn-sm btn-ok" @click="showNuova = !showNuova">+ Nuova</button>
    </div>

    <div v-if="showNuova" style="padding:12px;border-bottom:1px solid #f0f0f0;display:flex;gap:8px;flex-wrap:wrap">
      <select v-model="nuovaUserId" style="flex:1;min-width:120px">
        <option value="">Seleziona utente…</option>
        <option v-for="u in utentiDisponibili" :key="u.id" :value="u.id">{{ u.name }}</option>
      </select>
      <button class="btn-sm btn-ok" @click="avviaPrivata">Avvia chat</button>
    </div>

    <div class="chat-wrap">
      <div class="chat-contacts">
        <div v-if="store.conversazioni.length === 0" class="empty-state">Nessuna conversazione.</div>
        <div
          v-for="conv in store.conversazioni"
          :key="conv.id"
          class="contact-row"
          :class="{ sel: activeId === conv.id }"
          @click="open(conv.id)"
        >
          <div class="av" :class="conv.tipo === 'group' ? 'admin' : ''">
            {{ conv.tipo === 'group' ? 'GRP' : initials(conv.nome) }}
          </div>
          <div class="c-info">
            <div class="c-name">{{ conv.nome }}</div>
            <div class="c-prev">{{ conv.messaggi.at(-1)?.testo ?? '' }}</div>
          </div>
          <div v-if="conv.nonLetti > 0" class="c-badge">{{ conv.nonLetti }}</div>
        </div>
      </div>

      <div class="chat-main">
        <template v-if="active">
          <div class="chat-header">
            <div class="av" :class="active.tipo === 'group' ? 'admin' : ''">
              {{ active.tipo === 'group' ? 'GRP' : initials(active.nome) }}
            </div>
            <span class="chat-header-name">{{ active.nome }}</span>
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
              <template v-if="msg.allegatoUrl">
                <img v-if="isImage(msg.allegatoNome)" :src="msg.allegatoUrl" style="max-width:200px;border-radius:6px;display:block;margin-bottom:4px" />
                <a v-else :href="msg.allegatoUrl" target="_blank" style="font-size:12px;color:#6C63D5">📎 {{ msg.allegatoNome }}</a>
              </template>
              <span v-if="msg.testo !== msg.allegatoNome">{{ msg.testo }}</span>
              <div class="msg-time">{{ fmtTime(msg.timestamp) }}</div>
            </div>
          </div>

          <div class="chat-input-row">
            <input v-model="draft" type="text" placeholder="Scrivi un messaggio…" @keydown.enter="send" />
            <label class="btn-msg" style="cursor:pointer" title="Allega file">
              📎
              <input type="file" style="display:none" @change="uploadFile" />
            </label>
            <button class="btn-msg" @click="send">Invia</button>
          </div>
        </template>
        <div v-else class="empty-state" style="padding:24px">Seleziona una conversazione.</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { api } from '@/api'

const store = useChatStore()
const auth  = useAuthStore()

const activeId = ref<number | null>(null)
const draft    = ref('')
const msgsBox  = ref<HTMLElement | null>(null)
const showNuova = ref(false)
const nuovaUserId = ref<number | ''>('')
const utentiDisponibili = ref<{ id: number; name: string }[]>([])

const active = computed(() => store.conversazioni.find(c => c.id === activeId.value) ?? null)

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

async function open(id: number) {
  activeId.value = id
  await store.apri(id)
  scrollBottom()
}

async function send() {
  const testo = draft.value.trim()
  if (!testo || !active.value) return
  draft.value = ''
  await store.invia(active.value.id, testo)
}

async function uploadFile(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file || !active.value) return
  const form = new FormData()
  form.append('file', file)
  const token = auth.token
  await fetch(`/api/chat/conversazioni/${active.value.id}/messaggi/allegato`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: form,
  })
  ;(e.target as HTMLInputElement).value = ''
}

async function avviaPrivata() {
  if (!nuovaUserId.value) return
  const token = auth.token
  const res = await fetch(`/api/chat/conversazioni/privata?conUserId=${nuovaUserId.value}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!res.ok) return
  const conv = await res.json()
  await store.carica()
  showNuova.value = false
  nuovaUserId.value = ''
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
  utentiDisponibili.value = (await api.get<{ id: number; name: string }[]>('/api/users')
    .catch(() => [])).filter(u => u.id !== auth.user?.id)
  if (store.conversazioni.length > 0) open(store.conversazioni[0].id)
})

onUnmounted(() => store.disconnetti())
</script>

<style scoped>
.empty-state { font-size: 13px; color: var(--text-tertiary); }
.msg-sender { font-size: 10px; color: var(--purple-dark); margin-bottom: 2px; }
</style>
