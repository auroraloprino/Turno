import { api } from "@/api";

interface Utente {
  id: number;
  name: string;
  email: string;
}

let perId = new Map<number, Utente>();
let caricamento: Promise<void> | null = null;

function carica(forza = false): Promise<void> {
  if (caricamento && !forza) return caricamento;
  caricamento = api
    .get<Utente[]>("/api/users")
    .then((lista) => {
      perId = new Map(lista.map((u) => [u.id, u]));
    })
    .catch(() => {
      // se fallisce riprovo alla prossima chiamata invece di tenermi l'errore per sempre
      caricamento = null;
    });
  return caricamento;
}

// ogni microservizio conosce solo lo userId, il nome sta in auth-service, quindi l'unione la faccio qui
export async function conNomi<T extends { userId: number }>(lista: T[]): Promise<T[]> {
  await carica();
  // un utente appena creato non è ancora in cache, ricarico una volta sola
  if (lista.some((x) => !perId.has(x.userId))) await carica(true);
  return lista.map((x) => {
    const u = perId.get(x.userId);
    return { ...x, userName: u?.name ?? `Utente #${x.userId}`, userEmail: u?.email ?? "" };
  });
}
