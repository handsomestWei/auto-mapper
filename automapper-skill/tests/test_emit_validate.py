from tools.emit import emit_sample_json, emit_schema_xml
from tools.model import FieldNode, TYPE_INTEGER, TYPE_LIST, TYPE_STRING
from tools.validate import validate_rule_xml, validate_schema_xml


def test_emit_and_validate_schema():
    fields = [
        FieldNode(name="code", type=TYPE_INTEGER, eg="0", desc="状态码"),
        FieldNode(
            name="data",
            type=TYPE_LIST,
            children=[FieldNode(name="id", type=TYPE_STRING, eg="a")],
        ),
    ]
    xml = emit_schema_xml("demoJson", "demo", fields)
    assert 'id="demoJson"' in xml
    assert not validate_schema_xml(xml, "demoJson-schema.xml")
    js = emit_sample_json(fields)
    assert '"code": 0' in js
    assert '"id": "a"' in js


def test_validate_rule_newlist_order():
    bad = """<?xml version="1.0" encoding="UTF-8"?>
<rule id="x">
  <r from="$.data[0:].id" to="$.data[0:].id"/>
</rule>
"""
    errs = validate_rule_xml(bad, "x-rule.xml")
    assert any("newList" in e for e in errs)

    good = """<?xml version="1.0" encoding="UTF-8"?>
<rule id="x">
  <r from="$.data" to="$.data" func="newList"/>
  <r from="$.data[0:].id" to="$.data[0:].id"/>
</rule>
"""
    assert not validate_rule_xml(good, "x-rule.xml")
