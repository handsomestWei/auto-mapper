<script setup>
import { ref, reactive, onMounted, watch, onUnmounted, nextTick } from 'vue'
import TreeView from '../TreeView.vue'
import LineGutterText from '../LineGutterText.vue'
import TreeGutterView from '../TreeGutterView.vue'
import {
  buildSchemaFromJsonString,
  objectToJsonString,
  buildJsonExampleFromTree,
  schemaToXmlString,
  parseSchemaXmlString
} from '../schema.js'
import { formatJsonParseError, getJsonParseErrorLine } from '../jsonError.js'
import { applyPathHintsToNodes } from '../schemaDemoHints.js'
import { DEFAULT_JSON_SAMPLE } from '../demoSamples.js'

const defaultJson = DEFAULT_JSON_SAMPLE.trim()

const schema = reactive({
  id: 'demoJson',
  desc: '订单/列表查询响应样例',
  children: []
})

const jsonText = ref('')
const xmlText = ref('')
const jsonError = ref('')
/** JSON 解析错误行（1-based），与左侧行号、高亮一致 */
const jsonErrorLine = ref(null)

function clearJsonError() {
  jsonError.value = ''
  jsonErrorLine.value = null
}

function setJsonParseError(err, text) {
  jsonError.value = formatJsonParseError(err, text)
  jsonErrorLine.value = getJsonParseErrorLine(err, text)
}
const xmlPreviewOpen = ref(false)
/** null | 'json' | 'schema' | 'reset' */
const importConfirmKind = ref(null)
const fileJsonImport = ref(null)
const fileSchemaImport = ref(null)

function closeXmlPreview() {
  xmlPreviewOpen.value = false
}

function cancelImportConfirm() {
  importConfirmKind.value = null
}

function confirmImportProceed() {
  const k = importConfirmKind.value
  importConfirmKind.value = null
  if (k === 'reset') {
    resetDemo()
    return
  }
  nextTick(() => {
    if (k === 'json') fileJsonImport.value?.click()
    else if (k === 'schema') fileSchemaImport.value?.click()
  })
}

function requestImportJson() {
  importConfirmKind.value = 'json'
}

function requestImportSchema() {
  importConfirmKind.value = 'schema'
}

function requestReset() {
  importConfirmKind.value = 'reset'
}

async function onJsonFilePick(e) {
  const input = e.target
  const f = input.files?.[0]
  input.value = ''
  if (!f) return
  try {
    const text = await f.text()
    applyImportedJson(text)
  } catch (err) {
    jsonError.value = err.message ? `导入失败：${err.message}` : String(err)
    jsonErrorLine.value = null
  }
}

async function onSchemaFilePick(e) {
  const input = e.target
  const f = input.files?.[0]
  input.value = ''
  if (!f) return
  try {
    const text = await f.text()
    const parsed = parseSchemaXmlString(text)
    applyImportedSchema(parsed)
  } catch (err) {
    jsonError.value = `导入 schema 失败：${err.message || String(err)}`
    jsonErrorLine.value = null
  }
}

function applyImportedJson(text) {
  internal = true
  editSource = 'json'
  jsonText.value = text
  try {
    clearJsonError()
    const s = buildSchemaFromJsonString(schema.id, schema.desc, jsonText.value)
    applyPathHintsToNodes(s.children, '')
    schema.children = s.children
    syncXmlFromSchema()
  } catch (e) {
    setJsonParseError(e, jsonText.value)
  } finally {
    internal = false
  }
}

function applyImportedSchema(parsed) {
  internal = true
  editSource = 'tree'
  try {
    schema.id = parsed.id
    schema.desc = parsed.desc
    schema.children = parsed.children
    applyPathHintsToNodes(schema.children, '')
    const example = buildJsonExampleFromTree(buildFullSchema())
    jsonText.value = objectToJsonString(example)
    clearJsonError()
    syncXmlFromSchema()
  } catch (e) {
    jsonError.value = e.message || String(e)
    jsonErrorLine.value = null
  } finally {
    internal = false
  }
}

function onPreviewKeydown(e) {
  if (e.key !== 'Escape') return
  if (importConfirmKind.value) {
    e.preventDefault()
    cancelImportConfirm()
    return
  }
  if (xmlPreviewOpen.value) {
    e.preventDefault()
    closeXmlPreview()
  }
}

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
    clearJsonError()
    const s = buildSchemaFromJsonString(schema.id, schema.desc, jsonText.value)
    applyPathHintsToNodes(s.children, '')
    schema.children = s.children
    syncXmlFromSchema()
  } catch (e) {
    setJsonParseError(e, jsonText.value)
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
    clearJsonError()
    syncXmlFromSchema()
  } catch (e) {
    jsonError.value = e.message || String(e)
    jsonErrorLine.value = null
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
  schema.desc = '订单/列表查询响应样例'
  jsonText.value = defaultJson.trim()
  try {
    const s = buildSchemaFromJsonString(schema.id, schema.desc, jsonText.value)
    applyPathHintsToNodes(s.children, '')
    schema.children = s.children
    syncXmlFromSchema()
    clearJsonError()
  } catch (e) {
    setJsonParseError(e, jsonText.value)
  }
  internal = false
}

