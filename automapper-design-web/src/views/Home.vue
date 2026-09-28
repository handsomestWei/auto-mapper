<script setup>
defineOptions({ name: 'HomePage' })

import heroImg from '../assets/hero-mapping.png'
</script>

<template>
  <div class="home">
    <section class="hero">
      <div class="hero-copy">
        <p class="badge">
          <span class="badge-dot" />
          配置驱动 · JSON 映射
        </p>
        <h1 class="hero-title">
          可视化编辑 Schema
          <span>与字段映射规则</span>
        </h1>
        <p class="hero-lead">
          从 JSON 样例生成可读的 schema XML，对照字段树编写 rule，导出后即可交给
          <strong>automapper-core</strong> 加载执行。
        </p>
        <div class="hero-actions">
          <router-link
            class="btn-cta"
            to="/schema"
          >
            <svg
              class="btn-ico"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                fill="currentColor"
                d="M7 3h10a2 2 0 0 1 2 2v3H5V5a2 2 0 0 1 2-2Zm-2 7h16v9a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-9Z"
              />
            </svg>
            打开 Schema 设计器
          </router-link>
          <router-link
            class="btn-ghost"
            to="/rule"
          >
            编写转换规则
            <span class="arrow">→</span>
          </router-link>
        </div>
        <ul class="hero-points">
          <li>JSONPath 字段路径</li>
          <li>内置映射函数</li>
          <li>导出即用 XML</li>
        </ul>
      </div>
      <div class="hero-visual">
        <img
          class="hero-img"
          :src="heroImg"
          alt="JSON 字段树与目标结构之间的映射示意"
        >
      </div>
    </section>

    <section class="cards">
      <router-link
        class="card"
        to="/schema"
      >
        <span class="card-icon schema">
          <svg
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              fill="currentColor"
              d="M7 3h7.2L19 7.8V20a1.5 1.5 0 0 1-1.5 1.5h-10A1.5 1.5 0 0 1 6 20V4.5A1.5 1.5 0 0 1 7.5 3H7zm7.2 1.6V8H18l-3.8-3.4zM8.4 11.2h7.2v1.5H8.4v-1.5zm0 3h7.2v1.5H8.4V14.2zm0 3h5.2v1.5H8.4V17.2z"
            />
          </svg>
        </span>
        <span class="card-kicker">结构</span>
        <span class="card-title">Schema 设计器</span>
        <span class="card-desc">从 JSON 样例生成 schema XML，可视化编辑字段树、类型与描述。</span>
        <span class="card-go">进入设计器 <span class="arrow">→</span></span>
      </router-link>
      <router-link
        class="card"
        to="/rule"
      >
        <span class="card-icon rule">
          <svg
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              fill="currentColor"
              d="M5 6a2.2 2.2 0 1 1 0 4.4A2.2 2.2 0 0 1 5 6Zm0 7.6a2.2 2.2 0 1 1 0 4.4 2.2 2.2 0 0 1 0-4.4ZM19 6a2.2 2.2 0 1 1 0 4.4A2.2 2.2 0 0 1 19 6Zm0 7.6a2.2 2.2 0 1 1 0 4.4 2.2 2.2 0 0 1 0-4.4ZM8.6 7.7h4.8l-1.2-1.2 1.1-1.1 3.1 3.1-3.1 3.1-1.1-1.1 1.2-1.2H8.6V7.7Zm0 7.6h4.8l-1.2-1.2 1.1-1.1 3.1 3.1-3.1 3.1-1.1-1.1 1.2-1.2H8.6v-1.6Z"
            />
          </svg>
        </span>
        <span class="card-kicker">映射</span>
        <span class="card-title">字段转换规则设计器</span>
        <span class="card-desc">对照 schema 树编写 from / to / func 规则，并预览映射结果。</span>
        <span class="card-go">进入设计器 <span class="arrow">→</span></span>
      </router-link>
    </section>

    <section class="flow">
      <h2 class="flow-title">
        三步完成一次转换配置
      </h2>
      <ol class="flow-list">
        <li class="flow-item">
          <span class="flow-num">01</span>
          <strong>准备 JSON 样例</strong>
          <p>用一份目标形态的 JSON，推导字段树与类型。</p>
        </li>
        <li class="flow-item">
          <span class="flow-num">02</span>
          <strong>生成 Schema XML</strong>
          <p>在设计器里调整结构，导出可读、可版本管理的 schema。</p>
        </li>
        <li class="flow-item">
          <span class="flow-num">03</span>
          <strong>编写 Rule XML</strong>
          <p>用 JSONPath 声明字段对应，必要时挂上映射函数。</p>
        </li>
      </ol>
    </section>

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
  max-width: 1080px;
  margin: 0 auto;
  padding: 2.25rem 1.25rem 3rem;
}
.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 1fr);
  gap: 28px 36px;
  align-items: center;
  margin-bottom: 2.25rem;
}
.badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 14px;
  padding: 5px 11px 5px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #3730a3;
  background: rgba(238, 242, 255, 0.9);
  border: 1px solid #c7d2fe;
}
.badge-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22d3ee;
  box-shadow: 0 0 0 4px rgba(34, 211, 238, 0.18);
}
.hero-title {
  margin: 0 0 12px;
  font-size: clamp(1.65rem, 3vw, 2.25rem);
  font-weight: 700;
  letter-spacing: -0.035em;
  line-height: 1.22;
  color: #0f172a;
}
.hero-title span {
  display: block;
}
.hero-lead {
  margin: 0 0 22px;
  max-width: 36rem;
  font-size: 15px;
  line-height: 1.7;
  color: var(--muted);
}
.hero-lead strong {
  color: var(--text);
  font-weight: 600;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}
