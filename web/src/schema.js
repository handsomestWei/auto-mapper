/**
 * 与后端的 SchemaUtil（JSON→schema XML）语义对齐：
 * - 对象 → type java.lang.Object，子节点为 p
 * - 数组取首元素为模板 → type java.util.List，子结构来自首项
 * - 标量 → java 类型 + eg=字符串值
 */

const TYPE_LIST = 'java.util.List'
const TYPE_OBJECT = 'java.lang.Object'

function javaTypeForScalar(value) {
  if (value === null) return 'java.lang.String'
  if (typeof value === 'boolean') return 'java.lang.Boolean'
  if (typeof value === 'string') return 'java.lang.String'
  if (typeof value === 'number') {
    if (Number.isInteger(value)) return 'java.lang.Integer'
    return 'java.lang.Double'
  }
  return 'java.lang.String'
}

function buildProperty(name, data) {
  if (data !== null && typeof data === 'object' && !Array.isArray(data)) {
    return {
      name,
      type: TYPE_OBJECT,
      eg: '',
      children: buildChildrenFromObject(data)
    }
  }
  if (Array.isArray(data)) {
    const first = data.length ? data[0] : null
    let template = first
    if (first !== null && typeof first === 'object' && !Array.isArray(first)) {
      template = first
    } else if (first !== null) {
      template = { value: first }
    } else {
      template = {}
    }
    return {
      name,
      type: TYPE_LIST,
      eg: '',
      children: buildChildrenFromObject(
        first !== null && typeof first === 'object' && !Array.isArray(first) ? first : template
      )
    }
  }
  return {
    name,
    type: javaTypeForScalar(data),
    eg: data === null || data === undefined ? '' : String(data)
  }
}

function buildChildrenFromObject(obj) {
  if (obj === null || typeof obj !== 'object' || Array.isArray(obj)) return []
  return Object.keys(obj).map((k) => buildProperty(k, obj[k]))
}

export function jsonStringToObject(jsonStr) {
  const t = jsonStr.trim()
  if (!t) return {}
  return JSON.parse(t)
}

export function objectToJsonString(obj) {
  return JSON.stringify(obj, null, 2)
}

export function buildSchemaFromJsonString(schemaId, schemaDesc, jsonStr) {
  const obj = jsonStringToObject(jsonStr)
  return {
    id: schemaId,
    desc: schemaDesc,
    children: buildChildrenFromObject(obj)
  }
}

export function buildJsonExampleFromTree(tree) {
  const o = {}
  for (const c of tree.children) {
    o[c.name] = xmlElementToJson(c)
  }
  return o
}

function xmlElementToJson(p) {
  if (!p.children || p.children.length === 0) {
    if (p.type === TYPE_LIST) {
      return []
    }
    return parseExampleValue(p.type, p.eg, p.defVal)
  }
  if (p.type === TYPE_LIST) {
    const inner = {}
    for (const c of p.children) {
      inner[c.name] = xmlElementToJson(c)
    }
    return [inner]
  }
  if (p.type === TYPE_OBJECT) {
    const o = {}
    for (const c of p.children) {
      o[c.name] = xmlElementToJson(c)
    }
    return o
  }
  // 带嵌套的标量？按对象处理
  const o = {}
  for (const c of p.children) {
    o[c.name] = xmlElementToJson(c)
  }
  return o
}

function parseExampleValue(type, eg, defVal) {
  const raw = defVal !== undefined && defVal !== '' ? defVal : eg
  if (type === 'java.lang.String') return String(raw)
  if (type === 'java.lang.Boolean') return raw === 'true' || raw === true
  if (type === 'java.lang.Integer') {
    const n = parseInt(String(raw), 10)
    return Number.isNaN(n) ? 0 : n
  }
  if (type === 'java.lang.Long') {
    const n = parseInt(String(raw), 10)
    return Number.isNaN(n) ? 0 : n
  }
  if (type === 'java.lang.Double') {
    const n = parseFloat(String(raw))
    return Number.isNaN(n) ? 0 : n
  }
  return String(raw)
}

const ESCAPE_MAP = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }

function escapeAttr(s) {
  return String(s).replace(/[&<>"]/g, (c) => ESCAPE_MAP[c] || c)
}

function appendPXml(lines, p, depth) {
  const pad = '    '.repeat(depth)
  const hasKids = p.children && p.children.length > 0
  const selfClosing = !hasKids
  if (selfClosing) {
    lines.push(
      `${pad}<p name="${escapeAttr(p.name)}"` +
        (p.desc ? ` desc="${escapeAttr(p.desc)}"` : '') +
        ` type="${escapeAttr(p.type)}"` +
        (p.defVal !== undefined && p.defVal !== '' ? ` defVal="${escapeAttr(p.defVal)}"` : '') +
        ` eg="${escapeAttr(p.eg ?? '')}"/>`
    )
    return
  }
  lines.push(
    `${pad}<p name="${escapeAttr(p.name)}"` +
      (p.desc ? ` desc="${escapeAttr(p.desc)}"` : '') +
      ` type="${escapeAttr(p.type)}"` +
      (p.defVal !== undefined && p.defVal !== '' ? ` defVal="${escapeAttr(p.defVal)}"` : '') +
      ` eg="${escapeAttr(p.eg ?? '')}">`
  )
  for (const c of p.children) {
    appendPXml(lines, c, depth + 1)
  }
  lines.push(`${pad}</p>`)
}

export function schemaToXmlString(schema) {
  const lines = [
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>',
    `<schema id="${escapeAttr(schema.id)}" desc="${escapeAttr(schema.desc)}">`
  ]
  for (const p of schema.children) {
    appendPXml(lines, p, 1)
  }
  lines.push('</schema>')
  return lines.join('\n')
}

function readAttr(el, name) {
  return el.getAttribute(name) || ''
}

function parsePElement(pEl) {
  const n = { name: readAttr(pEl, 'name') }
  const desc = readAttr(pEl, 'desc')
  if (desc) n.desc = desc
  n.type = readAttr(pEl, 'type') || 'java.lang.String'
  const defVal = readAttr(pEl, 'defVal')
  if (defVal) n.defVal = defVal
  n.eg = readAttr(pEl, 'eg') ?? ''
  const childP = Array.from(pEl.getElementsByTagName('p')).filter((c) => c.parentNode === pEl)
  if (childP.length) {
    n.children = childP.map((c) => parsePElement(c))
  }
  return n
}

export function parseSchemaXmlString(xmlText) {
  const parser = new DOMParser()
  const doc = parser.parseFromString(xmlText, 'text/xml')
  const err = doc.querySelector('parsererror')
  if (err) {
    throw new Error(err.textContent || 'XML 解析失败')
  }
  const root = doc.documentElement
  if (!root || root.localName !== 'schema') {
    throw new Error('根元素应为 <schema>')
  }
  const schema = {
    id: readAttr(root, 'id') || 'schema',
    desc: readAttr(root, 'desc') || '',
    children: []
  }
  const topP = Array.from(root.getElementsByTagName('p')).filter((c) => c.parentNode === root)
  schema.children = topP.map((p) => parsePElement(p))
  return schema
}
