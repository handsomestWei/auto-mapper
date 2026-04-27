<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import TreeView from './TreeView.vue'
import LineGutterText from './LineGutterText.vue'
import TreeGutterView from './TreeGutterView.vue'
import {
  buildSchemaFromJsonString,
  objectToJsonString,
  buildJsonExampleFromTree,
  schemaToXmlString
} from './schema.js'
import { formatJsonParseError } from './jsonError.js'
import { applyPathHintsToNodes } from './schemaDemoHints.js'
import { countSchemaTreeNodes } from './treeLineCount.js'

const defaultJson = `{
  "version": "1.0",
  "ok": true,
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "row-001",
      "score": 98.5,
      "innerObj": {
        "objId": "ext-abc-99",
        "label": "示例标签"
      }
    }
  ],
  "meta": {
    "traceId": "trace-8f3a2b-0001"
  }
}`

const schema = reactive({
  id: 'demoJson',
  desc: '订单/列表查询响应样例（含 eg 与 desc 说明）',
  children: []
})

const jsonText = ref('')
const xmlText = ref('')
const jsonError = ref('')

let editSource = null
let internal = false

function buildFullSchema() {
  return { id: schema.id, desc: schema.desc, children: schema.children }
}

function syncXmlFromSchema() {
  xmlText.value = schemaToXmlString(buildFullSchema())
}

function applyFromJson() {
  if (internal) return
  editSource = 'json'
  internal = true
  try {
    jsonError.value = ''
    const s = buildSchemaFromJsonString(schema.id, schema.desc, jsonText.value)
    applyPathHintsToNodes(s.children, '')
    schema.children = s.children
    syncXmlFromSchema()
  } catch (e) {
    jsonError.value = formatJsonParseError(e, jsonText.value)
  } finally {
    internal = false
  }
}

function applyFromTree() {
  if (internal) return
  editSource = 'tree'
  internal = true
  try {
    const example = buildJsonExampleFromTree(buildFullSchema())
    jsonText.value = objectToJsonString(example)
    jsonError.value = ''
    syncXmlFromSchema()
  } catch (e) {
    jsonError.value = e.message || String(e)
  } finally {
    internal = false
  }
}

function onTreeUpdate(newChildren) {
  editSource = 'tree'
  schema.children = newChildren
  if (!internal) applyFromTree()
}

let jsonTimer
function onJsonInput() {
  clearTimeout(jsonTimer)
  jsonTimer = setTimeout(applyFromJson, 400)
}

function exportXml() {
  const blob = new Blob([xmlText.value], { type: 'application/xml;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${schema.id || 'schema'}-schema.xml`
  a.click()
  URL.revokeObjectURL(a.href)
}

function resetDemo() {
  internal = true
  editSource = 'json'
  schema.id = 'demoJson'
  schema.desc = '订单/列表查询响应样例（含 eg 与 desc 说明）'
  jsonText.value = defaultJson.trim()
  try {
    const s = buildSchemaFromJsonString(schema.id, schema.desc, jsonText.value)
    applyPathHintsToNodes(s.children, '')
    schema.children = s.children
    syncXmlFromSchema()
    jsonError.value = ''
  } catch (e) {
    jsonError.value = formatJsonParseError(e, jsonText.value)
  }
  internal = false
}

const treeLineCount = computed(() => countSchemaTreeNodes(schema.children))

onMounted(() => {
  internal = true
  editSource = 'json'
  jsonText.value = defaultJson.trim()
  try {
    const s = buildSchemaFromJsonString(schema.id, schema.desc, jsonText.value)
    applyPathHintsToNodes(s.children, '')
    schema.children = s.children
    syncXmlFromSchema()
  } catch {
    schema.children = []
  }
  internal = false
})

watch(
  () => [schema.id, schema.desc],
  () => {
    if (internal) return
    if (editSource === 'json') applyFromJson()
    else if (editSource === 'tree') applyFromTree()
  }
)
</script>

<template>
  <div class="app">
    <header class="header">
      <h1>auto-mapper · Schema 设计器</h1>
    </header>

    <div class="meta">
      <label>
        <span>schema id</span>
        <input
          v-model="schema.id"
          type="text"
          class="inp"
        >
      </label>
      <label>
        <span>描述 desc</span>
        <input
          v-model="schema.desc"
          type="text"
          class="inp meta-wide"
        >
      </label>
      <div class="meta-actions">
        <button
          type="button"
          class="btn"
          @click="resetDemo"
        >
          重置
        </button>
        <button
          type="button"
          class="btn primary"
          @click="exportXml"
        >
          导出 {{ schema.id || 'schema' }}-schema.xml
        </button>
      </div>
    </div>

    <div class="grid">
      <section class="panel">
        <h2>JSON 样例</h2>
        <p class="panel-hint">
          将完整 json 样例数据复制拷贝到本区域
        </p>
        <div class="code-wrap">
          <LineGutterText
            v-model="jsonText"
            @input="onJsonInput"
          />
        </div>
        <p
          v-if="jsonError"
          class="err"
        >
          {{ jsonError }}
        </p>
      </section>

      <section class="panel tree-panel">
        <h2>结构树</h2>
        <p class="panel-hint">
          可视化编辑调整字段
        </p>
        <TreeGutterView
          :line-count="treeLineCount"
          :min-rows="12"
        >
          <TreeView
            :nodes="schema.children"
            @update="onTreeUpdate"
          />
        </TreeGutterView>
      </section>

      <section class="panel">
        <h2>Schema XML</h2>
        <p class="panel-hint">
          内容预览
        </p>
        <div class="code-wrap">
          <LineGutterText
            v-model="xmlText"
            read-only
          />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.app {
  max-width: 1920px;
  margin: 0 auto;
  padding: 1rem 1.25rem 2rem;
}
.header h1 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
  color: var(--text);
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin: 1rem 0;
  align-items: center;
}
.meta label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 13px;
}
.meta .inp {
  width: 180px;
}
.meta-wide {
  width: min(420px, 40vw) !important;
}
.meta-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-left: auto;
}
.btn.primary {
  background: var(--accent);
  color: #fff;
  border-color: #1d4ed8;
}
.btn.primary:hover {
  background: #1d4ed8;
  border-color: #1e40af;
}
.grid {
  display: grid;
  grid-template-columns: 1fr 1.15fr 1fr;
  gap: 12px;
  min-height: calc(100vh - 180px);
}
@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
.panel {
  display: flex;
  flex-direction: column;
  min-height: 360px;
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 10px 12px;
}
.panel h2 {
  font-size: 13px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--text);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.panel-hint {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--muted);
  line-height: 1.4;
}
.code-wrap {
  flex: 1;
  display: flex;
  min-height: 0;
}
.code-wrap :deep(.gwrap) {
  min-height: 280px;
}
.tree-panel :deep(.tgw) {
  min-height: 280px;
}
.err {
  color: #c62828;
  font-size: 12px;
  margin: 6px 0 0;
}
</style>
