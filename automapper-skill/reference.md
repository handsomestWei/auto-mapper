# automapper XML 约定

与 `automapper-core` 运行时及 `automapper-design-web` 设计器对齐。生成器（`tools`）已按这些规则输出。

## Schema

- 根：`<schema id="..." desc="...">`。
- `id` 必须等于文件名 `{id}-schema.xml` 的 `{id}`。
- 节点：`<p name="" type="" desc="" eg=""/>`，容器可嵌套 `<p>`。
- `type` 仅允许：

  `java.lang.String`、`java.lang.Integer`、`java.lang.Long`、`java.lang.Double`、`java.lang.Boolean`、`java.lang.Object`、`java.util.List`

- `List` / `Object` 应有子节点（纯标量列表极少见，尽量避免）。

文档类型别名 → Java 类型由 `tools.model.TYPE_ALIASES` 处理（如 `int`→`Integer`，`string`→`String`）。

## JSON 样例

与 schema 树同构。`List` 输出长度为 1 的数组，元素为子字段对象。示例值取表中「示例」列，并按类型转 int/float/bool。

## Rule

- 根：`<rule id="..." desc="..." serializerFeatures="6,7">`。
- `id` 必须等于 `{id}-rule.xml` 的 `{id}`。
- 条目：`<r from="" to="" func="" val=""/>`，按声明顺序执行。
- JSONPath 使用 Fastjson 列表通配 `[0:]`（不是 `[*]`）。
- 映射 list 前必须：

  `<r from="$.data" to="$.data" func="newList"/>`

- 嵌套对象：

  `<r from="" to="$.data[0:].innerObj" func="newObject"/>`

- 再写叶子：`<r from="$.data[0:].idd" to="$.data[0:].id"/>`。

运行时 `map()` 使用**目标** schema id + rule id；源 JSON 是运行输入。生成器仍会写出 `{id}Src-schema.xml` 与 `{id}-src-sample.json`，便于对照，不参与默认 map 选型。

## 字段匹配

目标叶子按规范化名在源中查找：去下划线、小写，再查别名表（`idd`→`id`、`sku`→`goodsId`、`qty`→`count` 等）。同名路径优先；找不到则该目标字段不写 `from`/`to`。

## 校验

`python -m tools ...` 默认会扫描输出目录：

- schema/rule 的 id 与文件名
- 类型枚举
- 使用 `[0:]` 的 `to` 之前是否已有对应路径的 `newList`
