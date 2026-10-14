Naresh RenderDragon Builder

Independent Lazurite-based RenderDragon material builder.

This repository can build the Minecraft Bedrock render dragon shader in different ways and to run this repository, basematerial.bins must be present in the basematerial folder

How does this repository work?

This repository first uses materials.bins from the base-materials folder, takes this base materials.bin, injects source code into it, and creates a new materials.bin in the builds folder

Project Status: Work in Progress

This repository is currently under active development and is not yet fully complete. New features, improvements, bug fixes, and documentation may be added over time.

About This Project

This repository contains the source code and build tools for compiling Minecraft Bedrock Edition RenderDragon shaders and shader materials.

The project is intended to help developers and Minecraft creators rebuild, modify, edit, and customize shader materials, as well as develop their own shader projects using the available source code and build system.

What You Can Do

- Build and Rebuild: Compile shader materials from the available source code and build tools.
- Edit and Customize: Modify supported shader code, configurations, and material settings to create custom visual effects.
- Create Custom Shaders: Use this project as a foundation for developing your own Minecraft Bedrock Edition RenderDragon shader projects.
- Build Output Files: Generate supported compiled material files, such as ".material.bin", and package compatible resources into ".mcpack" files where applicable.
- Customize RenderChunk: Modify supported lighting, day/night color response, foliage animation, water appearance, reflection cues, and optional procedural ground-shadow effects.
- Inspect Material Interfaces: Explore supported material inputs, vertex attributes, fragment varyings, and built-in uniform mappings.
- Minecraft Integration: Use compatible shader builds through supported patching methods or tools such as MB Loader, depending on Minecraft version, device, and tool compatibility.

Important Notes

This project is still a work in progress. Some features, build configurations, and workflows may be incomplete, unstable, or require additional setup.

Successful compilation and in-game compatibility may depend on the Minecraft Bedrock Edition version, build environment, shader implementation, and tools used.

Contributions, testing, bug reports, and suggestions are welcome.

Project Information

- Project Status: Active Development
- Target Platform: Minecraft Bedrock Edition — RenderDragon
- Purpose: Shader development, compilation, modification, and customization
- Build System: Lazurite and BGFX shaderc
- License: MIT for the project's covered source code; third-party and Minecraft material data retain their respective licensing terms.

Requirements

The builder compiles the project's material shaders with Lazurite and BGFX shaderc.

- Git
- Python 3.11+
- Lazurite 0.10.0
- "rich" 15.x
- "packaging" 25+
- Official BGFX "shaderc" compiler

Install Python packages

python -m pip install -r requirements.txt

Build BGFX's shaderc tool by following its official build instructions:

https://bkaradzic.github.io/bgfx/build.html

The BGFX, bx, and bimg source repositories are needed to build its tools. This repository does not download or bundle a compiler binary. Point the builder to the resulting "shaderc" executable.

Linux/macOS:

export BGFX_SHADERC="/absolute/path/to/shaderc"

Windows PowerShell:

$env:BGFX_SHADERC = "C:\path\to\shaderc.exe"

Verify the setup

Linux/macOS:

./build.sh setup

Windows:

build.bat setup

Input

Put legally obtained base material data in:

assets/base-materials/
  RenderChunk.material.bin
  Sky.material.bin
  ...

Each source material can contain files such as:

src/materials/RenderChunk/
  fragment.sc
  vertex.sc
  varying.def.sc
  config.json
  material.json       # optional

Headers required by your shaders belong in "include/".

Active project shaders use BGFX shader support and project-local renderer headers. Minecraft base material files are separate inputs and are not covered by this project's MIT license.

Build

Build all eligible materials for Android

./build.sh mats -p android

Build selected materials

./build.sh mats -p android -m Sky EndSky RenderChunk

Other build profiles

./build.sh mats -p windows
./build.sh mats -p ios
./build.sh mats -p merged

Output is written to:

build/<profile>/

No placeholder ".material.bin" is generated. A file is considered successful only when Lazurite completes compilation and the expected output file exists.

