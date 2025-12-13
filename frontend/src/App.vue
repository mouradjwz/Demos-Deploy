<script setup lang="ts">
import { RouterView } from 'vue-router'
import Footer from "@/components/page/Footer.vue"
import HeaderView from "@/views/HeaderView.vue"
</script>

<template>
  <div id="app">
    <HeaderView />
    <main class="main-content">
      <RouterView />
    </main>
    <Footer />
  </div>
</template>

<style>
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: system-ui, sans-serif;
}

/* Belangrijk: dit zorgt dat de footer onderaan blijft */
#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* duwt de footer naar beneden */
.main-content {
  flex: 1;
}
</style>
