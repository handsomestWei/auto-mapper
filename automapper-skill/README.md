# automapper-skill

从接口文档生成 automapper 的 schema、JSON 样例；若同时有源和目标，再生成 rule。

Cursor 技能说明见 [SKILL.md](SKILL.md)。XML 约定见 [reference.md](reference.md)。

## 能力

- **输入**：截图 / 文本 / Word 先抽成 Markdown 字段表（见 `examples/`）。
- **粒度**：每个接口一套输出（一级标题拆分）。
- **源 / 目标**：按标题关键词自动识别（请求、入参、内部 ↔ 响应、出参、网关）。
- **Rule**：两侧都识别到才生成。

## 命令

```bash
cd automapper-skill
python -m tools --input examples/query_order.md --out-dir ./out
```

依赖 Python 3.9+ 标准库。测试：

```bash
pip install pytest
python -m pytest tests
```

## 示例

| 文件 | 场景 |
|------|------|
| `examples/query_order.md` | 单接口，内部源 + 网关目标，含 list/对象/别名 |
| `examples/two_apis.md` | 两个接口，各自出一套 |
| `examples/target_only.md` | 仅对端报文，只出 schema + JSON |
