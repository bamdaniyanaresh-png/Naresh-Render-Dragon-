from pathlib import Path
import shutil

from .config import BUILD_ROOT, PROFILES, ROOT, SHADERC, require_python
from .materials import (
    material_names,
    prepare_workspace,
    select_buildable_materials,
    validate_base_materials,
    validate_material,
)


def _check_lazurite():
    try:
        import lazurite  # noqa: F401
        from importlib.metadata import version
        found = version("lazurite")
    except Exception as exc:
        raise RuntimeError("Lazurite is not installed. Run: python -m pip install -r requirements.txt") from exc
    if found != "0.9.0":
        raise RuntimeError(f"Lazurite 0.9.0 is required; found {found}.")


def _check_rich():
    try:
        from importlib.metadata import version
        found = version("rich")
    except Exception as exc:
        raise RuntimeError("rich is not installed. Run: python -m pip install -r requirements.txt") from exc
    if not found.startswith("15."):
        raise RuntimeError(f"rich 15.x is required; found {found}.")


def check_environment():
    require_python()
    _check_lazurite()
    _check_rich()
    return True


def check(selected=None):
    if selected is None:
        validation_names = material_names()
        selected, skipped = select_buildable_materials(validation_names)
        if skipped:
            print(
                "[Naresh Builder] source materials without base inputs are not buildable: "
                + ", ".join(skipped)
            )
    else:
        selected = list(selected)
        validation_names = selected

    errors = []
    if not validation_names:
        errors.append("no materials found in src/materials")
    for name in validation_names:
        errors.extend(validate_material(name))
    errors.extend(validate_base_materials(selected))
    if not SHADERC.is_file():
        errors.append(
            "BGFX shaderc was not found; set BGFX_SHADERC or put shaderc on PATH "
            "and run the setup check"
        )
    return errors


def _compile(work: Path, profile: str, output: Path, selected: list[str], defines=None):
    from lazurite.compiler.macro_define import MacroDefine
    from lazurite.project import project

    output.mkdir(parents=True, exist_ok=True)
    project_path = work / "src" / "materials"
    patterns = [project_path / name for name in selected]
    macro_defines = [MacroDefine.from_string(x) for x in (defines or [])]

    project.compile(
        project_path=str(project_path),
        profiles=[profile],
        output_folder=str(output),
        material_patterns=[str(p) for p in patterns],
        shaderc_path=str(SHADERC),
        defines=macro_defines,
    )


def build(profile: str, selected: list[str], defines=None):
    check_environment()
    if profile not in PROFILES:
        raise RuntimeError(f"unsupported profile: {profile}")

    all_names = material_names()
    selected = list(selected or all_names)

    unknown = [name for name in selected if name not in all_names]
    if unknown:
        raise RuntimeError("unknown material(s): " + ", ".join(unknown))

    selected, skipped = select_buildable_materials(selected)
    if skipped:
        print(
            "[Naresh Builder] skipping materials without matching base material input: "
            + ", ".join(skipped)
        )
    if not selected:
        raise RuntimeError(
            "no buildable materials selected; add a matching .material.bin or "
            ".material.json under assets/base-materials"
        )

    errors = check(selected)
    if errors:
        raise RuntimeError("\n".join("- " + e for e in errors))

    work = prepare_workspace(ROOT, selected)
    output = BUILD_ROOT / profile
    output.mkdir(parents=True, exist_ok=True)
    # Do not silently leave stale successful artifacts behind.
    for name in selected:
        stale = output / f"{name}.material.bin"
        if stale.exists():
            stale.unlink()

    try:
        print(f"[Naresh Builder] compiling {', '.join(selected)} -> {profile}")
        _compile(work, profile, output, selected, defines)
    finally:
        shutil.rmtree(work, ignore_errors=True)

    missing = [name for name in selected if not (output / f"{name}.material.bin").is_file()]
    if missing:
        raise RuntimeError("Lazurite completed without expected material.bin: " + ", ".join(missing))

    print("\nBuilt:")
    for name in selected:
        print(f"  {output.relative_to(ROOT)}/{name}.material.bin")


def clean():
    shutil.rmtree(BUILD_ROOT / ".work", ignore_errors=True)
    for profile in PROFILES:
        shutil.rmtree(BUILD_ROOT / profile, ignore_errors=True)
    print("Build outputs cleaned.")
