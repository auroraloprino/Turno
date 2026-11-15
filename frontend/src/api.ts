import { useAuthStore } from "@/stores/auth";

async function send(path: string, options: RequestInit = {}, json = true): Promise<Response> {
  const auth = useAuthStore();

  const res = await fetch(path, {
    ...options,
    headers: {
      // con FormData il Content-Type lo mette il browser, serve il boundary del multipart
      ...(json ? { "Content-Type": "application/json" } : {}),
      ...(auth.token ? { Authorization: `Bearer ${auth.token}` } : {}),
      ...options.headers,
    },
  });

  if (res.status === 401) {
    if (window.location.pathname !== "/login") {
      auth.logout();
      window.location.href = "/login";
    }
    throw new Error("Sessione scaduta");
  }

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.message ?? `HTTP ${res.status}`);
  }

  return res;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await send(path, options);
  if (res.status === 204) return undefined as T;
  return res.json();
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "POST", body: JSON.stringify(body) }),
  patch: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "PATCH", body: JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
  upload: async <T>(path: string, file: File): Promise<T> => {
    const form = new FormData();
    form.append("file", file);
    return (await send(path, { method: "POST", body: form }, false)).json();
  },
  // un semplice <a href> non manderebbe il token, quindi scarico il file e lo apro da un blob
  blob: async (path: string): Promise<Blob> => (await send(path, {}, false)).blob(),
};
