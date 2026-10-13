$input a_color0, a_texcoord0, a_texcoord1, a_position
$output v_color0, v_ditheringAndMaskTinting, v_fog, v_lightmapUV, v_texcoord0, v_worldPos, v_position, v_viewVector

#include "bgfx_shader.sh"

uniform vec4 ViewPositionAndTime;
uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 MeshContext;

void main()
{
    vec4 world = mul(u_model[0], vec4(a_position, 1.0));

    gl_Position = mul(u_viewProj, world);

    v_color0 = a_color0;

    v_texcoord0 = a_texcoord0;

    vec2 lm = floor(a_texcoord1 * 15.0 + 0.5);
    v_lightmapUV = (lm + vec2(0.5)) / 16.0;

    v_worldPos = world.xyz;
    // Public position inputs for effects that need per-fragment geometry data.
    // v_position intentionally aliases the exact space used by the fog calculation.
    v_position = world.xyz;
    v_viewVector = ViewPositionAndTime.xyz - world.xyz;

    v_ditheringAndMaskTinting = vec2(0.0);

    float distanceToCamera =
        length(world.xyz - ViewPositionAndTime.xyz);

    float fogStart = FogAndDistanceControl.x;
    float fogEnd = max(FogAndDistanceControl.y, fogStart + 0.0001);

    float fog = clamp(
        (distanceToCamera - fogStart) /
        (fogEnd - fogStart),
        0.0,
        1.0
    );

    fog = clamp(
        fog + RenderChunkFogAlpha.x,
        0.0,
        1.0
    );

    v_fog = vec4(FogColor.rgb, fog);
}
