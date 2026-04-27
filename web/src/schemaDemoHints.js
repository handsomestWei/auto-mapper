/**
 * 为样例数据补充树节点上的 desc、defVal（与 XML 一致），eg 仍来自 JSON/树编辑。
 * 点路径：顶级 "a"，嵌套 "a.b"。
 */
export const DEFAULT_DEMO_PATH_HINTS = {
  version: { desc: '数据或接口契约的主版本号，便于兼容' },
  ok: { desc: '整笔请求在网关层是否成功' },
  code: { desc: '业务状态码；0=成功，非 0=业务异常', defVal: '0' },
  msg: { desc: '对 code 的补充说明，面向调用方/运维阅读' },
  data: { desc: '主业务数组，结构由首条元素决定' },
  'data.id': { desc: '行记录业务主键或外部 id' },
  'data.score': { desc: '分数字段，可映射为 Double' },
  'data.innerObj': { desc: '行内扩展对象' },
  'data.innerObj.objId': { desc: '与第三方系统一致的外部对象 id' },
  'data.innerObj.label': { desc: '用于列表/卡片展示的短文案' },
  meta: { desc: '请求级元信息（追踪、时间等）' },
  'meta.traceId': { desc: '分布式追踪 id，同一次请求全链路相同' }
}

export function applyPathHintsToNodes(nodes, pathPrefix) {
  if (!nodes || !nodes.length) return
  for (const n of nodes) {
    const full = pathPrefix ? `${pathPrefix}.${n.name}` : n.name
    const h = DEFAULT_DEMO_PATH_HINTS[full]
    if (h) {
      if (h.desc) n.desc = h.desc
      if (h.defVal !== undefined) n.defVal = h.defVal
    }
    if (n.children && n.children.length) {
      applyPathHintsToNodes(n.children, full)
    }
  }
}
