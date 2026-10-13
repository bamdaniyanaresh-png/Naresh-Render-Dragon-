$input a_color0, a_position, a_texcoord0
$output v_color0, v_fog, v_occlusionHeight, v_occlusionUV, v_texcoord0, v_worldPos

#include "bgfx_shader.sh"

uniform vec4 UVOffsetAndScale;
uniform vec4 PositionBaseOffset;
uniform vec4 PositionForwardOffset;
uniform vec4 Velocity;
uniform vec4 Dimensions;
uniform vec4 ViewPosition;
uniform vec4 FogAndDistanceControl;
uniform vec4 FogColor;

void main()
{
    vec3 worldPos =
        a_position +
        PositionBaseOffset.xyz;

    worldPos +=
        PositionForwardOffset.xyz;

    worldPos +=
        Velocity.xyz;

    vec4 world =
        vec4(worldPos, 1.0);

    gl_Position =
        mul(u_viewProj, world);

    v_worldPos = worldPos;
    v_color0 = a_color0;

    v_texcoord0 =
        a_texcoord0 * UVOffsetAndScale.zw +
        UVOffsetAndScale.xy;

    v_occlusionHeight =
        Dimensions.y;

    v_occlusionUV =
        a_texcoord0;

    float dist =
        length(worldPos - ViewPosition.xyz);

    float fogStart =
        FogAndDistanceControl.x;

    float fogEnd =
        max(
            FogAndDistanceControl.y,
            fogStart + 0.0001
        );

    float fog =
        clamp(
            (dist - fogStart) /
            (fogEnd - fogStart),
            0.0,
            1.0
        );

    v_fog =
        vec4(FogColor.rgb, fog);
}
