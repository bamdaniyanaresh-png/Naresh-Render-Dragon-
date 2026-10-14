# Shader inputs and controls

This project compiles BGFX/Lazurite material shaders. The material interface is
the contract between the engine's mesh/uniform data and the shader stages. It
is not a general-purpose runtime parameter system.

## Inspecting an interface

Run:

```bash
python -m tool interface RenderChunk
```

This reads the selected material's `varying.def.sc`, vertex/fragment stage
inputs, and effective `material.json` uniform overrides. `python -m tool list`
shows which source folders have matching base-material inputs and are currently
eligible to compile.

## RenderChunk values in this source tree

| Name | Type | Meaning |
|---|---|---|
| `a_position` | `vec3` | Mesh position attribute received by the vertex shader. |
| `a_color0` | `vec4` | Per-vertex color. |
| `a_texcoord0` | `vec2` | Material texture coordinates. |
| `a_texcoord1` | `vec2` | Packed lightmap input; decoded to `v_lightmapUV`. |
| `v_position` | `vec3` | Transformed position passed to the fragment shader. It is not an engine builtin. |
| `v_worldPos` | `vec3` | Compatibility alias carrying the same transformed position as `v_position`. |
| `v_viewVector` | `vec3` | `ViewPositionAndTime.xyz - transformedPosition`, in the same coordinate space used by this material's fog-distance calculation. |
| `v_color0` | `vec4` | Interpolated vertex color. |
| `v_texcoord0` | `vec2` | Interpolated material texture UV. |
| `v_lightmapUV` | `vec2` | Decoded lightmap UV used to sample `s_LightMapTexture`. |
| `v_fog` | `vec4` | Fog RGB in `.rgb`, fog blend amount in `.a`. |
| `v_shadowData` | `vec2` | Renderer animation clock and horizontal camera travel, passed from vertex to fragment for the procedural shadow and surface animation. |

Example fragment-stage use:

```glsl
vec3 effectPosition = v_position;
vec3 toCamera = normalize(v_viewVector);
```

The position is exactly the post-`u_model` value already used by the existing
RenderChunk fog calculation. Treat it as renderer/material space; do not assume
it is absolute world space. To convert between absolute and camera-relative
coordinates, first verify the target game's coordinate convention and the
matching `CameraPosition`/`ViewPositionAndTime` basis.

## Built-in uniform values

`tool/config.py` maps the project's supported uniform names to RenderDragon
builtin names. The list includes time (`TimeOfDay`, `ElapsedFrameTime`,
`ViewPositionAndTime`), camera positions/directions, fog and cloud colors,
weather, biome, dimension, moon phase, and render distance. The
`interface` command prints the exact names and mappings applied to each
material. A material with its own `material.json` uses that explicit
`uniform_overrides` map; it is not silently replaced with the default map.

All of these names and values are version-sensitive. A compiler can check
syntax, but only the target Minecraft version and loader can confirm that a
builtin is actually bound and has the expected meaning.

RenderChunk now maps the engine's instancing, billboard, seasons, depth, alpha,
opaque, and transparent variants. Its independent surface pass adds a
day/night lightmap tint, subtle tone shaping, animated vegetation/water
approximations, and the optional player-shaped shadow. These are shader-side
approximations; they do not add custom game uniforms.

The player-shaped shadow uses the existing view-vector varying plus camera,
time, and sun builtins. It does not add a custom player-position uniform; the
camera-based anchor and movement are estimates documented in
[`PLAYER_SHADOW.md`](PLAYER_SHADOW.md).

## What cannot be added by declaring a shader variable

`v_position` is made available by matching three declarations/assignments:

1. A `v_position` output in `vertex.sc`.
2. A matching `vec3 v_position : TEXCOORD...;` in `varying.def.sc`.
3. A matching `$input v_position` in `fragment.sc`.

It is vertex-to-fragment data, not a uniform and not an automatically supplied
game value. An arbitrary `uniform vec4 MyControl;` also does not create a
runtime control in Minecraft. A runtime uniform must be recognized by the
renderer, represented in material metadata, and set by the active loader.
Compile-time options can instead be declared as macros and passed with
`-D NAME=value`.

This source's RenderChunk mesh interface does not provide player world
position/velocity or a vertex normal attribute. Those cannot be reconstructed
reliably from a camera position alone. Effects that need those values require
an engine/loader interface that supplies them.