onMounted(() => {
  window.addEventListener('keydown', onPreviewKeydown)
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

onUnmounted(() => {
  window.removeEventListener('keydown', onPreviewKeydown)
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
      <h1>Schema 设计器</h1>
      <p class="header-desc">
        左侧为完整 JSON 样例，可直接编辑、粘贴或使用「导入 json」；右侧结构树中可视化编辑字段，并可通过「导入 / 预览 / 导出 schema」管理 XML。
      </p>
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
      <label class="meta-desc-row">
        <span>描述 desc</span>
        <input
          v-model="schema.desc"
          type="text"
          class="inp meta-wide"
        >
        <div class="meta-actions">
          <button
            type="button"
            class="btn"
            @click="requestReset"
          >
            重置
          </button>
        </div>
      </label>
    </div>

    <div class="grid">
      <section class="panel panel-json">
        <div class="panel-head">
          <h2>JSON 样例</h2>
          <div class="panel-head-actions">
            <button
              type="button"
              class="btn"
              @click="requestImportJson"
            >
              导入json
            </button>
          </div>
        </div>
        <div class="code-wrap">
          <LineGutterText
            v-model="jsonText"
            show-copy
            :error-line="jsonErrorLine"
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
        <div class="panel-head">
          <h2>Schema 结构树</h2>
          <div class="panel-head-actions">
            <button
              type="button"
              class="btn"
              @click="requestImportSchema"
            >
              导入schema
            </button>
            <button
              type="button"
              class="btn"
              @click="xmlPreviewOpen = true"
            >
              预览schema
            </button>
            <button
              type="button"
              class="btn primary"
              @click="exportXml"
            >
              导出schema
            </button>
          </div>
        </div>
        <TreeGutterView>
          <TreeView
            :nodes="schema.children"
            @update="onTreeUpdate"
          />
        </TreeGutterView>
      </section>
    </div>

    <input
      ref="fileJsonImport"
      type="file"
      class="file-input-hidden"
      accept=".json,application/json"
      @change="onJsonFilePick"
    >
    <input
      ref="fileSchemaImport"
      type="file"
      class="file-input-hidden"
      accept=".xml,text/xml,application/xml"
      @change="onSchemaFilePick"
    >

    <Teleport to="body">
      <div
        v-if="importConfirmKind"
        class="confirm-backdrop"
        @click.self="cancelImportConfirm"
      >
        <div
          class="confirm-sheet"
          role="alertdialog"
          aria-modal="true"
          aria-labelledby="import-confirm-title"
        >
          <h3
            id="import-confirm-title"
            class="confirm-title"
          >
            {{
              importConfirmKind === 'reset'
                ? '重置'
                : importConfirmKind === 'json'
                  ? '导入 JSON'
                  : '导入 Schema XML'
            }}
          </h3>
          <p class="confirm-body">
            {{
              importConfirmKind === 'reset'
                ? '重置将丢弃当前编辑，恢复为内置示例（含 JSON 样例、结构树与 schema），是否继续？'
                : '导入将覆盖当前编辑内容（含 JSON 样例、结构树与 schema），是否继续？'
            }}
          </p>
          <div class="confirm-actions">
            <button
              type="button"
              class="btn"
              @click="cancelImportConfirm"
            >
              取消
            </button>
            <button
              type="button"
              class="btn primary"
              @click="confirmImportProceed"
            >
              确定
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div
        v-if="xmlPreviewOpen"
        class="xml-preview-backdrop"
        @click.self="closeXmlPreview"
      >
        <div
          class="xml-preview-sheet"
          role="dialog"
          aria-modal="true"
          aria-labelledby="xml-preview-title"
        >
          <div class="xml-preview-head">
            <h2 id="xml-preview-title">
              Schema XML
            </h2>
            <button
              type="button"
              class="btn"
              @click="closeXmlPreview"
            >
              关闭
            </button>
          </div>
          <p class="panel-hint xml-preview-hint">
            内容预览
          </p>
          <div class="code-wrap xml-preview-code">
            <LineGutterText
              v-model="xmlText"
              read-only
              show-copy
            />
          </div>
        </div>
      </div>
    </Teleport>
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
.header-desc {
  margin: 0.4rem 0 0;
  max-width: 56rem;
  font-size: 12px;
  color: var(--muted);
  line-height: 1.45;
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
  width: min(280px, 32vw) !important;
}
.meta-desc-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
}
.meta-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  flex-shrink: 0;
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
  /* 与规则页左侧 panel-schema 同一套列宽 */
  grid-template-columns: minmax(240px, min(38vw, 460px)) minmax(0, 1fr);
  gap: 12px;
  min-height: calc(100vh - 180px);
}
@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
.panel-json {
  width: 100%;
  min-width: 0;
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
.panel-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin: 0 0 6px;
}
.panel-head h2 {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  letter-spacing: 0.03em;
}
.panel-head-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
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
.xml-preview-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(2px);
}
.xml-preview-sheet {
  display: flex;
  flex-direction: column;
  width: min(1200px, 92vw);
  max-height: min(88vh, 900px);
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 14px 16px;
  box-shadow:
    0 25px 50px -12px rgba(0, 0, 0, 0.22),
    0 0 0 1px rgba(255, 255, 255, 0.06) inset;
}
.xml-preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
}
.xml-preview-head h2 {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
  letter-spacing: 0.03em;
}
.xml-preview-hint {
  margin-bottom: 6px;
  flex-shrink: 0;
}
.xml-preview-code {
  flex: 1 1 auto;
  min-height: 0;
  height: min(480px, 65vh);
}
.xml-preview-code :deep(.gwrap) {
  min-height: min(480px, 65vh);
}
.file-input-hidden {
  position: fixed;
  left: -10000px;
  top: 0;
  width: 1px;
  height: 1px;
  opacity: 0;
  overflow: hidden;
}
.confirm-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1001;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(2px);
}
.confirm-sheet {
  width: min(420px, 92vw);
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 18px 20px;
  box-shadow:
    0 25px 50px -12px rgba(0, 0, 0, 0.22),
    0 0 0 1px rgba(255, 255, 255, 0.06) inset;
}
.confirm-title {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}
.confirm-body {
  margin: 0 0 18px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--muted);
}
.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
