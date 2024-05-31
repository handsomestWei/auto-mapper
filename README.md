# auto-mapper
java json对象属性值自动映射转换

## 简介
只需提供json示例报文，自动生成对象schema；声明映射规则；使用schema id和规则id即可实现转换。

## 设计思路
### 解耦
+ 传统的工具，映射转换关系需要硬编码，需要生成pojo java文件；传统的json schema标准格式文件可读性较差
+ 本工具使用xml描述映射规则，使用xml保存json schema提高可读和可维护性
+ 基于文件，实现解耦，减少胶水代码。且利用文件热更新能力，实现属性和规则的动态调整
+ 可用于上层低代码平台的接口生成、api网关的接口入出参管理、第三方接口对接开发等

### 属性映射
使用ali fastjson的JSONPath做属性路径访问。[path语法参考](https://github.com/alibaba/fastjson/wiki/JSONPath)

### 可扩展
支持编写自定义函数处理，在内部以反射的方式实现调用

## 使用说明
```
// step1 初始化。单例模式，全局做一次即可
AutoMapper autoMapper = AutoMapper.newInstance();

// step2 使用json示例自动生成json schema xml
String schemaId = "testJson";
SchemaUtil.createSchemaFile(schemaId, "for test", jsonData, schemaDir);

// step3 定义转换规则，编写rule.xml
String ruleId = "testRule";

// step4 对源pojo，依据规则，转换成目标testJson
JSONObject rs = AutoMapper.getInstance().map(srcObj, "testJson", "testRule");
```