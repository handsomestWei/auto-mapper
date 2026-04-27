<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import TreeView from './TreeView.vue'
import {
  buildSchemaFromJsonString,
  objectToJsonString,
  buildJsonExampleFromTree,
  schemaToXmlString,
  parseSchemaXmlString
} from './schema.js'

const defaultJson = `{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "",
      "innerObj": {
        "objId": ""
      }
    }
  ]
}`

const schema = reactive({
  id: 'testJson',
  desc: 'for test',
  children: []
})

const jsonText = ref('')
const xmlText = ref('')
const jsonError = ref('')
const xmlError = ref('')

let editSource = null
let internal = false

function buildFullSchema() {
  return { id: schema.id, desc: schema.desc, children: schema.children }
}

function applyFromJson() {
  if (internal) return
  editSource = 'json'
  internal = true
  try {
    jsonError.value = ''
    const s = buildSchemaFromJsonString(schema.id, schema.desc, jsonText.value)
    schema.children = s.children
    xmlText.value = schemaToXmlString(buildFullSchema())
    xmlError.value = ''
  } catch (e) {
    jsonError.value = e.message || String(e)
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
    xmlText.value = schemaToXmlString(buildFullSchema())
    xmlError.value = ''
  } catch (e) {
    jsonError.value = e.message || String(e)
  } finally {
    internal = false
  }
}

function applyFromXml() {
  if (internal) return
  editSource = 'xml'
  internal = true
  try {
    xmlError.value = ''
    const parsed = parseSchemaXmlString(xmlText.value)
    schema.id = parsed.id
    schema.desc = parsed.desc
    schema.children = parsed.children
    const example = buildJsonExampleFromTree(buildFullSchema())
    jsonText.value = objectToJsonString(example)
    jsonError.value = ''
  } catch (e) {
    xmlError.value = e.message || String(e)
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

let xmlTimer
function onXmlInput() {
  clearTimeout(xmlTimer)
  xmlTimer = setTimeout(applyFromXml, 400)
}

function exportXml() {
  const blob = new Blob([xmlText.value], { type: 'application/xml;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${schema.id || 'schema'}-schema.xml`
  a.click()
  URL.revokeObjectURL(a.href)
}

onMounted(() => {
  internal = true
  editSource = 'json'
  jsonText.value = defaultJson.trim()
  try {
    const s = buildSchemaFromJsonString(schema.id, schema.desc, jsonText.value)
    schema.children = s.children
    xmlText.value = schemaToXmlString(buildFullSchema())
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
    else if (editSource === 'xml') applyFromXml()
  }
)
</script>

<template>
  <div class="app">
    <header class="header">
      <h1>auto-mapper · Schema 设计器</h1>
      <p class="sub">
        与 Java
        <code>SchemaUtil</code>
        的 JSON→XML 结构一致；编辑任意一栏将联动更新其余区域。
      </p>
    </header>

    <div class="meta">
      <label>
        <span>schema id</span>
        <input v-model="schema.id" type="text" class="inp" />
      </label>
      <label>
        <span>描述 desc</span>
        <input v-model="schema.desc" type="text" class="inp" />
      </label>
    </div>

    <div class="grid">
      <section class="panel">
        <h2>JSON 样例</h2>
        <textarea
          v-model="jsonText"
          class="code"
          spellcheck="false"
          placeholder="粘贴 JSON 样例"
          @input="onJsonInput"
        />
        <p v-if="jsonError" class="err">{{ jsonError }}</p>
      </section>

      <section class="panel tree-panel">
        <h2>结构树</h2>
        <div class="tree-scroll">
          <TreeView :nodes="schema.children" @update="onTreeUpdate" />
        </div>
      </section>

      <section class="panel">
        <h2>Schema XML</h2>
        <textarea
          v-model="xmlText"
          class="code"
          spellcheck="false"
          @input="onXmlInput"
        />
        <p v-if="xmlError" class="err">{{ xmlError }}</p>
        <div class="actions">
          <button type="button" class="btn" @click="exportXml">导出 {{ schema.id || 'schema' }}-schema.xml</button>
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
  margin: 0 0 0.35rem;
}
.sub {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
}
.sub code {
  background: var(--panel-2);
  padding: 0 0.3rem;
  border-radius: 4px;
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
  width: 200px;
}
.grid {
  display: grid;
  grid-template-columns: 1fr 1.1fr 1fr;
  gap: 10px;
  min-height: calc(100vh - 200px);
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
  margin: 0 0 8px;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.code {
  flex: 1;
  width: 100%;
  min-height: 280px;
  font-family: ui-monospace, 'Cascadia Code', 'Fira Code', Menlo, monospace;
  font-size: 12px;
  line-height: 1.45;
  padding: 8px 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--bg);
  color: var(--text);
  resize: vertical;
}
.tree-panel .tree-scroll {
  flex: 1;
  overflow: auto;
  min-height: 240px;
  padding: 4px 0;
}
.err {
  color: #c62828;
  font-size: 12px;
  margin: 6px 0 0;
}
.actions {
  margin-top: 8px;
}
</style>
