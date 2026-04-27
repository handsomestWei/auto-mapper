<script setup>
import { ref, nextTick, watch, computed, onMounted } from 'vue'

const props = defineProps({
  lineCount: { type: Number, default: 1 },
  minRows: { type: Number, default: 8 }
})

const gutter = ref(null)
const inner = ref(null)

const nLines = computed(() => Math.max(props.lineCount, props.minRows, 1))
const lineNums = computed(() => Array.from({ length: nLines.value }, (_, i) => i + 1))

function scrollTree() {
  if (gutter.value && inner.value) {
    gutter.value.scrollTop = inner.value.scrollTop
  }
}

onMounted(() => nextTick(() => scrollTree()))
watch(
  () => [props.lineCount, props.minRows],
  () => nextTick(() => scrollTree())
)
defineExpose({ inner })
</script>

<template>
  <div class="tgw">
    <div
      ref="gutter"
      class="tgutter"
      aria-hidden="true"
    >
      <div
        v-for="k in lineNums"
        :key="k"
        class="tln"
      >
        {{ k }}
      </div>
    </div>
    <div
      ref="inner"
      class="tinner"
      @scroll="scrollTree"
    >
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
.tgutter {
  flex: 0 0 2.2rem;
  padding: 6px 4px 6px 6px;
  text-align: right;
  line-height: 1.45;
  font-size: 11px;
  color: var(--gutter);
  user-select: none;
  font-family: ui-monospace, Menlo, monospace;
  border-right: 1px solid var(--border);
  background: var(--gutter-bg);
  overflow: hidden;
}
.tln {
  min-height: calc(1.45 * 1em);
  padding-right: 2px;
}
.tinner {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 6px 8px;
}
</style>
