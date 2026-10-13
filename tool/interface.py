"""Inspect the material inputs exposed by this builder."""

import json
import re

from .config import DEFAULT_UNIFORM_OVERRIDES, SOURCE_ROOT

_DECLARATION = re.compile(
    r"^\s*(?:(?:centroid|flat|smooth|noperspective)\s+)?"
    r"(bool|int|uint|float|vec[234]|ivec[234]|uvec[234]|mat[234])\s+"
    r"([A-Za-z_]\w*)\s*:\s*([A-Za-z_]\w*)\s*;",
    re.MULTILINE,
)
_STAGE_IO = re.compile(r"^\s*\$(input|output)\s+(.+?)\s*$", re.MULTILINE)


def _stage_symbols(source: str, direction: str) -> list[str]:
    symbols = []
    for match in _STAGE_IO.finditer(source):
        if match.group(1) != direction:
            continue
        symbols.extend(
            part.strip()
            for part in match.group(2).split(",")
            if part.strip() and not part.strip().startswith("//")
        )
    return symbols


def inspect_material(name: str) -> dict:
    folder = (SOURCE_ROOT / name).resolve()
    source_root = SOURCE_ROOT.resolve()
    if folder.parent != source_root or not folder.is_dir():
        raise ValueError(f"material '{name}' does not exist in src/materials")

    varying_file = folder / "varying.def.sc"
    vertex_file = folder / "vertex.sc"
    fragment_file = folder / "fragment.sc"
    missing = [
        path.name
        for path in (varying_file, vertex_file, fragment_file)
        if not path.is_file()
    ]
    if missing:
        raise ValueError(f"{name}: missing " + ", ".join(missing))

    varying_source = varying_file.read_text(encoding="utf-8")
    vertex_source = vertex_file.read_text(encoding="utf-8")
    fragment_source = fragment_file.read_text(encoding="utf-8")
    declarations = [
        {"type": match.group(1), "name": match.group(2), "semantic": match.group(3)}
        for match in _DECLARATION.finditer(varying_source)
    ]
    metadata_file = folder / "material.json"
    overrides = dict(DEFAULT_UNIFORM_OVERRIDES)
    if metadata_file.is_file():
        try:
            material_metadata = json.loads(metadata_file.read_text(encoding="utf-8"))
            if not isinstance(material_metadata, dict):
                raise ValueError("top-level value must be an object")
            overrides = material_metadata.get("uniform_overrides", {})
        except (json.JSONDecodeError, OSError) as exc:
            raise ValueError(f"{name}: invalid material.json: {exc}") from exc
        if not isinstance(overrides, dict) or not all(
            isinstance(key, str) and isinstance(value, str)
            for key, value in overrides.items()
        ):
            raise ValueError(
                f"{name}: material.json uniform_overrides must map strings to strings"
            )

    return {
        "name": name,
        "attributes": [item for item in declarations if item["name"].startswith(("a_", "i_"))],
        "varyings": [item for item in declarations if item["name"].startswith("v_")],
        "vertex_inputs": _stage_symbols(vertex_source, "input"),
        "vertex_outputs": _stage_symbols(vertex_source, "output"),
        "fragment_inputs": _stage_symbols(fragment_source, "input"),
        "uniform_overrides": overrides,
    }


def print_interface(name: str) -> None:
    info = inspect_material(name)
    print(f"{info['name']} shader interface")

    print("\nVertex attributes:")
    if info["attributes"]:
        for item in info["attributes"]:
            print(f"  {item['name']:<30} {item['type']:<8} {item['semantic']}")
    else:
        print("  (none declared)")

    print("\nVertex-to-fragment varyings:")
    if info["varyings"]:
        for item in info["varyings"]:
            print(f"  {item['name']:<30} {item['type']:<8} {item['semantic']}")
    else:
        print("  (none declared)")

    print("\nStage linkage:")
    print("  vertex inputs:   " + (", ".join(info["vertex_inputs"]) or "(none)"))
    print("  vertex outputs:  " + (", ".join(info["vertex_outputs"]) or "(none)"))
    print("  fragment inputs: " + (", ".join(info["fragment_inputs"]) or "(none)"))

    print("\nUniform overrides available to this material:")
    for uniform, builtin in sorted(info["uniform_overrides"].items()):
        print(f"  {uniform:<34} -> {builtin}")

    print(
        "\nThese are compile-time and material-interface declarations, not a promise "
        "that every Minecraft version or loader supplies each runtime value."
    )
