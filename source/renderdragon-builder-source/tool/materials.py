from pathlib import Path
import json
import shutil

from .config import (
    BASE_MATERIALS,
    DEFAULT_UNIFORM_OVERRIDES,
    MATERIAL_FILES,
    SOURCE_ROOT,
)


def material_names():
    if not SOURCE_ROOT.is_dir():
        return []
    return sorted(
        p.name for p in SOURCE_ROOT.iterdir()
        if (
            p.is_dir()
            and not p.name.startswith((".", "_"))
            and ".backup" not in p.name.lower()
        )
    )


def available_base_materials() -> set[str]:
    """Return material names with a matching, locally supplied base material."""
    if not BASE_MATERIALS.is_dir():
        return set()

    names = set()
    for path in BASE_MATERIALS.iterdir():
        if not path.is_file():
            continue
        if path.name.endswith(".material.bin"):
            names.add(path.name[:-len(".material.bin")])
        elif path.name.endswith(".material.json"):
            names.add(path.name[:-len(".material.json")])
    return names


def select_buildable_materials(selected: list[str]) -> tuple[list[str], list[str]]:
    """Split selected source folders into buildable names and missing-base skips."""
    base_names = available_base_materials()
    buildable = [name for name in selected if name in base_names]
    missing = [name for name in selected if name not in base_names]
    return buildable, missing


def validate_material(name: str) -> list[str]:
    folder = SOURCE_ROOT / name
    errors = []
    if not folder.is_dir():
        return [f"material '{name}' does not exist in src/materials"]
    for filename in MATERIAL_FILES:
        if not (folder / filename).is_file():
            errors.append(f"{name}: missing {filename}")
    try:
        json.loads((folder / "config.json").read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"{name}: invalid config.json: {exc}")
    return errors


def validate_base_materials(selected: list[str]) -> list[str]:
    errors = []
    if not BASE_MATERIALS.is_dir():
        return [f"missing {BASE_MATERIALS.relative_to(BASE_MATERIALS.parents[1])}/ directory"]
    if not available_base_materials():
        return ["no .material.bin or .material.json files found in assets/base-materials"]
    # Lazurite matches merge sources by material name. Check selected names early.
    available = available_base_materials()
    for name in selected:
        if name not in available:
            errors.append(
                f"{name}: no matching base material found in assets/base-materials "
                f"(expected {name}.material.bin or {name}.material.json)"
            )
    return errors


def prepare_workspace(root: Path, selected: list[str]) -> Path:
    """Copy the whole project so all original relative paths remain valid."""
    work = root / "build" / ".work"
    if work.exists():
        shutil.rmtree(work)
    shutil.copytree(root, work, ignore=shutil.ignore_patterns(".git", "build", "__pycache__", "*.pyc"))

    materials = work / "src" / "materials"
    for child in list(materials.iterdir()):
        if child.is_dir() and not child.name.startswith((".", "_")) and child.name not in selected:
            shutil.rmtree(child)

    # Common Minecraft builtin-uniform mapping is generated only when the
    # material author did not provide their own material.json. This keeps the
    # public input as fragment/vertex/varying/config while preserving custom
    # metadata when supplied.
    for folder in materials.iterdir():
        if not folder.is_dir() or folder.name.startswith((".", "_")):
            continue
        metadata = folder / "material.json"
        if not metadata.exists():
            metadata.write_text(
                json.dumps({"uniform_overrides": DEFAULT_UNIFORM_OVERRIDES}, indent=2) + "\n",
                encoding="utf-8",
            )
    return work
