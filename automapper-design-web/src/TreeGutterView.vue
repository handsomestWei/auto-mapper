<script setup>
/** 可滚动外框；行号由各 TreeNode 行内的 .tln 展示（无左侧整块 gutter）。 */
</script>

<template>
  <div class="tgw">
    <div class="tinner">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.tgw {
  display: flex;
  min-height: 0;
  flex: 1;
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 6px;
  overflow: hidden;
  background: var(--code-bg);
}
.tinner {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 6px 8px;
}
</style>
