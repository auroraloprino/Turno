import { defineStore } from 'pinia'
import { ref } from 'vue'
import { Client } from '@stomp/stompjs'
import { api } from '@/api'
import { useAuthStore } from '@/stores/auth'

export interface Messaggio {
  id: number
  conversazioneId: number
  senderId: number
  senderName: string
  testo: string
  allegatoUrl: string | null
  allegatoNome: string | null
  timestamp: string
}

export interface Conversazione {
  id: number
  tipo: string
  nome: string
  partecipanti: number[]
  messaggi: Messaggio[]
  nonLetti: number
}

export const useChatStore = defineStore('chat', () => {
  const conversazioni = ref<Conversazione[]>([])
  const connesso = ref(false)
  let client: Client | null = null
  const subscribed = new Set<number>()

  async function carica() {
    const data = await api.get<Omit<Conversazione, 'messaggi' | 'nonLetti'>[]>('/api/chat/conversazioni')
    const existing = new Set(conversazioni.value.map(c => c.id))
    const nuove = data.filter(c => !existing.has(c.id))
    conversazioni.value = [
      ...conversazioni.value,
      ...nuove.map(c => ({ ...c, messaggi: [], nonLetti: 0 })),
    ]
    // subscribe to any newly loaded conversations
    nuove.forEach(c => sottoscrivi(c.id))
  }

  async function apri(id: number) {
    const conv = conversazioni.value.find(c => c.id === id)
    if (!conv) return
    if (conv.messaggi.length === 0) {
      conv.messaggi = await api.get<Messaggio[]>(`/api/chat/conversazioni/${id}/messaggi`)
    }
    conv.nonLetti = 0
  }

  async function invia(conversazioneId: number, testo: string) {
    await api.post(`/api/chat/conversazioni/${conversazioneId}/messaggi`, { testo })
  }

  function connetti() {
    const auth = useAuthStore()
    if (!auth.token || client) return

    client = new Client({
      brokerURL: `ws://localhost:8080/ws/websocket`,
      connectHeaders: { Authorization: `Bearer ${auth.token}` },
      onConnect: () => {
        connesso.value = true
        conversazioni.value.forEach(c => _subscribe(c.id))
      },
      onDisconnect: () => { connesso.value = false },
    })
    client.activate()
  }

  function disconnetti() {
    client?.deactivate()
    client = null
    connesso.value = false
    subscribed.clear()
  }

  function sottoscrivi(conversazioneId: number) {
    _subscribe(conversazioneId)
  }

  function _subscribe(conversazioneId: number) {
    if (!client?.connected || subscribed.has(conversazioneId)) return
    subscribed.add(conversazioneId)
    client.subscribe(`/topic/conversazione/${conversazioneId}`, frame => {
      const msg: Messaggio = JSON.parse(frame.body)
      const conv = conversazioni.value.find(x => x.id === msg.conversazioneId)
      if (!conv) return
      conv.messaggi.push(msg)
      conv.nonLetti++
    })
  }

  return { conversazioni, connesso, carica, apri, invia, connetti, disconnetti, sottoscrivi }
})
