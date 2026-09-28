# 查询订单 queryOrder

内部服务查询订单，对接 API 网关出参。

## 内部请求（源）

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| code | int | 状态码 | 0 |
| msg | string | 说明 | success |
| data[].idd | string | 行业务 id | row-001 |
| data[].objId | string | 外部对象 id | ext-abc-99 |

## 网关响应（目标）

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| code | int | 状态码 | 0 |
| msg | string | 说明 | success |
| data[].id | string | 行记录 id | row-001 |
| data[].innerObj.objId | string | 与第三方一致的对象 id | ext-abc-99 |
