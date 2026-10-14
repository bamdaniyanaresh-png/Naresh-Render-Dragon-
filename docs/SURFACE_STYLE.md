# RenderChunk surface controls

The independent surface treatment is implemented in
`include/renderer/effects/surface_style.h` and wired into
`src/materials/RenderChunk/vertex.sc` and `fragment.sc`. Controls are compile-time
macros; pass overrides with `-D NAME=value` when building.

| Macro | Default | Effect |
|---|---:|---|
| `RD_STYLE_DAY_TINT` | `vec3(1.04, 1.01, 0.95)` | Daylight balance applied to the game lightmap. |
| `RD_STYLE_NIGHT_TINT` | `vec3(0.80, 0.87, 1.08)` | Cool nighttime lightmap balance. |
| `RD_STYLE_SUNSET_TINT` | `vec3(1.13, 0.84, 0.66)` | Warm color near the sun's horizon. |
| `RD_STYLE_AMBIENT_FLOOR` | `0.045` | Minimum lightmap fill. |
| `RD_STYLE_SATURATION` | `1.045` | Final color saturation. |
| `RD_STYLE_HIGHLIGHT_ROLLOFF` | `0.10` | Soft highlight compression. |
| `RD_WATER_TINT` | `vec3(0.56, 0.79, 0.91)` | Transparent-surface water tint. |
| `RD_WATER_WAVE_RATE` | `1.35` | Time rate for vertex displacement and water ripples. |
| `RD_WATER_SHEEN` | `0.12` | Animated grazing-angle highlight strength. |
| `RD_WATER_REFLECTION` | `0.24` | Fog-color horizon reflection approximation. |
| `RD_WATER_SPECULAR` | `0.18` | Sun-alignment glint strength. |

Example:

```bash
./build.sh mats -p android -m RenderChunk \
  -D RD_STYLE_SATURATION=1.08 \
  -D 'RD_WATER_TINT=vec3(0.48,0.74,0.90)'
```

The sun tint responds to the built-in `SunDirection`; the water effect is a
mesh-color heuristic because this source interface does not expose a water
material ID, surface normals, or an environment cubemap. It is a stylized
reflection cue, not a physically based reflection pass. Plant motion likewise
uses vertex color and mesh position heuristics, so unusual alpha-tested meshes
may move slightly.

The values and macro names in this implementation are project-local `RD_*`
controls. No `NL_*` configuration is required.
