---
name: automapper-skill
description: >-
  配置驱动的 JSON 字段映射转换技能。从接口文档（截图、文本、Word、Markdown）生成 schema XML、JSON 样例；自动识别源与目标，每个接口一套产物，双侧都在时再出 rule。用户提到 automapper、接口文档出 schema、截图/Word 出 JSON 样例、源和目标映射或字段转换配置时使用。
---

# 配置驱动的 JSON 字段映射转换技能

把接口文档（截图 / 纯文本 / Word / Markdown）整理成 automapper 可用的 schema、JSON 样例；若同时识别到源和目标，再生成 rule。

## 何时使用

用户给出接口文档、字段表、报文样例，并要求产出 schema / JSON / 映射规则时使用本技能。详细 XML 约定见 [reference.md](reference.md)。

## 工作流

1. **抽取为 Markdown 字段表**（每个接口一套，一级标题一个接口）。
2. **自动判断源 / 目标**（请求、入参、内部 → 源；响应、出参、网关、第三方 → 目标）。
3. **运行生成器**（不要手写 XML，除非生成器无法覆盖的字段）。
4. **校验输出**，把未匹配字段写进 `NOTES.md`，必要时再人工改 rule。

### 1. 抽取

| 输入 | 做法 |
|------|------|
| 截图 / 图片 | 读图，把字段表抄成 Markdown 表格 |
| Word / `.docx` | 用文档工具或 `python-docx` 抽出表格与标题 |
| 已有 Markdown / 纯文本 | 直接使用 |

Markdown 约定：

- `# 中文名 camelId`：接口标题；拉丁标识作 schema/rule 的 `id` 与目录名。
- `##`：一侧报文标题，需能看出源或目标。
- 表头含：字段 / name、类型 / type、说明 / desc、示例 / eg。
- 嵌套路径用 `data[].id`、`data.innerObj.objId`。

形状见 `examples/query_order.md`、`examples/two_apis.md`、`examples/target_only.md`。

### 2. 源与目标

按标题关键词推断，**不要问用户哪张表是源**（除非两侧都无法判断）：

- 源：源、请求、入参、内部、request、source
- 目标：目标、响应、出参、网关、第三方、response、target、返回

两张表时：能识别则配对；否则默认第一张源、第二张目标。只有一侧时只出目标 schema + JSON，**不出 rule**。

### 3. 生成

在 `automapper-skill/` 下：

```bash
python -m tools --input <抽取后的.md> --out-dir <输出目录>
```

每个接口一个子目录：

| 文件 | 何时有 |
|------|--------|
| `{id}-schema.xml` | 目标（若仅一侧则用该侧） |
| `{id}-sample.json` | 同上 |
| `{id}Src-schema.xml`、`{id}-src-sample.json` | 识别到源 |
| `{id}-rule.xml` | 源和目标都有 |
| `NOTES.md` | 有生成说明时 |

### 4. 硬性约定

- 文件名与 XML `id` 一致：`queryOrder-schema.xml` ↔ `id="queryOrder"`。
- 类型只能是：`java.lang.String|Integer|Long|Double|Boolean|Object`、`java.util.List`。
- List 路径用 Fastjson 风格 `[0:]`。
- 列表先 `func="newList"`，嵌套对象再 `newObject`，然后才写叶子 `from`/`to`。
- 一条接口 = 一套输出，禁止把多个接口揉进同一个 schema。

生成器已做同名与常见别名匹配（如 `idd`→`id`、`sku`→`goodsId`）。对不上的目标字段不要瞎编 `from`，记入 `NOTES.md`。

## 不要做的事

- 不要把设计器 demo 或 `testJson`/`testRule` 的 id 复用到真实接口。
- 不要在未初始化 list/object 的情况下写 `[0:]` 叶子映射。
- 不要把 Word/截图直接当生成器输入；必须先抽成 Markdown 表。
