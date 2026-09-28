"""Parse mixed markdown / text with tables into InterfaceSpec list."""

from __future__ import annotations

import re
from typing import Dict, List, Optional, Tuple

from .infer import infer_interface_id, infer_role, infer_roles_for_two
from .model import FieldNode, InterfaceSpec, SideSpec
from .tree import rows_to_tree

_TABLE_SEP = re.compile(r"^\s*\|?\s*:?-{3,}")
_H1 = re.compile(r"^#\s+(.+)$")
_H2 = re.compile(r"^##\s+(.+)$")


def _split_row(line: str) -> List[str]:
    s = line.strip()
    if s.startswith("|"):
        s = s[1:]
    if s.endswith("|"):
        s = s[:-1]
    return [c.strip() for c in s.split("|")]


def _is_table_header(cells: List[str]) -> bool:
    joined = "".join(cells)
    keys = ("字段", "名称", "name", "field", "path", "属性")
    return any(k.lower() in joined.lower() or k in joined for k in keys)


def _colmap(header: List[str]) -> Dict[str, int]:
    m = {}
    for i, h in enumerate(header):
        hl = h.lower()
        if any(x in h or x in hl for x in ("字段", "名称", "name", "field", "path", "属性")):
            m["name"] = i
        elif any(x in h or x in hl for x in ("类型", "type")):
            m["type"] = i
        elif any(x in h or x in hl for x in ("说明", "描述", "desc", "comment", "含义")):
            m["desc"] = i
        elif any(x in h or x in hl for x in ("示例", "样例", "eg", "example", "举例")):
            m["eg"] = i
    if "name" not in m and header:
        m["name"] = 0
    if "type" not in m and len(header) > 1:
        m["type"] = 1
    return m


def _parse_tables(lines: List[str]) -> List[Tuple[int, List[dict]]]:
    """Return (start_line_index, rows)."""
    found = []
    i = 0
    n = len(lines)
    while i < n:
        if "|" not in lines[i]:
            i += 1
            continue
        cells = _split_row(lines[i])
        if i + 1 < n and (_TABLE_SEP.match(lines[i + 1]) or _TABLE_SEP.match(lines[i + 1].replace(" ", ""))):
            header = cells
            cmap = _colmap(header)
            i += 2
            rows = []
            while i < n and "|" in lines[i] and not _TABLE_SEP.match(lines[i]):
                vals = _split_row(lines[i])
                def cell(key: str) -> str:
                    idx = cmap.get(key)
                    if idx is None or idx >= len(vals):
                        return ""
                    return vals[idx]
                name = cell("name")
                if name and name not in ("字段", "name"):
                    rows.append(
                        {
                            "name": name,
                            "type": cell("type"),
                            "desc": cell("desc"),
                            "eg": cell("eg"),
                        }
                    )
                i += 1
            if rows:
                found.append((i, rows))
            continue
        i += 1
    return found


def _heading_before(lines: List[str], end_idx: int) -> str:
    for j in range(min(end_idx, len(lines)) - 1, -1, -1):
        m2 = _H2.match(lines[j])
        if m2:
            return m2.group(1).strip()
        m1 = _H1.match(lines[j])
        if m1:
            return m1.group(1).strip()
    return ""


def _split_interface_blocks(text: str) -> List[Tuple[str, str]]:
    lines = text.splitlines()
    starts = [(i, _H1.match(ln).group(1).strip()) for i, ln in enumerate(lines) if _H1.match(ln)]
    if not starts:
        return [("", text)]
    blocks = []
    for k, (idx, title) in enumerate(starts):
        end = starts[k + 1][0] if k + 1 < len(starts) else len(lines)
        body = "\n".join(lines[idx:end])
        blocks.append((title, body))
    return blocks


def parse_markdown(text: str) -> List[InterfaceSpec]:
    specs: List[InterfaceSpec] = []
    blocks = _split_interface_blocks(text)
    fallback_i = 1
    for title, body in blocks:
        lines = body.splitlines()
        tables = []
        # find tables with heading context
        i = 0
        n = len(lines)
        collected: List[Tuple[str, List[dict]]] = []
        while i < n:
            if "|" in lines[i]:
                cells = _split_row(lines[i])
                if i + 1 < n and re.search(r"-{3,}", lines[i + 1]):
                    heading = _heading_before(lines, i)
                    cmap = _colmap(cells)
                    i += 2
                    rows = []
                    while i < n and "|" in lines[i] and not re.search(r"^\s*\|?\s*:?-{3,}", lines[i]):
                        vals = _split_row(lines[i])
                        def cell(key: str) -> str:
                            idx = cmap.get(key)
                            if idx is None or idx >= len(vals):
                                return ""
                            return vals[idx]
                        name = cell("name")
                        if name:
                            rows.append(
                                {
                                    "name": name,
                                    "type": cell("type"),
                                    "desc": cell("desc"),
                                    "eg": cell("eg"),
                                }
                            )
                        i += 1
                    if rows:
                        collected.append((heading, rows))
                    continue
            i += 1

        api_id = infer_interface_id(title, fallback=f"api{fallback_i}")
        fallback_i += 1
        spec = InterfaceSpec(id=api_id, desc=title.strip() or api_id)

        if not collected:
            specs.append(spec)
            continue

        if len(collected) == 1:
            heading, rows = collected[0]
            role = infer_role(heading) or infer_role(title) or "target"
            side = SideSpec(role=role, desc=heading, fields=rows_to_tree(rows))
            if role == "source":
                spec.source = side
            else:
                spec.target = side
        else:
            # pair first two tables as source/target (more tables: first src, rest ignored except 2nd tgt)
            h0, r0 = collected[0]
            h1, r1 = collected[1]
            role0, role1 = infer_roles_for_two(h0 or title, h1)
            s0 = SideSpec(role=role0, desc=h0, fields=rows_to_tree(r0))
            s1 = SideSpec(role=role1, desc=h1, fields=rows_to_tree(r1))
            for s in (s0, s1):
                if s.role == "source":
                    spec.source = s
                else:
                    spec.target = s
        specs.append(spec)
    return specs
