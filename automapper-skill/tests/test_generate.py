from pathlib import Path
from xml.etree import ElementTree as ET

from tools.generate import generate_from_markdown
from tools.parse_markdown import parse_markdown


def test_query_order_both_sides_and_rule(tmp_path):
    md = Path(__file__).resolve().parents[1] / "examples" / "query_order.md"
    gens = generate_from_markdown(md.read_text(encoding="utf-8"), str(tmp_path))
    assert len(gens) == 1
    g = gens[0]
    assert g.interface_id == "queryOrder"
    assert g.schema_xml and g.sample_json and g.src_sample_json and g.rule_xml
    folder = tmp_path / "queryOrder"
    assert (folder / "queryOrder-schema.xml").is_file()
    assert (folder / "queryOrder-rule.xml").is_file()

    schema = ET.parse(folder / "queryOrder-schema.xml").getroot()
    assert schema.get("id") == "queryOrder"
    names = [p.get("name") for p in schema.findall("p")]
    assert names == ["code", "msg", "data"]

    rule = ET.parse(folder / "queryOrder-rule.xml").getroot()
    assert rule.get("id") == "queryOrder"
    funcs = [(r.get("to"), r.get("func"), r.get("from")) for r in rule.findall("r")]
    assert ("$.data", "newList", "$.data") in funcs
    assert ("$.data[0:].innerObj", "newObject", "") in funcs
    # alias idd -> id
    assert any(r.get("from") == "$.data[0:].idd" and r.get("to") == "$.data[0:].id" for r in rule.findall("r"))


def test_parse_two_apis():
    md = Path(__file__).resolve().parents[1] / "examples" / "two_apis.md"
    specs = parse_markdown(md.read_text(encoding="utf-8"))
    assert [s.id for s in specs] == ["queryOrder", "createOrder"]
    assert specs[0].source and specs[0].target
    assert specs[1].source and specs[1].target


def test_create_order_alias_rule(tmp_path):
    md = Path(__file__).resolve().parents[1] / "examples" / "two_apis.md"
    gens = generate_from_markdown(md.read_text(encoding="utf-8"), str(tmp_path))
    create = next(g for g in gens if g.interface_id == "createOrder")
    assert create.rule_xml
    rule = ET.parse(tmp_path / "createOrder" / "createOrder-rule.xml").getroot()
    pairs = {(r.get("from"), r.get("to")) for r in rule.findall("r")}
    assert ("$.sku", "$.goodsId") in pairs
    assert ("$.qty", "$.count") in pairs


def test_target_only_no_rule(tmp_path):
    md = Path(__file__).resolve().parents[1] / "examples" / "target_only.md"
    gens = generate_from_markdown(md.read_text(encoding="utf-8"), str(tmp_path))
    assert len(gens) == 1
    g = gens[0]
    assert g.schema_xml and g.sample_json
    assert g.rule_xml is None
    assert not (tmp_path / "payNotify" / "payNotify-rule.xml").exists()
