<script setup>
import { computed } from 'vue'

const props = defineProps({
  node: { type: Object, required: true },
  path: { type: Array, required: true },
  typeOptions: { type: Array, required: true },
  isContainer: { type: Function, required: true },
  lineByPath: { type: Object, required: true }
})

const emit = defineEmits(['patch', 'add-child', 'remove'])

const rowLine = computed(() => props.lineByPath.get(props.path.join('-')) || '')

const showAddChild = computed(
  () => props.node.type === 'java.util.List' || props.node.type === 'java.lang.Object'
)

function onNameInput(e) {
  emit('patch', props.path, { name: e.target.value })
}

function onTypeChange(e) {
  emit('patch', props.path, { type: e.target.value })
}

function onEgInput(e) {
  emit('patch', props.path, { eg: e.target.value })
}

function onDescInput(e) {
  emit('patch', props.path, { desc: e.target.value })
}

function onAddChild() {
  emit('add-child', props.path)
}

function onRemove() {
  emit('remove', props.path)
}
</script>

<template>
  <div
    class="node"
    :style="{ marginLeft: path.length > 1 ? '0.75rem' : '0' }"
  >
    <div class="row">
      <span
        v-if="rowLine"
        class="tln"
        :title="'行 ' + rowLine"
      >{{ rowLine }}</span>
      <input
        class="inp name"
        type="text"
        :value="node.name"
        title="字段名"
        @input="onNameInput"
      >
      <select
        class="sel"
        :value="node.type"
        title="类型"
        @change="onTypeChange"
      >
        <option
          v-for="t in typeOptions"
          :key="t"
          :value="t"
        >
          {{ t }}
        </option>
      </select>
      <input
        v-if="!isContainer(node)"
        class="inp eg"
        type="text"
        :value="node.eg ?? ''"
        placeholder="eg"
        title="示例值"
        @input="onEgInput"
      >
      <input
        class="inp desc"
        type="text"
        :value="node.desc ?? ''"
        placeholder="desc"
        title="描述（可选）"
        @input="onDescInput"
      >
      <button
        v-if="showAddChild"
        type="button"
        class="btn mini"
        title="为 List / Object 添加子字段"
        @click="onAddChild"
      >
        +
      </button>
      <button
        type="button"
        class="btn mini danger"
        title="删除"
        @click="onRemove"
      >
        ×
      </button>
    </div>
    <div
      v-if="isContainer(node) && node.children"
      class="children"
    >
      <TreeNode
        v-for="(c, i) in node.children"
        :key="path.join('-') + '-' + i + '-' + c.name"
        :node="c"
        :path="[...path, i]"
        :type-options="typeOptions"
        :is-container="isContainer"
        :line-by-path="lineByPath"
        @patch="(pth, p) => $emit('patch', pth, p)"
        @add-child="(pth) => $emit('add-child', pth)"
        @remove="(pth) => $emit('remove', pth)"
      />
    </div>
  </div>
</template>

<style scoped>
.row {
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
.inp {
  border: 1px solid var(--border);
  border-radius: 4px;
  padding: 4px 6px;
  background: var(--bg);
  color: var(--text);
  font-size: 12px;
}
.inp.name {
  width: 100px;
  font-weight: 500;
}
.sel {
  max-width: 150px;
  font-size: 12px;
  padding: 3px 4px;
  border-radius: 4px;
  border: 1px solid var(--border);
  background: var(--bg);
  color: var(--text);
}
.inp.eg {
  flex: 1;
  min-width: 60px;
}
.inp.desc {
  flex: 1;
  min-width: 50px;
  opacity: 0.95;
}
.btn.mini {
  padding: 2px 8px;
  font-size: 14px;
  line-height: 1.2;
}
.danger {
  color: #c62828;
}
.children {
  margin-top: 2px;
  border-left: 2px solid var(--accent-dim);
  padding-left: 4px;
}
</style>
