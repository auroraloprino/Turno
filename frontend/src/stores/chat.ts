import { defineStore } from 'pinia'
import { ref } from 'vue'

export type Role = 'admin' | 'user'

export interface User {
  id: number
  name: string
  role: Role
}

export interface Message {
  id: number
  senderId: number
  senderName: string
  text: string
  time: string
}

export interface Conversation {
  id: number
  type: 'group' | 'private'
  participants: number[]
  name: string
  messages: Message[]
  unread: number
}

export const useChatStore = defineStore('chat', () => {
  const currentUser = ref<User | null>(null)
  const conversations = ref<Conversation[]>([])
  let nextMsgId = 1

  function setCurrentUser(user: User) {
    currentUser.value = user
  }

  function sendMessage(conversationId: number, text: string) {
    if (!currentUser.value) return
    const conv = conversations.value.find(c => c.id === conversationId)
    if (!conv) return
    const now = new Date()
    const time = now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0')
    conv.messages.push({
      id: nextMsgId++,
      senderId: currentUser.value.id,
      senderName: currentUser.value.name,
      text,
      time,
    })
  }

  function markRead(conversationId: number) {
    const conv = conversations.value.find(c => c.id === conversationId)
    if (conv) conv.unread = 0
  }

  return { currentUser, conversations, setCurrentUser, sendMessage, markRead }
})
