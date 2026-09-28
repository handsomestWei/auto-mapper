<script setup>
defineProps({
  title: { type: String, required: true },
  badge: { type: String, default: '' },
  variant: {
    type: String,
    default: 'schema',
    validator: (v) => v === 'schema' || v === 'rule'
  },
  hints: { type: Array, default: () => [] }
})
</script>

<template>
  <header class="page-header">
    <span
      class="page-header-icon"
      :class="variant"
    >
      <svg
        v-if="variant === 'schema'"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          fill="currentColor"
          d="M7 3h7.2L19 7.8V20a1.5 1.5 0 0 1-1.5 1.5h-10A1.5 1.5 0 0 1 6 20V4.5A1.5 1.5 0 0 1 7.5 3H7zm7.2 1.6V8H18l-3.8-3.4zM8.4 11.2h7.2v1.5H8.4v-1.5zm0 3h7.2v1.5H8.4V14.2zm0 3h5.2v1.5H8.4V17.2z"
        />
      </svg>
      <svg
        v-else
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          fill="currentColor"
          d="M5 6a2.2 2.2 0 1 1 0 4.4A2.2 2.2 0 0 1 5 6Zm0 7.6a2.2 2.2 0 1 1 0 4.4 2.2 2.2 0 0 1 0-4.4ZM19 6a2.2 2.2 0 1 1 0 4.4A2.2 2.2 0 0 1 19 6Zm0 7.6a2.2 2.2 0 1 1 0 4.4 2.2 2.2 0 0 1 0-4.4ZM8.6 7.7h4.8l-1.2-1.2 1.1-1.1 3.1 3.1-3.1 3.1-1.1-1.1 1.2-1.2H8.6V7.7Zm0 7.6h4.8l-1.2-1.2 1.1-1.1 3.1 3.1-3.1 3.1-1.1-1.1 1.2-1.2H8.6v-1.6Z"
        />
      </svg>
    </span>
    <div class="page-header-copy">
      <div class="page-header-title-row">
        <h1>{{ title }}</h1>
        <span
          v-if="badge"
          class="page-header-badge"
          :class="variant"
        >{{ badge }}</span>
      </div>
      <p class="page-header-desc">
        <slot />
      </p>
      <ul
        v-if="hints.length"
        class="page-header-hints"
        :class="variant"
      >
        <li
          v-for="hint in hints"
          :key="hint"
        >
          {{ hint }}
        </li>
      </ul>
    </div>
  </header>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 12px 14px 13px;
  border: 1px solid #e0e7ff;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(238, 242, 255, 0.78), rgba(255, 255, 255, 0.94));
}
.page-header-icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  flex-shrink: 0;
}
.page-header-icon svg {
  width: 20px;
  height: 20px;
}
.page-header-icon.schema {
  color: #4f46e5;
  background: #eef2ff;
}
.page-header-icon.rule {
  color: #0891b2;
  background: #ecfeff;
}
.page-header-copy {
  min-width: 0;
  flex: 1;
}
.page-header-title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 10px;
}
.page-header-title-row h1 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: #0f172a;
  line-height: 1.25;
}
.page-header-badge {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
}
.page-header-badge.schema {
  color: #3730a3;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
}
.page-header-badge.rule {
  color: #155e75;
  background: #ecfeff;
  border: 1px solid #a5f3fc;
}
.page-header-desc {
  margin: 6px 0 0;
  max-width: 64rem;
  font-size: 13px;
  line-height: 1.55;
  color: var(--muted);
}
.page-header-hints {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 8px 0 0;
  padding: 0;
  list-style: none;
}
.page-header-hints li {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  font-size: 12px;
  color: #4338ca;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid #e0e7ff;
}
.page-header-hints li::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #818cf8;
}
.page-header-hints.rule li {
  color: #155e75;
  border-color: #a5f3fc;
}
.page-header-hints.rule li::before {
  background: #22d3ee;
}
</style>
