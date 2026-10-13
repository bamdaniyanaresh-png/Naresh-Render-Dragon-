import os

from .config import SHADERC


def setup_environment():
    if not SHADERC.is_file():
        raise RuntimeError(
            "BGFX shaderc was not found. Build the official bgfx tools, then either "
            "put shaderc on PATH or set BGFX_SHADERC to its full path."
        )
    if os.name != "nt" and not os.access(SHADERC, os.X_OK):
        raise RuntimeError(
            f"BGFX shaderc is not executable: {SHADERC}. "
            "Check its file permissions."
        )

    print(f"BGFX shaderc found: {SHADERC}")
    print("Compiler setup verified; no compiler binary was downloaded or copied.")
