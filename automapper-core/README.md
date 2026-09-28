# automapper-core

**automapper-core** 是 auto-mapper 的 **Java 运行时**：按 schema / rule XML 解析与执行映射，支持 JSONPath、内置映射函数与 SPI 自定义扩展，提供对象 → 目标 JSON 的 API，并监听配置文件热更新。以 **Maven 单模块**形式供业务工程依赖，不包含可视化编辑能力（编辑器见仓库内 `automapper-design-web/`）。

本模块对应仓库根 [README](../README.md) 中的职责划分：**解析 XML、装载规则、执行 JSONPath、调用函数扩展、产出目标 JSON**。

## 职责与边界

| 能力 | 说明 |
|------|------|
| 解析 XML | JAXB 读取 `*-schema.xml` / `*-rule.xml`，还原字段树与映射条目 |
| 装载规则 | 按 **schema id** / **rule id** 延迟加载并缓存；id 与文件名约定一致 |
| 执行 JSONPath | 使用 [Alibaba Fastjson JSONPath](https://github.com/alibaba/fastjson/wiki/JSONPath) 读写源/目标字段 |
| 调用函数扩展 | `func` 命中内置函数或 SPI 自定义实现；同名时 **自定义优先** |
| 产出目标 JSON | 以 schema（或 Class）生成目标模板，按规则逐条映射，返回 `JSONObject` |
| 热更新 | 监听 schema / rule 目录，已缓存配置在文件变更或删除时刷新 |

## 坐标与构建

```xml
<dependency>
    <groupId>com.wjy</groupId>
    <artifactId>automapper-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

GitHub Packages：`https://maven.pkg.github.com/handsomestWei/auto-mapper`

```bash
cd automapper-core
mvn compile
mvn test
```

要求 **JDK 8**。核心依赖包括 Fastjson、Jackson XML、JAXB、commons-io（文件监听）、Reflections（扫描内置函数）。

## 配置文件约定

默认从 classpath 资源根目录读取（`ClassLoader.getResource("")`）：

| 类型 | 默认目录 | 文件名 | 根节点 |
|------|----------|--------|--------|
| Schema | `automapper/schema/` | `{schemaId}-schema.xml` | `<schema id="...">` |
| Rule | `automapper/rule/` | `{ruleId}-rule.xml` | `<rule id="...">` |

**id 必须与文件名去掉后缀后的部分一致**，且全局唯一。初始化时也可指定绝对/相对目录：

```java
AutoMapper.newInstance("/opt/conf/automapper/schema/", "/opt/conf/automapper/rule/");
```

### Schema XML

描述目标 JSON 的字段树（名称、类型、样例、默认值、嵌套）。可用 `SchemaUtil.createSchemaFile` 从 JSON 样例生成。

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<schema id="testJson" desc="for test">
    <p name="code" type="java.lang.Integer" eg="0"/>
    <p name="msg" type="java.lang.String" eg="success"/>
    <p name="data" type="java.util.List" eg="">
        <p name="id" type="java.lang.String" eg=""/>
        <p name="innerObj" type="java.lang.Object" eg="">
            <p name="objId" type="java.lang.String" eg=""/>
        </p>
    </p>
</schema>
```

`<p>` 属性：`name`、`desc`、`type`（如 `java.lang.String` / `java.util.List` / `java.lang.Object`）、`defVal`、`eg`；对象与列表可再嵌套 `<p>`。

运行时会把 schema 转成 JSON 模板：列表类型会预置一个元素，避免泛型擦除后丢失结构。

### Rule XML

用 `from` / `to`（JSONPath）声明字段映射；`func` / `val` 可选。**按声明顺序逐条执行**。

```xml
<!-- serializerFeatures 为 Fastjson SerializerFeature 枚举的 ordinal，多个用逗号分隔 -->
<rule id="testRule" desc="for test" serializerFeatures="6,7">
    <r from="$.code" to="$.code"/>
    <r from="$.msg" to="$.msg"/>
    <r from="$.data" to="$.data" func="newList"/>
    <r from="" to="$.data[0:].innerObj" func="newObject"/>
    <r from="$.data[0:].idd" to="$.data[0:].id"/>
    <r from="$.data[0:].idd" to="$.data[0:].innerObj.objId"/>
</rule>
```

无 `func` 时：从 `from` 取值写入 `to`。源值为 `List` 时按多对多写入目标路径。

列表通配使用 Fastjson 风格的 **`[0:]`**（不是 `[*]`），例如 `$.data[0:].id`。

## 映射 API

```java
// 全局初始化一次（单例）。不传目录则使用 classpath 默认路径
AutoMapper autoMapper = AutoMapper.newInstance();

// 可选：从 JSON 样例生成 schema 文件
SchemaUtil.createSchemaFile("testJson", "for test", jsonData, schemaDir);

// 源对象 → 目标 schema + 规则
JSONObject rs = AutoMapper.getInstance().map(srcObj, "testJson", "testRule");

// 也可用 Class 作为目标模板（不读 schema 文件）
JSONObject rs2 = AutoMapper.getInstance().map(srcObj, TargetDto.class, "testRule");

autoMapper.destroy(); // 停止目录监听
```

执行过程：

1. 按 schema id（或 Class）得到目标 JSON **模板**并浅拷贝；
2. 按 rule id 取出规则列表；
3. 逐条：有 `func` 则走函数管理器，否则 JSONPath 赋值；
4. 按 `serializerFeatures` 再序列化/反序列化一遍，应用 Fastjson 序列化特性。

`map` 内部吞掉异常并打日志，失败时可能返回 `null` 或不完整对象，调用方需自行校验。

## 内置函数

包扫描注册 `com.wjy.automapper.mapper.func.impl` 下实现了 `AutoMapperFunc` 的类。在 rule 的 `func` 中写 **函数名**。

| 函数名 | 作用 | `val` |
|--------|------|--------|
| `newList` | 按源路径长度，用 schema 模板元素初始化目标列表 | 不用 |
| `newObject` | 按模板初始化目标对象；路径含 `[0:]` 时一对多写入 | 不用 |
| `setVal` | 对源或目标路径设固定值（双向：哪个路径非空写哪个） | 要写入的值 |
| `setValIfNone` | 同上，仅当当前值为空时写入 | 要写入的值 |
| `append` | 追加到目标：目标是列表则 `add`；否则字符串拼接 | 拼接符，默认 `,` |
| `dateFormat` | 日期格式转换，支持列表逐项 | `入格式,出格式`；某一侧空串视为 long 时间戳。例：`yyyy-MM-dd'T'HH:mm:ss,yyyy-MM-dd'T'HH:mm:ss.SSS` |
| `setDict` | 字典替换 | `原值-新值` 多组逗号分隔，例：`0-1,1-2` |
| `copyExclude` | 按 key 从源对象拷到目标，跳过指定 key | 逗号分隔的 key 列表 |
| `copyInclude` | 按 key 拷贝，只包含指定 key | 逗号分隔的 key 列表 |

列表字段一般要先 `newList`，嵌套对象再 `newObject`，然后再做字段级 `from`/`to`。

`val` 若为 `${...}` 形式，会先以该 **完整字符串** 为 key 调用 `System.getProperty`；取到则替换。接入 Spring 时，`@Value` 注入后需自行 `System.setProperty(...)`。

## 自定义函数（JDK SPI）

当 JSONPath 与内置函数不够时，实现 `AutoMapperFunc` 并用 SPI 注册。

```java
package com.example.mapper;

import com.alibaba.fastjson.JSONObject;
import com.wjy.automapper.mapper.func.AutoMapperFunc;

public class TrimFunc implements AutoMapperFunc {

    @Override
    public String getFuncName() {
        return "trim";
    }

    @Override
    public JSONObject execute(JSONObject srcObject, String srcPath, JSONObject targetObject,
            String targetPath, JSONObject targetTplObj, String val) {
        // 读写 JSONPath，返回更新后的 targetObject
        return targetObject;
    }
}
```

在业务工程（或本模块）的 `META-INF/services/com.wjy.automapper.mapper.func.AutoMapperFunc` 中写入实现类全名，一行一个：

```
com.example.mapper.TrimFunc
```

规则里：

```xml
<r from="$.name" to="$.name" func="trim"/>
```

要点：

- 函数名与内置相同时，**SPI 实现覆盖内置**；
- SPI 在 `AutoMapper` 初始化时加载，**不随 XML 热更新**；改函数代码需重新发布/重启；
- `execute` 应尽量吞掉自身异常并返回 `targetObject`，与内置实现一致，避免中断后续规则。

## 部署、使用与维护

面向把本模块接到 **业务工程 / API 网关** 之后：怎么放依赖、配置放哪、日常怎么调、改 XML 或升级 jar 时要注意什么。坐标与 `map` 签名见上文「坐标与构建」「映射 API」；监听细节见下文「热更新」。

### 部署

1. **引入依赖。** 业务工程用 Maven 依赖 `com.wjy:automapper-core`（当前 `1.0.0`）。若从 GitHub Packages 拉取，在 `settings.xml` 配置对应仓库与有 `read:packages` 权限的 token，再 `mvn deploy` / 安装到私服均可。
2. **JDK 与依赖。** 编译目标为 **JDK 8**。运行时会带上 Fastjson、JAXB、commons-io 等；若工程里已有 Fastjson，注意版本对齐，避免 JSONPath 行为不一致。
3. **配置目录不要打进只读 jar。** 默认从 classpath 的 `automapper/schema/`、`automapper/rule/` 读文件，适合本地试跑。生产环境 jar 内路径通常 **无法热更新**，应把 XML 放到可写目录（如 `/opt/conf/automapper/`），启动时指定：

   ```java
   AutoMapper.newInstance("/opt/conf/automapper/schema/", "/opt/conf/automapper/rule/");
   ```

   目录需事先存在，文件名仍为 `{id}-schema.xml` / `{id}-rule.xml`。
4. **进程内只初始化一次。** `newInstance` / `getInstance` 是单例；再调 `newInstance` 会 `destroy` 旧实例再重建。建议在应用启动钩子里初始化（Spring 可用 `@PostConstruct` 或 `ApplicationRunner`），在关闭钩子里调用 `destroy()` 停掉目录监听，避免线程泄漏。
5. **自定义函数随业务 jar 走。** SPI 实现类和 `META-INF/services/...AutoMapperFunc` 打在业务工程（或扩展包）里，随应用一起发布；**不能**只改 XML 就换一套函数实现。

设计器导出的 XML 拷到上述目录即可，不必在服务器上装 Node 或再跑 `automapper-design-web`。

### 使用

典型路径：网关或对接层拿到源报文（`JSONObject` / 先 `JSON.parseObject`）→ 按约定选用 **schema id**、**rule id** → `AutoMapper.getInstance().map(src, schemaId, ruleId)` → 把返回的目标 JSON 交给下游。

- id 必须和文件名去掉后缀后一致，且全局唯一。
- `map` **内部吞异常只打日志**，失败可能得到 `null` 或不完整对象，调用方要判空、必要时打业务日志或走降级。
- 上线前用真实样例在本模块跑一遍；设计器里的「映射预览」不能代替这次校验。
- 多套内外接口就准备多对 schema/rule，用 id 区分，不要把无关映射写进同一份 rule。

### 维护与更新

| 要改的东西 | 怎么做 | 要不要发版 / 重启 |
|------------|--------|-------------------|
| 已在用的 schema / rule 字段或路径 | 设计器改完导出，**覆盖**服务器对应 XML | 一般不用。已缓存文件约 **60 秒**内热加载（见「热更新」） |
| **新增**一套 schema 或 rule | 按命名约定放入目录，业务改用新的 id 去 `map` | 业务若写死了 id，改调用处需要发版；仅加文件时，第一次 `map` 会延迟加载 |
| 删除某套配置 | 删文件；缓存会去掉该 id | 仍去 `map` 会加载失败，需同步改调用或准备替代规则 |
| 回滚映射 | 用 Git 里上一版 XML 覆盖同一路径 | 同「修改已有文件」，等监听刷新 |
| 内置函数不够、改 SPI 实现 | 改 Java + SPI 注册，重新构建业务包 | **必须发版并重启**，函数表只在初始化时加载 |
| 升级 `automapper-core` 版本 | 改 Maven 版本，回归一遍主要 schema/rule | **必须发版并重启** |

建议把 XML 纳入 Git（或配置中心下发到机器目录），按环境分目录或分文件，避免人手改生产机却对不上仓库。改完可看应用日志里的解析/映射异常；热更新未生效时先确认：路径是否可写、文件名与 id 是否一致、该 id 是否已经被加载过、是否还在 60 秒间隔内。

## 热更新

`SchemaManager` / `RuleManager` 基于 Apache Commons IO `FileAlterationMonitor` 监听对应目录，默认间隔 **60 秒**（`XmlConstant.FILE_MONITOR_INTERVAL_DEFAULT_SEC`）。

| 事件 | 行为 |
|------|------|
| 首次 `map` / `getRule` / `getTplObjWithXml` | **延迟加载** 文件并放入内存缓存 |
| 文件修改 | 仅当该 id **已在缓存中** 时重新解析覆盖 |
| 文件删除 | 从缓存移除；下次访问再按文件是否存在决定 |
| 新建文件 | 监听器不主动加载；第一次按 id 访问时再读盘 |

因此：改一份 **已经用过** 的 XML，最多约一分钟内生效；全新 id 的文件只要命名正确，第一次 `map` 就会加载，不必等监听器。`AutoMapper.destroy()` 会停止监听。

配置目录若在只读 jar 内，文件监听通常无法感知运行时修改；生产环境建议把 schema / rule 放到可写外部目录，并用 `newInstance(schemaDir, ruleDir)` 指向该路径。

## 包结构（便于对照源码）

```
com.wjy.automapper
├── AutoMapper              # 映射入口
├── AbsFileMonitor          # 目录监听基类
├── schema/                 # Schema 解析、模板、热更新
├── rule/                   # Rule 解析、热更新
├── mapper.func             # AutoMapperFunc、FuncManager、内置枚举
│   └── impl/               # 内置函数实现
└── util/                   # JSONPath、XML、SPI 扫描等
```

## 与设计器的关系

[automapper-design-web](../automapper-design-web) 导出的 schema / rule 文本应遵循上文文件名与 XML 结构。浏览器内「映射预览」是前端演算，**可能与本模块细节不完全一致**；上线前请用本模块 `map` 做一次对照。
