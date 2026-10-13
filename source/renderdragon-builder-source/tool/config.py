from pathlib import Path
import os
import platform
import shutil
import sys

ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = ROOT / "src" / "materials"
BASE_MATERIALS = ROOT / "assets" / "base-materials"
INCLUDE_ROOT = ROOT / "include"
DATA_ROOT = ROOT / "tool" / "data"
SHADERC_OVERRIDE = os.environ.get("BGFX_SHADERC")
SHADERC_COMMAND = shutil.which(
    "shaderc.exe" if platform.system() == "Windows" else "shaderc"
)
if SHADERC_OVERRIDE:
    SHADERC = Path(SHADERC_OVERRIDE).expanduser().resolve()
elif SHADERC_COMMAND:
    SHADERC = Path(SHADERC_COMMAND).resolve()
else:
    SHADERC = DATA_ROOT / ("shaderc.exe" if platform.system() == "Windows" else "shaderc")
BUILD_ROOT = ROOT / "build"

PROFILES = {
    "android": ["ESSL_310"],
    "windows": ["Direct3D_SM50"],
    "ios": ["Metal"],
    "merged": ["ESSL_310", "Direct3D_SM50", "Metal"],
}

MATERIAL_FILES = ("fragment.sc", "vertex.sc", "varying.def.sc", "config.json")

DEFAULT_UNIFORM_OVERRIDES = {
    # Common renderer time interface.
    # These names intentionally do not collide with legacy material uniforms.
    "RendererTimeOfDay": "BUILTIN_MinecraftTimeOfDay",
    "RendererElapsedFrameTime": "BUILTIN_MinecraftElapsedFrameTime",
    "RendererViewPositionAndTime": "BUILTIN_MinecraftViewPositionAndTime",
    "ViewPositionAndTime": "BUILTIN_MinecraftViewPositionAndTime",

    "WeatherID": "BUILTIN_MinecraftWeatherID",
    "BiomeID": "BUILTIN_MinecraftBiomeID",
    "CloudHeight": "BUILTIN_MinecraftCloudHeight",
    "Day": "BUILTIN_MinecraftDay",
    "DimensionID": "BUILTIN_MinecraftDimensionID",
    "LocalClientID": "BUILTIN_MinecraftLocalClientID",
    "MoonPhase": "BUILTIN_MinecraftMoonPhase",
    "RenderDistance": "BUILTIN_MinecraftRenderDistance",
    "TimeOfDay": "BUILTIN_MinecraftTimeOfDay",
    "ElapsedFrameTime": "BUILTIN_MinecraftElapsedFrameTime",
    "CameraFacingDirection": "BUILTIN_MinecraftCameraFacingDirection",
    "CameraPosition": "BUILTIN_MinecraftCameraPosition",
    "LastCameraFacingDirection": "BUILTIN_MinecraftLastCameraFacingDirection",
    "LastCameraPosition": "BUILTIN_MinecraftLastCameraPosition",
    "SunDirection": "BUILTIN_MinecraftSunDirection",
    "CloudColor": "BUILTIN_MinecraftCloudColor",
    "FogColor": "BUILTIN_MinecraftFogColor",
}

def require_python() -> None:
    if sys.version_info < (3, 11):
        raise RuntimeError("Python 3.11 or newer is required.")
