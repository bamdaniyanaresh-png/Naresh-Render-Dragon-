$input a_position, a_color0, a_texcoord0
$output v_clipPosition, v_color0, v_fog, v_light, v_texcoord0, v_worldPos

#include "bgfx_shader.sh"

uniform vec4 FogColor;
uniform vec4 FogControl;
uniform vec4 TileLightColor;
uniform vec4 OverlayColor;

void main()
{
    vec4 world = mul(u_model[0], a_position);

    gl_Position = mul(u_modelViewProj, a_position);

    v_clipPosition = gl_Position;
    v_color0 = a_color0;
    v_light = TileLightColor;
    v_texcoord0 = a_texcoord0;
    v_worldPos = world.xyz;

    float dist = length(world.xyz);
    float fog = clamp(
        (dist - FogControl.x) /
        max(FogControl.y - FogControl.x, 0.0001),
        0.0,
        1.0
    );

    v_fog = vec4(FogColor.rgb, fog);
}
