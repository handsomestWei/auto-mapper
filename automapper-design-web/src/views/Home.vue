<script setup></script>

<template>
  <div class="home">
    <header class="home-head">
      <h1 class="home-title">
        automapper-design-web
      </h1>
      <p class="home-sub">
        配置驱动的 JSON 映射与可视化编辑
      </p>
    </header>
    <nav class="home-cards">
      <router-link
        class="card"
        to="/schema"
      >
        <span class="card-title">Schema 设计器</span>
        <span class="card-desc">从 JSON 样例生成 schema XML，可视化编辑字段树</span>
      </router-link>
      <router-link
        class="card"
        to="/rule"
      >
        <span class="card-title">字段转换规则设计器</span>
        <span class="card-desc">对照 schema 树编写 rule XML 映射条目</span>
      </router-link>
    </nav>
    <footer class="home-foot">
      <a
        class="foot-link"
        href="https://github.com/handsomestWei"
        target="_blank"
        rel="noopener noreferrer"
      >作者 GitHub</a>
      <span class="foot-sep">·</span>
      <a
        class="foot-link"
        href="https://github.com/handsomestWei/auto-mapper"
        target="_blank"
        rel="noopener noreferrer"
      >本仓库 auto-mapper</a>
    </footer>
  </div>
</template>

<style scoped>
.home {
  max-width: 880px;
  margin: 0 auto;
  padding: 2.5rem 1.25rem 3rem;
}
.home-head {
  margin-bottom: 2rem;
  text-align: center;
}
.home-title {
  font-size: 1.65rem;
  font-weight: 700;
  margin: 0 0 0.5rem;
  color: var(--text);
  letter-spacing: -0.02em;
}
.home-sub {
  margin: 0;
  font-size: 14px;
  color: var(--muted);
  line-height: 1.5;
}
.home-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 720px) {
  .home-cards {
    grid-template-columns: 1fr;
  }
}
.card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 1.25rem 1.35rem;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--panel);
  text-decoration: none;
  color: inherit;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease,
    background 0.15s ease;
}
.card:hover {
  border-color: var(--accent);
  background: #f0f5ff;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.12);
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}
.card-desc {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.45;
}
.home-foot {
  margin-top: 2.25rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--border);
  text-align: center;
  font-size: 13px;
}
.foot-link {
  color: var(--accent);
  text-decoration: none;
}
.foot-link:hover {
  text-decoration: underline;
}
.foot-sep {
  margin: 0 0.5rem;
  color: var(--muted);
}
</style>
