/**
 * 浏览器端规则执行，语义对齐 Java AutoMapper + NewListFunc + NewObjectFunc + JsonPathUtil。
 * 不实现全部内置函数；未识别的 func 将跳过（保持 target 不变）。
 */

/** 顶层骨架：结构与 Java shallowClone 类似；标量占位为 null（与 Java「拷贝字面量」不同）。
 *  规则跑完后会做 pruneUntouchedPreviewRootPlaceholders，删掉「模板有、但未被子规则 to 写到的顶层 null」，避免出现 code/null 与 code1/值并存。
 */
export function shallowCloneTemplate(tpl) {
  if (tpl === null || typeof tpl !== 'object' || Array.isArray(tpl)) {
    return tpl
  }
  const out = {}
  for (const [k, v] of Object.entries(tpl)) {
    if (Array.isArray(v)) {
      out[k] = v.length === 0 ? [] : null
    } else if (v !== null && typeof v === 'object' && !Array.isArray(v)) {
      out[k] = null
    } else {
      out[k] = null
    }
  }
  return out
}

const LIST_TOKEN = '[0:]'

/** @returns {string[]} 如 ['data[0:]','innerObj','objId'] */
export function parsePathSegments(path) {
  if (!path || path === '$') return []
  let s = path.trim()
  if (s.startsWith('$')) s = s.slice(1).replace(/^\./, '')
  if (!s) return []
  const parts = []
  let buf = ''
  for (let i = 0; i < s.length; i++) {
    const ch = s[i]
    if (ch === '.' && buf) {
      parts.push(buf)
      buf = ''
    } else {
      buf += ch
    }
  }
  if (buf) parts.push(buf)
  return parts
}

/**
 * segment 形如 `code`、`data[0:]`、`data[0]`、`innerObj`
 */
function parseSeg(seg) {
  if (!seg) return {}
  const mBr = /\[[^\]]*\]$/.exec(seg)
  if (!mBr) {
    return { name: seg, listW: false }
  }
  const base = seg.slice(0, -mBr[0].length)
  const br = mBr[0]
  if (br === LIST_TOKEN) {
    return { name: base, listW: true }
  }
  const num = br.match(/^\[(\d+)\]$/)
  if (num) {
    return { name: base, listW: false, index: parseInt(num[1], 10) }
  }
  return { name: seg, listW: false }
}

/** FastJSON 风格求值，支持 [0:]、[n]、链式 $.data[0:].a.b */
export function evalPath(root, path) {
  if (!path || path === '$') return root
  const segs = parsePathSegments(path)
  return walkEval(root, segs, 0)
}

function walkEval(cur, segs, i) {
  if (i >= segs.length) return cur
  if (cur === undefined || cur === null) return undefined
  const meta = parseSeg(segs[i])
  if (meta.index !== undefined) {
    const arr = cur[meta.name]
    if (!Array.isArray(arr)) return undefined
    const el = arr[meta.index]
    return walkEval(el, segs, i + 1)
  }
  const next = cur[meta.name]
  if (meta.listW) {
    if (!Array.isArray(next)) return undefined
    if (i === segs.length - 1) return next
    return next.map((el) => walkEval(el, segs, i + 1))
  }
  return walkEval(next, segs, i + 1)
}

export function pathSize(root, path) {
  const v = evalPath(root, path)
  return Array.isArray(v) ? v.length : 0
}

export function containsArrayToken(path) {
  return typeof path === 'string' && path.includes(LIST_TOKEN)
}

/** targetTpl 上取模板项：path 中 [0:] → [0] */
export function getTplItem(tplObj, path) {
  if (!path) return undefined
  const fixed = path.split(LIST_TOKEN).join('[0]')
  return evalPath(tplObj, fixed)
}

export function arraySetManyToMany(targetObject, targetPath, values) {
  const objects = values || []
  const len = objects.length
  for (let i = 0; i < len; i++) {
    const itemPath = targetPath.split(LIST_TOKEN).join(`[${i}]`)
    setPath(targetObject, itemPath, objects[i])
  }
}

export function arraySetOneToMany(targetObject, targetPath, tplValue) {
  const arrayPath = targetPath.substring(0, targetPath.lastIndexOf('.'))
  const size = pathSize(targetObject, arrayPath)
  for (let i = 0; i < size; i++) {
    const itemPath = targetPath.split(LIST_TOKEN).join(`[${i}]`)
    setPath(targetObject, itemPath, deepCloneValue(tplValue))
  }
}

function deepCloneValue(v) {
  if (v === null || typeof v !== 'object') return v
  return JSON.parse(JSON.stringify(v))
}

/** 不含 `[0:]` 通段的 set；每条路径已应为具体 `[i]`（由 arraySetMany/ManyToMany 替换） */
function setPathSimple(obj, path, value) {
  const segs = parsePathSegments(path)
  if (!segs.length) return
  let cur = obj
  for (let i = 0; i < segs.length - 1; i++) {
    const meta = parseSeg(segs[i])
    if (meta.listW) return
    if (meta.index !== undefined) {
      let arr = cur[meta.name]
      if (!Array.isArray(arr)) return
      if (arr[meta.index] == null) arr[meta.index] = {}
      cur = arr[meta.index]
      continue
    }
    if (cur[meta.name] == null || cur[meta.name] === undefined) cur[meta.name] = {}
    cur = cur[meta.name]
  }
  const last = parseSeg(segs[segs.length - 1])
  if (last.listW) return
  if (last.index !== undefined) {
    const arr = cur[last.name]
    if (!Array.isArray(arr)) return
    arr[last.index] = value
    return
  }
  cur[last.name] = value
}

