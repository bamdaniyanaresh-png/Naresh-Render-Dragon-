$input a_position, a_color0, a_texcoord0
$output v_color0, v_texcoord0, v_worldPos

#include "bgfx_shader.sh"

uniform vec4 CloudColor;
uniform vec4 DistanceControl;

void main()
{
    vec4 world = mul(u_model[0], vec4(a_position, 1.0));

    gl_Position = mul(u_viewProj, vec4(world.xyz, 1.0));

    v_color0 = a_color0 * CloudColor;
    v_texcoord0 = a_texcoord0;
    v_worldPos = world.xyz;
}
