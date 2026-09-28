"""Generate per-interface schema / JSON / rule files."""

from __future__ import annotations

import os
from dataclasses import dataclass
from typing import List, Optional

from .emit import emit_rule_xml, emit_sample_json, emit_schema_xml
from .model import InterfaceSpec
from .parse_markdown import parse_markdown
from .rules import build_rules


@dataclass
class GeneratedFiles:
    interface_id: str
    schema_xml: Optional[str] = None
    sample_json: Optional[str] = None
    src_sample_json: Optional[str] = None
    src_schema_xml: Optional[str] = None
    rule_xml: Optional[str] = None
    notes: List[str] = None

    def __post_init__(self):
        if self.notes is None:
            self.notes = []


def generate_interface(spec: InterfaceSpec) -> GeneratedFiles:
    out = GeneratedFiles(interface_id=spec.id)
    tgt = spec.target
    src = spec.source
    if tgt and tgt.fields:
        desc = tgt.desc or spec.desc or spec.id
        out.schema_xml = emit_schema_xml(spec.id, desc, tgt.fields)
        out.sample_json = emit_sample_json(tgt.fields)
    elif src and src.fields:
        # only source: still emit schema so designer has a tree (treat as target)
        desc = src.desc or spec.desc or spec.id
        out.schema_xml = emit_schema_xml(spec.id, desc, src.fields)
        out.sample_json = emit_sample_json(src.fields)
        out.notes.append("仅识别到一侧字段，已按目标 schema 输出，未生成 rule")
        return out
    else:
        out.notes.append("未解析到字段表")
        return out

    if src and src.fields:
        out.src_sample_json = emit_sample_json(src.fields)
        out.src_schema_xml = emit_schema_xml(f"{spec.id}Src", src.desc or "source", src.fields)
        if tgt and tgt.fields:
            rows = build_rules(src.fields, tgt.fields)
            out.rule_xml = emit_rule_xml(spec.id, spec.desc or spec.id, rows)
        else:
            out.notes.append("有源无目标，未生成 rule")
    else:
        out.notes.append("未识别到源侧，仅生成目标 schema 与 JSON 样例")
    return out


def write_generated(gen: GeneratedFiles, out_dir: str) -> List[str]:
    folder = os.path.join(out_dir, gen.interface_id)
    os.makedirs(folder, exist_ok=True)
    written = []

    def dump(name: str, content: Optional[str]) -> None:
        if not content:
            return
        path = os.path.join(folder, name)
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            f.write(content)
        written.append(path)

    dump(f"{gen.interface_id}-schema.xml", gen.schema_xml)
    dump(f"{gen.interface_id}-sample.json", gen.sample_json)
    dump(f"{gen.interface_id}-src-sample.json", gen.src_sample_json)
    dump(f"{gen.interface_id}Src-schema.xml", gen.src_schema_xml)
    dump(f"{gen.interface_id}-rule.xml", gen.rule_xml)
    if gen.notes:
        dump(
            "NOTES.md",
            "# 生成说明\n\n" + "\n".join(f"- {n}" for n in gen.notes) + "\n",
        )
    return written


def generate_from_markdown(text: str, out_dir: str) -> List[GeneratedFiles]:
    specs = parse_markdown(text)
    results = [generate_interface(s) for s in specs]
    for g in results:
        write_generated(g, out_dir)
    return results


def generate_from_file(path: str, out_dir: str) -> List[GeneratedFiles]:
    with open(path, encoding="utf-8") as f:
        text = f.read()
    return generate_from_markdown(text, out_dir)
