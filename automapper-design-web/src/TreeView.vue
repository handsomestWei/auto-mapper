<script setup>
import { computed } from 'vue'
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

const lineByPath = computed(() => {
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
  dfs(props.nodes, [])
  return m
})

/**
 * 只读取路径上的节点，不制造 children = [] 之类副作用
 */
function getNodeByPathReadOnly(list, path) {
  let cur = list
  for (let d = 0; d < path.length; d++) {
    const idx = path[d]
    if (idx < 0 || idx >= cur.length) return null
    const node = cur[idx]
    if (d === path.length - 1) return node
    if (!node.children) return null
    cur = node.children
  }
  return null
}

function patchAt(path, patch) {
  const next = deepCloneRoot(props.nodes)
  let list = next
  for (let i = 0; i < path.length - 1; i++) {
    const idx = path[i]
    if (idx < 0 || idx >= list.length) return
    if (!list[idx].children) list[idx].children = []
    list = list[idx].children
  }
  const last = path[path.length - 1]
  if (last < 0 || last >= list.length) return
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
  const parent = getNodeByPathReadOnly(next, path)
  if (!parent) return
  if (parent.type !== 'java.util.List' && parent.type !== 'java.lang.Object') {
    return
  }
  if (!parent.children) parent.children = []
  parent.children.push({
    name: 'newKey',
    type: 'java.lang.String',
    eg: '',
    desc: ''
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
  const parent = getNodeByPathReadOnly(next, parentPath)
  if (parent && parent.children) parent.children.splice(idx, 1)
  emit('update', next)
}
</script>

<template>
  <div class="tree">
    <TreeNode
      v-for="(n, i) in nodes"
      :key="i + n.name + n.type + (lineByPath.get(String(i)) || '')"
      :node="n"
      :path="[i]"
      :type-options="typeOptions"
      :is-container="isContainer"
      :line-by-path="lineByPath"
      @patch="(pth, p) => patchAt(pth, p)"
      @add-child="addChild"
      @remove="removeNode"
    />
    <p
      v-if="!nodes || nodes.length === 0"
      class="empty-hint"
    >
      无字段，请在左侧粘贴 JSON 样例
    </p>
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
