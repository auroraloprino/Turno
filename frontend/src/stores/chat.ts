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
  immagineUrl: string | null;
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
      .map((c) => {
        const esistente = existingMap.get(c.id);
        // tengo messaggi e non letti ma aggiorno nome e membri, che cambiano quando qualcuno entra o esce
        return esistente
          ? Object.assign(esistente, { nome: c.nome, partecipanti: c.partecipanti })
          : { ...c, messaggi: [], nonLetti: 0 };
      });
    data.forEach((c) => _subscribe(c.id));
  }

  async function apri(id: number) {
    _activeId.value = id;
    const conv = conversazioni.value.find((c) => c.id === id);
    if (!conv) return;
    const msgs = await api.get<Messaggio[]>(
      `/api/chat/conversazioni/${id}/messaggi`,
    );
    // il backend già li esclude, qui è solo una gguardai in più
    conv.messaggi = msgs.filter((m) => !m.eliminato);
    conv.nonLetti = 0;
  }

  async function invia(conversazioneId: number, testo: string) {
    const msg = await api.post<Messaggio>(
      `/api/chat/conversazioni/${conversazioneId}/messaggi`,
      { testo },
    );
    _upsertMsg(msg);
  }

  async function inviaImmagine(conversazioneId: number, file: File, testo: string) {
    const msg = await api.upload<Messaggio>(
      `/api/chat/conversazioni/${conversazioneId}/immagini`,
      file,
      testo ? { testo } : {},
    );
    _upsertMsg(msg);
  }

  async function creaGruppo(nome: string, userIds: number[]) {
    const conv = await api.post<Omit<Conversazione, "messaggi" | "nonLetti">>(
      "/api/chat/conversazioni/gruppo",
      { nome, userIds },
    );
    if (!conversazioni.value.some((c) => c.id === conv.id))
      conversazioni.value.push({ ...conv, messaggi: [], nonLetti: 0 });
    _subscribe(conv.id);
    return conv.id;
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

  async function aggiungiMembri(id: number, userIds: number[]) {
    const conv = await api.post<Omit<Conversazione, "messaggi" | "nonLetti">>(
      `/api/chat/conversazioni/${id}/partecipanti`,
      { userIds },
    );
    const locale = conversazioni.value.find((c) => c.id === id);
    if (locale) locale.partecipanti = conv.partecipanti;
  }

  async function abbandona(id: number) {
    await api.delete(`/api/chat/conversazioni/${id}/partecipanti/me`);
    const idx = conversazioni.value.findIndex((c) => c.id === id);
    if (idx >= 0) conversazioni.value.splice(idx, 1);
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
        // quando qualcuno mi aggiunge a un gruppo ricarico la lista, così compare senza aggiornare la pagina
        client!.subscribe(`/topic/notifiche/${auth.user!.id}`, (frame) => {
          if (JSON.parse(frame.body).tipo === "gruppo") carica();
        });
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
    inviaImmagine,
    creaGruppo,
    modifica,
    elimina,
    rimuovi,
    abbandona,
    aggiungiMembri,
    saveDismissed: _saveDismissed,
    connetti,
    disconnetti,
    setActive: (id: number | null) => {
      _activeId.value = id;
    },
  };
});
