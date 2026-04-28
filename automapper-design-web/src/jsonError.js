/**
 * 将 JSON.parse 等错误与字符串位置信息结合，便于带行号展示。
 * V8 错误信息如: Unexpected token X in JSON at position 12
 */
export function indexToLineCol(str, pos) {
  if (pos < 0) pos = 0
  if (pos > str.length) pos = str.length
  const before = str.slice(0, pos)
  const line = (before.match(/\n/g) || []).length + 1
  const lastNl = before.lastIndexOf('\n')
  const col = pos - (lastNl + 1) + 1
  return { line, col, pos }
}

export function formatJsonParseError(err, text) {
  const msg = err && err.message ? err.message : String(err)
  const m = msg.match(/position\s+(\d+)/i)
  if (m) {
    const pos = parseInt(m[1], 10)
    const { line, col } = indexToLineCol(text, pos)
    return `${msg}（第 ${line} 行，第 ${col} 列，字符位置 ${pos}）`
  }
  const trimmed = (text || '').trimEnd()
  if (msg.includes('JSON') && trimmed.length) {
    const { line, col, pos: endPos } = indexToLineCol(text, text.length)
    return `${msg}（文本次尾约第 ${line} 行，第 ${col} 列，索引 ${endPos}）`
  }
  return msg
}

/**
 * JSON 解析错误对应的 1-based 行号（与 formatJsonParseError 规则一致），无则 null。
 */
export function getJsonParseErrorLine(err, text) {
  const msg = err && err.message ? err.message : String(err)
  const m = msg.match(/position\s+(\d+)/i)
  if (m) {
    const pos = parseInt(m[1], 10)
    return indexToLineCol(text, pos).line
  }
  const t = text || ''
  if (msg.includes('JSON') && t.trimEnd().length) {
    return indexToLineCol(t, t.length).line
  }
  return null
}
