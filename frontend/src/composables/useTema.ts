import { ref } from "vue";

export type Tema = "dark" | "light";

const CHIAVE = "tema";

function iniziale(): Tema {
  try {
    const salvato = localStorage.getItem(CHIAVE);
    if (salvato === "dark" || salvato === "light") return salvato;
  } catch {
    // senza localStorage seguo il sistema
  }
  // nessuna scelta salvata, segue l'impostazione del sistema operativo
  return window.matchMedia("(prefers-color-scheme: light)").matches ? "light" : "dark";
}

const tema = ref<Tema>(iniziale());

function applica(t: Tema) {
  document.documentElement.dataset.theme = t;
}

// chiamata in main.ts prima del mount, così la pagina flasha
export function applicaTemaIniziale() {
  applica(tema.value);
}

export function useTema() {
  function cambia() {
    tema.value = tema.value === "dark" ? "light" : "dark";
    applica(tema.value);
    try {
      localStorage.setItem(CHIAVE, tema.value);
    } catch {
      // la scelta vale solo per questa sessione
    }
  }
  return { tema, cambia };
}