The source list includes every material folder. A build skips only materials that do not have a matching base input in "assets/base-materials/". Adding a legally obtained "<Material>.material.bin" or "<Material>.material.json" makes that source eligible without editing the builder.

Use the following commands to inspect available materials and their interfaces:

python -m tool list
python -m tool interface RenderChunk

Replace "RenderChunk" with the material name you want to inspect.

Run the builder's dependency-free checks with:

python -m unittest discover -s tests -v

RenderChunk Features

The independent "RenderChunk" pass supports the engine's instancing, billboard, seasons, alpha-test, opaque, transparent, and depth-only variants.

Its supported shader effects include:

- Day/Night Color Response: Lightmap-based color changes for different lighting conditions.
- Soft Highlight Shaping: Adjustments to surface highlights and lighting appearance.
- Foliage Sway: Shader-based movement for supported foliage rendering.
- Water Tint: Customizable water color response.
- Screen-Space Reflection Cues: Visual cues intended to improve the appearance of reflections.
- Optional Procedural Player-Shaped Ground Shadow: An estimated player-shaped shadow effect based on available camera values.

These are shader-level effects with limitations imposed by the available RenderDragon material interface. They should not be confused with full engine-level geometry shadows or physically accurate reflections.

Player Shadow

The optional procedural player-shaped ground shadow estimates its anchor and movement from camera values because the available material interface does not expose a player transform.

This means the effect has assumptions and limitations, particularly in third-person gameplay. It is not a replacement for a renderer-provided player shadow.

Read ""docs/PLAYER_SHADOW.md"" (docs/PLAYER_SHADOW.md) for its assumptions, limitations, and compile-time controls before relying on this feature in-game.

Surface Style and Color Controls

Tune the day/night palette and water response using the controls documented in ""docs/SURFACE_STYLE.md"" (docs/SURFACE_STYLE.md).

For current shader-source coverage and the base-material inputs still needed to compile every source folder, see ""docs/BUILD_STATUS.md"" (docs/BUILD_STATUS.md).

Shader Inputs and Runtime Limits

For the "RenderChunk" material, "v_position" is a vertex-to-fragment varying added by this project. It carries the transformed position already used by this material's fog-distance calculation; it is not an engine-provided magic variable.

"v_worldPos" remains available as a compatibility name for that same value.

"v_viewVector" is the vector from the surface toward "ViewPositionAndTime.xyz", in the same coordinate space.

The fragment shader also receives color, texture UV, decoded lightmap UV, and fog data. Built-in values such as time, camera position, fog, weather, and biome are mapped in "tool/config.py".

Use the following command to inspect the exact mapping names:

python -m tool interface RenderChunk

Compile-Time Settings

Shader compile-time settings can be passed with repeated "-D" options. For example:

./build.sh mats -p android -m RenderChunk -D ENABLE_EXAMPLE=1

The exact available settings depend on the shader source and its implementation.

Custom Uniform Limitations

Declaring an arbitrary "uniform" does not make Minecraft populate it. Runtime custom uniforms require a compatible material metadata mapping and a loader/client that actually sets the value.

This builder cannot invent player position, velocity, or other data absent from the game's renderer interface.

Verify custom effects in the target Minecraft version and loader. Successful compilation alone does not establish runtime compatibility.

Minecraft Integration and Compatibility

Compiling a ".material.bin" does not make the standard Minecraft Bedrock client load it from a resource pack.

This builder does not modify Minecraft or install a material loader. Loading custom materials requires a compatible external loader, modified client, or a platform-specific method of replacing the game's material files.

Compatibility depends on the Minecraft version, device, graphics platform, and loader used.

The builder is independent of any specific shader pack. It uses Lazurite and BGFX shaderc as third-party build tools.

Third-Party / Attribution

"include/bgfx_shader.sh" is from BGFX and retains its upstream copyright and license header.

Lazurite and BGFX shaderc are separate third-party tools. Minecraft/RenderDragon material inputs remain the property of their respective rights holders and are not relicensed by this project.

See ""THIRD_PARTY_NOTICES.md"" (THIRD_PARTY_NOTICES.md) and ""copyright information.txt"" (copyright%20information.txt) for details.
