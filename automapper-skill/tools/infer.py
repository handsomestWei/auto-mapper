"""Infer source vs target role from headings and surrounding text."""

from __future__ import annotations

import re
from typing import Optional

SOURCE_HINTS = (
    "源",
    "请求",
    "入参",
    "内部",
    "request",
    "src",
    "source",
    "入站",
    "上游",
)
TARGET_HINTS = (
    "目标",
    "响应",
    "出参",
    "网关",
    "第三方",
    "response",
    "target",
    "dst",
    "出站",
    "下游",
    "返回",
)


def _norm(text: str) -> str:
    return (text or "").strip().lower()


def infer_role(title: str, extra: str = "") -> Optional[str]:
    blob = f"{title} {extra}"
    low = _norm(blob)
    src_hit = any(h.lower() in low or h in blob for h in SOURCE_HINTS)
    tgt_hit = any(h.lower() in low or h in blob for h in TARGET_HINTS)
    if src_hit and not tgt_hit:
        return "source"
    if tgt_hit and not src_hit:
        return "target"
    if src_hit and tgt_hit:
        # 请求响应 in one title: prefer more specific last keyword position
        src_pos = max((low.rfind(h.lower()) for h in SOURCE_HINTS), default=-1)
        tgt_pos = max((low.rfind(h.lower()) for h in TARGET_HINTS), default=-1)
        if tgt_pos > src_pos:
            return "target"
        if src_pos > tgt_pos:
            return "source"
    return None


def infer_roles_for_two(title_a: str, title_b: str) -> tuple:
    ra, rb = infer_role(title_a), infer_role(title_b)
    if ra and rb and ra != rb:
        return ra, rb
    if ra == "source" and not rb:
        return "source", "target"
    if ra == "target" and not rb:
        return "target", "source"
    if rb == "source" and not ra:
        return "target", "source"
    if rb == "target" and not ra:
        return "source", "target"
    # default: first source, second target (请求在前、响应在后)
    return "source", "target"


_ID_TOKEN = re.compile(r"\b([A-Za-z][A-Za-z0-9_]{1,64})\b")


def infer_interface_id(title: str, fallback: str = "api") -> str:
    text = (title or "").strip()
    # explicit trailing identifier: 查询订单 queryOrder
    tokens = _ID_TOKEN.findall(text)
    camel = [t for t in tokens if any(c.isupper() for c in t[1:]) or t[0].islower()]
    if tokens:
        # prefer last latin token
        return tokens[-1]
    slug = re.sub(r"[^A-Za-z0-9]+", "", text)
    return slug[:40] if slug else fallback
