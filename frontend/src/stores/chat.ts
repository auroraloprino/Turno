import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { api } from "@/api";
import { useAuthStore } from "@/stores/auth";

export interface Messaggio {
  id: number;
  conversazioneId: number;
  senderId: number;
  senderName: string;
  testo: string;
  allegatoUrl: string | null;
  allegatoNome: string | null;
  timestamp: string;
  modificato: boolean;
  eliminato: boolean;
}

export interface Conversazione {
  id: number;
  tipo: string;
  nome: string;
  partecipanti: number[];
  messaggi: Messaggio[];
  nonLetti: number;
}

export const useChatStore = defineStore("chat", () => {
  const conversazioni = ref<Conversazione[]>([]);
  const connesso = ref(false);
  let client: Client | null = null;
  const subscribed = new Set<number>();
  const _activeId = ref<number | null>(null);
  const _dismissedKey = "chat_dismissed";
  const dismissed = new Set<number>(
    JSON.parse(localStorage.getItem(_dismissedKey) || "[]"),
  );

  function _saveDismissed() {
    localStorage.setItem(_dismissedKey, JSON.stringify([...dismissed]));
  }

  async function carica() {
    const data = await api.get<Omit<Conversazione, "messaggi" | "nonLetti">[]>(
      "/api/chat/conversazioni",
    );
    const existingMap = new Map(conversazioni.value.map((c) => [c.id, c]));
    conversazioni.value = data
      .filter((c) => !dismissed.has(c.id))
      .map((c) => existingMap.get(c.id) ?? { ...c, messaggi: [], nonLetti: 0 });
    data.forEach((c) => _subscribe(c.id));
  }

  async function apri(id: number) {
    _activeId.value = id;
    const conv = conversazioni.value.find((c) => c.id === id);
    if (!conv) return;
    conv.messaggi = await api.get<Messaggio[]>(
      `/api/chat/conversazioni/${id}/messaggi`,
    );
    conv.nonLetti = 0;
  }

  async function invia(conversazioneId: number, testo: string) {
    const msg = await api.post<Messaggio>(
      `/api/chat/conversazioni/${conversazioneId}/messaggi`,
      { testo },
    );
    _upsertMsg(msg);
  }

  async function modifica(messaggioId: number, testo: string) {
    const msg = await api.patch<Messaggio>(
      `/api/chat/messaggi/${messaggioId}`,
      { testo },
    );
    _upsertMsg(msg);
  }

  function _upsertMsg(msg: Messaggio) {
    const conv = conversazioni.value.find((c) => c.id === msg.conversazioneId);
    if (!conv) return;
    const idx = conv.messaggi.findIndex((m) => m.id === msg.id);
    if (idx >= 0) conv.messaggi[idx] = msg;
    else conv.messaggi.push(msg);
  }

  async function elimina(messaggioId: number) {
    await api.delete(`/api/chat/messaggi/${messaggioId}`);
  }

  function rimuovi(id: number) {
    dismissed.add(id);
    _saveDismissed();
    const idx = conversazioni.value.findIndex((c) => c.id === id);
    if (idx >= 0) conversazioni.value.splice(idx, 1);
  }

  function connetti() {
    const auth = useAuthStore();
    if (!auth.token || client || connesso.value) return;

    client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      connectHeaders: { Authorization: `Bearer ${auth.token}` },
      onConnect: async () => {
        connesso.value = true;
        await carica();
        conversazioni.value.forEach((c) => _subscribe(c.id));
      },
      onDisconnect: () => {
        connesso.value = false;
      },
      reconnectDelay: 5000,
    });
    client.activate();
  }

  function disconnetti() {
    client?.deactivate();
    client = null;
    connesso.value = false;
    subscribed.clear();
  }

  function _subscribe(conversazioneId: number) {
    if (!client?.connected || subscribed.has(conversazioneId)) return;
    subscribed.add(conversazioneId);
    client.subscribe(`/topic/conversazione/${conversazioneId}`, (frame) => {
      const msg: Messaggio = JSON.parse(frame.body);
      const conv = conversazioni.value.find(
        (x) => x.id === msg.conversazioneId,
      );
      if (!conv) return;
      const idx = conv.messaggi.findIndex((m) => m.id === msg.id);
      if (msg.eliminato) {
        if (idx >= 0) conv.messaggi.splice(idx, 1);
      } else if (idx >= 0) {
        conv.messaggi[idx] = msg;
      } else {
        conv.messaggi.push(msg);
        const auth = useAuthStore();
        if (
          msg.senderId !== auth.user?.id &&
          _activeId.value !== conversazioneId
        )
          conv.nonLetti++;
      }
    });
  }

  const totalNonLetti = computed(() =>
    conversazioni.value.reduce((s, c) => s + c.nonLetti, 0),
  );

  return {
    conversazioni,
    connesso,
    dismissed,
    totalNonLetti,
    carica,
    apri,
    invia,
    modifica,
    elimina,
    rimuovi,
    saveDismissed: _saveDismissed,
    connetti,
    disconnetti,
    setActive: (id: number | null) => {
      _activeId.value = id;
    },
  };
});
