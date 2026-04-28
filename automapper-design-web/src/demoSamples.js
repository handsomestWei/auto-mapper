/** 与 Schema 设计器默认 JSON 对齐，便于规则页载入同一棵树 */

export const DEFAULT_JSON_SAMPLE = `{
  "version": "1.0",
  "ok": true,
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "row-001",
      "score": 98.5,
      "innerObj": {
        "objId": "ext-abc-99",
        "label": "示例标签"
      }
    }
  ],
  "meta": {
    "traceId": "trace-8f3a2b-0001"
  }
}`

export const DEFAULT_RULE_XML = `<?xml version="1.0" encoding="UTF-8"?>
<rule id="testRule" desc="for test" serializerFeatures="6,7">
    <r from="$.code" to="$.code"/>
    <r from="$.msg" to="$.msg"/>
    <r from="$.data" to="$.data" func="newList"/>
    <r from="" to="$.data[0:].innerObj" func="newObject"/>
    <r from="$.data[0:].idd" to="$.data[0:].id"/>
    <r from="$.data[0:].idd" to="$.data[0:].innerObj.objId"/>
</rule>`
