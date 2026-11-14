<template>
  <div class="card">
    <div class="card-title">Utenti</div>
    <div v-if="loading" class="empty-state">Caricamento…</div>
    <div v-else-if="utenti.length === 0" class="empty-state">
      Nessun utente.
    </div>
    <div v-for="u in utenti" :key="u.id" class="user-row">
      <div class="avatar">{{ initials(u.name) }}</div>
      <div class="user-info">
        <div class="user-name">{{ u.name }}</div>
        <div class="user-role">{{ u.email }}</div>
      </div>
      <input
        v-if="u.role !== 'OWNER'"
        v-model="drafts[u.id]"
        class="team-input"
        :class="{ saved: savedId === u.id }"
        list="team-options"
        maxlength="60"
        placeholder="Team…"
        title="Team (es. Dev, Testing...). Usato per raggruppare il planner."
        @change="salvaTeam(u)"
        @keydown.enter="($event.target as HTMLInputElement).blur()"
      />
      <span class="pill pill-online">{{ u.role }}</span>
    </div>
    <datalist id="team-options">
      <option v-for="t in teams" :key="t" :value="t" />
    </datalist>
    <div v-if="error" class="team-error">{{ error }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { api } from "@/api";

interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: string;
  team: string | null;
}

const utenti = ref<UserResponse[]>([]);
const drafts = ref<Record<number, string>>({});
const loading = ref(false);
const error = ref("");
const savedId = ref<number | null>(null);

const teams = computed(() =>
  [
    ...new Set(utenti.value.map((u) => u.team).filter(Boolean) as string[]),
  ].sort((a, b) => a.localeCompare(b, "it")),
);

function initials(name: string) {
  return name
    .split(" ")
    .map((w) => w[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
}

async function salvaTeam(u: UserResponse) {
  error.value = "";
  const value = (drafts.value[u.id] ?? "").trim();
  // se cambia solo maiuscole/minuscole riuso la grafia già esistente ("cucina" -> "Cucina")
  const existing = teams.value.find(
    (t) => t.toLowerCase() === value.toLowerCase(),
  );
  const team = existing ?? value;
  if (team === (u.team ?? "")) {
    drafts.value[u.id] = team;
    return;
  }
  try {
    const updated = await api.patch<UserResponse>(`/api/users/${u.id}/team`, {
      team,
    });
    u.team = updated.team;
    drafts.value[u.id] = updated.team ?? "";
    savedId.value = u.id;
    setTimeout(() => {
      if (savedId.value === u.id) savedId.value = null;
    }, 1200);
  } catch (e) {
    drafts.value[u.id] = u.team ?? "";
    error.value = e instanceof Error ? e.message : "Errore nel salvataggio.";
  }
}

onMounted(async () => {
  loading.value = true;
  try {
    utenti.value = await api.get<UserResponse[]>("/api/users");
    drafts.value = Object.fromEntries(
      utenti.value.map((u) => [u.id, u.team ?? ""]),
    );
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.empty-state {
  font-size: 13px;
  color: var(--text-tertiary);
  padding: 8px 0;
}
.team-input {
  width: 130px;
  margin-right: 8px;
  background: var(--bg-tertiary);
  border: 0.5px solid var(--border-md);
  color: var(--text-primary);
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 12px;
  transition: border-color 0.2s;
}
.team-input::placeholder {
  color: var(--text-tertiary);
}
.team-input.saved {
  border-color: var(--green-dark);
}
.team-error {
  font-size: 12px;
  color: var(--coral-dark);
  margin-top: 8px;
}
</style>
