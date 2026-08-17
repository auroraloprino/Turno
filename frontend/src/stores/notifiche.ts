import { defineStore } from 'pinia'
import { ref } from 'vue'
import { Client } from '@stomp/stompjs'
import { useAuthStore } from './auth'

export interface Notifica {
  tipo: string
  messaggio: string
}

export const useNotificheStore = defineStore('notifiche', () => {
  const lista = ref<Notifica[]>([])
  let client: Client | null = null

  function connetti() {
    const auth = useAuthStore()
    if (!auth.user || client) return

    client = new Client({
      brokerURL: `ws://localhost:8080/ws/websocket`,
      connectHeaders: { Authorization: `Bearer ${auth.token}` },
      onConnect: () => {
        client!.subscribe(`/topic/notifiche/${auth.user!.id}`, msg => {
          lista.value.unshift(JSON.parse(msg.body))
        })
      },
    })
    client.activate()
  }

  function disconnetti() {
    client?.deactivate()
    client = null
  }

  function rimuovi(index: number) {
    lista.value.splice(index, 1)
  }

  return { lista, connetti, disconnetti, rimuovi }
})
