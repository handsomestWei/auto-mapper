"""Tests for dotted path → field tree."""

from tools.model import TYPE_LIST, TYPE_OBJECT, TYPE_STRING
from tools.tree import rows_to_tree, split_field_path


def test_split_array_and_nested():
    assert split_field_path("data[].id") == [("data", True), ("id", False)]
    assert split_field_path("data[0].id") == [("data", True), ("id", False)]
    assert split_field_path("data.innerObj.objId") == [
        ("data", False),
        ("innerObj", False),
        ("objId", False),
    ]


def test_rows_to_tree_list_object():
    roots = rows_to_tree(
        [
            {"name": "code", "type": "int", "desc": "状态码", "eg": "0"},
            {"name": "data[].id", "type": "string", "desc": "id", "eg": "a"},
            {"name": "data[].innerObj.objId", "type": "string", "desc": "oid", "eg": "x"},
        ]
    )
    assert [n.name for n in roots] == ["code", "data"]
    data = roots[1]
    assert data.type == TYPE_LIST
    inner = next(c for c in data.children if c.name == "innerObj")
    assert inner.type == TYPE_OBJECT
    assert inner.child("objId").type == TYPE_STRING
    assert inner.child("objId").eg == "x"
