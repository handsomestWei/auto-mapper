"""JSONPath helpers using Fastjson-style [0:] list wildcard."""

from __future__ import annotations

from typing import List

from .model import TYPE_LIST, FieldNode

LIST_TOKEN = "[0:]"


def join_path(parent: str, name: str, as_list: bool = False) -> str:
    if parent in ("", "$"):
        base = f"$.{name}"
    else:
        base = f"{parent}.{name}"
    return base


def child_walk_path(parent_path: str, node: FieldNode) -> str:
    """Path used when walking into this node's children."""
    p = join_path(parent_path, node.name)
    if node.type == TYPE_LIST:
        return f"{p}{LIST_TOKEN}"
    return p


def flatten_leaves(nodes: List[FieldNode], parent: str = "$") -> List[tuple]:
    """[(json_path, FieldNode), ...] for scalar leaves."""
    out = []
    for n in nodes:
        if n.type == TYPE_LIST:
            walk = f"{join_path(parent, n.name)}{LIST_TOKEN}"
            if not n.children:
                out.append((join_path(parent, n.name), n))
            else:
                out.extend(flatten_leaves(n.children, walk))
        elif n.type == "java.lang.Object":
            walk = join_path(parent, n.name)
            if not n.children:
                out.append((walk, n))
            else:
                out.extend(flatten_leaves(n.children, walk))
        else:
            out.append((join_path(parent, n.name), n))
    return out
