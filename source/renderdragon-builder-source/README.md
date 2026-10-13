# Naresh RenderDragon Builder

Independent Lazurite-based RenderDragon material builder.

## Requirements

The builder compiles the project's material shaders with Lazurite and BGFX shaderc.

- Git
- Python 3.11+
- lazurite 0.9.0
- rich 15.x
- packaging 25+
- official BGFX `shaderc` compiler

Install Python packages:

```bash
python -m pip install -r requirements.txt
```

Build BGFX's shaderc tool by following its official build instructions:

```text
https://bkaradzic.github.io/bgfx/build.html
```

The BGFX, bx, and bimg source repositories are needed to build its tools. This
repository does not download or bundle a compiler binary. Point the builder to
the resulting `shaderc` executable:

Linux/macOS:

```bash
export BGFX_SHADERC="/absolute/path/to/shaderc"
```

Windows PowerShell:

```bash
$env:BGFX_SHADERC = "C:\\path\\to\\shaderc.exe"
```

Verify the compiler is available:

```bash
./build.sh setup
```

On Windows, use:

```bat
build.bat setup
```

## Input

Put legally obtained base material data in:

```text
assets/base-materials/
  RenderChunk.material.bin
  Sky.material.bin
  ...
```

Each source material can contain:

```text
src/materials/RenderChunk/
  fragment.sc
  vertex.sc
  varying.def.sc
  config.json
  material.json       # optional
```

Headers required by your shaders belong in `include/`.

Active project shaders use BGFX shader support and project-local renderer
headers. Minecraft base material files are separate inputs and are not covered
by this project's MIT license.

## Build

Build all materials that have a matching base material for Android:

```bash
./build.sh mats -p android
```

Build selected materials:

```bash
./build.sh mats -p android -m Sky EndSky RenderChunk
```

Other profiles:

```bash
./build.sh mats -p windows
./build.sh mats -p ios
./build.sh mats -p merged
```

Output is written to `build/<profile>/`.

No placeholder `.material.bin` is generated. A file is considered successful only when
Lazurite completes compilation and the expected output file exists.

The source list includes every material folder. A build skips only materials that do not
have a matching base input in `assets/base-materials/`; adding a legally obtained
`<Material>.material.bin` or `<Material>.material.json` makes that source eligible without
editing the builder. Use `python -m tool list` to see source folders and
`python -m tool interface [Material]` to inspect a material's vertex attributes, fragment
varyings, and built-in uniform mappings.

Run the builder's dependency-free checks with:

```bash
python -m unittest discover -s tests -v
```

## Shader inputs and runtime limits

For the `RenderChunk` material, `v_position` is a vertex-to-fragment varying added by this
project. It carries the transformed position already used by this material's fog-distance
calculation; it is not an engine-provided magic variable. `v_worldPos` remains available
as a compatibility name for that same value. `v_viewVector` is the vector from the
surface toward `ViewPositionAndTime.xyz`, in the same coordinate space.

The fragment shader also receives color, texture UV, decoded lightmap UV, and fog data.
Built-in values such as time, camera position, fog, weather, and biome are mapped in
`tool/config.py`; see `python -m tool interface RenderChunk` for the exact mapping names.
Shader compile-time settings can be passed with repeated `-D` options, for example:

```bash
./build.sh mats -p android -m RenderChunk -D ENABLE_EXAMPLE=1
```

Declaring an arbitrary `uniform` does not make Minecraft populate it. Runtime custom
uniforms require a compatible material metadata mapping and a loader/client that actually
sets the value. This builder cannot invent player position/velocity or other data absent
from the game's renderer interface. Verify custom effects in the target Minecraft version
and loader; successful compilation alone does not establish runtime compatibility.

## Notes

The builder is independent of any specific shader pack. It still uses Lazurite
and BGFX shaderc as third-party build tools. Base materials are Minecraft data;
their copyright and distribution terms are separate from this project's license.

Compiling a `.material.bin` does not make the standard Minecraft Bedrock client load it from a
resource pack. This builder does not modify Minecraft or install a material loader. Loading
custom materials requires a compatible external loader/modified client or a platform-specific
method of replacing the game's material files; compatibility depends on the Minecraft version
and graphics platform.

## Third-Party / Attribution

`include/bgfx_shader.sh` is from BGFX and retains its upstream copyright and
license header. Lazurite and BGFX shaderc are separate third-party tools.
Minecraft/RenderDragon material inputs remain the property of their respective
rights holders and are not relicensed by this project.

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and
[copyright information.txt](copyright%20information.txt) for details.
