<template>
  <button
    class="tema-toggle"
    :title="tema === 'dark' ? 'Passa al tema chiaro' : 'Passa al tema scuro'"
    :aria-label="tema === 'dark' ? 'Passa al tema chiaro' : 'Passa al tema scuro'"
    @click="cambia"
  >
    <svg v-if="tema === 'dark'" viewBox="0 0 16 16" fill="none">
      <circle cx="8" cy="8" r="3" stroke="currentColor" stroke-width="1.3" />
      <path
        d="M8 1.5v1.5M8 13v1.5M1.5 8H3M13 8h1.5M3.4 3.4l1 1M11.6 11.6l1 1M3.4 12.6l1-1M11.6 4.4l1-1"
        stroke="currentColor"
        stroke-width="1.3"
        stroke-linecap="round"
      />
    </svg>
    <svg v-else viewBox="0 0 16 16" fill="none">
      <path
        d="M13.5 9.5A5.5 5.5 0 0 1 6.5 2.5a5.5 5.5 0 1 0 7 7z"
        stroke="currentColor"
        stroke-width="1.3"
        stroke-linejoin="round"
      />
    </svg>
  </button>
</template>

<script setup lang="ts">
import { useTema } from "@/composables/useTema";

const { tema, cambia } = useTema();
</script>

<style scoped>
.tema-toggle {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: 0.5px solid var(--border-md);
  border-radius: 50%;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.12s, background 0.12s;
}
.tema-toggle:hover {
  color: var(--text-primary);
  background: var(--bg-tertiary);
}
.tema-toggle svg {
  width: 16px;
  height: 16px;
}
</style>
