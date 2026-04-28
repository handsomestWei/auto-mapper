# auto-mapper

java json对象属性值自动映射转换。

## 仓库结构

| 目录 | 说明 |
|------|------|
| `automapper-core/` | **运行时**：按 schema / rule XML 解析与执行映射；支持 JSONPath、内置映射函数与 SPI 自定义扩展；提供 Java API（如对象→目标 JSON）、文件热更新等，供应用集成。 |
| `automapper-design-web/` | **可视化编辑**：浏览器内维护字段树结构与转换规则，生成可与「核心库」对齐的 schema XML / rule XML，并附映射结果预览辅助校验，独立于后端 JVM 部署。 |

## 简介

**auto-mapper** 是一套 **面向 JSON 报文的配置化结构与映射工具**：同一套配置文件既可供人读写，也可被 Java 运行时加载，把「接口报文长什么样、字段如何对应」从业务代码里抽离出来，减少硬编码与重复胶水逻辑。

要落地一次转换，通常只需三类输入：

1. **一份 JSON 样例**（或约定好的目标形态），用于推导或约束「目标结构」；
2. **schema XML**：描述字段树与类型等元信息，便于落盘、审查与版本管理；
3. **rule XML**：用 `from` / `to`（JSONPath）声明字段级映射，必要时在 `func` 中声明内置或自定义映射函数。

运行时通过 **schema id** 与 **rule id** 选用上述配置，将源数据（如 `JSONObject`、POJO）映射为目标结构。**不必**为每一条接口变更重写 Java POJO + 手写 setter 链路；规则的增删改优先落在 XML，再随发布或热更新生效。

与之配套：

- **`automapper-core`** 承担 **解析 XML、装载规则、执行 JSONPath、调用函数扩展、产出目标 JSON** 等能力，以 Maven 单模块的形式供业务工程依赖。
- **`automapper-design-web`** 不承担运行时转换，而是通过 **表单化 / 结构化编辑**降低 schema、rule 文件的编写门槛，并让导出的文本与引擎侧语义一致。

**典型适用场景**：低代码/API 网关/接口编排中的出入参对齐、多套系统或第三方网关报文字段映射、需要 **独立演进** schema 与规则且希望对运维与评审友好的团队。

## 设计思路

### 解耦

- 传统的工具，映射转换关系需要硬编码，需要生成 pojo java 文件；传统的 json schema 标准格式文件可读性较差
- 基于 xml 文件，使用 xml 描述映射规则，使用 xml 保存 json schema，提高可读和可维护性。减少胶水代码，且利用文件热更新能力，实现属性和规则的动态调整
- 可用于上层低代码平台的接口生成、api 网关的接口入出参管理、第三方接口对接开发等

### 属性映射

使用 ali fastjson 的 JSONPath 做属性路径访问。[path 语法参考](https://github.com/alibaba/fastjson/wiki/JSONPath)

### 映射函数

当 JSONPath 不满足需求时，可编写函数实现复杂的映射转换，使用时在 `rule 规则描述 xml` 的 `func` 属性中声明函数名

#### 内置函数

内置了部分映射函数

#### 自定义

可扩展，支持编写自定义函数处理。以 JDK SPI 方式注册

## 使用说明（automapper-core）

```bash
cd automapper-core
mvn compile
mvn test
```

示例代码：

```java
// step1 初始化。单例模式，全局做一次即可
AutoMapper autoMapper = AutoMapper.newInstance();

// step2 使用 json 示例自动生成 json schema xml
String schemaId = "testJson";
SchemaUtil.createSchemaFile(schemaId, "for test", jsonData, schemaDir);

// step3 定义转换规则，编写 rule.xml
String ruleId = "testRule";

// step4 对源 pojo，依据规则，转换成目标 testJson
JSONObject rs = AutoMapper.getInstance().map(srcObj, "testJson", "testRule");
```

## 前端设计器（automapper-design-web）

**功能面向**：在浏览器中完成与 **schema / rule 工程**相关的编辑与校验辅助，产物为普通 XML 文本，可与 `automapper-core` 使用的目录与命名约定对接。

- **Schema 设计器**：从 JSON 样例生成或调整字段树，实时生成与 `SchemaUtil` 思路一致的 **`*-schema.xml`**，支持导出、重置与结构树行号对照，减少手写 XML 出错。
- **字段转换规则设计器**：以表格形式维护多条 `from` / `to` / `func` / `val`，可按左侧 JSON 树上的路径插入 JSONPath；提供 **映射结果预览**（基于当前页的解析与内置规则语义在浏览器侧演算，用于对照，可能与线上引擎细节存在差异），便于在提交配置前自检。
- **部署形态**：静态前端资源本地或静态托管均可；不包含 Servlet、也不替代 `automapper-core` 运行时。

```bash
cd automapper-design-web
npm install
npm run dev
npm run build
npm run lint
```
