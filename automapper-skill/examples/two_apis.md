# 查询订单 queryOrder

## 请求报文

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| userId | string | 用户 id | u-1 |
| page | int | 页码 | 1 |

## 响应报文

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| code | int | 状态码 | 0 |
| data[].userId | string | 用户 id | u-1 |

# 创建订单 createOrder

网关入参对齐内部创建接口。

## 源：内部模型

| name | type | desc | eg |
|------|------|------|-----|
| sku | string | 货品编码 | SKU-9 |
| qty | int | 数量 | 2 |

## 目标：第三方网关

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| goodsId | string | 商品 id | SKU-9 |
| count | int | 购买数量 | 2 |