export function setPath(targetObject, path, value) {
  setPathSimple(targetObject, path, value)
}

function executeNewList(srcObject, srcPath, targetObject, targetPath, targetTplObject) {
  const size = pathSize(srcObject, srcPath)
  const tplArr = evalPath(targetTplObject, targetPath)
  let tplItem = {}
  if (Array.isArray(tplArr) && tplArr.length > 0) {
    tplItem = tplArr[0]
  }
  let targetArr = evalPath(targetObject, targetPath)
  if (targetArr != null) return targetObject
  targetArr = []
  setPathSimple(targetObject, targetPath, targetArr)
  for (let i = 0; i < size; i++) {
    targetArr.push(deepCloneValue(typeof tplItem === 'object' && tplItem !== null ? tplItem : {}))
  }
  return targetObject
}

function executeNewObject(srcObject, targetObject, targetPath, targetTplObject) {
  const tplItem = getTplItem(targetTplObject, targetPath)
  const base =
    tplItem !== null &&
    tplItem !== undefined &&
    typeof tplItem === 'object' &&
    !Array.isArray(tplItem)
      ? deepCloneValue(tplItem)
      : {}
  if (containsArrayToken(targetPath)) {
    arraySetOneToMany(targetObject, targetPath, base)
  } else {
    setPathSimple(targetObject, targetPath, base)
  }
  return targetObject
}

function mapPlain(srcObject, srcPath, targetObject, targetPath, targetTplObject) {
  if (!targetPath) return targetObject
  let srcVal = evalPath(srcObject, srcPath)
  /** from 为空时仅由 func 消费，本条无 func 时直接返回 */
  if ((srcPath === '' || srcPath === undefined) && srcVal === undefined) {
    return targetObject
  }
  if (srcVal === null || srcVal === undefined) {
    return targetObject
  }
  if (Array.isArray(srcVal)) {
    arraySetManyToMany(targetObject, targetPath, srcVal)
    return targetObject
  }
  setPath(targetObject, targetPath, srcVal)
  return targetObject
}

function mapWithFunc(funcName, srcObject, srcPath, targetObject, targetPath, targetTplObject) {
  if (!funcName) return targetObject
  if (funcName === 'newList') {
    return executeNewList(srcObject, srcPath, targetObject, targetPath, targetTplObject)
  }
  if (funcName === 'newObject') {
    return executeNewObject(srcObject, targetObject, targetPath, targetTplObject)
  }
  /** 其余内置函数略过 */
  return targetObject
}

/** 每条规则的 to（含 newList/newObject）：取第一路径段的属性名，表示根上会有一个「被写上」的子树。*/
function collectWrittenRootNamesFromRules(rs) {
  const names = new Set()
  const list = Array.isArray(rs) ? rs : []
  for (const r of list) {
    const toRaw = typeof r?.to === 'string' ? r.to.trim() : ''
    if (!toRaw) continue
    const parts = parsePathSegments(toRaw)
    if (!parts.length) continue
    const nm = parseSeg(parts[0]).name
    if (nm) names.add(nm)
  }
  return names
}

/**
 * 删除仅由模板占位、且没有任何规则的 to 以该顶层名开头的根字段（值为 null）。
 * 解决预览里改写 to 后出现「code:null + code1:value」双线根字段。
 */
function pruneUntouchedPreviewRootPlaceholders(targetObject, tplTop, writtenRootNames) {
  if (
    targetObject === null ||
    typeof targetObject !== 'object' ||
    Array.isArray(targetObject) ||
    tplTop === null ||
    typeof tplTop !== 'object' ||
    Array.isArray(tplTop)
  ) {
    return
  }
  for (const k of Object.keys(targetObject)) {
    if (!Object.prototype.hasOwnProperty.call(tplTop, k)) continue
    if (!writtenRootNames.has(k) && targetObject[k] === null) {
      delete targetObject[k]
    }
  }
}

/**
 * @param {object} srcObject 解析后的源 JSON
 * @param {object} targetTplFull buildJsonExampleFromTree 得到的完整模板（未经 shallowClone）
 * @param {{from?:string,to?:string,func?:string,val?:string}[]} rs 规则列表
 */
export function executeRuleMapping(srcObject, targetTplFull, rs) {
  const targetTplObject = JSON.parse(JSON.stringify(targetTplFull))
  let targetObject = shallowCloneTemplate(targetTplFull)
  const list = Array.isArray(rs) ? rs : []
  for (const r of list) {
    const func = r.func?.trim?.() || ''
    const fromPath = r.from ?? ''
    const toPath = r.to ?? ''
    const valPart = r.val ?? ''
    void valPart

    try {
      if (func && func.length > 0) {
        targetObject = mapWithFunc(func, srcObject, fromPath, targetObject, toPath, targetTplObject)
      } else if (!toPath || toPath.trim() === '') {
        continue
      } else {
        targetObject = mapPlain(srcObject, fromPath, targetObject, toPath, targetTplObject)
      }
    } catch {
      /* keep targetObject */
    }
  }
  pruneUntouchedPreviewRootPlaceholders(targetObject, targetTplFull || {}, collectWrittenRootNamesFromRules(list))
  return targetObject
}
