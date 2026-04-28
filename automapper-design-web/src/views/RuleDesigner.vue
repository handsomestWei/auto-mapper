<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import TreeGutterView from '../TreeGutterView.vue'
import LineGutterText from '../LineGutterText.vue'
import SchemaRuleRow from '../components/SchemaRuleRow.vue'
import {
  buildSchemaFromJsonString,
  parseSchemaXmlString,
  buildJsonExampleFromTree,
  objectToJsonString,
  jsonStringToObject
} from '../schema.js'
import { applyPathHintsToNodes } from '../schemaDemoHints.js'
import { DEFAULT_JSON_SAMPLE, DEFAULT_RULE_XML } from '../demoSamples.js'
import { parseRuleXmlString, ruleToXmlString } from '../ruleXml.js'
import { executeRuleMapping } from '../ruleMapExec.js'

/** 左侧：输入源（只读展示） */
const leftSchema = reactive({
  id: 'demoJson',
  desc: '订单/列表查询响应样例',
  children: []
})
const leftJsonText = ref('')

/** 右侧：仅对照展示导入的 schema/json；不参与映射预览与规则 */
const rightSchema = reactive({
  id: '',
  desc: '',
  children: []
})
const rightJsonText = ref('')
const rightHasImport = ref(false)

/** 默认折叠：JSON 预览 / 右侧对照区 */
const leftJsonPreviewOpen = ref(false)
const rightSampleOpen = ref(false)

const ruleRoot = reactive({
  id: 'testRule',
  desc: 'for test',
  serializerFeatures: '6,7',
  rs: []
})

const rulePreviewOpen = ref(false)
const fileRuleImport = ref(null)
const fileLeftJson = ref(null)
const fileLeftSchema = ref(null)
const fileRightJson = ref(null)
const fileRightSchema = ref(null)

const importPanelErr = ref('')

/**
 * null |
 * 'reset' | 'importRule' |
 * 'importLeftJson' | 'importLeftSchema' | 'importRightJson' | 'importRightSchema'
 */
const destructiveConfirmKind = ref(null)

function buildLineMap(children) {
  const m = new Map()
  let line = 1
  function dfs(arr, path) {
    for (let i = 0; i < arr.length; i++) {
      const p = [...path, i]
      m.set(p.join('-'), String(line))
      line += 1
      const ch = arr[i].children
      if (ch && ch.length) dfs(ch, p)
    }
  }
  dfs(children || [], [])
  return m
}

const lineByPath = computed(() => buildLineMap(leftSchema.children))
const lineByPathRight = computed(() => buildLineMap(rightSchema.children))

/** 映射预览：手动点击标题旁刷新，避免规则/左侧频繁变动时持续重算 */
const mappedPreviewText = ref('// 点击「转换结果预览」旁的刷新按钮，根据左侧 JSON 与当前规则生成预览。')

function recalcMappedPreview() {
  try {
    const raw = leftJsonText.value.trim()
    if (!raw) {
      mappedPreviewText.value = '// 暂无输入源 JSON。请使用「导入 JSON」或重置。'
      return
    }

    const tpl = buildJsonExampleFromTree({
      children: leftSchema.children || []
    })
    const parsed = jsonStringToObject(raw)
    const rsSan = (ruleRoot.rs || []).map((r) => ({
      from: r.from ?? '',
      to: r.to ?? '',
      func: r.func ?? '',
      val: r.val ?? ''
    }))
    const out = executeRuleMapping(parsed, tpl, rsSan)
    mappedPreviewText.value = JSON.stringify(out, null, 2)
  } catch (e) {
    mappedPreviewText.value = `// 映射预览失败：${e.message || e}`
  }
}

const ruleXmlText = computed(() =>
  ruleToXmlString({
    id: ruleRoot.id,
    desc: ruleRoot.desc,
    serializerFeatures: ruleRoot.serializerFeatures,
    rs: ruleRoot.rs
  })
)

function cancelDestructiveConfirm() {
  destructiveConfirmKind.value = null
}

function requestResetConfirm() {
  destructiveConfirmKind.value = 'reset'
}

function requestImportRuleConfirm() {
  destructiveConfirmKind.value = 'importRule'
}

function requestImportLeftJsonConfirm() {
  destructiveConfirmKind.value = 'importLeftJson'
}

function requestImportLeftSchemaConfirm() {
  destructiveConfirmKind.value = 'importLeftSchema'
}

