<template>
  <div class="card" style="margin-bottom:0;">
    <div class="card-title">Chat</div>
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
              {{ msg.testo }}
              <div class="msg-time">{{ fmtTime(msg.timestamp) }}</div>
            </div>
          </div>

          <div class="chat-input-row">
            <input v-model="draft" type="text" @keydown.enter="send" />
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

const store = useChatStore()
const auth  = useAuthStore()

const activeId = ref<number | null>(null)
const draft    = ref('')
const msgsBox  = ref<HTMLElement | null>(null)

const active = computed(() => store.conversazioni.find(c => c.id === activeId.value) ?? null)

function initials(name: string) {
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
}

function fmtTime(ts: string) {
  return new Date(ts).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
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

function scrollBottom() {
  nextTick(() => {
    if (msgsBox.value) msgsBox.value.scrollTop = msgsBox.value.scrollHeight
  })
}

watch(() => active.value?.messaggi.length, scrollBottom)

onMounted(async () => {
  await store.carica()
  store.connetti()
  if (store.conversazioni.length > 0) open(store.conversazioni[0].id)
})

onUnmounted(() => store.disconnetti())
</script>

<style scoped>
.empty-state {
  font-size: 13px;
  color: var(--text-tertiary);
}

.msg-sender {
  font-size: 10px;
  color: var(--purple-dark);
  margin-bottom: 2px;
}
</style>
