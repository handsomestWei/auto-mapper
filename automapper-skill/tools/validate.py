"""Validate generated schema / rule XML against automapper-core conventions."""

from __future__ import annotations

import os
import re
from typing import List
from xml.etree import ElementTree as ET

from .model import JAVA_TYPES, TYPE_LIST, TYPE_OBJECT

FILE_SCHEMA = re.compile(r"^(.+)-schema\.xml$")
FILE_RULE = re.compile(r"^(.+)-rule\.xml$")


class ValidationError(Exception):
    pass


def _check_types(el: ET.Element, path: str, errors: List[str]) -> None:
    for p in el.findall("p"):
        name = p.get("name") or ""
        typ = p.get("type") or ""
        here = f"{path}/{name}"
        if not name:
            errors.append(f"{here}: missing name")
        if typ not in JAVA_TYPES:
            errors.append(f"{here}: invalid type {typ!r}")
        kids = p.findall("p")
        if typ in (TYPE_LIST, TYPE_OBJECT) and not kids:
            errors.append(f"{here}: container {typ} has no children")
        _check_types(p, here, errors)


def validate_schema_xml(xml_text: str, filename: str = "") -> List[str]:
    errors: List[str] = []
    root = ET.fromstring(xml_text)
    if root.tag != "schema":
        return [f"{filename}: root must be <schema>"]
    sid = root.get("id") or ""
    if not sid:
        errors.append(f"{filename}: schema id required")
    m = FILE_SCHEMA.match(os.path.basename(filename)) if filename else None
    if m and m.group(1) != sid:
        errors.append(f"{filename}: id {sid!r} != filename stem {m.group(1)!r}")
    _check_types(root, sid or "schema", errors)
    return errors


def validate_rule_xml(xml_text: str, filename: str = "") -> List[str]:
    errors: List[str] = []
    root = ET.fromstring(xml_text)
    if root.tag != "rule":
        return [f"{filename}: root must be <rule>"]
    rid = root.get("id") or ""
    if not rid:
        errors.append(f"{filename}: rule id required")
    m = FILE_RULE.match(os.path.basename(filename)) if filename else None
    if m and m.group(1) != rid:
        errors.append(f"{filename}: id {rid!r} != filename stem {m.group(1)!r}")
    seen_init = set()
    for i, r in enumerate(root.findall("r"), 1):
        frm = r.get("from")
        to = r.get("to")
        func = r.get("func") or ""
        if to is None:
            errors.append(f"{filename} r[{i}]: missing to")
            continue
        if func in ("newList", "newObject"):
            seen_init.add(to)
        elif to and "[0:]" in to:
            # parent list should have been newList'd
            parent = to.split("[0:]")[0]
            if parent and parent not in seen_init:
                errors.append(f"{filename} r[{i}]: {to} used before newList on {parent}")
    return errors


def validate_dir(out_dir: str) -> List[str]:
    errors: List[str] = []
    for root, _dirs, files in os.walk(out_dir):
        for fn in files:
            path = os.path.join(root, fn)
            if fn.endswith("-schema.xml"):
                with open(path, encoding="utf-8") as f:
                    errors.extend(validate_schema_xml(f.read(), path))
            elif fn.endswith("-rule.xml"):
                with open(path, encoding="utf-8") as f:
                    errors.extend(validate_rule_xml(f.read(), path))
    return errors