function requestImportRightJsonConfirm() {
  destructiveConfirmKind.value = 'importRightJson'
}

function requestImportRightSchemaConfirm() {
  destructiveConfirmKind.value = 'importRightSchema'
}

function confirmDestructiveProceed() {
  const k = destructiveConfirmKind.value
  destructiveConfirmKind.value = null
  if (!k) return
  nextTick(() => {
    if (k === 'reset') {
      resetAllToDemo()
      return
    }
    if (k === 'importRule') {
      fileRuleImport.value?.click()
      return
    }
    if (k === 'importLeftJson') {
      fileLeftJson.value?.click()
      return
    }
    if (k === 'importLeftSchema') {
      fileLeftSchema.value?.click()
      return
    }
    if (k === 'importRightJson') {
      fileRightJson.value?.click()
      return
    }
    if (k === 'importRightSchema') {
      fileRightSchema.value?.click()
    }
  })
}

function onRulePageKeydown(e) {
  if (e.key !== 'Escape') return
  if (destructiveConfirmKind.value) {
    e.preventDefault()
    cancelDestructiveConfirm()
    return
  }
  if (rulePreviewOpen.value) {
    e.preventDefault()
    rulePreviewOpen.value = false
  }
}

const IMPORT_ANCHOR_BASE = 10_000

function applyDemoRule() {
  const p = parseRuleXmlString(DEFAULT_RULE_XML)
  ruleRoot.id = p.id
  ruleRoot.desc = p.desc
  ruleRoot.serializerFeatures = p.serializerFeatures
  ruleRoot.rs = p.rs.map((r, i) => ({
    ...r,
    _anchorOrder: IMPORT_ANCHOR_BASE + i + 1
  }))
}

function resetSchemaAndJsonFromDemo() {
  leftSchema.id = 'demoJson'
  leftSchema.desc = '订单/列表查询响应样例'
  leftJsonText.value = DEFAULT_JSON_SAMPLE.trim()
  try {
    const s = buildSchemaFromJsonString(leftSchema.id, leftSchema.desc, leftJsonText.value)
    applyPathHintsToNodes(s.children, '')
    leftSchema.children = s.children
  } catch {
    leftSchema.children = []
  }
  importPanelErr.value = ''
}

function resetRightDisplayOnly() {
  rightHasImport.value = false
  rightSchema.id = ''
  rightSchema.desc = ''
  rightSchema.children = []
  rightJsonText.value = ''
}

function resetAllToDemo() {
  resetSchemaAndJsonFromDemo()
  resetRightDisplayOnly()
  applyDemoRule()
}

function normalizeAnchor(rule) {
  const a = rule._anchorOrder
  return typeof a === 'number' && !Number.isNaN(a)
    ? a
    : Number.MAX_SAFE_INTEGER
}

/**
 * 在某一结构树行 L（左侧行号）插入一条用户规则索引。
 * 规则表中：由「导入 / 重置」载入的占位规则使用 ao >= IMPORT_ANCHOR_BASE，视为一段连续「预设尾」，
 * 与左侧树序号不可比；插在「第一段预设」之前的子区间里，仅按 ao<=L（且 ao 为左侧行号锚点）与用户规则对齐。
 */
function insertionIndexForAnchor(L) {
  const rs = ruleRoot.rs
  const firstPresetIdx = rs.findIndex((r) => normalizeAnchor(r) >= IMPORT_ANCHOR_BASE)
  const userEnd = firstPresetIdx === -1 ? rs.length : firstPresetIdx

  let posAfter = 0
  for (let i = 0; i < userEnd; i++) {
    const ao = normalizeAnchor(rs[i])
    if (ao <= L) {
      posAfter = i + 1
    }
  }
  return posAfter
}

function insertRuleAnchored(order, entry) {
  ruleRoot.rs.splice(insertionIndexForAnchor(order), 0, entry)
}

function onAddRule({ path, jsonPath }) {
  const key = Array.isArray(path) ? path.join('-') : ''
  const lineStr = lineByPath.value.get(key)
  const order = lineStr ? parseInt(lineStr, 10) : 0
  insertRuleAnchored(order, {
    from: jsonPath,
    to: jsonPath,
    func: '',
    val: '',
    _anchorOrder: order
  })
}

