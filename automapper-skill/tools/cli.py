"""CLI: markdown/text → schema XML, JSON samples, optional rule XML."""

from __future__ import annotations

import argparse
import json
import sys

from .generate import generate_from_file
from .validate import validate_dir


def main(argv=None) -> int:
    p = argparse.ArgumentParser(description="Generate automapper schema/rule from markdown tables")
    p.add_argument("--input", "-i", required=True, help="Markdown/text file extracted from docs or screenshots")
    p.add_argument("--out-dir", "-o", required=True, help="Output directory (one subfolder per interface)")
    p.add_argument("--skip-validate", action="store_true")
    args = p.parse_args(argv)

    gens = generate_from_file(args.input, args.out_dir)
    summary = [
        {
            "id": g.interface_id,
            "has_schema": bool(g.schema_xml),
            "has_rule": bool(g.rule_xml),
            "notes": g.notes,
        }
        for g in gens
    ]
    print(json.dumps(summary, ensure_ascii=False, indent=2))
    if not args.skip_validate:
        errs = validate_dir(args.out_dir)
        if errs:
            print("VALIDATION_FAILED", file=sys.stderr)
            for e in errs:
                print(e, file=sys.stderr)
            return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
