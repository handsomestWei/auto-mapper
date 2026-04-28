<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  readOnly: { type: Boolean, default: false },
  minRows: { type: Number, default: 12 },
  monosize: { type: String, default: '12px' },
  showCopy: { type: Boolean, default: false },
  errorLine: { type: Number, default: null }
})

const emit = defineEmits(['update:modelValue', 'input'])

const ta = ref(null)
const gutter = ref(null)
const copyHint = ref(false)

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

/** 报错行在行号、gta 上使用（与 \\n 逻辑行对应；若有自动折行则可能略有偏差） */
const errLineCss = computed(() => {
  if (props.errorLine == null || props.errorLine < 1) return {}
  return { '--err-line-num': props.errorLine }
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

async function copyText() {
  const t = props.modelValue ?? ''
  try {
    await navigator.clipboard.writeText(t)
    copyHint.value = true
    setTimeout(() => {
      copyHint.value = false
    }, 1600)
  } catch {
    try {
      ta.value?.select()
      document.execCommand('copy')
      copyHint.value = true
      setTimeout(() => {
        copyHint.value = false
      }, 1600)
    } catch {
      /* ignore */
    }
  }
}

function scrollErrorLineVisible() {
  const line = props.errorLine
  if (!line || !ta.value) return
  const lh = parseFloat(getComputedStyle(ta.value).lineHeight) || 18
  const lineTop = (line - 1) * lh
  const target = lineTop - ta.value.clientHeight / 2 + lh / 2
  ta.value.scrollTop = Math.max(0, target)
}

watch(
  () => [props.errorLine, props.modelValue],
  ([, v]) => {
    if (props.readOnly && ta.value && ta.value.value !== (v ?? '')) {
      ta.value.value = v ?? ''
    }
    nextTick(() => {
      syncGutterSize()
      onScroll()
      scrollErrorLineVisible()
    })
  }
)

defineExpose({ focus: () => ta.value?.focus() })

onMounted(() => {
  nextTick(() => {
    syncGutterSize()
    onScroll()
    scrollErrorLineVisible()
  })
})
</script>

<template>
  <div class="gwrap">
    <div class="gwrap-main">
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
          :class="{ 'ln-error': errorLine != null && n === errorLine }"
        >
          {{ n }}
        </div>
      </div>
      <div class="ta-wrap">
        <textarea
          ref="ta"
          class="gta"
          :class="{
            readonly: readOnly,
            'has-err-line': errorLine != null,
            'has-copy-pad': showCopy
          }"
          :style="[{ fontSize: monosize }, errLineCss]"
          :value="modelValue"
          :readOnly="readOnly"
          spellcheck="false"
          @input="onInput"
          @scroll="onScroll"
        />
        <button
          v-if="showCopy"
          type="button"
          class="copy-fab"
          :class="{ done: copyHint }"
          :title="copyHint ? '已复制' : '复制全部'"
          :aria-label="copyHint ? '已复制' : '复制全部'"
          @click.stop.prevent="copyText"
        >
          <svg
            v-if="!copyHint"
            class="copy-ico"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <rect
              x="9"
              y="9"
              width="13"
              height="13"
              rx="2"
              ry="2"
            />
            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
          </svg>
          <svg
            v-else
            class="copy-ico"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.25"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <path d="M20 6 9 17l-5-5" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.gwrap {
  position: relative;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 6px;
  overflow: hidden;
  background: var(--code-bg);
}

.gwrap-main {
  display: flex;
  flex: 1;
  min-height: 0;
  width: 100%;
}

.ta-wrap {
  position: relative;
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
}

.copy-fab {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 2;
  box-sizing: border-box;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 1px solid rgba(15, 23, 42, 0.09);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
  color: #64748b;
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition:
    color 0.15s ease,
    border-color 0.15s ease,
    background 0.15s ease,
    box-shadow 0.15s ease;
}

.copy-fab:hover {
  color: #334155;
  border-color: rgba(15, 23, 42, 0.14);
  background: #fff;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
}

.copy-fab:active {
  transform: scale(0.96);
}

.copy-fab.done {
  color: #059669;
  border-color: rgba(5, 150, 105, 0.25);
  background: rgba(236, 253, 245, 0.95);
}

.copy-ico {
  display: block;
  flex-shrink: 0;
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

.ln-error {
  color: #b71c1c;
  font-weight: 600;
  background: rgba(198, 40, 40, 0.14);
  border-radius: 2px;
  margin-right: -4px;
  padding-right: 4px;
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

.gta.has-copy-pad {
  padding-right: 40px;
}

/* 与 .ln 同一逻辑行：按换行计行；背景随内容滚动 */
.gta.has-err-line {
  background-image: linear-gradient(
    transparent 0,
    transparent calc(8px + (var(--err-line-num) - 1) * 1.45em),
    rgba(198, 40, 40, 0.12) calc(8px + (var(--err-line-num) - 1) * 1.45em),
    rgba(198, 40, 40, 0.12) calc(8px + var(--err-line-num) * 1.45em),
    transparent calc(8px + var(--err-line-num) * 1.45em)
  );
  background-attachment: local;
  background-repeat: no-repeat;
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
