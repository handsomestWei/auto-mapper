<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  readOnly: { type: Boolean, default: false },
  minRows: { type: Number, default: 12 },
  monosize: { type: String, default: '12px' }
})

const emit = defineEmits(['update:modelValue', 'input'])

const ta = ref(null)
const gutter = ref(null)

const lines = computed(() => {
  const t = props.modelValue
  if (!t) return 1
  return t.split('\n').length
})

const lineCount = computed(() => Math.max(lines.value, props.minRows, 1))

const lineList = computed(() => {
  const n = lineCount.value
  return Array.from({ length: n }, (_, i) => i + 1)
})

function onInput(e) {
  if (props.readOnly) return
  const v = e.target.value
  emit('update:modelValue', v)
  emit('input', e)
}

function syncGutterSize() {
  if (gutter.value && ta.value) {
    gutter.value.style.minHeight = `${ta.value.scrollHeight}px`
  }
}

function onScroll() {
  if (gutter.value && ta.value) {
    gutter.value.scrollTop = ta.value.scrollTop
  }
}

defineExpose({ focus: () => ta.value?.focus() })

onMounted(() => {
  nextTick(() => {
    syncGutterSize()
    onScroll()
  })
})

watch(
  () => props.modelValue,
  () => {
    nextTick(() => {
      syncGutterSize()
      onScroll()
    })
  }
)
</script>

<template>
  <div class="gwrap">
    <div
      ref="gutter"
      class="gutter"
      :style="{ fontSize: monosize }"
      aria-hidden="true"
    >
      <div
        v-for="n in lineList"
        :key="n"
        class="ln"
      >
        {{ n }}
      </div>
    </div>
    <textarea
      ref="ta"
      class="gta"
      :class="{ readonly: readOnly }"
      :style="{ fontSize: monosize }"
      :value="modelValue"
      :readOnly="readOnly"
      spellcheck="false"
      @input="onInput"
      @scroll="onScroll"
    />
  </div>
</template>

<style scoped>
.gwrap {
  display: flex;
  flex: 1;
  min-height: 0;
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 6px;
  overflow: hidden;
  background: var(--code-bg);
}
.gutter {
  flex-shrink: 0;
  width: 3.2rem;
  min-height: 0;
  max-height: 100%;
  padding: 8px 6px 8px 8px;
  text-align: right;
  line-height: 1.45;
  color: var(--gutter);
  user-select: none;
  font-family: ui-monospace, 'Cascadia Code', 'Fira Code', Menlo, monospace;
  overflow-y: auto;
  overflow-x: hidden;
  border-right: 1px solid var(--border);
  background: var(--gutter-bg);
}
.ln {
  min-height: calc(1.45 * 1em);
}
.gta {
  flex: 1;
  min-width: 0;
  min-height: 200px;
  margin: 0;
  border: 0;
  padding: 8px 10px;
  line-height: 1.45;
  resize: none;
  font-family: ui-monospace, 'Cascadia Code', 'Fira Code', Menlo, monospace;
  color: var(--text);
  background: var(--code-bg);
  outline: none;
}
.gta::placeholder {
  color: var(--muted);
}
.gta.readonly {
  color: #37474f;
  cursor: default;
  min-height: 280px;
}
</style>