/** JSONPath：去掉开头的 $ ，用于层级前缀判断 */
function stripJsonPathLead(p) {
  let s = String(p ?? '').trim()
  if (s === '$' || !s) return ''
  if (s.startsWith('$')) s = s.slice(1).replace(/^\./, '')
  return s
}

/**
 * descendant 的 to 是否严格落在 ancestorTo 之下（更深）。
 * $.data → $.data[0:].x 用「祖先名 + [」或「祖先名 + .」匹配。
 */
function toPathStrictDescendantOf(ancestorTo, descendantTo) {
  const a = stripJsonPathLead(ancestorTo)
  const b = stripJsonPathLead(descendantTo)
  if (!b || !a) return false
  if (b === a) return false
  return b.startsWith(a + '.') || b.startsWith(a + '[')
}

/** 删除本条，并同步删除「to 路径」视为其下级的所有规则（与 ruleIndent 按 to 分层一致） */
function removeRule(index) {
  const rs = ruleRoot.rs
  if (index < 0 || index >= rs.length) return
  const pivotTo = rs[index]?.to ?? ''
  const toDel = new Set([index])
  for (let j = 0; j < rs.length; j++) {
    if (j === index) continue
    if (toPathStrictDescendantOf(pivotTo, rs[j]?.to ?? '')) {
      toDel.add(j)
    }
  }
  for (const i of [...toDel].sort((x, y) => y - x)) {
    rs.splice(i, 1)
  }
}

function ruleIndent(to) {
  const t = String(to || '')
  if (!t || t === '$') return 0
  const inner = t.startsWith('$.') ? t.slice(2) : t.replace(/^\$/, '')
  if (!inner) return 0
  const depth = inner.split('.').length
  return Math.min(depth, 10) * 10
}

