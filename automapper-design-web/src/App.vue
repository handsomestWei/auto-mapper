<script setup>
import { watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
watch(
  () => route.meta.title,
  (t) => {
    document.title = t ? `${t} · automapper-design-web` : 'automapper-design-web'
  },
  { immediate: true }
)
</script>

<template>
  <div class="shell">
    <header class="site-nav">
      <router-link
        to="/"
        class="brand"
      >
        automapper-design-web
      </router-link>
      <nav class="links">
        <router-link to="/">
          首页
        </router-link>
        <router-link to="/schema">
          Schema 设计器
        </router-link>
        <router-link to="/rule">
          字段转换规则设计器
        </router-link>
      </nav>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
}
.site-nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 10px 1.25rem;
  border-bottom: 1px solid var(--border);
  background: var(--panel);
}
.brand {
  font-weight: 700;
  font-size: 15px;
  color: var(--text);
  text-decoration: none;
}
.brand:hover {
  color: var(--accent);
}
.links {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px 14px;
  margin-left: auto;
}
.links a {
  font-size: 13px;
  color: var(--muted);
  text-decoration: none;
  padding: 4px 0;
}
.links a:hover {
  color: var(--accent);
}
.links a.router-link-active {
  color: var(--accent);
  font-weight: 600;
}
.main {
  min-height: 0;
}
</style>
