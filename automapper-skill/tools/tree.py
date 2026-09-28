"""Build nested FieldNode trees from dotted / array field names."""

from __future__ import annotations

import re
from typing import Iterable, List, Sequence, Tuple

from .model import TYPE_LIST, TYPE_OBJECT, FieldNode, normalize_java_type

# data[].id  |  data[0].id  |  data.innerObj.objId
_TOKEN = re.compile(
    r"(?P<name>[A-Za-z_][\w]*)"
    r"(?P<arr>\[(?:\d+)?\]|\.?\[\])?"
)


def split_field_path(raw: str) -> List[Tuple[str, bool]]:
    """Return [(name, is_list), ...] from a table field cell."""
    text = (raw or "").strip()
    if not text:
        return []
    text = text.replace("[].", "[].").replace("[0].", "[].")
    parts: List[Tuple[str, bool]] = []
    rest = text
    while rest:
        if rest.startswith("."):
            rest = rest[1:]
            continue
        m = re.match(r"([A-Za-z_][\w]*)(\[(?:\d+)?\])?", rest)
        if not m:
            # fallback: take remaining as one name
            parts.append((rest, False))
            break
        name = m.group(1)
        is_list = bool(m.group(2))
        rest = rest[m.end() :]
        if rest.startswith("[]"):
            is_list = True
            rest = rest[2:]
        parts.append((name, is_list))
    return parts


def insert_path(
    roots: List[FieldNode],
    path: Sequence[Tuple[str, bool]],
    *,
    leaf_type: str,
    desc: str = "",
    eg: str = "",
) -> None:
    if not path:
        return
    nodes = roots
    for i, (name, is_list) in enumerate(path):
        last = i == len(path) - 1
        found = next((n for n in nodes if n.name == name), None)
        if found is None:
            java_type = leaf_type if last and not is_list else (TYPE_LIST if is_list else TYPE_OBJECT)
            found = FieldNode(name=name, type=java_type)
            nodes.append(found)
        else:
            if is_list:
                found.type = TYPE_LIST
            elif not last and found.type not in (TYPE_LIST, TYPE_OBJECT):
                found.type = TYPE_OBJECT
        if last:
            if not is_list:
                found.type = leaf_type
            found.desc = desc or found.desc
            found.eg = eg if eg != "" else found.eg
            if is_list and leaf_type not in (TYPE_LIST, TYPE_OBJECT) and not found.children:
                # list of scalars: keep List with no children (rare)
                pass
        nodes = found.children


def rows_to_tree(rows: Iterable[dict]) -> List[FieldNode]:
    """rows: {name, type, desc, eg}"""
    roots: List[FieldNode] = []
    for row in rows:
        path = split_field_path(str(row.get("name") or ""))
        insert_path(
            roots,
            path,
            leaf_type=normalize_java_type(str(row.get("type") or "")),
            desc=str(row.get("desc") or ""),
            eg=str(row.get("eg") or ""),
        )
    return roots
