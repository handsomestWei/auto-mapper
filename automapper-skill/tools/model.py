"""Shared field-tree model for automapper schema / JSON / rule generation."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import List, Optional

JAVA_TYPES = (
    "java.lang.String",
    "java.lang.Integer",
    "java.lang.Long",
    "java.lang.Double",
    "java.lang.Boolean",
    "java.lang.Object",
    "java.util.List",
)

TYPE_STRING = "java.lang.String"
TYPE_INTEGER = "java.lang.Integer"
TYPE_LONG = "java.lang.Long"
TYPE_DOUBLE = "java.lang.Double"
TYPE_BOOLEAN = "java.lang.Boolean"
TYPE_OBJECT = "java.lang.Object"
TYPE_LIST = "java.util.List"

TYPE_ALIASES = {
    "string": TYPE_STRING,
    "str": TYPE_STRING,
    "varchar": TYPE_STRING,
    "text": TYPE_STRING,
    "字符串": TYPE_STRING,
    "文本": TYPE_STRING,
    "int": TYPE_INTEGER,
    "integer": TYPE_INTEGER,
    "int32": TYPE_INTEGER,
    "整数": TYPE_INTEGER,
    "整型": TYPE_INTEGER,
    "long": TYPE_LONG,
    "int64": TYPE_LONG,
    "bigint": TYPE_LONG,
    "长整": TYPE_LONG,
    "long型": TYPE_LONG,
    "double": TYPE_DOUBLE,
    "float": TYPE_DOUBLE,
    "number": TYPE_DOUBLE,
    "decimal": TYPE_DOUBLE,
    "numeric": TYPE_DOUBLE,
    "浮点": TYPE_DOUBLE,
    "小数": TYPE_DOUBLE,
    "bool": TYPE_BOOLEAN,
    "boolean": TYPE_BOOLEAN,
    "布尔": TYPE_BOOLEAN,
    "object": TYPE_OBJECT,
    "obj": TYPE_OBJECT,
    "json": TYPE_OBJECT,
    "map": TYPE_OBJECT,
    "对象": TYPE_OBJECT,
    "array": TYPE_LIST,
    "list": TYPE_LIST,
    "[]": TYPE_LIST,
    "数组": TYPE_LIST,
    "列表": TYPE_LIST,
}


def normalize_java_type(raw: str) -> str:
    if not raw:
        return TYPE_STRING
    t = raw.strip()
    if t in JAVA_TYPES:
        return t
    key = t.lower().replace("java.lang.", "").replace("java.util.", "")
    return TYPE_ALIASES.get(key, TYPE_ALIASES.get(t, TYPE_STRING))


def is_container(java_type: str) -> bool:
    return java_type in (TYPE_LIST, TYPE_OBJECT)


@dataclass
class FieldNode:
    name: str
    type: str = TYPE_STRING
    desc: str = ""
    eg: str = ""
    children: List["FieldNode"] = field(default_factory=list)

    def child(self, name: str) -> Optional["FieldNode"]:
        for c in self.children:
            if c.name == name:
                return c
        return None


@dataclass
class SideSpec:
    role: str  # source | target
    desc: str = ""
    fields: List[FieldNode] = field(default_factory=list)


@dataclass
class InterfaceSpec:
    id: str
    desc: str = ""
    source: Optional[SideSpec] = None
    target: Optional[SideSpec] = None


@dataclass
class RuleRow:
    frm: str
    to: str
    func: str = ""
    val: str = ""