.btn-cta,
.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
}
.btn-cta {
  color: #fff;
  background: linear-gradient(180deg, #4f46e5 0%, #4338ca 100%);
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.28);
}
.btn-cta:hover {
  filter: brightness(1.06);
}
.btn-ico {
  width: 16px;
  height: 16px;
}
.btn-ghost {
  color: #3730a3;
  background: #fff;
  border: 1px solid #c7d2fe;
}
.btn-ghost:hover {
  background: #eef2ff;
}
.arrow {
  font-weight: 500;
}
.hero-points {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin: 0;
  padding: 0;
  list-style: none;
  font-size: 13px;
  color: var(--muted);
}
.hero-points li {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}
.hero-points li::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #818cf8;
}
.hero-visual {
  position: relative;
}
.hero-img {
  display: block;
  width: 100%;
  height: auto;
  border-radius: 20px;
  border: 1px solid rgba(199, 210, 254, 0.7);
  box-shadow:
    0 20px 50px rgba(79, 70, 229, 0.14),
    0 2px 0 rgba(255, 255, 255, 0.8) inset;
  background: #eef2ff;
}
.cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 2.5rem;
}
.card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 1.3rem 1.35rem 1.2rem;
  border: 1px solid rgba(199, 210, 254, 0.85);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  text-decoration: none;
  color: inherit;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}
.card:hover {
  transform: translateY(-3px);
  border-color: #818cf8;
  box-shadow: 0 16px 36px rgba(79, 70, 229, 0.14);
}
.card-icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  margin-bottom: 4px;
}
.card-icon svg {
  width: 20px;
  height: 20px;
}
.card-icon.schema {
  color: #4f46e5;
  background: #eef2ff;
}
.card-icon.rule {
  color: #0891b2;
  background: #ecfeff;
}
.card-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6366f1;
}
.card-title {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}
.card-desc {
  font-size: 13px;
  color: var(--muted);
  line-height: 1.55;
}
.card-go {
  margin-top: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #4f46e5;
}
.flow {
  padding: 1.35rem 1.4rem 1.45rem;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(238, 242, 255, 0.7), rgba(255, 255, 255, 0.9));
  border: 1px solid #e0e7ff;
}
.flow-title {
  margin: 0 0 1rem;
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}
.flow-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin: 0;
  padding: 0;
  list-style: none;
}
.flow-item {
  padding: 12px 12px 4px 0;
}
.flow-num {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #818cf8;
}
.flow-item strong {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #0f172a;
}
.flow-item p {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: var(--muted);
}
.home-foot {
  margin-top: 2.25rem;
  padding-top: 1.25rem;
  border-top: 1px solid #e5e7eb;
  text-align: center;
  font-size: 13px;
}
.foot-link {
  color: #4f46e5;
  text-decoration: none;
}
.foot-link:hover {
  text-decoration: underline;
}
.foot-sep {
  margin: 0 0.5rem;
  color: var(--muted);
}
@media (max-width: 720px) {
  .hero,
  .cards,
  .flow-list {
    grid-template-columns: 1fr;
  }
  .hero-img {
    max-height: 280px;
    object-fit: cover;
    object-position: center;
  }
}
</style>
