<template>
  <div class="card" style="margin-bottom:0;">
    <div class="card-title">Chat</div>
    <div class="chat-wrap">
      <div class="chat-contacts">
        <div
          v-for="conv in store.conversations"
          :key="conv.id"
          class="contact-row"
          :class="{ sel: activeId === conv.id }"
          @click="open(conv.id)"
        >
          <div class="av" :class="conv.type === 'group' ? 'admin' : ''">
            {{ conv.type === 'group' ? 'GRP' : initials(conv.name) }}
          </div>
          <div class="c-info">
            <div class="c-name">{{ conv.name }}</div>
            <div class="c-prev">{{ conv.messages.at(-1)?.text ?? '' }}</div>
          </div>
          <div v-if="conv.unread > 0" class="c-badge">{{ conv.unread }}</div>
        </div>
      </div>

      <div class="chat-main">
        <template v-if="active">
          <div class="chat-header">
            <div class="av" :class="active.type === 'group' ? 'admin' : ''">
              {{ active.type === 'group' ? 'GRP' : initials(active.name) }}
            </div>
            <span class="chat-header-name">{{ active.name }}</span>
            <div v-if="adminControls && active.type === 'private'" class="admin-actions">
              <button class="btn-sm btn-no" @click="rimuoviUtente">Rimuovi utente</button>
            </div>
          </div>

          <div class="msgs" ref="msgsBox">
            <div
              v-for="msg in active.messages"
              :key="msg.id"
              class="msg"
              :class="msg.senderId === store.currentUser?.id ? 'me' : 'them'"
            >
              <div v-if="active.type === 'group' && msg.senderId !== store.currentUser?.id" class="msg-sender">{{ msg.senderName }}</div>
              {{ msg.text }}
              <div class="msg-time">{{ msg.time }}</div>
            </div>
          </div>

          <div class="chat-input-row">
            <input
              v-model="draft"
              type="text"
              @keydown.enter="send"
            />
            <button class="btn-msg" @click="send">Invia</button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { useChatStore } from '@/stores/chat'

defineProps<{ adminControls?: boolean }>()

const store = useChatStore()
const activeId = ref<number | null>(store.conversations[0]?.id ?? null)
const draft = ref('')
const msgsBox = ref<HTMLElement | null>(null)

const active = computed(() => store.conversations.find(c => c.id === activeId.value) ?? null)

function initials(name: string) {
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
}

function open(id: number) {
  activeId.value = id
  store.markRead(id)
}

function send() {
  const text = draft.value.trim()
  if (!text || !active.value) return
  store.sendMessage(active.value.id, text)
  draft.value = ''
  nextTick(() => {
    if (msgsBox.value) msgsBox.value.scrollTop = msgsBox.value.scrollHeight
  })
}

function rimuoviUtente() {
  if (!active.value) return
  store.conversations.splice(store.conversations.indexOf(active.value), 1)
  activeId.value = store.conversations[0]?.id ?? null
}

watch(activeId, () => {
  nextTick(() => {
    if (msgsBox.value) msgsBox.value.scrollTop = msgsBox.value.scrollHeight
  })
})
</script>

<style scoped>
.admin-actions {
  margin-left: auto;
  display: flex;
  gap: 6px;
}

.msg-sender {
  font-size: 10px;
  color: var(--purple-dark);
  margin-bottom: 2px;
}
</style>