function exportRuleXml() {
  const blob = new Blob([ruleXmlText.value], { type: 'application/xml;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${ruleRoot.id || 'rule'}-rule.xml`
  a.click()
  URL.revokeObjectURL(a.href)
}

function onImportRuleFile(e) {
  const input = e.target
  const f = input.files?.[0]
  input.value = ''
  if (!f) return
  f.text()
    .then((text) => {
      const p = parseRuleXmlString(text)
      ruleRoot.id = p.id
      ruleRoot.desc = p.desc
      ruleRoot.serializerFeatures = p.serializerFeatures
      ruleRoot.rs = p.rs.map((r, i) => ({
        ...r,
        _anchorOrder: IMPORT_ANCHOR_BASE + i + 1
      }))
    })
    .catch(() => {
      /* ignore */
    })
}

function applyLeftFromJson(text) {
  leftJsonText.value = text.trim()
  const s = buildSchemaFromJsonString(leftSchema.id, leftSchema.desc, leftJsonText.value)
  applyPathHintsToNodes(s.children, '')
  leftSchema.children = s.children
  importPanelErr.value = ''
}

function applyLeftFromSchemaXml(text) {
  const parsed = parseSchemaXmlString(text)
  leftSchema.id = parsed.id
  leftSchema.desc = parsed.desc
  leftSchema.children = parsed.children
  applyPathHintsToNodes(leftSchema.children, '')
  const ex = buildJsonExampleFromTree({ children: leftSchema.children })
  leftJsonText.value = objectToJsonString(ex)
}

function applyRightFromJson(text) {
  rightJsonText.value = text.trim()
  const sid = leftSchema.id ? `${leftSchema.id}Target` : 'targetJson'
  const rs = buildSchemaFromJsonString(sid, 'imported', rightJsonText.value)
  applyPathHintsToNodes(rs.children, '')
  rightSchema.id = sid
  rightSchema.desc = 'imported'
  rightSchema.children = rs.children
  rightHasImport.value = true
  importPanelErr.value = ''
}

function applyRightFromSchemaXml(text) {
  const parsed = parseSchemaXmlString(text)
  rightSchema.id = parsed.id
  rightSchema.desc = parsed.desc
  rightSchema.children = parsed.children
  applyPathHintsToNodes(rightSchema.children, '')
  rightJsonText.value = objectToJsonString(buildJsonExampleFromTree({ children: rightSchema.children }))
  rightHasImport.value = true
}

function onLeftJsonPick(e) {
  const input = e.target
  const f = input.files?.[0]
  input.value = ''
  if (!f) return
  f.text()
    .then((text) => {
      try {
        applyLeftFromJson(text)
      } catch (err) {
        importPanelErr.value = err?.message || String(err)
      }
    })
    .catch((err) => {
      importPanelErr.value = err?.message || String(err)
    })
}

function onLeftSchemaPick(e) {
  const input = e.target
  const f = input.files?.[0]
  input.value = ''
  if (!f) return
  f.text()
    .then((text) => {
      try {
        applyLeftFromSchemaXml(text)
        importPanelErr.value = ''
      } catch (err) {
        importPanelErr.value = err?.message || String(err)
      }
    })
    .catch((err) => {
      importPanelErr.value = err?.message || String(err)
    })
}

function onRightJsonPick(e) {
  const input = e.target
  const f = input.files?.[0]
  input.value = ''
  if (!f) return
  f.text()
    .then((text) => {
      try {
        applyRightFromJson(text)
      } catch (err) {
        importPanelErr.value = err?.message || String(err)
      }
    })
    .catch((err) => {
      importPanelErr.value = err?.message || String(err)
    })
}

function onRightSchemaPick(e) {
  const input = e.target
  const f = input.files?.[0]
  input.value = ''
  if (!f) return
  f.text()
    .then((text) => {
      try {
        applyRightFromSchemaXml(text)
        importPanelErr.value = ''
      } catch (err) {
        importPanelErr.value = err?.message || String(err)
      }
    })
    .catch((err) => {
      importPanelErr.value = err?.message || String(err)
    })
}

onMounted(() => {
  window.addEventListener('keydown', onRulePageKeydown)
  resetAllToDemo()
})

onUnmounted(() => {
  window.removeEventListener('keydown', onRulePageKeydown)
})
</script>

<template>
  <div class="rule-page">
    <header class="header">
      <h1>字段转换规则设计器</h1>
      <p class="header-desc">
        左侧板块：只读源与树，节点加号插 JSONPath；中间板块：from/to/func/val 编辑规则；右侧板块：只读输出源与树，可选导入json样例或schema作为对照。
      </p>
    </header>

    <div class="meta">
      <label>
        <span>rule id</span>
        <input
          v-model="ruleRoot.id"
          type="text"
          class="inp"
        >
      </label>
      <label class="meta-desc-row">
        <span>desc</span>
        <input
          v-model="ruleRoot.desc"
          type="text"
          class="inp meta-wide"
        >
        <div class="meta-actions">
          <button
            type="button"
            class="btn btn-reset-meta"
            @click="requestResetConfirm"
          >
            重置
          </button>
        </div>
      </label>
    </div>
    <p
      v-if="importPanelErr"
      class="meta-err"
    >
      {{ importPanelErr }}
    </p>

    <input
      ref="fileLeftJson"
      type="file"
      class="file-input-hidden"
      accept=".json,application/json"
      @change="onLeftJsonPick"
    >
    <input
      ref="fileLeftSchema"
      type="file"
      class="file-input-hidden"
      accept=".xml,text/xml,application/xml"
      @change="onLeftSchemaPick"
    >
    <input
      ref="fileRightJson"
      type="file"
      class="file-input-hidden"
      accept=".json,application/json"
      @change="onRightJsonPick"
    >
    <input
      ref="fileRightSchema"
      type="file"
      class="file-input-hidden"
      accept=".xml,text/xml,application/xml"
      @change="onRightSchemaPick"
    >
    <input
      ref="fileRuleImport"
      type="file"
      class="file-input-hidden"
      accept=".xml,text/xml,application/xml"
      @change="onImportRuleFile"
    >

    <div class="grid">
      <!-- 左侧：输入源 -->
      <section class="panel panel-src">
        <div class="panel-head">
          <h2>输入源</h2>
          <div class="panel-head-actions">
            <button
              type="button"
              class="btn"
              @click="requestImportLeftJsonConfirm"
            >
              导入json
            </button>
            <button
              type="button"
              class="btn"
              @click="requestImportLeftSchemaConfirm"
            >
              导入schema xml
            </button>
          </div>
        </div>
        <p class="panel-hint">
          导入json样例或schema。在下方结构树点击 + 号插入规则。
        </p>
        <button
          type="button"
          class="collapse-toggle"
          :aria-expanded="leftJsonPreviewOpen"
          aria-controls="rule-src-json-preview"
          @click="leftJsonPreviewOpen = !leftJsonPreviewOpen"
        >
          <span class="collapse-chevron">{{ leftJsonPreviewOpen ? '▼' : '▶' }}</span>
          <span>JSON 预览</span>
        </button>
        <div
          v-show="leftJsonPreviewOpen"
          id="rule-src-json-preview"
          class="code-wrap inp-preview"
        >
          <LineGutterText
            :model-value="leftJsonText"
            read-only
            show-copy
          />
        </div>
        <TreeGutterView>
          <SchemaRuleRow
            v-for="(n, i) in leftSchema.children"
            :key="n.name + '-' + i + '-' + n.type"
            :node="n"
            :path="[i]"
            :root-children="leftSchema.children"
            :line-by-path="lineByPath"
            @add-rule="onAddRule"
          />
        </TreeGutterView>
      </section>

      <!-- 中：规则 -->
      <section class="panel rule-panel">
        <div class="panel-head">
          <h2>字段转换规则</h2>
          <div class="panel-head-actions">
            <button
              type="button"
              class="btn"
              @click="requestImportRuleConfirm"
            >
              导入rule xml
            </button>
            <button
              type="button"
              class="btn"
              @click="rulePreviewOpen = true"
            >
              预览rule
            </button>
            <button
              type="button"
              class="btn primary"
              @click="exportRuleXml"
            >
              导出rule
            </button>
          </div>
        </div>
        <div class="rule-scroll">
          <div
            v-if="!ruleRoot.rs.length"
            class="empty"
          >
            暂无规则，请在左侧节点选择「+」
          </div>
          <div
            v-for="(r, idx) in ruleRoot.rs"
            :key="'rule-row-' + idx"
            class="rule-block"
            :style="{ marginLeft: ruleIndent(r.to) + 'px' }"
          >
            <div class="rule-inline">
              <span class="badge">{{ idx + 1 }}</span>
              <div class="rule-field stretch">
                <span class="lf">from</span>
                <input
                  v-model="r.from"
                  type="text"
                  class="inp mono"
                >
              </div>
              <div class="rule-field stretch">
                <span class="lf">to</span>
                <input
                  v-model="r.to"
                  type="text"
                  class="inp mono"
                >
              </div>
              <div class="rule-field narrow">
                <span class="lf">func</span>
                <input
                  v-model="r.func"
                  type="text"
                  class="inp mono"
                  placeholder="newList …"
                >
              </div>
              <div class="rule-field narrow">
                <span class="lf">val</span>
                <input
                  v-model="r.val"
                  type="text"
                  class="inp mono"
                >
              </div>
              <button
                type="button"
                class="btn mini rm"
                title="删除此条（同时删除 to 为其子路径的规则）"
                @click="removeRule(idx)"
              >
                ×
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- 右：输出目标 + 预览 -->
      <section class="panel panel-out">
        <div class="panel-head">
          <h2>输出源</h2>
          <div class="panel-head-actions">
            <button
              type="button"
              class="btn"
              @click="requestImportRightJsonConfirm"
            >
              导入json
            </button>
            <button
              type="button"
              class="btn"
              @click="requestImportRightSchemaConfirm"
            >
              导入schema xml
            </button>
          </div>
        </div>
        <p class="panel-hint">
          可选导入json样例或schema作为输出示例，用于对照展示。
        </p>
        <button
          type="button"
          class="collapse-toggle"
          :aria-expanded="rightSampleOpen"
          aria-controls="rule-out-sample-blocks"
          @click="rightSampleOpen = !rightSampleOpen"
        >
          <span class="collapse-chevron">{{ rightSampleOpen ? '▼' : '▶' }}</span>
          <span>输出示例对照</span>
          <span
            v-if="rightHasImport"
            class="collapse-muted"
          >（JSON + 结构树）</span>
        </button>
        <div
          v-show="rightSampleOpen"
          id="rule-out-sample-blocks"
          class="out-sample-collapsed"
        >
          <template v-if="rightHasImport">
            <div class="code-wrap out-sample">
              <LineGutterText
                :model-value="rightJsonText"
                read-only
                show-copy
              />
            </div>
            <TreeGutterView>
              <SchemaRuleRow
                v-for="(n, i) in rightSchema.children"
                :key="'r-' + n.name + '-' + i + '-' + n.type"
                :node="n"
                :path="[i]"
                :root-children="rightSchema.children"
                :line-by-path="lineByPathRight"
                :show-add="false"
              />
            </TreeGutterView>
          </template>
          <div
            v-else
            class="out-placeholder"
          >
            （尚未导入——可导入右侧 JSON / schema 仅作展示）
          </div>
        </div>

        <div class="out-title-row">
          <h3 class="out-title">
            转换结果预览
          </h3>
          <button
            type="button"
            class="preview-refresh-btn"
            title="根据左侧源与当前规则重新计算"
            aria-label="刷新映射预览"
            @click="recalcMappedPreview"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
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
              <path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
              <path d="M3 3v5h5" />
              <path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16" />
              <path d="M16 16h5v5" />
            </svg>
          </button>
        </div>
        <p class="panel-hint out-hint">
          手动刷新更新；自定义 func 函数规则会被忽略。
        </p>
        <div class="code-wrap mapped-preview-wrap">
          <LineGutterText
            :model-value="mappedPreviewText"
            read-only
            show-copy
          />
        </div>
      </section>
    </div>

    <Teleport to="body">
      <div
        v-if="destructiveConfirmKind"
        class="confirm-backdrop"
        @click.self="cancelDestructiveConfirm"
      >
        <div
          class="confirm-sheet"
          role="alertdialog"
          aria-modal="true"
          aria-labelledby="rule-destructive-confirm-title"
        >
          <h3
            id="rule-destructive-confirm-title"
            class="confirm-title"
          >
            {{
              destructiveConfirmKind === 'reset'
                ? '重置'
                : destructiveConfirmKind === 'importRule'
                  ? '导入 Rule XML'
                  : destructiveConfirmKind === 'importLeftJson'
                    ? '导入左侧 JSON'
                    : destructiveConfirmKind === 'importLeftSchema'
                      ? '导入左侧 Schema XML'
                      : destructiveConfirmKind === 'importRightJson'
                        ? '导入右侧 JSON'
                        : '导入右侧 Schema XML'
            }}
          </h3>
          <p class="confirm-body">
            {{
              destructiveConfirmKind === 'reset'
                ? '重置将清空当前修改，恢复内置输入源与示例规则，并重置右侧对照展示，是否继续？'
                : destructiveConfirmKind === 'importRule'
                  ? '导入将覆盖当前规则（含 id、描述与全部映射行），是否继续？'
                  : destructiveConfirmKind === 'importLeftJson'
                    ? '导入将替换左侧输入源 JSON 与推导出的 Schema 结构，是否继续？'
                    : destructiveConfirmKind === 'importLeftSchema'
                      ? '导入将替换左侧输入源推导内容（schema 与示例 JSON），是否继续？'
                      : destructiveConfirmKind === 'importRightJson'
                        ? '将覆盖右侧展示区中的 JSON（仅展示，不参与转换预览）。是否继续？'
                        : '将覆盖右侧展示区中的 schema 与示例 JSON（仅展示，不参与转换预览）。是否继续？'
            }}
          </p>
          <div class="confirm-actions">
            <button
              type="button"
              class="btn"
              @click="cancelDestructiveConfirm"
            >
              取消
            </button>
            <button
              type="button"
              class="btn primary"
              @click="confirmDestructiveProceed"
            >
              确定
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div
        v-if="rulePreviewOpen"
        class="xml-preview-backdrop"
        @click.self="rulePreviewOpen = false"
      >
        <div class="xml-preview-sheet">
          <div class="xml-preview-head">
            <h2>Rule XML</h2>
            <button
              type="button"
              class="btn"
              @click="rulePreviewOpen = false"
            >
              关闭
            </button>
          </div>
          <p class="panel-hint xml-preview-hint">
            预览（只读）
          </p>
          <div class="code-wrap xml-preview-code">
            <LineGutterText
              :model-value="ruleXmlText"
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
.rule-page {
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
  max-width: 62rem;
  font-size: 12px;
  color: var(--muted);
  line-height: 1.45;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 1rem;
  margin: 1rem 0;
  min-width: 0;
}
.meta label {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  gap: 0.4rem;
  font-size: 13px;
  flex-shrink: 0;
  white-space: nowrap;
}
.meta-desc-row {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
  min-width: 0;
}
.meta .inp {
  width: 128px;
}
.meta .inp.meta-wide {
  width: 148px;
  flex: 0 0 auto;
}
.meta-actions {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  flex-shrink: 0;
}
.meta-err {
  color: #c62828;
  font-size: 12px;
  margin: -0.25rem 0 0.75rem;
}
.grid {
  display: grid;
  /* 左窄、中宽、右适中 */
  grid-template-columns: minmax(176px, 0.85fr) minmax(300px, 1.65fr) minmax(200px, 0.95fr);
  gap: 12px;
  align-items: stretch;
  min-height: calc(100vh - 220px);
}
@media (max-width: 1320px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
.panel {
  display: flex;
  flex-direction: column;
  min-height: 320px;
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 10px 12px;
  min-width: 0;
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
.collapse-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  margin: 0 0 8px;
  padding: 6px 10px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  background: var(--panel-2);
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
}
.collapse-toggle:hover {
  border-color: var(--accent-dim);
  background: var(--panel);
}
.collapse-chevron {
  flex: 0 0 auto;
  width: 1rem;
  font-size: 11px;
  color: var(--muted);
}
.collapse-muted {
  flex: 1;
  min-width: 0;
  font-weight: 400;
  font-size: 12px;
  color: var(--muted);
  text-align: right;
}
.out-sample-collapsed {
  margin-bottom: 10px;
  min-height: 0;
}
.out-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 0 4px;
}
.out-title {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--text);
  letter-spacing: 0.04em;
}
.preview-refresh-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 6px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface, transparent);
  color: var(--muted);
  cursor: pointer;
  line-height: 0;
}
.preview-refresh-btn:hover {
  color: var(--text);
  border-color: var(--border);
  opacity: 0.95;
}
.out-hint {
  margin-bottom: 6px !important;
}
.out-placeholder {
  font-size: 12px;
  color: var(--muted);
  padding: 8px;
  margin-bottom: 8px;
  border: 1px dashed var(--border);
  border-radius: 6px;
}
.inp-preview :deep(.gwrap),
.out-sample :deep(.gwrap),
.mapped-preview-wrap :deep(.gwrap) {
  min-height: 160px;
}
.panel :deep(.tgw) {
  min-height: 180px;
}
.code-wrap {
  flex: 0 1 auto;
  display: flex;
  min-height: 0;
  margin-bottom: 8px;
  width: 100%;
}
.rule-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
  max-height: calc(100vh - 260px);
}
.empty {
  color: var(--muted);
  font-size: 13px;
  padding: 1rem 0;
}
.rule-block {
  margin-bottom: 8px;
  padding: 6px 8px;
  background: var(--panel-2);
  border: 1px solid var(--border);
  border-radius: 8px;
}
.rule-inline {
  display: flex;
  flex-wrap: nowrap;
  align-items: stretch;
  gap: 6px;
  min-width: 0;
  overflow-x: auto;
  padding-bottom: 2px;
}
.badge {
  flex: 0 0 auto;
  align-self: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--accent);
  background: rgba(37, 99, 235, 0.1);
  padding: 2px 7px;
  border-radius: 4px;
  line-height: 1.3;
}
.rule-field {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  font-size: 12px;
}
.rule-field.stretch {
  flex: 0 1 208px;
  min-width: 96px;
  max-width: 208px;
}
.rule-field.narrow {
  flex: 0 0 96px;
  min-width: 72px;
  max-width: 110px;
}
.rule-field .lf {
  flex: 0 0 auto;
  font-size: 11px;
  color: var(--muted);
  white-space: nowrap;
}
.rule-field .inp.mono {
  flex: 1;
  width: 0;
  min-height: 28px;
  margin: 0;
  padding: 4px 6px;
  border-radius: 4px;
  border: 1px solid var(--border);
}
.btn.mini {
  padding: 2px 8px;
  font-size: 14px;
}
.rm {
  flex: 0 0 auto;
  align-self: center;
  color: #c62828;
}
.mono {
  font-family: ui-monospace, Menlo, monospace;
  font-size: 11px;
}
.btn.primary {
  background: var(--accent);
  color: #fff;
  border-color: #1d4ed8;
}
.btn.primary:hover {
  background: #1d4ed8;
}
.meta-actions .btn-reset-meta {
  background: #fff;
  color: var(--text);
  border-color: var(--border);
}
.meta-actions .btn-reset-meta:hover {
  background: var(--panel-2);
  border-color: var(--accent);
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
  width: min(460px, 92vw);
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
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.22);
}
.xml-preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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
}
.xml-preview-code {
  flex: 1;
  min-height: 0;
  height: min(480px, 65vh);
}
.xml-preview-code :deep(.gwrap) {
  min-height: min(480px, 65vh);
}
</style>
