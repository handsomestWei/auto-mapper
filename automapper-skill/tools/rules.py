"""Match target fields to source JSONPaths for rule rows."""

from __future__ import annotations

from typing import Dict, List, Optional, Tuple

from .jsonpath import LIST_TOKEN, child_walk_path, flatten_leaves, join_path
from .model import TYPE_LIST, TYPE_OBJECT, FieldNode, RuleRow

ALIASES = {
    "idd": "id",
    "orderid": "orderId",
    "userid": "userId",
    "goodsid": "goodsId",
    "sku": "goodsId",
    "qty": "count",
    "quantity": "count",
    "amt": "amount",
}


def _canon(name: str) -> str:
    s = (name or "").strip()
    key = s.replace("_", "").lower()
    if key in ALIASES:
        return ALIASES[key].lower()
    return key


def _index_source(nodes: List[FieldNode], parent: str = "$") -> Dict[str, List[str]]:
    """canonical name -> json paths (leaves and list nodes)."""
    idx: Dict[str, List[str]] = {}

    def add(name: str, path: str) -> None:
        idx.setdefault(_canon(name), []).append(path)

    def walk(items: List[FieldNode], p: str) -> None:
        for n in items:
            if n.type == TYPE_LIST:
                list_path = join_path(p, n.name)
                add(n.name, list_path)
                walk(n.children, f"{list_path}{LIST_TOKEN}")
            elif n.type == TYPE_OBJECT:
                walk(n.children, join_path(p, n.name))
            else:
                add(n.name, join_path(p, n.name))

    walk(nodes, parent)
    return idx


def _pick_path(cands: List[str], preferred_prefix: str) -> Optional[str]:
    if not cands:
        return None
    pref = preferred_prefix.rstrip(".")
    same = [c for c in cands if c.startswith(pref) or pref.startswith(c)]
    if len(same) == 1:
        return same[0]
    if same:
        return same[0]
    return cands[0]


def build_rules(source_fields: List[FieldNode], target_fields: List[FieldNode]) -> List[RuleRow]:
    src_idx = _index_source(source_fields)
    rows: List[RuleRow] = []

    def match_name(name: str, src_prefix: str) -> Optional[str]:
        return _pick_path(src_idx.get(_canon(name), []), src_prefix)

    def walk(items: List[FieldNode], tgt_parent: str, src_prefix: str) -> None:
        for n in items:
            if n.type == TYPE_LIST:
                to_list = join_path(tgt_parent, n.name)
                frm = match_name(n.name, src_prefix) or to_list
                rows.append(RuleRow(frm=frm, to=to_list, func="newList"))
                child_src = f"{frm}{LIST_TOKEN}" if LIST_TOKEN not in frm else frm
                walk(n.children, f"{to_list}{LIST_TOKEN}", child_src)
            elif n.type == TYPE_OBJECT:
                to_obj = join_path(tgt_parent, n.name)
                rows.append(RuleRow(frm="", to=to_obj, func="newObject"))
                walk(n.children, to_obj, src_prefix)
            else:
                to_p = join_path(tgt_parent, n.name)
                frm = match_name(n.name, src_prefix)
                if frm:
                    rows.append(RuleRow(frm=frm, to=to_p))

    walk(target_fields, "$", "$")
    return rows
