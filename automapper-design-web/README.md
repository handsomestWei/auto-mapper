# automapper-design-web

浏览器里的 **schema / rule 可视化编辑器**：用 JSON 样例生成字段树、对照树编写映射规则，导出与 [`automapper-core`](../automapper-core/README.md) 约定一致的 XML。适合内外接口对接、API 网关出入参对齐时，把「报文长什么样、字段怎么对应」配清楚再交给运行时执行。

本模块是 **纯静态前端**（Vue 3 + Vite），不执行线上映射，也不替代 core。

## 能做什么

| 页面 | 路径 | 作用 |
|------|------|------|
| 首页 | `/` | 入口与模块说明 |
| Schema 设计器 | `/schema` | 从 JSON 生成 / 编辑字段树，导入、预览、导出 `*-schema.xml` |
| 字段转换规则设计器 | `/rule` | 对照结构树编写 `from` / `to` / `func` / `val`，导入、预览、导出 `*-rule.xml`，并做映射结果预览 |

### Schema 设计器

- 左侧编辑 JSON 样例（粘贴或「导入 json」），解析后刷新右侧字段树。
- 右侧可视化改字段名、类型、样例值、描述，可增删子节点。
- 「导入 schema」用已有 XML 覆盖当前树；「预览 / 导出 schema」得到可落盘的 XML。
- 「重置」恢复内置示例。

导出文件名建议与 core 一致：`{schemaId}-schema.xml`，放到 `automapper/schema/`。

### 字段转换规则设计器

- **左侧输入源**：导入 JSON 或 schema，只读展示树；节点上的「+」按该字段的 JSONPath 插入一条规则。
- **中间规则表**：按顺序编辑 `from`、`to`、`func`、`val`（与 core 的 `<r>` 对应）。
- **右侧输出源**：可选导入目标 JSON / schema，仅作对照，不参与预览计算。
- 「转换结果预览」按当前左侧 JSON 与规则在 **浏览器里演算**，便于自检；自定义 `func` 会被忽略，细节也可能与 core 不完全一致，上线前请用运行时再跑一遍。
- 「导入 / 预览 / 导出 rule」管理 `*-rule.xml`。

导出文件名建议：`{ruleId}-rule.xml`，放到 `automapper/rule/`。

## 怎样运行启动

需要本机已安装 **Node.js 18+** 和 npm。

```bash
cd automapper-design-web
npm install
npm run dev
```

默认开发地址：**http://localhost:5173/**  
若 5173 已被占用，Vite 会顺延到 5174、5175…，以终端里打印的 Local 地址为准。

浏览器打开后即可进入首页，再点「Schema 设计器」或「字段转换规则设计器」。

| 命令 | 说明 |
|------|------|
| `npm run dev` | 本地开发（热更新） |
| `npm run build` | 打包到 `dist/`，可丢到任意静态托管 |
| `npm run preview` | 本地预览构建结果 |
| `npm run lint` | ESLint 检查并自动修复 |

开发时改 `src/` 下的 Vue 文件会自动刷新。不需要启动 Java / Maven，也不需要连数据库。

## 和 core 的关系

设计器负责 **配和导出**；core 负责 **加载 XML 并真正映射**。两边共用同一套 schema / rule 文本约定。更完整的运行时说明见 [automapper-core/README.md](../automapper-core/README.md)。
