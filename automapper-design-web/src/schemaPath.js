/** 由 schema UI 的树路径推导 JSONPath（与后端 fastjson JSONPath、`[0:]` List 占位一致） */

export function getNodeAtPath(children, path) {
  if (!path.length) return null
  let cur = children
  for (let d = 0; d < path.length; d++) {
    const idx = path[d]
    const node = cur[idx]
    if (!node) return null
    if (d === path.length - 1) return node
    cur = node.children || []
  }
  return null
}

/**
 * @param {Array} rootChildren schema.children
 * @param {number[]} path TreeView 式路径下标数组
 * @returns {string} e.g. $.code、$.data[0:].innerObj.objId
 */
export function jsonPathForSchemaNode(rootChildren, path) {
  if (!path.length) return '$'
  let acc = '$'
  let cur = rootChildren
  for (let d = 0; d < path.length; d++) {
    const idx = path[d]
    const node = cur[idx]
    if (!node) return acc
    const name = node.name
    if (d === 0) {
      acc += '.' + name
    } else {
      const parentPath = path.slice(0, d)
      const parent = getNodeAtPath(rootChildren, parentPath)
      if (parent && parent.type === 'java.util.List') {
        acc += '[0:].' + name
      } else {
        acc += '.' + name
      }
    }
    cur = node.children || []
  }
  return acc
}
