<template>
  <div class="chat-page">
    <ChatPanel />
  </div>
</template>

<script setup lang="ts">
import ChatPanel from "@/components/ChatPanel.vue";
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 100px);
  min-height: 420px;
}
</style>
