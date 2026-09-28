"""Emit schema XML / rule XML / JSON samples aligned with automapper-core."""

from __future__ import annotations

import json
from typing import List
from xml.etree.ElementTree import Element, tostring

from .model import TYPE_BOOLEAN, TYPE_DOUBLE, TYPE_INTEGER, TYPE_LIST, TYPE_LONG, TYPE_OBJECT, FieldNode, RuleRow


def _xml_header() -> str:
    return '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'


def _indent(elem: Element, level: int = 0) -> None:
    pad = "\n" + "    " * level
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = pad + "    "
        for i, child in enumerate(elem):
            _indent(child, level + 1)
            if not child.tail or not child.tail.strip():
                child.tail = pad + ("    " if i < len(elem) - 1 else "")
        if not elem[-1].tail or not elem[-1].tail.strip():
            elem[-1].tail = pad
    if level and (not elem.tail or not elem.tail.strip()):
        elem.tail = "\n" + "    " * (level - 1)


def _p_element(node: FieldNode) -> Element:
    el = Element("p")
    el.set("name", node.name)
    el.set("type", node.type)
    if node.desc:
        el.set("desc", node.desc)
    if node.eg != "" and node.type not in (TYPE_LIST, TYPE_OBJECT):
        el.set("eg", node.eg)
    for c in node.children:
        el.append(_p_element(c))
    return el


def emit_schema_xml(schema_id: str, desc: str, fields: List[FieldNode]) -> str:
    root = Element("schema")
    root.set("id", schema_id)
    if desc:
        root.set("desc", desc)
    for n in fields:
        root.append(_p_element(n))
    _indent(root)
    body = tostring(root, encoding="unicode")
    return _xml_header() + body + "\n"


def emit_rule_xml(rule_id: str, desc: str, rows: List[RuleRow], serializer_features: str = "6,7") -> str:
    root = Element("rule")
    root.set("id", rule_id)
    if desc:
        root.set("desc", desc)
    if serializer_features:
        root.set("serializerFeatures", serializer_features)
    for r in rows:
        el = Element("r")
        el.set("from", r.frm)
        el.set("to", r.to)
        if r.func:
            el.set("func", r.func)
        if r.val:
            el.set("val", r.val)
        root.append(el)
    _indent(root)
    body = tostring(root, encoding="unicode")
    return _xml_header() + body + "\n"


def _scalar_example(node: FieldNode):
    raw = node.eg
    t = node.type
    if t == TYPE_BOOLEAN:
        if raw == "":
            return False
        return str(raw).lower() in ("1", "true", "yes", "y")
    if t in (TYPE_INTEGER, TYPE_LONG):
        if raw == "":
            return 0
        try:
            return int(raw)
        except ValueError:
            return 0
    if t == TYPE_DOUBLE:
        if raw == "":
            return 0.0
        try:
            return float(raw)
        except ValueError:
            return 0.0
    return raw


def node_to_json_value(node: FieldNode):
    if node.type == TYPE_LIST:
        item = {}
        for c in node.children:
            item[c.name] = node_to_json_value(c)
        return [item] if item else []
    if node.type == TYPE_OBJECT:
        obj = {}
        for c in node.children:
            obj[c.name] = node_to_json_value(c)
        return obj
    return _scalar_example(node)


def emit_sample_json(fields: List[FieldNode]) -> str:
    obj = {n.name: node_to_json_value(n) for n in fields}
    return json.dumps(obj, ensure_ascii=False, indent=2) + "\n"
