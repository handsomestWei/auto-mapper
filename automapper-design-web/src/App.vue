<script setup>
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import brandMark from './assets/brand-mark.png'

const route = useRoute()
const isHome = computed(() => route.path === '/')

watch(
  () => route.meta.title,
  (t) => {
    document.title = t ? `${t} · automapper-design-web` : 'automapper-design-web'
  },
  { immediate: true }
)
</script>

<template>
  <div
    class="shell"
    :class="{ 'is-home': isHome }"
  >
    <header class="site-nav">
      <router-link
        to="/"
        class="brand"
      >
        <img
          class="brand-mark"
          :src="brandMark"
          width="32"
          height="32"
          alt=""
        >
        <span class="brand-copy">
          <span class="brand-name">AutoMapper</span>
          <span class="brand-sub">design-web</span>
        </span>
      </router-link>
      <nav class="links">
        <router-link
          to="/"
          class="nav-link"
        >
          <svg
            class="nav-ico"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              fill="currentColor"
              d="M12 3.2 3.8 10h1.7v9.2c0 .6.5 1.1 1.1 1.1H9.8v-5.6h4.4v5.6h3.2c.6 0 1.1-.5 1.1-1.1V10h1.7L12 3.2Z"
            />
          </svg>
          首页
        </router-link>
        <router-link
          to="/schema"
          class="nav-link"
        >
          <svg
            class="nav-ico"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              fill="currentColor"
              d="M7 3h10a2 2 0 0 1 2 2v3H5V5a2 2 0 0 1 2-2Zm-2 7h16v9a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-9Zm4 2.5a1 1 0 1 0 0 2h6a1 1 0 1 0 0-2H9Zm0 3.5a1 1 0 1 0 0 2h4a1 1 0 1 0 0-2H9Z"
            />
          </svg>
          Schema 设计器
        </router-link>
        <router-link
          to="/rule"
          class="nav-link"
        >
          <svg
            class="nav-ico"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              fill="currentColor"
              d="M4 6.5A2.5 2.5 0 1 1 8.9 7H15a3 3 0 0 1 3 3v.1A2.5 2.5 0 1 1 16 12.5V10a1 1 0 0 0-1-1H8.9A2.5 2.5 0 0 1 4 6.5Zm12.5 8A2.5 2.5 0 1 0 19 16.9V18a3 3 0 0 1-3 3H8.9A2.5 2.5 0 1 0 7 19.5V18a1 1 0 0 1 1-1h7.1a2.5 2.5 0 0 0 1.4-2.5Z"
            />
          </svg>
          字段转换规则
        </router-link>
      </nav>
      <a
        class="gh-link"
        href="https://github.com/handsomestWei/auto-mapper"
        target="_blank"
        rel="noopener noreferrer"
        title="GitHub 仓库"
      >
        <svg
          class="gh-ico"
          viewBox="0 0 24 24"
          aria-hidden="true"
        >
          <path
            fill="currentColor"
            d="M12 .5C5.37.5 0 5.87 0 12.5c0 5.3 3.44 9.8 8.2 11.38.6.11.82-.26.82-.58v-2.03c-3.34.73-4.04-1.61-4.04-1.61-.55-1.39-1.34-1.76-1.34-1.76-1.09-.75.08-.73.08-.73 1.2.08 1.84 1.24 1.84 1.24 1.07 1.83 2.81 1.3 3.5.99.11-.78.42-1.3.76-1.6-2.66-.3-5.47-1.33-5.47-5.93 0-1.31.47-2.38 1.24-3.22-.12-.3-.54-1.52.12-3.18 0 0 1.01-.32 3.3 1.23a11.5 11.5 0 0 1 6 0c2.29-1.55 3.3-1.23 3.3-1.23.66 1.66.24 2.88.12 3.18.77.84 1.24 1.91 1.24 3.22 0 4.61-2.81 5.62-5.49 5.92.43.37.81 1.1.81 2.22v3.29c0 .32.22.7.82.58A12.01 12.01 0 0 0 24 12.5C24 5.87 18.63.5 12 .5Z"
          />
        </svg>
        <span class="gh-text">GitHub</span>
      </a>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  background: var(--bg);
}
.shell.is-home {
  background:
    radial-gradient(1200px 520px at 12% -10%, rgba(99, 102, 241, 0.16), transparent 55%),
    radial-gradient(900px 480px at 92% 8%, rgba(34, 211, 238, 0.16), transparent 50%),
    linear-gradient(180deg, #f5f7ff 0%, #ffffff 42%);
}
.site-nav {
  position: sticky;
  top: 0;
  z-index: 40;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 16px;
  min-height: 58px;
  padding: 8px 1.25rem;
  border-bottom: 1px solid rgba(208, 213, 221, 0.75);
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(14px);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.7) inset, 0 8px 24px rgba(15, 23, 42, 0.04);
}
.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--text);
}
.brand-mark {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  object-fit: cover;
  box-shadow: 0 4px 10px rgba(79, 70, 229, 0.18);
}
.brand-copy {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}
.brand-name {
  font-weight: 700;
  font-size: 15px;
  letter-spacing: -0.03em;
}
.brand-sub {
  font-size: 11px;
  color: var(--muted);
  font-weight: 500;
}
.links {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-left: auto;
}
.nav-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 11px;
  border-radius: 999px;
  font-size: 13px;
  color: var(--muted);
  text-decoration: none;
  border: 1px solid transparent;
  transition:
    color 0.15s ease,
    background 0.15s ease,
    border-color 0.15s ease;
}
.nav-link:hover {
  color: var(--text);
  background: #eef2ff;
}
.nav-link.router-link-active {
  color: #3730a3;
  background: #eef2ff;
  border-color: #c7d2fe;
  font-weight: 600;
}
.nav-ico,
.gh-ico {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}
.gh-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  color: var(--muted);
  text-decoration: none;
  font-size: 13px;
  border: 1px solid var(--border);
  background: #fff;
}
.gh-link:hover {
  color: var(--text);
  border-color: #c7d2fe;
  background: #f8fafc;
}
@media (max-width: 720px) {
  .links {
    width: 100%;
    margin-left: 0;
    order: 3;
  }
  .gh-link {
    margin-left: auto;
  }
  .gh-text {
    display: none;
  }
}
.main {
  min-height: 0;
}
</style>
