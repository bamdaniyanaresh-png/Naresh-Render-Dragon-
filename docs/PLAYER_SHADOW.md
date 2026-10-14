# Procedural player-shaped shadow

`RenderChunk` includes an original, analytic silhouette effect in
`include/renderer/effects/player_shadow.h`. It combines a softened head,
torso, hands, and two animated leg shapes, projects the silhouette away from
the built-in sun direction, and applies it before fog. The effect uses the
kit's time-and-motion behavior with this project's own `RD_PLAYER_SHADOW_*`
controls and coordinate interface.

## Important limit: the engine does not provide a player transform

The current `RenderChunk` interface exposes camera position and direction, but
not player position, player facing, or player velocity. The effect therefore
estimates the feet as the camera position minus a configurable eye-to-feet
offset. Camera facing and camera movement are only proxies for player facing
and movement. This is most plausible in first-person play; it will drift or
animate incorrectly when a third-person camera moves independently.

The effect also assumes the renderer's Y axis is vertical. It obtains the
surface-to-camera vector from the same transformed position used by RenderChunk
fog, then derives the camera-to-surface offset from that varying. Horizontal
camera travel and the animation clock are passed through `v_shadowData`; no
custom player-position uniform is declared. The final behavior still needs
checking in the target Minecraft version and loader. The shader has no
surface-normal varying, so the foot-height fade is an approximation for
excluding walls.

## Controls

All controls are compile-time macros. Override them using the builder's
repeated `-D NAME=value` argument:

| Macro | Default | Meaning |
|---|---:|---|
| `RD_PLAYER_SHADOW_ENABLED` | `1` | Set to `0` to compile the effect out. |
| `RD_PLAYER_SHADOW_EYE_TO_FEET` | `1.62` | Camera-to-foot estimate in blocks. |
| `RD_PLAYER_SHADOW_OPACITY` | `0.48` | Overall darkening strength. |
| `RD_PLAYER_SHADOW_EDGE` | `0.045` | Soft silhouette edge width. |
| `RD_PLAYER_SHADOW_WALK_START` | `0.004` | Camera travel where leg sway starts. |
| `RD_PLAYER_SHADOW_WALK_FULL` | `0.040` | Camera travel where sway reaches full strength. |
| `RD_PLAYER_SHADOW_WALK_RATE` | `7.2` | Animation speed against renderer time. |
| `RD_PLAYER_SHADOW_LEG_SWAY` | `0.13` | Limb displacement during movement. |
| `RD_PLAYER_SHADOW_GROUND_FADE` | `0.34` | Height range used to fade the mask off surfaces. |
| `RD_PLAYER_SHADOW_TINT` | `vec3(0.24, 0.28, 0.34)` | RGB tint blended into the lit color. |

Example:

```bash
./build.sh mats -p android -m RenderChunk -D RD_PLAYER_SHADOW_OPACITY=0.38
```

This is a screen-space material approximation, not a renderer shadow map or a
shadow cast by the actual player mesh. For exact anchoring and animation, a
compatible loader must provide the player's world transform and velocity to
the material.
