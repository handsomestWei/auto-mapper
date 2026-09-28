from pathlib import Path

from tools.cli import main
from tools.validate import validate_dir


def test_cli_query_order(tmp_path):
    md = Path(__file__).resolve().parents[1] / "examples" / "query_order.md"
    code = main(["--input", str(md), "--out-dir", str(tmp_path)])
    assert code == 0
    errs = validate_dir(str(tmp_path))
    assert errs == []


def test_cli_two_apis(tmp_path):
    md = Path(__file__).resolve().parents[1] / "examples" / "two_apis.md"
    assert main(["--input", str(md), "--out-dir", str(tmp_path)]) == 0
    assert (tmp_path / "queryOrder" / "queryOrder-rule.xml").is_file()
    assert (tmp_path / "createOrder" / "createOrder-schema.xml").is_file()
