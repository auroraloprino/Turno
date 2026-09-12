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

    const wsUrl = `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws/websocket`

    client = new Client({
      brokerURL: wsUrl,
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
