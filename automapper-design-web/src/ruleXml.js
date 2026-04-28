/** 对应 RuleRootElement / RuleSingleElement，`r` 与测试资源 testRule-rule.xml */

const ESCAPE_MAP = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }

function escapeAttr(s) {
  return String(s).replace(/[&<>"]/g, (c) => ESCAPE_MAP[c] || c)
}

function readAttr(el, name) {
  return el.getAttribute(name) || ''
}

/**
 * @returns {{ id:string, desc:string, serializerFeatures:string, rs:{from,to,func,val}[] }}
 */
export function parseRuleXmlString(xmlText) {
  const doc = new DOMParser().parseFromString(xmlText, 'text/xml')
  const err = doc.querySelector('parsererror')
  if (err) throw new Error(err.textContent?.trim() || 'XML 解析失败')

  const root = doc.documentElement
  if (!root || root.localName !== 'rule') {
    throw new Error('根元素应为 <rule>')
  }

  const id = readAttr(root, 'id')
  const desc = readAttr(root, 'desc')
  const serializerFeatures = readAttr(root, 'serializerFeatures')

  /** 仅顶层直接子 `r`，与 JAXB Unmarshaller 语义一致（无嵌套 r） */
  const rs = [...root.children]
    .filter((el) => el.localName === 'r')
    .map((el) => ({
      from: readAttr(el, 'from'),
      to: readAttr(el, 'to'),
      func: readAttr(el, 'func'),
      val: readAttr(el, 'val')
    }))

  return { id, desc, serializerFeatures, rs }
}

/**
 * @param {{ id:string, desc:string, serializerFeatures:string, rs:{from?,to?,func?,val?}[] }} rule
 */
export function ruleToXmlString(rule) {
  const attr = [
    `id="${escapeAttr(rule.id ?? '')}"`,
    `desc="${escapeAttr(rule.desc ?? '')}"`
  ]
  if (rule.serializerFeatures) {
    attr.push(`serializerFeatures="${escapeAttr(rule.serializerFeatures)}"`)
  }
  const lines = [
    `<?xml version="1.0" encoding="UTF-8"?>`,
    `<rule ${attr.join(' ')}>`
  ]

  const rows = rule.rs || []
  for (const r of rows) {
    const parts = [`from="${escapeAttr(r.from ?? '')}"`, `to="${escapeAttr(r.to ?? '')}"`]
    if (r.func) parts.push(`func="${escapeAttr(r.func)}"`)
    if (r.val) parts.push(`val="${escapeAttr(r.val)}"`)
    lines.push(`    <r ${parts.join(' ')}/>`)
  }
  lines.push('</rule>')
  return lines.join('\n')
}
