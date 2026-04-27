<script setup>
import TreeNode from './TreeNode.vue'

const props = defineProps({
  nodes: { type: Array, default: () => [] }
})

const emit = defineEmits(['update'])

const typeOptions = [
  'java.lang.String',
  'java.lang.Integer',
  'java.lang.Long',
  'java.lang.Double',
  'java.lang.Boolean',
  'java.lang.Object',
  'java.util.List'
]

const isContainer = (n) => n.type === 'java.lang.Object' || n.type === 'java.util.List'

function patchAt(path, patch) {
  const next = deepCloneRoot(props.nodes)
  let list = next
  for (let i = 0; i < path.length - 1; i++) {
    const idx = path[i]
    if (!list[idx].children) list[idx].children = []
    list = list[idx].children
  }
  const last = path[path.length - 1]
  const node = { ...list[last], ...patch }
  if (patch.type) {
    if (patch.type === 'java.lang.Object' || patch.type === 'java.util.List') {
      if (!node.children) node.children = []
      delete node.eg
    } else {
      node.children = undefined
      if (node.eg === undefined) node.eg = ''
    }
  }
  list[last] = node
  emit('update', next)
}

function deepCloneRoot(nodes) {
  return JSON.parse(JSON.stringify(nodes))
}

function addChild(path) {
  const next = deepCloneRoot(props.nodes)
  const parent = getNodeByPath(next, path)
  if (!parent.children) parent.children = []
  parent.children.push({
    name: 'newKey',
    type: 'java.lang.String',
    eg: ''
  })
  emit('update', next)
}

function removeNode(path) {
  const next = deepCloneRoot(props.nodes)
  if (path.length === 0) return
  if (path.length === 1) {
    next.splice(path[0], 1)
    emit('update', next)
    return
  }
  const parentPath = path.slice(0, -1)
  const idx = path[path.length - 1]
  const parent = getNodeByPath(next, parentPath)
  if (parent.children) parent.children.splice(idx, 1)
  emit('update', next)
}

function getNodeByPath(list, path) {
  if (path.length === 0) return null
  let cur = list
  for (let i = 0; i < path.length; i++) {
    const idx = path[i]
    if (i === path.length - 1) return cur[idx]
    if (!cur[idx].children) cur[idx].children = []
    cur = cur[idx].children
  }
  return null
}
</script>

<template>
  <div class="tree">
    <TreeNode
      v-for="(n, i) in nodes"
      :key="i + n.name + n.type"
      :node="n"
      :path="[i]"
      :type-options="typeOptions"
      :is-container="isContainer"
      @patch="(pth, p) => patchAt(pth, p)"
      @add-child="addChild"
      @remove="removeNode"
    />
    <p v-if="!nodes || nodes.length === 0" class="empty-hint">无字段，请从左侧 JSON 或右侧 XML 输入</p>
  </div>
</template>

<style scoped>
.tree {
  min-height: 100%;
  font-size: 13px;
}
.empty-hint {
  color: var(--muted);
  margin: 0.5rem 0;
  font-size: 12px;
}
</style>
