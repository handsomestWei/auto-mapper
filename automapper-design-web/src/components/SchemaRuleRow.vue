<script setup>
import { computed } from 'vue'
import { jsonPathForSchemaNode } from '../schemaPath.js'
import SchemaRuleRow from './SchemaRuleRow.vue'

const props = defineProps({
  node: { type: Object, required: true },
  path: { type: Array, required: true },
  rootChildren: { type: Array, required: true },
  /** Schema 行号 Map（path key -> 行号字符串）；传 Map 亦可 */
  lineByPath: {
    type: [Object, Map],
    required: true
  },
  /** false 时不显示「+」，用于只读目标侧结构树 */
  showAdd: { type: Boolean, default: true }
})

const emit = defineEmits(['add-rule'])

const rowLine = computed(() => props.lineByPath.get(props.path.join('-')) || '')
const jsonPath = computed(() => jsonPathForSchemaNode(props.rootChildren, props.path))

const isContainer = (n) => n.type === 'java.lang.Object' || n.type === 'java.util.List'

function onAdd() {
  emit('add-rule', { path: [...props.path], jsonPath: jsonPath.value })
}
</script>

<template>
  <div class="node">
    <div class="sr-row">
      <span
        v-if="rowLine"
        class="tln"
        :title="'行 ' + rowLine"
      >{{ rowLine }}</span>
      <span
        class="nm"
        :title="jsonPath"
      >{{ node.name }}</span>
      <span class="tp">{{ node.type }}</span>
      <button
        v-if="showAdd"
        type="button"
        class="btn mini add-r"
        title="在此 JSONPath 添加一条转换规则（默认 from/to 为该路径）"
        @click="onAdd"
      >
        +
      </button>
    </div>
    <div
      v-if="isContainer(node) && node.children && node.children.length"
      class="children"
    >
      <SchemaRuleRow
        v-for="(c, i) in node.children"
        :key="path.join('-') + '-' + i + '-' + c.name"
        :node="c"
        :path="[...path, i]"
        :root-children="rootChildren"
        :line-by-path="lineByPath"
        :show-add="showAdd"
        @add-rule="(p) => $emit('add-rule', p)"
      />
    </div>
  </div>
</template>

<style scoped>
.node {
  font-size: 13px;
}
.sr-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  padding: 4px 6px;
  background: var(--panel-2);
  border-radius: 6px;
  border: 1px solid var(--border);
}
.tln {
  flex: 0 0 1.5rem;
  text-align: right;
  font-size: 11px;
  color: var(--gutter);
  user-select: none;
  font-family: ui-monospace, Menlo, monospace;
}
.nm {
  font-weight: 600;
  min-width: 80px;
  color: var(--text);
}
.tp {
  flex: 1;
  font-size: 11px;
  color: var(--muted);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}
.btn.mini {
  padding: 2px 8px;
  font-size: 14px;
  line-height: 1.2;
}
.add-r {
  margin-left: auto;
  flex-shrink: 0;
}
.children {
  margin-top: 2px;
  border-left: 2px solid var(--accent-dim);
  padding-left: 6px;
}
</style>
