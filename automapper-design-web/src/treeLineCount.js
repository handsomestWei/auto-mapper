export function countSchemaTreeNodes(nodes) {
  if (!nodes || !nodes.length) return 0
  let c = 0
  for (const n of nodes) {
    c += 1
    if (n.children && n.children.length) c += countSchemaTreeNodes(n.children)
  }
  return c
}
